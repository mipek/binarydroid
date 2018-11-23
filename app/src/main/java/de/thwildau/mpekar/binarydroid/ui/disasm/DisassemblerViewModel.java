package de.thwildau.mpekar.binarydroid.ui.disasm;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import net.fornwall.jelf.ElfFile;

/**
 * This is the main viewmodel that is shared across all disassembler related UIs.
 */
public class DisassemblerViewModel extends ViewModel {
    private final MutableLiveData<ElfFile> binary = new MutableLiveData<ElfFile>();

    public void setBinary(ElfFile item) {
        binary.setValue(item);
    }

    public LiveData<ElfFile> getBinary() {
        return binary;
    }
}
