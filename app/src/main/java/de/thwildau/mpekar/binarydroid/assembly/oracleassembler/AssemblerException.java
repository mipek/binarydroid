package de.thwildau.mpekar.binarydroid.assembly.oracleassembler;

public class AssemblerException extends Throwable {
    private String msg;
    public AssemblerException(String s) {
        this.msg = s;
    }

    public String getMessage() {
        return msg;
    }

    @Override
    public String toString() {
        return "AssemblerException{" +
                "msg='" + msg + '\'' +
                '}';
    }
}
