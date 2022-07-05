package com.example.android.fbstorage

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private val TAG = "WallpaperPerMission"
    private val MY_REQUEST_CODE = 111

    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    lateinit var imagePreview: ImageView
    lateinit var btn_choose_image: Button
    lateinit var btn_upload_image: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        makeRequest()
        setupPermission()

        val storage:FirebaseStorage = FirebaseStorage.getInstance()
        val storageref:StorageReference = storage.reference.child("myImages")
        val imageList:ArrayList<item> = ArrayList()

        val listAllTask: Task<ListResult> = storageref.listAll()
        listAllTask.addOnCompleteListener{ result ->
            val items:List<StorageReference> = result.result!!.items
            items.forEachIndexed{ index,item->
                item.downloadUrl.addOnSuccessListener {
                    Log.d("Item","$it")
                    imageList.add(item(it.toString()))
                }.addOnCompleteListener{
                    val recyclerView:RecyclerView = findViewById(R.id.recyclerView)
                    recyclerView.adapter = ImageAdapter(imageList,this)
                    recyclerView.layoutManager = LinearLayoutManager(this)
                }
            }
        }


        btn_choose_image = findViewById(R.id.btn_choose_image)
        btn_upload_image = findViewById(R.id.btn_upload_image)
        imagePreview = findViewById(R.id.image_preview)

        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        btn_choose_image.setOnClickListener { launchGallery() }
        btn_upload_image.setOnClickListener { uploadImage() }
    }

    private fun setupPermission() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.SET_WALLPAPER)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("Permission", "Permission to SET_WALLPAPER denied.")
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SET_WALLPAPER),MY_REQUEST_CODE)

    }

    private fun launchGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data == null || data.data == null){
                return
            }

            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                imagePreview.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage(){
        if(filePath != null){
            val ref = storageReference?.child("myImages/" + UUID.randomUUID().toString())
             ref?.putFile(filePath!!)
            Toast.makeText(this,"Uploaded",Toast.LENGTH_SHORT).show()

        }else{
            Toast.makeText(this, "Please Upload an Image", Toast.LENGTH_SHORT).show()
        }
    }

}