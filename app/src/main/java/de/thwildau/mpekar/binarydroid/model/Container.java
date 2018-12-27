package de.thwildau.mpekar.binarydroid.model;

import java.util.List;

public interface Container {
    Architectures getArch();
    long getEntryPoint();
    List<Section> getSections();
    List<SymbolItem> getSymbols();
    String getName();
    byte getWordSize();

    class Section {
        public String name;
        /**< Virtual address */
        public long va;
        /**< Raw address */
        public long raw;
        /**< Size of section in nbytes */
        public long size;
    }
}