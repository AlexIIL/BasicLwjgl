package alexiil.lwjgl.list;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.ContextCapabilities;

import alexiil.lwjgl.InternalPipeline;
import alexiil.lwjgl.LwjglWindowManager;
import alexiil.lwjgl.LwjglWindowManager.IGLFWEventListener;
import alexiil.lwjgl.Pipeline;
import alexiil.lwjgl.RunnablePipeline;
import alexiil.utils.input.AKeyEvent;
import alexiil.utils.input.EnumButton;
import alexiil.utils.input.EnumKeyModifier;
import alexiil.utils.input.EnumMouseEvent;
import alexiil.utils.input.IMouseEvent;
import alexiil.utils.input.MouseContext;
import alexiil.utils.input.MouseStateChangeEvent;
import alexiil.utils.render.window.IRenderCallList;
import alexiil.utils.render.window.IWindow;

public class OpenGlWindow implements IWindow, IGLFWEventListener {
    private Runnable render;
    private LwjglWindowManager window;
    public final ContextCapabilities caps;

    private final List<Consumer<AKeyEvent>> keyListeners = new ArrayList<>();
    private final List<Consumer<IMouseEvent>> mouseListeners = new ArrayList<>();

    public final MouseContext mouseContext = new MouseContext();
    private int mouseX, mouseY;

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
        window = new LwjglWindowManager(this, width, height, title, internal, fullscreen);
        window.init();
        new Thread(() -> {
            window.run();
        } , "opengl-renderer").start();
    }

    @Override
    public void close() {
        window.close();
    }

    @Override
    public void renderCallList(IRenderCallList list) {
        ((OpenGlCallList) list).render();
    }

    @Override
    public OpenGlCallList makeCallList() {
        return new OpenGlCallList(caps);
    }

    @Override
    public void setRenderer(Runnable render) {
        this.render = render;
    }

    @Override
    public void makeMain() {
        window.mainWindow = true;
    }

    @Override
    public int[] getSize() {
        return new int[] { window.getWidth(), window.getHeight() };
    }

    public void setSize(int width, int height) {
        if (window.fullscreen) {
            boolean main = window.mainWindow;
            window.mainWindow = false;
            close();
            open(width, height, window.getTitle(), true);
            window.mainWindow = main;
        }
        window.setSize(width, height);
    }

    public void setFullscreen(boolean fullscreen) {// TODO: Make ALL GLFW stuff happen in a SINGLE thread.
        if (window.fullscreen == fullscreen) {
            return;
        }
        boolean main = window.mainWindow;
        window.mainWindow = false;
        close();
        open(window.getWidth(), window.getHeight(), window.getTitle(), fullscreen);
        window.mainWindow = main;
    }

    @Override
    public void addKeyCallback(Consumer<AKeyEvent> keyEventListener) {
        keyListeners.add(keyEventListener);
    }

    @Override
    public void addMouseCallback(Consumer<IMouseEvent> mouseEventListener) {
        mouseListeners.add(mouseEventListener);
    }

    @Override
    public void curserPosChange(double xpos, double ypos) {
        mouseX = (int) xpos;
        mouseY = (int) ypos;
    }

    @Override
    public void mouseButtonChange(int id, int action, int mods) {
        EnumButton button;
        if (id == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            button = mouseContext.invertMouseButtons ? EnumButton.SECONDARY : EnumButton.PRIMARY;
        } else if (id == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            button = mouseContext.invertMouseButtons ? EnumButton.PRIMARY : EnumButton.SECONDARY;
        } else if (id == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            button = EnumButton.MIDDLE;
        } else {
            button = EnumButton.NONE;
        }

        EnumMouseEvent motion = action == GLFW.GLFW_PRESS ? EnumMouseEvent.PRESSED : EnumMouseEvent.RELEASED;

        EnumKeyModifier[] modifiers = new EnumKeyModifier[0];
        if ((mods / GLFW.GLFW_MOD_ALT) % 2 == 1) {
            modifiers = new EnumKeyModifier[] { EnumKeyModifier.ALT };
        }
        if ((mods / GLFW.GLFW_MOD_CONTROL) % 2 == 1) {
            EnumKeyModifier[] held = modifiers;
            modifiers = new EnumKeyModifier[modifiers.length + 1];
            System.arraycopy(held, 0, modifiers, 0, held.length);
            modifiers[held.length] = EnumKeyModifier.CONTROL;
        }
        if ((mods / GLFW.GLFW_MOD_SHIFT) % 2 == 1) {
            EnumKeyModifier[] held = modifiers;
            modifiers = new EnumKeyModifier[modifiers.length + 1];
            System.arraycopy(held, 0, modifiers, 0, held.length);
            modifiers[held.length] = EnumKeyModifier.SHIFT;
        }
        if ((mods / GLFW.GLFW_MOD_SUPER) % 2 == 1) {
            EnumKeyModifier[] held = modifiers;
            modifiers = new EnumKeyModifier[modifiers.length + 1];
            System.arraycopy(held, 0, modifiers, 0, held.length);
            modifiers[held.length] = EnumKeyModifier.META;
        }

        IMouseEvent event = new MouseStateChangeEvent(mouseX, mouseY, mouseX, mouseY, button, motion, modifiers);
        event = mouseContext.changeFor(event);

        for (Consumer<IMouseEvent> listener : mouseListeners) {
            listener.accept(event);
        }

        System.out.println(event.toString());

        if (motion == EnumMouseEvent.RELEASED) {
            // Also generate a CLICKED event
            event = new MouseStateChangeEvent(mouseX, mouseY, mouseX, mouseY, button, EnumMouseEvent.CLICKED, modifiers);
            event = mouseContext.changeFor(event);

            for (Consumer<IMouseEvent> listener : mouseListeners) {
                listener.accept(event);
            }

            System.out.println(event.toString());
        }
    }
}
