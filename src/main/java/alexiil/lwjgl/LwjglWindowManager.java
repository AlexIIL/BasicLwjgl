package alexiil.lwjgl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.Sys;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.libffi.Closure;

public class LwjglWindowManager {
    // The window handle
    private long window;

    private int width, height;
    private final String windowName;
    private final Pipeline pipeline;
    private final List<Closure> closures = new ArrayList<Closure>();
    private boolean close = false;
    public boolean mainWindow = false;

    public LwjglWindowManager(int width, int height, String windowName, Pipeline pipe) {
        this.width = width;
        this.height = height;
        this.windowName = windowName;
        this.pipeline = pipe;
        pipe.setManager(this);
    }

    public void run() {
        System.out.println("Hello LWJGL " + Sys.getVersion());
        try {
            init();
            loop();

            // Release window and window callbacks
            GLFW.glfwDestroyWindow(window);
        }
        finally {
            // Terminate GLFW and release the GLFWerrorfun
            GLFW.glfwTerminate();
            while (closures.size() != 0) {
                closures.remove(0).release();
            }
        }
        if (mainWindow) {
            System.exit(0);
        }
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFW.glfwSetErrorCallback(registerClosure(Callbacks.errorCallbackPrint(System.err)));

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (GLFW.glfwInit() != GL11.GL_TRUE)
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure our window
        GLFW.glfwDefaultWindowHints(); // optional, the current window hints are already the default
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE); // the window will stay hidden after creation
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_TRUE); // the window will be resizable

        // Create the window
        window = GLFW.glfwCreateWindow(width, height, windowName, MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        GLFW.glfwSetKeyCallback(window, registerClosure(new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE)
                    GLFW.glfwSetWindowShouldClose(window, GL11.GL_TRUE); // We will detect this in our rendering loop
            }
        }));

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

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the ContextCapabilities instance and makes the OpenGL
        // bindings available for use.
        GLContext.createFromCurrent();

        pipeline.pre();

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (GLFW.glfwWindowShouldClose(window) == GL11.GL_FALSE && !close) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            GL11.glPushMatrix();
            pipeline.during();
            GL11.glPopMatrix();

            GLFW.glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            GLFW.glfwPollEvents();
        }

        pipeline.after();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void close() {
        close = true;
    }

    public <T extends Closure> T registerClosure(T closure) {
        closures.add(closure);
        return closure;
    }
}
