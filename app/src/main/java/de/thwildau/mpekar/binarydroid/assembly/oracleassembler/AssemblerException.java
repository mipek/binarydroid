package de.thwildau.mpekar.binarydroid.assembly.oracleassembler;

public class AssemblerException extends Throwable {

    private AssemblerError error;
    private String x;
    private String y;

    public AssemblerException(AssemblerError error) {
        this.error = error;
    }
    public AssemblerException(AssemblerError error, String x) {
        this.error = error;
        this.x = x;
    }
    public AssemblerException(AssemblerError error, String x, String y) {
        this.error = error;
        this.x = x;
        this.y = y;
    }

    public AssemblerError getErrorType() {
        return error;
    }

    public String getX() {
        return x;
    }

    public String getY() {
        return y;
    }

    @Override
    public String toString() {
        return "AssemblerException{" +
                "error='" + getErrorType() + '\'' +
                '}';
    }
}
