package com.example.rx_colorwheel_app;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rxcolorwheel.RXColorWheel;

import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DefaultViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DefaultViewFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    Toast toast;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DefaultViewFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static DefaultViewFragment newInstance(String param1, String param2) {
        DefaultViewFragment fragment = new DefaultViewFragment();
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
        return inflater.inflate(R.layout.fragment_default_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView color_text = getView().findViewById(R.id.color_text);

        RXColorWheel defaultPicker = getView().findViewById(R.id.RxColorPicker);

        defaultPicker.setButtonTouchListener(new RXColorWheel.ButtonTouchListener() {
            @Override
            public void on_cPointerTouch() {
                String random_text[] = getResources().getStringArray(R.array.random_text);
                Random random = new Random();
                toast = Toast.makeText(getContext(), random_text[random.nextInt(random_text.length)], Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void on_excPointerTouch() {
                defaultPicker.setStepperMode(!defaultPicker.getStepperMode());
                if(defaultPicker.getStepperMode()){toast = Toast.makeText(getContext(),"Stepper mode is enabled", Toast.LENGTH_SHORT);}
                else {toast = Toast.makeText(getContext(), "Stepper mode is off", Toast.LENGTH_SHORT);}
                toast.show();
            }
        });

        defaultPicker.setColorChangeListener(new RXColorWheel.ColorChagneListener() {
            @Override
            public void onColorChanged(int color) {
                color_text.setText(String.format("#%06X", (0xFFFFFF & color)));
            }

            @Override
            public void firstDraw(int color) {
                color_text.setText(String.format("#%06X", (0xFFFFFF & color)));
            }
        });

        defaultPicker.setStepperListener(new RXColorWheel.StepperListener() {
            @Override
            public void onStep() {
                Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.EFFECT_HEAVY_CLICK));
                } else {
                    //deprecated in API 26
                    v.vibrate(50);
                }
            }
        });

    }

}