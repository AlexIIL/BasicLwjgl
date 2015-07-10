package alexiil.lwjgl.list;

import alexiil.utils.render.glcompat.IRenderCallList;
import alexiil.utils.render.glcompat.IWindow;
import alexiil.utils.render.glcompat.SwingTools;

public class OpenGlTools extends SwingTools {
    public static void init() {
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
