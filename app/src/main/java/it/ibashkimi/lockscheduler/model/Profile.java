package it.ibashkimi.lockscheduler.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class Profile {

    @NonNull
    private String id;

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

    public Profile(@NonNull String id, String name) {
        this(id, name, new ArrayList<Condition>(), new ArrayList<Action>(), new ArrayList<Action>());
    }

    public Profile(@NonNull String id, String name, @NonNull List<Condition> conditions, @NonNull List<Action> enterActions, @NonNull List<Action> exitActions) {
        this.id = id;
        this.name = name;
        if (this.name == null)
            this.name = "";
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

    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
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
        return String.format(Locale.ENGLISH, "Profile[id=%s, name=%s, conditions=%d, enterActions=%d, exitActions=%d]", id, name, conditions.size(), enterActions.size(), exitActions.size());
    }
}
