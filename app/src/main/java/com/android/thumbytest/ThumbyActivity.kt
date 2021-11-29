package com.android.thumbytest

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.thumbytest.listener.SeekListener

class ThumbyActivity : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    private var centerCrop: CenterCropVideoView? = null
    private var timeLine: ThumbnailTimeline? = null

    companion object {
        const val EXTRA_THUMBNAIL_POSITION = "org.buffer.android.thumby.EXTRA_THUMBNAIL_POSITION"
        const val EXTRA_URI = "org.buffer.android.thumby.EXTRA_URI"

        fun getStartIntent(context: Context, uri: Uri, thumbnailPosition: Long = 0): Intent {
            val intent = Intent(context, ThumbyActivity::class.java)
            intent.putExtra(EXTRA_URI, uri)
            intent.putExtra(EXTRA_THUMBNAIL_POSITION, thumbnailPosition)
            return intent
        }
    }

    private var videoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_thumby)
        title = getString(R.string.picker_title)
        toolbar = findViewById(R.id.toolbar)
        centerCrop = findViewById(R.id.view_thumbnail)
        timeLine = findViewById(R.id.thumbs)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        videoUri = intent.getParcelableExtra<Uri>(EXTRA_URI) as Uri

        setupVideoContent()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.thumbnail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_menu_done -> {
                finishWithData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupVideoContent() {
        videoUri?.let { centerCrop?.setDataSource(this, it) }
        timeLine?.seekListener = seekListener
        timeLine?.currentSeekPosition =
            intent.getLongExtra(EXTRA_THUMBNAIL_POSITION, 0).toFloat()
        timeLine?.viewTreeObserver?.addOnGlobalLayoutListener(
            object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    timeLine?.viewTreeObserver?.removeOnGlobalLayoutListener(
                        this
                    )
                    timeLine?.uri = videoUri
                }
            })
    }

    private fun finishWithData() {
        val intent = Intent()
        intent.putExtra(
            EXTRA_THUMBNAIL_POSITION,
            timeLine?.currentProgress?.let {
                (centerCrop?.getDuration()?.div(100))?.times(
                    it
                )
                    ?.toLong()?.times(1000)
            }
        )
        intent.putExtra(EXTRA_URI, videoUri)
        setResult(RESULT_OK, intent)
        finish()
    }

    private val seekListener = object : SeekListener {
        override fun onVideoSeeked(percentage: Double) {
            val duration = centerCrop?.getDuration() ?: 0
            centerCrop?.seekTo((percentage.toInt() * duration) / 100)
        }
    }
}