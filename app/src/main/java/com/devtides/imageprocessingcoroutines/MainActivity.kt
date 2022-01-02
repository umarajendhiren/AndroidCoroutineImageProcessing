package com.devtides.imageprocessingcoroutines

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.net.URL

class MainActivity : AppCompatActivity() {


    /*project objective:
    * going to run two couroutines on two different dispatcher to do two different tasks
    * one coroutine will download image from URL
    * another coroutine apply filter for downloaded image
    *
    * */

    private lateinit var imageview: ImageView
    private lateinit var progressBar: ProgressBar

    private val IMAGE_URL =
        "https://raw.githubusercontent.com/DevTides/JetpackDogsApp/master/app/src/main/res/drawable/dog.png"

    /*we need to create this coroutineScope on Main dispatcher because we will use this coroutine result to update imageview on main thread*/
    var coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageview = findViewById(R.id.imageView)
        progressBar = findViewById(R.id.progressBar)


        /*launching this coroutine in this scope of main thread*/
        coroutineScope.launch {

            //here we need to call getOriginalBitmap() but this function makes network communication and input ,output call
            //so wants to do this on IO dispatcher .

            val originalDeferred = coroutineScope.async(Dispatchers.IO) {
                getOriginalBitmap()


            }


            //we can get the image from network.once its available we can use that image .so we need to wait for that image to be available.
            //by calling await(), we can consume result from async call
            val originalBitmap = originalDeferred.await()

            //loadImage(originalBitmap)


            //after getting original image ,am going to apply some filter for the image .that process will be run in different coroutine on default dispatcher

            val filteredDeferred = coroutineScope.async(Dispatchers.Default) {

                filterImage(originalBitmap)
            }

            //here we need to wait for the result from async

            val filteredBitmap = filteredDeferred.await()

            //then load the filtered image into imageview
            loadImage(filteredBitmap)


        }
    }

    /*this method download the image from given url and convert that to Bitmap then will return
    * this is the network call .we can not do this on main thread .so we need to open up new coroutine scope to do this task on IO dispatcher.*/
    fun getOriginalBitmap() = URL(IMAGE_URL).openStream().use {
        BitmapFactory.decodeStream(it)
    }

    fun loadImage(bitmap: Bitmap) {
        progressBar.visibility = View.GONE

        imageview.visibility = View.VISIBLE
        imageview.setImageBitmap(bitmap)


    }

    //function as expression
    fun filterImage(originalBitmap: Bitmap) = Filter.apply(originalBitmap)

    /*fun filterImage(originalBitmap:Bitmap):Bitmap{

        val filteredImage=Filter.apply(originalBitmap)
        return filteredImage

    }*/


}
