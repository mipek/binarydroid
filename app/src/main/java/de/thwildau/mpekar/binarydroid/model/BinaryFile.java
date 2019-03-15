package de.thwildau.mpekar.binarydroid.model;

/**
 * Encapsulates a binary that is located somewhere on the device
 */
public class BinaryFile {
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

    /**
     * Returns the name of the package this binary is stored in.
     * @return package name
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Sets the name of the package this binary is stored in.
     * @param packageName	Package name
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Returns the architecture of the binary.
     * @return binary architecture (for example arm, arm64, x86, AMD64 and so on..)
     */
    public String getArch() {
        return arch;
    }

    /**
     * Set the architecture of the binary
     * @param arch	Architecture string
     */
    public void setArch(String arch) {
        this.arch = arch;
    }

    /**
     * Returns the name of the binary
     * @return name of the binary
     */
    public String getBinary() {
        return binary;
    }

    /**
     * Set the name of the binary file.
     * @param binary	name of the binary file
     */
    public void setBinary(String binary) {
        this.binary = binary;
    }

    /**
     * Returns the color that is used when displaying this binary.
     * The default implementation assigns colors based on the packageName.
     * This is useful because you can quickly see which binaries belong together..
     * @return package name
     */
    public int getColor() {
        return color;
    }

    /**
     * Build absolute path to the binary file this object represents
     * @return absolute path to binary file
     */
    public String buildPath() {
        // TODO: update this when we support "app directories" other than /data/app
        return String.format("/data/app/%s/lib/%s/%s",
                getPackageName(), getArch(), getBinary());
    }
}
