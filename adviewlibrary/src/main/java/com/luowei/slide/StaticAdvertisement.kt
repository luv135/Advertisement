package com.luowei.slide

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import com.unistrong.luowei.commlib.Log
import com.unistrong.luowei.kotlin.hide
import com.unistrong.luowei.kotlin.show
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by luowei on 2017/12/5.
 */
class StaticAdvertisement : AbsAdvertisement {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    private val DEBUG = false
    var timeOut = 3000

    var currentIndex = 0
        private set(value) {
            field = value
//            if (DEBUG) Log.d("currentIndex=$currentIndex")
            indicator.onPageSelected(currentIndex)
        }
    val playlist = ArrayList<SlideAdapter.Item>()

    private var timerHandler: Handler? = MyHandler(this)

    val roll3dContainer = Roll3DContainer(context)
    private val videoView = VideoView(context)
    private val indicator = CircleIndicator(context)

    init {
        addView(videoView)
        addView(roll3dContainer)
        val layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        layoutParams.bottomMargin = 10
        indicator.setPadding(0, 10, 0, 10)
        indicator.gravity = Gravity.CENTER_HORIZONTAL
//        indicator.setBackgroundColor(context.resources.getColor(R.color.indicator_background))
        addView(indicator, layoutParams)
        val listener: AnimatorListenerAdapter = object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
//                updateItem()
                slideDelay()
            }
        }
        roll3dContainer.listener = listener
        videoView.listener = {
            if (it == VideoView.ListenState.End) {
                if (DEBUG) Log.d("play ok")
//                roll3dContainer.currentBitmap = videoView.bitmap
//                roll3dContainer.show()
//                videoView.hide()
                slideNext(true)
            } else {
                val alpha = roll3dContainer.animate().alpha(0f)
                alpha.duration = 1000
                alpha.setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        roll3dContainer.hide()
                        roll3dContainer.alpha = 1f
                    }
                })
                alpha.start()
            }
        }
    }

//    private fun updateItem() {
//        if (DEBUG) Log.d()
//        if (currentItem!!.type == SlideAdapter.ItemType.Image) {
//            slideDelay()
//        } else {
//            videoView.play()
////            val alpha = roll3dContainer.animate().alpha(0f)
////            alpha.duration = 1000
////            alpha.setListener(object : AnimatorListenerAdapter() {
////                override fun onAnimationEnd(animation: Animator?) {
////                    super.onAnimationEnd(animation)
////                    roll3dContainer.hide()
////                    roll3dContainer.alpha = 1f
////                }
////            })
////            alpha.start()
//        }
//    }

    override fun clear() {
        playlist.clear()
        notifyDataChange(true)
    }

    override fun addItem(item: SlideAdapter.Item) {
//        super.addItem(item)
        playlist.add(item)
        notifyDataChange(playlist.size <= 1)
    }

    private fun slideDelay() {
        if (DEBUG) Log.d()
        timerHandler!!.removeMessages(0)
        timerHandler!!.sendEmptyMessageDelayed(0, timeOut.toLong())
    }

    private var currentItem: SlideAdapter.Item? = null

    private fun slideNext(force: Boolean = false) {
        if (currentItem?.type == SlideAdapter.ItemType.Video && !force) return
        if (playlist.size > 0) currentIndex = (currentIndex + 1) % playlist.size
        val item = playlist.getOrNull(currentIndex) ?: defaultItem
        if (item.type == SlideAdapter.ItemType.Video) {
            if (item.path != currentItem?.path) videoView.initVideoResource(item!!.path)
            videoView.play()
        } else {
            if (currentItem?.type == SlideAdapter.ItemType.Video) {
                roll3dContainer.currentBitmap = videoView.bitmap
                roll3dContainer.show()
                videoView.stop()
            }
            if (item.path != currentItem?.path) {
//            val bitmap = getImagePath(item!!)
                val bitmap = BitmapLoader.loadBitmap(item.path, width, height)
                if (bitmap == null) {
                    Log.d("${item!!.path} bitmap is empty slide next(${playlist.size})")
                    playlist.removeAt(currentIndex)
                    notifyDataChange()
                } else {
                    roll3dContainer.nextBitmap = bitmap
                }
            }
        }
        currentItem = item
    }

    private fun getImagePath(item: SlideAdapter.Item): Bitmap? {
        val path = when (item.type) {
            SlideAdapter.ItemType.Image -> item.path
            else -> item.videoImage
        }
        return try {
            path ?: return null
            return BitmapLoader.loadBitmap(path, width, height)
        } catch (e: Exception) {
            Log.e(e.localizedMessage)
            null
        }

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        slideDelay()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        timerHandler!!.removeMessages(0)
    }

    internal class MyHandler(pager: StaticAdvertisement) : Handler() {
        private var pager: WeakReference<StaticAdvertisement> = WeakReference(pager)

        override fun handleMessage(msg: Message) {
            pager.get()?.slideNext()
        }
    }

    private lateinit var defaultItem: SlideAdapter.Item

    override fun setDefaultImageFile(path: String) {
        defaultItem = SlideAdapter.Item(SlideAdapter.ItemType.Image, path)
        notifyDataChange()
    }

    fun notifyDataChange(updateNow: Boolean = false) {
        Log.d("updateNow=$updateNow")
        var update = updateNow
        if (currentItem != null) {
            if (playlist.find { it.path == currentItem!!.path } == null) {
                update = true
            }
        }
        indicator.updateIndicator(playlist.size)
        if (update) {
            slideNext(true)
        } else slideDelay()
//        slideDelay()
    }

    fun next() {
        slideNext(true)
        slideDelay()
    }

    fun prev() {
        if (playlist.size > 0) {
            currentIndex = (currentIndex - 2) % playlist.size
            if (currentIndex < 0) {
                currentIndex += playlist.size
            }
        }
        slideNext(true)
        slideDelay()
    }
}