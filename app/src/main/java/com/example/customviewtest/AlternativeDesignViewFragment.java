package com.example.customviewtest;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.rxcolorwheel.RXColorWheel;
import com.google.android.material.button.MaterialButton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AlternativeDesignViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlternativeDesignViewFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AlternativeDesignViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AlternativeDesignViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AlternativeDesignViewFragment newInstance(String param1, String param2) {
        AlternativeDesignViewFragment fragment = new AlternativeDesignViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_alternative_design_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View color_shape = getView().findViewById(R.id.color_shape);

        TextView color_text = getView().findViewById(R.id.color_text);

        GradientDrawable bg = (GradientDrawable) color_shape.getBackground().getCurrent();

        RXColorWheel alternativePicker = getView().findViewById(R.id.AlternativeColorPicker);
        alternativePicker.setColorPalette(new int[]{Color.RED, Color.BLUE, Color.CYAN, Color.YELLOW});

        alternativePicker.addColorChangeListener(new RXColorWheel.ColorChagneListener() {
            @Override
            public void onColorChanged(int color) {
                bg.setColor(color);
                color_text.setText(String.format("#%06X", (0xFFFFFF & color)));
            }
        });

    }
}