package de.thwildau.mpekar.binarydroid.assembly.oracleassembler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tokenize assembly.
 * Based on: https://github.com/Vector35/generate_assembler/blob/master/arm/common.cpp
 */
class Tokenizer {
    enum TokenType {
        OpCode,
        GPR,
        QREG,
        DREG,
        PREG,
        CREG,
        SREG,
        IRQ,
        SHIFT,
        RLIST,
        NUM,
        OPT,
        STATR,
        PUNC,
    }

    static class Token {
        TokenType type;
        long ival;
        String sval;

        public Token(TokenType type, long ival, String sval) {
            this.type = type;
            this.ival = ival;
            this.sval = sval;
        }
    }

    private Map<String, Integer> alias2gpr;
    private Map<String, Integer> irqs;
    private Map<String, Integer> shifts;
    Tokenizer() {
        alias2gpr = new HashMap<>(7);
        alias2gpr.put("sb", 9);
        alias2gpr.put("sl", 10);
        alias2gpr.put("fp", 11);
        alias2gpr.put("ip", 12);
        alias2gpr.put("sp", 13);
        alias2gpr.put("lr", 14);
        alias2gpr.put("pc", 15);

        irqs = new HashMap<>(8);
        irqs.put("none", 0);
        irqs.put("a", 1);
        irqs.put("i", 2);
        irqs.put("f", 4);
        irqs.put("ai", 3);
        irqs.put("af", 5);
        irqs.put("if", 6);
        irqs.put("aif", 7);

        shifts = new HashMap<>(5);
        shifts.put("lsl", 1);
        shifts.put("lsr", 1);
        shifts.put("asr", 1);
        shifts.put("rrx", 1);
        shifts.put("ror", 1);
    }

    /**
     * Tokenize assembly code
     * @param assembly  Single line assembly
     * @return          List of tokens
     */
    List<Token> tokenize(String assembly) throws AssemblerException {
        List<Token> tokens = new ArrayList<>();
        List<String> pretoks = new ArrayList<>();

        // opcode
        int endpos;
        for (endpos=0; endpos<assembly.length(); ++endpos) {
            char ch = assembly.charAt(endpos);
            if (ch != '.' && !Character.isLetterOrDigit(ch))
                break;
        }

        if (endpos > 0) {
            String opcode = assembly.substring(0, endpos);
            pretoks.add(opcode);
        }

        for (int i=endpos; i<assembly.length();) {
            int start = i;
            char ch = assembly.charAt(i);
            char nextch = (i+1<assembly.length())?assembly.charAt(i+1):'\0';

            // stretches of letters/nums
            if (Character.isLetter(ch)) {
                ++i;
                for (endpos=i; endpos<assembly.length(); ++endpos) {
                    char letnum = assembly.charAt(endpos);
                    if (!Character.isLetterOrDigit(letnum) && letnum != '_') {
                        break;
                    }
                }

                pretoks.add(assembly.substring(start, endpos));
                i += (endpos - i);
            }

            // immediates
            else if (ch == '#') {
                ++i;
                for (endpos=i; endpos<assembly.length(); ++endpos) {
                    char letnum = assembly.charAt(endpos);
                    if (!isXDigit(letnum) && letnum != '-' && letnum != '+' &&
                            letnum != 'x' && letnum != 'e' && letnum != '.') {
                        break;
                    }
                }

                pretoks.add(assembly.substring(start, endpos));
                i += (endpos - i);
            }

            // hex literals
            else if (ch =='0' && nextch == 'x') {
                i += 2;
                for (endpos=i; endpos<assembly.length(); ++endpos) {
                    char letnum = assembly.charAt(endpos);
                    if (!isXDigit(letnum)) {
                        break;
                    }
                }

                pretoks.add(assembly.substring(start, endpos));
                i += (endpos - i);
            }

            // decimal literals
            else if (Character.isDigit(ch)) {
                for (endpos=i; endpos<assembly.length(); ++endpos) {
                    char letnum = assembly.charAt(endpos);
                    if (!Character.isDigit(letnum)) {
                        break;
                    }
                }

                pretoks.add(assembly.substring(start, endpos));
                i += (endpos - i);
            }

            // punctuation
            else if (isPunctuation(ch)) {
                pretoks.add(assembly.substring(start, ++i));
            }

            // register lists {...}
            else if (ch == '{') {
                ++i;
                while(true) {
                    if (i < assembly.length()) {
                        if (assembly.charAt(i) == '}') {
                            break;
                        }
                    } else {
                        throw new AssemblerException("unterminated register list");
                    }
                    ++i;
                }
                ++i;
                pretoks.add(assembly.substring(start, i));
            }

            // discard spaces
            else if (ch == ' ') {
                ++i;
            }

            // otherwise, error
            else {
                throw new AssemblerException("unexpected character at: " + i
                        + " (original input: " + assembly + ")");
            }
        }

        // loop over the rest
        for (int i=0; i<pretoks.size(); ++i) {
            String tok = pretoks.get(i);
            char ch = tok.charAt(0);
            char nextch = (1<tok.length())?tok.charAt(1):'\0';

            if (i == 0) {
                // TODO: cleanup opcode?
                tokens.add(new Token(TokenType.OpCode, 0, tok));
            } else if (isPunctuation(ch) && tok.length() == 1) {
                tokens.add(new Token(TokenType.PUNC, 0, tok));
            } else if ((ch == 'a' || ch == 'c' || ch == 's') &&
                    tok.length() >= 4 &&
                    tok.substring(1, 4).equals("psr_")) {
                long val = 0;
                for (int j = 5; j<tok.length(); ++j) {
                    char jchar = tok.charAt(j);
                    if (jchar == 'n') val |= 8;
                    else if (jchar == 'z') val |= 4;
                    else if (jchar == 'c') val |= 2;
                    else if (jchar == 'v') val |= 1;
                }
                tokens.add(new Token(TokenType.STATR, val, ""));
            } else if (ch == '{') {
                String regList = tok.substring(i+1);
                long val = 0;
                for (int j = 0; j<regList.length(); ++j) {
                    String p = regList.substring(j);
                    val |= (1 << Reg2Num(p));

                    int newpos;
                    for (newpos = j; newpos<p.length(); ++newpos) {
                        char jchar = p.charAt(newpos);
                        if (!(jchar==',' || jchar==' ' || jchar=='}'))
                            j++;
                        else
                            break;
                    }
                    for (newpos = j; newpos<p.length(); ++newpos) {
                        char jchar = p.charAt(newpos);
                        if (jchar==',' || jchar==' ' || jchar=='}')
                            j++;
                        else
                            break;
                    }
                }
                tokens.add(new Token(TokenType.RLIST, val, ""));
            } else if (ch == 'r' && Character.isDigit(nextch)) {
                tokens.add(new Token(TokenType.GPR, Long.parseLong(tok.substring(1)), ""));
            } else if (ch == 'q' && Character.isDigit(nextch)) {
                tokens.add(new Token(TokenType.QREG, Long.parseLong(tok.substring(1)), ""));
            } else if (ch == 'd' && Character.isDigit(nextch)) {
                tokens.add(new Token(TokenType.DREG, Long.parseLong(tok.substring(1)), ""));
            } else if (ch == 'c' && Character.isDigit(nextch)) {
                tokens.add(new Token(TokenType.CREG, Long.parseLong(tok.substring(1)), ""));
            } else if (ch == 's' && Character.isDigit(nextch)) {
                tokens.add(new Token(TokenType.SREG, Long.parseLong(tok.substring(1)), ""));
            } else if (ch == '#') {
                if (tok.length() >= 3 && tok.charAt(1) == '0' && tok.charAt(2) == 'x') {
                    tokens.add(new Token(TokenType.NUM, Long.parseLong(tok.substring(3),16), ""));
                } else {
                    tokens.add(new Token(TokenType.NUM, Long.parseLong(tok.substring(1)), ""));
                }
            } else {
                Integer val = alias2gpr.get(tok);
                if (val != null) {
                    tokens.add(new Token(TokenType.GPR, val.longValue(), ""));
                } else {
                    val = irqs.get(tok);
                    if (val != null) {
                        tokens.add(new Token(TokenType.IRQ, val.longValue(), ""));
                    } else {
                        val = shifts.get(tok);
                        if (val != null) {
                            tokens.add(new Token(TokenType.SHIFT, val.longValue(), ""));
                        } else {
                            throw new AssemblerException("unrecognized token: " + tok +
                            " (original string: " + assembly + " )");
                        }
                    }
                }
            }
        }

        return tokens;
    }

