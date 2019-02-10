package de.thwildau.mpekar.binarydroid.ui.main;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import de.thwildau.mpekar.binarydroid.MainActivity;
import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.Utils;
import de.thwildau.mpekar.binarydroid.model.BinaryFile;

import static android.content.DialogInterface.BUTTON_POSITIVE;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link InteractionListener}
 * interface.
 */
public class BinaryListFragment extends Fragment {
    private InteractionListener mListener;
    private TextView rootInfo;
    private Button rootRequest;
    private RecyclerView recyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BinaryListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_applist_list, container, false);

        rootInfo = view.findViewById(R.id.rootrequiredlabel);
        rootRequest = view.findViewById(R.id.rootreq);
        recyclerView = view.findViewById(R.id.list);

        final SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);

        // Kick this off to initialize the fragment view
        onRootAccessChange(preferences, false);

        // Add root request button that triggers the messagebox
        rootRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRootRequestDialog(preferences);
            }
        });

        return view;
    }

    private void onRootAccessChange(SharedPreferences preferences, boolean allowOverride) {
        int allowRootState = preferences.getInt(MainActivity.PERF_ALLOWROOT, MainActivity.ALLOWROOT_DENY);
        if (!allowOverride && allowRootState != MainActivity.ALLOWROOT_GRANT) {
            // Hide root required message and request button if access was already granted.
            rootInfo.setVisibility(View.VISIBLE);
            rootRequest.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            // Root granted, only show the RecyclerView
            rootInfo.setVisibility(View.GONE);
            rootRequest.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            // Set the adapter via our view model.
            // This will ask the system for SU permission
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            BinaryListViewModel vm = ViewModelProviders.of(this).get(BinaryListViewModel.class);

            vm.getBinaries().observe(this, new Observer<List<BinaryFile>>() {
                @Override
                public void onChanged(@Nullable List<BinaryFile> list) {
                    BinaryListViewAdapter adapter = new BinaryListViewAdapter(list, mListener);
                    recyclerView.setAdapter(adapter);
                }
            });
        }
    }

    private void showRootRequestDialog(final SharedPreferences preferences) {
        int allowRootState = preferences.getInt(MainActivity.PERF_ALLOWROOT, MainActivity.ALLOWROOT_UNSPECIFIED);
        if (allowRootState == MainActivity.ALLOWROOT_UNSPECIFIED) {
            Utils.requestSU(getActivity(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    SharedPreferences.Editor editor = preferences.edit();
                    if (which == BUTTON_POSITIVE) {
                        editor.putInt(MainActivity.PERF_ALLOWROOT, MainActivity.ALLOWROOT_GRANT);
                    } else {
                        editor.putInt(MainActivity.PERF_ALLOWROOT, MainActivity.ALLOWROOT_DENY);
                    }
                    editor.commit();
                    onRootAccessChange(preferences, (which == BUTTON_POSITIVE));
                }
            });
        }
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
}
