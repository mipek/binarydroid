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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Section section = (Section) o;

            if (va != section.va) return false;
            if (raw != section.raw) return false;
            if (size != section.size) return false;
            return name != null ? name.equals(section.name) : section.name == null;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (int) (va ^ (va >>> 32));
            result = 31 * result + (int) (raw ^ (raw >>> 32));
            result = 31 * result + (int) (size ^ (size >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return name + " " + Long.toHexString(va) + "-" + Long.toHexString(va+size);
        }
    }
}