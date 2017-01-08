package it.ibashkimi.lockscheduler.domain;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class Profile implements Parcelable {

    private long id;
    private String name;
    private boolean enabled = true;
    private LockMode enterLockMode;
    private LockMode exitLockMode;
    private boolean entered;
    private ArrayList<Condition> conditions;
    private ArrayList<Action> actions;
    private boolean autoNameAssignment = true;

    public Profile(long id) {
        this(id, "");
    }

    public Profile(long id, String name) {
        this(id, name, false, true, new LockMode(LockMode.LockType.UNCHANGED), new LockMode(LockMode.LockType.UNCHANGED));
    }

    public Profile(long id, String name, boolean enabled, LockMode enterLockMode, LockMode exitLockMode) {
        this(id, name, false, enabled, enterLockMode, exitLockMode);
    }

    private Profile(long id, String name, boolean autoNameAssignment, boolean enabled, LockMode enterLockMode, LockMode exitLockMode) {
        this.id = id;
        this.name = name;
        this.autoNameAssignment = autoNameAssignment;
        this.enabled = enabled;
        this.enterLockMode = enterLockMode;
        this.exitLockMode = exitLockMode;
        this.conditions = new ArrayList<>();
    }

    public ArrayList<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(ArrayList<Condition> conditions) {
        this.conditions = conditions;
    }

    public boolean isEntered() {
        return entered;
    }

    public void setEntered(boolean entered) {
        this.entered = entered;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        setName(name, false);
    }

    public void setName(String name, boolean auto) {
        this.name = name;
        this.autoNameAssignment = auto;
    }

    public boolean isNameAutomaticallyGenerated() {
        return autoNameAssignment;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LockMode getEnterLockMode() {
        return enterLockMode;
    }

    public void setEnterLockMode(LockMode enterLockMode) {
        this.enterLockMode = enterLockMode;
    }

    public LockMode getExitLockMode() {
        return exitLockMode;
    }

    public void setExitLockMode(LockMode exitLockMode) {
        this.exitLockMode = exitLockMode;
    }

    public void update(Profile profile) {
        enabled = profile.isEnabled();
        name = profile.getName();
        enterLockMode = profile.getEnterLockMode();
        exitLockMode = profile.getExitLockMode();
        conditions = profile.getConditions();
        //actions = profile.getActions();
    }

    public Condition getCondition(@Condition.Type int type) {
        for (Condition condition : conditions) {
            if (condition.getType() == type)
                return condition;
        }
        return null;
    }

    public PlaceCondition getPlaceCondition() {
        Condition condition = getCondition(Condition.Type.PLACE);
        if (condition != null)
            return (PlaceCondition) condition;
        return null;
    }

    public TimeCondition getTimeCondition() {
        Condition condition = getCondition(Condition.Type.TIME);
        if (condition != null)
            return (TimeCondition) condition;
        return null;
    }

    public WifiCondition getWifiCondition() {
        Condition condition = getCondition(Condition.Type.WIFI);
        if (condition != null)
            return (WifiCondition) condition;
        return null;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "Profile{id=%d, name=%s, enterLock=%s, exitLock=%s, %s}", id, name, enterLockMode, exitLockMode, getPlaceCondition());
    }


    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", "" + id);
            jsonObject.put("name", name);
            jsonObject.put("enabled", enabled);
            jsonObject.put("enterLock", enterLockMode.toJson().toString());
            jsonObject.put("exitLock", exitLockMode.toJson().toString());
            jsonObject.put("conditions_len", conditions.size());
            for (int i = 0; i < conditions.size(); i++) {
                jsonObject.put("condition_" + i, conditions.get(i).toJson());
            }
            jsonObject.put("entered", entered);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static Profile parseJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        Profile profile = new Profile(Long.parseLong(jsonObject.getString("id")));
        profile.setName(jsonObject.getString("name"));
        profile.setEnabled(jsonObject.getBoolean("enabled"));
        profile.setEnterLockMode(LockMode.parseJson(jsonObject.getString("enterLock")));
        profile.setExitLockMode(LockMode.parseJson(jsonObject.getString("exitLock")));

        int conditionsLen = jsonObject.getInt("conditions_len");
        ArrayList<Condition> conditions = new ArrayList<>(conditionsLen);
        for (int i = 0; i < conditionsLen; i++) {
            String conditionJson = jsonObject.getString("condition_" + i);
            JSONObject conditionJsonObject = new JSONObject(conditionJson);
            @Condition.Type int type = conditionJsonObject.getInt("type");
            Condition condition = null;
            switch (type) {
                case Condition.Type.PLACE:
                    condition = PlaceCondition.parseJson(conditionJson);
                    conditions.add(condition);
                    break;
                case Condition.Type.TIME:
                    condition = TimeCondition.parseJson(conditionJson);
                    conditions.add(condition);
                    break;
                case Condition.Type.WIFI:
                    condition = WifiCondition.parseJson(conditionJson);
                    conditions.add(condition);
            }
        }
        profile.setConditions(conditions);

        profile.setEntered(jsonObject.getBoolean("entered"));
        return profile;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(enabled ? 1 : 0);
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeParcelable(enterLockMode, flags);
        dest.writeParcelable(exitLockMode, flags);
        dest.writeInt(entered ? 1 : 0);
    }

    public static final Parcelable.Creator<Profile> CREATOR = new Parcelable.Creator<Profile>() {
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }

        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };

    private Profile(Parcel in) {
        enabled = in.readInt() == 1;
        id = in.readLong();
        name = in.readString();
        enterLockMode = in.readParcelable(LockMode.class.getClassLoader());
        exitLockMode = in.readParcelable(LockMode.class.getClassLoader());
        entered = in.readInt() == 1;
    }
}
