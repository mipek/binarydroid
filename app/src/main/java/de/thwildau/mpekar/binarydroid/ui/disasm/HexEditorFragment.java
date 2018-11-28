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

public class HexEditorFragment extends Fragment {

    private DisassemblerViewModel viewModel;
    private File elfFile;
   //private HexEditView hexView;

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
        viewModel = ViewModelProviders.of(getActivity()).get(DisassemblerViewModel.class);

        // register observer on our {@link ByteAccessor} inside the ViewModel so we can point it to the HexView
        final HexEditorFragment me = this;
        viewModel.getAccessorr().observe(this, new Observer<ByteAccessor>() {
            @Override
            public void onChanged(@Nullable ByteAccessor byteAccessor) {
                HexEditView hexView = me.getView().findViewById(R.id.hexview);
                if (hexView != null) {
                    hexView.setAccessor(byteAccessor);
                } else {
                    Log.e("BinaryDroid", "couldn't find HexView");
                }
            }
        });

        // init viewmodel only on first call (when |elfFile| is not null)
        if (elfFile != null) {
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

}