    /**
     * Given a set of tokens, generates the resulting assembly
     * @param tokens    list of tokens
     * @return          assembly
     */
    public String tokensToSyntax(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<tokens.size(); ++i) {
            if (i == 1) {
                sb.append("<c> ");
            }

            Token token = tokens.get(i);
            switch (token.type) {
                case PUNC:
                case OpCode:
                    sb.append(token.sval);
                    break;
                default:
                    sb.append(tokenTypeToString(token.type));
                    break;
            }
        }

        return sb.toString();
    }

    private static String tokenTypeToString(TokenType type) {
        switch (type) {
            case GPR: return "GPR";
            case QREG: return "QREG";
            case DREG: return "DREG";
            case PREG: return "PREG";
            case CREG: return "CREG";
            case SREG: return "SREG";
            case IRQ: return "IRQ";
            case SHIFT: return "SHIFT";
            case RLIST: return "RLIST";
            case NUM: return "NUM";
            case OPT: return "OPT";
            case STATR: return "STATR";
            default:
                return "ERR_RESOLVING_TOKEN_TYPE";
        }
    }

    private static int Reg2Num(String reg) throws AssemblerException {
        char ch1 = reg.charAt(0);
        char ch2 = reg.charAt(1);
        if (ch1 == 'r') {
            return Integer.parseInt(reg.substring(1));
        } else if (ch1 == 's' && ch2 == 'b') {
            return 9;
        } else if (ch1 == 's' && ch2 == 'l') {
            return 10;
        } else if (ch1 == 'f' && ch2 == 'p') {
            return 11;
        } else if (ch1 == 'i' && ch2 == 'p') {
            return 12;
        } else if (ch1 == 's' && ch2 == 'p') {
            return 13;
        } else if (ch1 == 'l' && ch2 == 'r') {
            return 14;
        } else if (ch1 == 'p' && ch2 == 'c') {
            return 15;
        } else {
            throw new AssemblerException("invalid register: " + reg);
        }
    }

    private static boolean isXDigit(char code) {
        return  Character.isDigit(code) ||
                (code >= 'a' && code <= 'f') ||
                (code >= 'A' && code <= 'F');
    }

    private static boolean isPunctuation(char x) {
        return (x=='['||x==']'||x=='('||x==')'||x==','||x=='.'||x=='*'
		        ||x=='+'||x=='-'||x=='!'||x=='^'||x==':'||x=='^');
    }
}
