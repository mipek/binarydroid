package de.thwildau.mpekar.binarydroid;

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
import de.thwildau.mpekar.binarydroid.assembly.DisassemblerCapstone;
import de.thwildau.mpekar.binarydroid.model.ContainerELF;
import de.thwildau.mpekar.binarydroid.model.SymbolItem;
import de.thwildau.mpekar.binarydroid.ui.disasm.DisasmFragment;
import de.thwildau.mpekar.binarydroid.ui.disasm.DisassemblerFragment;
import de.thwildau.mpekar.binarydroid.ui.disasm.DisassemblerViewModel;
import de.thwildau.mpekar.binarydroid.ui.disasm.HexEditorFragment;
import de.thwildau.mpekar.binarydroid.ui.disasm.SymbolFragment;

public class DisassemblerActivity extends AppCompatActivity
        implements SymbolFragment.OnSymbolSelectListener {
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

                viewModel.setDisasm(new DisassemblerCapstone());

                try {
                    ByteAccessor accessor = new ByteAccessor(file);
                    viewModel.setAccessor(accessor);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    finish();
                }

                try {
                    ElfFile elf = ElfFile.fromFile(file);
                    viewModel.setBinary(new ContainerELF(elf));
                } catch (IOException e) {
                    e.printStackTrace();
                    finish();
                }
            } else {
                Toast.makeText(getApplicationContext(),
                        R.string.binarynotexists, Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        // Register viewpager adapter and install fragment change notifications
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

    @Override
    public void onSymbolSelected(SymbolItem item) {
        DisassemblerViewModel viewModel =
                ViewModelProviders.of(this).get(DisassemblerViewModel.class);

        viewModel.setAddress(item.addr);

        // change active view to disassembler
        pager.setCurrentItem(DisassemblerPagerAdapter.VIEW_DISASM);
    }

    private class DisassemblerPagerAdapter extends FragmentStatePagerAdapter {
        private DisasmFragment[] fragments;

        public static final int VIEW_TOTAL_COUNT = 3;
        public static final int VIEW_HEXEDIT = 0;
        public static final int VIEW_DISASM = 1;
        public static final int VIEW_SYMBOLS = 2;

        public DisassemblerPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new DisasmFragment[VIEW_TOTAL_COUNT];
            fragments[VIEW_HEXEDIT] = new HexEditorFragment();
            fragments[VIEW_DISASM] = new DisassemblerFragment();
            fragments[VIEW_SYMBOLS] = new SymbolFragment();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return fragments[VIEW_HEXEDIT];
                case 1:
                    return fragments[VIEW_DISASM];
                case 2:
                    return fragments[VIEW_SYMBOLS];
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