package edu.vt.cs5254.fancygallery

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavHost
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import edu.vt.cs5254.fancygallery.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        val navHost = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHost

        binding.bottomNav.setupWithNavController(navHost.navController)

        binding.bottomNav.setOnItemReselectedListener { item ->
            NavigationUI.onNavDestinationSelected(item, navHost.navController)
        }

        setContentView(binding.root)
    }
}
