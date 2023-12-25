package com.liam.drawingscreen

import androidx.compose.ui.graphics.Color

data class PathStyle(
    var color: Color = Color.Black,
    var alpha: Float = 1.0f,
    var width: Float = 10.0f
)