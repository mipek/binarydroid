package de.thwildau.mpekar.binarydroid.ui.main;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.thwildau.mpekar.binarydroid.R;

/**
 * This adapter is responsible for managing the list of installed applications.
 * The user can check each individual app that he wants to include in his symbol search.
 */
class SymbolSearchAppListAdapter extends RecyclerView.Adapter {
    private SymbolSearchFragment symbolSearchFragment;
    private List<SymbolSearchFragment.FilteredAppInfo> appList;

    SymbolSearchAppListAdapter(SymbolSearchFragment symbolSearchFragment) {
        this.symbolSearchFragment = symbolSearchFragment;
        List<ApplicationInfo> applications = getPackageManager().
                getInstalledApplications(PackageManager.GET_META_DATA);

        // Copy data to our own list because we store additional information in our objects.
        appList = new ArrayList<>(applications.size());
        for (ApplicationInfo appInfo: applications) {
            if (appInfo.packageName.contains("nekotachi")) continue;
            appList.add(new SymbolSearchFragment.FilteredAppInfo(appInfo, false));
        }
    }

    private PackageManager getPackageManager() {
        return symbolSearchFragment.getContext().getPackageManager();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_symbolsearch_appentry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder vh = (ViewHolder)holder;
        // set current state based on the element position
        vh.setAppInfo(getPackageManager(), appList.get(position));
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    SymbolSearchFragment.FilteredAppInfo getItemByIndex(int index) {
        return appList.get(index);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        private ImageView appIcon;
        private TextView appName;
        private CheckBox checker;
        private SymbolSearchFragment.FilteredAppInfo appInfo;

        ViewHolder(View view) {
            super(view);
            this.view = view;

            appIcon = view.findViewById(R.id.appIcon);
            appName = view.findViewById(R.id.appName);
            checker = view.findViewById(R.id.appChecker);
            checker.setChecked(false);

            View.OnClickListener checkBoxTrampoline = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (checker.isEnabled() && view.getId() != checker.getId()) {
                        // This is not the CheckBox you are looking for, move along
                        checker.setChecked(!checker.isChecked());
                    }
                    appInfo.setChecked(checker.isChecked());
                }
            };

            // "redirect" every click event to the checkbox
            appIcon.setOnClickListener(checkBoxTrampoline);
            appName.setOnClickListener(checkBoxTrampoline);
            view.setOnClickListener(checkBoxTrampoline);
            // most important listener: the checkbox itself
            checker.setOnClickListener(checkBoxTrampoline);
        }

        void setAppInfo(PackageManager packageManager, SymbolSearchFragment.FilteredAppInfo appInfo) {
            this.appInfo = appInfo;
            Drawable icon = packageManager.getApplicationIcon(appInfo);
            CharSequence label = packageManager.getApplicationLabel(appInfo);

            appIcon.setImageDrawable(icon);
            appName.setText(label);
            checker.setChecked(appInfo.isChecked());
        }

        public void setEnabled(boolean enabled) {
            checker.setEnabled(enabled);
        }
    }
}
