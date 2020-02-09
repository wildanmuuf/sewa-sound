package com.unpam.sewasound;

import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.viewpager.widget.ViewPager;

import android.graphics.Color;
import android.os.Bundle;

import android.view.MenuItem;
import android.widget.TextView;

public class ListTabSingleUser extends AppCompatActivity {
    String namaPelapak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_tab_single_user);
        namaPelapak = getIntent().getStringExtra("nama");

        setTitle("List data Sound "+namaPelapak);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initViews();

    }

    private void initViews() {
        // setting view pager
        ViewPager viewPager = findViewById(R.id.viewPager);
        setupViewPager(viewPager);

        // setting tabLayout
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#00FF00"));
        tabLayout.setSelectedTabIndicatorHeight((int) (5 * getResources().getDisplayMetrics().density));
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        PagerAdapter mainFragmentPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mainFragmentPagerAdapter.addFragment(new ListDataSoundSingleUser(), "Sound");
        mainFragmentPagerAdapter.addFragment(new FavoriteFragment(), "Favorite");
        viewPager.setAdapter(mainFragmentPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        setTitle("List Data Sound " +namaPelapak);
                        break;
                    case 1:
                        setTitle("Favorite");
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;

        }
        return true;
    }
}
