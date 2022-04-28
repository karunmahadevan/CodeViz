package edu.bloomu.km25601.mpchart.fastpath

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import edu.bloomu.km25601.mpchart.R
import kotlin.math.abs

/**
 *
 *
 * @author Karun Mahadevan
 */

class MyCanvasView(context: Context) : View(context) {
    private val STROKE_WIDTH = 12f
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private val backgroundColor = ResourcesCompat.getColor(resources,
        R.color.bgGradEnd, null)

    private var drawColor = ResourcesCompat.getColor(resources, R.color.black, null)
    private var paint = Paint().apply {
        color = drawColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = STROKE_WIDTH
    }
    private var startPaint = Paint().apply {
        color = Color.rgb(0, 255, 0)
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL
    }
    private var endPaint = Paint().apply {
        color = Color.rgb(255, 0, 0)
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL
    }

    private var path = Path()

    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f

    private var currentX = 0f
    private var currentY = 0f

    private var touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    var touchPoints = arrayListOf<Pair<Float, Float>>()
    private lateinit var firstPoint: Pair<Float, Float>
    private lateinit var lastPoint: Pair<Float, Float>


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (::extraBitmap.isInitialized) extraBitmap.recycle()

        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)
        extraCanvas.drawRect(Rect(0, 0, 50, 50), startPaint)
        extraCanvas.drawRect(Rect(450, 450, 500, 500), endPaint)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(extraBitmap, 0f, 0f, null)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        motionTouchEventX = event!!.x
        motionTouchEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
            }
        }
        return true
    }

    private fun touchStart() {
        path.reset()
        path.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
        firstPoint = Pair(currentY, currentX)
        touchPoints.add(Pair(currentY, currentX))
    }

    private fun touchMove() {
        val dx = abs(motionTouchEventX - currentX)
        val dy = abs(motionTouchEventY - currentY)

        if (dx >= touchTolerance || dy >= touchTolerance) {
            path.quadTo(currentX, currentY, (motionTouchEventX + currentX) / 2,
                (motionTouchEventY + currentY) / 2)
            currentX = motionTouchEventX
            currentY = motionTouchEventY

            if ((currentY > 50 && currentX > 50) || (currentX <=450 && currentY <=450)) {
                touchPoints.add(Pair(currentY, currentX))
                extraCanvas.drawPath(path, paint)
            }
        }
        invalidate()
    }

    private fun touchUp() {
        lastPoint = touchPoints[touchPoints.size - 1]
        path.reset()
    }

}