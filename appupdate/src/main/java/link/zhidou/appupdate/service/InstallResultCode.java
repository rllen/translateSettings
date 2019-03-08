package link.zhidou.appupdate.service;

/**
 * created by yue.gan 18-7-18
 */
public class InstallResultCode {

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} on success.
     *
     * 
     */
    public static final int INSTALL_SUCCEEDED = 1;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if the package is
     * already installed.
     *
     * 
     */
    public static final int INSTALL_FAILED_ALREADY_EXISTS = -1;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if the package archive
     * file is invalid.
     *
     * 
     */
    public static final int INSTALL_FAILED_INVALID_APK = -2;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if the URI passed in
     * is invalid.
     *
     * 
     */
    public static final int INSTALL_FAILED_INVALID_URI = -3;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if the package manager
     * service found that the device didn't have enough storage space to install the app.
     *
     * 
     */
    public static final int INSTALL_FAILED_INSUFFICIENT_STORAGE = -4;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if a
     * package is already installed with the same name.
     *
     * 
     */
    public static final int INSTALL_FAILED_DUPLICATE_PACKAGE = -5;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
     * the requested shared user does not exist.
     *
     * 
     */
    public static final int INSTALL_FAILED_NO_SHARED_USER = -6;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
     * a previously installed package of the same name has a different signature
     * than the new package (and the old package's data was not removed).
     *
     * 
     */
    public static final int INSTALL_FAILED_UPDATE_INCOMPATIBLE = -7;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
     * the new package is requested a shared user which is already installed on the
     * device and does not have matching signature.
     *
     * 
     */
    public static final int INSTALL_FAILED_SHARED_USER_INCOMPATIBLE = -8;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
     * the new package uses a shared library that is not available.
     *
     * 
     */
    public static final int INSTALL_FAILED_MISSING_SHARED_LIBRARY = -9;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
     * the new package uses a shared library that is not available.
     *
     * 
     */
    public static final int INSTALL_FAILED_REPLACE_COULDNT_DELETE = -10;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
     * the new package failed while optimizing and validating its dex files,
     * either because there was not enough storage or the validation failed.
     *
     * 
     */
    public static final int INSTALL_FAILED_DEXOPT = -11;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
     * the new package failed because the current SDK version is older than
     * that required by the package.
     * //        PackageHelper packageHelper = new PackageHelper(getContext());
    //        packageHelper.setOnInstalledListener(new PackageHelper.OnInstalledListener() {
    //            @Override
    //            public void onInstalled(PackageHelper.ActionResult actionResult) {
    //                LogUtils.d(TAG, "install success!");
    //                FileUtils.delete(apkFile.getParentFile(), null);
    //                AppUtils.startApp(getContext(), "link.zhidou.translator", "link.zhidou.translator.ui.activity.SplashActivity");
    //            }
    //        });
    //        packag
     */
    public static final int INSTALL_FAILED_OLDER_SDK = -12;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
     * the new package failed because it contains a content provider with the
     * same authority as a provider already installed in the system.
     * 
     */
    public static final int INSTALL_FAILED_CONFLICTING_PROVIDER = -13;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
     * the new package failed because the current SDK version is newer than
     * that required by the package.
     * 
     */
    public static final int INSTALL_FAILED_NEWER_SDK = -14;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
     * the new package failed because it has specified that it is a test-only
     * package and the caller has not supplied the { #INSTALL_ALLOW_TEST}
     * flag.
     * 
     */
    public static final int INSTALL_FAILED_TEST_ONLY = -15;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
     * the package being installed contains native code, but none that is
     * compatible with the the device's CPU_ABI.
     * 
     */
    public static final int INSTALL_FAILED_CPU_ABI_INCOMPATIBLE = -16;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
     * the new package uses a feature that is not available.
     * 
     */
    public static final int INSTALL_FAILED_MISSING_FEATURE = -17;

    // ------ Errors related to sdcard
    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
     * a secure container mount point couldn't be accessed on external media.
     * 
     */
    public static final int INSTALL_FAILED_CONTAINER_ERROR = -18;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
     * the new package couldn't be installed in the specified install
     * location.
     * 
     */
    public static final int INSTALL_FAILED_INVALID_INSTALL_LOCATION = -19;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
     * the new package couldn't be installed in the specified install
     * location because the media is not available.
     * 
     */
    public static final int INSTALL_FAILED_MEDIA_UNAVAILABLE = -20;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
     * the new package couldn't be installed because the verification timed out.
     * 
     */
    public static final int INSTALL_FAILED_VERIFICATION_TIMEOUT = -21;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
     * the new package couldn't be installed because the verification did not succeed.
     * 
     */
    public static final int INSTALL_FAILED_VERIFICATION_FAILURE = -22;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
     * the package changed from what the calling program expected.
     * 
     */
    public static final int INSTALL_FAILED_PACKAGE_CHANGED = -23;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
     * the new package is assigned a different UID than it previously held.
     * 
     */
    public static final int INSTALL_FAILED_UID_CHANGED = -24;

    /**
     * Installation return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)} if
     * the new package has an older version code than the currently installed package.
     * 
     */
    public static final int INSTALL_FAILED_VERSION_DOWNGRADE = -25;

    /**
     * Installation parse return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)}
     * if the parser was given a path that is not a file, or does not end with the expected
     * '.apk' extension.
     * 
     */
    public static final int INSTALL_PARSE_FAILED_NOT_APK = -100;

    /**
     * Installation parse return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)}
     * if the parser was unable to retrieve the AndroidManifest.xml file.
     * 
     */
    public static final int INSTALL_PARSE_FAILED_BAD_MANIFEST = -101;

    /**
     * Installation parse return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)}
     * if the parser encountered an unexpected exception.
     * 
     */
    public static final int INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION = -102;

    /**
     * Installation parse return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)}
     * if the parser did not find any certificates in the .apk.
     * 
     */
    public static final int INSTALL_PARSE_FAILED_NO_CERTIFICATES = -103;

    /**
     * Installation parse return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)}
     * if the parser found inconsistent certificates on the files in the .apk.
     * 
     */
    public static final int INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES = -104;

    /**
     * Installation parse return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)}
     * if the parser encountered a CertificateEncodingException in one of the
     * files in the .apk.
     * 
     */
    public static final int INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING = -105;

    /**
     * Installation parse return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)}
     * if the parser encountered a bad or missing package name in the manifest.
     * 
     */
    public static final int INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME = -106;

    /**
     * Installation parse return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)}
     * if the parser encountered a bad shared user id name in the manifest.
     * 
     */
    public static final int INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID = -107;

    /**
     * Installation parse return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)}
     * if the parser encountered some structural problem in the manifest.
     * 
     */
    public static final int INSTALL_PARSE_FAILED_MANIFEST_MALFORMED = -108;

    /**
     * Installation parse return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)}
     * if the parser did not find any actionable tags (instrumentation or application)
     * in the manifest.
     * 
     */
    public static final int INSTALL_PARSE_FAILED_MANIFEST_EMPTY = -109;

    /**
     * Installation failed return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)}
     * if the system failed to install the package because of system issues.
     * 
     */
    public static final int INSTALL_FAILED_INTERNAL_ERROR = -110;

    /**
     * Installation failed return code: this is passed to the { IPackageInstallObserver} by
     * { #installPackage(android.net.Uri, IPackageInstallObserver, int)}
     * if the system failed to install the package because the user is restricted from installing
     * apps.
     * 
     */
    public static final int INSTALL_FAILED_USER_RESTRICTED = -111;
}
