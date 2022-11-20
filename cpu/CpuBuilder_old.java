
package cpu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CpuBuilder_old {
    private static final String NOT_USED = "/* not implemented */;";
    public static final int INDIRECT_X = 0;
    public static final int ZERO_PAGE = 1;
    public static final int IMMEDIATE = 2;
    public static final int ABSOLUTE = 3;
    public static final int INDIRECT_Y = 4;
    public static final int ZERO_PAGE_X = 5;
    public static final int ABSOLUTE_Y = 6;
    public static final int ABSOLUTE_X = 7;
    public static final int INDIRECT = 8;
    public static final int ACCUMULATOR = 9;
    public static final int ZERO_PAGE_Y = 10;
    public static final int RELATIVE = 11;
    private static final String NEXT_BYTE_STR = "ram[PC++]";
    private static final String INDIRECT_X_STR = "int pos=(nextByte()+X)&0xff; ram[(ram[pos]&0xff)|((ram[pos+1]&0xff)<<8)]";
    private static final String ZERO_PAGE_STR = "ram[nextByte()&0xff]";
    private static final String IMMEDIATE_STR = "nextByte()";
    private static final String ABSOLUTE_STR = "ram[(nextByte()&0xff)|((nextByte()&0xff)<<8)]";
    private static final String INDIRECT_Y_STR = "int pos=nextByte()&0xff; ram[(((ram[pos]&0xff)|((ram[pos+1]&0xff)<<8))+(Y&0xff))&0xffff]";
    private static final String ZERO_PAGE_X_STR = "ram[(nextByte()+X)&0xff]";
    private static final String ZERO_PAGE_Y_STR = "ram[(nextByte()+Y)&0xff]";
    private static final String ABSOLUTE_Y_STR = "ram[(((nextByte()&0xff)|((nextByte()&0xff)<<8))+(Y&0xff))&0xffff]";
    private static final String ABSOLUTE_X_STR = "ram[(((nextByte()&0xff)|((nextByte()&0xff)<<8))+(X&0xff))&0xffff]";
    private static final String INDIRECT_STR = "int pos=ram[nextByte()&0xff]&0xff; ram[(ram[pos]&0xff)|((ram[pos+1]&0xff)<<8)]";
    private static final String ACCUMULATOR_STR = "A";
    private static final String RELATIVE_STR = "nextByte()";
    public PrintWriter pw;
    public boolean[] validCode = new boolean[256];
    public String[] opCodes = new String[256];
    public int[] cycles = new int[256];

    private void build() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("res/opcodes.txt"));
        String line = "";
        String start = "--------------------------------------------";
        String end = "";
        boolean decoding = false;
        while ((line = br.readLine()) != null) {
            if ((line = line.trim()).equals(start)) {
                decoding = true;
                continue;
            }
            if (line.equals(end)) {
                decoding = false;
                continue;
            }
            if (!decoding) continue;
            String[] words = line.trim().split(" ( )+");
            int opcode = Integer.parseInt(words[2], 16);
            String cStr = words[4];
            int cc = 0;
            if (cStr.endsWith("**")) {
                cc = Integer.parseInt(cStr.substring(0, cStr.length() - 2));
                System.out.println(line);
            } else if (cStr.endsWith("*")) {
                cc = Integer.parseInt(cStr.substring(0, cStr.length() - 1));
                System.out.println(line);
            } else {
                cc = Integer.parseInt(cStr);
            }
            this.cycles[opcode] = cc;
            this.validCode[opcode] = true;
            this.opCodes[opcode] = words[1];
        }
        this.pw = new PrintWriter(new FileWriter(new File("src/cpu/CPU.java")));
        this.writeHeader();
        this.writeOpCodes();
        this.writeFooter();
        this.pw.close();
    }

    private void writeHeader() {
        this.pw.println("package cpu;");
        this.pw.println("");
        this.pw.println("public class CPU {");
        this.pw.println("  public boolean C, Z, I, D, B, V, N;");
        this.pw.println("  public char PC = 0x600;");
        this.pw.println("  public byte SP, A, X, Y;");
        this.pw.println("  public int cycles = 0;");
        this.pw.println("");
        this.pw.println("  public byte[] ram = new byte[65536];");
        this.pw.println("");
        this.pw.println("  public void tick() {");
        this.pw.println("    byte opCode=ram[PC++];");
        this.pw.println("    switch (opCode&0xff) {");
    }

    private void writeFooter() {
        this.pw.println("      default:{cycles+=2;break;}");
        this.pw.println("    }");
        this.pw.println("  }");
        this.pw.println("}");
    }

    private void writeOpCodes() {
        int opCode = 0;
        while (opCode < 256) {
            if (this.validCode[opCode]) {
                int aaa = opCode >> 5 & 7;
                int bbb = opCode >> 2 & 7;
                int cc = opCode >> 0 & 3;
                String str = "";
                int mode = -1;
                if (opCode == 0) {
                    str = NOT_USED;
                } else if (opCode == 32) {
                    str = "ram[0x200+--SP]=(byte)(((PC+1)>>8)&0xff);ram[0x200+--SP]=(byte)(((PC+1))&0xff);PC=(char)((nextByte()&0xff)|((nextByte()&0xff)<<8));";
                } else if (opCode == 64) {
                    str = "int flags=ram[0x200+SP++]&0xff;C=(flags&1)>0;Z=(flags&2)>0;I=(flags&4)>0;D=(flags&8)>0;B=(flags&16)>0;V=(flags&64)>0;N=(flags&128)>0;PC=(char)((ram[0x200+SP++]&0xff)|((ram[0x200+SP++]&0xff)<<8));";
                } else if (opCode == 96) {
                    str = "PC=(char)((((ram[0x200+SP++]&0xff))|((ram[0x200+SP++]&0xff)<<8))+1);";
                } else if (opCode == 8) {
                    str = "int flags=0;if(C)flags+=1;if(Z)flags+=2;if(I)flags+=4;if(D)flags+=8;if(B)flags+=16;if(V)flags+=64;if(N)flags+=128;ram[0x200+--SP]=(byte)(flags);";
                } else if (opCode == 40) {
                    str = "int flags=ram[0x200+SP++]&0xff;C=(flags&1)>0;Z=(flags&2)>0;I=(flags&4)>0;D=(flags&8)>0;B=(flags&16)>0;V=(flags&64)>0;N=(flags&128)>0;";
                } else if (opCode == 72) {
                    str = "ram[0x200+--SP]=A;";
                } else if (opCode == 104) {
                    str = "A=ram[0x200+SP++];";
                } else if (opCode == 136) {
                    str = "--Y;Z=Y==0;N=Y<0;";
                } else if (opCode == 168) {
                    str = "Y=A;Z=Y==0;N=Y<0;";
                } else if (opCode == 200) {
                    str = "++Y;Z=Y==0;N=Y<0;";
                } else if (opCode == 232) {
                    str = "++X;Z=X==0;N=X<0;";
                } else if (opCode == 24) {
                    str = "C=false;";
                } else if (opCode == 56) {
                    str = "C=true;";
                } else if (opCode == 88) {
                    str = "/* not implemented */;I=true;";
                } else if (opCode == 120) {
                    str = "/* not implemented */;I=false;";
                } else if (opCode == 152) {
                    str = "A=Y;Z=A==0;N=A<0;";
                } else if (opCode == 184) {
                    str = "V=false;";
                } else if (opCode == 216) {
                    str = "/* not implemented */;D=false;";
                } else if (opCode == 248) {
                    str = "/* not implemented */;D=true;";
                } else if (opCode == 138) {
                    str = "A=X;Z=A==0;N=A<0;";
                } else if (opCode == 154) {
                    str = "SP=X;";
                } else if (opCode == 170) {
                    str = "X=A;Z=X==0;N=X<0;";
                } else if (opCode == 186) {
                    str = "X=SP;Z=X==0;N=X<0;";
                } else if (opCode == 202) {
                    str = "--X;Z=X==0;N=X<0;";
                } else if (opCode == 234) {
                    str = ";";
                } else if (cc == 1) {
                    if (bbb == 0) {
                        mode = 0;
                    }
                    if (bbb == 1) {
                        mode = 1;
                    }
                    if (bbb == 2) {
                        mode = 2;
                    }
                    if (bbb == 3) {
                        mode = 3;
                    }
                    if (bbb == 4) {
                        mode = 4;
                    }
                    if (bbb == 5) {
                        mode = 5;
                    }
                    if (bbb == 6) {
                        mode = 6;
                    }
                    if (bbb == 7) {
                        mode = 7;
                    }
                    if (aaa == 0) {
                        str = "A|=M; Z=A==0; N=A<0;";
                    }
                    if (aaa == 1) {
                        str = "A&=M; Z=A==0; N=A<0;";
                    }
                    if (aaa == 2) {
                        str = "A^=M; Z=A==0; N=A<0;";
                    }
                    if (aaa == 3) {
                        str = "int v=(A&0xff)+(M&0xff); if(C)v++; A=(byte)v; Z=A==0; N=A<0; C=v>255;";
                    }
                    if (aaa == 4) {
                        str = "M=A;";
                    }
                    if (aaa == 5) {
                        str = "A=M; Z=A==0; N=A<0;";
                    }
                    if (aaa == 6) {
                        str = "byte v=M; C=((A&0xff)>=(v&0xff)); Z=A==v; N=A-v<0;";
                    }
                    if (aaa == 7) {
                        str = "int v=(A&0xff)-(M&0xff); if(!C)v--; A=(byte)v; Z=A==0; N=A<0; C=v>=0;";
                    }
                } else if (cc == 2) {
                    if (bbb == 0) {
                        mode = 2;
                    }
                    if (bbb == 1) {
                        mode = 1;
                    }
                    if (bbb == 2) {
                        mode = 9;
                    }
                    if (bbb == 3) {
                        mode = 3;
                    }
                    if (bbb == 5) {
                        mode = 5;
                    }
                    if (bbb == 7) {
                        mode = 7;
                    }
                    if (bbb == 5 && (aaa == 4 || aaa == 5)) {
                        mode = 10;
                    }
                    if (bbb == 7 && (aaa == 4 || aaa == 5)) {
                        mode = 6;
                    }
                    if (aaa == 0) {
                        str = "int oval=M;C=oval<0;int v=(oval&0xff)<<1;M=(byte)v;Z=A==0;N=(byte)v<0;";
                    }
                    if (aaa == 1) {
                        str = "int oval=M;boolean oc=C;C=oval<0;int v=((oval&0xff)<<1)+(oc?1:0);M=(byte)v;Z=A==0;N=(byte)v<0;";
                    }
                    if (aaa == 2) {
                        str = "int oval=M;C=(oval&1)==1;int v=(oval&0xff)>>1;M=(byte)v;Z=A==0;N=(byte)v<0;";
                    }
                    if (aaa == 3) {
                        str = "int oval=M;boolean oc=C;C=(oval&1)==1;int v=((oval&0xff)>>1)+(oc?128:0);M=(byte)v;Z=A==0;N=(byte)v<0;";
                    }
                    if (aaa == 4) {
                        str = "M=X;";
                    }
                    if (aaa == 5) {
                        str = "X=M;Z=X==0;N=X<0;";
                    }
                    if (aaa == 6) {
                        str = "byte v=--M;Z=v==0;N=v<0;";
                    }
                    if (aaa == 7) {
                        str = "byte v=++M;Z=v==0;N=v<0;";
                    }
                } else if (cc == 0) {
                    if (bbb == 4) {
                        mode = 11;
                        int xx = aaa >> 1 & 3;
                        boolean cmp = (aaa & 1) == 1;
                        String pre = "int t=M; ";
                        String post = "{PC+=t;cycles+=1;}";
                        if (cmp) {
                            if (xx == 0) {
                                str = String.valueOf(pre) + "if(N)" + post;
                            }
                            if (xx == 1) {
                                str = String.valueOf(pre) + "if(V)" + post;
                            }
                            if (xx == 2) {
                                str = String.valueOf(pre) + "if(C)" + post;
                            }
                            if (xx == 3) {
                                str = String.valueOf(pre) + "if(Z)" + post;
                            }
                        } else {
                            if (xx == 0) {
                                str = String.valueOf(pre) + "if(!N)" + post;
                            }
                            if (xx == 1) {
                                str = String.valueOf(pre) + "if(!V)" + post;
                            }
                            if (xx == 2) {
                                str = String.valueOf(pre) + "if(!C)" + post;
                            }
                            if (xx == 3) {
                                str = String.valueOf(pre) + "if(!Z)" + post;
                            }
                        }
                    } else {
                        if (bbb == 0) {
                            mode = 2;
                        }
                        if (bbb == 1) {
                            mode = 1;
                        }
                        if (bbb == 3) {
                            mode = 3;
                        }
                        if (bbb == 5) {
                            mode = 5;
                        }
                        if (bbb == 7) {
                            mode = 7;
                        }
                        if (bbb == 5 && (aaa == 4 || aaa == 5)) {
                            mode = 10;
                        }
                        if (bbb == 7 && (aaa == 4 || aaa == 5)) {
                            mode = 6;
                        }
                        if (aaa == 1) {
                            str = "int tmp=M&0xff; Z=(A&tmp)==0; V=(tmp&64)>0; N=(tmp&128)>0;";
                        }
                        if (aaa == 2) {
                            str = "PC=(char)((nextByte()&0xff)|((nextByte()&0xff)<<8));";
                        }
                        if (aaa == 3) {
                            str = "int pos=ram[nextByte()&0xff]&0xff; PC=(char)((ram[pos]&0xff)|((ram[pos+1]&0xff)<<8));";
                        }
                        if (aaa == 4) {
                            str = "M=Y;";
                        }
                        if (aaa == 5) {
                            str = "Y=M;Z=Y==0;N=Y<0;";
                        }
                        if (aaa == 6) {
                            str = "byte v=M; C=((Y&0xff)>=(v&0xff)); Z=Y==v; N=Y-v<0;";
                        }
                        if (aaa == 7) {
                            str = "byte v=M; C=((X&0xff)>=(v&0xff)); Z=X==v; N=X-v<0;";
                        }
                    }
                }
                if (str.length() > 0) {
                    this.pw.print("      case 0x" + Integer.toHexString(opCode) + ":{");
                    this.pw.print("cycles+=" + this.cycles[opCode] + ";");
                    String accText = this.get(mode);
                    int pp = accText.lastIndexOf(";");
                    if (pp >= 0) {
                        str = String.valueOf(accText.substring(0, pp)) + "; " + str;
                        accText = accText.substring(pp + 2);
                    }
                    str = str.replaceAll("M", accText);
                    str = str.replaceAll("nextByte\\(\\)", NEXT_BYTE_STR);
                    this.pw.print(str);
                    this.pw.println("break;}");
                }
            }
            ++opCode;
        }
    }

    public String get(int mode) {
        String str = "";
        if (mode == 0) {
            str = INDIRECT_X_STR;
        }
        if (mode == 1) {
            str = ZERO_PAGE_STR;
        }
        if (mode == 2) {
            str = "nextByte()";
        }
        if (mode == 3) {
            str = ABSOLUTE_STR;
        }
        if (mode == 4) {
            str = INDIRECT_Y_STR;
        }
        if (mode == 5) {
            str = ZERO_PAGE_X_STR;
        }
        if (mode == 10) {
            str = ZERO_PAGE_Y_STR;
        }
        if (mode == 6) {
            str = ABSOLUTE_Y_STR;
        }
        if (mode == 7) {
            str = ABSOLUTE_X_STR;
        }
        if (mode == 8) {
            str = INDIRECT_STR;
        }
        if (mode == 9) {
            str = ACCUMULATOR_STR;
        }
        if (mode == 11) {
            str = "nextByte()";
        }
        return str;
    }

    public static void main(String[] args) throws IOException {
        new CpuBuilder_old().build();
    }
}

