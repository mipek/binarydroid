package de.thwildau.mpekar.binarydroid.model;

import java.io.Serializable;

/**
 * Encapsulates a binary that is located somewhere on the device
 */
public class BinaryFile implements Serializable {
    private String packageName;
    private String arch;
    private String binary;
    private final int color;

    public BinaryFile(String packageName, String arch, String binary, int color) {
        this.packageName = packageName;
        this.arch = arch;
        this.binary = binary;
        this.color = color;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    public String getBinary() {
        return binary;
    }

    public void setBinary(String binary) {
        this.binary = binary;
    }

    public int getColor() {
        return color;
    }

    /**
     * Build absolute path to the binary file this object represents
     * @return
     */
    public String buildPath() {
        // TODO: update this when we support "app directories" other than /data/app
        return String.format("/data/app/%s/lib/%s/%s",
                getPackageName(), getArch(), getBinary());
    }
}
