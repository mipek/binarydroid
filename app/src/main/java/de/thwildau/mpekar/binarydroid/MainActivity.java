package de.thwildau.mpekar.binarydroid;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import de.thwildau.mpekar.binarydroid.model.BinaryFile;
import de.thwildau.mpekar.binarydroid.ui.main.BinaryListFragment;
import de.thwildau.mpekar.binarydroid.ui.main.FileBrowserFragment;
import de.thwildau.mpekar.binarydroid.ui.main.SymbolSearchFragment;

public class MainActivity extends AppCompatActivity implements BinaryListFragment.InteractionListener {
    MainPagerAdapter pagerAdapter;
    ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        System.setProperty("jna.debug_load.jna", "true");
        Log.d("BinaryDroid", "Resource path:"  + ClassLoader.getSystemClassLoader().toString());

        pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        pager = (ViewPager) findViewById(R.id.container);
        pager.setAdapter(pagerAdapter);

        // Add main fragment if this is first creation
        //if (savedInstanceState == null) {
        //    getSupportFragmentManager().beginTransaction()
        //            .replace(R.id.container, BinaryListFragment.newInstance())
        //            .commitNow();
        //}

        // Add tabs to the action bar
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        android.support.v7.app.ActionBar.TabListener tabListener = new android.support.v7.app.ActionBar.TabListener() {
            @Override
            public void onTabSelected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {

            }
        };

        pager.setOnPageChangeListener(
            new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    // When swiping between pages, select the corresponding tab.
                    getSupportActionBar().setSelectedNavigationItem(position);
                }
            });

        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(pagerAdapter.getItemName(i))
                            .setTabListener(tabListener));
        }
    }

    @Override
    public void onListFragmentInteraction(BinaryFile item) {
        Intent intent = new Intent(this, DisassemblerActivity.class);
        intent.putExtra(DisassemblerActivity.EXTRA_BINPATH, item.buildPath());
        startActivity(intent);
    }

    private class MainPagerAdapter extends FragmentPagerAdapter {
        private BinaryListFragment binaryListFragment;
        private FileBrowserFragment fileBrowserFragment;
        private SymbolSearchFragment symbolSearchFragment;

        public MainPagerAdapter(FragmentManager fm) {
            super(fm);

            binaryListFragment = new BinaryListFragment();
            fileBrowserFragment = new FileBrowserFragment();
            symbolSearchFragment = new SymbolSearchFragment();
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return binaryListFragment;
                case 1:
                    return fileBrowserFragment;
                case 2:
                    return symbolSearchFragment;
                default:
                    return null;
            }
        }

        public String getItemName(int position) {
            Resources resources = getApplicationContext().getResources();
            switch(position) {
                case 0:
                    return resources.getString(R.string.tab_installedapps);
                case 1:
                    return resources.getString(R.string.tab_filebrowser);
                case 2:
                    return resources.getString(R.string.tab_symbolsearch);
                default:
                    throw new IllegalArgumentException("position is out of range");
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
