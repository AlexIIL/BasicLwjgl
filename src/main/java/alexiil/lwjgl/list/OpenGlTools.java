package alexiil.lwjgl.list;

import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;

import alexiil.utils.render.glcompat.IRenderCallList;
import alexiil.utils.render.glcompat.IWindow;
import alexiil.utils.render.glcompat.SwingTools;

public class OpenGlTools extends SwingTools {
    public static void init() {
        ContextCapabilities caps = null;
        try {
            GLContext context = GLContext.createFromCurrent();
            caps = context.getCapabilities();
            context.destroy();
        }
        catch (Throwable t) {
            System.out.println("Open GL is not supported on this hardware!");
            t.printStackTrace();
        }
        init(caps);
    }

    public static void init(ContextCapabilities caps) {
        if (caps == null) {// OpenGL is NOT supported on this system. Don't override the swing system
            System.out.println("There was no OpenGL context! Not running with opengl...");
            return;
        }
        if (!caps.OpenGL11) {// OpenGL 1.1 is not supported. Not enough to even begin to render anything :/
            System.out.println("OpenGL 1.1 was not available! Not enough commands available to even begin running opengl");
            return;
        }
        SwingTools.instance = new OpenGlTools();
    }

    @Override
    public IWindow<? extends IRenderCallList> makeNewWindow() {
        return new OpenGlWindow();
    }

    @Override
    public IRenderCallList makeNewCallList() {
        return new OpenGlCallList();
    }
}
