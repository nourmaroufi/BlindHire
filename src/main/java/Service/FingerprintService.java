package Service;

import com.sun.jna.*;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.*;
import com.sun.jna.win32.*;

/**
 * Windows Hello biometric authentication using JNA (Java Native Access).
 *
 * Free, no external tools, no dotnet, no PowerShell — pure Java + JNA.
 * JNA is Apache 2.0 licensed and available on Maven Central.
 *
 * How it works:
 *   Calls CredUIPromptForWindowsCredentialsW from credui.dll with the
 *   CREDUIWIN_SECURE_PROMPT | CREDUIWIN_AUTHPACKAGE_ONLY flags.
 *   This triggers the native Windows Security dialog which uses whatever
 *   Windows Hello method the user has configured: fingerprint, face, or PIN.
 *
 * What this does NOT do:
 *   It does not store or read fingerprint data — Windows handles that entirely.
 *   We just ask "is this person who they say they are?" and get YES or NO.
 *
 * Add to pom.xml:
 *   <dependency>
 *     <groupId>net.java.dev.jna</groupId>
 *     <artifactId>jna</artifactId>
 *     <version>5.14.0</version>
 *   </dependency>
 *   <dependency>
 *     <groupId>net.java.dev.jna</groupId>
 *     <artifactId>jna-platform</artifactId>
 *     <version>5.14.0</version>
 *   </dependency>
 */
public class FingerprintService {

    public enum Result {
        SUCCESS,
        NOT_AVAILABLE,
        NOT_CONFIGURED,
        FAILED,
        ERROR
    }

    // ── credui.dll flags ──────────────────────────────────────────────────────
    private static final int CREDUIWIN_GENERIC          = 0x00000001;
    private static final int CREDUIWIN_SECURE_PROMPT    = 0x00001000; // uses secure desktop
    private static final int CREDUIWIN_AUTHPACKAGE_ONLY = 0x00000010; // only show auth packages
    private static final int CREDUIWIN_IN_CRED_ONLY     = 0x00000020;

    // ── Error codes from credui.dll ───────────────────────────────────────────
    private static final int ERROR_SUCCESS              = 0;
    private static final int ERROR_CANCELLED            = 1223;
    private static final int ERROR_NO_SUCH_LOGON_SESSION = 1312;

    // ── Negotiate auth package ID (cached after first lookup) ─────────────────
    private static volatile int negotiatePackageId = -1;

    // =========================================================================
    //  JNA interface for credui.dll
    // =========================================================================

    interface CredUI extends StdCallLibrary {
        CredUI INSTANCE = Native.load("credui", CredUI.class);

        /**
         * CredUIPromptForWindowsCredentialsW
         * Shows the Windows credential / Windows Hello UI.
         */
        int CredUIPromptForWindowsCredentialsW(
                CREDUI_INFO pUiInfo,         // UI customization (title, message)
                int         dwAuthError,     // 0 = no prior error
                IntByReference pulAuthPackage,
                Pointer     pvInAuthBuffer,  // null on first call
                int         ulInAuthBufferSize,
                PointerByReference ppvOutAuthBuffer,
                IntByReference pulOutAuthBufferSize,
                IntByReference pfSave,
                int         dwFlags
        );

        /** Free the output buffer returned by CredUIPromptForWindowsCredentialsW */
        void CoTaskMemFree(Pointer pv);
    }

    interface Secur32Ext extends StdCallLibrary {
        Secur32Ext INSTANCE = Native.load("secur32", Secur32Ext.class);

        /**
         * AcquireCredentialsHandleW — used to get the Negotiate package ID
         * which enables Windows Hello (biometric) in the credential dialog.
         */
        int LsaLookupAuthenticationPackage(
                Pointer      LsaHandle,
                LSA_STRING   PackageName,
                IntByReference AuthenticationPackage
        );

        int LsaConnectUntrusted(PointerByReference LsaHandle);
        int LsaDeregisterLogonProcess(Pointer LsaHandle);
    }

    // CREDUI_INFO structure
    public static class CREDUI_INFO extends Structure {
        public int     cbSize;
        public WinDef.HWND hwndParent;
        public String  pszMessageText;
        public String  pszCaptionText;
        public WinDef.HBITMAP hbmBanner;

        @Override
        protected java.util.List<String> getFieldOrder() {
            return java.util.Arrays.asList(
                    "cbSize", "hwndParent", "pszMessageText", "pszCaptionText", "hbmBanner");
        }

        public CREDUI_INFO(String caption, String message) {
            cbSize = size();
            pszCaptionText = caption;
            pszMessageText = message;
            hwndParent = null;
            hbmBanner  = null;
        }
    }

    // LSA_STRING structure for LsaLookupAuthenticationPackage
    public static class LSA_STRING extends Structure {
        public short  Length;
        public short  MaximumLength;
        public String Buffer;

        @Override
        protected java.util.List<String> getFieldOrder() {
            return java.util.Arrays.asList("Length", "MaximumLength", "Buffer");
        }

        public LSA_STRING(String s) {
            Buffer        = s;
            Length        = (short) s.length();
            MaximumLength = (short) (s.length() + 1);
        }
    }

    // =========================================================================
    //  PUBLIC API
    // =========================================================================

