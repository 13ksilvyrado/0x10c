package computer;

import computer.DCPU;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class Assembler {
    public char[] ram;
    public int pc = 0;
    public Map<Position, String> labelUsages = new HashMap<Position, String>();
    public static final List<String> twoAddrOpCodes = Arrays.asList("SET", "ADD", "SUB", "MUL", "DIV", "MOD", "SHL", "SHR", "AND", "BOR", "XOR", "IFE", "IFN", "IFG", "IFB");
    public static final List<String> oneAddrOpCodes = Arrays.asList("JMP", "JSR");
    public static final List<String> zeroAddrOpCodes = Arrays.asList("RET");
    public String opcodes = "ABCXYZIJ";
    private Map<String, Macro> macros = new HashMap<String, Macro>();
    private Macro currentMacro;
    private Scope currentScope = new Scope(null);

    public Assembler(char[] ram) {
        this.ram = ram;
    }

    private int decodeAddr(String string) {
        if (string.toUpperCase().equals("POP")) {
            return 24;
        }
        if (string.toUpperCase().equals("PEEK")) {
            return 25;
        }
        if (string.toUpperCase().equals("PUSH")) {
            return 26;
        }
        if (string.toUpperCase().equals("SP")) {
            return 27;
        }
        if (string.toUpperCase().equals("PC")) {
            return 28;
        }
        if (string.toUpperCase().equals("O")) {
            return 29;
        }
        if (string.length() == 1 && this.opcodes.indexOf(string.toUpperCase()) >= 0) {
            return 0 + this.opcodes.indexOf(string.toUpperCase());
        }
        if (string.length() == 3 && string.startsWith("[") && string.endsWith("]") && this.opcodes.indexOf(string.toUpperCase().substring(1, 2)) >= 0) {
            return 8 + this.opcodes.indexOf(string.toUpperCase().substring(1, 2));
        }
        if (Character.isDigit(string.charAt(0))) {
            int val = this.parseNumber(string);
            if (val < 32) {
                return 32 + val;
            }
            this.ram[this.pc++] = (char)val;
            return 31;
        }
        if (string.startsWith("[") && string.endsWith("]") && Character.isDigit(string.charAt(1))) {
            if ((string = string.substring(1, string.length() - 1)).indexOf("+") >= 0) {
                String[] tokens = string.split("\\+");
                int val = this.parseNumber(tokens[0]);
                this.ram[this.pc++] = (char)val;
                if (this.opcodes.indexOf(tokens[1].toUpperCase()) < 0) {
                    throw new IllegalArgumentException("Must be a register!");
                }
                return 16 + this.opcodes.indexOf(tokens[1].toUpperCase());
            }
            int val = this.parseNumber(string);
            this.ram[this.pc++] = (char)val;
            return 30;
        }
        if (string.startsWith("[") && string.endsWith("]")) {
            if ((string = string.substring(1, string.length() - 1)).indexOf("+") >= 0) {
                String[] tokens = string.split("\\+");
                this.labelUsages.put(new Position(this.currentScope, this.pc), tokens[0]);
                this.ram[this.pc++] = 48879;
                if (this.opcodes.indexOf(tokens[1].toUpperCase()) < 0) {
                    throw new IllegalArgumentException("Must be a register!");
                }
                return 16 + this.opcodes.indexOf(tokens[1].toUpperCase());
            }
            this.labelUsages.put(new Position(this.currentScope, this.pc), string);
            this.ram[this.pc++] = 48879;
            return 30;
        }
        this.labelUsages.put(new Position(this.currentScope, this.pc), string);
        this.ram[this.pc++] = 48879;
        return 31;
    }

    private int parseNumber(String string) {
        int val = 0;
        val = string.startsWith("0x") ? Integer.parseInt(string.substring(2), 16) : (string.startsWith("0b") ? Integer.parseInt(string.substring(2), 2) : Integer.parseInt(string));
        return val &= 0xFFFF;
    }

    private void parseData(String string) {
        if (Character.isDigit(string.charAt(0))) {
            this.ram[this.pc++] = (char)this.parseNumber(string);
        } else if (string.startsWith("\"")) {
            string = string.substring(1, string.length() - 1);
            int i = 0;
            while (i < string.length()) {
                this.ram[this.pc++] = string.charAt(i);
                ++i;
            }
        } else {
            this.labelUsages.put(new Position(this.currentScope, this.pc), string);
            this.ram[this.pc++] = 48879;
        }
    }

    private void parseLine(String line) {
        if (line.length() == 0) {
            return;
        }
        String[] words = line.split("\\\"");
        line = "";
        int i = 0;
        while (i < words.length) {
            if (i % 2 == 0) {
                words[i] = words[i].replaceAll("\\{", " \\{ ");
            }
            if (i % 2 == 0) {
                words[i] = words[i].replaceAll("\\}", " \\} ");
            }
            line = String.valueOf(line) + words[i];
            if (i < words.length - 1) {
                line = String.valueOf(line) + "\"";
            }
            ++i;
        }
        ArrayList<String> tokenList = new ArrayList<String>();
        String delims = " \t,";
        StringTokenizer st = new StringTokenizer(line, String.valueOf(delims) + "\"", true);
        while (st.hasMoreTokens()) {
            String token = st.nextToken(String.valueOf(delims) + "\"");
            if (token.equalsIgnoreCase("\"")) {
                token = "\"" + st.nextToken("\"") + "\"";
                if (st.hasMoreTokens()) {
                    st.nextToken("\"");
                }
            }
            if (token.length() <= 1 && delims.indexOf(token) >= 0) continue;
            tokenList.add(token);
        }
        String[] tokens = tokenList.toArray(new String[0]);
        this.handleTokens(tokens);
    }

    public void handleTokens(String[] tokens) {
        int i = 0;
        while (i < tokens.length) {
            if (tokens[i].startsWith(";")) {
                return;
            }
            if (this.currentMacro != null) {
                this.currentMacro.tokens.add(tokens[i]);
                if (tokens[i].equals("}")) {
                    this.currentMacro = null;
                }
            } else if (tokens[i].startsWith(":")) {
                this.currentScope.labelPositions.put(tokens[i].substring(1), new Position(this.currentScope, this.pc));
            } else if (tokens[i].equalsIgnoreCase("#macro")) {
                String title = "";
                while (!tokens[i + 1].equals("{")) {
                    String t = tokens[++i];
                    if (title.length() > 0) {
                        title = String.valueOf(title) + ",";
                    }
                    title = String.valueOf(title) + t;
                }
                String[] parts = title.split("[(,) \t]+");
                this.currentMacro = new Macro();
                int j = 1;
                while (j < parts.length) {
                    this.currentMacro.params.add(parts[j]);
                    ++j;
                }
                this.macros.put(String.valueOf(parts[0]) + "(" + parts.length, this.currentMacro);
            } else if (tokens[i].equalsIgnoreCase("DAT")) {
                while (++i < tokens.length) {
                    if (tokens[i].startsWith(";")) {
                        return;
                    }
                    this.parseData(tokens[i]);
                    ++i;
                }
            } else if (twoAddrOpCodes.contains(tokens[i].toUpperCase())) {
                int op = this.pc;
                this.ram[this.pc++] = '\u0000';
                int opCode = twoAddrOpCodes.indexOf(tokens[i].toUpperCase()) + 1;
                int addr1 = this.decodeAddr(tokens[++i]);
                int addr2 = this.decodeAddr(tokens[++i]);
                if (addr1 >= 31 && opCode < 12) {
                    throw new IllegalArgumentException("Can't assign a literal value!");
                }
                this.ram[op] = (char)(opCode | addr1 << 4 | addr2 << 10);
            } else if (oneAddrOpCodes.contains(tokens[i].toUpperCase())) {
                int op = this.pc;
                this.ram[this.pc++] = '\u0000';
                int opCode = oneAddrOpCodes.indexOf(tokens[i].toUpperCase()) + 1;
                int addr = this.decodeAddr(tokens[++i]);
                this.ram[op] = (char)(opCode << 4 | addr << 10);
            } else if (zeroAddrOpCodes.contains(tokens[i].toUpperCase())) {
                this.ram[this.pc++] = (char)(zeroAddrOpCodes.indexOf(tokens[i].toUpperCase()) + 1 << 8);
            } else if (tokens[i].equals("{")) {
                this.currentScope = new Scope(this.currentScope);
            } else if (tokens[i].equals("}")) {
                this.currentScope = this.currentScope.parent;
            } else if (tokens[i].contains("(")) {
                String title = "";
                do {
                    String t = tokens[i];
                    if (title.length() > 0) {
                        title = String.valueOf(title) + ",";
                    }
                    title = String.valueOf(title) + t;
                } while (!tokens[i++].endsWith(")"));
                --i;
                String[] parts = title.split("[(,) \t]+");
                this.macros.get(String.valueOf(parts[0]) + "(" + parts.length).insert(parts);
            } else {
                throw new IllegalArgumentException("Bad token " + tokens[i]);
            }
            ++i;
        }
    }

    public void include(String file) throws Exception {
        Scope oldScope = this.currentScope;
        this.currentScope = new Scope(oldScope);
        oldScope.inheritedScopes.add(this.currentScope);
        String fileName = file;
        BufferedReader br = new BufferedReader(new InputStreamReader(Assembler.class.getResourceAsStream("/" + file)));
        String line = "";
        int lines = 0;
        while ((line = br.readLine()) != null) {
            ++lines;
            if ((line = line.trim()).startsWith("#include ")) {
                try {
                    this.include(line.substring("#include ".length()));
                }
                catch (Exception e) {
                    System.out.println("[" + fileName + ":" + lines + "] Failed to include file: " + line.trim());
                    e.printStackTrace();
                }
                continue;
            }
            try {
                this.parseLine(line);
            }
            catch (Exception e) {
                System.out.println("[" + fileName + ":" + lines + "] Failed to parse line: " + line.trim());
                e.printStackTrace();
            }
        }
        br.close();
        this.currentScope = oldScope;
    }

    public void assemble(String file) throws Exception {
        this.include(file);
        for (Position pos : this.labelUsages.keySet()) {
            String label = this.labelUsages.get(pos);
            if (label.startsWith("PC+")) {
                int toSkip = Integer.parseInt(label.substring(3));
                int pp = pos.pos - 1;
                int i = 0;
                while (i <= toSkip) {
                    pp += DCPU.getInstructionLength(this.ram[pp]);
                    ++i;
                }
                this.ram[pos.pos] = (char)pp;
                continue;
            }
            Position labelPos = pos.scope.findLabel(label);
            if (labelPos == null) {
                throw new IllegalArgumentException("Undefined label " + label);
            }
            this.ram[pos.pos] = (char)labelPos.pos;
        }
    }

    private class Macro {
        public List<String> tokens = new ArrayList<String>();
        public List<String> params = new ArrayList<String>();

        private Macro() {
        }

        public void insert(String[] values) {
            String[] ts = new String[this.tokens.size()];
            int i = 0;
            while (i < ts.length) {
                String token = this.tokens.get(i);
                int p = this.params.indexOf(token);
                ts[i] = p >= 0 ? values[p + 1] : token;
                ++i;
            }
            Assembler.this.handleTokens(ts);
        }
    }

    private class Position {
        public final Scope scope;
        public final int pos;

        public Position(Scope scope, int pos) {
            this.scope = scope;
            this.pos = pos;
        }
    }

    private class Scope {
        public Map<String, Position> labelPositions = new HashMap<String, Position>();
        public Scope parent;
        public List<Scope> inheritedScopes = new ArrayList<Scope>();

        public Scope(Scope parent) {
            this.parent = parent;
        }

        public Position findLabel(String label) {
            return this.findLabel(label, new ArrayList<Scope>());
        }

        public Position findLabel(String label, List<Scope> testedScopes) {
            if (testedScopes.contains(this)) {
                return null;
            }
            testedScopes.add(this);
            if (this.labelPositions.containsKey(label)) {
                return this.labelPositions.get(label);
            }
            int i = 0;
            while (i < this.inheritedScopes.size()) {
                Position p = this.inheritedScopes.get(i).findLabel(label, testedScopes);
                if (p != null) {
                    return p;
                }
                ++i;
            }
            if (this.parent != null) {
                return this.parent.findLabel(label, testedScopes);
            }
            return null;
        }
    }
}

