package com.android.thumbytest

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.android.thumbytest.listener.SeekListener
import kotlin.math.roundToInt

class ThumbnailTimeline @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs) {


    private var seekbar: CenterCropVideoView? = null
    private var container: LinearLayout? = null
    private var containerSeekbar: FrameLayout? = null
    private var frameDimension: Int = 0
    var currentProgress = 0.0
    var currentSeekPosition = 0f
    var seekListener: SeekListener? = null
    var uri: Uri? = null
        set(value) {
            field = value
            field?.let {
                loadThumbnails(it)
                invalidate()
                seekbar?.setDataSource(context, it, 4)
                seekbar?.seekTo(currentSeekPosition.toInt())
            }
        }

    init {
        View.inflate(getContext(), R.layout.view_timeline, this)
        seekbar = findViewById(R.id.view_seek_bar)
        containerSeekbar = findViewById(R.id.container_seek_bar)
        container = findViewById(R.id.container_thumbnails)
        frameDimension = context.resources.getDimensionPixelOffset(R.dimen.frames_video_height)
        isFocusable = true
        isFocusableInTouchMode = true
        setBackgroundColor(ContextCompat.getColor(context, R.color.white))

        val margin = DisplayMetricsUtil.convertDpToPixel(16f, context).toInt()
        val params = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        params.setMargins(margin, 0, margin, 0)
        layoutParams = params
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_MOVE -> handleTouchEvent(event)
        }
        return true
    }

    private fun handleTouchEvent(event: MotionEvent) {
        val seekViewWidth = context.resources.getDimensionPixelSize(R.dimen.frames_video_height)
        currentSeekPosition = (event.x.roundToInt() - (seekViewWidth / 2)).toFloat()

        val availableWidth =
            (container?.width?.minus((layoutParams as LinearLayout.LayoutParams).marginEnd) ?: 0) -
                    (layoutParams as LinearLayout.LayoutParams).marginStart
        if (currentSeekPosition + seekViewWidth > container?.right ?: 0) {
            currentSeekPosition = (container?.right?.minus(seekViewWidth))?.toFloat() ?: 0F
        } else if (currentSeekPosition < container?.left ?: 0) {
            currentSeekPosition = paddingStart.toFloat()
        }

        currentProgress = (currentSeekPosition.toDouble() / availableWidth.toDouble()) * 100
        containerSeekbar?.translationX = currentSeekPosition
        seekbar?.seekTo(((currentProgress * (seekbar?.getDuration() ?: 0)) / 100).toInt())

        seekListener?.onVideoSeeked(currentProgress)
    }

    private fun loadThumbnails(uri: Uri) {
        val metaDataSource = MediaMetadataRetriever()
        metaDataSource.setDataSource(context, uri)

        val videoLength = (metaDataSource.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_DURATION
        )?.toInt()?.times(1000))?.toLong()

        val thumbnailCount = 30

        val interval = videoLength?.div(thumbnailCount)

        for (i in 0 until thumbnailCount - 1) {
            val frameTime = i * (interval ?: 0)
            var bitmap =
                metaDataSource.getFrameAtTime(frameTime, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            try {
                val targetWidth: Int
                val targetHeight: Int
                if ((bitmap?.height ?: 0) > (bitmap?.width ?: 0)) {
                    targetHeight = frameDimension
                    val percentage = frameDimension.toFloat() / (bitmap?.height ?: 0)
                    targetWidth = (bitmap?.width?.times(percentage))?.toInt() ?: 0
                } else {
                    targetWidth = frameDimension
                    val percentage = frameDimension.toFloat() / (bitmap?.width ?: 0)
                    targetHeight = (bitmap?.height?.times(percentage))?.toInt() ?: 0
                }
                bitmap =
                    bitmap?.let { Bitmap.createScaledBitmap(it, targetWidth, targetHeight, false) }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            container?.addView(ThumbnailView(context).apply { setImageBitmap(bitmap) })
        }
        metaDataSource.release()
    }
}