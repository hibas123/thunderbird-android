package com.fsck.k9.ui.messagelist.item

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.ReplacementSpan
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils

/**
 * Draws the text it is applied to as a small tag, i.e. on a rounded rectangle in the given color.
 */
internal class TagSpan(
    @param:ColorInt private val backgroundColor: Int,
    private val horizontalPadding: Float,
    private val cornerRadius: Float,
) : ReplacementSpan() {
    private val rect = RectF()

    @ColorInt
    private val textColor: Int =
        if (ColorUtils.calculateLuminance(backgroundColor) > LUMINANCE_THRESHOLD) BLACK else WHITE

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        return (paint.measureText(text, start, end) + 2 * horizontalPadding).toInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint,
    ) {
        val width = paint.measureText(text, start, end)
        val originalColor = paint.color

        rect.set(x, top.toFloat(), x + width + 2 * horizontalPadding, bottom.toFloat())
        paint.color = backgroundColor
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

        paint.color = textColor
        canvas.drawText(text, start, end, x + horizontalPadding, y.toFloat(), paint)

        paint.color = originalColor
    }

    private companion object {
        const val LUMINANCE_THRESHOLD = 0.5
        const val BLACK = 0xFF000000.toInt()
        const val WHITE = 0xFFFFFFFF.toInt()
    }
}
