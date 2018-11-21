package de.thwildau.mpekar.binarydroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import de.thwildau.mpekar.binarydroid.ui.binarydroid.BinaryDroidFragment;

public class BinaryDroid extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.binary_droid_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, BinaryDroidFragment.newInstance())
                    .commitNow();
        }
    }
}
