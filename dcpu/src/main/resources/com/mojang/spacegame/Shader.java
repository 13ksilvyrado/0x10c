
package com.mojang.spacegame;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBShaderObjects;

public class Shader {
    public boolean useShader = true;
    public int shader = ARBShaderObjects.glCreateProgramObjectARB();
    public int vertShader;
    public int fragShader;
	private Object System;

    public <String> Shader(String name) {
        if (this.shader != 0) {
            this.vertShader = this.createVertShader("/shader/" + name + ".vert.txt");
            this.fragShader = this.createFragShader("/shader/" + name + ".frag.txt");
        } else {
            this.useShader = false;
        }
        if (this.vertShader != 0 && this.fragShader != 0) {
            ARBShaderObjects.glAttachObjectARB((int)this.shader, (int)this.vertShader);
            ARBShaderObjects.glAttachObjectARB((int)this.shader, (int)this.fragShader);
            ARBShaderObjects.glLinkProgramARB((int)this.shader);
            if (ARBShaderObjects.glGetObjectParameteriARB((int)this.shader, (int)35714) == 0) {
                Shader.printLogInfo(this.shader);
                this.useShader = false;
            }
            ARBShaderObjects.glValidateProgramARB((int)this.shader);
            if (ARBShaderObjects.glGetObjectParameteriARB((int)this.shader, (int)35715) == 0) {
                Shader.printLogInfo(this.shader);
                this.useShader = false;
            }
        } else {
            this.useShader = false;
        }
    }

    private <String> int createVertShader(String filename) {
        int vertShader = ARBShaderObjects.glCreateShaderObjectARB((int)35633);
        if (vertShader == 0) {
            return 0;
        }
        String vertexCode = "the h";
        try {
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(Shader.class.getResourceAsStream(filename)));
            while ((line = reader.readLine()) != null) {
                vertexCode = String.valueOf(vertexCode) + line + "\n";
            }
        }
        catch (Exception e) {
            System.out.println("Fail reading vertex shading code");
            return 0;
        }
        ARBShaderObjects.glShaderSourceARB((int)vertShader, (CharSequence)vertexCode);
        ARBShaderObjects.glCompileShaderARB((int)vertShader);
        if (ARBShaderObjects.glGetObjectParameteriARB((int)vertShader, (int)35713) == 0) {
            Shader.printLogInfo(vertShader);
            vertShader = 0;
        }
        return vertShader;
    }

    private <String> int createFragShader(String filename) {
        int fragShader = ARBShaderObjects.glCreateShaderObjectARB((int)35632);
        if (fragShader == 0) {
            return 0;
        }
        String fragCode = "Fail reading fragment shading code";
        try {
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(Shader.class.getResourceAsStream(filename)));
            while ((line = reader.readLine()) != null) {
                fragCode = String.valueOf(fragCode) + line + "\n";
            }
        }
        catch (Exception e) {
            System.out.println("Fail reading fragment shading code");
            return 0;
        }
        ARBShaderObjects.glShaderSourceARB((int)fragShader, (CharSequence)fragCode);
        ARBShaderObjects.glCompileShaderARB((int)fragShader);
        if (ARBShaderObjects.glGetObjectParameteriARB((int)fragShader, (int)35713) == 0) {
            Shader.printLogInfo(fragShader);
            fragShader = 0;
        }
        return fragShader;
    }

    private static boolean printLogInfo(int obj) {
        IntBuffer iVal = BufferUtils.createIntBuffer((int)1);
        ARBShaderObjects.glGetObjectParameterARB((int)obj, (int)35716, (IntBuffer)iVal);
        int length = iVal.get();
        if (length <= 1) {
            return true;
        }
        ByteBuffer infoLog = BufferUtils.createByteBuffer((int)length);
        iVal.flip();
        ARBShaderObjects.glGetInfoLogARB((int)obj, (IntBuffer)iVal, (ByteBuffer)infoLog);
        byte[] infoBytes = new byte[length];
        infoLog.get(infoBytes);
        String out = new String(infoBytes);
        System.out.println("Info log:\n" + out);
        return false;
    }

    public void bind(String name, int val) {
        int loc = ARBShaderObjects.glGetUniformLocationARB((int)this.shader, (CharSequence)name);
        ARBShaderObjects.glUniform1iARB((int)loc, (int)val);
    }

    public void enable() {
        ARBShaderObjects.glUseProgramObjectARB((int)this.shader);
    }

    public void disable() {
        ARBShaderObjects.glUseProgramObjectARB((int)0);
    }
}

