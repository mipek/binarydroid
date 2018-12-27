package de.thwildau.mpekar.binarydroid.ui.disasm;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.Utils;
import de.thwildau.mpekar.binarydroid.model.SymbolItem;
import de.thwildau.mpekar.binarydroid.ui.disasm.SymbolFragment.OnSymbolSelectListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link SymbolItem} and makes a call to the
 * specified {@link SymbolFragment.OnSymbolSelectListener}.
 */
public class SymbolRecyclerViewAdapter extends RecyclerView.Adapter<SymbolRecyclerViewAdapter.ViewHolder> {

    private List<SymbolItem> mValues;
    private final OnSymbolSelectListener mListener;

    public SymbolRecyclerViewAdapter(List<SymbolItem> items, OnSymbolSelectListener listener) {
        mValues = items;
        mListener = listener;
    }

    public List<SymbolItem> getValues() {
        return mValues;
    }

    public void setValues(List<SymbolItem> mValues) {
        this.mValues = mValues;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.symbol_fragment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        byte wordSize = 4;//getVm().getBinary().getValue().getWordSize();
        holder.mItem = mValues.get(position);
        holder.address.setText(Utils.l2s(holder.mItem.addr, wordSize));
        holder.name.setText(holder.mItem.name);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onSymbolSelected(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView address;
        public final TextView name;
        public SymbolItem mItem;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            this.address = (TextView) view.findViewById(R.id.address);
            this.name = (TextView) view.findViewById(R.id.name);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + name.getText() + "'";
        }
    }
}
