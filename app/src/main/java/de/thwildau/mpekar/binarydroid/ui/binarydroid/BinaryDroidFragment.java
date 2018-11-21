package de.thwildau.mpekar.binarydroid.ui.binarydroid;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.thwildau.mpekar.binarydroid.R;

public class BinaryDroidFragment extends Fragment {

    private BinaryDroidViewModel mViewModel;

    public static BinaryDroidFragment newInstance() {
        return new BinaryDroidFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.binary_droid_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(BinaryDroidViewModel.class);
        // TODO: Use the ViewModel
    }

}
