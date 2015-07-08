package alexiil.lwjgl;

public abstract class Pipeline {
    private LwjglWindowManager manager;

    public abstract void pre();

    public abstract void during();

    public abstract void after();

    public LwjglWindowManager getManager() {
        return manager;
    }

    public void setManager(LwjglWindowManager newManager) {
        if (manager == null) {
            manager = newManager;
        }
    }
}
