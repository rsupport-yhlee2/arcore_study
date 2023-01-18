package com.example.arcorestudy

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.arcorestudy.databinding.ActivityMainBinding

@Suppress("UNREACHABLE_CODE", "DEPRECATED_IDENTITY_EQUALS")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var renderer: MainRenderer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideStatusBarAndTitleBar()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
    }

    @SuppressLint("SetTextI18n")
    private fun initialize() {
        sessionManager = SessionManager.create(binding.glSurfaceView.context!!.applicationContext)
        sessionManager.create()
        renderer = MainRenderer(sessionManager)
        binding.glSurfaceView.apply {
            preserveEGLContextOnPause = true
            setEGLContextClientVersion(3)
            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
        binding.reset.setOnClickListener {
            sessionManager.cubeScene.clear()
            sessionManager.arObjectScene.clear()
            renderer.distanceLiveData.value = 0f
        }
        renderer.distanceLiveData.observe(this) {
            binding.distance.text = "$it m"
        }
        renderer.planeLiveData.observe(this) {
            binding.plane.text = it
        }
        renderer.mode.observe(this) {
            binding.change.text = it
        }
        binding.change.setOnClickListener {
            if (renderer.mode.value == "arObject") {
                renderer.mode.value = "cube"
            } else {
                renderer.mode.value = "arObject"
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.glSurfaceView.onPause()
        sessionManager.mSession?.pause()
    }

    override fun onResume() {
        super.onResume()
        requestCameraPermission()
        sessionManager.resume()
        binding.glSurfaceView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionManager.destroy()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.onTouch = true
                renderer.getXY(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                renderer.getXY(event.x, event.y)
            }
            MotionEvent.ACTION_UP -> {
                renderer.onTouch = false
            }
        }
        return true
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        }
    }

    private fun hideStatusBarAndTitleBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

}