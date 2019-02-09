package de.thwildau.mpekar.binarydroid.ui.disasm;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import de.thwildau.mpekar.binarydroid.R;
import de.thwildau.mpekar.binarydroid.assembly.ByteAccessor;

public class SaveFileDialog extends Dialog implements View.OnClickListener {
    private ByteAccessor accessor;
    private EditText savePath;
    private Button saveYes;

    public SaveFileDialog(@NonNull Context context, ByteAccessor accessor) {
        super(context);
        this.accessor = accessor;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_savefile);

        savePath = findViewById(R.id.savepath);
        // Set default path to somewhere meaningful
        File dir = Environment.getExternalStorageDirectory();
        savePath.setText(dir.getAbsolutePath() + "/file.bin");

        Button saveNo = findViewById(R.id.saveno);
        saveYes = findViewById(R.id.saveyes);

        saveNo.setOnClickListener(this);
        saveYes.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == saveYes.getId()) {
            File destFile = new File(savePath.getText().toString());
            try {
                accessor.writeModifiedFile(destFile);
                Toast.makeText(getContext(), R.string.savecomplete, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(getContext(), R.string.savefileerror, Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        dismiss();
    }
}
