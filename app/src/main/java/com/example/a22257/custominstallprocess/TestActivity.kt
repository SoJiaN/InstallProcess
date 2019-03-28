package com.example.a22257.custominstallprocess

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_constraint_barrier_test)
    }

    override fun onResume() {
        super.onResume()
        val button = findViewById<Button>(R.id.button2)
        button.setOnClickListener { Log.e(TAG, "onClick: ") }
    }

    companion object {
        private val TAG = "TestActivity"
    }
}
