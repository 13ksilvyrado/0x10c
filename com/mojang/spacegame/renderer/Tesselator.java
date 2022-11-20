package com.mojang.spacegame.renderer;

import com.mojang.spacegame.MemoryTracker;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import util.GLX;

public class Tesselator {
    private static boolean TRIANGLE_MODE = false;
    private static boolean USE_VBO = false;
    private static final int MAX_MEMORY_USE = 0x1000000;
    private static final int MAX_FLOATS = 0x200000;
    private ByteBuffer buffer;
    private IntBuffer ib;
    private FloatBuffer fb;
    private ShortBuffer sb;
    private int[] array;
    private int vertices = 0;
    private double u;
    private double v;
    private int tex2;
    private int col;
    private boolean hasColor = false;
    private boolean hasTexture = false;
    private boolean hasTexture2 = false;
    private boolean hasNormal = false;
    private int p = 0;
    private int count = 0;
    private boolean noColor = false;
    private int mode;
    private double xo;
    private double yo;
    private double zo;
    private int normal;
    public static final Tesselator instance = new Tesselator(0x200000);
    private boolean tesselating = false;
    private boolean vboMode = false;
    private IntBuffer vboIds;
    private int vboId = 0;
    private int vboCounts = 10;
    private int size;

    private Tesselator(int size) {
        this.size = size;
        this.buffer = MemoryTracker.createByteBuffer(size * 4);
        this.ib = this.buffer.asIntBuffer();
        this.fb = this.buffer.asFloatBuffer();
        this.sb = this.buffer.asShortBuffer();
        this.array = new int[size];
        boolean bl = this.vboMode = USE_VBO && GLContext.getCapabilities().GL_ARB_vertex_buffer_object;
        if (this.vboMode) {
            this.vboIds = MemoryTracker.createIntBuffer(this.vboCounts);
            ARBVertexBufferObject.glGenBuffersARB((IntBuffer)this.vboIds);
        }
    }

    public Tesselator getUniqueInstance(int size) {
        return new Tesselator(size);
    }

    public void end() {
        if (!this.tesselating) {
            throw new IllegalStateException("Not tesselating!");
        }
        this.tesselating = false;
        if (this.vertices > 0) {
            this.ib.clear();
            this.ib.put(this.array, 0, this.p);
            this.buffer.position(0);
            this.buffer.limit(this.p * 4);
            if (this.vboMode) {
                this.vboId = (this.vboId + 1) % this.vboCounts;
                ARBVertexBufferObject.glBindBufferARB((int)34962, (int)this.vboIds.get(this.vboId));
                ARBVertexBufferObject.glBufferDataARB((int)34962, (ByteBuffer)this.buffer, (int)35040);
            }
            if (this.hasTexture) {
                if (this.vboMode) {
                    GL11.glTexCoordPointer((int)2, (int)5126, (int)32, (long)12L);
                } else {
                    this.fb.position(3);
                    GL11.glTexCoordPointer((int)2, (int)32, (FloatBuffer)this.fb);
                }
                GL11.glEnableClientState((int)32888);
            }
            if (this.hasTexture2) {
                GLX.glClientActiveTexture(GLX.GL_TEXTURE1);
                if (this.vboMode) {
                    GL11.glTexCoordPointer((int)2, (int)5122, (int)32, (long)24L);
                } else {
                    this.sb.position(14);
                    GL11.glTexCoordPointer((int)2, (int)32, (ShortBuffer)this.sb);
                }
                GL11.glEnableClientState((int)32888);
                GLX.glClientActiveTexture(GLX.GL_TEXTURE0);
            }
            if (this.hasColor) {
                if (this.vboMode) {
                    GL11.glColorPointer((int)4, (int)5121, (int)32, (long)20L);
                } else {
                    this.buffer.position(20);
                    GL11.glColorPointer((int)4, (boolean)true, (int)32, (ByteBuffer)this.buffer);
                }
                GL11.glEnableClientState((int)32886);
            }
            if (this.hasNormal) {
                if (this.vboMode) {
                    GL11.glNormalPointer((int)5121, (int)32, (long)24L);
                } else {
                    this.buffer.position(24);
                    GL11.glNormalPointer((int)32, (ByteBuffer)this.buffer);
                }
                GL11.glEnableClientState((int)32885);
            }
            if (this.vboMode) {
                GL11.glVertexPointer((int)3, (int)5126, (int)32, (long)0L);
            } else {
                this.fb.position(0);
                GL11.glVertexPointer((int)3, (int)32, (FloatBuffer)this.fb);
            }
            GL11.glEnableClientState((int)32884);
            if (this.mode == 7 && TRIANGLE_MODE) {
                GL11.glDrawArrays((int)4, (int)0, (int)this.vertices);
            } else {
                GL11.glDrawArrays((int)this.mode, (int)0, (int)this.vertices);
            }
            GL11.glDisableClientState((int)32884);
            if (this.hasTexture) {
                GL11.glDisableClientState((int)32888);
            }
            if (this.hasTexture2) {
                GLX.glClientActiveTexture(GLX.GL_TEXTURE1);
                GL11.glDisableClientState((int)32888);
                GLX.glClientActiveTexture(GLX.GL_TEXTURE0);
            }
            if (this.hasColor) {
                GL11.glDisableClientState((int)32886);
            }
            if (this.hasNormal) {
                GL11.glDisableClientState((int)32885);
            }
        }
        this.clear();
    }

