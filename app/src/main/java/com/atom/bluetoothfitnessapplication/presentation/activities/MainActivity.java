package com.atom.bluetoothfitnessapplication.presentation.activities;

import android.Manifest;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionScene;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.applandeo.materialcalendarview.CalendarDay;
import com.atom.bluetoothfitnessapplication.R;
import com.atom.bluetoothfitnessapplication.data.interfaces.SensorData;
import com.atom.bluetoothfitnessapplication.data.interfaces.StartStopButtonListener;
import com.atom.bluetoothfitnessapplication.data.models.Backs;
import com.atom.bluetoothfitnessapplication.data.models.ExerciseDescription;
import com.atom.bluetoothfitnessapplication.data.models.Flapjacks;
import com.atom.bluetoothfitnessapplication.data.models.MountainClimbers;
import com.atom.bluetoothfitnessapplication.data.models.Plank;
import com.atom.bluetoothfitnessapplication.data.models.PushUp;
import com.atom.bluetoothfitnessapplication.data.models.SitUps;
import com.atom.bluetoothfitnessapplication.data.models.Skipping;
import com.atom.bluetoothfitnessapplication.data.models.Walking;
import com.atom.bluetoothfitnessapplication.data.models.Weights;
import com.atom.bluetoothfitnessapplication.databinding.ActivityMainBinding;
import com.atom.bluetoothfitnessapplication.presentation.viewmodels.ExerciseViewModel;
import com.atom.bluetoothfitnessapplication.services.BluetoothLeService;
import com.atom.bluetoothfitnessapplication.utilities.Constants;
import com.atom.bluetoothfitnessapplication.utilities.MethodUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;




@SuppressLint("MissingPermission")
public class MainActivity extends AppCompatActivity implements StartStopButtonListener {

    private BluetoothManager bluetoothManager;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning;
    private static final long SCAN_PERIOD = 10000;
    private final int REQUEST_ENABLE_BT = 200;


    private boolean connected;

    private final Handler handler = new Handler();

    private String deviceAddress;

    private BluetoothLeService bluetoothLeService;

    private final ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

