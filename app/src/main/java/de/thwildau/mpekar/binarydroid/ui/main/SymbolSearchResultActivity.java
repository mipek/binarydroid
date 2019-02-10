package de.thwildau.mpekar.binarydroid.ui.main;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.SymbolSearchInterface;
import de.thwildau.mpekar.binarydroid.Utils;
import de.thwildau.mpekar.binarydroid.model.BinaryFile;
import de.thwildau.mpekar.binarydroid.model.SymbolItem;

/**
 * This activity is responsible for displaying the symbol search results.
 */
public class SymbolSearchResultActivity extends AppCompatActivity {

    LinearLayoutManager layoutManager;
    private RecyclerView recyclerView;
    private List<SymbolSearchInterface.ResultEntry> results;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.symbolsearchresult_activity);

        results = ActivityResult.symbolSearchResults;

        final PackageManager packageManager = getPackageManager();
        recyclerView = findViewById(R.id.symbolsearchresults);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.symbolsearchresult_entry, parent, false);
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                ViewHolder vh = (ViewHolder) holder;

                SymbolSearchInterface.ResultEntry result = results.get(position);
                BinaryFile binary = result.binary;
                SymbolItem symbol = result.symbol;

                String packageName = Utils.trimPackageNameNumber(binary.getPackageName());

                vh.path.setText(binary.buildPath());
                vh.name.setText(symbol.toString());
                try {
                    vh.icon.setImageDrawable(packageManager.getApplicationIcon(packageName));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public int getItemCount() {
                return results.size();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Delete symbol search result
        ActivityResult.symbolSearchResults = null;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView path;
        TextView name;

        public ViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.symbolresulticon);
            path = view.findViewById(R.id.symbolresultpath);
            name = view.findViewById(R.id.symbolresultname);
        }
    }
}
