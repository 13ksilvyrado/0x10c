
package com.mojang.spacegame;

import com.mojang.spacegame.Shader;
import com.mojang.spacegame.renderer.Tesselator;
import com.mojang.spacegame.renderer.Textures;
import computer.Assembler;
import computer.DCPU;
import computer.VirtualKeyboard;
import computer.VirtualMonitor;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;
import util.GLX;

public class SpaceGame<Object> implements Runnable {
    public static final String GAME_NAME = "SpaceGame";
    public static final String GAME_VERSION = "prototype";
    private boolean stop = false;
    private int lists;
    private Shader shadowVolumeShader;
    private Shader lightShader;
    private Shader depthShader;
    private Shader fourLight;
    private Shader distanceShader;
    private Shader displayShader;
    private int[] noiseTexture = new int[32];
    private int cubeMap;
    private DCPU cpu = new DCPU();
    private VirtualMonitor vmonitor;
    private VirtualKeyboard vkeyboard;
    private int monitorTexture;
    float time;
    private float yRot;
    private float xRot;
    private float xCam;
    private float yCam;
    private float zCam;
	private Object System;

    public SpaceGame() {
        this.vmonitor = new VirtualMonitor(this.cpu.ram, 32768);
        this.vkeyboard = new VirtualKeyboard(this.cpu.ram, 36864);
        this.monitorTexture = 0;
        this.time = 0.0f;
    }

