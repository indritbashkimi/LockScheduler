package it.ibashkimi.lockscheduler.model;

public class ProfileFactory {

    public static Profile createLockProfile() {
        Profile profile = new Profile(Long.toString(System.currentTimeMillis()));
        profile.getTrueActions().add(new LockAction());
        profile.getFalseActions().add(new LockAction());
        return profile;
    }
}
