package alexiil.lwjgl;

import org.lwjgl.opengl.GL11;

public class InternalPipeline extends Pipeline {
    public final Pipeline internal;

    public InternalPipeline(Pipeline internal) {
        this.internal = internal;
    }

    private int lastWidth = 0;

    public int getWidth() {
        return getManager().getWidth();
    }

    public int getHeight() {
        return getManager().getHeight();
    }

    @Override
    public void pre() {
        GL11.glClearColor(0, 0, 0, 0);
        internal.pre();
    }

    @Override
    public void during() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, getWidth(), getHeight(), 0, 1, -1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glTranslatef(getWidth() / 2F, getHeight() / 2F, 0);

        internal.during();

        GL11.glPopMatrix();
    }

    @Override
    public void after() {
        internal.after();
    }

    @Override
    public void setManager(LwjglWindowManager newManager) {
        super.setManager(newManager);
        internal.setManager(newManager);
    }
}
