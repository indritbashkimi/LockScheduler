package it.ibashkimi.lockscheduler.model;


public class LockAction extends Action {

    private LockMode lockMode;

    public LockAction() {
        this(new LockMode(LockMode.LockType.UNCHANGED));
    }

    public LockAction(LockMode lockMode) {
        super(Type.LOCK);
        this.lockMode = lockMode;
    }

    public LockMode getLockMode() {
        return lockMode;
    }

    public void setLockMode(LockMode lockMode) {
        this.lockMode = lockMode;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LockAction)) {
            return false;
        }
        LockAction action = (LockAction) obj;
        return getLockMode().equals(action.getLockMode());
    }
}
