package Service;

import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.*;
import org.bytedeco.opencv.opencv_face.*;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_face.*;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Base64;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Face capture and recognition using:
 *  - Haar cascade for detection
 *  - LBPH (Local Binary Pattern Histogram) for recognition
 *
 * WHY LBPH instead of pixel diff:
 *   Pixel diff (absdiff) just compares brightness values — it will accept
 *   any two faces with similar lighting or skin tone as a match.
 *   LBPH extracts texture micro-patterns unique to each face and gives a
 *   proper confidence score. Threshold ~80 means "same person".
 *
 * HOW STORAGE WORKS:
 *   - At signup: capture one face → encode as Base64 and store in DB (unchanged)
 *   - At login:  load stored face → train a single-sample LBPH model →
 *                capture live face → predict → check confidence score
 *
 * LBPH confidence: lower = better match. Typical values:
 *   0–50   → very confident same person
 *   50–80  → likely same person (lighting/angle variation)
 *   80+    → different person
 */
public class FaceService {

    // LBPH confidence threshold — below this = same person
    // 80.0 is a good balance: strict enough to reject others,
    // lenient enough to handle lighting/angle changes
    private static final double LBPH_THRESHOLD = 65.0;
    private static final int    FACE_SIZE      = 200; // larger = more detail for LBPH

    private OpenCVFrameGrabber         grabber;
    private OpenCVFrameConverter.ToMat converter;
    private CascadeClassifier          faceDetector;
    private volatile boolean           running   = false;

    private Mat    lastFrame = null;
    private final Object frameLock = new Object();

    // =========================================================================
    //  LIFECYCLE
    // =========================================================================

    public void startCamera() throws Exception {
        grabber = new OpenCVFrameGrabber(0);
        grabber.setImageWidth(640);
        grabber.setImageHeight(480);
        grabber.start();
        converter    = new OpenCVFrameConverter.ToMat();
        faceDetector = loadCascade();
        running      = true;
    }

    public void stopCamera() {
        running = false;
        synchronized (frameLock) {
            if (lastFrame != null) { lastFrame.release(); lastFrame = null; }
        }
        if (grabber != null) {
            try { grabber.stop(); grabber.release(); }
            catch (Exception ignored) {}
        }
    }

    public boolean isRunning() { return running; }

    // =========================================================================
    //  FRAME GRAB  (preview loop — stores last good frame for capture)
    // =========================================================================

    public Image grabFrameAsImage() {
        if (!running || grabber == null) return null;
        try {
            Frame frame = grabber.grab();
            if (frame == null || frame.image == null) return null;
            Mat mat = converter.convert(frame);
            if (mat == null || mat.empty()) return null;

            synchronized (frameLock) {
                if (lastFrame != null) lastFrame.release();
                lastFrame = mat.clone();
            }

            drawFaceBoxes(mat);
            BufferedImage bi = toBufImg(mat);
            mat.release();
            return bi != null ? SwingFXUtils.toFXImage(bi, null) : null;
        } catch (Exception e) {
            return null;
        }
    }

    // =========================================================================
    //  CAPTURE SNAPSHOT  (signup — returns Base64 of cropped grayscale face)
    // =========================================================================

    public String captureFaceSnapshot() throws Exception {
        if (!running || grabber == null)
            throw new Exception("Camera is not running.");
        if (faceDetector == null || faceDetector.empty())
            throw new Exception("Face detector failed to load — check console.");

        Mat mat = getLastFrameCopy();

        try {
            Rect safe = getSingleFaceRect(mat);
            Mat  gray = extractFaceMat(mat, safe);
            String b64 = toBase64(gray);
            gray.release();
            return b64;
        } finally {
            mat.release();
        }
    }

    // =========================================================================
    //  COMPARE  (login — LBPH recognition)
    // =========================================================================

    /**
     * Trains a single-sample LBPH model on the stored face, then predicts
     * against the live face. Returns true if confidence < LBPH_THRESHOLD.
     *
     * Single-sample LBPH is not as robust as multi-sample, but works well
     * for this use case (one registered photo per user).
     */
    public boolean compareFace(String storedBase64) throws Exception {
        if (storedBase64 == null || storedBase64.isEmpty())
            throw new Exception("No face registered for this account.");

        // Decode the stored face
        Mat storedFace = base64ToGrayMat(storedBase64);
        if (storedFace == null)
            throw new Exception("Could not decode stored face data.");

        // Capture a fresh live face
        String liveFaceB64 = captureFaceSnapshot();
        Mat    liveFace    = base64ToGrayMat(liveFaceB64);
        if (liveFace == null) {
            storedFace.release();
            throw new Exception("Could not capture live face.");
        }

        try {
            // ── Train LBPH on the single stored sample ────────────────────────
            // We use label 0 for "registered user"
            MatVector trainImages = new MatVector(1);
            trainImages.put(0, storedFace);

            Mat trainLabels = new Mat(1, 1, CV_32SC1);
            trainLabels.ptr(0, 0).putInt(0);

            LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create(
                    1,    // radius
                    8,    // neighbors
                    8,    // grid_x
                    8,    // grid_y
                    LBPH_THRESHOLD * 2  // internal threshold — we check manually below
            );
            recognizer.train(trainImages, trainLabels);

            // ── Predict ───────────────────────────────────────────────────────
            int[]    label      = {-1};
            double[] confidence = {Double.MAX_VALUE};
            recognizer.predict(liveFace, label, confidence);

            System.out.printf("[FaceService] LBPH confidence=%.2f  threshold=%.2f  match=%b%n",
                    confidence[0], LBPH_THRESHOLD, confidence[0] <= LBPH_THRESHOLD);

            return confidence[0] <= LBPH_THRESHOLD;

        } finally {
            storedFace.release();
            liveFace.release();
        }
    }

