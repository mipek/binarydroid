package de.thwildau.mpekar.binarydroid.ui.main;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.model.BinaryFile;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link InteractionListener}
 * interface.
 */
public class BinaryListFragment extends Fragment {
    private InteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BinaryListFragment() {
    }

    public static BinaryListFragment newInstance() {
        return new BinaryListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_applist_list, container, false);

        // Set the adapter via our view model
        if (view instanceof RecyclerView) {
            final RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

            BinaryListViewModel vm = ViewModelProviders.of(this).get(BinaryListViewModel.class);

            vm.getBinaries().observe(this, new Observer<List<BinaryFile>>() {
                @Override
                public void onChanged(@Nullable List<BinaryFile> list) {
                    BinaryListViewAdapter adapter = new BinaryListViewAdapter(list, mListener);
                    recyclerView.setAdapter(adapter);
                }
            });
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof InteractionListener) {
            mListener = (InteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement InteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface provides to ability to interact with other fragments/activities.
     */
    public interface InteractionListener {
        void onListFragmentInteraction(BinaryFile item);
    }
}
