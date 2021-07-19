package com.dynamicdevz.dynamicfirebase.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.MenuInflater
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.dynamicdevz.dynamicfirebase.R
import com.dynamicdevz.dynamicfirebase.model.FirePost
import com.dynamicdevz.dynamicfirebase.viewmodel.MainViewModel
import com.dynamicdevz.dynamicfirebase.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val adapter = FirePostAdapter()

    private val viewModel: MainViewModel by viewModels()

    private lateinit var locationManager: LocationManager

    private val pictureResponse = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            uploadImageToFirebaseStrorage()
        } else {
            Toast.makeText(this, "Failed to take photo.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFirebaseStrorage() {
        val bitmap = BitmapFactory.decodeFile(tempDirectory)//it.extras?.get("data") as Bitmap
        binding.uploadImageview.setImageBitmap(bitmap)

        val timeStamp = SimpleDateFormat("HHmmss_MMyyyy", Locale.US).format(Date())

        val storageReference =
            FirebaseStorage.getInstance().reference.child("uploads/${timeStamp}")

        val byteOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteOutputStream)

        val uploadTask = storageReference.putBytes(byteOutputStream.toByteArray())
        uploadTask.addOnCompleteListener { task ->

            if (task.isSuccessful) {

                storageReference.downloadUrl.addOnCompleteListener { urlTask ->

                    if (urlTask.isSuccessful) {
                        imageUrl = urlTask.result.toString()
                        Log.d("TAG_X", imageUrl)
                    } else {
                        Log.d(
                            "TAG_X",
                            "Failed to get the url : ${urlTask.exception?.localizedMessage}"
                        )
                    }
                }
            } else {
                Log.d("TAG_X", task.exception?.localizedMessage ?: "Unknown..")
            }
        }
    }


    private var imageUrl = ""

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(p0: Location) {
            Log.d("TAG_X", "Current Location ${p0.latitude}, ${p0.longitude} ")
            viewModel.getLocationAddress(p0)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            super.onStatusChanged(provider, status, extras)
        }

        override fun onProviderEnabled(provider: String) {
            super.onProviderEnabled(provider)
        }

        override fun onProviderDisabled(provider: String) {
            //super.onProviderDisabled(provider)
        }
    }

    var location = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.menuImageview.setOnClickListener {

            val menu = PopupMenu(this, it)
            val inflater = menu.menuInflater
            inflater.inflate(R.menu.main_menu, menu.menu)
            menu.setOnMenuItemClickListener {

                if (it.itemId == R.id.signout_menu_item) {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, LoginActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }

                true
            }
            menu.show()


        }

        viewModel.locData.observe(this, {

            binding.locationTv.visibility = View.VISIBLE
            it.results[0]?.let { r ->
                binding.locationTv.text = r.formatted_address
                location = r.formatted_address
            }
        })

        binding.mainRv.adapter = adapter

        binding.uploadImageview.setOnClickListener {

            val tempFile = createTemporary()

            val imageURI = FileProvider.getUriForFile(
                this,
                "com.dynamicdevz.file_provider",
                tempFile
            )

            Log.d("TAG_X", "${imageURI}")

            //val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            //intent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI)
//            startActivityForResult(intent, 808)
            pictureResponse.launch(imageURI)
        }

        binding.uploadButton.setOnClickListener {

            if (imageUrl.isEmpty()) {
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

    private var tempDirectory: String = ""

    private fun createTemporary(): File {

        val fileName = SimpleDateFormat("mm_HH_ss_ddMMyyyy", Locale.US).format(Date())

        val directory = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        val file = File.createTempFile(
            fileName,
            ".jpeg",
            directory
        )
        tempDirectory = file.absolutePath
        Log.d("TAG_X", tempDirectory)
        return file
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

        measureTimeMillis { }
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
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) { //Yes
                    registerLocationReceiver()

                } else { //No
                    //Ask again!!
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
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
        if (this::locationManager.isInitialized) {
            locationManager.removeUpdates(locationListener)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("TAG_X", "onActivityResult called")
        data?.let {

            Log.d("TAG_X", "onActivityResult called2")
            val bitmap = BitmapFactory.decodeFile(tempDirectory)//it.extras?.get("data") as Bitmap
            binding.uploadImageview.setImageBitmap(bitmap)

            val timeStamp = SimpleDateFormat("HHmmss_MMyyyy", Locale.US).format(Date())

            val storageReference =
                FirebaseStorage.getInstance().reference.child("uploads/${timeStamp}")

            val byteOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteOutputStream)

            val uploadTask = storageReference.putBytes(byteOutputStream.toByteArray())
            uploadTask.addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    storageReference.downloadUrl.addOnCompleteListener { urlTask ->

                        if (urlTask.isSuccessful) {
                            imageUrl = urlTask.result.toString()
                            Log.d("TAG_X", imageUrl)
                        } else {
                            Log.d(
                                "TAG_X",
                                "Failed to get the url : ${urlTask.exception?.localizedMessage}"
                            )
                        }
                    }
                } else {
                    Log.d("TAG_X", task.exception?.localizedMessage ?: "Unknown..")
                }
            }
        } ?: {
            Log.d("TAG_X", "onActivityResult called null")
        }()
    }

}