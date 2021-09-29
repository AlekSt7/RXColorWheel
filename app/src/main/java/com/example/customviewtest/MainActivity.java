package com.example.customviewtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    ViewPager viewPager;
    TabLayout tabLayout;
    LinearLayout wrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        viewPager.setOffscreenPageLimit(3);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        wrapper = findViewById(R.id.wrapper);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                AnimatedColor colorAnim;

                switch (position) {

                    case 0:
                        colorAnim = new AnimatedColor(Color.argb(255, 74,71,79), Color.parseColor("#212e3c"));
                        wrapper.setBackgroundColor(colorAnim.with(positionOffset));
                        break;
                    case 1:
                        colorAnim = new AnimatedColor(Color.parseColor("#212e3c"), Color.parseColor("#FF141417"));
                        wrapper.setBackgroundColor(colorAnim.with(positionOffset));
                        break;
                    case 2:
                        colorAnim = new AnimatedColor(Color.parseColor("#FF141417"), Color.parseColor("#212e3c"));
                        wrapper.setBackgroundColor(colorAnim.with(positionOffset));
                        break;
                }

            }

            @Override
            public void onPageSelected(int position) { }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new DefaultViewFragment(), "Default");
        adapter.addFragment(new MaterialViewFragment(), "Material");
        adapter.addFragment(new AlternativeDesignViewFragment(), "Altermate");
        viewPager.setAdapter(adapter);

    }

}