    /**
     * Checks whether the Windows credential dialog is available.
     * Always returns SUCCESS on Windows 10/11 — the dialog is always present.
     * Returns NOT_AVAILABLE if not running on Windows.
     */
    public static Result checkAvailability() {
        if (!isWindows()) return Result.NOT_AVAILABLE;
        try {
            // Quick sanity check — just verify credui.dll loads
            CredUI.INSTANCE.toString();
            return Result.SUCCESS;
        } catch (UnsatisfiedLinkError e) {
            return Result.NOT_AVAILABLE;
        }
    }

    /**
     * Shows the Windows Hello / Windows Security verification dialog.
     * Blocks until the user verifies or cancels.
     */
    public static Result verify(String message) {
        if (!isWindows()) return Result.NOT_AVAILABLE;
        try {
            return showWindowsHelloDialog(message);
        } catch (UnsatisfiedLinkError e) {
            System.err.println("[Fingerprint] JNA link error: " + e.getMessage());
            return Result.NOT_AVAILABLE;
        } catch (Exception e) {
            System.err.println("[Fingerprint] Error: " + e.getMessage());
            return Result.ERROR;
        }
    }

    public static String getResultMessage(Result r) {
        return switch (r) {
            case SUCCESS        -> "✅ Identity verified!";
            case NOT_AVAILABLE  -> "❌ Windows Hello not available on this device.";
            case NOT_CONFIGURED -> "⚠ Windows Hello not set up.\nGo to: Settings → Accounts → Sign-in options";
            case FAILED         -> "❌ Verification failed or was cancelled.";
            default             -> "❌ An error occurred. Please try again.";
        };
    }

    // =========================================================================
    //  CORE — show the Windows Hello dialog via credui.dll
    // =========================================================================

    private static Result showWindowsHelloDialog(String message) {
        CREDUI_INFO uiInfo = new CREDUI_INFO("BlindHire", message);

        IntByReference    authPackage      = new IntByReference(getWindowsHelloPackageId());
        PointerByReference outBuffer       = new PointerByReference();
        IntByReference    outBufferSize    = new IntByReference();
        IntByReference    save             = new IntByReference(0);

        // CREDUIWIN_SECURE_PROMPT  → shows on the secure desktop (same as UAC)
        // CREDUIWIN_AUTHPACKAGE_ONLY → shows only biometric / PIN options, not password
        int flags = CREDUIWIN_SECURE_PROMPT | CREDUIWIN_AUTHPACKAGE_ONLY;

        int result = CredUI.INSTANCE.CredUIPromptForWindowsCredentialsW(
                uiInfo,
                0,
                authPackage,
                null, 0,
                outBuffer, outBufferSize,
                save,
                flags
        );

        // Free the output buffer immediately — we don't need the credentials
        if (outBuffer.getValue() != null) {
            CredUI.INSTANCE.CoTaskMemFree(outBuffer.getValue());
        }

        System.out.println("[Fingerprint] CredUI result code: " + result);

        if (result == ERROR_SUCCESS)   return Result.SUCCESS;
        if (result == ERROR_CANCELLED) return Result.FAILED;

        // If AUTHPACKAGE_ONLY fails (no biometric package), fall back to generic prompt
        if (result != ERROR_SUCCESS) {
            return showGenericDialog(message);
        }

        return Result.FAILED;
    }

    /**
     * Fallback: generic Windows credential prompt (shows password field).
     * Used when no biometric package is available.
     */
    private static Result showGenericDialog(String message) {
        CREDUI_INFO uiInfo = new CREDUI_INFO("BlindHire — Verify Identity", message);

        IntByReference    authPackage   = new IntByReference(0);
        PointerByReference outBuffer    = new PointerByReference();
        IntByReference    outBufferSize = new IntByReference();
        IntByReference    save          = new IntByReference(0);

        int result = CredUI.INSTANCE.CredUIPromptForWindowsCredentialsW(
                uiInfo, 0,
                authPackage,
                null, 0,
                outBuffer, outBufferSize,
                save,
                CREDUIWIN_GENERIC | CREDUIWIN_SECURE_PROMPT
        );

        if (outBuffer.getValue() != null)
            CredUI.INSTANCE.CoTaskMemFree(outBuffer.getValue());

        System.out.println("[Fingerprint] Generic CredUI result: " + result);
        return result == ERROR_SUCCESS ? Result.SUCCESS : Result.FAILED;
    }

    /**
     * Gets the Windows Hello / Negotiate auth package ID.
     * This makes the dialog show biometric options (fingerprint, face, PIN)
     * instead of a generic username/password prompt.
     */
    private static int getWindowsHelloPackageId() {
        if (negotiatePackageId >= 0) return negotiatePackageId;

        try {
            PointerByReference lsaHandle = new PointerByReference();
            int status = Secur32Ext.INSTANCE.LsaConnectUntrusted(lsaHandle);
            if (status != 0) return 0;

            LSA_STRING pkgName = new LSA_STRING("MICROSOFT_AUTHENTICATION_PACKAGE_V1_0");
            IntByReference pkgId = new IntByReference();
            Secur32Ext.INSTANCE.LsaLookupAuthenticationPackage(
                    lsaHandle.getValue(), pkgName, pkgId);
            Secur32Ext.INSTANCE.LsaDeregisterLogonProcess(lsaHandle.getValue());

            negotiatePackageId = pkgId.getValue();
            System.out.println("[Fingerprint] Auth package ID: " + negotiatePackageId);
        } catch (Exception e) {
            System.err.println("[Fingerprint] Could not get package ID: " + e.getMessage());
            negotiatePackageId = 0;
        }
        return negotiatePackageId;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("windows");
    }
}