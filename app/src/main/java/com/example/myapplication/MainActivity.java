package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import android.widget.SeekBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;



public class MainActivity extends AppCompatActivity {

    // Request code for location permissions
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    // Client for receiving location updates from the FusedLocationProvider
    private FusedLocationProviderClient fusedLocationClient;

    // Callback for handling location updates
    private LocationCallback locationCallback;

    // TextView used to display the user's speed
    private TextView speedTextView;

    // Last known location
    private Location lastLocation;

    // Button for pausing and resuming location updates
    private Button pauseButton;

    // Flag indicating whether the location updates are paused
    private boolean isPaused = false;

    // Flag indicating whether synthetic location data should be used
    private boolean useSyntheticLocation = false;

    // Handler for generating synthetic location updates
    private Handler syntheticLocationHandler = new Handler();

    // SeekBar for adjusting the font size of the displayed speed
    private SeekBar fontSizeSeekBar;

    // Button for displaying help information about the app
    private Button helpButton;
    // TextView for displaying the elapsed time
    private TextView elapsedTimeTextView;
    // Start time
    private long startTime;
    // Handler for elapsed time updates
    private Handler elapsedTimeHandler = new Handler();
    // TextView for displaying the distance traveled
    private TextView distanceTraveledTextView;
    // Total distance traveled
    private float totalDistance = 0;
    // TextView for displaying the Altitude
    private TextView altitudeTextView;
    // Spinner for changing the time units
    private Spinner timeUnitSpinner;
    // Spinner for changing the distance units
    private Spinner distanceUnitSpinner;
    // Spinner for changing the speed units
    private Spinner speedUnitSpinner;
    // HandlerThread for location updates with a background looper
    private HandlerThread locationHandlerThread;
    // Handler for location updates.
    private Handler locationHandler;
    // Store the highest speed after reset
    private float highestSpeed;
    // Store the lowest speed after reset
    private float lowestSpeed;
    // Store the highest altitude after reset
    private double highestAltitude;
    // Store the lowest altitude after reset
    private double lowestAltitude;
    // Store the highest distance after reset
    private float highestDistance;
    // Store the shortest distance after reset
    private float lowestDistance;
    // Store the previous speed
    private float previousSpeed = 0;
    // Store the previous altitude
    private double previousAltitude = 0;
    // Store the previous elapsed time
    private long previousElapsedTime = 0;
    // Store the previous distance
    private float previousDistance = 0;
    // TextView for speed change
    private TextView speedChangeTextView;
    // TextView for distance change
    private TextView distanceChangeTextView;
    // TextView for altitude change
    private TextView altitudeChangeTextView;
    // Moving time
    private long movingTimeInSeconds = 0;
    // Handler for moving time
    private Handler movingTimeHandler = new Handler();
    // TextView for moving time
    private TextView movingTimeTextView;
    // Flag to check if device is moving
    private boolean isMoving = false;
    // Array to store all the distance traveled data
    private List<Float> distanceList = new ArrayList<>();
    // Array to store all the speed data
    private List<Float> speedList = new ArrayList<>();
    // Array to store all the altitude data
    private List<Double> altitudeList = new ArrayList<>();





