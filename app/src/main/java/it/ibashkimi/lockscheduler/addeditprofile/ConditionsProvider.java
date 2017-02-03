package it.ibashkimi.lockscheduler.addeditprofile;

import java.util.List;

import it.ibashkimi.lockscheduler.model.Condition;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public interface ConditionsProvider {

    List<Condition> getConditions();

    void updateConditions(List<Condition> conditions);
}
