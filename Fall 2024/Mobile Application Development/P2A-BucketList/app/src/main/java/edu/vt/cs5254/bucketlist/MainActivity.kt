package edu.vt.cs5254.bucketlist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.vt.cs5254.bucketlist.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}