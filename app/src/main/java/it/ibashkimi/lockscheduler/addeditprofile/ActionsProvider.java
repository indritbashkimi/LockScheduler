package it.ibashkimi.lockscheduler.addeditprofile;

import java.util.List;

import it.ibashkimi.lockscheduler.model.Action;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public interface ActionsProvider {

    List<Action> getTrueActions();

    List<Action> getFalseActions();

    void updateTrueActions(List<Action> actions);

    void updateFalseActions(List<Action> actions);
}
