package com.ibashkimi.lockscheduler.addeditprofile.conditions.location

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.graphics.ColorUtils
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.util.ThemeUtils

class MapCoverView : View {
    private val circlePaint: Paint = Paint()
    private val circumferencePaint: Paint = Paint()

    var centerX: Float = 0f
    var centerY: Float = 0f
    var radius: Float = 0f

    private var margin: Int = 100

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.style.AppTheme
    ) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context)
    }

    private fun init(context: Context) {
        margin = resources.getDimensionPixelOffset(R.dimen.place_picker_circle_margin)
        val circumferenceColor = ThemeUtils.obtainColor(context, R.attr.colorPrimary, Color.RED)
        val circleColor = ColorUtils.setAlphaComponent(circumferenceColor, 50)

        circlePaint.isAntiAlias = true
        circlePaint.color = circleColor
        circlePaint.strokeWidth = ThemeUtils.dpToPx(context, 2)
        circlePaint.style = Paint.Style.FILL

        circumferencePaint.isAntiAlias = true
        circumferencePaint.color = circumferenceColor
        circumferencePaint.strokeWidth = ThemeUtils.dpToPx(context, 2)
        circumferencePaint.style = Paint.Style.STROKE


    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(centerX, centerY, radius, circlePaint)
        canvas.drawCircle(centerX, centerY, radius, circumferencePaint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = (w / 2).toFloat()
        centerY = (h / 2).toFloat()
        radius = (if (centerX <= centerY) centerX else centerY) - margin
    }
}