    // =========================================================================
    //  PRIVATE — face extraction helpers
    // =========================================================================

    private Mat getLastFrameCopy() throws Exception {
        synchronized (frameLock) {
            if (lastFrame == null || lastFrame.empty())
                throw new Exception("No preview frame yet — wait for the camera to start.");
            return lastFrame.clone();
        }
    }

    /**
     * Detects exactly one face in the frame, clamps the rect to bounds,
     * and returns the safe Rect. Throws user-friendly exceptions otherwise.
     */
    private Rect getSingleFaceRect(Mat mat) throws Exception {
        Rect[] faces = detectFaces(mat);

        if (faces.length == 0)
            throw new Exception(
                    "No face detected. Try:\n" +
                            "  • Face the camera directly\n" +
                            "  • Sit 50-70 cm from the screen\n" +
                            "  • Ensure your face is well-lit\n" +
                            "  • Remove hat or glasses"
            );
        if (faces.length > 1)
            throw new Exception("Multiple faces detected — only one person should be visible.");

        // Clamp rect to Mat boundaries (prevents OpenCV assertion crash)
        Rect raw  = faces[0];
        int  imgW = mat.cols();
        int  imgH = mat.rows();
        int  x    = Math.max(0, raw.x());
        int  y    = Math.max(0, raw.y());
        int  w    = Math.min(raw.width(),  imgW - x);
        int  h    = Math.min(raw.height(), imgH - y);

        if (w <= 0 || h <= 0)
            throw new Exception("Detected face region is invalid — please try again.");

        return new Rect(x, y, w, h);
    }

    /**
     * Crops the face region from the frame, resizes to FACE_SIZE × FACE_SIZE,
     * and converts to grayscale. Returns a new Mat (caller must release).
     */
    private Mat extractFaceMat(Mat frame, Rect faceRect) {
        Mat cropped = new Mat(frame, faceRect);
        Mat resized = new Mat();
        resize(cropped, resized, new Size(FACE_SIZE, FACE_SIZE));
        Mat gray = new Mat();
        cvtColor(resized, gray, COLOR_BGR2GRAY);
        // Apply histogram equalization to normalize lighting
        equalizeHist(gray, gray);
        cropped.release();
        resized.release();
        return gray;
    }

    /**
     * Decodes a Base64 face string (stored at signup) back to a grayscale Mat.
     * Used for LBPH training/comparison at login.
     */
    private Mat base64ToGrayMat(String b64) {
        try {
            byte[]        bytes = Base64.getDecoder().decode(b64);
            BufferedImage bi    = ImageIO.read(new ByteArrayInputStream(bytes));
            if (bi == null) return null;

            Java2DFrameConverter       jc = new Java2DFrameConverter();
            OpenCVFrameConverter.ToMat oc = new OpenCVFrameConverter.ToMat();
            Mat mat = oc.convert(jc.convert(bi));
            if (mat == null || mat.empty()) return null;

            Mat gray = new Mat();
            if (mat.channels() > 1) cvtColor(mat, gray, COLOR_BGR2GRAY);
            else mat.copyTo(gray);

            Mat resized = new Mat();
            resize(gray, resized, new Size(FACE_SIZE, FACE_SIZE));
            // Normalize lighting — important for consistent comparison
            equalizeHist(resized, resized);

            mat.release();
            gray.release();
            return resized;
        } catch (Exception e) {
            System.err.println("[FaceService] base64ToGrayMat: " + e.getMessage());
            return null;
        }
    }

    // =========================================================================
    //  CASCADE LOADING  (scan every JAR on the classpath — unchanged)
    // =========================================================================

    private static final String[] CASCADE_NAMES = {
            "haarcascade_frontalface_alt.xml",
            "haarcascade_frontalface_alt2.xml",
            "haarcascade_frontalface_default.xml"
    };

