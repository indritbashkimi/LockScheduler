package it.ibashkimi.lockscheduler.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

import static it.ibashkimi.lockscheduler.model.LockMode.LockType.PASSWORD;
import static it.ibashkimi.lockscheduler.model.LockMode.LockType.PIN;
import static it.ibashkimi.lockscheduler.model.LockMode.LockType.SWIPE;
import static it.ibashkimi.lockscheduler.model.LockMode.LockType.UNCHANGED;


public class LockMode implements Parcelable {

    @IntDef({PIN, PASSWORD, SWIPE, UNCHANGED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LockType {
        int PIN = 0;
        int PASSWORD = 1;
        int SWIPE = 2;
        int UNCHANGED = 3;
    }

    @LockType
    private int lockType;
    private String input;

    public LockMode(@LockType int lockType) {
        this.lockType = lockType;
        this.input = "";
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    @LockMode.LockType
    public int getLockType() {
        return lockType;
    }

    public void setLockType(@LockType int lockType) {
        this.lockType = lockType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof LockMode))
            return false;
        LockMode lockMode = (LockMode) obj;
        if (lockType != lockMode.lockType)
            return false;
        switch (lockType) {
            case LockType.PASSWORD:
                if (!input.equals(lockMode.getInput())) return false;
                break;
            case LockType.PIN:
                if (!input.equals(lockMode.getInput())) return false;
                break;
            case LockType.SWIPE:
                break;
            case LockType.UNCHANGED:
                break;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "LockMode{%s}", lockTypeToString(lockType));
    }

    public static String lockTypeToString(@LockType int lockType) {
        switch (lockType) {
            case UNCHANGED:
                return "Unchanged";
            case LockType.PASSWORD:
                return "Password";
            case LockType.PIN:
                return "PIN";
            case LockType.SWIPE:
                return "Swipe";
            default:
                return "Unknown";
        }
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("lockType", "" + lockType);
            jsonObject.put("input", input);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    public static LockMode parseJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        @LockType int lockType = Integer.parseInt(jsonObject.getString("lockType"));
        LockMode lockMode = new LockMode(lockType);
        lockMode.setInput(jsonObject.getString("input"));
        return lockMode;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(lockType);
        dest.writeString(input);
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
        input = in.readString();
    }
}
