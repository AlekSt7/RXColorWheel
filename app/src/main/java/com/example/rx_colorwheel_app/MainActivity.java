package com.example.rx_colorwheel_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.res.Resources;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.google.android.material.tabs.TabLayout;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ViewPager viewPager;
    TabLayout tabLayout;
    LinearLayout wrapper;
    Resources resources;

    DefaultViewFragment defaultViewFragment = new DefaultViewFragment();
    MaterialViewFragment materialViewFragment = new MaterialViewFragment();
    AlternativeDesignViewFragment alternativeDesignViewFragment = new AlternativeDesignViewFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_MainTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resources = getResources();

        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        viewPager.setOffscreenPageLimit(3);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        wrapper = findViewById(R.id.wrapper);

        int default_c = getResources().getColor(R.color.fragment_default_background);
        int material_c = getResources().getColor(R.color.fragment_material_background);
        int alternative_c = getResources().getColor(R.color.fragment_alternative_background);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                AnimatedColor colorAnim;

                switch (position) {

                    case 0:
                        colorAnim = new AnimatedColor(default_c, material_c);
                        wrapper.setBackgroundColor(colorAnim.with(positionOffset));
                        break;
                    case 1:
                        colorAnim = new AnimatedColor(material_c, alternative_c);
                        wrapper.setBackgroundColor(colorAnim.with(positionOffset));
                        break;
                    case 2:
                        colorAnim = new AnimatedColor(alternative_c, material_c);
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
        adapter.addFragment(defaultViewFragment, "Default");
        adapter.addFragment(materialViewFragment, "Material");
        adapter.addFragment(alternativeDesignViewFragment, "Altremate");
        viewPager.setAdapter(adapter);

    }

    public String randomText(){
        String random_text[] = resources.getStringArray(R.array.random_text);
        Random random = new Random();
        return random_text[random.nextInt(random_text.length)];
    }

}