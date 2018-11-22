package de.thwildau.mpekar.binarydroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import de.thwildau.mpekar.binarydroid.model.BinaryFile;
import de.thwildau.mpekar.binarydroid.ui.main.BinaryListFragment;

public class MainActivity extends AppCompatActivity implements BinaryListFragment.InteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Add main fragment if this is first creation
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, BinaryListFragment.newInstance())
                    .commitNow();
        }
    }

    @Override
    public void onListFragmentInteraction(BinaryFile item) {
        Intent intent = new Intent(this, DisassemblerActivity.class);
        intent.putExtra(DisassemblerActivity.EXTRA_BINPATH, item.buildPath());
        startActivity(intent);
    }
}
