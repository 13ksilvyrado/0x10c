package com.mojang.spacegame.renderer;

import com.mojang.spacegame.renderer.Tesselator;
import com.mojang.spacegame.renderer.VertexData;
import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.PathIterator;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.GLUtessellator;
import org.lwjgl.util.glu.GLUtessellatorCallback;

public class GlyphGenerator
implements GLUtessellatorCallback {
    Tesselator t = Tesselator.instance;

    public void draw(String line) {
        int i = 0;
        while (i < line.length()) {
            this.drawChar(line.charAt(i));
            ++i;
        }
    }

    public void drawChar(char ch) {
        Font font = new Font("monospaced", 0, 16);
        GlyphVector gv = font.createGlyphVector(new FontRenderContext(null, false, true), "" + ch);
        Shape shape = gv.getOutline();
        PathIterator pi = shape.getPathIterator(null, 0.1);
        double[] coords = new double[6];
        GLUtessellator gt = GLU.gluNewTess();
        gt.gluTessCallback(100100, (GLUtessellatorCallback)this);
        gt.gluTessCallback(100101, (GLUtessellatorCallback)this);
        gt.gluTessCallback(100102, (GLUtessellatorCallback)this);
        gt.gluTessCallback(100105, (GLUtessellatorCallback)this);
        gt.gluTessBeginPolygon(null);
        gt.gluTessProperty(100140, (double)(pi.getWindingRule() == 1 ? 100131 : 100134));
        while (!pi.isDone()) {
            int type = pi.currentSegment(coords);
            if (type == 0) {
                gt.gluTessBeginContour();
                gt.gluTessVertex(coords, 0, (Object)2);
            } else if (type == 1) {
                gt.gluTessVertex(coords, 0, (Object)new VertexData(coords[0], coords[1]));
            } else if (type == 4) {
                gt.gluTessEndContour();
            }
            pi.next();
        }
        gt.gluTessEndPolygon();
        GL11.glTranslatef((float)gv.getGlyphMetrics(0).getAdvance(), (float)0.0f, (float)0.0f);
    }

    public void begin(int mode) {
        this.t.begin(mode);
    }

    public void beginData(int arg0, Object arg1) {
    }

    public void combine(double[] coords, Object[] data, float[] weight, Object[] outData) {
        int i = 0;
        while (i < outData.length) {
            double[] combined = new double[]{coords[0], coords[1], coords[2], 1.0, 1.0, 1.0};
            outData[i] = new VertexData(coords[0], coords[1]);
            ++i;
        }
    }

    public void combineData(double[] arg0, Object[] arg1, float[] arg2, Object[] arg3, Object arg4) {
    }

    public void edgeFlag(boolean arg0) {
    }

    public void edgeFlagData(boolean arg0, Object arg1) {
    }

    public void end() {
        this.t.end();
    }

    public void endData(Object arg0) {
    }

    public void error(int arg0) {
    }

    public void errorData(int arg0, Object arg1) {
    }

    public void vertex(Object arg0) {
        VertexData vd = (VertexData)arg0;
        this.t.vertex(vd.x, vd.y, 0.0);
    }

    public void vertexData(Object arg0, Object arg1) {
    }
}

