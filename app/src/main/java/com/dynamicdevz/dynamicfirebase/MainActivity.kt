package com.dynamicdevz.dynamicfirebase

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.dynamicdevz.dynamicfirebase.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val adapter = FirePostAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseDatabase.setPersistenceEnabled(true)

        binding.mainRv.adapter = adapter
        binding.uploadImageview.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 808)
        }

        binding.uploadButton.setOnClickListener {

            val timeStamp = Date()
            val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)

            val caption = binding.nameEt.text.toString().trim()

            val firePost = FirePost(
                sdf.format(timeStamp),
                caption,
                "1234566",
                "Kendy509",
                123
            )

            firebaseDatabase.reference.child("Posts")
                .push().setValue(firePost)

            val key:String = firebaseDatabase.reference.child("Animals")
                .push().key?:""

            firebaseDatabase.reference.child("Animals").child(key).setValue(Animals(key, "Alien", "101"))

        }

        firebaseDatabase.reference.child("Posts")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<FirePost>()
                    snapshot.children.forEach {
                        it.getValue(FirePost::class.java)?.let { v ->
                            list.add(v)
                        }
                    }
                    adapter.fireList = list
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        data?.let {

            val bitmap = it.extras?.get("data") as Bitmap
            binding.uploadImageview.setImageBitmap(bitmap)

        }

    }

}