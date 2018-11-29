package de.thwildau.mpekar.binarydroid.ui.disasm;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.assembly.ByteAccessor;
import de.thwildau.mpekar.binarydroid.views.HexEditView;

public class HexEditorFragment extends DisasmFragment {

    private DisassemblerViewModel viewModel;

    public static HexEditorFragment newInstance() {
        HexEditorFragment frag = new HexEditorFragment();
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
    }

}
