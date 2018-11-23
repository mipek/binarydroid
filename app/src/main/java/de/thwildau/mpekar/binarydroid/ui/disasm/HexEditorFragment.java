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

import de.thwildau.mpekar.binarydroid.R;

public class HexEditorFragment extends Fragment {

    private DisassemblerViewModel viewModel;
    private ElfFile elf;

    public static HexEditorFragment newInstance(ElfFile elf) {
        HexEditorFragment frag = new HexEditorFragment();
        frag.elf = elf;
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

        viewModel.setBinary(elf);
    }

}
