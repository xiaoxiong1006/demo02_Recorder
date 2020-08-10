package com.example.demo02_recorder

import android.media.MediaRecorder
import android.os.Handler
import java.io.IOException
import java.util.*

class RecorderTask(private val mOnVolumeChangeListener:OnVolumeChangeListener) {
    private var mRecorder: MediaRecorder? = null
    private var isRecording //是否正在录音
            = false
    private var mTimer: Timer? = null
    private var mHandler: Handler? = null

    /**
     * 获取音量变化的接口
     */
    interface OnVolumeChangeListener {
        fun volumeChange(per:Double)
    }

    init {
        mTimer = Timer() //创建Timer
        mHandler = Handler() //创建Handler
    }

    /**
     * 每隔0.6秒回调一次音量
     */
    private fun cbkVolume() {
        mTimer!!.schedule(object : TimerTask() {
            override fun run() {
                if (isRecording) {
                    val per: Float
                    per = try {
                        //获取音量大小
                        mRecorder!!.maxAmplitude / 32767f //最大32767
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                        Math.random().toFloat()
                    }
                    if (mOnVolumeChangeListener != null) {
                        mHandler!!.post {
                            mOnVolumeChangeListener?.volumeChange(
                                per.toDouble()
                            )
                        }
                    }
                }
            }
        }, 0, 600)
    }

    /**
     * 开始录音
     */
    fun startRecorder(file: String?) {
        if (mRecorder == null) {
            // [1]获取MediaRecorder类的实例
            mRecorder = MediaRecorder()
        }

        //配置MediaRecorder
        // [2]设置录音来源为主麦克风
        mRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        // [3]设置录音输入格式为MPEG_4
        mRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        // [4]设置音频的编码方式，设置编解码方式为AAC，相比较mp3文件更小，更清晰
        mRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        // [5]设置录音文件位置、即为存储路径
        mRecorder?.setOutputFile(file)
        try {
            mRecorder?.prepare()
            mRecorder?.start()
            isRecording = true
            cbkVolume()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 停止录音
     */
    fun stop() {
        try {
            mRecorder!!.stop() // [6]停止录
            isRecording = false
            mRecorder!!.release()
            mRecorder = null
        } catch (e: RuntimeException) {
            mRecorder!!.reset() //[8] 重置MediaRecorder
            mRecorder!!.release() //[9] 释放MediaRecorder
            mRecorder = null
            isRecording = false
        }
    }

}

