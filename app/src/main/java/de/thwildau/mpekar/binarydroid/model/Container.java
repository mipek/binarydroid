package de.thwildau.mpekar.binarydroid.model;

import java.util.List;

/**
 * Describes a binary file format.
 */
public interface Container {
    /**
     * Returns the architecture of the executeable code in this container
     * @return  Architecture
     */
    Architectures getArch();

    /**
     * Returns the entrypoint address
     * @return  Entrypoint address
     */
    long getEntryPoint();

    /**
     * Returns a list of all sections that are specified for this container
     * @return  Section list
     */
    List<Section> getSections();

    /**
     * Returns a list of all symbols that are exported in the container
     * @return  Symbol list
     */
    List<SymbolItem> getSymbols();

    /**
     * Returns the name of the container (for example: ELF, PE and so on...)
     * @return  Container name
     */
    String getName();

    /**
     * Returns the "native" word size (based on the used architecture).
     * For example:
     *  - x86 has a word size of 4 bytes (32 bit)
     *  - AMD64 has a word size of 8 bytes (64 bit)
     * @return  word size
     */
    byte getWordSize();

    /**
     * Describes a section in a executeable.
     */
    class Section {
        /**< Section name */
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