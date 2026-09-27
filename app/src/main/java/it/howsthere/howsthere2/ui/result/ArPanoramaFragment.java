package it.howsthere.howsthere2.ui.result;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.Size;
import android.util.SizeF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.camera2.interop.Camera2CameraInfo;
import androidx.camera.camera2.interop.ExperimentalCamera2Interop;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraState;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.LiveData;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.DateFormat;
import java.util.concurrent.ExecutionException;

import it.howsthere.howsthere2.R;

/** Fullscreen camera view sharing the exact panorama/date held by the result activity. */
public class ArPanoramaFragment extends DialogFragment implements SensorEventListener {
    private static final String TAG = "panorama-ar";
    private PreviewView cameraView;
    private ArOverlayView overlay;
    private TextView status;
    private MaterialButton retry;
    private ProcessCameraProvider provider;
    private Preview preview;
    private LiveData<CameraState> cameraStates;
    private DisplayManager displays;
    private final DisplayManager.DisplayListener displayListener = new DisplayManager.DisplayListener() {
        @Override public void onDisplayAdded(int id) { }
        @Override public void onDisplayRemoved(int id) { }
        @Override public void onDisplayChanged(int id) {
            if (preview != null && cameraView != null && cameraView.getDisplay() != null) {
                preview.setTargetRotation(cameraView.getDisplay().getRotation());
                overlay.invalidate();
            }
        }
    };
    private SensorManager sensors;
    private Sensor rotationSensor;
    private final float[] matrix = new float[9];
    private final float[] gravity = new float[3];
    private final float[] magnetic = new float[3];
    private boolean haveGravity, haveMagnetic, haveOrientation, sensorAvailable, lowAccuracy;
    private boolean resumed, startingCamera, permissionAsked;
    private int cameraGeneration;
    private int cameraMessage = R.string.ar_camera_starting;
    private double declination;
    private float headingCorrection, altitudeCorrection, scaleCorrection = 1f;

