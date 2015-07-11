package alexiil.lwjgl.list;

import alexiil.lwjgl.InternalPipeline;
import alexiil.lwjgl.LwjglWindowManager;
import alexiil.lwjgl.Pipeline;
import alexiil.lwjgl.RunnablePipeline;
import alexiil.utils.render.glcompat.IWindow;

public class OpenGlWindow implements IWindow<OpenGlCallList> {
    private Runnable render;
    private LwjglWindowManager window;

    @Override
    public void open(int width, int height, String title) {
        Pipeline actual = new RunnablePipeline(null, () -> {
            if (render != null) {
                render.run();
            }
        }, null);
        Pipeline internal = new InternalPipeline(actual);
        window = new LwjglWindowManager(width, height, title, internal);
    }

    @Override
    public void close() {
        window.close();
    }

    @Override
    public void renderCallList(OpenGlCallList list) {
        list.render();
    }

    @Override
    public void setRenderer(Runnable render) {
        this.render = render;
    }

    @Override
    public void makeMain() {
        window.mainWindow = true;
    }
}
