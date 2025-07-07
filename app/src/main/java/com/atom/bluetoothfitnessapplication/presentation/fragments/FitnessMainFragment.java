package com.atom.bluetoothfitnessapplication.presentation.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.atom.bluetoothfitnessapplication.R;
import com.atom.bluetoothfitnessapplication.data.interfaces.PassData;
import com.atom.bluetoothfitnessapplication.data.interfaces.StartStopButtonListener;
import com.atom.bluetoothfitnessapplication.databinding.FragmentFitnessMainBinding;

import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FitnessMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FitnessMainFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "FitnessMainFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentFitnessMainBinding binding;

    private StartStopButtonListener startStopButtonListener;

    public FitnessMainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FitnessMainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FitnessMainFragment newInstance(String param1, String param2) {
        FitnessMainFragment fragment = new FitnessMainFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFitnessMainBinding.inflate(inflater, container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.fitnessMainFragmentStartButton.setOnClickListener(v -> {
            startStopButtonListener.startStopButtonPressed("on");
        });

        binding.fitnessMainFragmentStopButton.setOnClickListener(v -> {
            startStopButtonListener.startStopButtonPressed("off");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //Declared Globally in MainActivity.java
        startStopButtonListener = (StartStopButtonListener) context;
    }
}