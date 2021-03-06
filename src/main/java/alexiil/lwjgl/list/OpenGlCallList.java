package alexiil.lwjgl.list;

import java.awt.Color;
import java.util.List;

import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import alexiil.lwjgl.font.FontRenderer;
import alexiil.utils.render.window.IRenderCallList;

public class OpenGlCallList implements IRenderCallList {
    private final List<Runnable> insns = Lists.newArrayList();
    private int listAddress, timesRendered, lastRenderedSize;
    public final ContextCapabilities caps;

    public OpenGlCallList(ContextCapabilities caps) {
        this.caps = caps;
    }

    @Override
    public void polygon(double[][] points) {
        insns.add(() -> {
            GL11.glBegin(GL11.GL_POLYGON);
            for (int i = 0; i < points.length; i++) {
                GL11.glVertex2d(points[i][0], points[i][1]);
            }
            GL11.glEnd();
        });
    }

    @Override
    public void line(double[][] points) {
        insns.add(() -> {
            GL11.glBegin(GL11.GL_LINES);
            for (int i = 0; i < points.length; i++) {
                GL11.glVertex2d(points[i][0], points[i][1]);
            }
            GL11.glEnd();
        });
    }

    @Override
    public int[] text(String text, double x, double y, int size, boolean centerX, boolean centerY) {
        insns.add(() -> {
            GL11.glPushMatrix();
            GL11.glScaled(size, size, size);
            FontRenderer.renderFont(this, text, size).run();
            GL11.glPopMatrix();
        });
        return FontRenderer.getSize(text, size);
    }

    @Override
    public void colour(Color c) {
        insns.add(() -> {
            GL11.glColor3i(c.getRed(), c.getGreen(), c.getBlue());
        });
    }

    @Override
    public void list(IRenderCallList list) {
        insns.add(() -> {
            ((OpenGlCallList) list).render();
        });
    }

    @Override
    public void pushState() {
        insns.add(() -> {
            GL11.glPushMatrix();
        });
    }

    @Override
    public void scale(double mul) {
        insns.add(() -> {
            GL11.glScaled(mul, mul, 1);
        });
    }

    @Override
    public void offset(double x, double y) {
        insns.add(() -> {
            GL11.glTranslated(x, y, 0);
        });
    }

    @Override
    public void rotate(double degrees) {
        insns.add(() -> {
            GL11.glRotated(degrees, 0, 0, 1);
        });
    }

    @Override
    public void popState() {
        insns.add(() -> {
            GL11.glPopMatrix();
        });
    }

    @Override
    public void dispose() {
        if (listAddress != -1) {
            GL11.glDeleteLists(listAddress, 1);
        }
    }

    public void render() {
        int size = insns.size();
        if (size != lastRenderedSize) {
            lastRenderedSize = size;
            dispose();
            listAddress = -1;
        }

        if (listAddress != -1) {
            GL11.glPushMatrix();
            GL11.glCallList(listAddress);
            GL11.glPopMatrix();
            return;
        }
        listAddress = GL11.glGenLists(1);

        GL11.glNewList(listAddress, GL11.GL_COMPILE);

        GL11.glPushMatrix();

        for (Runnable insn : insns)
            insn.run();

        GL11.glPopMatrix();

        GL11.glEndList();
        GL11.glCallList(listAddress);
    }
}
