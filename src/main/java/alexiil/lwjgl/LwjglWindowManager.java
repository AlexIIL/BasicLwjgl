package alexiil.lwjgl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.Sys;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.libffi.Closure;

public class LwjglWindowManager {
    public interface IGlfwEventListener {
        void curserPosChange(double xpos, double ypos);

        void mouseButtonChange(int button, int action, int mods);
    }

    private static long fullscreenMonitor = MemoryUtil.NULL;

    // The window handle
    volatile long window;

    private final long primaryMonitor;
    private int width, height;
    private final String windowName;
    private final Pipeline pipeline;
    public final boolean fullscreen;
    private final List<Closure> closures = new ArrayList<Closure>();
    public boolean mainWindow = false;
    private final IGlfwEventListener listener;

    volatile boolean close = false;
    volatile boolean hasClosed = false;
    volatile boolean shouldExit = false;

    /** You must call this method if you want to use a fullscreen display */
    public static void startingInit() {
        fullscreenMonitor = GLFW.glfwGetPrimaryMonitor();
    }

    LwjglWindowManager(IGlfwEventListener listener, int width, int height, String windowName, Pipeline pipe, boolean fullscreen) {
        this.listener = listener;
        this.width = width;
        this.height = height;
        this.windowName = windowName;
        this.pipeline = pipe;
        this.fullscreen = fullscreen;
        primaryMonitor = fullscreen ? fullscreenMonitor : MemoryUtil.NULL;
        pipe.setManager(this);
    }

    public void run() {
        System.out.println("Using LWJGL version " + Sys.getVersion());
        try {
            loop();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            // Terminate GLFW and release the closures
            System.out.println("Releasing " + closures.size() + " closures...");
            while (closures.size() != 0) {
                closures.remove(0).release();
            }
            hasClosed = true;
        }
        if (mainWindow) {
            shouldExit = true;
        }
    }

    public void init() {
        // Create the window
        window = GLFW.glfwCreateWindow(width, height, windowName, primaryMonitor, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        GLFW.glfwSetKeyCallback(window, registerClosure(GLFW.GLFWKeyCallback((window, key, scancode, action, mods) -> {
            // Force exit when the "Esc+Shift+Ctrl+Alt" keys are pressed
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE && mods == 7) {
                GLFW.glfwSetWindowShouldClose(window, GL11.GL_TRUE); // We will detect this in our rendering loop
            } else {

            }
        })));

        if (listener != null) {
            GLFW.glfwSetMouseButtonCallback(window, registerClosure(GLFW.GLFWMouseButtonCallback((window, button, action, mods) -> {
                listener.mouseButtonChange(button, action, mods);
            })));

            GLFW.glfwSetCursorPosCallback(window, registerClosure(GLFW.GLFWCursorPosCallback((window, xpos, ypos) -> {
                listener.curserPosChange(xpos, ypos);
            })));
        }

        GLFW.glfwSetWindowSizeCallback(window, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                if (window == LwjglWindowManager.this.window) {
                    LwjglWindowManager.this.width = width;
                    LwjglWindowManager.this.height = height;
                    System.out.println("Set the window size to (" + width + ", " + height + ")");
                }
            }
        });

        // Get the resolution of the primary monitor
        ByteBuffer vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        // Center our window
        GLFW.glfwSetWindowPos(window, (GLFWvidmode.width(vidmode) - width) / 2, (GLFWvidmode.height(vidmode) - height) / 2);

        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(window);
        // Enable v-sync
        GLFW.glfwSwapInterval(1);

        // Make the window visible
        GLFW.glfwShowWindow(window);

        GLContext.createFromCurrent();
        System.out.println("OpenGL version " + GL11.glGetString(GL11.GL_VERSION));
    }

    void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the ContextCapabilities instance and makes the OpenGL
        // bindings available for use.
        GLContext.createFromCurrent();//Crashes here

        pipeline.pre();

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (GLFW.glfwWindowShouldClose(window) == GL11.GL_FALSE && !close) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            GL11.glPushMatrix();
            pipeline.during();
            GL11.glPopMatrix();

            GLFW.glfwSwapBuffers(window); // swap the color buffers
        }

        pipeline.after();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getTitle() {
        return windowName;
    }

    public void close() {
        close = true;
    }

    public <T extends Closure> T registerClosure(T closure) {
        closures.add(closure);
        return closure;
    }

    public void setSize(int width, int height) {
        GLFW.glfwSetWindowSize(window, width, height);
        this.width = width;
        this.height = height;
    }
}
