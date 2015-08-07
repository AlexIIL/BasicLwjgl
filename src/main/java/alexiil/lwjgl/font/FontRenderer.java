package alexiil.lwjgl.font;

import alexiil.lwjgl.list.OpenGlCallList;

public class FontRenderer {
    public static FontRenderer currentRenderer = new FontRenderer();

    public static Runnable renderFont(OpenGlCallList list, String text, int size) {
        return currentRenderer.renderFontInternal(list, text, size);
    }

    public static int[] getSize(String text, int size) {
        return new int[] { 0, 0 };
    }

    public FontRenderer() {

    }

    protected Runnable renderFontInternal(OpenGlCallList list, String text, int size) {
        return () -> {

        };
    }
}