    private static <ByteBuffer, BufferedImage, URL> ByteBuffer loadIcon(URL url) throws IOException {
        BufferedImage image = image.read(url);
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getHeight());
        ByteBuffer bb = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);
        bb.order(ByteOrder.nativeOrder());
        bb.asIntBuffer().put(pixels);
        return bb;
    }

    public void init() throws Exception {
        new Assembler(this.cpu.ram).assemble("os.asm");
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int w = gd.getDisplayMode().getWidth() * 8 / 10;
        int h = gd.getDisplayMode().getHeight() * 8 / 10;
        int x = gd.getDisplayMode().getWidth() * 1 / 10;
        int y = gd.getDisplayMode().getHeight() * 1 / 10 / 2;
        Display.setResizable((boolean)true);
        Display.setDisplayMode((DisplayMode)new DisplayMode(w, h));
        Display.setLocation((int)x, (int)y);
        Display.setTitle((String)"SpaceGame [prototype]");
        Display.setIcon((ByteBuffer[])new ByteBuffer[]{SpaceGame.loadIcon(this.getClass().getResource("/icon16.png")), SpaceGame.loadIcon(this.getClass().getResource("/icon32.png"))});
        Display.create((PixelFormat)new PixelFormat().withDepthBits(24).withBitsPerPixel(32).withStencilBits(8));
        Keyboard.create();
        Mouse.create();
        Mouse.setGrabbed((boolean)true);
        Controllers.create();
        int controllerCount = Controllers.getControllerCount();
        System.out.println("controllers: " + controllerCount);
        int i = 0;
        while (i < controllerCount) {
            Controller c = Controllers.getController((int)i);
            System.out.println("Controller " + i + ": " + c.getName());
            System.out.println("Axis: " + c.getAxisCount());
            int j = 0;
            while (j < c.getAxisCount()) {
                System.out.println("   Axis " + j + ": " + c.getAxisName(j));
                ++j;
            }
            System.out.println("Buttons: " + c.getButtonCount());
            j = 0;
            while (j < c.getButtonCount()) {
                System.out.println("   Button " + j + ": " + c.getButtonName(j));
                ++j;
            }
            ++i;
        }
        GL11.glShadeModel((int)7425);
        this.lightShader = new Shader("test");
        this.depthShader = new Shader("depth");
        this.fourLight = new Shader("4light");
        this.distanceShader = new Shader("dist");
        this.shadowVolumeShader = new Shader("shadowVolume");
        this.displayShader = new Shader("display");
        int j = 0;
        while (j < 32) {
            Random random = new Random();
            BufferedImage noise = new BufferedImage(256, 256, 2);
            int[] pixels = new int[65536];
            int i2 = 0;
            while (i2 < pixels.length) {
                pixels[i2] = random.nextInt();
                ++i2;
            }
            noise.setRGB(0, 0, 256, 256, pixels, 0, 256);
            this.noiseTexture[j] = Textures.instance.getTexture(noise);
            ++j;
        }
        this.lists = GL11.glGenLists((int)2);
        GL11.glNewList((int)this.lists, (int)4864);
        this.renderScene();
        GL11.glEndList();
        GL11.glNewList((int)(this.lists + 1), (int)4864);
        this.renderShadows();
        GL11.glEndList();
        this.cubeMap = Textures.instance.generateCubeMap();
        this.monitorTexture = Textures.instance.getTexture(new BufferedImage(128, 128, 2));
        this.xCam = 5.1f;
        this.zCam = 7.3f;
        this.yRot = 10.0f;
        this.xRot = -50.0f;
    }

    public void renderScene() {
        this.renderMonitor(false);
        this.renderRoom(0.0, 0.0, 0.0, 8.0, 3.0, 8.0);
        float s = 0.2f;
        Random random = new Random(1000L);
        int i = 0;
        while (i < 10) {
            GL11.glPushMatrix();
            float x = (float)(random.nextDouble() - 0.5) * 4.0f + 4.0f;
            float y = (float)random.nextDouble() * 2.5f;
            float z = (float)(random.nextDouble() - 0.5) * 4.0f + 4.0f;
            GL11.glTranslatef((float)x, (float)y, (float)z);
            GL11.glRotatef((float)(-this.time * 360.0f), (float)0.0f, (float)1.0f, (float)0.0f);
            GL11.glRotatef((float)(random.nextFloat() * 360.0f), (float)0.0f, (float)0.0f, (float)1.0f);
            GL11.glRotatef((float)(random.nextFloat() * 360.0f), (float)1.0f, (float)0.0f, (float)0.0f);
            this.box(-s, -s, -s, s, s, s);
            GL11.glPopMatrix();
            ++i;
        }
    }

    public void renderShadows() {
        this.renderMonitorShadow();
        this.renderShadowRoom(0.0, 0.0, 0.0, 8.0, 3.0, 8.0);
        float s = 0.2f;
        Random random = new Random(1000L);
        int i = 0;
        while (i < 10) {
            GL11.glPushMatrix();
            float x = (float)(random.nextDouble() - 0.5) * 4.0f + 4.0f;
            float y = (float)random.nextDouble() * 2.5f;
            float z = (float)(random.nextDouble() - 0.5) * 4.0f + 4.0f;
            GL11.glTranslatef((float)x, (float)y, (float)z);
            GL11.glRotatef((float)(-this.time * 360.0f), (float)0.0f, (float)1.0f, (float)0.0f);
            GL11.glRotatef((float)(random.nextFloat() * 360.0f), (float)0.0f, (float)0.0f, (float)1.0f);
            GL11.glRotatef((float)(random.nextFloat() * 360.0f), (float)1.0f, (float)0.0f, (float)0.0f);
            this.shadowBox(-s, -s, -s, s, s, s);
            GL11.glPopMatrix();
            ++i;
        }
    }

    private void renderShadowRoom(double x0, double y0, double z0, double x1, double y1, double z1) {
        double t = 0.2f;
        this.shadowBox(x0 - t, y0, z0, x0, y1, z1);
        this.shadowBox(x1, y0, z0, x1 + t, y1, z1);
        this.shadowBox(x0 - t, y0 - t, z0 - t, x1 + t, y0, z1 + t);
        this.shadowBox(x0 - t, y1, z0 - t, x1 + t, y1 + t, z1 + t);
        double xm = (x0 + x1) / 2.0;
        double ym = (y0 + y1) / 2.0;
        this.shadowBox(x0, y0, z0 - t, xm - 2.0, y1, z0);
        this.shadowBox(xm + 2.0, y0, z0 - t, x1, y1, z0);
        this.shadowBox(xm - 2.0, y0, z0 - t, xm + 2.0, ym - 1.0, z0);
        this.shadowBox(xm - 2.0, ym + 1.0, z0 - t, xm + 2.0, y1, z0);
        this.shadowBox(x0, y0, z1, x1, y1, z1 + t);
    }

    private void renderRoom(double x0, double y0, double z0, double x1, double y1, double z1) {
        double t = 0.2f;
        this.box(x0 - t, y0, z0, x0, y1, z1);
        this.box(x1, y0, z0, x1 + t, y1, z1);
        this.box(x0 - t, y0 - t, z0 - t, x1 + t, y0, z1 + t);
        this.box(x0 - t, y1, z0 - t, x1 + t, y1 + t, z1 + t);
        double xm = (x0 + x1) / 2.0;
        double ym = (y0 + y1) / 2.0;
        this.box(x0, y0, z0 - t, xm - 2.0, y1, z0);
        this.box(xm + 2.0, y0, z0 - t, x1, y1, z0);
        this.box(xm - 2.0, y0, z0 - t, xm + 2.0, ym - 1.0, z0);
        this.box(xm - 2.0, ym + 1.0, z0 - t, xm + 2.0, y1, z0);
        this.box(x0, y0, z1, x1, y1, z1 + t);
    }

    public void renderMonitorShadow() {
        if (this != null) {
            return;
        }
        double x = 0.0;
        double y = 1.32;
        double z = 3.9;
        double scale = 2.0;
        double th = 0.01 * scale;
        double w = 0.5 * scale;
        double h = 0.375 * scale;
        double d = 0.05 * scale;
        GL11.glPushMatrix();
        GL11.glTranslatef((float)((float)x), (float)((float)y), (float)((float)z));
        GL11.glRotatef((float)-90.0f, (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glTranslatef((float)((float)(-w / 2.0)), (float)0.0f, (float)((float)(-d)));
        this.shadowBox(0.0, 0.0, d - th, w, h, d);
        this.shadowBox(-th, 0.0, 0.0, 0.0, h, d);
        this.shadowBox(w, 0.0, 0.0, w + th, h, d);
        this.shadowBox(-th, -th, 0.0, w + th, 0.0, d);
        this.shadowBox(-th, h, 0.0, w + th, h + th, d);
        GL11.glPopMatrix();
    }

    public void renderMonitor(boolean display) {
        double x = 0.0;
        double y = 1.32;
        double z = 3.9;
        double scale = 2.0;
        double th = 0.01 * scale;
        double w = 0.5 * scale;
        double h = 0.375 * scale;
        double d = 0.05 * scale;
        GL11.glPushMatrix();
        GL11.glTranslatef((float)((float)x), (float)((float)y), (float)((float)z));
        GL11.glRotatef((float)-90.0f, (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glTranslatef((float)((float)(-w / 2.0)), (float)0.0f, (float)((float)(-d)));
        if (display) {
            double zz = 0.015;
            Tesselator t = Tesselator.instance;
            GL11.glEnable((int)3042);
            GL11.glBlendFunc((int)770, (int)771);
            t.begin(7);
            t.color(0x202020, 240);
            t.vertexUV(0.0, h, zz, 1.0, 0.0);
            t.vertexUV(w, h, zz, 0.0, 0.0);
            t.vertexUV(w, 0.0, zz, 0.0, 0.75);
            t.vertexUV(0.0, 0.0, zz, 1.0, 0.75);
            t.end();
            GL11.glEnable((int)3553);
            this.vmonitor.render();
            Textures.instance.replaceTexture(this.vmonitor.pixels, 128, 128, this.monitorTexture);
            GL11.glBindTexture((int)3553, (int)this.monitorTexture);
            this.displayShader.enable();
            GL11.glBlendFunc((int)1, (int)1);
            zz = 0.012;
            int b = 6;
            t.begin(7);
            t.color(0xFFFFFF);
            t.vertexUV(0.0, h, zz, (double)(255 + b) / 128.0, (double)(127 - b) / 128.0);
            t.vertexUV(w, h, zz, (double)(127 - b) / 128.0, (double)(127 - b) / 128.0);
            t.vertexUV(w, 0.0, zz, (double)(127 - b) / 128.0, (223.0 + (double)b) / 128.0);
            t.vertexUV(0.0, 0.0, zz, (double)(255 + b) / 128.0, (223.0 + (double)b) / 128.0);
            t.end();
            GL11.glDisable((int)3553);
            GL11.glDisable((int)3042);
            this.displayShader.disable();
        } else {
            this.box(0.0, 0.0, d - th, w, h, d);
            this.box(-th, 0.0, 0.0, 0.0, h, d);
            this.box(w, 0.0, 0.0, w + th, h, d);
            this.box(-th, -th, 0.0, w + th, 0.0, d);
            this.box(-th, h, 0.0, w + th, h + th, d);
        }
        GL11.glPopMatrix();
    }

    public void gameLoop() throws LWJGLException {
        int i = 0;
        while (i < 100) {
            this.cpu.tick();
            ++i;
        }
        GL11.glBindTexture((int)34067, (int)this.cubeMap);
        this.time = (float)(System.currentTimeMillis() % 10000L) / 10000.0f;
        int lights = 1;
        if (!Mouse.isGrabbed()) {
            while (Mouse.next()) {
                if (Mouse.getEventButton() != 0 || !Mouse.getEventButtonState()) continue;
                Mouse.setGrabbed((boolean)true);
            }
        } else {
            if (!Display.isCurrent() || !Display.isActive()) {
                System.out.println("Release!!");
                Mouse.setGrabbed((boolean)false);
            }
            while (Keyboard.next()) {
                if (!Keyboard.getEventKeyState()) continue;
                if (Keyboard.getEventKey() == 1) {
                    Mouse.setGrabbed((boolean)false);
                    continue;
                }
                this.vkeyboard.keyTyped(Keyboard.getEventCharacter() & 0x7F);
            }
            this.xRot += (float)Mouse.getDX() * 0.1f;
            this.yRot -= (float)Mouse.getDY() * 0.1f;
            if (this.yRot < -90.0f) {
                this.yRot = -90.0f;
            }
            if (this.yRot > 90.0f) {
                this.yRot = 90.0f;
            }
            double xa = 0.0;
            double ya = 0.0;
            if (Keyboard.isKeyDown((int)17)) {
                ya += 1.0;
            }
            if (Keyboard.isKeyDown((int)30)) {
                xa -= 1.0;
            }
            if (Keyboard.isKeyDown((int)31)) {
                ya -= 1.0;
            }
            if (Keyboard.isKeyDown((int)32)) {
                xa += 1.0;
            }
            this.yCam = 1.7f;
            double rott = (double)this.xRot * Math.PI * 2.0 / 360.0;
            double c = Math.cos(rott);
            double s = Math.sin(rott);
            double speed = 0.01;
            this.zCam = (float)((double)this.zCam + (s * xa - c * ya) * speed);
            this.xCam = (float)((double)this.xCam + (c * xa + s * ya) * speed);
        }
        float r = 0.0f;
        float g = 0.0f;
        float b = 0.0f;
        GL11.glClearColor((float)r, (float)g, (float)b, (float)1.0f);
        GL11.glClear((int)17664);
        int w = Display.getWidth();
        int h = Display.getHeight();
        if (w < 256 || h < 256) {
            if (w < 256) {
                w = 256;
            }
            if (h < 256) {
                h = 256;
            }
            try {
                Display.setResizable((boolean)false);
                Display.setDisplayMode((DisplayMode)new DisplayMode(w, h));
                Display.setResizable((boolean)true);
            }
            catch (LWJGLException e) {
                e.printStackTrace();
            }
        }
        float aspect = (float)w / (float)h;
        float rot = (float)((double)this.time * Math.PI * 2.0);
        GL11.glEnable((int)2929);
        GL11.glEnable((int)2884);
        GL11.glDepthFunc((int)515);
        GL11.glShadeModel((int)7425);
        GL11.glCullFace((int)1029);
        GL11.glViewport((int)0, (int)0, (int)w, (int)h);
        GL11.glClear((int)16640);
        GL11.glMatrixMode((int)5889);
        GL11.glLoadIdentity();
        GLU.gluPerspective((float)70.0f, (float)aspect, (float)0.1f, (float)40.0f);
        GL11.glMatrixMode((int)5888);
        GL11.glLoadIdentity();
        GL11.glRotatef((float)this.yRot, (float)1.0f, (float)0.0f, (float)0.0f);
        GL11.glRotatef((float)this.xRot, (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glTranslatef((float)(-this.xCam), (float)(-this.yCam), (float)(-this.zCam));
        GL11.glDisable((int)3042);
        this.depthShader.enable();
        this.renderScene();
        GL11.glDepthMask((boolean)false);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)1, (int)1);
        Random random = new Random(600L);
        int i2 = 0;
        while (i2 <= lights) {
            GL11.glBindTexture((int)3553, (int)this.noiseTexture[i2 % this.noiseTexture.length]);
            int j = 0;
            while (j < 1) {
                float dist = random.nextFloat() * 4.0f;
                float xx = (float)Math.sin((double)rot + (double)((float)i2 / (float)lights) * Math.PI * 2.0) * dist + 4.0f;
                float yy = random.nextFloat() * 2.8f + 0.1f;
                float zz = (float)Math.cos((double)rot + (double)((float)i2 / (float)lights) * Math.PI * 2.0) * dist + 4.0f;
                float rr = (float)(Math.sin(((double)((float)i2 / (float)lights) + 0.0) * Math.PI * 2.0) * 0.05 + 0.55);
                float gg = (float)(Math.sin(((double)((float)i2 / (float)lights) + 0.3333333333333333) * Math.PI * 2.0) * 0.05 + 0.55);
                float bb = (float)(Math.sin(((double)((float)i2 / (float)lights) + 0.6666666666666666) * Math.PI * 2.0) * 0.05 + 0.55);
                GL11.glLightf((int)(16384 + j), (int)4617, (float)0.2f);
                if (i2 == lights) {
                    xx = 0.3f;
                    yy = 1.7f;
                    zz = 3.95f;
                    rr = 0.5f;
                    gg = 0.5f;
                    bb = 1.0f;
                    GL11.glLightf((int)(16384 + j), (int)4617, (float)2.0f);
                } else {
                    bb = 0.0f;
                    gg = 0.0f;
                    rr = 0.0f;
                }
                GL11.glLight((int)(16384 + j), (int)4611, (FloatBuffer)GLX.getf(xx, yy, zz, 1.0f));
                GL11.glLight((int)(16384 + j), (int)4609, (FloatBuffer)GLX.getf(rr, gg, bb, 1.0f));
                GL11.glLight((int)(16384 + j), (int)4608, (FloatBuffer)GLX.getf(0.0f, 0.0f, 0.0f, 1.0f));
                GL11.glLight((int)(16384 + j), (int)4610, (FloatBuffer)GLX.getf(0.0f, 0.0f, 0.0f, 1.0f));
                GL11.glLightf((int)(16384 + j), (int)4615, (float)0.0f);
                ++j;
            }
            GL11.glClear((int)1024);
            GL11.glEnable((int)2960);
            this.shadowVolumeShader.enable();
            GL11.glColorMask((boolean)false, (boolean)false, (boolean)false, (boolean)false);
            GL11.glDepthFunc((int)513);
            GL11.glCullFace((int)1028);
            GL11.glStencilFunc((int)519, (int)0, (int)255);
            GL11.glStencilOp((int)7680, (int)7682, (int)7680);
            this.renderShadows();
            GL11.glCullFace((int)1029);
            GL11.glStencilFunc((int)519, (int)0, (int)255);
            GL11.glStencilOp((int)7680, (int)7683, (int)7680);
            this.renderShadows();
            GL11.glStencilFunc((int)514, (int)0, (int)255);
            GL11.glStencilOp((int)7680, (int)7680, (int)7680);
            GL11.glCullFace((int)1029);
            GL11.glDepthFunc((int)515);
            this.fourLight.enable();
            GL11.glColorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
            this.renderScene();
            ++i2;
        }
        this.lightShader.disable();
        GL11.glDisable((int)2896);
        GL11.glDisable((int)2960);
        GL11.glDepthMask((boolean)true);
        GL11.glDisable((int)3042);
        this.renderMonitor(true);
        GL11.glDisable((int)2884);
        Display.update();
        if (Display.isCloseRequested()) {
            this.stop();
        }
    }

    private void ibox(double x0, double y0, double z0, double x1, double y1, double z1) {
        Tesselator t = Tesselator.instance;
        t.begin(7);
        t.vertex(x0, y0, z1);
        t.vertex(x1, y0, z1);
        t.vertex(x1, y0, z0);
        t.vertex(x0, y0, z0);
        t.vertex(x0, y1, z0);
        t.vertex(x1, y1, z0);
        t.vertex(x1, y1, z1);
        t.vertex(x0, y1, z1);
        t.vertex(x1, y0, z0);
        t.vertex(x1, y0, z1);
        t.vertex(x1, y1, z1);
        t.vertex(x1, y1, z0);
        t.vertex(x0, y0, z1);
        t.vertex(x0, y0, z0);
        t.vertex(x0, y1, z0);
        t.vertex(x0, y1, z1);
        t.vertex(x0, y1, z1);
        t.vertex(x1, y1, z1);
        t.vertex(x1, y0, z1);
        t.vertex(x0, y0, z1);
        t.vertex(x0, y0, z0);
        t.vertex(x1, y0, z0);
        t.vertex(x1, y1, z0);
        t.vertex(x0, y1, z0);
        t.end();
    }

    private void box(double x0, double y0, double z0, double x1, double y1, double z1) {
        Tesselator t = Tesselator.instance;
        t.begin(7);
        this.quad(x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        this.quad(x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0);
        this.quad(x1, y1, z0, x1, y1, z1, x1, y0, z1, x1, y0, z0);
        this.quad(x0, y1, z1, x0, y1, z0, x0, y0, z0, x0, y0, z1);
        this.quad(x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
        this.quad(x0, y1, z0, x1, y1, z0, x1, y0, z0, x0, y0, z0);
        t.end();
    }

    private void shadowBox(double x0, double y0, double z0, double x1, double y1, double z1) {
        Tesselator t = Tesselator.instance;
        t.begin(7);
        this.shadowQuad(x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        this.shadowQuad(x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0);
        this.shadowQuad(x1, y1, z0, x1, y1, z1, x1, y0, z1, x1, y0, z0);
        this.shadowQuad(x0, y1, z1, x0, y1, z0, x0, y0, z0, x0, y0, z1);
        this.shadowQuad(x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
        this.shadowQuad(x0, y1, z0, x1, y1, z0, x1, y0, z0, x0, y0, z0);
        t.end();
    }

    private void quad(double x0, double y0, double z0, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3) {
        Tesselator t = Tesselator.instance;
        double Ux = x1 - x0;
        double Uy = y1 - y0;
        double Uz = z1 - z0;
        double Vx = x3 - x0;
        double Vy = y3 - y0;
        double Vz = z3 - z0;
        double Nx = Uy * Vz - Uz * Vy;
        double Ny = Uz * Vx - Ux * Vz;
        double Nz = Ux * Vy - Uy * Vx;
        double dist = Math.sqrt(Nx * Nx + Ny * Ny + Nz * Nz);
        t.normal((float)(Nx /= dist), (float)(Ny /= dist), (float)(Nz /= dist));
        t.vertex(x0, y0, z0);
        t.vertex(x1, y1, z1);
        t.vertex(x2, y2, z2);
        t.vertex(x3, y3, z3);
    }

    private void shadowQuad(double x0, double y0, double z0, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3) {
        Tesselator t = Tesselator.instance;
        double Ux = x1 - x0;
        double Uy = y1 - y0;
        double Uz = z1 - z0;
        double Vx = x3 - x0;
        double Vy = y3 - y0;
        double Vz = z3 - z0;
        double Nx = Uy * Vz - Uz * Vy;
        double Ny = Uz * Vx - Ux * Vz;
        double Nz = Ux * Vy - Uy * Vx;
        double dist = Math.sqrt(Nx * Nx + Ny * Ny + Nz * Nz);
        t.normal((float)(Nx /= dist), (float)(Ny /= dist), (float)(Nz /= dist));
        t.color(0);
        t.vertex(x0, y0, z0);
        t.vertex(x1, y1, z1);
        t.vertex(x2, y2, z2);
        t.vertex(x3, y3, z3);
        t.color(0xFF0000);
        t.vertex(x3, y3, z3);
        t.vertex(x2, y2, z2);
        t.vertex(x1, y1, z1);
        t.vertex(x0, y0, z0);
        t.color(0xFF0000);
        t.vertex(x0, y0, z0);
        t.vertex(x1, y1, z1);
        t.color(0);
        t.vertex(x1, y1, z1);
        t.vertex(x0, y0, z0);
        t.color(0xFF0000);
        t.vertex(x1, y1, z1);
        t.vertex(x2, y2, z2);
        t.color(0);
        t.vertex(x2, y2, z2);
        t.vertex(x1, y1, z1);
        t.color(0xFF0000);
        t.vertex(x2, y2, z2);
        t.vertex(x3, y3, z3);
        t.color(0);
        t.vertex(x3, y3, z3);
        t.vertex(x2, y2, z2);
        t.color(0xFF0000);
        t.vertex(x3, y3, z3);
        t.vertex(x0, y0, z0);
        t.color(0);
        t.vertex(x0, y0, z0);
        t.vertex(x3, y3, z3);
    }

    private void stop() {
        this.stop = true;
    }

    @Override
    public void run() {
        try {
            this.init();
        }
        catch (Exception e) {
            this.crash("Initialization", e);
            return;
        }
        try {
            long lastFrameTime = System.currentTimeMillis();
            int frames = 0;
            while (!this.stop) {
                this.gameLoop();
                ++frames;
                long now = System.currentTimeMillis();
                if (now - lastFrameTime <= 1000L) continue;
                lastFrameTime += 1000L;
                System.out.println(String.valueOf(frames) + " fps");
                frames = 0;
            }
        }
        catch (Exception e) {
            this.crash("Runtime", e);
            return;
        }
    }

    private <String, Exception> void crash(String where, Exception e) {
        System.out.println("Crash at " + where);
        e.printStackTrace();
    }

    public static <String, Runnable> void main(String[] args) {
        SpaceGame sg = new SpaceGame();
       Thread thread = new Thread((Runnable)sg, "Game").start();
    }
}

