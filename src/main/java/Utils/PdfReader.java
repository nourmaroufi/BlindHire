package Utils;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;

/**
 * Extracts raw text from a PDF file using Apache PDFBox 3.x.
 *
 * NOTE: PDFBox 3.x replaced PDDocument.load(File) with Loader.loadPDF(File).
 *
 * ── HOW TO ADD PDFBOX TO YOUR PROJECT ────────────────────────────────────────
 *  Maven — add to pom.xml:
 *    <dependency>
 *      <groupId>org.apache.pdfbox</groupId>
 *      <artifactId>pdfbox</artifactId>
 *      <version>3.0.2</version>
 *    </dependency>
 *
 *  Gradle — add to build.gradle:
 *    implementation 'org.apache.pdfbox:pdfbox:3.0.2'
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class PdfReader {

    /**
     * Reads all text content from a PDF file.
     *
     * @param file  the PDF file to read
     * @return      the full extracted text, or null if the file can't be read
     */
    public static String extractText(File file) {
        if (file == null || !file.exists()) return null;

        try (PDDocument doc = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            return text.trim().isEmpty() ? null : text;
        } catch (Exception e) {
            System.err.println("[PdfReader] Could not read PDF: " + e.getMessage());
            return null;
        }
    }
}