    private void clear() {
        this.vertices = 0;
        this.buffer.clear();
        this.p = 0;
        this.count = 0;
    }

    public void begin() {
        this.begin(4);
    }

    public void begin(int mode) {
        if (this.tesselating) {
            throw new IllegalStateException("Already tesselating!");
        }
        this.tesselating = true;
        this.clear();
        this.mode = mode;
        this.hasNormal = false;
        this.hasColor = false;
        this.hasTexture = false;
        this.hasTexture2 = false;
        this.noColor = false;
    }

    public void tex(double u, double v) {
        this.hasTexture = true;
        this.u = u;
        this.v = v;
    }

    public void tex2(int tex2) {
        this.hasTexture2 = true;
        this.tex2 = tex2;
    }

    public void color(float r, float g, float b) {
        this.color((int)(r * 255.0f), (int)(g * 255.0f), (int)(b * 255.0f));
    }

    public void color(float r, float g, float b, float a) {
        this.color((int)(r * 255.0f), (int)(g * 255.0f), (int)(b * 255.0f), (int)(a * 255.0f));
    }

    public void color(int r, int g, int b) {
        this.color(r, g, b, 255);
    }

    public void color(int r, int g, int b, int a) {
        if (this.noColor) {
            return;
        }
        if (r > 255) {
            r = 255;
        }
        if (g > 255) {
            g = 255;
        }
        if (b > 255) {
            b = 255;
        }
        if (a > 255) {
            a = 255;
        }
        if (r < 0) {
            r = 0;
        }
        if (g < 0) {
            g = 0;
        }
        if (b < 0) {
            b = 0;
        }
        if (a < 0) {
            a = 0;
        }
        this.hasColor = true;
        this.col = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? a << 24 | b << 16 | g << 8 | r : r << 24 | g << 16 | b << 8 | a;
    }

    public void color(byte r, byte g, byte b) {
        this.color(r & 0xFF, g & 0xFF, b & 0xFF);
    }

    public void vertexUV(double x, double y, double z, double u, double v) {
        this.tex(u, v);
        this.vertex(x, y, z);
    }

    public void vertex(double x, double y, double z) {
        ++this.count;
        if (this.mode == 7 && TRIANGLE_MODE && this.count % 4 == 0) {
            int i = 0;
            while (i < 2) {
                int offs = 8 * (3 - i);
                if (this.hasTexture) {
                    this.array[this.p + 3] = this.array[this.p - offs + 3];
                    this.array[this.p + 4] = this.array[this.p - offs + 4];
                }
                if (this.hasTexture2) {
                    this.array[this.p + 7] = this.array[this.p - offs + 7];
                }
                if (this.hasColor) {
                    this.array[this.p + 5] = this.array[this.p - offs + 5];
                }
                this.array[this.p + 0] = this.array[this.p - offs + 0];
                this.array[this.p + 1] = this.array[this.p - offs + 1];
                this.array[this.p + 2] = this.array[this.p - offs + 2];
                ++this.vertices;
                this.p += 8;
                ++i;
            }
        }
        if (this.hasTexture) {
            this.array[this.p + 3] = Float.floatToRawIntBits((float)this.u);
            this.array[this.p + 4] = Float.floatToRawIntBits((float)this.v);
        }
        if (this.hasTexture2) {
            this.array[this.p + 7] = this.tex2;
        }
        if (this.hasColor) {
            this.array[this.p + 5] = this.col;
        }
        if (this.hasNormal) {
            this.array[this.p + 6] = this.normal;
        }
        this.array[this.p + 0] = Float.floatToRawIntBits((float)(x + this.xo));
        this.array[this.p + 1] = Float.floatToRawIntBits((float)(y + this.yo));
        this.array[this.p + 2] = Float.floatToRawIntBits((float)(z + this.zo));
        this.p += 8;
        ++this.vertices;
        if (this.vertices % 4 == 0 && this.p >= this.size - 32) {
            this.end();
            this.tesselating = true;
        }
    }

    public void color(int c) {
        int r = c >> 16 & 0xFF;
        int g = c >> 8 & 0xFF;
        int b = c & 0xFF;
        this.color(r, g, b);
    }

    public void color(int c, int alpha) {
        int r = c >> 16 & 0xFF;
        int g = c >> 8 & 0xFF;
        int b = c & 0xFF;
        this.color(r, g, b, alpha);
    }

    public void noColor() {
        this.noColor = true;
    }

    public void normal(float x, float y, float z) {
        this.hasNormal = true;
        byte xx = (byte)(x * 127.0f);
        byte yy = (byte)(y * 127.0f);
        byte zz = (byte)(z * 127.0f);
        this.normal = xx | yy << 8 | zz << 16;
    }

    public void offset(double xo, double yo, double zo) {
        this.xo = xo;
        this.yo = yo;
        this.zo = zo;
    }

    public void addOffset(float x, float y, float z) {
        this.xo += (double)x;
        this.yo += (double)y;
        this.zo += (double)z;
    }
}

