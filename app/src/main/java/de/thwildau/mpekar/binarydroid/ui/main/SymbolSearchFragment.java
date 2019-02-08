package de.thwildau.mpekar.binarydroid.ui.main;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.chrisplus.rootmanager.RootManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.SymbolSearcher;
import de.thwildau.mpekar.binarydroid.Utils;
import de.thwildau.mpekar.binarydroid.model.BinaryFile;
import de.thwildau.mpekar.binarydroid.model.SymbolItem;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static de.thwildau.mpekar.binarydroid.MainActivity.ALLOWROOT_DENY;
import static de.thwildau.mpekar.binarydroid.MainActivity.ALLOWROOT_GRANT;
import static de.thwildau.mpekar.binarydroid.MainActivity.PERF_ALLOWROOT;

public class SymbolSearchFragment extends Fragment implements SymbolSearchInterface {
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    CheckBox ignoreCase;
    CheckBox regexSyntax;
    CheckBox limitSearchApps;
    Button startSearch;
    View appListDisabledOverlay;
    Set<String> selectedApps;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_symbolsearch, container, false);
        recyclerView = view.findViewById(R.id.applist);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(new SymbolSearchAppListAdapter(this));

        appListDisabledOverlay = view.findViewById(R.id.applist_disabled);
        ignoreCase = view.findViewById(R.id.ignoreCase);
        regexSyntax = view.findViewById(R.id.regexSyntax);
        limitSearchApps = view.findViewById(R.id.limitSearch);

        // We enable/disable the application list based on the state of the checkbox above it.
        limitSearchApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isAppListVisible = limitSearchApps.isChecked();
                recyclerView.setEnabled(isAppListVisible);
                recyclerView.setLayoutFrozen(!isAppListVisible);
                appListDisabledOverlay.setVisibility(isAppListVisible ? View.GONE : View.VISIBLE);

                for (int childCount = recyclerView.getChildCount(), i = 0; i < childCount; ++i) {
                    final RecyclerView.ViewHolder holder =
                            recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
                    if (holder != null) {
                        ((SymbolSearchAppListAdapter.ViewHolder) holder).setEnabled(isAppListVisible);
                    }
                }
            }
        });
        limitSearchApps.callOnClick(); // properly initialize application list starting state

        // "start search" button functionality
        final TextView searchString = view.findViewById(R.id.searchView);
        startSearch = view.findViewById(R.id.btnstartsearch);
        startSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String symbolName = searchString.getText().toString();

                // Make sure we have root access
                final SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                int allowRootState = preferences.getInt(PERF_ALLOWROOT, ALLOWROOT_DENY);
                if (allowRootState != ALLOWROOT_GRANT) {
                    // Ask the user if it is OK so request SU permissions
                    Utils.requestSU(getActivity(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            SharedPreferences.Editor editor = preferences.edit();
                            if (which == BUTTON_POSITIVE) {
                                editor.putInt(PERF_ALLOWROOT, ALLOWROOT_GRANT);
                                startSearch(symbolName);
                            } else {
                                editor.putInt(PERF_ALLOWROOT, ALLOWROOT_DENY);
                            }
                            editor.commit();
                        }
                    });
                } else {
                    // We already asked the user about the SU permission thing; start the search
                    startSearch(symbolName);
                }
            }
        });

        return view;
    }

    // Returns a set of all application package names that are selected/filtered.
    private Set<String> getSelectedApps() {
        HashSet<String> set = new HashSet<>();

        if (limitSearchApps.isChecked()) {
            SymbolSearchAppListAdapter adapter = (SymbolSearchAppListAdapter) recyclerView.getAdapter();
            for (int i = 0; i < adapter.getItemCount(); ++i) {
                FilteredAppInfo appInfo = adapter.getItemByIndex(i);

                // If this application is checked we have to add it to our set.
                if (appInfo.checked) {
                    set.add(appInfo.packageName);
                }
            }
        }
        return set;
    }

    private void startSearch(final String symbolName) {
        // Obtain SU permissions
        RootManager.getInstance().obtainPermission();

        final SymbolSearchFragment myself = this;
        final BinaryListViewModel vm = ViewModelProviders.of(getActivity()).get(BinaryListViewModel.class);
        vm.getBinaries().observe(this, new Observer<List<BinaryFile>>() {
            @Override
            public void onChanged(@Nullable final List<BinaryFile> binaries) {
                // Remove observer (new search will create a new one)
                vm.getBinaries().removeObservers(myself);
                // Start the search in the background
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        SymbolSearcher symbolSearcher = new SymbolSearcher(
                                binaries,
                                regexSyntax.isChecked(),
                                ignoreCase.isChecked());

                        // Register the search progress listener
                        symbolSearcher.setListener(myself);
                        // Do the actual searching..
                        symbolSearcher.search(symbolName);
                    }
                });
            }
        });
    }

    @Override
    public void onSearchStarted() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Disable the button while a search is currently in progress
                startSearch.setEnabled(false);
            }
        });

        // Update selected/filtered app list cache
        selectedApps = getSelectedApps();
        Log.d("BinaryDroid", "Starting symbol search");
    }

    @Override
    public void onSymbolMatch(BinaryFile binary, SymbolItem symbolItem) {
        Log.d("BinaryDroid", "Symbol match: " + symbolItem.toString());
    }

    @Override
    public void onSearchComplete(int symbolCount) {
        // Re-enable the search button
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Disable the button while a search is currently in progress
                startSearch.setEnabled(true);
            }
        });

        Log.d("BinaryDroid", "Symbol search finished, symbolCount=" + symbolCount);
    }

    @Override
    public boolean shouldSkipApp(String packageName) {
        if (limitSearchApps.isChecked()) {
            // We use our cached app list to ensure we're O(1)
            return !selectedApps.contains(packageName);
        }
        return false;
    }

    static class FilteredAppInfo extends ApplicationInfo {
        private boolean checked;

        FilteredAppInfo(ApplicationInfo applicationInfo, boolean checked) {
            super(applicationInfo);
            this.checked = checked;
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }
    }
}