    /**
     * Overrides the onCreate method to initialize the activity when the app is first created
     * @param savedInstanceState: A Bundle object containing the activity's previously saved state
     * https://developer.android.com/training/location/request-updates
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        altitudeTextView = findViewById(R.id.altitudeTextView);
        speedTextView = findViewById(R.id.speedTextView);
        timeUnitSpinner = findViewById(R.id.timeUnitSpinner);
        distanceUnitSpinner = findViewById(R.id.distanceUnitSpinner);
        speedUnitSpinner = findViewById(R.id.speedUnitSpinner);
        locationHandlerThread = new HandlerThread("LocationHandlerThread");
        locationHandlerThread.start();
        locationHandler = new Handler(locationHandlerThread.getLooper());
        populateSpinners();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        movingTimeTextView = findViewById(R.id.movingTimeTextView);
        speedChangeTextView = findViewById(R.id.speedChangeTextView);
        distanceChangeTextView = findViewById(R.id.distanceChangeTextView);
        altitudeChangeTextView = findViewById(R.id.altitudeChangeTextView);

        // Callback for receiving location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateDistanceAndSpeed(location);
                    updateAltitude(location);
                    lastLocation = new Location(location); // update the lastLocation
                }
            }
        };
        // Setup pause button for pausing and resuming location updates
        pauseButton = findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPaused) {
                    if (useSyntheticLocation) {
                        startSyntheticLocationUpdates();
                    } else {
                        startLocationUpdates();
                    }
                    pauseButton.setText("Pause");
                } else {
                    stopLocationUpdates();
                    if (useSyntheticLocation) {
                        stopSyntheticLocationUpdates();
                    }
                    lastLocation = null; // Reset lastLocation on pause
                    updateSpeedDisplay(0.0f); // Reset speed to 0
                    pauseButton.setText("Resume");
                }
                isPaused = !isPaused; // Toggle the paused state
            }
        });


        // Set toggle button for "Use Real Location" and "Use Synthetic Location"
        Button toggleModeButton = findViewById(R.id.toggleModeButton);
        toggleModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useSyntheticLocation = !useSyntheticLocation;
                // If user is using synthetic location, change the toggle button to "Use Real Location" and vice versa
                if (useSyntheticLocation) {
                    stopLocationUpdates(); // Stop real GPS updates
                    toggleModeButton.setText("Use Real Location");
                    startSyntheticLocationUpdates();
                } else {
                    stopSyntheticLocationUpdates(); // Stop synthetic updates
                    resetDistanceTraveled();
                    toggleModeButton.setText("Use Synthetic Location");
                    startLocationUpdates(); // Start real GPS updates
                }
            }
        });

        // Request location permission or start location updates if permission is granted
        requestLocationPermission();

        // Setup a seekbar to change font size
        // https://www.geeksforgeeks.org/android-creating-a-seekbar/#
        fontSizeSeekBar = findViewById(R.id.fontSizeSeekBar);
        fontSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Change the font size of the speed
                float fontSize = 10 + (progress * 0.5f);

                // Display the speed in TextView
                speedTextView.setTextSize(fontSize);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Handle when the user starts dragging the SeekBar thumb
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Handle when the user stops dragging the SeekBar thumb
            }
        });

        // Setup a help button "?" that will display information on how to use the app
        helpButton = findViewById(R.id.helpButton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                // Display app instructions in an alert dialog
                // https://www.geeksforgeeks.org/how-to-create-an-alert-dialog-box-in-android/
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("App Instructions")
                        .setMessage("Welcome to our app! Here's a quick guide on how to use it effectively:\n\n" +
                                "1. Real-Time GPS Data: To access accurate GPS data from your phone, click on the 'Use Real Location' button.\n\n" +
                                "2. Synthetic GPS Data: Alternatively, you can use synthetic GPS data by clicking on the 'Use Synthetic Location' button.\n\n" +
                                "3. Monitoring Speed: The app automatically calculates your speed based on the received location data.\n\n" +
                                "4. Pause and Resume: Click the 'Pause' button to halt speed updates. Click it again to resume real-time tracking.\n\n" +
                                "5. Adjust Font Size: Customize your experience by sliding the seekbar to change the displayed speed font size."
                        )
                        // Create an "OK" button to close the dialog box
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                // Show the dialog box
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        elapsedTimeTextView = findViewById(R.id.elapsedTimeTextView); // TextView for elapsed time
        startTime = System.currentTimeMillis();  // get the current time
        startElapsedTimeUpdates();
        distanceTraveledTextView = findViewById(R.id.distanceTraveledTextView); // TextView for distance traveled

        // Initialize time unit spinner
        timeUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                long elapsedMillis = System.currentTimeMillis() - startTime;
                long elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis);
                updateElapsedTime(elapsedSeconds, position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });

        // Initialize distance unit spinner
        distanceUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                int distanceUnitPreference = distanceUnitSpinner.getSelectedItemPosition();
                updateDistanceTraveled(totalDistance, distanceUnitPreference);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        // Initialize the Reset button
        Button resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset the location
                if (useSyntheticLocation) {
                    stopSyntheticLocationUpdates();
                    startSyntheticLocationUpdates();

                } else {
                    stopLocationUpdates();
                    startLocationUpdates();
                }
                // Reset the metrics
                resetMetrics(v);
            }
        });

        // Initialize Statistics button
        // https://developer.android.com/reference/android/content/Intent
        Button statsButton = findViewById(R.id.statsButton);
        statsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StatsActivity.class);
                startActivity(intent);
            }
        });

    }

    /**
     * Requests location permission from the user
     * If permission is granted, starts location updates
     * If permission is denied, displays a toast message indicating that location permission has been denied
     * @param: None
     * https://developer.android.com/training/location/permissions
     */
    private void requestLocationPermission() {
        // Check if the app lacks location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // If permissions are not granted, request the necessary permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // If permissions are already granted, start receiving location updates
            startLocationUpdates();
        }
    }

    /**
     * Overrides the method to handle the result of the permission request.
     * If the requested location permission is granted, it starts location updates
     * If permission is denied, it displays a toast message indicating that location permission has been denied.
     * @param requestCode Integer code that identifies a specific permission request or activity result
     * @param permissions The requested permissions. Never null.
     * @param grantResults An array of integers that holds the results of the permissions request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Starts receiving location updates
     * Checks for location permission before initiating updates
     * Uses a LocationRequest object to specify the interval and priority of location updates
     * @param: None
     * https://developer.android.com/training/location/request-updates
     */
    private void startLocationUpdates() {
        // Check if the app has location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Create a LocationRequest object to specify the update interval and priority
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Request location updates using the FusedLocationProviderClient
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    /**
     * Overrides the onPause() method of the activity lifecycle
     * Stops location updates when the activity goes into the paused state to conserve resources
     * @param: None
     */
    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates
        if (useSyntheticLocation) {
            stopSyntheticLocationUpdates();
        } else {
            stopLocationUpdates();
        }
        // Stop elapsed time updates
        stopElapsedTimeUpdates();
    }

    /**
     * Overrides the onResume() method of the activity lifecycle
     * Resumes location updates when the activity goes into the paused state to conserve resources
     * Resumes elapsed time updates
     * @param: None
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Start location updates
        if (useSyntheticLocation) {
            startSyntheticLocationUpdates();
        } else {
            startLocationUpdates();
        }
        // Start elapsed time updates
        startElapsedTimeUpdates();
    }

    /**
     * Overrides the onStop() method of the activity lifecycle
     * Stops elapsed time updates
     * @param: None
     */
    @Override
    protected void onStop() {
        super.onStop();
        resetElapsedTime();
        resetDistanceTraveled();
    }

    /**
     * Overrides the onDestroy() method of the activity lifecycle
     * Quits the handler thread for the location updates
     * @param: None
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationHandlerThread.quitSafely();
    }

    /**
     * Stops receiving location updates
     * Removes the location update callback to halt location data retrieval
     * @param: None
     */
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Starts generating synthetic location updates
     * Removes any pending callback messages to halt the generation of synthetic locations
     * @param: None
     */
    private void startSyntheticLocationUpdates() {
        if (lastLocation == null) {
            // Check permissions before accessing getLastLocation
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Location permissions are not granted", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                lastLocation = location;
                            }
                        });
            } catch (SecurityException se) {
                // Handle the exception, e.g., log it or display a toast
                Toast.makeText(this, "Failed to get the last location", Toast.LENGTH_SHORT).show();
            }
        }
        syntheticLocationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Location syntheticLocation = new Location("synthetic");

                if (lastLocation != null) {
                    syntheticLocation.setLatitude(lastLocation.getLatitude());
                    syntheticLocation.setLongitude(lastLocation.getLongitude());
                }

                double deltaLat = 4.4704 * 0.707 / 111000.0;
                double deltaLon = 4.4704 * 0.707 / (111000.0 * Math.cos(Math.toRadians(syntheticLocation.getLatitude())));

                syntheticLocation.setLatitude(syntheticLocation.getLatitude() + deltaLat);
                syntheticLocation.setLongitude(syntheticLocation.getLongitude() + deltaLon);
                syntheticLocation.setSpeed(4.4704f);

                updateDistanceAndSpeed(syntheticLocation); // pass syntheticLocation instead of lastLocation
                updateAltitude(syntheticLocation); // same for altitude update

                syntheticLocationHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    /**
     * Stops generating synthetic location updates
     * Removes any pending callback messages to halt the generation of synthetic locations
     * @param: None
     */
    private void stopSyntheticLocationUpdates() {
        syntheticLocationHandler.removeCallbacksAndMessages(null);
    }

    /**
     * This method uses the Handler to schedule a periodic task
     * every second that calculates and updates the elapsed time.
     * @param: None
     * https://www.geeksforgeeks.org/timeunit-class-in-java-with-examples/
     * https://www.geeksforgeeks.org/tasks-in-real-time-systems/
     **/
    private void startElapsedTimeUpdates() {
        elapsedTimeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long elapsedMillis = System.currentTimeMillis() - startTime;
                long elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis);
                int unitPreference = timeUnitSpinner.getSelectedItemPosition();
                updateElapsedTime(elapsedSeconds, unitPreference);
                elapsedTimeHandler.postDelayed(this, 1000);  // update every second
            }
        }, 100);
    }

    /**
     * Stops the timer
     * @param: None
     */
    private void stopElapsedTimeUpdates() {
        elapsedTimeHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Resets the startTime to current Time
     * @param: None
     */
    private void resetElapsedTime() {
        startTime = System.currentTimeMillis();
    }

    /**
     * Resets the distance traveled
     * @param: None
     */
    private void resetDistanceTraveled() {
        totalDistance = 0;
        distanceTraveledTextView.setText(String.format("Distance Traveled: %.2f m", totalDistance));
        lastLocation = null;
    }

    /**
     * Method that handles the location and speed data
     * @param location - location parameter to get the speed data
     */
    private void updateDistanceAndSpeed(Location location) {
        locationHandler.post(() -> {
            if (isPaused) {
                updateSpeedDisplay(0.0f);
                return;
            }
            if (lastLocation != null) {
                float distanceToCurrentLocation = lastLocation.distanceTo(location);
                totalDistance += distanceToCurrentLocation;
            }
            lastLocation = location;

            float speedMetersPerSecond = location.getSpeed();
            float speedMilesPerHour = speedMetersPerSecond * 2.23694f; // Convert to miles per hour


            speedList.add(speedMilesPerHour); // Add speed to the list
            // Find the highest and non-zero lowest speed from the list
            highestSpeed = Collections.max(speedList);
            Collections.sort(speedList);
            if (speedList.size() > 1) {
                lowestSpeed = speedList.get(1);
            } else if (!speedList.isEmpty()) {
                lowestSpeed = speedList.get(0);
            } else {
                lowestSpeed = 0; // Set to 0 if the list is empty
            }

            distanceList.add(totalDistance); // Add distance to the list
            // Find the highest and lowest non-zero distance from the list
            highestDistance = Collections.max(distanceList);
            Collections.sort(distanceList);
            if (distanceList.size() > 1) {
                lowestDistance = distanceList.get(1);
            } else if (!distanceList.isEmpty()) {
                lowestDistance = distanceList.get(0);
            } else {
                lowestDistance = 0; // Set to 0 if the list is empty
            }

            runOnUiThread(() -> {
                updateSpeedDisplay(speedMilesPerHour);
                updateSpeedChange(speedMilesPerHour, previousSpeed);
                updateDistanceChange(totalDistance, previousDistance);
                int distanceUnitPreference = distanceUnitSpinner.getSelectedItemPosition();
                updateDistanceTraveled(totalDistance, distanceUnitPreference);
                // Update for the next iteration
                previousSpeed = speedMilesPerHour;
                previousDistance = totalDistance;
            });

        });
    }

    /**
     * Method to update the speed display depending on the speed
     * Green for below 10 mph
     * Yellow for between 10 and 20 mph
     * Red for above 20 mph
     * @param speed - The speed the phone is travelling
     */
    private void updateSpeedDisplay(float speed) {
        int speedUnitPreference = speedUnitSpinner.getSelectedItemPosition();
        String speedDisplayText;

        switch (speedUnitPreference) {
            case 0: // Meters per second
                float speedMps = speed / 2.237f;
                speedDisplayText = String.format("%.2f m/s", speedMps);
                break;
            case 1: // Kilometers per hour
                float speedKmph = speed * 1.609f;
                speedDisplayText = String.format("%.2f km/h", speedKmph);
                break;
            case 2: // Miles per hour
                speedDisplayText = String.format("%.2f mph", speed);
                break;
            case 3: // Minutes per mile
                if (speed > 0) {
                    float speedMinutesPerMile = 60 / speed;
                    speedDisplayText = String.format("%.2f min/mile", speedMinutesPerMile);
                } else {
                    speedDisplayText = "0 min/mile";
                }
                break;
            default:
                speedDisplayText = String.format("%.2f mph", speed);
                break;
        }
        if (speed < 10) {
            speedTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.speed_slow));
        } else if (speed < 20) {
            speedTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.speed_moderate));
        } else {
            speedTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.speed_fast));
        }

        speedTextView.setText(speedDisplayText);
    }

    /**
     * Method to update the altitude of the phone
     * @param location - location of the phone (can be synthetic or real location)
     */
    private void updateAltitude(Location location) {
        double altitude = location.getAltitude(); // altitude in meters
        altitudeTextView.setText(String.format("Altitude: %.2f m", altitude));

        // For synthetic location
        if (altitude == 0.0) {
            lowestAltitude = 0;
            highestAltitude = 0;
        }
        // For real location
        else {
            altitudeList.add(altitude); // Add altitude to the list
            // Find the highest and non-zero lowest altitude from the list
            highestAltitude = Collections.max(altitudeList);
            Collections.sort(altitudeList);
            if (altitudeList.size() > 1) {
                lowestAltitude = altitudeList.get(1);
            } else if (!altitudeList.isEmpty()) {
                lowestAltitude = altitudeList.get(0);
            } else {
                lowestAltitude = 0; // Set to 0 if the list is empty
            }
        }

        // Update the altitude change indicator
        updateAltitudeChange(altitude, previousAltitude);
        previousAltitude = altitude;
    }

    /**
     * Method to populate the Spinner classes
     * @param: None
     * https://www.geeksforgeeks.org/spinner-in-android-using-java-with-example/
     * https://www.digitalocean.com/community/tutorials/android-spinner-drop-down-list
     */
    private void populateSpinners() {
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this,
                R.array.time_units, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeUnitSpinner.setAdapter(timeAdapter);

        ArrayAdapter<CharSequence> distanceAdapter = ArrayAdapter.createFromResource(this,
                R.array.distance_units, android.R.layout.simple_spinner_item);
        distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceUnitSpinner.setAdapter(distanceAdapter);

        ArrayAdapter<CharSequence> speedAdapter = ArrayAdapter.createFromResource(this,
                R.array.speed_units, android.R.layout.simple_spinner_item);
        speedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speedUnitSpinner.setAdapter(speedAdapter);
    }

    /**
     * Method to update the elapsed time based on the selected unit
     * @param elapsedSeconds - Units in seconds
     * @param position - Case statement for the user's selected unit
     */
    private void updateElapsedTime(long elapsedSeconds, int position) {
        String displayedTime = "";
        switch (position) {
            case 0: // Seconds
                displayedTime = String.format("Elapsed Time: %ds", elapsedSeconds);
                break;
            case 1: // Minutes
                displayedTime = String.format("Elapsed Time: %dmin", TimeUnit.SECONDS.toMinutes(elapsedSeconds));
                break;
            case 2: // Hours
                displayedTime = String.format("Elapsed Time: %dh", TimeUnit.SECONDS.toHours(elapsedSeconds));
                break;
            case 3: // Days
                long days = TimeUnit.SECONDS.toDays(elapsedSeconds);
                displayedTime = String.format("Elapsed Time: %dd", days);
                break;
            default:
                break;
        }
        elapsedTimeTextView.setText(displayedTime);
    }

    /**
     * Method to update the distance traveled based on the user's selection
     * @param distanceInMeters - Distance traveled
     * @param unitPreference - Case statement for the user's selected unit
     */
    private void updateDistanceTraveled(float distanceInMeters, int unitPreference) {
        String displayText;
        switch (unitPreference) {
            case 0: // Meters
                displayText = String.format("Distance Traveled: %.2f m", distanceInMeters);
                break;
            case 1: // Kilometers
                displayText = String.format("Distance Traveled: %.2f km", distanceInMeters / 1000);
                break;
            case 2: // Miles
                displayText = String.format("Distance Traveled: %.2f miles", distanceInMeters * 0.000621371);
                break;
            case 3: // Feet
                displayText = String.format("Distance Traveled: %.2f ft", distanceInMeters * 3.28084);
                break;
            default:
                displayText = String.format("Distance Traveled: %.2f m", distanceInMeters);
        }
        distanceTraveledTextView.setText(displayText);
    }


    /**
     * Reset button that allows user to reset displayed speed, altitude, distance traveled, and elapsed time
     * @param view
     */
    public void resetMetrics(View view) {
        // Save the current extreme values to SharedPreferences
        saveHighestSpeed(highestSpeed);
        saveLowestSpeed(lowestSpeed);
        saveHighestDistance(highestDistance);
        saveLowestDistance(lowestDistance);
        saveHighestAltitude(highestAltitude);
        saveLowestAltitude(lowestAltitude);

        // Reset elapsed time
        resetElapsedTime();

        // Reset distance traveled
        resetDistanceTraveled();

        // Reset lastLocation to null
        lastLocation = null;

        // Reset speed display to 0
        updateSpeedDisplay(0.0f);

        // Clear the list for the next reset
        distanceList.clear();
        altitudeList.clear();
        speedList.clear();

    }

    /**
     * Method to save highest speed in SharedPreferences
     * @param highestSpeed
     * https://www.geeksforgeeks.org/shared-preferences-in-android-with-examples/#
     */
    private void saveHighestSpeed(float highestSpeed) {
        SharedPreferences preferences = getSharedPreferences("SpeedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("highestSpeed", highestSpeed);
        editor.apply();
    }

    /**
     * Method to save lowest speed in SharedPreferences
     * @param lowestSpeed
     * https://www.geeksforgeeks.org/shared-preferences-in-android-with-examples/#
     */
    private void saveLowestSpeed(float lowestSpeed) {
        SharedPreferences preferences = getSharedPreferences("SpeedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("lowestSpeed", lowestSpeed);
        editor.apply();
    }

    /**
     * Method to save highest altitude in SharedPreferences
     * @param highestAltitude
     * https://www.geeksforgeeks.org/shared-preferences-in-android-with-examples/#
     */
    private void saveHighestAltitude(double highestAltitude) {
        SharedPreferences preferences = getSharedPreferences("AltitudePreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("highestAltitude", (float) highestAltitude);
        editor.apply();
    }

    /**
     * Method to save lowest altitude in SharedPreferences
     * @param lowestAltitude
     * https://www.geeksforgeeks.org/shared-preferences-in-android-with-examples/#
     */
    private void saveLowestAltitude(double lowestAltitude) {
        SharedPreferences preferences = getSharedPreferences("AltitudePreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("lowestAltitude", (float) lowestAltitude);
        editor.apply();
    }

    /**
     * Method to save highest distance in SharedPreferences
     * @param highestDistance
     * https://www.geeksforgeeks.org/shared-preferences-in-android-with-examples/#
     */
    private void saveHighestDistance(float highestDistance) {
        SharedPreferences preferences = getSharedPreferences("DistancePreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("highestDistance", (float) highestDistance);
        editor.apply();
    }

    /**
     * Method to save lowest distance in SharedPreferences
     * @param lowestDistance
     * https://www.geeksforgeeks.org/shared-preferences-in-android-with-examples/#
     */
    private void saveLowestDistance(float lowestDistance) {
        SharedPreferences preferences = getSharedPreferences("DistancePreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("lowestDistance", (float) lowestDistance);
        editor.apply();
    }

    /**
     * Method to display the speed change
     * @param currentSpeed
     * @param previousSpeed
     */
    private void updateSpeedChange(float currentSpeed, float previousSpeed) {
        float speedChange = currentSpeed - previousSpeed;
        String speedChangeText;

        int speedUnitPreference = speedUnitSpinner.getSelectedItemPosition();
        float speedIndicator = 0;

        switch (speedUnitPreference) {
            case 0: // Meters per second
                speedIndicator = speedChange / 2.237f;
                break;
            case 1: // Kilometers per hour
                speedIndicator = speedChange * 1.609f;
                break;
            case 2: // Miles per hour
                speedIndicator = speedChange;
                break;
            case 3: // Minutes per mile
                if (speedChange > 0) {
                    speedIndicator = 60 / speedChange;
                }
                break;
        }

        if (speedChange > 0) {
            speedChangeText = "Speed ↑" + speedIndicator;
        } else if (speedChange < 0) {
            speedChangeText = "Speed ↓" + Math.abs(speedIndicator);
        } else {
            speedChangeText = "Speed Unchanged";
        }
        speedChangeTextView.setText(speedChangeText);
    }

    /**
     * Method to display the distance change
     * @param currentDistance
     * @param previousDistance
     */
    private void updateDistanceChange(float currentDistance, float previousDistance) {
        float distanceChange = currentDistance - previousDistance;
        int distanceUnitPreference = distanceUnitSpinner.getSelectedItemPosition();
        String distanceChangeText;
        float distanceIndicator = 0;

        switch (distanceUnitPreference) {
            case 0: // Meters
                distanceIndicator = distanceChange;
                break;
            case 1: // Kilometers
                distanceIndicator = distanceChange / 1000;
                break;
            case 2: // Miles
                distanceIndicator = (float) (distanceChange * 0.000621371);
                break;
            case 3: // Feet
                distanceIndicator = (float) (distanceChange * 3.28084);
                break;
        }

        if (distanceChange > 0) {
            distanceChangeText = "Distance ↑" + distanceIndicator;
        } else if (distanceChange < 0) {
            distanceChangeText = "Distance ↓" + Math.abs(distanceIndicator);
        } else {
            distanceChangeText = "Distance Unchanged";
        }
        distanceChangeTextView.setText(distanceChangeText);
    }

    /**
     * Method to display the altitude change
     * @param currentAltitude
     * @param previousAltitude
     */
    private void updateAltitudeChange(double currentAltitude, double previousAltitude) {
        double altitudeChange = currentAltitude - previousAltitude;
        String altitudeChangeText;
        if (altitudeChange > 0) {
            altitudeChangeText = "Altitude ↑" + altitudeChange;
        } else if (altitudeChange < 0) {
            altitudeChangeText = "Altitude ↓" + Math.abs(altitudeChange);
        } else {
            altitudeChangeText = "Altitude Unchanged";
        }
        altitudeChangeTextView.setText(altitudeChangeText);
    }

    /**
     * Method to update the moving time
     * @param: None
     */
    private void startMovingTimeUpdates() {
        if (isMoving) {
            return; // Exit if moving time updates are already running
        }
        isMoving = true; // Set the flag to indicate that updates are running
        movingTimeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                movingTimeInSeconds++;  // Increment the moving time
                updateMovingTimeDisplay();  // Update the displayed moving time

                if (!isPaused) {
                    movingTimeHandler.postDelayed(this, 1000);  // Schedule the next update in 1 second
                } else {
                    isMoving = false; // If paused, reset the flag
                }
            }
        }, 1000);
    }
    /**
     * Method to stop the updates of moving time
     * @param: None
     */
    private void stopMovingTimeUpdates() {
        isMoving = false; // Reset the flag
        movingTimeHandler.removeCallbacksAndMessages(null);  // Stop any scheduled updates
    }
    /**
     * Method to update the moving time TextView
     * @param: None
     */
    private void updateMovingTimeDisplay() {
        long hours = movingTimeInSeconds / 3600;
        long minutes = (movingTimeInSeconds % 3600) / 60;
        long seconds = movingTimeInSeconds % 60;

        String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        movingTimeTextView.setText("Moving Time: " + formattedTime);
    }


}
