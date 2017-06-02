package com.ibashkimi.lockscheduler.model.source.serializer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import com.ibashkimi.lockscheduler.model.Action;
import com.ibashkimi.lockscheduler.model.Condition;
import com.ibashkimi.lockscheduler.model.Profile;

public class ProfileSerializer {

    public static String toJson(Profile profile) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", "" + profile.getId());
            jsonObject.put("name", profile.getName());
            jsonObject.put("conditions_len", profile.getConditions().size());
            for (int i = 0; i < profile.getConditions().size(); i++) {
                jsonObject.put("condition_" + i, ConditionSerializer.conditionToJson(profile.getConditions().get(i)));
            }
            jsonObject.put("true_actions_size", profile.getEnterActions().size());
            for (int i = 0; i < profile.getEnterActions().size(); i++) {
                jsonObject.put("true_action_" + i, ActionSerializer.actionToJson(profile.getEnterActions().get(i)));
            }
            jsonObject.put("false_actions_size", profile.getExitActions().size());
            for (int i = 0; i < profile.getExitActions().size(); i++) {
                jsonObject.put("false_action_" + i, ActionSerializer.actionToJson(profile.getExitActions().get(i)));
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject.toString();
    }

    public static Profile parseJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        String id = jsonObject.getString("id");
        String name = jsonObject.getString("name");

        int conditionsLen = jsonObject.getInt("conditions_len");
        ArrayList<Condition> conditions = new ArrayList<>(conditionsLen);
        for (int i = 0; i < conditionsLen; i++) {
            String conditionJson = jsonObject.getString("condition_" + i);
            Condition condition = ConditionSerializer.parseCondition(conditionJson);
            conditions.add(condition);
        }

        int trueActionsSize = jsonObject.getInt("true_actions_size");
        ArrayList<Action> trueActions = new ArrayList<>(trueActionsSize);
        for (int i = 0; i < trueActionsSize; i++) {
            String actionRep = jsonObject.getString("true_action_" + i);
            Action action = ActionSerializer.parseLockAction(actionRep);
            trueActions.add(action);
        }

        int falseActionsSize = jsonObject.getInt("false_actions_size");
        ArrayList<Action> falseActions = new ArrayList<>(falseActionsSize);
        for (int i = 0; i < falseActionsSize; i++) {
            String actionRep = jsonObject.getString("false_action_" + i);
            Action action = ActionSerializer.parseLockAction(actionRep);
            falseActions.add(action);
        }

        return new Profile(id, name, conditions, trueActions, falseActions);
    }
}
