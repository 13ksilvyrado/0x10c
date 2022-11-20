package computer;

import computer.Assembler;
import computer.VirtualKeyboard;
import computer.VirtualMonitor;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javax.swing.JFrame;

public class DCPU {
    public char[] ram = new char[65536];
    public char pc;
    public char sp;
    public char o;
    public char[] registers = new char[8];
    public int cycles;
    private static volatile boolean stop = false;
	private static DCPU[] cpus;
	private static int i;
	private static int j;
	private static long ops;
	private static int hz;
	private static long nsPerFrame;
	private static int cyclesPerFrame;
	private static double tick;
	private static long nextTime;
	private static long startTime;
	private static double total;
	private static long a;
	private static long b;
	private static long c;
	private static long passedTime;
    private static final int khz = 100;

    public int getAddr(int type) {
        if (type >= 32) {
            return 0x20000 | type & 0x1F;
        }
        switch (type & 0xF8) {
            case 0: {
                return 65536 + (type & 7);
            }
            case 8: {
                return this.registers[type & 7];
            }
            case 16: {
                char c = this.pc;
                this.pc = (char)(c + '\u0001');
                return this.ram[c] + this.registers[type & 7] & 0xFFFF;
            }
            case 24: {
                switch (type & 7) {
                    case 0: {
                        char c = this.sp;
                        this.sp = (char)(c + '\u0001');
                        return c & 0xFFFF;
                    }
                    case 1: {
                        return this.sp & 0xFFFF;
                    }
                    case 2: {
                        this.sp = (char)(this.sp - '\u0001');
                        return this.sp & 0xFFFF;
                    }
                    case 3: {
                        return 65544;
                    }
                    case 4: {
                        return 65545;
                    }
                    case 5: {
                        return 65552;
                    }
                    case 6: {
                        ++this.cycles;
                        char c = this.pc;
                        this.pc = (char)(c + '\u0001');
                        return this.ram[c];
                    }
                }
                ++this.cycles;
                char c = this.pc;
                this.pc = (char)(c + '\u0001');
                return 0x20000 | this.ram[c];
            }
        }
        throw new IllegalStateException("Illegal value type " + type + "! How did you manage that!?");
    }

    public char get(int addr) {
        if (addr < 65536) {
            return this.ram[addr & 0xFFFF];
        }
        if (addr < 65544) {
            return this.registers[addr & 7];
        }
        if (addr >= 131072) {
            return (char)addr;
        }
        if (addr == 65544) {
            return this.sp;
        }
        if (addr == 65545) {
            return this.pc;
        }
        if (addr == 65552) {
            return this.o;
        }
        throw new IllegalStateException("Illegal address " + Integer.toHexString(addr) + "! How did you manage that!?");
    }

    public void set(int addr, char val) {
        if (addr < 65536) {
            this.ram[addr & 65535] = val;
        } else if (addr < 65544) {
            this.registers[addr & 7] = val;
        } else if (addr < 131072) {
            if (addr == 65544) {
                this.sp = val;
            } else if (addr == 65545) {
                this.pc = val;
            } else if (addr == 65552) {
                this.o = val;
            } else {
                throw new IllegalStateException("Illegal address " + Integer.toHexString(addr) + "! How did you manage that!?");
            }
        }
    }

    public static int getInstructionLength(char opcode) {
        int len = 1;
        int cmd = opcode & 0xF;
        if (cmd == 0) {
            int atype;
            cmd = opcode >> 4 & 0xF;
            if (cmd > 0 && (((atype = opcode >> 10 & 0x3F) & 0xF8) == 16 || atype == 31 || atype == 30)) {
                ++len;
            }
        } else {
            int atype = opcode >> 4 & 0x3F;
            int btype = opcode >> 10 & 0x3F;
            if ((atype & 0xF8) == 16 || atype == 31 || atype == 30) {
                ++len;
            }
            if ((btype & 0xF8) == 16 || btype == 31 || btype == 30) {
                ++len;
            }
        }
        return len;
    }

    public void skip() {
        ++this.cycles;
        char c = this.pc;
        this.pc = (char)(c + '\u0001');
        this.pc = (char)(this.pc + DCPU.getInstructionLength(this.ram[c]));
    }

