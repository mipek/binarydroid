package de.thwildau.mpekar.binarydroid.ui.disasm;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.nio.ByteBuffer;

import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.Utils;
import de.thwildau.mpekar.binarydroid.assembly.ByteAccessor;

/**
 * This class is responsible for showing the hex viewer.
 */
public class HexEditorFragment extends Fragment {
    private static final int BYTES_PER_ROW_LANDSCAPE = 16;
    private static final int BYTES_PER_ROW_PORTRAIT = 8;
    private LayoutManager layoutManager;
    private RecyclerView recyclerView;
    private DisassemblerViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_hexed, container, false);
        recyclerView = v.findViewById(R.id.hexedrecycler);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(this.layoutManager);

        viewModel = ViewModelProviders.of(getActivity()).get(DisassemblerViewModel.class);

        // (Re-)Set the Adapter whenever we get a (new) ByteAccessor
        viewModel.getAccessor().observe(this, new Observer<ByteAccessor>() {
            @Override
            public void onChanged(@Nullable final ByteAccessor byteAccessor) {
                recyclerView.setAdapter(new RecyclerView.Adapter() {
                    @NonNull
                    @Override
                    public android.support.v7.widget.RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View v = LayoutInflater.from(parent.getContext()).
                                inflate(R.layout.fragment_hexed_line, parent, false);
                        return new ViewHolder(v);
                    }

                    @Override
                    public void onBindViewHolder(@NonNull android.support.v7.widget.RecyclerView.ViewHolder holder, int position) {
                        ViewHolder vh = (ViewHolder) holder;

                        int wordSize = getWordSize();
                        int bytesPerLine = getBytesPerRow();
                        int offset = getBytesPerRow() * position;

                        vh.addr.setText(Utils.l2s((long) offset, wordSize));

                        StringBuilder displayBytes = new StringBuilder(bytesPerLine * 3);
                        ByteBuffer bytes = byteAccessor.getBytes(offset, bytesPerLine);
                        for (int i=0; i<bytes.limit(); ++i) {
                            if (i != 0) {
                                displayBytes.append(' ');
                            }

                            byte value = bytes.get(i);
                            Utils.b2s(displayBytes, value);
                        }
                        vh.bytes.setText(displayBytes.toString());

                        StringBuilder displayChars = new StringBuilder(bytesPerLine * 2);
                        for (int i=0; i<bytes.limit(); ++i) {
                            // we can use "getChar" because it reads two characters
                            displayChars.append((char)bytes.get(i));
                        }
                        vh.ascii.setText(displayChars.toString());
                    }

                    @Override
                    public int getItemCount() {
                        return (int) (byteAccessor.getTotalBytes() / ((long) getBytesPerRow()));
                    }

                    private int getWordSize() {
                        try {
                            return viewModel.getBinary().getValue().getWordSize();
                        } catch (NullPointerException e) {
                            return 8;
                        }
                    }
                });
            }
        });

        // Observe the address, when it changes we need to jump to the specified address
        viewModel.getAddress().observe(this, new Observer<Long>() {
            @Override
            public void onChanged(@Nullable Long newAddress) {
                int position = (int) (newAddress / getBytesPerRow());
                layoutManager.scrollToPosition(position);
            }
        });

        return v;
    }

    // Returns the amount of bytes shown per row
    private int getBytesPerRow() {
        // TODO: cool feature would be to dynamically adjust this to the
        // maximum amount of bytes we can fit on the current screen
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return BYTES_PER_ROW_LANDSCAPE;
        }
        return BYTES_PER_ROW_PORTRAIT;
    }

    class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        final TextView addr;
        final TextView ascii;
        final TextView bytes;

        ViewHolder(View view) {
            super(view);
            this.addr = (TextView) view.findViewById(R.id.hexed_addr);
            this.bytes = (TextView) view.findViewById(R.id.hexed_bytes);
            this.ascii = (TextView) view.findViewById(R.id.hexed_ascii);
        }
    }
}