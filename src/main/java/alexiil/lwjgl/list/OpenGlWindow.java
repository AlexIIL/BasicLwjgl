package alexiil.lwjgl.list;

import org.lwjgl.opengl.ContextCapabilities;

import alexiil.lwjgl.InternalPipeline;
import alexiil.lwjgl.LwjglWindowManager;
import alexiil.lwjgl.Pipeline;
import alexiil.lwjgl.RunnablePipeline;
import alexiil.utils.render.window.IWindow;

public class OpenGlWindow implements IWindow<OpenGlCallList> {
    private Runnable render;
    private LwjglWindowManager window;
    public final ContextCapabilities caps;

    public OpenGlWindow(ContextCapabilities caps) {
        this.caps = caps;
    }

    @Override
    public void open(int width, int height, String title) {
        open(width, height, title, false);
    }

    private void open(int width, int height, String title, boolean fullscreen) {
        Pipeline actual = new RunnablePipeline(null, () -> {
            if (render != null) {
                render.run();
            }
        } , null);
        Pipeline internal = new InternalPipeline(actual);
        window = new LwjglWindowManager(width, height, title, internal, false);
        new Thread(() -> {
            window.run();
        } , "opengl-renderer").start();
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

    public void setSize(int width, int height) {
        if (window.fullscreen) {
            boolean main = window.mainWindow;
            window.mainWindow = false;
            window.close();
            open(width, height, window.getTitle(), true);
            window.mainWindow = main;

        }
        window.setSize(width, height);
    }

    public void setFullscreen(boolean fullscreen) {
        if (window.fullscreen == fullscreen) {
            return;
        }
        window.mainWindow = false;
        window.close();
        open(window.getWidth(), window.getHeight(), window.getTitle(), fullscreen);
    }
}
