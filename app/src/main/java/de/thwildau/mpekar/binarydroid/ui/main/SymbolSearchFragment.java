package de.thwildau.mpekar.binarydroid.ui.main;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.thwildau.mpekar.binarydroid.R;

public class SymbolSearchFragment extends Fragment {
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
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
        return view;
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

            //ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            //layoutParams.height = (int) (parent.getHeight() * EntryHeightPercentage);
           // view.setLayoutParams(layoutParams);

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
