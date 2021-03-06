package com.luowei.slide

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.app.FragmentManager
import android.util.AttributeSet
import android.widget.FrameLayout
import com.unistrong.luowei.adsslidelib.R
import java.io.File
import java.io.FileOutputStream

/**
 * Created by luowei on 2017/9/11.
 */
abstract class AbsAdvertisement : FrameLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    val DEFAULT_IMAGE: String
//    var workHandler: Handler
    private val BASEPATH = context.getExternalFilesDir("Advertisement").absolutePath

    init {
        val file = File(BASEPATH, "ADS_DEFAULT.jpg")
        DEFAULT_IMAGE = file.absolutePath
        if (!file.exists()) {
            context.resources.openRawResource(R.raw.ads).copyTo(FileOutputStream(file))
        }
//        val handlerThread = HandlerThread("AdvertisementVideoToImageThread")
//        handlerThread.start()
//        workHandler = Handler(handlerThread.looper)
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
//        workHandler.looper.quit()
    }


    open fun addItem(item: SlideAdapter.Item) {
        if (item.type == SlideAdapter.ItemType.Video) {
            item.videoImage = File(BASEPATH, File(item.path).name).absolutePath
//            workHandler.post { VideoToImage.saveImage(context, item.path, item.videoImage!!) }
        }
    }

    open fun clear() {
    }


    open fun setDefaultImageFile(path: String) {
    }
}