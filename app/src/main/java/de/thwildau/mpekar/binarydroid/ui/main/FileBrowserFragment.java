package de.thwildau.mpekar.binarydroid.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import de.thwildau.mpekar.binarydroid.R;

import static android.app.Activity.RESULT_OK;

public class FileBrowserFragment extends Fragment {
    private static final int BROWSER_REQUEST_CODE = 1337;
    private EditText filePath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filebrowser, container, false);
        filePath = view.findViewById(R.id.filepath);
        view.findViewById(R.id.btnbrowse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                intent.setType("application/octet-stream");
                startActivityForResult(intent, BROWSER_REQUEST_CODE);
            }
        });
        view.findViewById(R.id.btnopen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO:
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BROWSER_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                File f = new File(new URI(data.getDataString()));
                filePath.setText(f.getAbsolutePath());
            } catch (URISyntaxException e) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}
