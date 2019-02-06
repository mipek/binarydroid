package de.thwildau.mpekar.binarydroid.model;

import net.fornwall.jelf.ElfFile;
import net.fornwall.jelf.ElfSection;
import net.fornwall.jelf.ElfSymbol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContainerELF implements Container {
    private ElfFile elfFile;
    private List<Section> sections = null;

    public ContainerELF(ElfFile file) {
        elfFile = file;
    }

    @Override
    public Architectures getArch() {
        switch (elfFile.arch) {
            case ElfFile.ARCH_ARM:
                return Architectures.ARM;
            case ElfFile.ARCH_AARCH64:
                return Architectures.ARM64;
            case ElfFile.ARCH_i386:
                return Architectures.X86;
            case ElfFile.ARCH_X86_64:
                return Architectures.AMD64;
            default:
                return Architectures.Unknown;
        }
    }

    @Override
    public long getEntryPoint() {
        return elfFile.entry_point;
    }

    @Override
    public List<Section> getSections() {
        if (sections == null) {
            sections = new ArrayList<>(elfFile.num_sh);
            try {
                for (int i = 0; i < elfFile.num_sh; i++) {
                    ElfSection sh = elfFile.getSection(i);

                    Section s = new Section();
                    s.name = sh.getName();
                    s.va = sh.address;
                    s.raw = sh.section_offset;
                    s.size = sh.size;
                    sections.add(s);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sections;
    }

    @Override
    public List<SymbolItem> getSymbols() {
        try {
            ElfSection section = elfFile.getDynamicSymbolTableSection();
            if (section == null) return new ArrayList<>();

            int symbolCount = section.getNumberOfSymbols();
            List<SymbolItem> symbols = new ArrayList<>(symbolCount);
            for (int i=0; i<symbolCount; ++i) {
                ElfSymbol elfSymbol = section.getELFSymbol(i);
                final String name = elfSymbol.getName();

                // no point in adding empty symbols or symbols that do not point anywhere
                if (name != null && name.length() > 0 && elfSymbol.value > 0) {
                    symbols.add(new SymbolItem(name, elfSymbol.value));
                }
            }

            return symbols;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getName() {
        return "ELF";
    }

    @Override
    public byte getWordSize() { //"native" word size
        switch (getArch()) {
            case ARM64:
            case AMD64:
                return 8;
            case ARM:
            case X86:
                return 4;
            default:
                throw new RuntimeException("unknown arch");
        }
    }
}
