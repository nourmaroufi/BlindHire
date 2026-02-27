package Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * Windows Hello fingerprint / biometric authentication.
 *
 * Strategy: compile and run a tiny C# program on-the-fly using csc.exe
 * (the C# compiler, built into every Windows machine with .NET Framework).
 *
 * Why C# instead of PowerShell:
 *   PowerShell cannot handle WinRT IAsyncOperation bridging reliably —
 *   the Add-Type / AsTask reflection approach breaks on most machines.
 *   C# handles 'await' on WinRT APIs natively with no reflection tricks.
 *
 * csc.exe location (tried in order):
 *   - C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe  (most common)
 *   - C:\Windows\Microsoft.NET\Framework\v4.0.30319\csc.exe    (32-bit fallback)
 *   - Searched via where.exe if above not found
 */
public class FingerprintService {

    public enum Result {
        SUCCESS,
        NOT_AVAILABLE,
        NOT_CONFIGURED,
        FAILED,
        ERROR
    }

    // C# source — compiled to a temp .exe each call
    // Uses UserConsentVerifier.RequestVerificationAsync (Windows 10+)
    private static final String CS_SOURCE =
            "using System;\n" +
                    "using System.Threading.Tasks;\n" +
                    "using Windows.Security.Credentials.UI;\n" +
                    "\n" +
                    "class BlindHireFP {\n" +
                    "    static void Main(string[] args) {\n" +
                    "        string mode = args.Length > 0 ? args[0] : \"verify\";\n" +
                    "        string msg  = args.Length > 1 ? args[1] : \"BlindHire — Verify your identity\";\n" +
                    "        try {\n" +
                    "            if (mode == \"check\") {\n" +
                    "                var avail = Task.Run(async () =>\n" +
                    "                    await UserConsentVerifier.CheckAvailabilityAsync()).Result;\n" +
                    "                switch (avail) {\n" +
                    "                    case UserConsentVerifierAvailability.Available:\n" +
                    "                        Console.WriteLine(\"AVAILABLE\"); break;\n" +
                    "                    case UserConsentVerifierAvailability.DeviceNotPresent:\n" +
                    "                        Console.WriteLine(\"NOT_AVAILABLE\"); break;\n" +
                    "                    case UserConsentVerifierAvailability.NotConfiguredForUser:\n" +
                    "                        Console.WriteLine(\"NOT_CONFIGURED\"); break;\n" +
                    "                    default:\n" +
                    "                        Console.WriteLine(\"NOT_AVAILABLE\"); break;\n" +
                    "                }\n" +
                    "            } else {\n" +
                    "                var result = Task.Run(async () =>\n" +
                    "                    await UserConsentVerifier.RequestVerificationAsync(msg)).Result;\n" +
                    "                switch (result) {\n" +
                    "                    case UserConsentVerificationResult.Verified:\n" +
                    "                        Console.WriteLine(\"SUCCESS\"); break;\n" +
                    "                    case UserConsentVerificationResult.DeviceNotPresent:\n" +
                    "                        Console.WriteLine(\"NOT_AVAILABLE\"); break;\n" +
                    "                    case UserConsentVerificationResult.NotConfiguredForUser:\n" +
                    "                        Console.WriteLine(\"NOT_CONFIGURED\"); break;\n" +
                    "                    case UserConsentVerificationResult.Canceled:\n" +
                    "                        Console.WriteLine(\"FAILED\"); break;\n" +
                    "                    default:\n" +
                    "                        Console.WriteLine(\"FAILED\"); break;\n" +
                    "                }\n" +
                    "            }\n" +
                    "        } catch (Exception ex) {\n" +
                    "            Console.Error.WriteLine(ex.Message);\n" +
                    "            Console.WriteLine(\"ERROR\");\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n";

    // Cache the compiled exe path so we only compile once per app session
    private static volatile String cachedExePath = null;
    private static volatile boolean compileAttempted = false;

    // =========================================================================
    //  PUBLIC API
    // =========================================================================

    public static Result checkAvailability() {
        return run("check", "");
    }

    public static Result verify(String message) {
        return run("verify", message);
    }

    public static String getResultMessage(Result result) {
        switch (result) {
            case SUCCESS:        return "✅ Identity verified!";
            case NOT_AVAILABLE:  return "❌ Windows Hello not available on this device.";
            case NOT_CONFIGURED: return "⚠ Windows Hello not set up.\nGo to: Settings → Accounts → Sign-in options";
            case FAILED:         return "❌ Verification failed or was cancelled.";
            default:             return "❌ An error occurred. Please try again.";
        }
    }

    // =========================================================================
    //  INTERNAL
    // =========================================================================

    private static Result run(String mode, String message) {
        String exePath = getOrBuildExe();
        if (exePath == null) return Result.ERROR;

        try {
            ProcessBuilder pb = new ProcessBuilder(exePath, mode, message);
            pb.redirectErrorStream(false);
            Process proc = pb.start();

            String stdout = readStream(proc.getInputStream());
            String stderr = readStream(proc.getErrorStream());
            proc.waitFor(30, java.util.concurrent.TimeUnit.SECONDS);

            if (!stderr.isBlank())
                System.err.println("[Fingerprint] stderr: " + stderr.trim());

            System.out.println("[Fingerprint] output: " + stdout.trim());

            String out = stdout.trim();
            if (out.contains("SUCCESS"))        return Result.SUCCESS;
            if (out.contains("NOT_CONFIGURED")) return Result.NOT_CONFIGURED;
            if (out.contains("NOT_AVAILABLE"))  return Result.NOT_AVAILABLE;
            if (out.contains("FAILED"))         return Result.FAILED;
            return Result.ERROR;

        } catch (Exception e) {
            System.err.println("[Fingerprint] run error: " + e.getMessage());
            return Result.ERROR;
        }
    }

    /**
     * Compiles the C# source to a temp .exe on first call, caches the path.
     * Returns null if csc.exe is not found or compilation fails.
     */
    private static synchronized String getOrBuildExe() {
        if (cachedExePath != null) return cachedExePath;
        if (compileAttempted) return null; // already failed — don't retry
        compileAttempted = true;

        String cscPath = findCsc();
        if (cscPath == null) {
            System.err.println("[Fingerprint] csc.exe not found — fingerprint unavailable.");
            return null;
        }

        try {
            // Write C# source to temp file
            File csFile  = File.createTempFile("BlindHireFP_", ".cs");
            File exeFile = new File(csFile.getParent(),
                    csFile.getName().replace(".cs", ".exe"));
            csFile.deleteOnExit();
            exeFile.deleteOnExit();

            Files.writeString(csFile.toPath(), CS_SOURCE, StandardCharsets.UTF_8);

            // Compile — reference Windows Runtime winmd for WinRT types
            String winmdPath = findWindowsRuntimeWinmd();
            ProcessBuilder pb;
            if (winmdPath != null) {
                pb = new ProcessBuilder(
                        cscPath,
                        "/nologo",
                        "/platform:x64",
                        "/out:" + exeFile.getAbsolutePath(),
                        "/reference:" + winmdPath,
                        csFile.getAbsolutePath()
                );
            } else {
                // Try without explicit winmd — works on some machines
                pb = new ProcessBuilder(
                        cscPath,
                        "/nologo",
                        "/platform:x64",
                        "/out:" + exeFile.getAbsolutePath(),
                        csFile.getAbsolutePath()
                );
            }

            pb.redirectErrorStream(true);
            Process proc = pb.start();
            String output = readStream(proc.getInputStream());
            int exitCode = proc.waitFor();

            if (exitCode != 0 || !exeFile.exists()) {
                System.err.println("[Fingerprint] Compile failed (exit " + exitCode + "):\n" + output);
                // Try again with explicit winmd path searched differently
                return tryCompileWithSystemWinmd(cscPath, csFile, exeFile);
            }

            System.out.println("[Fingerprint] ✅ Compiled to: " + exeFile.getAbsolutePath());
            cachedExePath = exeFile.getAbsolutePath();
            return cachedExePath;

        } catch (Exception e) {
            System.err.println("[Fingerprint] Compile exception: " + e.getMessage());
            return null;
        }
    }

    /**
     * Second compile attempt — searches System32 and WindowsApps for the winmd.
     */
    private static String tryCompileWithSystemWinmd(String cscPath, File csFile, File exeFile) {
        try {
            // Windows.winmd is in C:\Windows\System32\WinMetadata\
            File winmetaDir = new File("C:\\Windows\\System32\\WinMetadata");
            if (!winmetaDir.exists()) return null;

            // Collect all .winmd files in WinMetadata
            File[] winmds = winmetaDir.listFiles(
                    f -> f.getName().endsWith(".winmd"));
            if (winmds == null || winmds.length == 0) return null;

            // Build /reference: args for all winmd files
            java.util.List<String> cmd = new java.util.ArrayList<>();
            cmd.add(cscPath);
            cmd.add("/nologo");
            cmd.add("/platform:x64");
            cmd.add("/out:" + exeFile.getAbsolutePath());
            for (File w : winmds)
                cmd.add("/reference:" + w.getAbsolutePath());
            cmd.add(csFile.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            String output = readStream(proc.getInputStream());
            int exitCode = proc.waitFor();

            if (exitCode == 0 && exeFile.exists()) {
                System.out.println("[Fingerprint] ✅ Compiled (WinMetadata): " + exeFile);
                cachedExePath = exeFile.getAbsolutePath();
                return cachedExePath;
            }
            System.err.println("[Fingerprint] Second compile attempt failed:\n" + output);
        } catch (Exception e) {
            System.err.println("[Fingerprint] Second compile exception: " + e.getMessage());
        }
        return null;
    }

    // =========================================================================
    //  HELPER — find csc.exe
    // =========================================================================

    private static String findCsc() {
        // Most common .NET Framework 4.x paths
        String[] candidates = {
                "C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\csc.exe",
                "C:\\Windows\\Microsoft.NET\\Framework\\v4.0.30319\\csc.exe",
                "C:\\Windows\\Microsoft.NET\\Framework64\\v3.5\\csc.exe",
                "C:\\Windows\\Microsoft.NET\\Framework\\v3.5\\csc.exe",
        };
        for (String path : candidates) {
            if (new File(path).exists()) {
                System.out.println("[Fingerprint] Found csc.exe: " + path);
                return path;
            }
        }
        // Try 'where csc' as last resort
        try {
            Process p = new ProcessBuilder("where", "csc").start();
            String out = readStream(p.getInputStream()).trim();
            p.waitFor();
            if (!out.isBlank()) {
                String first = out.split("\\r?\\n")[0].trim();
                if (new File(first).exists()) return first;
            }
        } catch (Exception ignored) {}

        return null;
    }

    // =========================================================================
    //  HELPER — find Windows.winmd for WinRT types
    // =========================================================================

    private static String findWindowsRuntimeWinmd() {
        // Standard location on Windows 10/11
        String[] candidates = {
                "C:\\Program Files (x86)\\Windows Kits\\10\\UnionMetadata\\Windows.winmd",
                "C:\\Program Files\\Windows Kits\\10\\UnionMetadata\\Windows.winmd",
        };
        for (String p : candidates) {
            if (new File(p).exists()) return p;
        }
        // Try searching Windows Kits for any Windows.winmd
        File wk = new File("C:\\Program Files (x86)\\Windows Kits\\10\\UnionMetadata");
        if (!wk.exists()) wk = new File("C:\\Program Files\\Windows Kits\\10\\UnionMetadata");
        if (wk.isDirectory()) {
            File[] subs = wk.listFiles(File::isDirectory);
            if (subs != null) {
                for (File sub : subs) {
                    File winmd = new File(sub, "Windows.winmd");
                    if (winmd.exists()) return winmd.getAbsolutePath();
                }
            }
        }
        return null;
    }

    // =========================================================================
    //  HELPER
    // =========================================================================

    private static String readStream(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append("\n");
        return sb.toString();
    }
}