package de.thwildau.mpekar.binarydroid.ui.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chrisplus.rootmanager.RootManager;

import java.util.List;

import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.SymbolSearcher;
import de.thwildau.mpekar.binarydroid.Utils;
import de.thwildau.mpekar.binarydroid.model.BinaryFile;
import de.thwildau.mpekar.binarydroid.model.SymbolItem;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static de.thwildau.mpekar.binarydroid.MainActivity.ALLOWROOT_DENY;
import static de.thwildau.mpekar.binarydroid.MainActivity.ALLOWROOT_GRANT;
import static de.thwildau.mpekar.binarydroid.MainActivity.PERF_ALLOWROOT;

public class SymbolSearchFragment extends Fragment {
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    CheckBox ignoreCase;
    CheckBox regexSyntax;
    CheckBox limitSearchApps;
    View appListDisabledOverlay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_symbolsearch, container, false);
        recyclerView = view.findViewById(R.id.applist);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(new AppListAdapter());

        appListDisabledOverlay = view.findViewById(R.id.applist_disabled);
        ignoreCase = view.findViewById(R.id.ignoreCase);
        regexSyntax = view.findViewById(R.id.regexSyntax);
        limitSearchApps = view.findViewById(R.id.limitSearch);
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
                        ((ViewHolder) holder).setEnabled(isAppListVisible);
                    }
                }
            }
        });
        limitSearchApps.callOnClick(); // properly initialize application list state

        final TextView searchString = view.findViewById(R.id.searchView);
        final Button startSearch = view.findViewById(R.id.btnstartsearch);
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
                    startSearch(symbolName);
                }
            }
        });

        return view;
    }

    private void startSearch(final String symbolName) {
        // Obtain SU permissions
        RootManager.getInstance().obtainPermission();

        BinaryListViewModel vm = ViewModelProviders.of(getActivity()).get(BinaryListViewModel.class);
        final List<BinaryFile> binaries = vm.getBinaries().getValue();
        if (binaries != null && binaries.size() > 0) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    SymbolSearcher symbolSearcher = new SymbolSearcher(
                            binaries,
                            regexSyntax.isChecked(),
                            ignoreCase.isChecked());

                    // Register the search progress listener
                    symbolSearcher.setListener(new SymbolSearchInterface() {
                        @Override
                        public void onSymbolMatch(BinaryFile binary, SymbolItem symbolItem) {
                            Log.d("BinaryDroid", "Symbol match: " + symbolItem.toString());
                        }

                        @Override
                        public boolean shouldSkipApp(String packageName) {
                            if (limitSearchApps.isChecked()) {
                                // TODO: add support for search limiting
                            }
                            return false;
                        }
                    });
                    symbolSearcher.search(symbolName);
                }
            });
        } else {
            Toast.makeText(getContext(), R.string.couldntgetsymbols, Toast.LENGTH_SHORT).show();
        }
    }

    private class AppListAdapter extends RecyclerView.Adapter {
        private List<ApplicationInfo> appList;

        AppListAdapter() {
            appList = getContext().getPackageManager().
                    getInstalledApplications(PackageManager.GET_META_DATA);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_symbolsearch_appentry, parent, false);
            return new ViewHolder(view, false);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ViewHolder vh = (ViewHolder)holder;
            vh.setAppInfo(appList.get(position));
        }

        @Override
        public int getItemCount() {
            return appList.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        private ImageView appIcon;
        private TextView appName;
        private CheckBox checker;

        public ViewHolder(View view, boolean checkerState) {
            super(view);
            this.view = view;

            appIcon = view.findViewById(R.id.appIcon);
            appName = view.findViewById(R.id.appName);
            checker = view.findViewById(R.id.appChecker);
            checker.setChecked(checkerState);

            View.OnClickListener checkBoxTrampoline = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (checker.isEnabled()) {
                        checker.setChecked(!checker.isChecked());
                    }
                }
            };

            // "redirect" every click event to the checkbox
            appIcon.setOnClickListener(checkBoxTrampoline);
            appName.setOnClickListener(checkBoxTrampoline);
            view.setOnClickListener(checkBoxTrampoline);
        }

        public void setAppInfo(ApplicationInfo appInfo) {
            PackageManager packageManager = getContext().getPackageManager();
            Drawable icon = packageManager.getApplicationIcon(appInfo);
            CharSequence label = packageManager.getApplicationLabel(appInfo);

            appIcon.setImageDrawable(icon);
            appName.setText(label);
        }

        public void setEnabled(boolean enabled) {
            checker.setEnabled(enabled);
        }
    }
}
