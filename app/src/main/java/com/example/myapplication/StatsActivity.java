package com.example.myapplication;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class StatsActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private float highestSpeed;
    private float lowestSpeed;
    private double highestAltitude;
    private double lowestAltitude;
    private float highestDistance;
    private float lowestDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // Get the SharedPreferences object for speed data
        SharedPreferences speedPreferences = getSharedPreferences("SpeedPreferences", MODE_PRIVATE);
        highestSpeed = speedPreferences.getFloat("highestSpeed", 0.0f);
        lowestSpeed = speedPreferences.getFloat("lowestSpeed", 0.0f);

        // Get the SharedPreferences object for altitude data
        SharedPreferences altitudePreferences = getSharedPreferences("AltitudePreferences", MODE_PRIVATE);
        highestAltitude = altitudePreferences.getFloat("highestAltitude", 0.0f);
        lowestAltitude = altitudePreferences.getFloat("lowestAltitude", 0.0f);

        // Retrieve lowest speed from SharedPreferences
        SharedPreferences distancePreferences = getSharedPreferences("DistancePreferences", MODE_PRIVATE);
        highestDistance = distancePreferences.getFloat("highestDistance", 0.0f);
        lowestDistance = distancePreferences.getFloat("lowestDistance", 0.0f);

        // Display highest and lowest values in TextViews
        TextView highestSpeedTextView = findViewById(R.id.highestSpeedTextView);
        highestSpeedTextView.setText("Highest Speed: " + highestSpeed + " mph");

        TextView lowestSpeedTextView = findViewById(R.id.lowestSpeedTextView);
        lowestSpeedTextView.setText("Lowest Speed: " + lowestSpeed + " mph");

        TextView highestAltitudeTextView = findViewById(R.id.highestAltitudeTextView);
        highestAltitudeTextView.setText("Highest Altitude: " + highestAltitude + " m");

        TextView lowestAltitudeTextView = findViewById(R.id.lowestAltitudeTextView);
        lowestAltitudeTextView.setText("Lowest Altitude: " + lowestAltitude + " m");

        TextView highestDistanceTextView = findViewById(R.id.highestDistanceTextView);
        highestDistanceTextView.setText("Longest Distance: " + highestDistance + " m");

        TextView lowestDistanceTextView = findViewById(R.id.lowestDistanceTextView);
        lowestDistanceTextView.setText("Shortest Distance: " + lowestDistance + " m");
    }

}
