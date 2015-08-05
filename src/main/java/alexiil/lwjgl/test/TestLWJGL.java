package alexiil.lwjgl.test;

import org.lwjgl.opengl.GL11;

import alexiil.lwjgl.list.OpenGlTools;
import alexiil.utils.render.window.IRenderingTools;
import alexiil.utils.render.window.IWindow;

public class TestLWJGL {
    private static boolean up = false, angUp = false;
    private static float colour = 1, angle = 1;

    public static final int HEIGHT = 1080 / 2, WIDTH = 1920 / 2, FPS = 120, TIME_DIFF = 1000 / FPS;

    private static long lastSecond = -1, lastTime = -1;
    private static int numFrames = 0;

    public static void before() {
        lastSecond = System.currentTimeMillis();
        lastTime = lastSecond;
    }

    public static void during() {
        // if (up)
        // colour += 0.001;
        // else
        // colour -= 0.001;
        // if (colour >= 0.99)
        // up = false;
        // if (colour <= 0.01)
        // up = true;

        if (angUp)
            angle += 0.001;
        else
            angle -= 0.001;

        GL11.glPushMatrix();
        // GL11.glTranslatef(0, 0.1f, 0);
        // GL11.glColor3i(0xFFFFFFFF, 0, 0xAFFFFFFF);
        GL11.glRotatef(angle * 360, 0, 1, 0);
        GL11.glColor3f(0, colour, 0);

        poly(-20, -20, 20, 20);

        GL11.glColor3f(0, 0, colour);
        GL11.glTranslatef(2, 0, 0);

        poly(0, 0, 100, 100);

        GL11.glPopMatrix();

        // GL11.glFlush();

        numFrames++;
        long now = System.currentTimeMillis();
        long diff = now - lastTime;
        lastTime = now;
        if (now - lastSecond > 1000) {
            lastSecond = now;
            System.out.println(numFrames + " fps");
            numFrames = 0;
        }
        if (diff < TIME_DIFF) {
            try {
                Thread.sleep(TIME_DIFF - diff);
            }
            catch (InterruptedException ignored) {
                System.out.println("interupted!");
                // Ignore
            }
        }
    }

    private static void poly(float x0, float y0, float x1, float y1) {
        GL11.glBegin(GL11.GL_POLYGON);
        GL11.glVertex2f(x0, y0);
        GL11.glVertex2f(x1, y0);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x0, y1);
        GL11.glEnd();
    }

    public static void main(String[] args) {
        IRenderingTools tools = OpenGlTools.init();
        IWindow window = tools.makeNewWindow();
        before();
        window.setRenderer(TestLWJGL::during);
        window.open(WIDTH, HEIGHT, "Window Title");
        window.makeMain();
    }
}
