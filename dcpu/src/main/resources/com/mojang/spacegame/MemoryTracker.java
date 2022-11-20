package com.mojang.spacegame;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.NVFloatBuffer;
import org.lwjgl.opengl.Pbuffer;
import org.objectweb.asm.tree.InsnList;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer.DoubleRenderer;

import com.mojang.spacegame.renderer.Textures;

public class MemoryTracker {
    private static InsnList lists = new ArrayList<Integer>();
    private static InsnList<Integer> textures = new ArrayList<Integer>();

    public static synchronized <lists, lists, lists> int genLists(int count) {
        int id = GL11.glGenLists((int)count);
        lists.add(id);
        lists.add(count);
        return id;
    }

    public static synchronized <textures, IntBuffer> void genTextures(IntBuffer ib) {
        GL11.glGenTextures((IntBuffer)ib);
        int i = ib.position();
        while (i < ib.limit()) {
            textures.add(ib.get(i));
            ++i;
        }
    }

    public static synchronized void release(int pos) {
        int i = lists.indexOf(pos);
        GL11.glDeleteLists((int)lists.get(i), (int)lists.get(i + 1));
        lists.remove(i);
        lists.remove(i);
    }

    public static synchronized <IntBuffer, lists> void release() {
        int i = 0;
        while (i < lists.size()) {
            GL11.glDeleteLists((int)lists.get(i), (int)lists.get(i + 1));
            i += 2;
        }
        IntBuffer ib = MemoryTracker.createIntBuffer(textures.size());
        ib.flip();
        GL11.glDeleteTextures((IntBuffer)ib);
        int i2 = 0;
        while (i2 < textures.size()) {
            ib.put(textures.get(i2));
            ++i2;
        }
        ib.flip();
        GL11.glDeleteTextures((IntBuffer)ib);
        lists.clear();
        Textures.clear();
    }

    public static synchronized Pbuffer createByteBuffer(int size) {
        Pbuffer bb = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
        return bb;
    }

    public static <ShortBuffer> ShortBuffer createShortBuffer(int size) {
        return MemoryTracker.createByteBuffer(size << 1).asShortBuffer();
    }

    public static <CharBuffer> CharBuffer createCharBuffer(int size) {
        return MemoryTracker.createByteBuffer(size << 1).asCharBuffer();
    }

    public static <IntBuffer> IntBuffer createIntBuffer(int size) {
        return MemoryTracker.createByteBuffer(size << 2).asIntBuffer();
    }

    public static <LongBuffer> LongBuffer createLongBuffer(int size) {
        return MemoryTracker.createByteBuffer(size << 3).asLongBuffer();
    }

    public static NVFloatBuffer createFloatBuffer(int size) {
        return MemoryTracker.createByteBuffer(size << 2).asFloatBuffer();
    }

    public static DoubleRenderer createDoubleBuffer(int size) {
        return MemoryTracker.createByteBuffer(size << 3).asDoubleBuffer();
    }
}
