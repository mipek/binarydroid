package de.thwildau.mpekar.binarydroid.ui.disasm;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

import de.thwildau.mpekar.binarydroid.AestheticColorGenerator;
import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.Utils;
import de.thwildau.mpekar.binarydroid.model.Container;
import de.thwildau.mpekar.binarydroid.model.SymbolItem;
import de.thwildau.mpekar.binarydroid.ui.disasm.SymbolFragment.OnSymbolSelectListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link SymbolItem} and makes a call to the
 * specified {@link SymbolFragment.OnSymbolSelectListener}.
 */
public class SymbolRecyclerViewAdapter extends RecyclerView.Adapter<SymbolRecyclerViewAdapter.ViewHolder> {

    private List<SymbolItem> values;
    private List<Container.Section> sections;
    private HashMap<String, Integer> sectionColors;
    private final OnSymbolSelectListener listener;

    public SymbolRecyclerViewAdapter(OnSymbolSelectListener listener) {
        this.values = null;
        this.listener = listener;
    }

    public List<SymbolItem> getValues() {
        return values;
    }

    public void setValues(List<Container.Section> sections, List<SymbolItem> mValues) {
        this.sections = sections;
        this.values = mValues;
        // Assign each section a random color
        if (sections != null) {
            AestheticColorGenerator colorGenerator = new AestheticColorGenerator();
            this.sectionColors = new HashMap<>(sections.size());
            for (Container.Section section : sections) {
                if (section.name != null && section.size > 0) {
                    int color = colorGenerator.generateRandomColor();
                    this.sectionColors.put(section.name, color);
                }
            }
        }
    }

    // Return the section the symbol points to
    private Container.Section getSymbolSection(SymbolItem symbol) {
        if (this.sections != null) {
            for (Container.Section section : this.sections) {
                if (symbol.addr >= section.va && symbol.addr < (section.va + section.size)) {
                    return section;
                }
            }
        }
        return null;
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
        holder.symbol = values.get(position);
        holder.address.setText("0x" + Utils.l2s(holder.symbol.addr, wordSize));
        holder.name.setText(holder.symbol.name);

        int sectionColor = Color.GRAY;
        Container.Section section = getSymbolSection(holder.symbol);
        if (section != null) {
            Integer color = this.sectionColors.get(section.name);
            if (color != null) {
                sectionColor = color;
            }

            holder.section.setText(section.name);
        } else {
            holder.section.setText(R.string.secetionunknown);
        }

        // Set circle color based on section name
        GradientDrawable drawable = (GradientDrawable) holder.icon.getDrawable();
        drawable.setColor(sectionColor);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSymbolSection(holder.symbol) == null) {
                    Toast.makeText
                            (holder.view.getContext(), R.string.nosection, Toast.LENGTH_SHORT).show();
                    return;

                }
                if (listener != null) {
                    listener.onSymbolSelected(holder.symbol);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final ImageView icon;
        public final TextView section;
        public final TextView address;
        public final TextView name;
        public SymbolItem symbol;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            this.icon = view.findViewById(R.id.symicon);
            this.section = view.findViewById(R.id.symsection);
            this.address = view.findViewById(R.id.address);
            this.name = view.findViewById(R.id.name);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + name.getText() + "'";
        }
    }
}
