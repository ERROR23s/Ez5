package com.example.musicalbum2

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.TableLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.musicalbum2.databinding.ActivitySplashScreenBinding

class SplashScreen : AppCompatActivity() {

    var permissions = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.MODIFY_AUDIO_SETTINGS
    )
    val permissionCode= 1001

    lateinit var bind:ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivitySplashScreenBinding.inflate(layoutInflater)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(bind.root)
        supportActionBar?.hide()

        if (checkPermissions()){
            goHome()
        }else{
            ActivityCompat.requestPermissions(this@SplashScreen,permissions,permissionCode)
        }
    }
    private fun checkPermissions():Boolean{
        for(perms in permissions){
            var data = application.checkCallingOrSelfPermission(perms)
            if(data != PackageManager.PERMISSION_GRANTED){
                return false
            }
        }
        return true
    }
    private fun goHome(){
        Handler().postDelayed({
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }, 5000)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            permissionCode-> {
                if(grantResults.isNotEmpty()&& grantResults[0]==PackageManager.PERMISSION_GRANTED&& grantResults[1]==PackageManager.PERMISSION_GRANTED)
                {
                    goHome()
                    finish()
                }else{
                    Toast.makeText(applicationContext, "Please Grand Permissions",Toast.LENGTH_SHORT).show()
                }

            }else->{
            Toast.makeText(applicationContext, "Error Occured",Toast.LENGTH_SHORT).show()
            finish()
        }
        }
    }

}