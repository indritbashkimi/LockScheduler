package it.ibashkimi.lockscheduler.model;


import org.json.JSONException;
import org.json.JSONObject;


public class LockAction extends Action {

    private LockMode lockMode;

    public LockAction() {
        this(new LockMode(LockMode.LockType.UNCHANGED));
    }

    public LockAction(LockMode lockMode) {
        super(Type.LOCK);
        this.lockMode = lockMode;
    }

    public LockMode getLockMode() {
        return lockMode;
    }

    public void setLockMode(LockMode lockMode) {
        this.lockMode = lockMode;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LockAction)) {
            return false;
        }
        LockAction action = (LockAction) obj;
        return getLockMode().equals(action.getLockMode());
    }

    @Override
    public String toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("type", getType());
            json.put("lock_mode", lockMode.toJson());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json.toString();
    }

    public static LockAction parseJson(String jsonRep) {
        try {
            JSONObject json = new JSONObject(jsonRep);
            //@Type int type = json.getInt("type");
            LockMode lockMode = LockMode.parseJson(json.getString("lock_mode"));
            return new LockAction(lockMode);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
