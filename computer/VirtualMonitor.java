package computer;

import java.io.IOException;
import javax.imageio.ImageIO;

public class VirtualMonitor {
    public static final int WIDTH_CHARS = 32;
    public static final int HEIGHT_CHARS = 12;
    public static final int WIDTH_PIXELS = 128;
    public static final int HEIGHT_PIXELS = 96;
    private final char[] ram;
    private final int offset;
    private final int charOffset;
    private final int miscDataOffset;
    private final int[] colorBase = new int[256];
    private final int[] colorOffs = new int[256];
    public int[] pixels = new int[16384];

    public VirtualMonitor(char[] ram, int offset) {
        this.ram = ram;
        this.offset = offset;
        this.charOffset = offset + 384;
        this.miscDataOffset = this.charOffset + 256;
        int i = 0;
        while (i < 256) {
            int bg = VirtualMonitor.genColor(i % 16);
            int fg = VirtualMonitor.genColor(i / 16);
            this.colorBase[i] = bg;
            this.colorOffs[i] = fg - bg;
            ++i;
        }
        int[] pixels = new int[4096];
        try {
            ImageIO.read(VirtualMonitor.class.getResource("/font.png")).getRGB(0, 0, 128, 32, pixels, 0, 128);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        int c = 0;
        while (c < 128) {
            int ro = this.charOffset + c * 2;
            int xo = c % 32 * 4;
            int yo = c / 32 * 8;
            ram[ro + 0] = '\u0000';
            ram[ro + 1] = '\u0000';
            int xx = 0;
            while (xx < 4) {
                int bb = 0;
                int yy = 0;
                while (yy < 8) {
                    if ((pixels[xo + xx + (yo + yy) * 128] & 0xFF) > 128) {
                        bb |= 1 << yy;
                    }
                    ++yy;
                }
                int n = ro + xx / 2;
                ram[n] = (char)(ram[n] | bb << (xx + 1 & 1) * 8);
                ++xx;
            }
            ++c;
        }
    }

    private static int genColor(int i) {
        int b = (i >> 0 & 1) * 170;
        int g = (i >> 1 & 1) * 170;
        int r = (i >> 2 & 1) * 170;
        if (i == 6) {
            g += 85;
        } else if (i >= 8) {
            r += 85;
            g += 85;
            b += 85;
        }
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    public void render() {
        long time = System.currentTimeMillis() / 16L;
        boolean blink = time / 20L % 2L == 0L;
        int y = 0;
        while (y < 12) {
            int x = 0;
            while (x < 32) {
                char dat = this.ram[this.offset + x + y * 32];
                int ch = dat & 0x7F;
                int colorIndex = dat >> 8 & 0xFF;
                int co = this.charOffset + ch * 2;
                int color = this.colorBase[colorIndex];
                int colorAdd = this.colorOffs[colorIndex];
                if (blink && (dat & 0x80) > 0) {
                    colorAdd = 0;
                }
                int pixelOffs = x * 4 + y * 8 * 128;
                int xx = 0;
                while (xx < 4) {
                    int bits = this.ram[co + (xx >> 1)] >> (xx + 1 & 1) * 8 & 0xFF;
                    int yy = 0;
                    while (yy < 8) {
                        this.pixels[pixelOffs + xx + yy * 128] = color + colorAdd * (bits >> yy & 1);
                        ++yy;
                    }
                    ++xx;
                }
                ++x;
            }
            ++y;
        }
        int color = this.colorBase[this.ram[this.miscDataOffset] & 0xF];
        int y2 = 96;
        while (y2 < 128) {
            int x = 0;
            while (x < 128) {
                this.pixels[x + y2 * 128] = color;
                ++x;
            }
            ++y2;
        }
    }

    public int getBackgroundColor() {
        return this.colorBase[this.ram[this.miscDataOffset] & 0xF];
    }

    public void setPixels(int[] pixels) {
        this.pixels = pixels;
    }
}

