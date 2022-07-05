package com.example.android.fbstorage

import android.app.Dialog
import android.app.WallpaperManager
import android.content.ClipData
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class ImageAdapter(private var items:List<item>,private val context:Context):
    RecyclerView.Adapter<ImageAdapter.ViewHolder> (){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageAdapter.ViewHolder {
       return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item,parent,false))
    }

    override fun onBindViewHolder(holder: ImageAdapter.ViewHolder, position: Int) {
        val item:item = items[position]
        Picasso.get().load(item.imageUrl).into(holder.imageView)
        holder.imageView.setOnClickListener{
            setupDialog(item)
        }
    }



    override fun getItemCount(): Int {
      return  items.size
    }

    class ViewHolder (view: View):RecyclerView.ViewHolder(view){
        val imageView:ImageView = view.findViewById(R.id.imageView)
    }
    private fun setupDialog(item:item) {
        val dialog = Dialog(context,R.style.DialogStyle)
        dialog.setContentView(R.layout.dialog_wallpaper)
        val dialogImageView = dialog.findViewById<ImageView>(R.id.dialogImageView)
        val setWallpaperBtn = dialog.findViewById<Button>(R.id.setWallpaperBtn)
        val dialogProgressBar = dialog.findViewById<ProgressBar>(R.id.dialogProgressBar)

        dialogProgressBar.visibility = View.VISIBLE
        setWallpaperBtn.visibility = View.GONE

        //load image into Picasso
        Picasso.get().load(item.imageUrl).into(dialogImageView, object : com.squareup.picasso.Callback {
            override fun onSuccess() {
                setWallpaperBtn.visibility = View.VISIBLE
                dialogProgressBar.visibility = View.GONE
            }

            override fun onError(e: Exception?) {
                Log.d("errorLoad", e.toString())
            }

        })

        setWallpaperBtn.setOnClickListener {
            val wallpaperManager = WallpaperManager.getInstance(context) as WallpaperManager
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val bitmap: Bitmap = dialogImageView.drawable.toBitmap()

                    // for async set wallpaper
                    withContext(Dispatchers.IO) { wallpaperManager.setBitmap(bitmap) }
                    Toast.makeText(context, "Wallpaper successfully installed!", Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    Toast.makeText(context, "Error: $e", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }


            }

        }

        dialog.show()

    }
    }

