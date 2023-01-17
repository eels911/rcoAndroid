package com.sabina.project.base.external.images

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.sabina.project.base.R

fun ImageView.downloadFromUrl(link: String) {
    Glide.with(context)
        .load(link)
        .centerCrop()
        .error(R.drawable.ic_error_24)
        .transition(DrawableTransitionOptions.withCrossFade(200))
        .into(this)
}

fun ImageView.downloadFromUrlCropped(link: String) {
    Glide.with(context)
        .load(link)
        .centerCrop()
        .apply(RequestOptions().override(500, 500))
        .error(R.drawable.ic_error_24)
        .transition(DrawableTransitionOptions.withCrossFade(200))
        .into(this)
}