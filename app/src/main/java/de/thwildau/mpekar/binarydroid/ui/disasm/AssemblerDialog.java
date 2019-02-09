package de.thwildau.mpekar.binarydroid.ui.disasm;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.Utils;
import de.thwildau.mpekar.binarydroid.assembly.Disassembler;
import de.thwildau.mpekar.binarydroid.assembly.oracleassembler.AssemblerException;
import de.thwildau.mpekar.binarydroid.assembly.oracleassembler.AssemblerOracle;

public class AssemblerDialog extends Dialog {
    private Disassembler disassembler;
    public AssemblerDialog(@NonNull Context context, Disassembler disassembler) {
        super(context);
        this.disassembler = disassembler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_easyassemble);

        final EditText addr = findViewById(R.id.asm_addr);
        final EditText code = findViewById(R.id.asm_code);

        Button cancel = findViewById(R.id.btncancel);
        final Button assemble = findViewById(R.id.btnassemble);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        assemble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long address = Long.parseLong(addr.getText().toString(), 16);
                String assembly = code.getText().toString();
                AssemblerOracle assembler = new AssemblerOracle(disassembler);
                try {
                    byte[] bytes = assembler.assembleSingle(assembly, address);
                    StringBuilder sb = new StringBuilder();
                    for (int i=0; i<bytes.length; ++i) {
                        Utils.b2s(sb, bytes[i]);
                    }
                    Toast.makeText(getContext(), sb.toString(), Toast.LENGTH_LONG).show();
                } catch (AssemblerException e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}
