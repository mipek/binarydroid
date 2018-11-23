package de.thwildau.mpekar.binarydroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import net.fornwall.jelf.ElfFile;

import java.io.File;
import java.io.IOException;

import de.thwildau.mpekar.binarydroid.ui.disasm.HexEditorFragment;

public class DisassemblerActivity extends AppCompatActivity {

    public static final String EXTRA_BINPATH = "extra_binpath";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Add main fragment if this is first creation
        if (savedInstanceState == null) {
            String binpath = getIntent().getStringExtra(EXTRA_BINPATH);
            try {
                ElfFile elf = ElfFile.fromFile(new File(binpath));

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, HexEditorFragment.newInstance(elf))
                        .commitNow();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}