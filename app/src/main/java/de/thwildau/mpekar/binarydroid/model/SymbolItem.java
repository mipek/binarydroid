package de.thwildau.mpekar.binarydroid.model;

public class SymbolItem {
    public final String name;
    public final long addr;

    public SymbolItem(String name, long addr) {
        this.name = name;
        this.addr = addr;
    }

    @Override
    public String toString() {
        return name;
    }
}
