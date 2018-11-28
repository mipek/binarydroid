package de.thwildau.mpekar.binarydroid.ui.disasm;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.fornwall.jelf.ElfFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.assembly.ByteAccessor;
import de.thwildau.mpekar.binarydroid.ui.views.HexEditView;

public class DisassemblerFragment extends DisasmFragment {

    private DisassemblerViewModel viewModel;

    public static DisassemblerFragment newInstance() {
        DisassemblerFragment frag = new DisassemblerFragment();
        return frag;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.hexed_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(DisassemblerViewModel.class);

        final DisassemblerFragment me = this;
        viewModel.getBinary().observe(this, new Observer<ElfFile>() {
            @Override
            public void onChanged(@Nullable ElfFile elf) {
                // go to entrypoint
                viewModel.setAddress(elf.entry_point);
            }
        });
    }

}
