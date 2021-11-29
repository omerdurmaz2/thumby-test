package com.android.thumbytest

import android.content.Intent
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import com.android.thumbytest.ThumbyActivity.Companion.EXTRA_THUMBNAIL_POSITION
import com.android.thumbytest.ThumbyActivity.Companion.EXTRA_URI
import com.android.thumbytest.util.ThumbyUtils

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getGalleryVideo.launch("video/*")
    }

    private val getGalleryVideo =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                launcher.launch(ThumbyActivity.getStartIntent(this, uri))
            }
        }
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data != null) {
                val imageUri = it?.data?.getParcelableExtra<Uri>(EXTRA_URI) as Uri
                val location = it.data?.getLongExtra(EXTRA_THUMBNAIL_POSITION, 0)
                val bitmap = location?.let { it1 ->
                    ThumbyUtils.getBitmapAtFrame(
                        this, imageUri, it1,
                        200, 200
                    )
                }
                findViewById<ImageView>(R.id.ivThumb).setImageBitmap(bitmap)
            }
        }

}