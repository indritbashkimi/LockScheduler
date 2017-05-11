package it.ibashkimi.lockscheduler.model.source.serializer;

import org.json.JSONException;
import org.json.JSONObject;

import it.ibashkimi.lockscheduler.model.Action;
import it.ibashkimi.lockscheduler.model.LockAction;
import it.ibashkimi.lockscheduler.model.LockMode;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class ActionSerializer {

    public static String actionToJson(Action action) {
        switch (action.getType()) {
            case Action.Type.LOCK:
                return toJson((LockAction) action);
        }
        throw new RuntimeException("Unsupported action: " + action.getType());
    }

    public static Action parsejson(String jsonRep) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonRep);
        @Action.Type int type = jsonObject.getInt("type");
        switch (type) {
            case Action.Type.LOCK:
                return parseLockAction(jsonObject);
        }
        throw new RuntimeException("Unsupported action: " + type);
    }

    public static String toJson(LockAction action) {
        JSONObject json = new JSONObject();
        try {
            json.put("type", action.getType());
            json.put("lock_mode", action.getLockMode().toJson());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json.toString();
    }

    public static LockAction parseLockAction(String json) throws JSONException {
        return parseLockAction(new JSONObject(json));
    }

    public static LockAction parseLockAction(JSONObject jsonObject) throws JSONException {
        //@Type int type = json.getInt("type");
        LockMode lockMode = LockMode.parseJson(jsonObject.getString("lock_mode"));
        return new LockAction(lockMode);
    }
}
