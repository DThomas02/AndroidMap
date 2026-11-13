package com.example.androidmap

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val mapContainer = findViewById<View>(R.id.map)
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
}
