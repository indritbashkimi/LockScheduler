package it.ibashkimi.lockscheduler.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class Profile {

    @NonNull
    private String id;
    @NonNull
    private String name;
    @NonNull
    private List<Condition> conditions;
    @NonNull
    private List<Action> enterActions;
    @NonNull
    private List<Action> exitActions;

    private boolean active;

    public Profile(@NonNull String id) {
        this(id, "");
    }

    public Profile(@NonNull String id, @NonNull String name) {
        this(id, name, new ArrayList<Condition>(), new ArrayList<Action>(), new ArrayList<Action>());
    }

    public Profile(@NonNull String id, @NonNull String name, @NonNull List<Condition> conditions, @NonNull List<Action> enterActions, @NonNull List<Action> exitActions) {
        this.id = id;
        this.name = name;
        this.conditions = conditions;
        this.enterActions = enterActions;
        this.exitActions = exitActions;
    }

    @NonNull
    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(@NonNull List<Condition> conditions) {
        this.conditions = conditions;
    }

    public boolean containsCondition(@Condition.Type int type) {
        for (Condition condition : conditions)
            if (condition.getType() == type)
                return true;
        return false;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        setName(name, false);
    }

    public void setName(@NonNull String name, boolean auto) {
        this.name = name;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public List<Action> getEnterActions() {
        return enterActions;
    }

    public void setEnterActions(@NonNull List<Action> enterActions) {
        this.enterActions = enterActions;
    }

    @NonNull
    public List<Action> getExitActions() {
        return exitActions;
    }

    public void setExitActions(@NonNull List<Action> exitActions) {
        this.exitActions = exitActions;
    }

    public void update(Profile profile) {
        name = profile.getName();
        active = profile.isActive();
        conditions = profile.getConditions();
        enterActions = profile.getEnterActions();
        exitActions = profile.getExitActions();
    }

    @Nullable
    public Action getAction(@Action.Type int type, boolean fromTrueActions) {
        List<Action> actions = fromTrueActions ? enterActions : exitActions;
        for (Action action : actions)
            if (action.getType() == Action.Type.LOCK)
                return action;
        return null;
    }

    @Nullable
    public Condition getCondition(@Condition.Type int type) {
        for (Condition condition : conditions) {
            if (condition.getType() == type)
                return condition;
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Profile))
            return false;
        Profile profile = (Profile) obj;
        if (conditions.size() != profile.getConditions().size() || !id.equals(profile.getId()) || !name.equals(profile.getName()) || enterActions.size() != profile.getEnterActions().size() || exitActions.size() != profile.getExitActions().size())
            return false;
        for (int i = 0; i < conditions.size(); i++) {
            if (!conditions.get(i).equals(profile.getConditions().get(i)))
                return false;
        }
        for (int i = 0; i < enterActions.size(); i++) {
            if (!enterActions.get(i).equals(profile.getEnterActions().get(i)))
                return false;
        }
        for (int i = 0; i < exitActions.size(); i++) {
            if (!exitActions.get(i).equals(profile.getExitActions().get(i)))
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "Profile{id=%s, name=%s, conditions=%d, enterActions=%d, exitActions=%d}", id, name, conditions.size(), enterActions.size(), exitActions.size());
    }

    public boolean isEmpty() {
        return (name == null) ||
                (name.equals("")) ||
                (conditions == null) ||
                (conditions.size() == 0);
    }


    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", "" + id);
            jsonObject.put("name", name);
            jsonObject.put("conditions_len", conditions.size());
            for (int i = 0; i < conditions.size(); i++) {
                jsonObject.put("condition_" + i, conditions.get(i).toJson());
            }
            jsonObject.put("active", active);
            jsonObject.put("true_actions_size", enterActions.size());
            for (int i = 0; i < enterActions.size(); i++) {
                jsonObject.put("true_action_" + i, enterActions.get(i).toJson());
            }
            jsonObject.put("false_actions_size", exitActions.size());
            for (int i = 0; i < enterActions.size(); i++) {
                jsonObject.put("false_action_" + i, exitActions.get(i).toJson());
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject.toString();
    }


    public static Profile parseJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        Profile profile = new Profile(jsonObject.getString("id"));
        profile.setName(jsonObject.getString("name"));

        int conditionsLen = jsonObject.getInt("conditions_len");
        ArrayList<Condition> conditions = new ArrayList<>(conditionsLen);
        for (int i = 0; i < conditionsLen; i++) {
            String conditionJson = jsonObject.getString("condition_" + i);
            JSONObject conditionJsonObject = new JSONObject(conditionJson);
            @Condition.Type int type = conditionJsonObject.getInt("type");
            Condition condition;
            switch (type) {
                case Condition.Type.PLACE:
                    condition = PlaceCondition.parseJson(conditionJson);
                    break;
                case Condition.Type.TIME:
                    condition = TimeCondition.parseJson(conditionJson);
                    break;
                case Condition.Type.WIFI:
                    condition = WifiCondition.parseJson(conditionJson);
                    break;
                default:
                    condition = null;
            }
            if (condition != null) conditions.add(condition);
        }
        profile.setConditions(conditions);

        int trueActionsSize = jsonObject.getInt("true_actions_size");
        ArrayList<Action> trueActions = new ArrayList<>(trueActionsSize);
        for (int i = 0; i < trueActionsSize; i++) {
            String actionRep = jsonObject.getString("true_action_" + i);
            Action action;
            @Action.Type int type = new JSONObject(actionRep).getInt("type");
            switch (type) {
                case Action.Type.LOCK:
                    action = LockAction.parseJson(actionRep);
                    break;
                default:
                    action = null;
            }
            trueActions.add(action);
        }
        profile.setEnterActions(trueActions);

        int falseActionsSize = jsonObject.getInt("false_actions_size");
        ArrayList<Action> falseActions = new ArrayList<>(falseActionsSize);
        for (int i = 0; i < falseActionsSize; i++) {
            String actionRep = jsonObject.getString("false_action_" + i);
            Action action;
            @Action.Type int type = new JSONObject(actionRep).getInt("type");
            switch (type) {
                case Action.Type.LOCK:
                    action = LockAction.parseJson(actionRep);
                    break;
                default:
                    action = null;
            }
            if (action != null)
                falseActions.add(action);
        }
        profile.setExitActions(falseActions);

        profile.active = jsonObject.getBoolean("active");
        return profile;
    }
}
