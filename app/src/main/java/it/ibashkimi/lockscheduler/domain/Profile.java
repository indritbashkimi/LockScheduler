package it.ibashkimi.lockscheduler.domain;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class Profile {

    private long id;
    private String name;
    private List<Condition> conditions;
    private List<Action> trueActions;
    private List<Action> falseActions;

    private boolean active;

    public Profile(long id) {
        this(id, "");
    }

    public Profile(long id, String name) {
        this(id, name, new ArrayList<Condition>(), new ArrayList<Action>(), new ArrayList<Action>());
    }

    public Profile(long id, String name, List<Condition> conditions, List<Action> trueActions, List<Action> falseActions) {
        this.id = id;
        this.name = name;
        this.conditions = conditions;
        this.trueActions = trueActions;
        this.falseActions = falseActions;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        setName(name, false);
    }

    public void setName(String name, boolean auto) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Action> getTrueActions() {
        return trueActions;
    }

    public void setTrueActions(List<Action> trueActions) {
        this.trueActions = trueActions;
    }

    public List<Action> getFalseActions() {
        return falseActions;
    }

    public void setFalseActions(List<Action> falseActions) {
        this.falseActions = falseActions;
    }

    public void update(Profile profile) {
        name = profile.getName();
        active = profile.isActive();
        conditions = profile.getConditions();
        trueActions = profile.getTrueActions();
        falseActions = profile.getFalseActions();
    }

    public Action getAction(@Action.Type int type, boolean fromTrueActions) {
        List<Action> actions = fromTrueActions ? trueActions : falseActions;
        for (Action action : actions)
            if (action.getType() == Action.Type.LOCK)
                return action;
        return null;
    }

    public LockAction getLockAction(boolean fromTrueActions) {
        Action action = getAction(Action.Type.LOCK, fromTrueActions);
        if (action != null)
            return (LockAction) action;
        return null;
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
    public boolean equals(Object obj) {
        if (!(obj instanceof Profile))
            return false;
        Profile profile = (Profile) obj;
        if (conditions.size() != profile.getConditions().size() || id != profile.getId() || !name.equals(profile.getName()) || trueActions.size() != profile.getTrueActions().size() || falseActions.size() != profile.getFalseActions().size())
            return false;
        for (int i = 0; i < conditions.size(); i++) {
            if (!conditions.get(i).equals(profile.getConditions().get(i)))
                return false;
        }
        for (int i = 0; i < trueActions.size(); i++) {
            if (!trueActions.get(i).equals(profile.getTrueActions().get(i)))
                return false;
        }
        for (int i = 0; i < falseActions.size(); i++) {
            if (!falseActions.get(i).equals(profile.getFalseActions().get(i)))
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "Profile{id=%d, name=%s, conditions=%d, trueActions=%d, falseActions=%d}", id, name, conditions.size(), trueActions.size(), falseActions.size());
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
            jsonObject.put("true_actions_size", trueActions.size());
            for (int i = 0; i < trueActions.size(); i++) {
                jsonObject.put("true_action_" + i, trueActions.get(i).toJson());
            }
            jsonObject.put("false_actions_size", falseActions.size());
            for (int i = 0; i < trueActions.size(); i++) {
                jsonObject.put("false_action_" + i, falseActions.get(i).toJson());
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject.toString();
    }


    public static Profile parseJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        Profile profile = new Profile(Long.parseLong(jsonObject.getString("id")));
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
        profile.setTrueActions(trueActions);

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
        profile.setFalseActions(falseActions);

        profile.setActive(jsonObject.getBoolean("active"));
        return profile;
    }
}
