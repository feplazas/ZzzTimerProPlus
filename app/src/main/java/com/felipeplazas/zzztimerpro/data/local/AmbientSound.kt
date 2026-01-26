package com.felipeplazas.zzztimerpro.data.local

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes

data class AmbientSound(
    val id: Int,
    @StringRes val nameResId: Int,
    @StringRes val descriptionResId: Int,
    @DrawableRes val iconResId: Int,
    @RawRes val audioResId: Int
)
