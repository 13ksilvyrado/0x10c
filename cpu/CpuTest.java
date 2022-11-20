/*
 * Decompiled with CFR 0.151.
 */
package cpu;

import cpu.CPU;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;
import javax.swing.JFrame;

public class CpuTest {
    private static final int[] colors = new int[]{-16777216, -1, -7864320, -5570578, -3390260, -16724907, -16777046, -1118601, -2258859, -10075136, -34953, -13421773, -8947849, -5570714, -16742145, -4473925};

    public static void main(String[] args) throws Exception {
        Random random = new Random();
        final CPU cpu = new CPU();
        try (var br = new BufferedReader(new FileReader("src/dump.txt"))) {
			Thread t = new Thread(){

			    @Override
			    public void run() {
			        Canvas canvas = new Canvas();
			        canvas.setMinimumSize(new Dimension(256, 256));
			        canvas.setPreferredSize(new Dimension(256, 256));
			        canvas.setMaximumSize(new Dimension(256, 256));
			        JFrame frame = new JFrame();
			        frame.add(canvas);
			        frame.pack();
			        frame.setResizable(false);
			        frame.setLocationRelativeTo(null);
			        frame.setVisible(true);
			        frame.setDefaultCloseOperation(3);
			        BufferedImage img = new BufferedImage(32, 32, 2);
			        int[] pixels = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
			        while (true) {
			            try {
			                while (true) {
			                    int i = 0;
			                    while (i < 1024) {
			                        pixels[i] = colors[cpu.ram[512 + i] & 0xF];
			                        ++i;
			                    }
			                    Graphics g = canvas.getGraphics();
			                    g.drawImage(img, 0, 0, 256, 256, null);
			                    g.dispose();
			                    Thread.sleep(10L);
			                }
			            }
			            catch (Exception e) {
			                e.printStackTrace();
			                continue;
			            }
			        }
			    }
			};
			t.start();
			String line = "";
			while ((line = br.readLine()) != null) {
			    if ((line = line.trim()).length() == 0) continue;
			    String[] words = line.split(" ");
			    int memPos = Integer.parseInt(words[0].substring(0, 4), 16);
			    int i = 0;
			    while (i < words.length - 1) {
			        if (!words[i + 1].startsWith("[")) {
			            String b0 = words[i + 1].substring(0, 2);
			            String b1 = words[i + 1].substring(2);
			            if (!b0.equals("--")) {
			                cpu.ram[memPos++] = (byte)Integer.parseInt(b0, 16);
			            }
			            if (!b1.equals("--")) {
			                cpu.ram[memPos++] = (byte)Integer.parseInt(b1, 16);
			            }
			        }
			        ++i;
			    }
			}
		}
        long nsPerFrame = 16666666L;
        long nextTime = System.nanoTime();
        long[] usages = new long[256];
        block2: while (true) {
            long a = System.nanoTime();
            while (System.nanoTime() < nextTime) {
                Thread.yield();
            }
            long b = System.nanoTime();
            nextTime += nsPerFrame;
            cpu.cycles -= 16666;
            int codes = 0;
            while (cpu.cycles < 0) {
                cpu.ram[254] = (byte)random.nextInt(256);
                int code = cpu.ram[cpu.PC] & 0xFF;
                ++codes;
                cpu.tick();
                int n = code;
                usages[n] = usages[n] + 1L;
            }
            long c = System.nanoTime();
            System.out.println(String.valueOf((double)((c - b) * 1000000L / (c - a)) / 10000.0) + "% (" + codes + " ops)");
            long total = 0L;
            int i = 0;
            while (i < usages.length) {
                total += usages[i];
                ++i;
            }
            i = 0;
            while (true) {
                if (i >= usages.length) continue block2;
                if (usages[i] > 0L) {
                    System.out.println(String.valueOf(Integer.toHexString(i)) + ": " + (double)(usages[i] * 1000000L / total) / 10000.0 + "%");
                }
                ++i;
            }
        }
    }
}

