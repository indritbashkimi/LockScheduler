package it.ibashkimi.lockscheduler.domain;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

import static it.ibashkimi.lockscheduler.domain.LockMode.LockType.FINGERPRINT;
import static it.ibashkimi.lockscheduler.domain.LockMode.LockType.PASSWORD;
import static it.ibashkimi.lockscheduler.domain.LockMode.LockType.PIN;
import static it.ibashkimi.lockscheduler.domain.LockMode.LockType.SEQUENCE;
import static it.ibashkimi.lockscheduler.domain.LockMode.LockType.SWIPE;
import static it.ibashkimi.lockscheduler.domain.LockMode.LockType.UNCHANGED;


public class LockMode implements Parcelable {

    @IntDef({FINGERPRINT, PIN, PASSWORD, SEQUENCE, SWIPE, UNCHANGED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LockType {
        int PIN = 0;
        int PASSWORD = 1;
        int SEQUENCE = 2;
        int SWIPE = 3;
        int UNCHANGED = 4;
        int FINGERPRINT = 5;
    }

    @LockType
    private int lockType;
    private String pin;
    private String password;

    public LockMode(@LockType int lockType) {
        this.lockType = lockType;
        this.pin = "";
        this.password = "";
    }

    public String getPin() {
        return pin;
    }

    public String getPassword() {
        return password;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @LockMode.LockType
    public int getLockType() {
        return lockType;
    }

    public void setLockType(@LockType int lockType) {
        this.lockType = lockType;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "LockMode{%s}", lockTypeToString(lockType));
    }

    public static String lockTypeToString(@LockType int lockType) {
        switch (lockType) {
            case UNCHANGED:
                return "UNCHANGED";
            case LockType.PASSWORD:
                return "PASSWORD";
            case LockType.PIN:
                return "PIN";
            case LockType.SEQUENCE:
                return "SEQUENCE";
            case LockType.SWIPE:
                return "SWIPE";
            case FINGERPRINT:
                return "FINGERPRINT";
            default:
                return "UNKNOWN";
        }
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("lockType", "" + lockType);
            jsonObject.put("pin", pin);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    public static LockMode parseJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        @LockType int lockType = Integer.parseInt(jsonObject.getString("lockType"));
        LockMode lockMode = new LockMode(lockType);
        lockMode.setPin(jsonObject.getString("pin"));
        lockMode.setPassword(jsonObject.getString("password"));
        return lockMode;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(lockType);
        dest.writeString(pin);
        dest.writeString(password);
    }

    public static final Parcelable.Creator<LockMode> CREATOR = new Parcelable.Creator<LockMode>() {
        public LockMode createFromParcel(Parcel in) {
            return new LockMode(in);
        }

        public LockMode[] newArray(int size) {
            return new LockMode[size];
        }
    };

    private LockMode(Parcel in) {
        @LockType int lock = in.readInt();
        lockType = lock;
        pin = in.readString();
        password = in.readString();
    }
}
