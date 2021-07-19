package com.dynamicdevz.dynamicfirebase.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class StartupChooser : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        FirebaseAuth.getInstance().currentUser?.let {
            //User is logged in
            if (it.isEmailVerified)
                startActivity(Intent(this, MainActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            else {
                Toast.makeText(this, "Please!! Verify your email first", Toast.LENGTH_SHORT).show()
                openLogin()
                it.sendEmailVerification()
            }
        } ?: run {
            //User is not logged in
            openLogin()
        }
    }

    private fun StartupChooser.openLogin() {
        startActivity(Intent(this, LoginActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}