package de.thwildau.mpekar.binarydroid.ui.disasm;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.fornwall.jelf.ElfFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.disasm.ByteAccessor;

public class HexEditorFragment extends Fragment {

    private DisassemblerViewModel viewModel;
    private File elfFile;

    public static HexEditorFragment newInstance(File elfFile) {
        HexEditorFragment frag = new HexEditorFragment();
        frag.elfFile = elfFile;
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
        viewModel = ViewModelProviders.of(this).get(DisassemblerViewModel.class);

        try {
            ByteAccessor accessor = new ByteAccessor(elfFile);
            viewModel.setAccessor(accessor);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            ElfFile elf = ElfFile.fromFile(elfFile);
            viewModel.setBinary(elf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
