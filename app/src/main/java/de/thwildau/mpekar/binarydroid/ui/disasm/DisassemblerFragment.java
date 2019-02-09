package de.thwildau.mpekar.binarydroid.ui.disasm;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.Utils;
import de.thwildau.mpekar.binarydroid.assembly.ByteAccessor;
import de.thwildau.mpekar.binarydroid.assembly.Disassembler;
import de.thwildau.mpekar.binarydroid.model.Container;

/**
 * Fragment responsible for showing the disassembly.
 */
public class DisassemblerFragment extends Fragment {

    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerView;
    private DisassemblerViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_disasm, container, false);
        recyclerView = v.findViewById(R.id.disasmrecycler);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(this.layoutManager);

        viewModel = ViewModelProviders.of(getActivity()).get(DisassemblerViewModel.class);

        //TODO: if we want to support other architectures we cant rely
        // on a fixed instruction size (think about CISC architectures like x86)
        final int instructionSize = 4;

        // Update adapter whenever we get a (new) binary file.
        viewModel.getBinary().observe(this, new Observer<Container>() {
            @Override
            public void onChanged(@Nullable Container container) {
                final int wordSize = container.getWordSize();

                recyclerView.setAdapter(new RecyclerView.Adapter() {
                    @NonNull
                    @Override
                    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View v = LayoutInflater.from(parent.getContext()).
                                inflate(R.layout.fragment_disasm_line, parent, false);
                        return new ViewHolder(v);
                    }

                    @Override
                    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                        Disassembler d = getDisassembler();
                        ByteAccessor accessor = getAccessor();

                        if (d != null && accessor != null) {
                            int address = position * instructionSize;

                            // Disassemble
                            Disassembler.Instruction[] insns =
                                    d.disassemble(getAccessor(), address, instructionSize);

                            // Get instruction
                            Disassembler.Instruction insn;
                            if (insns.length > 1) {
                                // reserved for future; theoretically you can decode multiple
                                // instructions with a single "disassemble"-call. This is
                                // interesting when we add support for architectures without
                                // fixed instruction sizes (like x86)..
                                throw new RuntimeException("unexpected instruction count");
                            } else if (insns.length == 1) {
                                insn = insns[0];
                            } else {
                                // failed to decode, provide dummy instruction
                                insn = Utils.dummyInstruction((short) 4);
                            }

                            ViewHolder viewHolder = (ViewHolder) holder;
                            viewHolder.addr.setText(Utils.l2s(address, wordSize));
                            viewHolder.mnemonic.setText(insn.toString());
                        }
                    }

                    @Override
                    public int getItemCount() {
                        ByteAccessor accessor = getAccessor();
                        if (accessor != null) {
                            // We will never have more instructions than the following:
                            return (int) (accessor.getTotalBytes() / instructionSize);
                        }
                        return 0;
                    }

                    private Disassembler getDisassembler() {
                        try {
                            return viewModel.getDisasm().getValue();
                        } catch (NullPointerException ex) {
                            return null;
                        }
                    }

                    private ByteAccessor getAccessor() {
                        try {
                            return viewModel.getAccessor().getValue();
                        } catch (NullPointerException ex) {
                            return null;
                        }
                    }
                });
            }
        });

        // Observe the address, when it changes we need to jump to the specified address
        viewModel.getAddress().observe(this, new Observer<Long>() {
            @Override
            public void onChanged(@Nullable Long newAddress) {
                // One "position" = one instruction. We assume instruction size is always 4 bytes.
                int position = (int) (newAddress / instructionSize);
                layoutManager.scrollToPosition(position);
            }
        });

        return v;
    }

    class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        final TextView addr;
        final TextView mnemonic;

        ViewHolder(View view) {
            super(view);
            this.addr = (TextView) view.findViewById(R.id.disasm_addr);
            this.mnemonic = (TextView) view.findViewById(R.id.disasm_mnemonic);
        }
    }
}
