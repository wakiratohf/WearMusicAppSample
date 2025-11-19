package com.toh.phone

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView


    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) loadAudio()
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyclerView = RecyclerView(this)
        setContentView(recyclerView)


        recyclerView.layoutManager = LinearLayoutManager(this)

        checkWearableConnect()
        checkPermission()
    }

    private fun checkWearableConnect() {
        if (ApplicationModules.instant.wearableManager?.isWearApiAvailable == false) {
            ApplicationModules.instant.wearableManager?.connect()
        }
    }

    private fun checkPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }


        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            loadAudio()
        } else {
            permissionLauncher.launch(permission)
        }
    }


    private fun loadAudio() {
        val audioList = AudioRepository.getAudioList(this)
        recyclerView.adapter = AudioAdapter(audioList)
    }
}