    private final ActivityResultLauncher<String> permission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                if (getView() == null) return;
                if (granted) startCamera();
                else showCameraError(R.string.ar_permission_denied);
            });

    static void open(Fragment source, boolean moon) {
        if (source.getParentFragmentManager().isStateSaved()
                || source.getParentFragmentManager().findFragmentByTag(TAG) != null) return;
        ArPanoramaFragment fragment = new ArPanoramaFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean("moon", moon);
        fragment.setArguments(arguments);
        fragment.show(source.getParentFragmentManager(), TAG);
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setStyle(STYLE_NO_TITLE, R.style.Theme_Howsthere2_Ar);
        if (state != null) {
            permissionAsked = state.getBoolean("permissionAsked");
            headingCorrection = state.getFloat("heading");
            altitudeCorrection = state.getFloat("altitude");
            scaleCorrection = state.getFloat("scale", 1f);
        }
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        return inflater.inflate(R.layout.fragment_panorama_ar, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, Bundle state) {
        cameraView = view.findViewById(R.id.ar_camera);
        cameraView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
        overlay = view.findViewById(R.id.ar_overlay);
        status = view.findViewById(R.id.ar_status);
        retry = view.findViewById(R.id.ar_retry);
        displays = (DisplayManager) requireContext().getSystemService(android.content.Context.DISPLAY_SERVICE);
        sensors = (SensorManager) requireContext().getSystemService(android.content.Context.SENSOR_SERVICE);
        rotationSensor = sensors.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (rotationSensor == null) rotationSensor = sensors.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        sensorAvailable = rotationSensor != null || (sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
                && sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null);
        boolean moon = requireArguments().getBoolean("moon");
        TextView legend = view.findViewById(R.id.ar_trajectory_legend);
        legend.setText(moon ? R.string.ar_moon_legend : R.string.ar_sun_legend);
        legend.setTextColor(moon ? Color.rgb(215, 202, 255) : Color.rgb(255, 212, 90));
        new ViewModelProvider(requireActivity()).get(ResultViewModel.class).getPanorama()
                .observe(getViewLifecycleOwner(), panorama -> {
                    if (panorama == null) { dismiss(); return; }
                    overlay.setPanorama(panorama, moon);
                    declination = new GeomagneticField((float) panorama.lat, (float) panorama.lon,
                            0, System.currentTimeMillis()).getDeclination();
                    TextView title = view.findViewById(R.id.ar_title);
                    title.setText(getString(moon ? R.string.ar_moon_title : R.string.ar_sun_title,
                            DateFormat.getDateInstance(DateFormat.MEDIUM).format(panorama.date)));
                });
        view.findViewById(R.id.ar_close).setOnClickListener(v -> dismiss());
        View alignment = view.findViewById(R.id.ar_alignment);
        view.findViewById(R.id.ar_align).setOnClickListener(v ->
                alignment.setVisibility(alignment.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));
        Slider heading = view.findViewById(R.id.ar_heading);
        Slider altitude = view.findViewById(R.id.ar_altitude);
        Slider scale = view.findViewById(R.id.ar_scale);
        heading.setValue(headingCorrection);
        altitude.setValue(altitudeCorrection);
        scale.setValue(scaleCorrection);
        heading.addOnChangeListener((slider, value, fromUser) -> { headingCorrection = value; align(); });
        altitude.addOnChangeListener((slider, value, fromUser) -> { altitudeCorrection = value; align(); });
        scale.addOnChangeListener((slider, value, fromUser) -> { scaleCorrection = value; align(); });
        view.findViewById(R.id.ar_reset).setOnClickListener(v -> {
            heading.setValue(0); altitude.setValue(0); scale.setValue(1);
        });
        align();
        retry.setOnClickListener(v -> {
            if (hasCameraPermission()) startCamera();
            else if (permissionAsked && !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + requireContext().getPackageName())));
            } else requestCameraPermission();
        });
        View chrome = view.findViewById(R.id.ar_chrome);
        ViewCompat.setOnApplyWindowInsetsListener(chrome, (v, insets) -> {
            Insets safe = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            return insets;
        });
        View header = view.findViewById(R.id.ar_header);
        View footer = view.findViewById(R.id.ar_footer);
        footer.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) ->
                overlay.setLabelInsets(header.getBottom(), view.getHeight() - footer.getTop()));
        cameraView.getPreviewStreamState().observe(getViewLifecycleOwner(), stream -> {
            boolean ready = stream == PreviewView.StreamState.STREAMING;
            overlay.setCameraReady(ready);
            if (ready) { cameraMessage = 0; retry.setVisibility(View.GONE); updateStatus(); }
        });
        if (!hasCameraPermission() && !permissionAsked) requestCameraPermission();
    }

    private void align() { overlay.setAlignment(headingCorrection, altitudeCorrection, scaleCorrection); }

    @Override public void onStart() {
        super.onStart();
        Window window = requireDialog().getWindow();
        if (window == null) return;
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WindowCompat.setDecorFitsSystemWindows(window, false);
        WindowInsetsControllerCompat bars = WindowCompat.getInsetsController(window, window.getDecorView());
        bars.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        bars.hide(WindowInsetsCompat.Type.systemBars());
    }

    @Override public void onResume() {
        super.onResume();
        resumed = true;
        haveGravity = haveMagnetic = haveOrientation = false;
        overlay.clearOrientation();
        displays.registerDisplayListener(displayListener, null);
        if (rotationSensor != null) {
            sensors.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
        } else if (sensorAvailable) {
            sensors.registerListener(this, sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
            sensors.registerListener(this, sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        }
        if (hasCameraPermission()) startCamera();
        else showCameraError(R.string.ar_permission_denied);
        updateStatus();
    }

    @Override public void onPause() {
        resumed = false;
        displays.unregisterDisplayListener(displayListener);
        sensors.unregisterListener(this);
        stopCamera();
        super.onPause();
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        permissionAsked = true;
        permission.launch(Manifest.permission.CAMERA);
    }

    private void startCamera() {
        if (!resumed || startingCamera || preview != null || !hasCameraPermission()) return;
        startingCamera = true;
        int generation = ++cameraGeneration;
        cameraMessage = R.string.ar_camera_starting;
        retry.setVisibility(View.GONE);
        updateStatus();
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(requireContext());
        future.addListener(() -> {
            if (!resumed || generation != cameraGeneration || cameraView == null) return;
            startingCamera = false;
            try {
                provider = future.get();
                if (!provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                    showCameraError(R.string.ar_no_camera);
                    return;
                }
                Preview useCase = new Preview.Builder().setTargetRotation(cameraView.getDisplay().getRotation()).build();
                useCase.setSurfaceProvider(cameraView.getSurfaceProvider());
                Camera camera = provider.bindToLifecycle(getViewLifecycleOwner(), CameraSelector.DEFAULT_BACK_CAMERA, useCase);
                preview = useCase;
                configureProjection(camera);
                cameraStates = camera.getCameraInfo().getCameraState();
                cameraStates.observe(getViewLifecycleOwner(), cameraState -> {
                    if (cameraState.getError() != null) {
                        stopCamera();
                        showCameraError(R.string.ar_camera_error);
                    }
                });
            } catch (ExecutionException | RuntimeException | androidx.camera.core.CameraInfoUnavailableException error) {
                Log.w(TAG, "Unable to start camera preview", error);
                stopCamera();
                showCameraError(R.string.ar_camera_error);
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                showCameraError(R.string.ar_camera_error);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    @OptIn(markerClass = ExperimentalCamera2Interop.class)
    private void configureProjection(Camera camera) {
        Camera2CameraInfo info = Camera2CameraInfo.from(camera.getCameraInfo());
        Rect active = info.getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        Size pixels = info.getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
        SizeF physical = info.getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
        float[] focal = info.getCameraCharacteristic(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
        Integer orientation = info.getCameraCharacteristic(CameraCharacteristics.SENSOR_ORIENTATION);
        if (active == null || pixels == null || physical == null || focal == null || focal.length == 0
                || physical.getWidth() <= 0 || physical.getHeight() <= 0 || orientation == null) {
            throw new IllegalStateException("Missing camera geometry");
        }
        overlay.setCamera(cameraView, orientation,
                focal[0] * pixels.getWidth() / physical.getWidth(),
                focal[0] * pixels.getHeight() / physical.getHeight(),
                active.exactCenterX(), active.exactCenterY());
    }

    private void stopCamera() {
        cameraGeneration++;
        startingCamera = false;
        if (cameraStates != null && getView() != null) cameraStates.removeObservers(getViewLifecycleOwner());
        cameraStates = null;
        if (provider != null && preview != null) provider.unbind(preview);
        preview = null;
        if (overlay != null) overlay.setCameraReady(false);
    }

    private void showCameraError(int message) {
        cameraMessage = message;
        if (retry != null) {
            retry.setVisibility(View.VISIBLE);
            retry.setText(hasCameraPermission() ? R.string.ar_retry : R.string.ar_allow_camera);
        }
        updateStatus();
    }

    private void updateStatus() {
        if (status == null) return;
        status.setText(cameraMessage != 0 ? cameraMessage : !sensorAvailable ? R.string.ar_no_sensor
                : !haveOrientation ? R.string.ar_waiting_sensor : lowAccuracy ? R.string.ar_calibrate
                : R.string.ar_position_hint);
    }

    @Override public void onSensorChanged(SensorEvent event) {
        if (!resumed || overlay == null) return;
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR
                || event.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(matrix, event.values);
        } else {
            boolean accelerometer = event.sensor.getType() == Sensor.TYPE_ACCELEROMETER;
            float[] target = accelerometer ? gravity : magnetic;
            boolean initialized = accelerometer ? haveGravity : haveMagnetic;
            for (int i = 0; i < 3; i++) target[i] = initialized ? 0.8f * target[i] + 0.2f * event.values[i] : event.values[i];
            if (accelerometer) haveGravity = true; else haveMagnetic = true;
            if (!haveGravity || !haveMagnetic || !SensorManager.getRotationMatrix(matrix, null, gravity, magnetic)) return;
        }
        if (!haveOrientation) { haveOrientation = true; updateStatus(); }
        overlay.setOrientation(matrix, declination);
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) return;
        lowAccuracy = accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW;
        updateStatus();
    }

    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        state.putBoolean("permissionAsked", permissionAsked);
        state.putFloat("heading", headingCorrection);
        state.putFloat("altitude", altitudeCorrection);
        state.putFloat("scale", scaleCorrection);
    }

    @Override public void onDestroyView() {
        stopCamera();
        sensors.unregisterListener(this);
        cameraView = null;
        overlay = null;
        status = null;
        retry = null;
        super.onDestroyView();
    }
}