            if(bluetoothLeService != null)
            {
                if (!bluetoothLeService.initialize()) {
                    //finish();
                    Log.e(TAG, "onServiceConnected: Unable to initialize Bluetooth");
                }
                if(deviceAddress!=null) {
                    final boolean result = bluetoothLeService.connect(deviceAddress);
                    Log.d(TAG, "onServiceConnected connect request result "+result);
                }
            } else {
                Log.d(TAG, "onServiceConnected: BluetoothLeService unavailable");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothLeService = null;
        }
    };

    private final ScanCallback bleScanCallback = new ScanCallback()
    {
        @RequiresApi(api = Build.VERSION_CODES.S)
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            super.onScanResult(callbackType, result);

            List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
            bluetoothDevices.add(result.getDevice());

            deviceAddress = result.getDevice().getAddress();
            Log.d(TAG, "onScanResult Bluetooth Devices "+bluetoothDevices);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "onScanFailed: "+errorCode);
        }
    };

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            if(BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))
            {
                connected = true;
                Toast.makeText(context, "Bluetooth Device connected",
                        Toast.LENGTH_SHORT).show();
                bluetoothExerciseStatus.setText(R.string.bluetooth_connected);
                bluetoothStatusImageView.setImageResource(R.drawable.round_bluetooth_connected_24);
                Log.d(TAG, "onReceive action 1: "+action);

            } else if(BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                connected = false;
                bluetoothExerciseStatus.setText(R.string.bluetooth_not_connected);
                bluetoothStatusImageView.setImageResource(R.drawable.round_bluetooth_disabled_24);
                Toast.makeText(context, "Bluetooth Device not connected",
                        Toast.LENGTH_SHORT).show();

            } else if(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "onReceive action 2: "+action);
                Log.d(TAG, "gattUpdateReceiver onReceive List of Services: "+
                        bluetoothLeService.getSupportedGattServices());


            } else if(BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                float[] sensorData = intent.getFloatArrayExtra(BluetoothLeService.EXTRA_SENSOR_DATA);


                if((sensorData != null)) {

                    String ax, ay,az, gx, gy, gz;

                    ax = String.valueOf(sensorData[0]);
                    ay = String.valueOf(sensorData[1]);
                    az = String.valueOf(sensorData[2]);

                    gx = String.valueOf(sensorData[3]);
                    gy = String.valueOf(sensorData[4]);
                    gz = String.valueOf(sensorData[5]);

                    String[] sensorDataArray = {ax, ay, az, gx, gy, gz};

                    Log.d(TAG, "onReceive accelerometer: "+ Arrays.toString(sensorData));
                    //Log.d(TAG, "onReceive accelerometer string[]: "+ Arrays.toString(sensorDataArray));


                    saveExerciseData(sensorDataArray);

                }
            }
        }
    };


    private final static String TAG = "MainActivity";
    private ExerciseViewModel exerciseViewModel;

    private CompositeDisposable compositeDisposable;


    private TextInputEditText descriptionTextInputEditText;

    private TextView bluetoothExerciseStatus;

    private TextView timerTextView;

    private Chip pushUpChip;
    private Chip sitUpChip;
    private Chip skippingChip;
    private Chip walkingChip;
    private Chip flapJackChip;
    private Chip weightsChip;
    private Chip backsChip;
    private Chip mountainClimbersChip;
    private Chip plankExerciseChip;
    private MaterialButton stopTimerButton;

    private MaterialButton resetTimerButton;

    private ImageView bluetoothStatusImageView;

    private LocalDateTime localDateTime;
    private String dateOfExercise;

    private MaterialButton clearButton;
    private MaterialButton plotButton;

    private RadioButton accelerometerRadioButton;
    private RadioButton gyroscopeRadioButton;

    private MaterialAutoCompleteTextView exerciseAutoCompleteTextView;
    private MaterialAutoCompleteTextView  accelerationAutoCompleteTextView;
    private TextInputEditText dateTextInputEditText;

    private ViewFlipper viewFlipper;

    private Boolean running = false;
    private Boolean wasRunning = false;
    private int seconds = 0;


    private com.applandeo.materialcalendarview.CalendarView calendarView;

    private LineChart lineChartView;


    private ActivityMainBinding binding;


    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextInputLayout descriptionTextInput = binding.mainActivityExerciseDescriptionTextInputLayout;

        exerciseViewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);

        compositeDisposable = new CompositeDisposable();

        bluetoothExerciseStatus = binding.mainActivityBluetoothExerciseStatusTextView;
        descriptionTextInputEditText = binding.mainActivityExerciseDescriptionTextInputEditText;
        bluetoothStatusImageView = binding.mainActivityBluetoothStatusImageView;

        clearButton = binding.mainActivityClearPlotGraphMaterialButton;
        plotButton = binding.mainActivityPlotGraphMaterialButton;

        exerciseAutoCompleteTextView = binding.mainActivityExerciseTypeDropDownAutoCompleteTextView;
        accelerationAutoCompleteTextView = binding.mainActivityAccelerationDropDownAutoCompleteTextView;

        dateTextInputEditText = binding.mainActivityExerciseDateTextInputEditText;

        calendarView = binding.mainActivityCalenderView;

        timerTextView = binding.mainActivityTimerTextView;

        accelerometerRadioButton = binding.mainActivityAccelerometerRadioButton;
        gyroscopeRadioButton = binding.mainActivityGyroscopeRadioButton;

        viewFlipper = binding.mainActivityViewFlipper;



        localDateTime = LocalDateTime.now();

        lineChartView = binding.mainActivityLineChartView;

        lineChartView.setBackgroundColor(getColor(R.color.white));
        lineChartView.setDrawGridBackground(false);


        if (savedInstanceState!=null) {
            seconds = savedInstanceState.getInt("seconds");
            running = savedInstanceState.getBoolean("running");
            wasRunning = savedInstanceState.getBoolean("wasRunning");
        }


        runTimer();
        displayDaysWhenExercisesHappenedOnCalender();


        plotButton.setOnClickListener(view ->
        {
            String dateOfExercise = dateTextInputEditText.getText().toString().trim();
            String exerciseType = exerciseAutoCompleteTextView.getText().toString().trim();
            String directionType = accelerationAutoCompleteTextView.getText().toString().trim();

            String plotType = "";

            boolean b = !exerciseType.equals("")
                    && !directionType.equals("")
                    && !dateOfExercise.equals("");

            if(accelerometerRadioButton.isChecked())
            {
                plotType = "Acceleration";

                if(b)
                {
                    String dateOfExerciseFormatted = changeDateFormat(dateOfExercise);

                    plotSelectedExerciseData(exerciseType, directionType, plotType, dateOfExerciseFormatted);

                    Toast.makeText(this, "Tap and See your plot if available",
                            Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this,
                            "Please select exercise type, acceleration type and date",
                            Toast.LENGTH_SHORT).show();
                }
            } else if(gyroscopeRadioButton.isChecked()) {

                plotType = "Gyro";
                if(b)
                {
                    String dateOfExerciseFormatted = changeDateFormat(dateOfExercise);

                    plotSelectedExerciseData(exerciseType, directionType, plotType, dateOfExerciseFormatted);

                    Toast.makeText(this, "Tap and See your plot if available",
                            Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this,
                            "Please select exercise type, acceleration type and date",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        clearButton.setOnClickListener(view -> lineChartView.clear());


        String month = localDateTime.getMonth().toString().toUpperCase().charAt(0)
                +localDateTime.getMonth().toString().toLowerCase().substring(1);

        dateOfExercise = String.format("%1s %2s %3s", localDateTime.getDayOfMonth(), month,
                localDateTime.getYear());

        //This Chip Selections
        pushUpChip = binding.mainActivityPushUpChip;
        walkingChip = binding.mainActivityWalkingChip;
        sitUpChip= binding.mainActivitySitUpChip;
        skippingChip = binding.mainActivitySkippingChip;
        flapJackChip = binding.mainActivityFlapJacksChip;
        weightsChip = binding.mainActivityWeightsChip;
        backsChip = binding.mainActivityBacksChip;
        mountainClimbersChip = binding.mainActivityMountainClimbersChip;
        plankExerciseChip = binding.mainActivityPlankChip;

        stopTimerButton = binding.mainActivityStopTimerMaterialButton;
        resetTimerButton = binding.mainActivityResetTimerMaterialButton;

        descriptionTextInput.setEndIconOnClickListener(v -> {

            String descriptionText = Objects.requireNonNull(descriptionTextInputEditText.getText())
                    .toString();
            saveDescriptionData(descriptionText);
        });


        stopTimerButton.setOnClickListener(view -> {
            onStopTimer();
            stopExercise();
        });

        //ResetTimer, does it reset even when the exercise is still continuing? I think so
        resetTimerButton.setOnClickListener(view -> {
            onResetTimer();
            timerTextView.clearComposingText();
        });

        //Bluetooth and permissions below

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        boolean b = bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        String[] permissions = {android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if ((ActivityCompat.checkSelfPermission(this, permissions[0])
                != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this, permissions[1])
                        != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this, permissions[2])
                        != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this, permissions[3])
                        != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this, permissions[4])
                        != PackageManager.PERMISSION_GRANTED) ||
                        (ActivityCompat.checkSelfPermission(this, permissions[5])
                                != PackageManager.PERMISSION_GRANTED)) {

            ActivityCompat.requestPermissions(this, permissions, REQUEST_ENABLE_BT);
        }

        bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();


        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device",
                    Toast.LENGTH_SHORT).show();
        }

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBluetoothIntent);
        }

        scanBLEDevice();
        Log.d(TAG, "onCreate: "+gattUpdateReceiver.getResultCode());
    }

    /**
     * Changes the date format from dd/MM/YYYY into DD MM YYYY
     * For example 04/06/2021 to  04 June 2021
     *
     * @param dateOfExercise date of exercise of the format  dd/MM/YYYY
     * @return the formatted date of the form '04 June 2021' as an example
     */
    private String changeDateFormat(String dateOfExercise)
    {
        String formattedDate = "";

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if(!dateOfExercise.equals(""))
        {
            LocalDate dateSt = LocalDate.parse(dateOfExercise, inputFormatter);

            String month = dateSt.getMonth().toString().toUpperCase().charAt(0)
                    +dateSt.getMonth().toString().toLowerCase().substring(1);

            formattedDate = String.format("%1s %2s %3s", dateSt.getDayOfMonth(), month,
                    dateSt.getYear());
        }

        return formattedDate;
    }

    /**
     * Plots and labels the sensor data points onto the chart view
     *
     * @param entries sensor data points
     * @param label label of the data
     */
    private void plotLineDataOnLineChartView(List<Entry> entries, String label)
    {
        LineData lineData = new LineData();

        LineDataSet lineDataSet = new LineDataSet(entries, label);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawValues(true);
        lineDataSet.setLineWidth(2.5f);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawValues(true);
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);


        lineData.addDataSet(lineDataSet);

        lineChartView.setData(lineData);
    }


    /**
     * Plots sensor data stored in the database of the selected exercise
     * of a single direction in the X, Y or Z direction at a particular date of exercise.
     *
     * @param exerciseType exercise type selected of which the sensor data to be plotted.
     * @param direction a single exercise direction in X, Y or Z
     * @param plotType graph plot selection using acceleration or gyroscope data
     * @param dateOfExercise exercise date
     */
    private void plotSelectedExerciseData(String exerciseType, String direction,
                                 String plotType, String dateOfExercise)
    {
        Disposable disposable;

        List<Entry> entries = new ArrayList<>();

        if(exerciseType.equals(getString(R.string.push_up)))
        {
            disposable = exerciseViewModel.getPushUpDataByDate(dateOfExercise)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(this::isDisposed)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(this::onError)
                    .subscribe(pushUps ->
                    {
                        if(plotType.equals("Acceleration"))
                        {
                            switch (direction) {
                                case "X", "Y", "Z" -> {

                                    plotAccelerationData(pushUps, entries, direction, exerciseType);
                                }
                            }

                        } else if(plotType.equals("Gyro")) {
                            switch (direction) {
                                case "X", "Y", "Z" -> {

                                    plotGyroData(pushUps, entries, direction, exerciseType);
                                }
                            }
                        }
                    });

            compositeDisposable.add(disposable);

        } else if(exerciseType.equals(getString(R.string.sit_up)))
        {
            disposable = exerciseViewModel.getSitUpDataByDate(dateOfExercise)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(this::isDisposed)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(this::onError)
                    .subscribe(sitUps ->
                    {

                        if(plotType.equals("Acceleration"))
                        {
                            switch (direction) {
                                case "X", "Y", "Z" -> {

                                    plotAccelerationData(sitUps, entries, direction, exerciseType);
                                }
                            }

                        } else if(plotType.equals("Gyro")) {
                            switch (direction) {
                                case "X", "Y", "Z" -> {

                                    plotGyroData(sitUps, entries, direction, exerciseType);
                                }
                            }
                        }
                    });

            compositeDisposable.add(disposable);


        } else if(exerciseType.equals(getString(R.string.skipping)))
        {

            disposable = exerciseViewModel.getSkippingDataByDate(dateOfExercise)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(this::isDisposed)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(this::onError)
                    .subscribe(skippings ->
                    {
                        if(plotType.equals("Acceleration"))
                        {
                            switch (direction) {
                                case "X", "Y", "Z" -> {

                                    plotAccelerationData(skippings, entries, direction, exerciseType);
                                }
                            }

                        } else if(plotType.equals("Gyro")) {
                            switch (direction) {
                                case "X", "Y", "Z" -> {

                                    plotGyroData(skippings, entries, direction, exerciseType);
                                }
                            }
                        }
                    });

            compositeDisposable.add(disposable);

        } else if(exerciseType.equals(getString(R.string.walking)))
        {

            disposable = exerciseViewModel.getWalkingDataByDate(dateOfExercise)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(this::isDisposed)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(this::onError)
                    .subscribe(walkings ->
                    {
                        if(plotType.equals("Acceleration"))
                        {
                            switch (direction) {
                                case "X", "Y", "Z" -> {

                                    plotAccelerationData(walkings, entries, direction, exerciseType);
                                }
                            }

                        } else if(plotType.equals("Gyro")) {
                            switch (direction) {
                                case "X", "Y", "Z" -> {

                                    plotGyroData(walkings, entries, direction, exerciseType);
                                }
                            }
                        }
                    });

            compositeDisposable.add(disposable);

        } else if(exerciseType.equals(getString(R.string.flap_jack)))
        {

            disposable = exerciseViewModel.getFlapJackDataByDate(dateOfExercise)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(this::isDisposed)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(this::onError)
                    .subscribe(flapjacks ->
                    {

                        if(plotType.equals("Acceleration"))
                        {
                            switch (direction) {
                                case "X", "Y", "Z" -> {

                                    plotAccelerationData(flapjacks, entries, direction, exerciseType);
                                }
                            }

                        } else if(plotType.equals("Gyro")) {
                            switch (direction) {
                                case "X", "Y", "Z" -> {

                                    plotGyroData(flapjacks, entries, direction, exerciseType);
                                }
                            }
                        }
                    });

            compositeDisposable.add(disposable);


        } else if(exerciseType.equals(getString(R.string.weights)))
        {

            disposable = exerciseViewModel.getWeightsDataByDate(dateOfExercise)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(this::isDisposed)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(this::onError)
                    .subscribe(weights ->
                    {
                        if(plotType.equals("Acceleration"))
                        {
                            switch (direction) {
                                case "X", "Y", "Z" -> {

                                    plotAccelerationData(weights, entries, direction, exerciseType);
                                }
                            }

                        } else if(plotType.equals("Gyro")) {
                            switch (direction) {
                                case "X", "Y", "Z" -> {

                                    plotGyroData(weights, entries, direction, exerciseType);
                                }
                            }
                        }
                    });

            compositeDisposable.add(disposable);

        } else if(exerciseType.equals(getString(R.string.backs)))
        {

            disposable = exerciseViewModel.getBackDataByDate(dateOfExercise)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(this::isDisposed)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(this::onError)
                    .subscribe(backs ->
                    {
                        if(plotType.equals("Acceleration"))
                        {
                            switch (direction) {
                                case "X", "Y", "Z" -> {

                                    plotAccelerationData(backs, entries, direction, exerciseType);
                                }
                            }

                        } else if(plotType.equals("Gyro")) {
                            switch (direction) {
                                case "X", "Y", "Z" -> {

                                    plotGyroData(backs, entries, direction, exerciseType);
                                }
                            }
                        }
                    });

            compositeDisposable.add(disposable);

        } else if(exerciseType.equals(getString(R.string.mt_climbers)))
        {

            disposable = exerciseViewModel.getMountainClimberDataByDate(dateOfExercise)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(this::isDisposed)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(this::onError)
                    .subscribe(mountainClimbers ->
                    {
                        if(plotType.equals("Acceleration"))
                        {
                            switch (direction) {
                                case "X", "Y", "Z" -> {

                                    plotAccelerationData(mountainClimbers, entries, direction, exerciseType);
                                }
                            }

                        } else if(plotType.equals("Gyro")) {
                            switch (direction) {
                                case "X", "Y", "Z" -> {

                                    plotGyroData(mountainClimbers, entries, direction, exerciseType);
                                }
                            }
                        }

                    });

            compositeDisposable.add(disposable);

        } else if(exerciseType.equals(getString(R.string.plank)))
        {

            disposable = exerciseViewModel.getPlankDataByDate(dateOfExercise)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(this::isDisposed)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(this::onError)
                    .subscribe(planks -> {

                        if(plotType.equals("Acceleration"))
                        {
                            switch (direction) {
                                case "X", "Y", "Z" -> {

                                    plotAccelerationData(planks, entries, direction, exerciseType);
                                }
                            }

                        } else if(plotType.equals("Gyro")) {
                            switch (direction) {
                                case "X", "Y", "Z" -> {

                                    plotGyroData(planks, entries, direction, exerciseType);
                                }
                            }
                        }
                    });

            compositeDisposable.add(disposable);

        }
        else {
            Log.d(TAG, "getExerciseData: "+ R.string.push_up);
            Toast.makeText(this, "No exercise type selected or invalid exercise type",
                    Toast.LENGTH_SHORT).show();

        }

    }


    /**
     * Saves components of a string array of a chip selected
     * (or checked) exercise onto the database.
     *
     * @param exerciseData a string array of sensor data of the form
     *                     [accX, accY, accZ, gyroX, gyroY, gyroZ]
     */
    private void saveExerciseData(String[] exerciseData)
    {
        Disposable disposable;

        if(pushUpChip.isChecked())
        {
            onStartTimer();

            disposable = exerciseViewModel.insertPushUp(
                            new PushUp(exerciseData[0],exerciseData[1],exerciseData[2],
                                    exerciseData[3],exerciseData[4],exerciseData[5],
                                    LocalTime.now().toString(), dateOfExercise))
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(this::isDisposed)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(this::onError)
                    .doOnComplete(() -> Log.d(TAG, "saveExerciseData: completed"))
                    .subscribe();

            Toast.makeText(this, R.string.push_up_exercise, Toast.LENGTH_SHORT).show();

            compositeDisposable.add(disposable);

        } else if(sitUpChip.isChecked()) {

            onStartTimer();

            disposable = exerciseViewModel.insertSitUps(new SitUps(
                            exerciseData[0],exerciseData[1],exerciseData[2],
                            exerciseData[3],exerciseData[4],exerciseData[5],
                            LocalTime.now().toString(), dateOfExercise))
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(this::isDisposed)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(this::onError)
                    .doOnComplete(() -> Log.d(TAG, "saveExerciseData: completed"))
                    .subscribe();

            Toast.makeText(this, R.string.sit_up_exercise, Toast.LENGTH_SHORT).show();

            compositeDisposable.add(disposable);

        } else if(skippingChip.isChecked()) {

            onStartTimer();

            disposable = exerciseViewModel.insertSkipping(new Skipping(
                            exerciseData[0],exerciseData[1],exerciseData[2],
                            exerciseData[3],exerciseData[4],exerciseData[5],
                            LocalTime.now().toString(), dateOfExercise))
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(this::isDisposed)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(this::onError)
                    .doOnComplete(() -> Log.d(TAG, "saveExerciseData: completed"))
                    .subscribe();

            Toast.makeText(this, R.string.skipping_exercise, Toast.LENGTH_SHORT).show();

            compositeDisposable.add(disposable);

        } else if(walkingChip.isChecked()) {

            onStartTimer();

            disposable = exerciseViewModel.insertWalking(new Walking(
                            exerciseData[0],exerciseData[1],exerciseData[2],
                            exerciseData[3],exerciseData[4],exerciseData[5],
                            LocalTime.now().toString(), dateOfExercise))
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(this::isDisposed)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(this::onError)
                    .doOnComplete(() -> Log.d(TAG, "saveExerciseData: completed"))
                    .subscribe();

            Toast.makeText(this, R.string.walking_exercise_on_going, Toast.LENGTH_SHORT).show();

            compositeDisposable.add(disposable);

        } else if(flapJackChip.isChecked()) {

            onStartTimer();

            disposable = exerciseViewModel.insertFlapJacks(new Flapjacks(
                            exerciseData[0],exerciseData[1],exerciseData[2],
                            exerciseData[3],exerciseData[4],exerciseData[5],
                            LocalTime.now().toString(), dateOfExercise))
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(this::isDisposed)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(this::onError)
                    .doOnComplete(() -> Log.d(TAG, "saveExerciseData: completed"))
                    .subscribe();

            Toast.makeText(this, R.string.flap_jack_exercise, Toast.LENGTH_SHORT).show();

            compositeDisposable.add(disposable);

        } else if(weightsChip.isChecked()) {

            onStartTimer();

            disposable = exerciseViewModel.insertWeights(new Weights(
                            exerciseData[0],exerciseData[1],exerciseData[2],
                            exerciseData[3],exerciseData[4],exerciseData[5],
                            LocalTime.now().toString(), dateOfExercise))
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(this::isDisposed)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(this::onError)
                    .doOnComplete(() -> Log.d(TAG, "saveExerciseData: completed"))
                    .subscribe();

            Toast.makeText(this, R.string.weights_exercise_on_going, Toast.LENGTH_SHORT).show();

            compositeDisposable.add(disposable);

        } else if(backsChip.isChecked()) {

            onStartTimer();

            disposable = exerciseViewModel.insertBacks(new Backs(
                            exerciseData[0],exerciseData[1],exerciseData[2],
                            exerciseData[3],exerciseData[4],exerciseData[5],
                            LocalTime.now().toString(), dateOfExercise))
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(this::isDisposed)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(this::onError)
                    .doOnComplete(() -> Log.d(TAG, "saveExerciseData: completed"))
                    .subscribe();

            Toast.makeText(this, R.string.backs_exercise, Toast.LENGTH_SHORT).show();

            compositeDisposable.add(disposable);

        } else if(mountainClimbersChip.isChecked()) {

            onStartTimer();

            disposable = exerciseViewModel.insertMountainClimbers(new MountainClimbers(
                            exerciseData[0],exerciseData[1],exerciseData[2],
                            exerciseData[3],exerciseData[4],exerciseData[5],
                            LocalTime.now().toString(), dateOfExercise))
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(this::isDisposed)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(this::onError)
                    .doOnComplete(() -> Log.d(TAG, "saveExerciseData: completed"))
                    .subscribe();

            Toast.makeText(this, R.string.mountain_climbers_exercise, Toast.LENGTH_SHORT).show();

            compositeDisposable.add(disposable);

        } else if(plankExerciseChip.isChecked()) {

            onStartTimer();

            disposable = exerciseViewModel.insertPlank(new Plank(
                            exerciseData[0],exerciseData[1],exerciseData[2],
                            exerciseData[3],exerciseData[4],exerciseData[5],
                            LocalTime.now().toString(), dateOfExercise))
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(this::isDisposed)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(this::onError)
                    .doOnComplete(() -> Log.d(TAG, "saveExerciseData: completed"))
                    .subscribe();

            Toast.makeText(this, R.string.plank_exercise_on_going, Toast.LENGTH_SHORT).show();

            compositeDisposable.add(disposable);

        }
    }


    /**
     * Saves and stores exercise description of the user onto the database.
     *
     * @param description description of the exercise and additional information
     *                    describing the current conditions of the exercise they performing.
     */
    private void saveDescriptionData(String description)
    {
        if(!description.equals("")) {

            Disposable descriptionDescription = exerciseViewModel
                    .insertExerciseDescription(
                            new ExerciseDescription(LocalDateTime.now().toString(), description))
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(this::isDisposed)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(this::onError)
                    .doOnComplete(() -> {
                        Toast.makeText(this, "Exercise Description has been saved!",
                                Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "saveDescriptionData: completed");})
                    .subscribe();

            compositeDisposable.add(descriptionDescription);

        } else {
            Toast.makeText(this, "No exercise description", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Display the days of which the user exercised on by gathering all the dates of exercise
     * descriptions that are stored in database and display them dates on the calender view.
     */
    private void displayDaysWhenExercisesHappenedOnCalender()
    {

        Disposable disposableDescription = exerciseViewModel
                .getAllExerciseDescriptions()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(this::isDisposed)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(this::onError)
                .doOnComplete(() -> Log.d(TAG, "getExerciseDescriptionData: Completed"))
                .subscribe(exerciseDescriptions ->
                {

                    if(!exerciseDescriptions.isEmpty())
                    {
                        List<String> getDates = new ArrayList<>();
                        List<CalendarDay> calendarDays = new ArrayList<>();
                        List<Calendar> calendars = new ArrayList<>();

                        for (ExerciseDescription exerciseDescription : exerciseDescriptions)
                        {

                            getDates.add(exerciseDescription.getDateTime().substring(0, 10));

                            getDates = getDates.stream().distinct().collect(Collectors.toList());

                        }

                        for(String dateString : getDates)
                        {
                            String[] splitDates = dateString.split("-");

                            int year = Integer.parseInt(splitDates[0]);
                            int month = Integer.parseInt(splitDates[1]);
                            int day = Integer.parseInt(splitDates[2]);

                            Calendar calendar = Calendar.getInstance();
                            calendar.set(year, month-1, day);

                            calendars.add(calendar);

                        }

                        calendarView.setSelectedDates(calendars);
                        calendarView.setSelected(true);


                    } else {
                        Log.d(TAG, "getExerciseDescriptionData:" +
                                " No dates available, therefore no dates " +
                                "exercised shown on the calender");
                    }
                });

        compositeDisposable.add(disposableDescription);
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    /**
     * Saves current second and the current boolean state running and wasRunning object
     *
     * @param outState a bundle object to store current seconds
     *                 and current boolean running and wasRunning
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("seconds", seconds);
        outState.putBoolean("running", running);
        outState.putBoolean("wasRunning", wasRunning);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);

        wasRunning = running;
        running = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        compositeDisposable.clear();
    }

    /**
     * Resumes bluetooth scanning, connection and timer
     * after a change in screen orientation or a call popping up
     */

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter(),
                Context.RECEIVER_NOT_EXPORTED);

        if(bluetoothLeService != null)
        {
            if(deviceAddress != null)
            {
                final boolean result = bluetoothLeService.connect(deviceAddress);
                Log.d(TAG, "onResume Connect request result= "+result);
            }
        }

        if(wasRunning)
        {
            running = true;
        }
    }


    private static IntentFilter makeGattUpdateIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);

        return intentFilter;
    }

    /**
     * Scans for bluetooth devices for a certain period of time
     */
    private void scanBLEDevice()
    {
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(() -> {
                scanning = false;
                bluetoothLeScanner.stopScan(bleScanCallback);
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothLeScanner.startScan(bleScanCallback);

        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(bleScanCallback);
        }
    }


    /**
     * Displays Bluetooth Gatt services coming from the sensor connected to the app
     * through bluetooth and passes a characteristic object to the BLE Service
     *
     * @param gattServices gatt services coming from the bluetooth sensor
     */
    private void displayGattServices(List<BluetoothGattService> gattServices)
    {
        BluetoothGattService service = null;

        BluetoothGattCharacteristic characteristic = null;

        for (BluetoothGattService gattService: gattServices)
        {
            if(UUID.fromString(Constants.RP2040_SERVICE_UUID).equals(gattService.getUuid()))
            {
                service = gattService;
                characteristic = service
                        .getCharacteristic(UUID
                                .fromString(Constants.RP2040_GYRO_CHARACTERISTICS_UUID));

                bluetoothLeService.readCharacteristic(characteristic);
            }
        }
    }

    /**
     * Method not related to the main activity an is a dud method that does not affect
     * the overall functionality of the app
     */
    @Override
    public void startStopButtonPressed(String buttonStatus)
    {
        Log.d(TAG, "startStopButtonPressed: "+buttonStatus);
        bluetoothLeService.writeCharacteristicToBLEDevice(buttonStatus);
    }


    /**
     * Checks if the object disposable is disposed and if not
     * it gets added to composite disposable object
     * @param disposable an disposable object
     */
    private void isDisposed(Disposable disposable) {
        if (!disposable.isDisposed()) {
            compositeDisposable.add(disposable);
        }
    }


    /**
     * Throws an error, resets the timer and dispose a
     * disposable object to stop running in the background
     * @param throwable an object containing the message of the error
     */
    private void onError(Throwable throwable) {
        Log.e(TAG, "error: ", throwable);
        onResetTimer();
        compositeDisposable.dispose();
    }


    /**
     * Starts the timer by setting running to true
     */
    private void onStartTimer()
    {
        running = true;
    }

    /**
     * Stops the timer by setting running to false
     */
    private void onStopTimer()
    {
        running = false;
    }

    /**
     * Resets the timer by making seconds equals to 0 and set running to false
     */
    private void onResetTimer()
    {
        running = false;
        seconds = 0;
    }

    /**
     * A timer that runs in the background and displays on the textview
     * in the main UI every second
     */
    private void runTimer()
    {
        final Handler handler = new Handler();

        handler.post(new Runnable() {

            @Override
            public void run()
            {
                int hours = seconds/3600;
                int minutes = (seconds%3600)/60;
                int secs = seconds%60;

                String time = String.format(Locale.getDefault(),
                        "%02d : %02d : %02d", hours, minutes, secs);

                timerTextView.setText(time);

                if(running){
                    seconds++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    /**
     * Set the selected chip from being checked to unchecked.
     * It similar to a user presses the chip to start and
     * presses again to stop the exercise
     */
    private void stopExercise() {

        List<Chip> chips = new ArrayList<>();
        chips.add(pushUpChip);
        chips.add(walkingChip);
        chips.add(sitUpChip);
        chips.add(skippingChip);
        chips.add(flapJackChip);
        chips.add(weightsChip);
        chips.add(backsChip);
        chips.add(mountainClimbersChip);
        chips.add(plankExerciseChip);

        chips.forEach(chip -> {
            if(chip.isChecked()) chip.setChecked(false);
        });
    }


    /**
     * It loops through a list of sensor data of a particular exercise ,
     * gets the acceleration value of the direction chosen , create an entry object
     * and add them into a list of type Entry (List<Entry>)
     *
     * @param sensorData a list of sensor data of a particular/selected exercise type
     * @param entries an empty initialized Entry List
     * @param direction X, Y or Z direction
     * @param exerciseType type of exercise selected such as push ups, sit ups etc..
     */
    private void plotAccelerationData(List<? extends SensorData> sensorData, List<Entry> entries,
                                      String direction, String exerciseType)
    {

        for (SensorData data : sensorData)
        {
            long id = data.getId();

            String valueString = switch (direction)
            {
                case "X" -> data.getAccX();
                case "Y" -> data.getAccY();
                case "Z" -> data.getAccZ();
                default -> "0";
            };

            float value = Float.parseFloat(valueString);
            entries.add(new Entry((float) id, value));

        }

        reduceAndPlotEntries(entries, exerciseType);

    }

    /**
     * It loops through a list of sensor data of a particular exercise ,
     * gets the gyro value of the direction chosen , create an entry object
     * and add them into a list of type Entry (List<Entry>)
     *
     * @param sensorData a list of sensor data of a particular/selected exercise type
     * @param entries an empty initialized Entry List
     * @param direction X, Y or Z direction
     * @param exerciseType type of exercise selected such as push ups, sit ups etc..
     */
    private void plotGyroData(List<? extends SensorData> sensorData, List<Entry> entries,
                              String direction, String exerciseType)
    {

        for (SensorData data : sensorData)
        {
            long id = data.getId();

            String valueString = switch (direction)
            {
                case "X" -> data.getGyroX();
                case "Y" -> data.getGyroY();
                case "Z" -> data.getGyroZ();
                default -> "0";
            };

            float value = Float.parseFloat(valueString);
            entries.add(new Entry((float) id, value));

        }

        reduceAndPlotEntries(entries, exerciseType);
    }

    /**
     * The method below uses the Largest Triangle Tree Buckets( LTTB ) method
     * to downsample the data in the background, reduce it from 10 000+ data points
     * to a maximum 500 data points to maintain visual similarity to the original data
     * and plot the reduced data points without the app experiencing
     * ANR (Application Not Responding)
     *
     * @param entries list of time series data points
     * @param exerciseType type of exercise chosen by the user
     */
    private void reduceAndPlotEntries(List<Entry> entries, String exerciseType)
    {
        Disposable disposable = Single.just(MethodUtils.downsample(entries, 500))
                  .subscribeOn(Schedulers.computation())
                  .doOnSubscribe(this::isDisposed)
                  .observeOn(AndroidSchedulers.mainThread())
                  .doOnError(this::onError)
                  .subscribe(entries1 -> {
                      plotLineDataOnLineChartView(entries1, exerciseType);
                  });

        compositeDisposable.add(disposable);
    }
}