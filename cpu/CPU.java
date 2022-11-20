package cpu;

public class CPU {
    public boolean C;
    public boolean Z;
    public boolean I;
    public boolean D;
    public boolean B;
    public boolean V;
    public boolean N;
    public char PC = (char)1536;
    public byte SP;
    public byte A;
    public byte X;
    public byte Y;
    public int cycles = 0;
    public byte[] ram = new byte[65536];

    public void tick() {
        char c = this.PC;
        this.PC = (char)(c + '\u0001');
        byte opCode = this.ram[c];
        switch (opCode & 0xFF) {
            case 0: {
                this.cycles += 7;
                break;
            }
            case 1: {
                this.cycles += 6;
                char c2 = this.PC;
                this.PC = (char)(c2 + '\u0001');
                int pos = this.ram[c2] + this.X & 0xFF;
                this.A = (byte)(this.A | this.ram[this.ram[pos] & 0xFF | (this.ram[pos + 1] & 0xFF) << 8]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 5: {
                this.cycles += 3;
                char c3 = this.PC;
                this.PC = (char)(c3 + '\u0001');
                this.A = (byte)(this.A | this.ram[this.ram[c3] & 0xFF]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 6: {
                this.cycles += 5;
                char c4 = this.PC;
                this.PC = (char)(c4 + '\u0001');
                byte oval = this.ram[this.ram[c4] & 0xFF];
                this.C = oval < 0;
                int v = (oval & 0xFF) << 1;
                char c5 = this.PC;
                this.PC = (char)(c5 + '\u0001');
                this.ram[this.ram[c5] & 255] = (byte)v;
                this.Z = this.A == 0;
                this.N = (byte)v < 0;
                break;
            }
            case 8: {
                this.cycles += 3;
                int flags = 0;
                if (this.C) {
                    ++flags;
                }
                if (this.Z) {
                    flags += 2;
                }
                if (this.I) {
                    flags += 4;
                }
                if (this.D) {
                    flags += 8;
                }
                if (this.B) {
                    flags += 16;
                }
                if (this.V) {
                    flags += 64;
                }
                if (this.N) {
                    flags += 128;
                }
                this.SP = (byte)(this.SP - 1);
                this.ram[512 + this.SP] = (byte)flags;
                break;
            }
            case 9: {
                this.cycles += 2;
                char c6 = this.PC;
                this.PC = (char)(c6 + '\u0001');
                this.A = (byte)(this.A | this.ram[c6]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 10: {
                this.cycles += 2;
                byte oval = this.A;
                this.C = oval < 0;
                int v = (oval & 0xFF) << 1;
                this.A = (byte)v;
                this.Z = this.A == 0;
                this.N = (byte)v < 0;
                break;
            }
            case 13: {
                this.cycles += 4;
                char c7 = this.PC;
                char c8 = this.PC = (char)(c7 + '\u0001');
                this.PC = (char)(c8 + '\u0001');
                this.A = (byte)(this.A | this.ram[this.ram[c7] & 0xFF | (this.ram[c8] & 0xFF) << 8]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 14: {
                this.cycles += 6;
                char c9 = this.PC;
                char c10 = this.PC = (char)(c9 + '\u0001');
                this.PC = (char)(c10 + '\u0001');
                byte oval = this.ram[this.ram[c9] & 0xFF | (this.ram[c10] & 0xFF) << 8];
                this.C = oval < 0;
                int v = (oval & 0xFF) << 1;
                char c11 = this.PC;
                char c12 = this.PC = (char)(c11 + '\u0001');
                this.PC = (char)(c12 + '\u0001');
                this.ram[this.ram[c11] & 255 | (this.ram[c12] & 255) << 8] = (byte)v;
                this.Z = this.A == 0;
                this.N = (byte)v < 0;
                break;
            }
            case 16: {
                this.cycles += 2;
                char c13 = this.PC;
                this.PC = (char)(c13 + '\u0001');
                byte t = this.ram[c13];
                if (this.N) break;
                this.PC = (char)(this.PC + t);
                ++this.cycles;
                break;
            }
            case 17: {
                this.cycles += 5;
                char c14 = this.PC;
                this.PC = (char)(c14 + '\u0001');
                int pos = this.ram[c14] & 0xFF;
                this.A = (byte)(this.A | this.ram[(this.ram[pos] & 0xFF | (this.ram[pos + 1] & 0xFF) << 8) + (this.Y & 0xFF) & 0xFFFF]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 21: {
                this.cycles += 4;
                char c15 = this.PC;
                this.PC = (char)(c15 + '\u0001');
                this.A = (byte)(this.A | this.ram[this.ram[c15] + this.X & 0xFF]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 22: {
                this.cycles += 6;
                char c16 = this.PC;
                this.PC = (char)(c16 + '\u0001');
                byte oval = this.ram[this.ram[c16] + this.X & 0xFF];
                this.C = oval < 0;
                int v = (oval & 0xFF) << 1;
                char c17 = this.PC;
                this.PC = (char)(c17 + '\u0001');
                this.ram[this.ram[c17] + this.X & 255] = (byte)v;
                this.Z = this.A == 0;
                this.N = (byte)v < 0;
                break;
            }
            case 24: {
                this.cycles += 2;
                this.C = false;
                break;
            }
            case 25: {
                this.cycles += 4;
                char c18 = this.PC;
                char c19 = this.PC = (char)(c18 + '\u0001');
                this.PC = (char)(c19 + '\u0001');
                this.A = (byte)(this.A | this.ram[(this.ram[c18] & 0xFF | (this.ram[c19] & 0xFF) << 8) + (this.Y & 0xFF) & 0xFFFF]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 29: {
                this.cycles += 4;
                char c20 = this.PC;
                char c21 = this.PC = (char)(c20 + '\u0001');
                this.PC = (char)(c21 + '\u0001');
                this.A = (byte)(this.A | this.ram[(this.ram[c20] & 0xFF | (this.ram[c21] & 0xFF) << 8) + (this.X & 0xFF) & 0xFFFF]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 30: {
                this.cycles += 7;
                char c22 = this.PC;
                char c23 = this.PC = (char)(c22 + '\u0001');
                this.PC = (char)(c23 + '\u0001');
                byte oval = this.ram[(this.ram[c22] & 0xFF | (this.ram[c23] & 0xFF) << 8) + (this.X & 0xFF) & 0xFFFF];
                this.C = oval < 0;
                int v = (oval & 0xFF) << 1;
                char c24 = this.PC;
                char c25 = this.PC = (char)(c24 + '\u0001');
                this.PC = (char)(c25 + '\u0001');
                this.ram[(this.ram[c24] & 255 | (this.ram[c25] & 255) << 8) + (this.X & 255) & 65535] = (byte)v;
                this.Z = this.A == 0;
                this.N = (byte)v < 0;
                break;
            }
            case 32: {
                this.cycles += 6;
                this.SP = (byte)(this.SP - 1);
                this.ram[512 + this.SP] = (byte)(this.PC + '\u0001' >> 8 & 0xFF);
                this.SP = (byte)(this.SP - 1);
                this.ram[512 + this.SP] = (byte)(this.PC + '\u0001' & 0xFF);
                char c26 = this.PC;
                char c27 = this.PC = (char)(c26 + '\u0001');
                this.PC = (char)(c27 + '\u0001');
                this.PC = (char)(this.ram[c26] & 0xFF | (this.ram[c27] & 0xFF) << 8);
                break;
            }
            case 33: {
                this.cycles += 6;
                char c28 = this.PC;
                this.PC = (char)(c28 + '\u0001');
                int pos = this.ram[c28] + this.X & 0xFF;
                this.A = (byte)(this.A & this.ram[this.ram[pos] & 0xFF | (this.ram[pos + 1] & 0xFF) << 8]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 36: {
                this.cycles += 3;
                char c29 = this.PC;
                this.PC = (char)(c29 + '\u0001');
                int tmp = this.ram[this.ram[c29] & 0xFF] & 0xFF;
                this.Z = (this.A & tmp) == 0;
                this.V = (tmp & 0x40) > 0;
                this.N = (tmp & 0x80) > 0;
                break;
            }
            case 37: {
                this.cycles += 3;
                char c30 = this.PC;
                this.PC = (char)(c30 + '\u0001');
                this.A = (byte)(this.A & this.ram[this.ram[c30] & 0xFF]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 38: {
                this.cycles += 5;
                char c31 = this.PC;
                this.PC = (char)(c31 + '\u0001');
                byte oval = this.ram[this.ram[c31] & 0xFF];
                boolean oc = this.C;
                this.C = oval < 0;
                int v = ((oval & 0xFF) << 1) + (oc ? 1 : 0);
                char c32 = this.PC;
                this.PC = (char)(c32 + '\u0001');
                this.ram[this.ram[c32] & 255] = (byte)v;
                this.Z = this.A == 0;
                this.N = (byte)v < 0;
                break;
            }
            case 40: {
                this.cycles += 4;
                byte by = this.SP;
                this.SP = (byte)(by + 1);
                int flags = this.ram[512 + by] & 0xFF;
                this.C = (flags & 1) > 0;
                this.Z = (flags & 2) > 0;
                this.I = (flags & 4) > 0;
                this.D = (flags & 8) > 0;
                this.B = (flags & 0x10) > 0;
                this.V = (flags & 0x40) > 0;
                this.N = (flags & 0x80) > 0;
                break;
            }
            case 41: {
                this.cycles += 2;
                char c33 = this.PC;
                this.PC = (char)(c33 + '\u0001');
                this.A = (byte)(this.A & this.ram[c33]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 42: {
                this.cycles += 2;
                byte oval = this.A;
                boolean oc = this.C;
                this.C = oval < 0;
                int v = ((oval & 0xFF) << 1) + (oc ? 1 : 0);
                this.A = (byte)v;
                this.Z = this.A == 0;
                this.N = (byte)v < 0;
                break;
            }
            case 44: {
                this.cycles += 4;
                char c34 = this.PC;
                char c35 = this.PC = (char)(c34 + '\u0001');
                this.PC = (char)(c35 + '\u0001');
                int tmp = this.ram[this.ram[c34] & 0xFF | (this.ram[c35] & 0xFF) << 8] & 0xFF;
                this.Z = (this.A & tmp) == 0;
                this.V = (tmp & 0x40) > 0;
                this.N = (tmp & 0x80) > 0;
                break;
            }
            case 45: {
                this.cycles += 4;
                char c36 = this.PC;
                char c37 = this.PC = (char)(c36 + '\u0001');
                this.PC = (char)(c37 + '\u0001');
                this.A = (byte)(this.A & this.ram[this.ram[c36] & 0xFF | (this.ram[c37] & 0xFF) << 8]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 46: {
                this.cycles += 6;
                char c38 = this.PC;
                char c39 = this.PC = (char)(c38 + '\u0001');
                this.PC = (char)(c39 + '\u0001');
                byte oval = this.ram[this.ram[c38] & 0xFF | (this.ram[c39] & 0xFF) << 8];
                boolean oc = this.C;
                this.C = oval < 0;
                int v = ((oval & 0xFF) << 1) + (oc ? 1 : 0);
                char c40 = this.PC;
                char c41 = this.PC = (char)(c40 + '\u0001');
                this.PC = (char)(c41 + '\u0001');
                this.ram[this.ram[c40] & 255 | (this.ram[c41] & 255) << 8] = (byte)v;
                this.Z = this.A == 0;
                this.N = (byte)v < 0;
                break;
            }
            case 48: {
                this.cycles += 2;
                char c42 = this.PC;
                this.PC = (char)(c42 + '\u0001');
                byte t = this.ram[c42];
                if (!this.N) break;
                this.PC = (char)(this.PC + t);
                ++this.cycles;
                break;
            }
            case 49: {
                this.cycles += 5;
                char c43 = this.PC;
                this.PC = (char)(c43 + '\u0001');
                int pos = this.ram[c43] & 0xFF;
                this.A = (byte)(this.A & this.ram[(this.ram[pos] & 0xFF | (this.ram[pos + 1] & 0xFF) << 8) + (this.Y & 0xFF) & 0xFFFF]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 53: {
                this.cycles += 4;
                char c44 = this.PC;
                this.PC = (char)(c44 + '\u0001');
                this.A = (byte)(this.A & this.ram[this.ram[c44] + this.X & 0xFF]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 54: {
                this.cycles += 6;
                char c45 = this.PC;
                this.PC = (char)(c45 + '\u0001');
                byte oval = this.ram[this.ram[c45] + this.X & 0xFF];
                boolean oc = this.C;
                this.C = oval < 0;
                int v = ((oval & 0xFF) << 1) + (oc ? 1 : 0);
                char c46 = this.PC;
                this.PC = (char)(c46 + '\u0001');
                this.ram[this.ram[c46] + this.X & 255] = (byte)v;
                this.Z = this.A == 0;
                this.N = (byte)v < 0;
                break;
            }
            case 56: {
                this.cycles += 2;
                this.C = true;
                break;
            }
            case 57: {
                this.cycles += 4;
                char c47 = this.PC;
                char c48 = this.PC = (char)(c47 + '\u0001');
                this.PC = (char)(c48 + '\u0001');
                this.A = (byte)(this.A & this.ram[(this.ram[c47] & 0xFF | (this.ram[c48] & 0xFF) << 8) + (this.Y & 0xFF) & 0xFFFF]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 61: {
                this.cycles += 4;
                char c49 = this.PC;
                char c50 = this.PC = (char)(c49 + '\u0001');
                this.PC = (char)(c50 + '\u0001');
                this.A = (byte)(this.A & this.ram[(this.ram[c49] & 0xFF | (this.ram[c50] & 0xFF) << 8) + (this.X & 0xFF) & 0xFFFF]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 62: {
                this.cycles += 7;
                char c51 = this.PC;
                char c52 = this.PC = (char)(c51 + '\u0001');
                this.PC = (char)(c52 + '\u0001');
                byte oval = this.ram[(this.ram[c51] & 0xFF | (this.ram[c52] & 0xFF) << 8) + (this.X & 0xFF) & 0xFFFF];
                boolean oc = this.C;
                this.C = oval < 0;
                int v = ((oval & 0xFF) << 1) + (oc ? 1 : 0);
                char c53 = this.PC;
                char c54 = this.PC = (char)(c53 + '\u0001');
                this.PC = (char)(c54 + '\u0001');
                this.ram[(this.ram[c53] & 255 | (this.ram[c54] & 255) << 8) + (this.X & 255) & 65535] = (byte)v;
                this.Z = this.A == 0;
                this.N = (byte)v < 0;
                break;
            }
            case 64: {
                this.cycles += 6;
                byte by = this.SP;
                this.SP = (byte)(by + 1);
                int flags = this.ram[512 + by] & 0xFF;
                this.C = (flags & 1) > 0;
                this.Z = (flags & 2) > 0;
                this.I = (flags & 4) > 0;
                this.D = (flags & 8) > 0;
                this.B = (flags & 0x10) > 0;
                this.V = (flags & 0x40) > 0;
                this.N = (flags & 0x80) > 0;
                byte by2 = this.SP;
                byte by3 = this.SP = (byte)(by2 + 1);
                this.SP = (byte)(by3 + 1);
                this.PC = (char)(this.ram[512 + by2] & 0xFF | (this.ram[512 + by3] & 0xFF) << 8);
                break;
            }
            case 65: {
                this.cycles += 6;
                char c55 = this.PC;
                this.PC = (char)(c55 + '\u0001');
                int pos = this.ram[c55] + this.X & 0xFF;
                this.A = (byte)(this.A ^ this.ram[this.ram[pos] & 0xFF | (this.ram[pos + 1] & 0xFF) << 8]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 69: {
                this.cycles += 3;
                char c56 = this.PC;
                this.PC = (char)(c56 + '\u0001');
                this.A = (byte)(this.A ^ this.ram[this.ram[c56] & 0xFF]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 70: {
                this.cycles += 5;
                char c57 = this.PC;
                this.PC = (char)(c57 + '\u0001');
                byte oval = this.ram[this.ram[c57] & 0xFF];
                this.C = (oval & 1) == 1;
                int v = (oval & 0xFF) >> 1;
                char c58 = this.PC;
                this.PC = (char)(c58 + '\u0001');
                this.ram[this.ram[c58] & 255] = (byte)v;
                this.Z = this.A == 0;
                this.N = (byte)v < 0;
                break;
            }
            case 72: {
                this.cycles += 3;
                this.SP = (byte)(this.SP - 1);
                this.ram[512 + this.SP] = this.A;
                break;
            }
            case 73: {
                this.cycles += 2;
                char c59 = this.PC;
                this.PC = (char)(c59 + '\u0001');
                this.A = (byte)(this.A ^ this.ram[c59]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 74: {
                this.cycles += 2;
                byte oval = this.A;
                this.C = (oval & 1) == 1;
                int v = (oval & 0xFF) >> 1;
                this.A = (byte)v;
                this.Z = this.A == 0;
                this.N = (byte)v < 0;
                break;
            }
            case 76: {
                this.cycles += 3;
                char c60 = this.PC;
                char c61 = this.PC = (char)(c60 + '\u0001');
                this.PC = (char)(c61 + '\u0001');
                this.PC = (char)(this.ram[c60] & 0xFF | (this.ram[c61] & 0xFF) << 8);
                break;
            }
            case 77: {
                this.cycles += 4;
                char c62 = this.PC;
                char c63 = this.PC = (char)(c62 + '\u0001');
                this.PC = (char)(c63 + '\u0001');
                this.A = (byte)(this.A ^ this.ram[this.ram[c62] & 0xFF | (this.ram[c63] & 0xFF) << 8]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 78: {
                this.cycles += 6;
                char c64 = this.PC;
                char c65 = this.PC = (char)(c64 + '\u0001');
                this.PC = (char)(c65 + '\u0001');
                byte oval = this.ram[this.ram[c64] & 0xFF | (this.ram[c65] & 0xFF) << 8];
                this.C = (oval & 1) == 1;
                int v = (oval & 0xFF) >> 1;
                char c66 = this.PC;
                char c67 = this.PC = (char)(c66 + '\u0001');
                this.PC = (char)(c67 + '\u0001');
                this.ram[this.ram[c66] & 255 | (this.ram[c67] & 255) << 8] = (byte)v;
                this.Z = this.A == 0;
                this.N = (byte)v < 0;
                break;
            }
            case 80: {
                this.cycles += 2;
                char c68 = this.PC;
                this.PC = (char)(c68 + '\u0001');
                byte t = this.ram[c68];
                if (this.V) break;
                this.PC = (char)(this.PC + t);
                ++this.cycles;
                break;
            }
            case 81: {
                this.cycles += 5;
                char c69 = this.PC;
                this.PC = (char)(c69 + '\u0001');
                int pos = this.ram[c69] & 0xFF;
                this.A = (byte)(this.A ^ this.ram[(this.ram[pos] & 0xFF | (this.ram[pos + 1] & 0xFF) << 8) + (this.Y & 0xFF) & 0xFFFF]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 85: {
                this.cycles += 4;
                char c70 = this.PC;
                this.PC = (char)(c70 + '\u0001');
                this.A = (byte)(this.A ^ this.ram[this.ram[c70] + this.X & 0xFF]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 86: {
                this.cycles += 6;
                char c71 = this.PC;
                this.PC = (char)(c71 + '\u0001');
                byte oval = this.ram[this.ram[c71] + this.X & 0xFF];
                this.C = (oval & 1) == 1;
                int v = (oval & 0xFF) >> 1;
                char c72 = this.PC;
                this.PC = (char)(c72 + '\u0001');
                this.ram[this.ram[c72] + this.X & 255] = (byte)v;
                this.Z = this.A == 0;
                this.N = (byte)v < 0;
                break;
            }
            case 88: {
                this.cycles += 2;
                this.I = true;
                break;
            }
            case 89: {
                this.cycles += 4;
                char c73 = this.PC;
                char c74 = this.PC = (char)(c73 + '\u0001');
                this.PC = (char)(c74 + '\u0001');
                this.A = (byte)(this.A ^ this.ram[(this.ram[c73] & 0xFF | (this.ram[c74] & 0xFF) << 8) + (this.Y & 0xFF) & 0xFFFF]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 93: {
                this.cycles += 4;
                char c75 = this.PC;
                char c76 = this.PC = (char)(c75 + '\u0001');
                this.PC = (char)(c76 + '\u0001');
                this.A = (byte)(this.A ^ this.ram[(this.ram[c75] & 0xFF | (this.ram[c76] & 0xFF) << 8) + (this.X & 0xFF) & 0xFFFF]);
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 94: {
                this.cycles += 7;
                char c77 = this.PC;
                char c78 = this.PC = (char)(c77 + '\u0001');
                this.PC = (char)(c78 + '\u0001');
                byte oval = this.ram[(this.ram[c77] & 0xFF | (this.ram[c78] & 0xFF) << 8) + (this.X & 0xFF) & 0xFFFF];
                this.C = (oval & 1) == 1;
                int v = (oval & 0xFF) >> 1;
                char c79 = this.PC;
                char c80 = this.PC = (char)(c79 + '\u0001');
                this.PC = (char)(c80 + '\u0001');
                this.ram[(this.ram[c79] & 255 | (this.ram[c80] & 255) << 8) + (this.X & 255) & 65535] = (byte)v;
                this.Z = this.A == 0;
                this.N = (byte)v < 0;
                break;
            }
            case 96: {
                this.cycles += 6;
                byte by = this.SP;
                byte by4 = this.SP = (byte)(by + 1);
                this.SP = (byte)(by4 + 1);
                this.PC = (char)((this.ram[512 + by] & 0xFF | (this.ram[512 + by4] & 0xFF) << 8) + 1);
                break;
            }
            case 97: {
                this.cycles += 6;
                char c81 = this.PC;
                this.PC = (char)(c81 + '\u0001');
                int pos = this.ram[c81] + this.X & 0xFF;
                int v = (this.A & 0xFF) + (this.ram[this.ram[pos] & 0xFF | (this.ram[pos + 1] & 0xFF) << 8] & 0xFF);
                if (this.C) {
                    ++v;
                }
                this.A = (byte)v;
                this.Z = this.A == 0;
                this.N = this.A < 0;
                this.C = v > 255;
                break;
            }
            case 101: {
                this.cycles += 3;
                char c82 = this.PC;
                this.PC = (char)(c82 + '\u0001');
                int v = (this.A & 0xFF) + (this.ram[this.ram[c82] & 0xFF] & 0xFF);
                if (this.C) {
                    ++v;
                }
                this.A = (byte)v;
                this.Z = this.A == 0;
                this.N = this.A < 0;
                this.C = v > 255;
                break;
            }
            case 102: {
                this.cycles += 5;
                char c83 = this.PC;
                this.PC = (char)(c83 + '\u0001');
                byte oval = this.ram[this.ram[c83] & 0xFF];
                boolean oc = this.C;
                this.C = (oval & 1) == 1;
                int v = ((oval & 0xFF) >> 1) + (oc ? 128 : 0);
                char c84 = this.PC;
                this.PC = (char)(c84 + '\u0001');
                this.ram[this.ram[c84] & 255] = (byte)v;
                this.Z = this.A == 0;
                this.N = (byte)v < 0;
                break;
            }
            case 104: {
                this.cycles += 4;
                byte by = this.SP;
                this.SP = (byte)(by + 1);
                this.A = this.ram[512 + by];
                break;
            }
            case 105: {
                this.cycles += 2;
                char c85 = this.PC;
                this.PC = (char)(c85 + '\u0001');
                int v = (this.A & 0xFF) + (this.ram[c85] & 0xFF);
                if (this.C) {
                    ++v;
                }
                this.A = (byte)v;
                this.Z = this.A == 0;
                this.N = this.A < 0;
                this.C = v > 255;
                break;
            }
            case 106: {
                this.cycles += 2;
                byte oval = this.A;
                boolean oc = this.C;
                this.C = (oval & 1) == 1;
                int v = ((oval & 0xFF) >> 1) + (oc ? 128 : 0);
                this.A = (byte)v;
                this.Z = this.A == 0;
                this.N = (byte)v < 0;
                break;
            }
            case 108: {
                this.cycles += 5;
                char c86 = this.PC;
                this.PC = (char)(c86 + '\u0001');
                int pos = this.ram[this.ram[c86] & 0xFF] & 0xFF;
                this.PC = (char)(this.ram[pos] & 0xFF | (this.ram[pos + 1] & 0xFF) << 8);
                break;
            }
            case 109: {
                this.cycles += 4;
                char c87 = this.PC;
                char c88 = this.PC = (char)(c87 + '\u0001');
                this.PC = (char)(c88 + '\u0001');
                int v = (this.A & 0xFF) + (this.ram[this.ram[c87] & 0xFF | (this.ram[c88] & 0xFF) << 8] & 0xFF);
                if (this.C) {
                    ++v;
                }
                this.A = (byte)v;
                this.Z = this.A == 0;
                this.N = this.A < 0;
                this.C = v > 255;
                break;
            }
            case 110: {
                this.cycles += 6;
                char c89 = this.PC;
                char c90 = this.PC = (char)(c89 + '\u0001');
                this.PC = (char)(c90 + '\u0001');
                byte oval = this.ram[this.ram[c89] & 0xFF | (this.ram[c90] & 0xFF) << 8];
                boolean oc = this.C;
                this.C = (oval & 1) == 1;
                int v = ((oval & 0xFF) >> 1) + (oc ? 128 : 0);
                char c91 = this.PC;
                char c92 = this.PC = (char)(c91 + '\u0001');
                this.PC = (char)(c92 + '\u0001');
                this.ram[this.ram[c91] & 255 | (this.ram[c92] & 255) << 8] = (byte)v;
                this.Z = this.A == 0;
                this.N = (byte)v < 0;
                break;
            }
            case 112: {
                this.cycles += 2;
                char c93 = this.PC;
                this.PC = (char)(c93 + '\u0001');
                byte t = this.ram[c93];
                if (!this.V) break;
                this.PC = (char)(this.PC + t);
                ++this.cycles;
                break;
            }
            case 113: {
                this.cycles += 5;
                char c94 = this.PC;
                this.PC = (char)(c94 + '\u0001');
                int pos = this.ram[c94] & 0xFF;
                int v = (this.A & 0xFF) + (this.ram[(this.ram[pos] & 0xFF | (this.ram[pos + 1] & 0xFF) << 8) + (this.Y & 0xFF) & 0xFFFF] & 0xFF);
                if (this.C) {
                    ++v;
                }
                this.A = (byte)v;
                this.Z = this.A == 0;
                this.N = this.A < 0;
                this.C = v > 255;
                break;
            }
            case 117: {
                this.cycles += 4;
                char c95 = this.PC;
                this.PC = (char)(c95 + '\u0001');
                int v = (this.A & 0xFF) + (this.ram[this.ram[c95] + this.X & 0xFF] & 0xFF);
                if (this.C) {
                    ++v;
                }
                this.A = (byte)v;
                this.Z = this.A == 0;
                this.N = this.A < 0;
                this.C = v > 255;
                break;
            }
            case 118: {
                this.cycles += 6;
                char c96 = this.PC;
                this.PC = (char)(c96 + '\u0001');
                byte oval = this.ram[this.ram[c96] + this.X & 0xFF];
                boolean oc = this.C;
                this.C = (oval & 1) == 1;
                int v = ((oval & 0xFF) >> 1) + (oc ? 128 : 0);
                char c97 = this.PC;
                this.PC = (char)(c97 + '\u0001');
                this.ram[this.ram[c97] + this.X & 255] = (byte)v;
                this.Z = this.A == 0;
                this.N = (byte)v < 0;
                break;
            }
            case 120: {
                this.cycles += 2;
                this.I = false;
                break;
            }
            case 121: {
                this.cycles += 4;
                char c98 = this.PC;
                char c99 = this.PC = (char)(c98 + '\u0001');
                this.PC = (char)(c99 + '\u0001');
                int v = (this.A & 0xFF) + (this.ram[(this.ram[c98] & 0xFF | (this.ram[c99] & 0xFF) << 8) + (this.Y & 0xFF) & 0xFFFF] & 0xFF);
                if (this.C) {
                    ++v;
                }
                this.A = (byte)v;
                this.Z = this.A == 0;
                this.N = this.A < 0;
                this.C = v > 255;
                break;
            }
            case 125: {
                this.cycles += 4;
                char c100 = this.PC;
                char c101 = this.PC = (char)(c100 + '\u0001');
                this.PC = (char)(c101 + '\u0001');
                int v = (this.A & 0xFF) + (this.ram[(this.ram[c100] & 0xFF | (this.ram[c101] & 0xFF) << 8) + (this.X & 0xFF) & 0xFFFF] & 0xFF);
                if (this.C) {
                    ++v;
                }
                this.A = (byte)v;
                this.Z = this.A == 0;
                this.N = this.A < 0;
                this.C = v > 255;
                break;
            }
            case 126: {
                this.cycles += 7;
                char c102 = this.PC;
                char c103 = this.PC = (char)(c102 + '\u0001');
                this.PC = (char)(c103 + '\u0001');
                byte oval = this.ram[(this.ram[c102] & 0xFF | (this.ram[c103] & 0xFF) << 8) + (this.X & 0xFF) & 0xFFFF];
                boolean oc = this.C;
                this.C = (oval & 1) == 1;
                int v = ((oval & 0xFF) >> 1) + (oc ? 128 : 0);
                char c104 = this.PC;
                char c105 = this.PC = (char)(c104 + '\u0001');
                this.PC = (char)(c105 + '\u0001');
                this.ram[(this.ram[c104] & 255 | (this.ram[c105] & 255) << 8) + (this.X & 255) & 65535] = (byte)v;
                this.Z = this.A == 0;
                this.N = (byte)v < 0;
                break;
            }
            case 129: {
                this.cycles += 6;
                char c106 = this.PC;
                this.PC = (char)(c106 + '\u0001');
                int pos = this.ram[c106] + this.X & 0xFF;
                this.ram[this.ram[pos] & 255 | (this.ram[pos + 1] & 255) << 8] = this.A;
                break;
            }
            case 132: {
                this.cycles += 3;
                char c107 = this.PC;
                this.PC = (char)(c107 + '\u0001');
                this.ram[this.ram[c107] & 255] = this.Y;
                break;
            }
            case 133: {
                this.cycles += 3;
                char c108 = this.PC;
                this.PC = (char)(c108 + '\u0001');
                this.ram[this.ram[c108] & 255] = this.A;
                break;
            }
            case 134: {
                this.cycles += 3;
                char c109 = this.PC;
                this.PC = (char)(c109 + '\u0001');
                this.ram[this.ram[c109] & 255] = this.X;
                break;
            }
            case 136: {
                this.cycles += 2;
                this.Y = (byte)(this.Y - 1);
                this.Z = this.Y == 0;
                this.N = this.Y < 0;
                break;
            }
            case 138: {
                this.cycles += 2;
                this.A = this.X;
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 140: {
                this.cycles += 4;
                char c110 = this.PC;
                char c111 = this.PC = (char)(c110 + '\u0001');
                this.PC = (char)(c111 + '\u0001');
                this.ram[this.ram[c110] & 255 | (this.ram[c111] & 255) << 8] = this.Y;
                break;
            }
            case 141: {
                this.cycles += 4;
                char c112 = this.PC;
                char c113 = this.PC = (char)(c112 + '\u0001');
                this.PC = (char)(c113 + '\u0001');
                this.ram[this.ram[c112] & 255 | (this.ram[c113] & 255) << 8] = this.A;
                break;
            }
            case 142: {
                this.cycles += 4;
                char c114 = this.PC;
                char c115 = this.PC = (char)(c114 + '\u0001');
                this.PC = (char)(c115 + '\u0001');
                this.ram[this.ram[c114] & 255 | (this.ram[c115] & 255) << 8] = this.X;
                break;
            }
            case 144: {
                this.cycles += 2;
                char c116 = this.PC;
                this.PC = (char)(c116 + '\u0001');
                byte t = this.ram[c116];
                if (this.C) break;
                this.PC = (char)(this.PC + t);
                ++this.cycles;
                break;
            }
            case 145: {
                this.cycles += 6;
                char c117 = this.PC;
                this.PC = (char)(c117 + '\u0001');
                int pos = this.ram[c117] & 0xFF;
                this.ram[(this.ram[pos] & 255 | (this.ram[pos + 1] & 255) << 8) + (this.Y & 255) & 65535] = this.A;
                break;
            }
            case 148: {
                this.cycles += 4;
                char c118 = this.PC;
                this.PC = (char)(c118 + '\u0001');
                this.ram[this.ram[c118] + this.Y & 255] = this.Y;
                break;
            }
            case 149: {
                this.cycles += 4;
                char c119 = this.PC;
                this.PC = (char)(c119 + '\u0001');
                this.ram[this.ram[c119] + this.X & 255] = this.A;
                break;
            }
            case 150: {
                this.cycles += 4;
                char c120 = this.PC;
                this.PC = (char)(c120 + '\u0001');
                this.ram[this.ram[c120] + this.Y & 255] = this.X;
                break;
            }
            case 152: {
                this.cycles += 2;
                this.A = this.Y;
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 153: {
                this.cycles += 5;
                char c121 = this.PC;
                char c122 = this.PC = (char)(c121 + '\u0001');
                this.PC = (char)(c122 + '\u0001');
                this.ram[(this.ram[c121] & 255 | (this.ram[c122] & 255) << 8) + (this.Y & 255) & 65535] = this.A;
                break;
            }
            case 154: {
                this.cycles += 2;
                this.SP = this.X;
                break;
            }
            case 157: {
                this.cycles += 5;
                char c123 = this.PC;
                char c124 = this.PC = (char)(c123 + '\u0001');
                this.PC = (char)(c124 + '\u0001');
                this.ram[(this.ram[c123] & 255 | (this.ram[c124] & 255) << 8) + (this.X & 255) & 65535] = this.A;
                break;
            }
            case 160: {
                this.cycles += 2;
                char c125 = this.PC;
                this.PC = (char)(c125 + '\u0001');
                this.Y = this.ram[c125];
                this.Z = this.Y == 0;
                this.N = this.Y < 0;
                break;
            }
            case 161: {
                this.cycles += 6;
                char c126 = this.PC;
                this.PC = (char)(c126 + '\u0001');
                int pos = this.ram[c126] + this.X & 0xFF;
                this.A = this.ram[this.ram[pos] & 0xFF | (this.ram[pos + 1] & 0xFF) << 8];
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 162: {
                this.cycles += 2;
                char c127 = this.PC;
                this.PC = (char)(c127 + '\u0001');
                this.X = this.ram[c127];
                this.Z = this.X == 0;
                this.N = this.X < 0;
                break;
            }
            case 164: {
                this.cycles += 3;
                char c128 = this.PC;
                this.PC = (char)(c128 + '\u0001');
                this.Y = this.ram[this.ram[c128] & 0xFF];
                this.Z = this.Y == 0;
                this.N = this.Y < 0;
                break;
            }
            case 165: {
                this.cycles += 3;
                char c129 = this.PC;
                this.PC = (char)(c129 + '\u0001');
                this.A = this.ram[this.ram[c129] & 0xFF];
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 166: {
                this.cycles += 3;
                char c130 = this.PC;
                this.PC = (char)(c130 + '\u0001');
                this.X = this.ram[this.ram[c130] & 0xFF];
                this.Z = this.X == 0;
                this.N = this.X < 0;
                break;
            }
            case 168: {
                this.cycles += 2;
                this.Y = this.A;
                this.Z = this.Y == 0;
                this.N = this.Y < 0;
                break;
            }
            case 169: {
                this.cycles += 2;
                char c131 = this.PC;
                this.PC = (char)(c131 + '\u0001');
                this.A = this.ram[c131];
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 170: {
                this.cycles += 2;
                this.X = this.A;
                this.Z = this.X == 0;
                this.N = this.X < 0;
                break;
            }
            case 172: {
                this.cycles += 4;
                char c132 = this.PC;
                char c133 = this.PC = (char)(c132 + '\u0001');
                this.PC = (char)(c133 + '\u0001');
                this.Y = this.ram[this.ram[c132] & 0xFF | (this.ram[c133] & 0xFF) << 8];
                this.Z = this.Y == 0;
                this.N = this.Y < 0;
                break;
            }
            case 173: {
                this.cycles += 4;
                char c134 = this.PC;
                char c135 = this.PC = (char)(c134 + '\u0001');
                this.PC = (char)(c135 + '\u0001');
                this.A = this.ram[this.ram[c134] & 0xFF | (this.ram[c135] & 0xFF) << 8];
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 174: {
                this.cycles += 4;
                char c136 = this.PC;
                char c137 = this.PC = (char)(c136 + '\u0001');
                this.PC = (char)(c137 + '\u0001');
                this.X = this.ram[this.ram[c136] & 0xFF | (this.ram[c137] & 0xFF) << 8];
                this.Z = this.X == 0;
                this.N = this.X < 0;
                break;
            }
            case 176: {
                this.cycles += 2;
                char c138 = this.PC;
                this.PC = (char)(c138 + '\u0001');
                byte t = this.ram[c138];
                if (!this.C) break;
                this.PC = (char)(this.PC + t);
                ++this.cycles;
                break;
            }
            case 177: {
                this.cycles += 5;
                char c139 = this.PC;
                this.PC = (char)(c139 + '\u0001');
                int pos = this.ram[c139] & 0xFF;
                this.A = this.ram[(this.ram[pos] & 0xFF | (this.ram[pos + 1] & 0xFF) << 8) + (this.Y & 0xFF) & 0xFFFF];
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 180: {
                this.cycles += 4;
                char c140 = this.PC;
                this.PC = (char)(c140 + '\u0001');
                this.Y = this.ram[this.ram[c140] + this.Y & 0xFF];
                this.Z = this.Y == 0;
                this.N = this.Y < 0;
                break;
            }
            case 181: {
                this.cycles += 4;
                char c141 = this.PC;
                this.PC = (char)(c141 + '\u0001');
                this.A = this.ram[this.ram[c141] + this.X & 0xFF];
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 182: {
                this.cycles += 4;
                char c142 = this.PC;
                this.PC = (char)(c142 + '\u0001');
                this.X = this.ram[this.ram[c142] + this.Y & 0xFF];
                this.Z = this.X == 0;
                this.N = this.X < 0;
                break;
            }
            case 184: {
                this.cycles += 2;
                this.V = false;
                break;
            }
            case 185: {
                this.cycles += 4;
                char c143 = this.PC;
                char c144 = this.PC = (char)(c143 + '\u0001');
                this.PC = (char)(c144 + '\u0001');
                this.A = this.ram[(this.ram[c143] & 0xFF | (this.ram[c144] & 0xFF) << 8) + (this.Y & 0xFF) & 0xFFFF];
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 186: {
                this.cycles += 2;
                this.X = this.SP;
                this.Z = this.X == 0;
                this.N = this.X < 0;
                break;
            }
            case 188: {
                this.cycles += 4;
                char c145 = this.PC;
                char c146 = this.PC = (char)(c145 + '\u0001');
                this.PC = (char)(c146 + '\u0001');
                this.Y = this.ram[(this.ram[c145] & 0xFF | (this.ram[c146] & 0xFF) << 8) + (this.Y & 0xFF) & 0xFFFF];
                this.Z = this.Y == 0;
                this.N = this.Y < 0;
                break;
            }
            case 189: {
                this.cycles += 4;
                char c147 = this.PC;
                char c148 = this.PC = (char)(c147 + '\u0001');
                this.PC = (char)(c148 + '\u0001');
                this.A = this.ram[(this.ram[c147] & 0xFF | (this.ram[c148] & 0xFF) << 8) + (this.X & 0xFF) & 0xFFFF];
                this.Z = this.A == 0;
                this.N = this.A < 0;
                break;
            }
            case 190: {
                this.cycles += 4;
                char c149 = this.PC;
                char c150 = this.PC = (char)(c149 + '\u0001');
                this.PC = (char)(c150 + '\u0001');
                this.X = this.ram[(this.ram[c149] & 0xFF | (this.ram[c150] & 0xFF) << 8) + (this.Y & 0xFF) & 0xFFFF];
                this.Z = this.X == 0;
                this.N = this.X < 0;
                break;
            }
            case 192: {
                this.cycles += 2;
                char c151 = this.PC;
                this.PC = (char)(c151 + '\u0001');
                byte v = this.ram[c151];
                this.C = (this.Y & 0xFF) >= (v & 0xFF);
                this.Z = this.Y == v;
                this.N = this.Y - v < 0;
                break;
            }
            case 193: {
                this.cycles += 6;
                char c152 = this.PC;
                this.PC = (char)(c152 + '\u0001');
                int pos = this.ram[c152] + this.X & 0xFF;
                byte v = this.ram[this.ram[pos] & 0xFF | (this.ram[pos + 1] & 0xFF) << 8];
                this.C = (this.A & 0xFF) >= (v & 0xFF);
                this.Z = this.A == v;
                this.N = this.A - v < 0;
                break;
            }
            case 196: {
                this.cycles += 3;
                char c153 = this.PC;
                this.PC = (char)(c153 + '\u0001');
                byte v = this.ram[this.ram[c153] & 0xFF];
                this.C = (this.Y & 0xFF) >= (v & 0xFF);
                this.Z = this.Y == v;
                this.N = this.Y - v < 0;
                break;
            }
            case 197: {
                this.cycles += 3;
                char c154 = this.PC;
                this.PC = (char)(c154 + '\u0001');
                byte v = this.ram[this.ram[c154] & 0xFF];
                this.C = (this.A & 0xFF) >= (v & 0xFF);
                this.Z = this.A == v;
                this.N = this.A - v < 0;
                break;
            }
            case 198: {
                this.cycles += 5;
                char c155 = this.PC;
                this.PC = (char)(c155 + '\u0001');
                int n = this.ram[c155] & 0xFF;
                byte by = (byte)(this.ram[n] - 1);
                this.ram[n] = by;
                byte v = by;
                this.Z = v == 0;
                this.N = v < 0;
                break;
            }
            case 200: {
                this.cycles += 2;
                this.Y = (byte)(this.Y + 1);
                this.Z = this.Y == 0;
                this.N = this.Y < 0;
                break;
            }
            case 201: {
                this.cycles += 2;
                char c156 = this.PC;
                this.PC = (char)(c156 + '\u0001');
                byte v = this.ram[c156];
                this.C = (this.A & 0xFF) >= (v & 0xFF);
                this.Z = this.A == v;
                this.N = this.A - v < 0;
                break;
            }
            case 202: {
                this.cycles += 2;
                this.X = (byte)(this.X - 1);
                this.Z = this.X == 0;
                this.N = this.X < 0;
                break;
            }
            case 204: {
                this.cycles += 4;
                char c157 = this.PC;
                char c158 = this.PC = (char)(c157 + '\u0001');
                this.PC = (char)(c158 + '\u0001');
                byte v = this.ram[this.ram[c157] & 0xFF | (this.ram[c158] & 0xFF) << 8];
                this.C = (this.Y & 0xFF) >= (v & 0xFF);
                this.Z = this.Y == v;
                this.N = this.Y - v < 0;
                break;
            }
            case 205: {
                this.cycles += 4;
                char c159 = this.PC;
                char c160 = this.PC = (char)(c159 + '\u0001');
                this.PC = (char)(c160 + '\u0001');
                byte v = this.ram[this.ram[c159] & 0xFF | (this.ram[c160] & 0xFF) << 8];
                this.C = (this.A & 0xFF) >= (v & 0xFF);
                this.Z = this.A == v;
                this.N = this.A - v < 0;
                break;
            }
            case 206: {
                this.cycles += 3;
                char c161 = this.PC;
                char c162 = this.PC = (char)(c161 + '\u0001');
                this.PC = (char)(c162 + '\u0001');
                int n = this.ram[c161] & 0xFF | (this.ram[c162] & 0xFF) << 8;
                byte by = (byte)(this.ram[n] - 1);
                this.ram[n] = by;
                byte v = by;
                this.Z = v == 0;
                this.N = v < 0;
                break;
            }
            case 208: {
                this.cycles += 2;
                char c163 = this.PC;
                this.PC = (char)(c163 + '\u0001');
                byte t = this.ram[c163];
                if (this.Z) break;
                this.PC = (char)(this.PC + t);
                ++this.cycles;
                break;
            }
            case 209: {
                this.cycles += 5;
                char c164 = this.PC;
                this.PC = (char)(c164 + '\u0001');
                int pos = this.ram[c164] & 0xFF;
                byte v = this.ram[(this.ram[pos] & 0xFF | (this.ram[pos + 1] & 0xFF) << 8) + (this.Y & 0xFF) & 0xFFFF];
                this.C = (this.A & 0xFF) >= (v & 0xFF);
                this.Z = this.A == v;
                this.N = this.A - v < 0;
                break;
            }
            case 213: {
                this.cycles += 4;
                char c165 = this.PC;
                this.PC = (char)(c165 + '\u0001');
                byte v = this.ram[this.ram[c165] + this.X & 0xFF];
                this.C = (this.A & 0xFF) >= (v & 0xFF);
                this.Z = this.A == v;
                this.N = this.A - v < 0;
                break;
            }
            case 214: {
                this.cycles += 6;
                char c166 = this.PC;
                this.PC = (char)(c166 + '\u0001');
                int n = this.ram[c166] + this.X & 0xFF;
                byte by = (byte)(this.ram[n] - 1);
                this.ram[n] = by;
                byte v = by;
                this.Z = v == 0;
                this.N = v < 0;
                break;
            }
            case 216: {
                this.cycles += 2;
                this.D = false;
                break;
            }
            case 217: {
                this.cycles += 4;
                char c167 = this.PC;
                char c168 = this.PC = (char)(c167 + '\u0001');
                this.PC = (char)(c168 + '\u0001');
                byte v = this.ram[(this.ram[c167] & 0xFF | (this.ram[c168] & 0xFF) << 8) + (this.Y & 0xFF) & 0xFFFF];
                this.C = (this.A & 0xFF) >= (v & 0xFF);
                this.Z = this.A == v;
                this.N = this.A - v < 0;
                break;
            }
            case 221: {
                this.cycles += 4;
                char c169 = this.PC;
                char c170 = this.PC = (char)(c169 + '\u0001');
                this.PC = (char)(c170 + '\u0001');
                byte v = this.ram[(this.ram[c169] & 0xFF | (this.ram[c170] & 0xFF) << 8) + (this.X & 0xFF) & 0xFFFF];
                this.C = (this.A & 0xFF) >= (v & 0xFF);
                this.Z = this.A == v;
                this.N = this.A - v < 0;
                break;
            }
            case 222: {
                this.cycles += 7;
                char c171 = this.PC;
                char c172 = this.PC = (char)(c171 + '\u0001');
                this.PC = (char)(c172 + '\u0001');
                int n = (this.ram[c171] & 0xFF | (this.ram[c172] & 0xFF) << 8) + (this.X & 0xFF) & 0xFFFF;
                byte by = (byte)(this.ram[n] - 1);
                this.ram[n] = by;
                byte v = by;
                this.Z = v == 0;
                this.N = v < 0;
                break;
            }
            case 224: {
                this.cycles += 2;
                char c173 = this.PC;
                this.PC = (char)(c173 + '\u0001');
                byte v = this.ram[c173];
                this.C = (this.X & 0xFF) >= (v & 0xFF);
                this.Z = this.X == v;
                this.N = this.X - v < 0;
                break;
            }
            case 225: {
                this.cycles += 6;
                char c174 = this.PC;
                this.PC = (char)(c174 + '\u0001');
                int pos = this.ram[c174] + this.X & 0xFF;
                int v = (this.A & 0xFF) - (this.ram[this.ram[pos] & 0xFF | (this.ram[pos + 1] & 0xFF) << 8] & 0xFF);
                if (!this.C) {
                    --v;
                }
                this.A = (byte)v;
                this.Z = this.A == 0;
                this.N = this.A < 0;
                this.C = v >= 0;
                break;
            }
            case 228: {
                this.cycles += 3;
                char c175 = this.PC;
                this.PC = (char)(c175 + '\u0001');
                byte v = this.ram[this.ram[c175] & 0xFF];
                this.C = (this.X & 0xFF) >= (v & 0xFF);
                this.Z = this.X == v;
                this.N = this.X - v < 0;
                break;
            }
            case 229: {
                this.cycles += 3;
                char c176 = this.PC;
                this.PC = (char)(c176 + '\u0001');
                int v = (this.A & 0xFF) - (this.ram[this.ram[c176] & 0xFF] & 0xFF);
                if (!this.C) {
                    --v;
                }
                this.A = (byte)v;
                this.Z = this.A == 0;
                this.N = this.A < 0;
                this.C = v >= 0;
                break;
            }
            case 230: {
                this.cycles += 5;
                char c177 = this.PC;
                this.PC = (char)(c177 + '\u0001');
                int n = this.ram[c177] & 0xFF;
                byte by = (byte)(this.ram[n] + 1);
                this.ram[n] = by;
                byte v = by;
                this.Z = v == 0;
                this.N = v < 0;
                break;
            }
            case 232: {
                this.cycles += 2;
                this.X = (byte)(this.X + 1);
                this.Z = this.X == 0;
                this.N = this.X < 0;
                break;
            }
            case 233: {
                this.cycles += 2;
                char c178 = this.PC;
                this.PC = (char)(c178 + '\u0001');
                int v = (this.A & 0xFF) - (this.ram[c178] & 0xFF);
                if (!this.C) {
                    --v;
                }
                this.A = (byte)v;
                this.Z = this.A == 0;
                this.N = this.A < 0;
                this.C = v >= 0;
                break;
            }
            case 234: {
                this.cycles += 2;
                break;
            }
            case 236: {
                this.cycles += 4;
                char c179 = this.PC;
                char c180 = this.PC = (char)(c179 + '\u0001');
                this.PC = (char)(c180 + '\u0001');
                byte v = this.ram[this.ram[c179] & 0xFF | (this.ram[c180] & 0xFF) << 8];
                this.C = (this.X & 0xFF) >= (v & 0xFF);
                this.Z = this.X == v;
                this.N = this.X - v < 0;
                break;
            }
            case 237: {
                this.cycles += 4;
                char c181 = this.PC;
                char c182 = this.PC = (char)(c181 + '\u0001');
                this.PC = (char)(c182 + '\u0001');
                int v = (this.A & 0xFF) - (this.ram[this.ram[c181] & 0xFF | (this.ram[c182] & 0xFF) << 8] & 0xFF);
                if (!this.C) {
                    --v;
                }
                this.A = (byte)v;
                this.Z = this.A == 0;
                this.N = this.A < 0;
                this.C = v >= 0;
                break;
            }
            case 238: {
                this.cycles += 6;
                char c183 = this.PC;
                char c184 = this.PC = (char)(c183 + '\u0001');
                this.PC = (char)(c184 + '\u0001');
                int n = this.ram[c183] & 0xFF | (this.ram[c184] & 0xFF) << 8;
                byte by = (byte)(this.ram[n] + 1);
                this.ram[n] = by;
                byte v = by;
                this.Z = v == 0;
                this.N = v < 0;
                break;
            }
            case 240: {
                this.cycles += 2;
                char c185 = this.PC;
                this.PC = (char)(c185 + '\u0001');
                byte t = this.ram[c185];
                if (!this.Z) break;
                this.PC = (char)(this.PC + t);
                ++this.cycles;
                break;
            }
            case 241: {
                this.cycles += 5;
                char c186 = this.PC;
                this.PC = (char)(c186 + '\u0001');
                int pos = this.ram[c186] & 0xFF;
                int v = (this.A & 0xFF) - (this.ram[(this.ram[pos] & 0xFF | (this.ram[pos + 1] & 0xFF) << 8) + (this.Y & 0xFF) & 0xFFFF] & 0xFF);
                if (!this.C) {
                    --v;
                }
                this.A = (byte)v;
                this.Z = this.A == 0;
                this.N = this.A < 0;
                this.C = v >= 0;
                break;
            }
            case 245: {
                this.cycles += 4;
                char c187 = this.PC;
                this.PC = (char)(c187 + '\u0001');
                int v = (this.A & 0xFF) - (this.ram[this.ram[c187] + this.X & 0xFF] & 0xFF);
                if (!this.C) {
                    --v;
                }
                this.A = (byte)v;
                this.Z = this.A == 0;
                this.N = this.A < 0;
                this.C = v >= 0;
                break;
            }
            case 246: {
                this.cycles += 6;
                char c188 = this.PC;
                this.PC = (char)(c188 + '\u0001');
                int n = this.ram[c188] + this.X & 0xFF;
                byte by = (byte)(this.ram[n] + 1);
                this.ram[n] = by;
                byte v = by;
                this.Z = v == 0;
                this.N = v < 0;
                break;
            }
            case 248: {
                this.cycles += 2;
                this.D = true;
                break;
            }
            case 249: {
                this.cycles += 4;
                char c189 = this.PC;
                char c190 = this.PC = (char)(c189 + '\u0001');
                this.PC = (char)(c190 + '\u0001');
                int v = (this.A & 0xFF) - (this.ram[(this.ram[c189] & 0xFF | (this.ram[c190] & 0xFF) << 8) + (this.Y & 0xFF) & 0xFFFF] & 0xFF);
                if (!this.C) {
                    --v;
                }
                this.A = (byte)v;
                this.Z = this.A == 0;
                this.N = this.A < 0;
                this.C = v >= 0;
                break;
            }
            case 253: {
                this.cycles += 4;
                char c191 = this.PC;
                char c192 = this.PC = (char)(c191 + '\u0001');
                this.PC = (char)(c192 + '\u0001');
                int v = (this.A & 0xFF) - (this.ram[(this.ram[c191] & 0xFF | (this.ram[c192] & 0xFF) << 8) + (this.X & 0xFF) & 0xFFFF] & 0xFF);
                if (!this.C) {
                    --v;
                }
                this.A = (byte)v;
                this.Z = this.A == 0;
                this.N = this.A < 0;
                this.C = v >= 0;
                break;
            }
            case 254: {
                this.cycles += 7;
                char c193 = this.PC;
                char c194 = this.PC = (char)(c193 + '\u0001');
                this.PC = (char)(c194 + '\u0001');
                int n = (this.ram[c193] & 0xFF | (this.ram[c194] & 0xFF) << 8) + (this.X & 0xFF) & 0xFFFF;
                byte by = (byte)(this.ram[n] + 1);
                this.ram[n] = by;
                byte v = by;
                this.Z = v == 0;
                this.N = v < 0;
                break;
            }
            default: {
                this.cycles += 2;
            }
        }
    }
}

