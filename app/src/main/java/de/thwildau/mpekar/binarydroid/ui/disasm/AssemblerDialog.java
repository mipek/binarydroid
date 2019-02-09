package de.thwildau.mpekar.binarydroid.ui.disasm;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.nio.ByteBuffer;

import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.Utils;
import de.thwildau.mpekar.binarydroid.assembly.ByteAccessor;
import de.thwildau.mpekar.binarydroid.assembly.Disassembler;
import de.thwildau.mpekar.binarydroid.assembly.oracleassembler.AssemblerException;
import de.thwildau.mpekar.binarydroid.assembly.oracleassembler.AssemblerOracle;

/**
 * Basic single line assembler dialog.
 */
public class AssemblerDialog extends Dialog {
    private final Disassembler disassembler;
    private final ByteAccessor accessor;
    private final Callback cb;

    public AssemblerDialog(@NonNull Context context,
                           Disassembler disassembler, ByteAccessor accessor, Callback cb) {
        super(context);
        this.disassembler = disassembler;
        this.accessor = accessor;
        this.cb = cb;
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
                CharSequence addrChars = addr.getText();
                String assembly = code.getText().toString();

                // Make sure we have actual input to work with
                if (addrChars.length() == 0 || assembly.isEmpty()) {
                    Toast.makeText(
                            getContext(), R.string.pleaseinputstuff, Toast.LENGTH_SHORT).show();
                    return;
                }

                long address = Long.parseLong(addr.getText().toString(), 16);
                AssemblerOracle assembler = new AssemblerOracle(disassembler);
                try {
                    byte[] bytes = assembler.assembleSingle(assembly, address);
                    StringBuilder sb = new StringBuilder();
                    for (int i=0; i<bytes.length; ++i) {
                        Utils.b2s(sb, bytes[i]);
                    }
                    Toast.makeText(
                            getContext(), "Bytes: " + sb.toString(), Toast.LENGTH_SHORT).show();

                    // modify bytes in memory (until save)
                    ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
                    byteBuffer.put(bytes);
                    accessor.modifyBytes(address, byteBuffer);

                    // we succeeded
                    cb.onDialogAction(true);
                    dismiss();
                    return;
                } catch (AssemblerException e) {
                    Toast.makeText(getContext(), translateException(e), Toast.LENGTH_LONG).show();
                }
                cb.onDialogAction(false);
            }
        });
    }

    private String translateException(AssemblerException ex) {
        Resources res = getContext().getResources();
        int stringId = 0;
        switch (ex.getErrorType()) {
            case LookupFailed:
                stringId = R.string.asmerr_lookupfail;
                break;
            case FailureLimitReached:
                stringId = R.string.asmerr_limitreached;
                break;
            case CannotAssemble:
                stringId = R.string.asmerr_cannotasm;
                break;
            case UnterminatedRegisterList:
                stringId = R.string.asmerr_untermreglist;
                break;
            case UnexpectedChar:
                stringId = R.string.asmerr_unexpectedchar;
                break;
            case UnrecognizedToken:
                stringId = R.string.asmerr_unrecognizedtoken;
                break;
            case InvalidRegister:
                stringId = R.string.asmerr_invreg;
                break;
        }
        try {
            return getContext().getResources().getString(stringId, ex.getX(), ex.getY());
        } catch (Resources.NotFoundException x) {
            return ex.toString();
        }
    }

    public interface Callback {
        /**
         * Fired when the file has been modified.
         * @param positive
         */
        void onDialogAction(boolean positive);
    }
}
