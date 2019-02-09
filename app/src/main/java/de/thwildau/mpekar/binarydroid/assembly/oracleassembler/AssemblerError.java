package de.thwildau.mpekar.binarydroid.assembly.oracleassembler;

/**
 * Enumerates all possible assembler error types
 */
public enum AssemblerError {
    LookupFailed, // "invalid syntax, tried to lookup: X"
    FailureLimitReached, // "failure limit reached"
    CannotAssemble, //"cannot assemble, valid operands?"
    UnterminatedRegisterList, //"unterminated register list"
    UnexpectedChar, // "unexpected character at: X (original input: Y)
    UnrecognizedToken, // "unrecognized token: X (original input: Y)
    InvalidRegister // invalid register:  + reg
}
