package de.thwildau.mpekar.binarydroid.ui.disasm;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import net.fornwall.jelf.ElfFile;

import de.thwildau.mpekar.binarydroid.assembly.ByteAccessor;

/**
 * This is the main viewmodel that is shared across all disassembler related UIs.
 */
public class DisassemblerViewModel extends ViewModel {
    private final MutableLiveData<ElfFile> binary = new MutableLiveData<ElfFile>();
    private final MutableLiveData<ByteAccessor> accessor = new MutableLiveData<>();

    public void setBinary(ElfFile item) {
        binary.setValue(item);
    }

    public LiveData<ElfFile> getBinary() {
        return binary;
    }

    public void setAccessor(ByteAccessor ac) {
        accessor.setValue(ac);
    }

    public LiveData<ByteAccessor> getAccessorr() {
        return accessor;
    }
}
