package com.example.demo02_recorder

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator


class RhythmView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var mMaxHeight= 0.0 //最到点
    var mPerHeight //实时高度
            = 0.0
        set(value) {
            field = value
            mAnimator?.start()
        }
    private var min= 0.0 //最小x
    private var max = 0.0 //最大x
    private var firstPhase:Float= 0.0f //初相
    private var amplitude:Float= mMaxHeight.toFloat() //振幅
    private var angularFrequency=0.0 //角频率

    private var mPaint: Paint? = null //主画笔
    private var mPath: Path? = null  //主路径
    private var mReflexPath: Path? = null  //镜像路径
    private var mAnimator: ValueAnimator? = null
    private var mHeight = 0
    private var mWidth = 0

    init {
        //初始化主画笔
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint?.color= Color.BLUE
        mPaint?.style = Paint.Style.STROKE
        mPaint?.strokeWidth = dp(2f)
        //初始化主路径
        mPath = Path()
        mReflexPath = Path()
        //数字时间流
        mAnimator = ValueAnimator.ofFloat(0f, (2 * Math.PI).toFloat())
        mAnimator?.duration=1000
        mAnimator?.repeatCount=ValueAnimator.RESTART
        mAnimator?.interpolator= LinearInterpolator()
        mAnimator?.addUpdateListener(ValueAnimator.AnimatorUpdateListener { a: ValueAnimator ->
            firstPhase = a.animatedValue as Float
            amplitude = (mMaxHeight * mPerHeight * (1 - a.animatedValue as Float / (2 * Math.PI))).toFloat()
            invalidate()
        })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = MeasureSpec.getSize(widthMeasureSpec)
        mHeight = MeasureSpec.getSize(heightMeasureSpec)
        mMaxHeight = (mHeight / 2 * 0.9)
        min = (-mWidth / 2.toDouble())
        max = (mWidth / 2.toDouble())
        handleColor()
        setMeasuredDimension(mWidth, mHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        mPath!!.reset()
        mReflexPath!!.reset()
        super.onDraw(canvas)
        canvas!!.save()
        canvas.translate(mWidth / 2.toFloat(), mHeight / 2.toFloat())

        // 绘制第二条曲线
        formPath()
        mPaint!!.alpha = 255
        canvas.drawPath(mPath!!, mPaint!!)
        mPaint!!.alpha = 66
        canvas.drawPath(mReflexPath!!, mPaint!!)

        canvas.restore()
    }

    /**
     * 将像素转换成对应的像素密度单位dp
     */
    private fun dp(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
        )
    }

    /**
     * 对应法则
     *
     * @param x 原像(自变量)
     * @return 像(因变量)
     */
    private fun f(x: Double): Double {
        val len = max - min
        val a =
            4 / (4 + Math.pow(rad(x / Math.PI * 800 / len), 4.0))
        val aa = Math.pow(a, 2.5)
        angularFrequency = 2 * Math.PI / (rad(len) / 2)
        return (aa * amplitude * Math.sin(angularFrequency * rad(x) - firstPhase))
    }

    /**
     * 将角度值转换为弧度值
     */
    private fun rad(deg:Double): Double {
        return deg / 180 * Math.PI
    }

    //加渐变色
    private fun handleColor() {
        val colors = intArrayOf(
            Color.parseColor("#33F60C0C"),  //红
            Color.parseColor("#F3B913"),  //橙
            Color.parseColor("#E7F716"),  //黄
            Color.parseColor("#3DF30B"),  //绿
            Color.parseColor("#0DF6EF"),  //青
            Color.parseColor("#0829FB"),  //蓝
            Color.parseColor("#33B709F4")
        )
        val pos = floatArrayOf(
            1f / 10, 2f / 7, 3f / 7, 4f / 7, 5f / 7, 9f / 10, 1f
        )
        mPaint!!.shader = LinearGradient(
            min.toFloat(), 0.toFloat(), max.toFloat(), 0.toFloat(),
            colors, pos,
            Shader.TileMode.CLAMP
        )
    }

    /**
     * 第二条曲线的路径
     */
    private fun formPath() {
        mPath!!.moveTo(min.toFloat(), f(min).toFloat())
        mReflexPath!!.moveTo(min.toFloat(), f(min).toFloat())
        var x = min
        while (x <= max) {
            val y = f(x).toFloat()
            mPath!!.lineTo(x.toFloat(), y)
            mReflexPath!!.lineTo(x.toFloat(), -y)
            x++
        }
    }



}