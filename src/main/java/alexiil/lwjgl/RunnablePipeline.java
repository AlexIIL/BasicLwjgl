package alexiil.lwjgl;

public class RunnablePipeline extends Pipeline {
    public final Runnable pre, during, after;

    public RunnablePipeline(Runnable pre, Runnable during, Runnable after) {
        this.pre = pre;
        this.during = during;
        this.after = after;
    }

    @Override
    public void pre() {
        if (pre != null) {
            pre.run();
        }
    }

    @Override
    public void during() {
        if (during != null) {
            during.run();
        }
    }

    @Override
    public void after() {
        if (after != null) {
            after.run();
        }
    }
}
