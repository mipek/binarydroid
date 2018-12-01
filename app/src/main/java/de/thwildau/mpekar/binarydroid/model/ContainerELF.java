package de.thwildau.mpekar.binarydroid.model;

import net.fornwall.jelf.ElfFile;
import net.fornwall.jelf.ElfSection;

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
            List<Section> sections = new ArrayList<>();
            try {
                for (int i = 0; i < elfFile.num_sh; i++) {
                    ElfSection sh = elfFile.getSection(i);

                    Section s = new Section();
                    s.name = sh.getName();
                    s.va = sh.address;
                    s.raw = sh.section_offset;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sections;
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
