package com.sabina.project.base.external.images

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class ImageUtils @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    companion object {
        private const val WIDTH = 1722
        private const val HEIGHT = 2362
        private const val QUALITY = 70
    }

    fun createBitmap(filePath: String, function: (ByteArray) -> Unit) {
        Glide.with(context)
            .asBitmap()
            .load(filePath)
            .into(object : CustomTarget<Bitmap>(WIDTH, HEIGHT) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val bos = ByteArrayOutputStream()
                    resource.compress(Bitmap.CompressFormat.JPEG, QUALITY, bos)
                    function.invoke(bos.toByteArray())
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }
}