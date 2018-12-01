package de.thwildau.mpekar.binarydroid.ui.disasm;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import net.fornwall.jelf.ElfFile;

import de.thwildau.mpekar.binarydroid.assembly.ByteAccessor;
import de.thwildau.mpekar.binarydroid.assembly.Disassembler;
import de.thwildau.mpekar.binarydroid.model.Container;

/**
 * This is the main viewmodel that is shared across all disassembler related UIs.
 */
public class DisassemblerViewModel extends ViewModel {
    private final MutableLiveData<Container> binary = new MutableLiveData<Container>();
    private final MutableLiveData<ByteAccessor> accessor = new MutableLiveData<>();
    private final MutableLiveData<Long> address = new MutableLiveData<>();
    private final MutableLiveData<Disassembler> disasm = new MutableLiveData<>();
    private final MutableLiveData<Integer> fontSizeDisasm = new MutableLiveData<>();

    public void setBinary(Container item) {
        binary.setValue(item);
    }

    public LiveData<Container> getBinary() {
        return binary;
    }

    public void setAccessor(ByteAccessor ac) {
        accessor.setValue(ac);
    }

    public LiveData<ByteAccessor> getAccessor() {
        return accessor;
    }

    public void setAddress(long addy) {
        address.setValue(addy);
    }

    public LiveData<Long> getAddress() {
        return address;
    }

    public LiveData<Disassembler> getDisasm() {
        return disasm;
    }

    public void setDisasm(Disassembler d) {
        disasm.setValue(d);
    }

    public void setDisassemblerFontSize(int addy) {
        fontSizeDisasm.setValue(addy);
    }

    public LiveData<Integer> getDisassemblerFontSize() {
        return fontSizeDisasm;
    }
}
