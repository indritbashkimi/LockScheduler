package it.ibashkimi.lockscheduler.addeditprofile.actions;


import java.util.List;

import it.ibashkimi.lockscheduler.model.Action;


public class ActionsPresenter implements ActionsContract.Presenter {

    private ActionsContract.View view;

    private List<Action> trueActions;

    private List<Action> falseActions;

    public ActionsPresenter(ActionsContract.View view) {
        this(view, null, null);
    }

    public ActionsPresenter(ActionsContract.View view, List<Action> trueActions, List<Action> falseActions) {
        this.view = view;
        this.trueActions = trueActions;
        this.falseActions = falseActions;
    }

    @Override
    public void start() {
        if (trueActions != null) {

        }
    }

    @Override
    public List<Action> saveTrueConditions() {
        return null;
    }

    @Override
    public List<Action> saveFalseConditions() {
        return null;
    }

    @Override
    public void deleteCondition(int conditionId) {

    }

    @Override
    public void populateConditions() {

    }

    @Override
    public boolean isDataMissing() {
        return false;
    }

    private void showTrueActions() {

    }

    private void showFalseActions() {}
}
