package com.ibashkimi.lockscheduler.model.source.serializer;

import com.ibashkimi.lockscheduler.model.action.Action;
import com.ibashkimi.lockscheduler.model.action.LockAction;

import org.json.JSONException;
import org.json.JSONObject;

class ActionSerializer {

    static String actionToJson(Action action) {
        switch (action.getType()) {
            case Action.Type.LOCK:
                return toJson((LockAction) action);
        }
        throw new RuntimeException("Unsupported action: " + action.getType());
    }

    static Action parseJson(String jsonRep) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonRep);
        @Action.Type int type = jsonObject.getInt("type");
        switch (type) {
            case Action.Type.LOCK:
                return parseLockAction(jsonObject);
        }
        throw new RuntimeException("Unsupported action: " + type);
    }

    private static String toJson(LockAction action) {
        JSONObject json = new JSONObject();
        try {
            json.put("type", action.getType());
            json.put("lockType", "" + action.getLockType());
            json.put("input", action.getInput());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json.toString();
    }

    static LockAction parseLockAction(String json) throws JSONException {
        return parseLockAction(new JSONObject(json));
    }

    private static LockAction parseLockAction(JSONObject jsonObject) throws JSONException {
        @LockAction.LockType int lockType = Integer.parseInt(jsonObject.getString("lockType"));
        String input = jsonObject.getString("input");
        return new LockAction(lockType, input);
    }
}
