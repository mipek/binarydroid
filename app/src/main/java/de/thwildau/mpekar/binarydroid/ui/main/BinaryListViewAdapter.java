package de.thwildau.mpekar.binarydroid.ui.main;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.Utils;
import de.thwildau.mpekar.binarydroid.model.BinaryFile;

/**
 * {@link RecyclerView.Adapter} that can display a {@link BinaryFile} and makes a call to the
 * specified {@link InteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class BinaryListViewAdapter extends RecyclerView.Adapter<BinaryListViewAdapter.ViewHolder> {

    private final List<BinaryFile> appList;
    private final InteractionListener mListener;
    private static final float EntryHeightPercentage = 0.2f;

    public BinaryListViewAdapter(List<BinaryFile> list, InteractionListener listener) {
        appList = list;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_applist, parent, false);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * EntryHeightPercentage);
        view.setLayoutParams(layoutParams);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final BinaryFile binaryFile = appList.get(position);
        holder.binaryFile = binaryFile;

        String packageName = Utils.trimPackageNameNumber(binaryFile.getPackageName());
        holder.container.setBackgroundColor(binaryFile.getColor());
        holder.packageName.setText(packageName);
        holder.architecture.setText(binaryFile.getArch());
        holder.binary.setText(binaryFile.getBinary());

        try {
            Drawable applicationIcon = holder.icon.getContext().
                    getPackageManager().getApplicationIcon(packageName);
            holder.icon.setImageDrawable(applicationIcon);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Notify the listener (if any)
                if (null != mListener) {
                    mListener.onSelectBinaryFile(binaryFile);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public BinaryFile binaryFile;

        public final ConstraintLayout container;
        public final ImageView icon;
        public final TextView packageName;
        public final TextView architecture;
        public final TextView binary;

        public ViewHolder(View view) {
            super(view);
            this.view = view;

            container = view.findViewById(R.id.bin);
            icon = view.findViewById(R.id.applisticon);
            packageName = view.findViewById(R.id.packageName);
            architecture = view.findViewById(R.id.arch);
            binary = view.findViewById(R.id.binary);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + binaryFile + "'";
        }
    }
}
