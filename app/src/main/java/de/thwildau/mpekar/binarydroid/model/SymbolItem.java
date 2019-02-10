package de.thwildau.mpekar.binarydroid.model;

/**
 * Describes a single symbol.
 */
public class SymbolItem {
    /**< Symbol identifier */
    public final String name;
    /**< Symbol address */
    public final long addr;

    public SymbolItem(String name, long addr) {
        this.name = name;
        this.addr = addr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SymbolItem that = (SymbolItem) o;

        if (addr != that.addr) return false;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (int) (addr ^ (addr >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return name + " @ " + Long.toHexString(addr);
    }
}
