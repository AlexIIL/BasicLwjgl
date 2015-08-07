package alexiil.lwjgl.list;

import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;

import alexiil.lwjgl.GLFWManagerThread;
import alexiil.lwjgl.LwjglWindowManager;
import alexiil.lwjgl.Pipeline;
import alexiil.lwjgl.RunnablePipeline;
import alexiil.utils.render.window.IRenderingTools;
import alexiil.utils.render.window.SwingTools;

public class OpenGlTools implements IRenderingTools {
    private static final OpenGlTools instance = new OpenGlTools();
    private static ContextCapabilities caps = null;

    public static IRenderingTools init() {
        try {
            // Bit strange, basically attempts to open a window for a frame to check if it can use openGL, and what is
            // supported.
            Pipeline pipe = new RunnablePipeline(() -> {
                GLContext context = GLContext.createFromCurrent();
                caps = context.getCapabilities();
            } , () -> {} , () -> {});
            LwjglWindowManager manager = GLFWManagerThread.enqueRequest(1, 1, "Test Window", false, pipe, null);
            manager.close();
            manager.init();
            manager.run();
        } catch (Throwable t) {
            System.out.println("Open GL is not supported on this hardware!");
            t.printStackTrace();
        }
        return init(caps);
    }

    public static IRenderingTools init(ContextCapabilities caps) {
        if (caps == null) {// OpenGL is NOT supported on this system. Don't override the swing system
            System.out.println("There was no OpenGL context! Not running with opengl...");
            return SwingTools.instance;
        }
        if (!caps.OpenGL11) {// OpenGL 1.1 is not supported. Not enough to even begin to render anything :/
            System.out.println("OpenGL 1.1 was not available! Not enough commands available to even begin running opengl");
            return SwingTools.instance;
        }
        return instance;
    }

    @Override
    public OpenGlWindow makeNewWindow() {
        return new OpenGlWindow(caps);
    }

    @Override
    public OpenGlCallList makeNewCallList() {
        return new OpenGlCallList(caps);
    }
}
