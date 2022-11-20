package com.mojang.spacegame.renderer;

import com.mojang.spacegame.MemoryTracker;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import javax.imageio.ImageIO;
import org.lwjgl.opengl.GL11;
import util.IntHashMap;
import util.LongHashMap;

public class Textures<String> {
    public static final Textures instance = new Textures();
    public static boolean MIPMAP = false;
    private LongHashMap<String, Integer> idMap = new HashMap();
    private HashMap<String, int[]> pixelsMap = new HashMap();
    private IntHashMap<BufferedImage> loadedImages = new IntHashMap();
    private IntBuffer ib = MemoryTracker.createIntBuffer(1);
    private ByteBuffer pixels = MemoryTracker.createByteBuffer(0x1000000);
    public boolean clamp = false;
    public boolean blur = false;
    private BufferedImage missingNo = new BufferedImage(64, 64, 2);
    byte[] newPixels = new byte[1];

    private Textures() {
        Graphics graphics = this.missingNo.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, 64, 64);
        graphics.setColor(Color.BLACK);
        graphics.drawString("missingtex", 1, 10);
        graphics.dispose();
    }

    public int[] loadTexturePixels(String string) {
        Class<Textures> clazz = Textures.class;
        int[] nArray = this.pixelsMap.get(string);
        if (nArray != null) {
            return nArray;
        }
        try {
            int[] nArray2 = null;
            InputStream inputStream = clazz.getResourceAsStream(string);
            nArray2 = inputStream == null ? this.loadTexturePixels(this.missingNo) : this.loadTexturePixels(this.readImage(inputStream));
            this.pixelsMap.put(string, nArray2);
            return nArray2;
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
            int[] nArray3 = this.loadTexturePixels(this.missingNo);
            this.pixelsMap.put(string, nArray3);
            return nArray3;
        }
    }

    private int[] loadTexturePixels(BufferedImage bufferedImage) {
        int n = bufferedImage.getWidth();
        int n2 = bufferedImage.getHeight();
        int[] nArray = new int[n * n2];
        bufferedImage.getRGB(0, 0, n, n2, nArray, 0, n);
        return nArray;
    }

    private int[] loadTexturePixels(BufferedImage bufferedImage, int[] nArray) {
        int n = bufferedImage.getWidth();
        int n2 = bufferedImage.getHeight();
        bufferedImage.getRGB(0, 0, n, n2, nArray, 0, n);
        return nArray;
    }

    public int loadTexture(String string) {
        Class<Textures> clazz = Textures.class;
        Integer n = this.idMap.get(string);
        if (n != null) {
            return n;
        }
        try {
            this.ib.clear();
            MemoryTracker.genTextures(this.ib);
            n = this.ib.get(0);
            InputStream inputStream = clazz.getResourceAsStream(string);
            if (inputStream == null) {
                this.loadTexture(this.missingNo, n);
            } else {
                this.loadTexture(this.readImage(inputStream), n);
            }
            this.idMap.put(string, (int)n);
            return n;
        }
        catch (Exception exception) {
            exception.printStackTrace();
            MemoryTracker.genTextures(this.ib);
            n = this.ib.get(0);
            this.loadTexture(this.missingNo, n);
            this.idMap.put(string, (int)n);
            return n;
        }
    }

    public int generateCubeMap() {
        this.ib.clear();
        MemoryTracker.genTextures(this.ib);
        int n = this.ib.get(0);
        Random random = new Random();
        byte[] byArray = new byte[0x100000];
        for (int i = 0; i < byArray.length / 4; ++i) {
            int n2 = 255;
            int n3 = random.nextInt(256);
            int n4 = random.nextInt(256);
            int n5 = random.nextInt(256);
            byArray[i * 4 + 0] = (byte)n3;
            byArray[i * 4 + 1] = (byte)n4;
            byArray[i * 4 + 2] = (byte)n5;
            byArray[i * 4 + 3] = (byte)n2;
        }
        this.pixels.clear();
        this.pixels.put(byArray);
        this.pixels.position(0).limit(byArray.length);
        GL11.glBindTexture((int)34067, (int)n);
        GL11.glTexImage2D((int)34069, (int)0, (int)6408, (int)512, (int)512, (int)0, (int)6408, (int)5121, (ByteBuffer)this.pixels);
        GL11.glTexImage2D((int)34070, (int)0, (int)6408, (int)512, (int)512, (int)0, (int)6408, (int)5121, (ByteBuffer)this.pixels);
        GL11.glTexImage2D((int)34071, (int)0, (int)6408, (int)512, (int)512, (int)0, (int)6408, (int)5121, (ByteBuffer)this.pixels);
        GL11.glTexImage2D((int)34072, (int)0, (int)6408, (int)512, (int)512, (int)0, (int)6408, (int)5121, (ByteBuffer)this.pixels);
        GL11.glTexImage2D((int)34073, (int)0, (int)6408, (int)512, (int)512, (int)0, (int)6408, (int)5121, (ByteBuffer)this.pixels);
        GL11.glTexImage2D((int)34074, (int)0, (int)6408, (int)512, (int)512, (int)0, (int)6408, (int)5121, (ByteBuffer)this.pixels);
        GL11.glTexParameteri((int)34067, (int)10241, (int)9728);
        GL11.glTexParameteri((int)34067, (int)10240, (int)9728);
        GL11.glTexParameteri((int)3553, (int)10242, (int)33069);
        GL11.glTexParameteri((int)3553, (int)10243, (int)33069);
        return n;
    }

    public int getTexture(BufferedImage bufferedImage) {
        this.ib.clear();
        MemoryTracker.genTextures(this.ib);
        int n = this.ib.get(0);
        this.loadTexture(bufferedImage, n);
        this.loadedImages.put(n, bufferedImage);
        return n;
    }

    public void loadTexture(BufferedImage bufferedImage, int n) {
        int n2;
        int n3;
        int n4;
        int n5;
        int n6;
        GL11.glBindTexture((int)3553, (int)n);
        if (MIPMAP) {
            GL11.glTexParameteri((int)3553, (int)10241, (int)9986);
            GL11.glTexParameteri((int)3553, (int)10240, (int)9728);
        } else {
            GL11.glTexParameteri((int)3553, (int)10241, (int)9728);
            GL11.glTexParameteri((int)3553, (int)10240, (int)9728);
        }
        if (this.blur) {
            GL11.glTexParameteri((int)3553, (int)10241, (int)9729);
            GL11.glTexParameteri((int)3553, (int)10240, (int)9729);
        }
        if (this.clamp) {
            GL11.glTexParameteri((int)3553, (int)10242, (int)33071);
            GL11.glTexParameteri((int)3553, (int)10243, (int)33071);
        } else {
            GL11.glTexParameteri((int)3553, (int)10242, (int)10497);
            GL11.glTexParameteri((int)3553, (int)10243, (int)10497);
        }
        int n7 = bufferedImage.getWidth();
        int n8 = bufferedImage.getHeight();
        int[] nArray = new int[n7 * n8];
        byte[] byArray = new byte[n7 * n8 * 4];
        bufferedImage.getRGB(0, 0, n7, n8, nArray, 0, n7);
        for (n6 = 0; n6 < nArray.length; ++n6) {
            n5 = nArray[n6] >> 24 & 0xFF;
            n4 = nArray[n6] >> 16 & 0xFF;
            n3 = nArray[n6] >> 8 & 0xFF;
            n2 = nArray[n6] & 0xFF;
            byArray[n6 * 4 + 0] = (byte)n4;
            byArray[n6 * 4 + 1] = (byte)n3;
            byArray[n6 * 4 + 2] = (byte)n2;
            byArray[n6 * 4 + 3] = (byte)n5;
        }
        this.pixels.clear();
        this.pixels.put(byArray);
        this.pixels.position(0).limit(byArray.length);
        GL11.glTexImage2D((int)3553, (int)0, (int)6408, (int)n7, (int)n8, (int)0, (int)6408, (int)5121, (ByteBuffer)this.pixels);
        if (MIPMAP) {
            for (n6 = 1; n6 <= 7; ++n6) {
                n5 = n7 >> n6 - 1;
                n4 = n7 >> n6;
                n3 = n8 >> n6;
                for (n2 = 0; n2 < n4; ++n2) {
                    for (int i = 0; i < n3; ++i) {
                        int n9 = this.pixels.getInt((n2 * 2 + 0 + (i * 2 + 0) * n5) * 4);
                        int n10 = this.pixels.getInt((n2 * 2 + 1 + (i * 2 + 0) * n5) * 4);
                        int n11 = this.pixels.getInt((n2 * 2 + 1 + (i * 2 + 1) * n5) * 4);
                        int n12 = this.pixels.getInt((n2 * 2 + 0 + (i * 2 + 1) * n5) * 4);
                        int n13 = this.crispBlend(this.crispBlend(n9, n10), this.crispBlend(n11, n12));
                        this.pixels.putInt((n2 + i * n4) * 4, n13);
                    }
                }
                GL11.glTexImage2D((int)3553, (int)n6, (int)6408, (int)n4, (int)n3, (int)0, (int)6408, (int)5121, (ByteBuffer)this.pixels);
            }
        }
    }

    public void replaceTexture(int[] nArray, int n, int n2, int n3) {
        GL11.glBindTexture((int)3553, (int)n3);
        int n4 = 0;
        int n5 = n * n2 * 4;
        if (this.newPixels.length < n5) {
            this.newPixels = new byte[n5];
        }
        for (int i = 0; i < n * n2; ++i) {
            int n6 = nArray[i] >> 24 & 0xFF;
            int n7 = nArray[i] >> 16 & 0xFF;
            int n8 = nArray[i] >> 8 & 0xFF;
            int n9 = nArray[i] & 0xFF;
            this.newPixels[i * 4 + 0] = (byte)n7;
            this.newPixels[i * 4 + 1] = (byte)n8;
            this.newPixels[i * 4 + 2] = (byte)n9;
            this.newPixels[i * 4 + 3] = (byte)n6;
        }
        this.pixels.clear();
        this.pixels.put(this.newPixels, 0, n5);
        this.pixels.position(0).limit(nArray.length * 4);
        GL11.glTexSubImage2D((int)3553, (int)n4, (int)0, (int)0, (int)n, (int)n2, (int)6408, (int)5121, (ByteBuffer)this.pixels);
    }

    public void releaseTexture(int n) {
        this.loadedImages.remove(n);
        this.ib.clear();
        this.ib.put(n);
        this.ib.flip();
        GL11.glDeleteTextures((IntBuffer)this.ib);
    }

    public void tick() {
        int n = -1;
    }

    private int crispBlend(int n, int n2) {
        int n3 = (n & 0xFF000000) >> 24 & 0xFF;
        int n4 = (n2 & 0xFF000000) >> 24 & 0xFF;
        int n5 = 255;
        if (n3 + n4 == 0) {
            n3 = 1;
            n4 = 1;
            n5 = 0;
        }
        int n6 = (n >> 16 & 0xFF) * n3;
        int n7 = (n >> 8 & 0xFF) * n3;
        int n8 = (n & 0xFF) * n3;
        int n9 = (n2 >> 16 & 0xFF) * n4;
        int n10 = (n2 >> 8 & 0xFF) * n4;
        int n11 = (n2 & 0xFF) * n4;
        int n12 = (n6 + n9) / (n3 + n4);
        int n13 = (n7 + n10) / (n3 + n4);
        int n14 = (n8 + n11) / (n3 + n4);
        return n5 << 24 | n12 << 16 | n13 << 8 | n14;
    }

    public void reloadAll() {
        BufferedImage bufferedImage;
        Class<Textures> clazz = Textures.class;
        Iterator<Object> iterator = this.loadedImages.keySet().iterator();
        while (iterator.hasNext()) {
            int n = iterator.next();
            bufferedImage = this.loadedImages.get(n);
            this.loadTexture(bufferedImage, n);
        }
        for (String string : this.idMap.keySet()) {
            try {
                bufferedImage = this.readImage(clazz.getResourceAsStream(string));
                int n = this.idMap.get(string);
                this.loadTexture(bufferedImage, n);
                this.blur = false;
                this.clamp = false;
            }
            catch (IOException iOException) {
                iOException.printStackTrace();
            }
        }
        for (String string : this.pixelsMap.keySet()) {
            try {
                bufferedImage = this.readImage(clazz.getResourceAsStream(string));
                this.loadTexturePixels(bufferedImage, this.pixelsMap.get(string));
                this.blur = false;
                this.clamp = false;
            }
            catch (IOException iOException) {
                iOException.printStackTrace();
            }
        }
    }

    private BufferedImage readImage(InputStream inputStream) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(inputStream);
        inputStream.close();
        return bufferedImage;
    }

    public void bind(int n) {
        if (n < 0) {
            return;
        }
        GL11.glBindTexture((int)3553, (int)n);
    }

    public void bind(String string) {
        this.bind(this.loadTexture(string));
    }
}

