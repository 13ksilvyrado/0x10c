
package util;

import com.mojang.spacegame.MemoryTracker;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.ARBMultitexture;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GLContext;




public class GLX {
    public static int GL_TEXTURE0;
    public static int GL_TEXTURE1;
    private static boolean useArb;
    private static FloatBuffer fb;

    static {
        useArb = false;
        fb = MemoryTracker.createFloatBuffer(4);
    }

    public static void init() {
        boolean bl = useArb = GLContext.getCapabilities().GL_ARB_multitexture && !GLContext.getCapabilities().OpenGL13;
        if (useArb) {
            GL_TEXTURE0 = 33984;
            GL_TEXTURE1 = 33985;
        } else {
            GL_TEXTURE0 = 33984;
            GL_TEXTURE1 = 33985;
        }
    }

    public static void glActiveTexture(int texture) {
        if (useArb) {
            ARBMultitexture.glActiveTextureARB((int)texture);
        } else {
            GL13.glActiveTexture((int)texture);
        }
    }

    public static void glClientActiveTexture(int texture) {
        if (useArb) {
            ARBMultitexture.glClientActiveTextureARB((int)texture);
        } else {
            GL13.glClientActiveTexture((int)texture);
        }
    }

    public static void glMultiTexCoord2f(int texture, float u, float v) {
        if (useArb) {
            ARBMultitexture.glMultiTexCoord2fARB((int)texture, (float)u, (float)v);
        } else {
            GLX.glMultiTexCoord2f((int)texture, (float)u, (float)v);
        }
    }

    public static FloatBuffer getf(float a, float b, float c) {
        fb.clear();
        fb.put(a).put(b).put(c);
        fb.flip();
        return fb;
    }

    public static FloatBuffer getf(float a, float b, float c, float d) {
        fb.clear();
        fb.put(a).put(b).put(c).put(d);
        fb.flip();
        return fb;
    }
}