    private CascadeClassifier loadCascade() {
        System.out.println("[FaceService] Scanning classpath JARs for cascade XML...");
        CascadeClassifier cc = scanClasspathJars();
        if (cc != null) return cc;

        cc = scanDirectory(new File(System.getProperty("java.io.tmpdir")));
        if (cc != null) return cc;

        for (String dir : System.getProperty("java.library.path", "").split(File.pathSeparator)) {
            cc = scanDirectory(new File(dir));
            if (cc != null) return cc;
        }

        try {
            java.lang.reflect.Method m =
                    org.bytedeco.javacpp.Loader.class.getMethod("getCacheDir");
            File cacheDir = (File) m.invoke(null);
            if (cacheDir != null) {
                cc = scanDirectoryRecursive(cacheDir, 2);
                if (cc != null) return cc;
            }
        } catch (Exception ignored) {}

        System.err.println("[FaceService] ❌ Face detector could not be loaded.");
        return new CascadeClassifier();
    }

    private CascadeClassifier scanClasspathJars() {
        Set<URL> urls = new LinkedHashSet<>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        while (cl != null) {
            if (cl instanceof URLClassLoader)
                for (URL u : ((URLClassLoader) cl).getURLs()) urls.add(u);
            cl = cl.getParent();
        }
        if (ClassLoader.getSystemClassLoader() instanceof URLClassLoader)
            for (URL u : ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs())
                urls.add(u);
        for (String e : System.getProperty("java.class.path", "").split(File.pathSeparator)) {
            try { urls.add(new File(e).toURI().toURL()); } catch (Exception ignored) {}
        }
        for (URL url : urls) {
            if (!url.getFile().endsWith(".jar")) continue;
            File jar = new File(url.getFile());
            if (!jar.exists()) continue;
            CascadeClassifier cc = extractCascadeFromJar(jar);
            if (cc != null) return cc;
        }
        return null;
    }

    private CascadeClassifier extractCascadeFromJar(File jarFile) {
        try (ZipFile zip = new ZipFile(jarFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                for (String name : CASCADE_NAMES) {
                    if (entry.getName().endsWith(name)) {
                        File tmp = File.createTempFile("blindhire_cc_", ".xml");
                        tmp.deleteOnExit();
                        try (InputStream in = zip.getInputStream(entry);
                             FileOutputStream out = new FileOutputStream(tmp)) {
                            byte[] buf = new byte[8192]; int len;
                            while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
                        }
                        if (tmp.length() < 1024) continue;
                        CascadeClassifier cc = new CascadeClassifier();
                        if (cc.load(tmp.getAbsolutePath())) {
                            System.out.println("[FaceService] ✅ Loaded: "
                                    + jarFile.getName() + " → " + entry.getName());
                            return cc;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private CascadeClassifier scanDirectory(File dir) {
        return scanDirectoryRecursive(dir, 0);
    }

    private CascadeClassifier scanDirectoryRecursive(File dir, int depth) {
        if (dir == null || !dir.isDirectory()) return null;
        File[] files = dir.listFiles();
        if (files == null) return null;
        for (File f : files) {
            if (f.isFile()) {
                for (String name : CASCADE_NAMES) {
                    if (f.getName().equals(name) && f.length() > 1024) {
                        CascadeClassifier cc = new CascadeClassifier();
                        if (cc.load(f.getAbsolutePath())) {
                            System.out.println("[FaceService] ✅ Loaded from file: " + f);
                            return cc;
                        }
                    }
                }
            } else if (f.isDirectory() && depth > 0) {
                CascadeClassifier cc = scanDirectoryRecursive(f, depth - 1);
                if (cc != null) return cc;
            }
        }
        return null;
    }

    // =========================================================================
    //  DETECTION
    // =========================================================================

    private Rect[] detectFaces(Mat frame) {
        if (faceDetector == null || faceDetector.empty()) return new Rect[0];
        Mat gray = new Mat();
        cvtColor(frame, gray, COLOR_BGR2GRAY);
        equalizeHist(gray, gray);
        RectVector out = new RectVector();
        faceDetector.detectMultiScale(gray, out, 1.05, 3, 0, new Size(50, 50), new Size());
        gray.release();
        Rect[] rects = new Rect[(int) out.size()];
        for (int i = 0; i < rects.length; i++) rects[i] = out.get(i);
        return rects;
    }

    private void drawFaceBoxes(Mat frame) {
        for (Rect r : detectFaces(frame))
            rectangle(frame, r, new Scalar(0, 255, 0, 0), 2, 8, 0);
    }

    // =========================================================================
    //  IMAGE HELPERS
    // =========================================================================

    private BufferedImage toBufImg(Mat mat) {
        try {
            OpenCVFrameConverter.ToMat c  = new OpenCVFrameConverter.ToMat();
            Java2DFrameConverter       jc = new Java2DFrameConverter();
            return jc.convert(c.convert(mat));
        } catch (Exception e) { return null; }
    }

    private String toBase64(Mat mat) throws Exception {
        BufferedImage bi = toBufImg(mat);
        if (bi == null) throw new Exception("Could not convert face to image.");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, "jpg", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}