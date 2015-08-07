package alexiil.lwjgl;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import alexiil.lwjgl.LwjglWindowManager.IGlfwEventListener;

public class GLFWManagerThread implements Runnable {
    private static class WindowRequest {
        private final Thread toWake;
        private final int width, height;
        private final String title;
        private final boolean fullscreen;
        private final Pipeline pipe;
        private final IGlfwEventListener listener;

        private volatile boolean isReady;
        private volatile LwjglWindowManager window;

        private WindowRequest(Thread toWake, int width, int height, String title, boolean fullscreen, Pipeline pipe, IGlfwEventListener listener) {
            this.toWake = toWake;
            this.width = width;
            this.height = height;
            this.title = title;
            this.fullscreen = fullscreen;
            this.pipe = pipe;
            this.listener = listener;
        }

        private boolean isReady() {
            return isReady;
        }

        private LwjglWindowManager init() {
            window = new LwjglWindowManager(listener, width, height, title, pipe, fullscreen);
            window.init();
            isReady = true;
            return window;
        }

        @Override
        public String toString() {
            return "WindowRequest [toWake=" + toWake + ", width=" + width + ", height=" + height + ", title=" + title + ", fullscreen=" + fullscreen
                + ", pipe=" + pipe + ", listener=" + listener + ", isReady=" + isReady + ", window=" + window + "]";
        }
    }

    private static Deque<WindowRequest> requests = new ConcurrentLinkedDeque<>();
    private static volatile boolean canTakeRequests = true;
    private static GLFWManagerThread instance;

    private List<LwjglWindowManager> windows = new ArrayList<>();
    private long timeToWait = 1000;

    @Override
    public void run() {
        if (GLFW.glfwInit() != GL11.GL_TRUE) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        GLFWErrorCallback errorCallback = Callbacks.errorCallbackPrint(System.err);

        GLFW.glfwSetErrorCallback(errorCallback);

        // Configure all windows
        GLFW.glfwDefaultWindowHints(); // optional, the current window hints are already the default
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE); // the window will stay hidden after creation
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_TRUE); // the window will be resizable

        LwjglWindowManager.startingInit();

        while (windows.size() > 0 || requests.size() > 0 || timeToWait > 0) {
            if (windows.size() == 0 || requests.size() == 0) {
                timeToWait--;
            } else {
                timeToWait = 1000;
            }

            long before = System.currentTimeMillis();

            if (requests.size() > 0) {
                WindowRequest request = requests.pop();
                LwjglWindowManager manager = request.init();
                windows.add(manager);
                request.toWake.interrupt();
            }

            LwjglWindowManager toClose = null;
            for (LwjglWindowManager manager : windows) {
                if (manager.hasClosed) {
                    toClose = manager;
                    break;
                }
            }
            if (toClose != null) {
                GLFW.glfwDestroyWindow(toClose.window);
                toClose.window = MemoryUtil.NULL;
                windows.remove(toClose);
                if (toClose.shouldExit) {
                    canTakeRequests = false;
                    for (LwjglWindowManager manager : windows) {
                        manager.close = true;
                    }
                    requests.clear();
                }
            }

            GLFW.glfwPollEvents();

            long diff = System.currentTimeMillis() - before;
            if (diff < 17) {
                try {
                    Thread.sleep(17 - diff);
                } catch (InterruptedException ignored) {}
            }
        }

        errorCallback.release();

        GLFW.glfwTerminate();

        instance = null;

        if (canTakeRequests == false) {
            System.exit(0);
        }
    }

    public static LwjglWindowManager enqueRequest(int width, int height, String title, boolean fullscreen, Pipeline pipe,
            IGlfwEventListener listener) {
        if (!canTakeRequests) {
            // Only happens when the main window has asked to be closed
            System.out.println("Could not take the request!");
            return null;
        }
        WindowRequest request = new WindowRequest(Thread.currentThread(), width, height, title, fullscreen, pipe, listener);
        System.out.println("New " + request.toString());
        requests.add(request);
        if (instance == null) {
            System.out.println("Creating a new GLFW Manager Thread");
            instance = new GLFWManagerThread();
            new Thread(instance, "glfw-manager").start();
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}

        if (!request.isReady()) {
            System.out.println("The window was not ready!");
        }
        System.out.println("Request successful");
        return request.window;
    }
}
