package de.thwildau.mpekar.binarydroid.assembly.oracleassembler;

import android.util.Log;

import java.util.List;
import java.util.Random;

import de.thwildau.mpekar.binarydroid.assembly.Assembler;
import de.thwildau.mpekar.binarydroid.assembly.Disassembler;

/**
 * Implements a "oracle assembler" (assembler that is based on a disassembler).
 * Based on:
 *  - https://binary.ninja/blog/images/jailbreak2018.pdf
 *  - https://github.com/Vector35/generate_assembler
 */
public class AssemblerOracle implements Assembler {
    private static final int INSTR_SIZE = 4;
    private static final int N_OFFSPRING  = 1;
    private static final int N_BITS_FLIP  = 3;
    private static final int FAILURES_LIMIT = 4000;
    private Disassembler disasm;
    private Tokenizer t;

    public AssemblerOracle(Disassembler disassembler) {
        this.disasm = disassembler;
        this.t = new Tokenizer();
    }

    @Override
    public byte [] assembleSingle(String assembly, long addr) throws AssemblerException {
        assembly = assembly.toLowerCase();

        // tokenize our input
        List<Tokenizer.Token> tokens = t.tokenize(assembly);

        // form syntax
        String syntax = t.tokensToSyntax(tokens);

        // look it up
        LookupTable.TableEntry info = LookupTable.getInstance().lookup(syntax);
        if (info == null) {
            throw new AssemblerException(AssemblerError.LookupFailed, syntax);
        }

        // start with the parent
        int parent = Integer.reverseBytes(info.seed);
        float init_score, top_score;
        init_score = top_score = score(tokens, parent, addr);

        // cache the xor masks
        int vary_mask = info.mask;
        int n_flips = 0;
        int[] flipper_idx = new int[32];
        long[] flipper = new long[32];
        for (int i=0; i<32; ++i) {
            if ((vary_mask & (1 << i)) != 0) {
                flipper_idx[n_flips] = i;
                flipper[n_flips++] = 1<<i;
            }
        }

        int failures = 0;
        int failstreak = 0;

        // vary the parent
        int b1i=0;
        while (true) {
            // winner?
            if (top_score > 99.99f) {
                return intToByteArray(parent);
            }

            boolean overtake = false;

            for (; ; b1i = (b1i+1) % n_flips) {
                int child = (int)(parent ^ flipper[b1i]);
                child = hookMiddle(info.seed, child, flipper_idx[b1i]);

                float s = score(tokens, child, addr);
                if (s > top_score) {
                    Log.d("BinaryDroid", "score improved to: " + s +
                            " with child=" + Integer.toHexString(child));
                    parent = child;
                    top_score = s;
                    overtake = true;
                    b1i = (b1i+1) % n_flips;
                    break;
                }
                
                failures++;
                if (failures > FAILURES_LIMIT) {
                    throw new AssemblerException(AssemblerError.FailureLimitReached);
                }

                failstreak++;
                if (failstreak >= n_flips) {
                    // generate a new parent that's at leat as good as the seed
                    Random rand = new Random();
                    while(true) {
                        parent = info.seed;
                        for (int i=0; i<n_flips; ++i) {
                            if ((rand.nextInt() % 2) == 0) {
                                parent ^= flipper[i];
                                parent = hookMiddle(info.seed, parent, flipper_idx[i]);
                            }
                        }

                        top_score = score(tokens, parent, addr);

                        if (top_score >= init_score) {
                            // reseed the parent
                            break;
                            /*Disassembler.Instruction[] insns = disasm.disassemble(
                                    intToByteArray(parent), addr, INSTR_SIZE);
                            if (insns.length != 1) {
                                throw new AssemblerException("re-seed disassembler error");
                            }
                            Disassembler.Instruction insn = insns[0];*/
                        } else {
                            // reseed fail
                            failures++;
                        }

                        if (failures > FAILURES_LIMIT) {
                            throw new AssemblerException(AssemblerError.CannotAssemble);
                        }
                    }
                }

                if (overtake) {
                    failstreak = 0;
                }
            }
        }
    }

    // SECOND shot at manipulating the assembling process
    private int hookMiddle(int seed, int insword, int bit) {
        return insword;
    }

    // Last shot at manipulating the assembling process.
    // Fill in anything missing, like OFFS
    private long hookLast(long seed, long insword, List<Tokenizer.Token> tokens) {
        return insword;
    }

    private float score(List<Tokenizer.Token> baseline, int newcomer, long addr) throws AssemblerException {

        byte [] bytes = intToByteArray(newcomer);
        Disassembler.Instruction[] insns = disasm.disassemble(bytes, addr, INSTR_SIZE);
        if (insns.length != 1) {
            return 0;
            //throw new AssemblerException("disassembler error");
        }
        Disassembler.Instruction insn = insns[0];

        // compare mnemonics before doing more work
        String mnem = baseline.get(0).sval;
        if (!mnem.equals(insn.mnemonic())) {
            return 0;
        }

        // mnemonics are the same, tokenize now
        String line = insn.toString();
        List<Tokenizer.Token> newTokens = t.tokenize(line);

        return fitness(baseline, newTokens);
    }

    // Compares two token lists and returns how good they match
    private float fitness(List<Tokenizer.Token> dst, List<Tokenizer.Token> src) {
        int n = dst.size();

        // same number of tokens?
        if (n != src.size())
            return 0;

        float score = 0;
        float scorePerToken = 100.0f / n;

        Log.d("BinaryDroid", "fitness(): \"" +
                        t.tokensToSyntax(dst) + "\" vs \"" + t.tokensToSyntax(src) + "\"");

        // for each token
        for (int i=0; i<n; ++i) {
            Tokenizer.Token srcToken = src.get(i);
            Tokenizer.Token dstToken = dst.get(i);

            if (srcToken.type != dstToken.type) {
                return 0;
            }

            switch (srcToken.type) {
                case GPR:
                case QREG:
                case DREG:
                case PREG:
                case CREG:
                case SREG:
                case NUM:
                case STATR:
                case RLIST:
                {
                    float hamming_similarity = hammingSimilar32(srcToken.ival, dstToken.ival);
                    score += hamming_similarity * scorePerToken;
                    break;
                }
                /* opcodes, suffixes, and punctuation must string match */
                case SHIFT: /* "ror", "asr", "rrx", etc. */
                case OpCode:
                case PUNC:
                    if(srcToken.sval.equals(dstToken.sval))
                        score += scorePerToken;
                    break;
                default:
            }
        }

        return score;
    }

    private static long countBits32(long x) {
        x = x - ((x >> 1) & 0x55555555);
        x = (x & 0x33333333) + ((x >> 2) & 0x33333333);
        return ((((x + (x >> 4)) & 0x0F0F0F0F) * 0x01010101) >> 24);
    }

    private static float hammingSimilar32(long a, long b) {
        return (32-countBits32(a ^ b)) / 32.0f;
    }

    // https://stackoverflow.com/a/2183259
    private static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }
}
