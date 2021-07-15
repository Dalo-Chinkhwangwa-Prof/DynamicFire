package com.dynamicdevz.dynamicfirebase.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dynamicdevz.dynamicfirebase.model.FirePost
import com.dynamicdevz.dynamicfirebase.viewmodel.MainViewModel
import com.dynamicdevz.dynamicfirebase.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val adapter = FirePostAdapter()

    private val viewModel: MainViewModel by viewModels()

    private lateinit var locationManager: LocationManager

    private var imageUrl = ""

    private val locationListener = LocationListener {
        Log.d("TAG_X", "Current Location ${it.latitude}, ${it.longitude} ")
        viewModel.getLocationAddress(it)
    }

    var location = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.locData.observe(this, {

            binding.locationTv.visibility = View.VISIBLE
            it.results[0]?.let { r ->
                binding.locationTv.text = r.formatted_address
                location = r.formatted_address
            }
        })


        firebaseDatabase.setPersistenceEnabled(true)

        binding.mainRv.adapter = adapter

        binding.uploadImageview.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 808)
        }

        binding.uploadButton.setOnClickListener {


            if(imageUrl.isEmpty()) {
                Toast.makeText(this, "Please take a photo.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val timeStamp = Date()
            val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)

            val caption = binding.nameEt.text.toString().trim()

            val firePost = FirePost(
                sdf.format(timeStamp),
                caption,
                imageUrl,
                "Kendy509",
                0,
                location
            )

            firebaseDatabase.reference.child("Posts")
                .push().setValue(firePost)

            val key: String = firebaseDatabase.reference.child("Animals")
                .push().key ?: ""

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

        binding.appCompatButton.setOnClickListener {
            //Implicit Intent tp open settings of current application
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
                val uri = Uri.fromParts("package", packageName, null)
                it.data = uri
            }
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        //1. check if permission is granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //Permission is granted
            registerLocationReceiver()
            binding.uiBlocker.visibility = View.GONE
        } else { //Permission not granted
            //2. Request permission
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            700
        )
    }

    //3. override onRequestPermissionResult
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 700) {
            if (permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION) { //looking for location permission
                //has been granted?
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){ //Yes
                    registerLocationReceiver()

                }else { //No
                    //Ask again!!
                        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                            requestLocationPermission()
                        } else {
                            binding.uiBlocker.visibility = View.VISIBLE

                        }

                }

            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun registerLocationReceiver() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            5000,
            5f,
            locationListener
        )

//        Geocoder

    }


    override fun onStop() {
        super.onStop()
        if(this::locationManager.isInitialized){
            locationManager.removeUpdates(locationListener)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        data?.let {

            val bitmap = it.extras?.get("data") as Bitmap
            binding.uploadImageview.setImageBitmap(bitmap)

            val timeStamp = SimpleDateFormat("HHmmss_MMyyyy", Locale.US).format(Date())

            val storageReference = FirebaseStorage.getInstance().reference.child("uploads/${timeStamp}")

            val byteOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteOutputStream)

            val uploadTask = storageReference.putBytes(byteOutputStream.toByteArray())
            uploadTask.addOnCompleteListener{ task ->

                if(task.isSuccessful){

                    storageReference.downloadUrl.addOnCompleteListener { urlTask ->

                        if(urlTask.isSuccessful){
                            imageUrl = urlTask.result.toString()
                            Log.d("TAG_X", imageUrl)
                        } else {
                            Log.d("TAG_X", "Failed to get the url : ${urlTask.exception?.localizedMessage}")
                        }

                    }


                } else {
                    Log.d("TAG_X", task.exception?.localizedMessage?:"Unknown..")
                }


            }

        }

    }

}