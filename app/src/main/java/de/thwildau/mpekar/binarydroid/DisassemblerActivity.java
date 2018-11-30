package de.thwildau.mpekar.binarydroid;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import net.fornwall.jelf.ElfFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.thwildau.mpekar.binarydroid.assembly.ByteAccessor;
import de.thwildau.mpekar.binarydroid.ui.disasm.DisasmFragment;
import de.thwildau.mpekar.binarydroid.ui.disasm.DisassemblerFragment;
import de.thwildau.mpekar.binarydroid.ui.disasm.DisassemblerViewModel;
import de.thwildau.mpekar.binarydroid.ui.disasm.HexEditorFragment;

public class DisassemblerActivity extends AppCompatActivity {
    public static final String EXTRA_BINPATH = "extra_binpath";

    private ViewPager pager;
    private DisassemblerPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disasm_activity);

        // Initialize our disassembler view model
        if (savedInstanceState == null) {
            String binpath = getIntent().getStringExtra(EXTRA_BINPATH);
            File file = new File(binpath);

            if (file.exists()) {
                DisassemblerViewModel viewModel =
                        ViewModelProviders.of(this).get(DisassemblerViewModel.class);

                try {
                    ByteAccessor accessor = new ByteAccessor(file);
                    viewModel.setAccessor(accessor);
                } catch (FileNotFoundException e) {
                    e.printStackTrace(); //TODO: proper handling
                    finish();
                }

                try {
                    ElfFile elf = ElfFile.fromFile(file);
                    viewModel.setBinary(elf);
                } catch (IOException e) {
                    e.printStackTrace(); //TODO: proper handling
                    finish();
                }

                //getSupportFragmentManager().beginTransaction()
                //        .replace(R.id.container, HexEditorFragment.newInstance())
                //        .commitNow();
            } else {
                // TODO: error msg
                finish();
            }
        }

        // Register viewpager adapter
        pager = findViewById(R.id.container);
        pagerAdapter = new DisassemblerPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

        pager.addOnPageChangeListener (new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                DisasmFragment newFrag = (DisasmFragment)pagerAdapter.getItem(position);
                int currentPosition = pager.getCurrentItem();
                if (currentPosition >= 0) {
                    DisasmFragment oldFrag = (DisasmFragment) pagerAdapter.getItem(currentPosition);
                    oldFrag.onChangeFragment(false);
                }
                newFrag.onChangeFragment(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}

            @Override
            public void onPageScrolled(int position, float arg1, int arg2) {}
        });
    }

    private class DisassemblerPagerAdapter extends FragmentStatePagerAdapter {
        private static final int NUM_VIEWS = 2;
        private DisasmFragment[] fragments;

        public DisassemblerPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new DisasmFragment[NUM_VIEWS];
            fragments[0] = new HexEditorFragment();
            fragments[1] = new DisassemblerFragment();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return fragments[0];
                case 1:
                    return fragments[1];
                default:
                    throw new RuntimeException("unknown position " + position);
            }
        }

        @Override
        public int getCount() {
            return fragments.length;
        }
    }
}