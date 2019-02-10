package de.thwildau.mpekar.binarydroid.ui.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import de.thwildau.mpekar.binarydroid.ContentResolver;
import de.thwildau.mpekar.binarydroid.R;

import static android.app.Activity.RESULT_OK;

/**
 * Allows the user to analyze files from the file system.
 */
public class FileBrowserFragment extends Fragment {
    private static final int BROWSER_REQUEST_CODE = 1337;
    private static final int REQUEST_READ_STORAGE = 1338;
    private EditText filePath;
    private InteractionListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filebrowser, container, false);
        filePath = view.findViewById(R.id.filepath);
        // Start file browser activity when clicking "browse"-button
        view.findViewById(R.id.btnbrowse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                intent.setType("application/octet-stream");
                startActivityForResult(intent, BROWSER_REQUEST_CODE);
            }
        });
        // Open file when clicking open..
        view.findViewById(R.id.btnopen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    tryToOpenFile();
                }
            }
        });
        return view;
    }

    // Actually opens the file. Only call this when you have "read external storage" permissions.
    private void doFileOpen() {
        String path = filePath.getText().toString();
        File f = new File(path);
        if (f.exists()) {
            listener.onSelectFilePath(path);
        }
    }

    // Requests permission to read on external storage if required
    private void tryToOpenFile() {
        boolean hasPermission =
                (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
        } else {
            doFileOpen();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_READ_STORAGE) {
            // Did the user gave us permission?
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doFileOpen();
            } else {
                Toast.makeText(getContext(), R.string.missingreadperm, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Put path of selected file in our TextView
        if (requestCode == BROWSER_REQUEST_CODE && resultCode == RESULT_OK) {
            String path = ContentResolver.getPath(getContext(), data.getData());
            if (path == null || path.isEmpty()) {
                Toast.makeText(getContext(), R.string.invalidpath, Toast.LENGTH_LONG).show();
                return;
            }
            filePath.setText(path);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // our parent has to implement the InteractionListener interface
        if (context instanceof InteractionListener) {
            listener = (InteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement InteractionListener");
        }
    }
}
