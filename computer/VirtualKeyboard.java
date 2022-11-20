package computer;

public class VirtualKeyboard {
    private final char[] ram;
    private final int offset;
    private int pp = 0;

    public VirtualKeyboard(char[] ram, int offset) {
        this.ram = ram;
        this.offset = offset;
    }

    public void keyTyped(int i) {
        if (i == 0) {
            return;
        }
        System.out.println("Type " + (char)i + "/" + i);
        if (this.ram[this.offset + this.pp] != '\u0000') {
            return;
        }
        this.ram[this.offset + this.pp] = (char)i;
        this.pp = this.pp + 1 & 0xF;
    }
}

