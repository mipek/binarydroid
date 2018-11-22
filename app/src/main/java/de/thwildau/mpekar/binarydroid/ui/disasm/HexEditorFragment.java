package de.thwildau.mpekar.binarydroid.ui.disasm;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.thwildau.mpekar.binarydroid.R;

public class HexEditorFragment extends Fragment {

    private HexEditorViewModel viewModel;

    public static HexEditorFragment newInstance(String binpath) {
        return new HexEditorFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.hexed_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(HexEditorViewModel.class);
        // TODO: Use the ViewModel
    }

}
