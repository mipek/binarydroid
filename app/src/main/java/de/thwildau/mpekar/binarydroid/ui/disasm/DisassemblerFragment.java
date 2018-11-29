package de.thwildau.mpekar.binarydroid.ui.disasm;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.fornwall.jelf.ElfFile;

import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.assembly.DisassemblerCapstone;

public class DisassemblerFragment extends DisasmFragment {

    private DisassemblerViewModel viewModel;
    private TextView tv;

    public static DisassemblerFragment newInstance() {
        DisassemblerFragment frag = new DisassemblerFragment();
        return frag;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.disasm_fragment, container, false);
        tv = v.findViewById(R.id.textView);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(DisassemblerViewModel.class);

        final DisassemblerFragment me = this;
        viewModel.getBinary().observe(this, new Observer<ElfFile>() {
            @Override
            public void onChanged(@Nullable ElfFile elf) {
                // go to entrypoint
                viewModel.setAddress(elf.entry_point);

                StringBuilder test = new StringBuilder();
                DisassemblerCapstone disasm = new DisassemblerCapstone();
                long addy = viewModel.getAddress().getValue();
                for (int i = 0; i < 8; ++i) {
                    String line = disasm.disassemble(viewModel.getAccessorr().getValue(), addy);
                    test.append(line);
                    test.append('\n');
                    addy += 4;
                }
                tv.setText(test.toString());
            }
        });
    }

}
