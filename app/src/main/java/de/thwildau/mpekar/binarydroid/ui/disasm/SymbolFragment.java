package de.thwildau.mpekar.binarydroid.ui.disasm;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.model.Container;
import de.thwildau.mpekar.binarydroid.model.SymbolItem;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnSymbolSelectListener}
 * interface.
 */
public class SymbolFragment extends DisasmFragment {

    private static final String ARG_COLUMN_COUNT = "column-count";

    private int mColumnCount = 1;
    private OnSymbolSelectListener mListener;
    private DisassemblerViewModel viewModel;
    private RecyclerView recyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SymbolFragment() {
    }

    @SuppressWarnings("unused")
    public static SymbolFragment newInstance(int columnCount) {
        SymbolFragment fragment = new SymbolFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        viewModel = ViewModelProviders.of(getActivity()).get(DisassemblerViewModel.class);
        viewModel.getBinary().observe(this, new Observer<Container>() {
            @Override
            public void onChanged(@Nullable Container elf) {
                SymbolRecyclerViewAdapter adapter = (SymbolRecyclerViewAdapter) recyclerView.getAdapter();
                adapter.setValues(elf.getSymbols());
            }
        });
    }

    private List<SymbolItem> getSymbolList() {
        if (viewModel.getBinary() != null && viewModel.getBinary().getValue() != null) {
            return viewModel.getBinary().getValue().getSymbols();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_symbol_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new SymbolRecyclerViewAdapter(getSymbolList(), mListener));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSymbolSelectListener) {
            mListener = (OnSymbolSelectListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSymbolSelectListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnSymbolSelectListener {
        void onSymbolSelected(SymbolItem item);
    }
}
