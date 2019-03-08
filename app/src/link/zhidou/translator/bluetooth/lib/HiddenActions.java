package link.zhidou.translator.bluetooth.lib;

/**
 * Date: 18-6-25
 * Time: 下午2:01
 * Email: lostsearover@gmail.com
 */
public interface HiddenActions {

    /**
     * Broadcast actions
     */
    String ACTION_DISAPPEARED = "android.bluetooth.device.action.DISAPPEARED";
    String ACTION_ALIAS_CHANGED = "android.bluetooth.device.action.ALIAS_CHANGED";
    String ACTION_PAIRING_CANCEL = "android.bluetooth.device.action.PAIRING_CANCEL";


    /**
     * constants
     */
    String EXTRA_REASON = "android.bluetooth.device.extra.REASON";


    /**
     * A bond attempt succeeded
     * @hide
     */
    public static final int BOND_SUCCESS = 0;

    /**
     * A bond attempt failed because pins did not match, or remote device did
     * not respond to pin request in time
     * @hide
     */
    public static final int UNBOND_REASON_AUTH_FAILED = 1;

    /**
     * A bond attempt failed because the other side explicitly rejected
     * bonding
     * @hide
     */
    public static final int UNBOND_REASON_AUTH_REJECTED = 2;

    /**
     * A bond attempt failed because we canceled the bonding process
     * @hide
     */
    public static final int UNBOND_REASON_AUTH_CANCELED = 3;

    /**
     * A bond attempt failed because we could not contact the remote device
     * @hide
     */
    public static final int UNBOND_REASON_REMOTE_DEVICE_DOWN = 4;

    /**
     * A bond attempt failed because a discovery is in progress
     * @hide
     */
    public static final int UNBOND_REASON_DISCOVERY_IN_PROGRESS = 5;

    /**
     * A bond attempt failed because of authentication timeout
     * @hide
     */
    public static final int UNBOND_REASON_AUTH_TIMEOUT = 6;

    /**
     * A bond attempt failed because of repeated attempts
     * @hide
     */
    public static final int UNBOND_REASON_REPEATED_ATTEMPTS = 7;

    /**
     * A bond attempt failed because we received an Authentication Cancel
     * by remote end
     * @hide
     */
    public static final int UNBOND_REASON_REMOTE_AUTH_CANCELED = 8;

    /**
     * An existing bond was explicitly revoked
     * @hide
     */
    public static final int UNBOND_REASON_REMOVED = 9;


    /**
     *  Default priority for devices that allow incoming
     * and outgoing connections for the profile
     * @hide
     **/
    public static final int PRIORITY_ON = 100;

    /**
     * Default priority for devices that does not allow incoming
     * connections and outgoing connections for the profile.
     * @hide
     **/
    public static final int PRIORITY_OFF = 0;

    /**
     * Default priority when not set or when the device is unpaired
     * @hide
     * */
    public static final int PRIORITY_UNDEFINED = -1;


    /** @hide */
    public static final int PROFILE_HEADSET = 0;
    /** @hide */
    public static final int PROFILE_A2DP = 1;
    /** @hide */
    public static final int PROFILE_OPP = 2;
    /** @hide */
    public static final int PROFILE_HID = 3;
    /** @hide */
    public static final int PROFILE_PANU = 4;
    /** @hide */
    public static final int PROFILE_NAP = 5;
    /** @hide */
    public static final int PROFILE_A2DP_SINK = 6;


    public static final int ACCESS_UNKNOWN = 0;

    public static final int ACCESS_ALLOWED = 1;


    public static final int ACCESS_REJECTED = 2;

    /**
     * No preferrence of physical transport for GATT connections to remote dual-mode devices
     */
    public static final int TRANSPORT_AUTO = 0;

    /**
     * Prefer BR/EDR transport for GATT connections to remote dual-mode devices
     */
    public static final int TRANSPORT_BREDR = 1;

    /**
     * Prefer LE transport for GATT connections to remote dual-mode devices
     */
    public static final int TRANSPORT_LE = 2;


    public static final String ACTION_CONNECTION_STATE_CHANGED =
            "android.bluetooth.map.profile.action.CONNECTION_STATE_CHANGED";

    public static final String EXTRA_LOCAL_ROLE = "android.bluetooth.pan.extra.LOCAL_ROLE";
}
