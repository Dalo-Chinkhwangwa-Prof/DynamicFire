package com.dynamicdevz.dynamicfirebase.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.dynamicdevz.dynamicfirebase.R
import com.dynamicdevz.dynamicfirebase.databinding.ActivityLoginActiivtyBinding
import com.dynamicdevz.dynamicfirebase.model.NewUser
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity(), SignupFragment.SignupDelegate {

    private lateinit var binding: ActivityLoginActiivtyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginActiivtyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signupTextview.setOnClickListener {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.signup_frame, SignupFragment.getInstance())
                .addToBackStack(SignupFragment.getInstance().tag)
                .commit()
        }

        binding.loginButton.setOnClickListener {

            val email = binding.emailEdittext.text.toString().trim()
            val password = binding.passwordEdittext.text.toString().trim()

            Log.d("TAG_X", "Password... '$password'")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                email,
                password
            ).addOnCompleteListener {
                Log.d("TAG_X", "Completed")
                if (it.isSuccessful) {
                    if (FirebaseAuth.getInstance().currentUser?.isEmailVerified == true) {
                        startActivity(Intent(this, MainActivity::class.java).also {
                            it.flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    } else {
                        FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
                        Toast.makeText(
                            this,
                            "Please check your email for verification",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Failed to login: ${it.exception?.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }

    override fun registerUser(newUser: NewUser) {
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(
                newUser.email,
                newUser.password
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
                    Toast.makeText(
                        this,
                        "Please verify your email address.",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    Toast.makeText(this, "Error : oops: ${it.exception}", Toast.LENGTH_SHORT).show()
                    Log.d("TAG_X", "${it.exception}")
                }

            }
    }
}