package com.example.androidmap

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.Surface
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.math.atan2
import kotlin.math.roundToInt

class MainActivity : FragmentActivity(), SensorEventListener {
    private lateinit var compass: TextView
    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null
    private val rotationMatrix = FloatArray(9)
    private val screenRotationMatrix = FloatArray(9)
    private val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        compass = findViewById(R.id.compass)
        sensorManager = getSystemService(SensorManager::class.java)
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)

        val mapContainer = findViewById<View>(R.id.map_screen)
        ViewCompat.setOnApplyWindowInsetsListener(mapContainer) { view, insets ->
            val safeArea = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(safeArea.left, safeArea.top, safeArea.right, safeArea.bottom)
            insets
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            val augustaTech = LatLng(33.418992, -82.048439)
            map.addMarker(
                MarkerOptions()
                    .position(augustaTech)
                    .title(getString(R.string.augusta_tech_name))
                    .snippet(getString(R.string.augusta_tech_address))
            )
            if (savedInstanceState == null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(augustaTech, 16f))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        rotationSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        sensorManager.unregisterListener(this)
        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        val (xAxis, yAxis) = when (compass.display?.rotation) {
            Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
            Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
            Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
            else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
        }
        SensorManager.remapCoordinateSystem(rotationMatrix, xAxis, yAxis, screenRotationMatrix)
        val topEast = screenRotationMatrix[1]
        val topNorth = screenRotationMatrix[4]
        val azimuth = if (topEast * topEast + topNorth * topNorth < 0.01f) {
            // When held upright, the top edge has almost no horizontal heading.
            atan2(-screenRotationMatrix[2], -screenRotationMatrix[5])
        } else {
            atan2(topEast, topNorth)
        }
        if (!azimuth.isFinite()) return

        val heading = Math.toDegrees(azimuth.toDouble())
        val direction = directions[Math.floorMod((heading / 45).roundToInt(), directions.size)]
        if (compass.text != direction) {
            compass.text = direction
            compass.contentDescription = getString(R.string.compass_description, direction)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
}
