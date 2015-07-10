package alexiil.lwjgl.list;

import org.lwjgl.opengl.ContextCapabilities;

import alexiil.utils.render.glcompat.IRenderCallList;
import alexiil.utils.render.glcompat.IWindow;
import alexiil.utils.render.glcompat.SwingTools;

public class OpenGlTools extends SwingTools {
    public static void init(ContextCapabilities caps) {
        if (caps == null) {// OpenGL is NOT supported on this system. Don't override the swing system
            return;
        }
        if (!caps.OpenGL11) {// OpenGL 1.1 is not supported. Not enough to even begin to render anything :/
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