    public void tick() {
        ++this.cycles;
        char c = this.pc;
        this.pc = (char)(c + '\u0001');
        char opcode = this.ram[c];
        int cmd = opcode & 0xF;
        if (cmd == 0) {
            cmd = opcode >> 4 & 0xF;
            if (cmd == 0) {
                cmd = opcode >> 8 & 0xFF;
                switch (cmd) {
                    case 1: {
                        char c2 = this.sp;
                        this.sp = (char)(c2 + '\u0001');
                        this.pc = this.ram[c2 & 0xFFFF];
                    }
                }
            } else {
                int atype = opcode >> 10 & 0x3F;
                int aaddr = this.getAddr(atype);
                char a = this.get(aaddr);
                switch (cmd) {
                    case 1: {
                        this.pc = a;
                        break;
                    }
                    case 2: {
                        this.sp = (char)(this.sp - '\u0001');
                        this.ram[this.sp & 65535] = (char)(this.pc - 2 + DCPU.getInstructionLength(opcode));
                        this.pc = a;
                    }
                }
            }
        } else {
            int atype = opcode >> 4 & 0x3F;
            int btype = opcode >> 10 & 0x3F;
            int aaddr = this.getAddr(atype);
            char a = this.get(aaddr);
            int baddr = this.getAddr(btype);
            char b = this.get(baddr);
            switch (cmd) {
                case 1: {
                    a = b;
                    break;
                }
                case 2: {
                    ++this.cycles;
                    int val = a + b;
                    a = (char)val;
                    this.o = (char)(val >> 16);
                    break;
                }
                case 3: {
                    ++this.cycles;
                    int val = a - b;
                    a = (char)val;
                    this.o = (char)(val >> 16);
                    break;
                }
                case 4: {
                    ++this.cycles;
                    int val = a * b;
                    a = (char)val;
                    this.o = (char)(val >> 16);
                    break;
                }
                case 5: {
                    this.cycles += 2;
                    if (b == '\u0000') {
                        a = '\u0000';
                        break;
                    }
                    long val = ((long)a << 16) / (long)b;
                    a = (char)(val >> 16);
                    this.o = (char)val;
                    break;
                }
                case 6: {
                    this.cycles += 2;
                    if (b == '\u0000') {
                        a = '\u0000';
                        break;
                    }
                    a = (char)(a % b);
                    break;
                }
                case 7: {
                    ++this.cycles;
                    long val = (long)a << b;
                    a = (char)val;
                    this.o = (char)(val >> 16);
                    break;
                }
                case 8: {
                    ++this.cycles;
                    long val = (long)a << 16 - b;
                    a = (char)(val >> 16);
                    this.o = (char)val;
                    break;
                }
                case 9: {
                    a = (char)(a & b);
                    break;
                }
                case 10: {
                    a = (char)(a | b);
                    break;
                }
                case 11: {
                    a = (char)(a ^ b);
                    break;
                }
                case 12: {
                    ++this.cycles;
                    if (a != b) {
                        this.skip();
                    }
                    return;
                }
                case 13: {
                    ++this.cycles;
                    if (a == b) {
                        this.skip();
                    }
                    return;
                }
                case 14: {
                    ++this.cycles;
                    if (a <= b) {
                        this.skip();
                    }
                    return;
                }
                case 15: {
                    ++this.cycles;
                    if ((a & b) == 0) {
                        this.skip();
                    }
                    return;
                }
            }
            this.set(aaddr, a);
        }
    }

    /*
     * Unable to fully structure code
     */
    private static <GOTO> void testCpus(int cpuCount, char[] ram) {
        cpus = new DCPU[cpuCount];
        i = 0;
        while (i < cpuCount) {
            cpus[i] = new DCPU();
            j = 0;
            while (j < 65536) {
                cpus[i].ram[j] = ram[j];
                ++j;
            }
            ++i;
        }
        ops = 0L;
        hz = 100000;
        cyclesPerFrame = hz / 60;
        nsPerFrame = 16666666L;
        nextTime = System.nanoTime();
        tick = 0.0;
        total = 0.0;
        startTime = System.currentTimeMillis();
        while (!DCPU.stop) {
            a = System.nanoTime();
            while (System.nanoTime() < nextTime) {
                try {
                    Thread.sleep(1L);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            b = System.nanoTime();
            j = 0;
             {
                cpus[j].tick();
                do {
                    if (cpus[j].cycles < cyclesPerFrame);
                    cpus[j].cycles -= cyclesPerFrame;
                    ++j;

                    // 2 sources

                } while (j < cpuCount);
            }
            c = System.nanoTime();
            ops += (long)cyclesPerFrame;
            nextTime += nsPerFrame;
            tick += (double)(c - b) / 1.0E9;
            total += (double)(c - a) / 1.0E9;
        }
        passedTime = System.currentTimeMillis() - startTime;
        System.out.println(String.valueOf(cpuCount) + " DCPU at " + (double)ops / (double)passedTime + " khz, " + tick * 100.0 / total + "% cpu use");
    }

    public static void main(String[] args) throws Exception {
        final DCPU cpu = new DCPU();
        new Assembler(cpu.ram).assemble("os.asm");
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : 1;
        final int cpusPerCore = args.length > 1 ? Integer.parseInt(args[1]) : 100;
        int seconds = args.length > 2 ? Integer.parseInt(args[2]) : 5;
        System.out.println("Aiming at 100 khz, with " + cpusPerCore + " DCPUs per thread, on " + threads + " threads.");
        System.out.println("idk lol");
        System.out.println("Running test for " + seconds + " seconds..");
        int i = 0;
        while (i < threads) {
            new Thread(){

                @Override
                public void run() {
                    DCPU.testCpus(cpusPerCore, cpu.ram);
                }
            }.start();
            ++i;
        }
        i = seconds;
        while (i > 0) {
            System.out.println(String.valueOf(i) + "..");
            Thread.sleep(1000L);
            --i;
        }
        stop = true;
    }
}

