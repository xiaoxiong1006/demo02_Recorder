package com.example.demo02_recorder

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.text.format.DateFormat
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(),RecorderTask.OnVolumeChangeListener{

    val PERMISSION_FILE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    val PERMISSION_RECORD = Manifest.permission.RECORD_AUDIO

    private val requestPermissions = arrayOf<String>(
        PERMISSION_FILE, PERMISSION_RECORD
    )

    enum class State{
        INIT,RECORING,PLAYING
    }

    var stateRecord : State = State.INIT
    var statePlay:State =State.INIT
    lateinit var speakImg : ImageView
    lateinit var playImg : ImageView
    lateinit var timer:Chronometer

    //lateinit var mMediaRecorder : MediaRecorder
    lateinit var fileName : String
    lateinit var filePath : String

    var mMediaPlayer = MediaPlayer()

    lateinit var mMediaRecorderTask: RecorderTask
    lateinit var mViewRhythm: RhythmView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       // mMediaRecorder = MediaRecorder()

        speakImg= findViewById(R.id.speak_img)
        playImg = findViewById(R.id.play_img)
        timer = findViewById(R.id.timer)
        mViewRhythm = findViewById(R.id.view_rhythm)
        mMediaRecorderTask = RecorderTask(this)


        speakImg.setOnClickListener {
            if(checkPermission()){
                record()
            }
            else{
                requestPermission()
            }
        }

        playImg.setOnClickListener {
            play()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //requestCode是我们请求权限时的请求码
        when(requestCode){
            1 -> {
                record()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        //mMediaRecorder?.release()
        mMediaPlayer?.release()
    }

    override fun volumeChange(per: Double) {
        mViewRhythm.mPerHeight = per
    }

    /**
     * 动态请求必须的权限
     */
    fun requestPermission() {
        val permissions: Array<String>
        val mPermissionList = ArrayList<String>()

        mPermissionList.clear()
        for (i in requestPermissions.indices) {
            // 检查权限是否已经获取到过，没有获取到过并把权限放入请求权限集合中用于接下来请求权限
            if (ContextCompat.checkSelfPermission(this, requestPermissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(requestPermissions[i])
            }
        }
        // mPermissionList集合不为空，表示有未授予的权限
        if (!mPermissionList.isEmpty()) {
            //将List转为数组
            permissions = mPermissionList.toTypedArray()
            //请求权限
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
    }

    /**
     * 检查权限
     * 未取得权限返回false,否则返回true
     */
    fun checkPermission():Boolean{
        for (i in requestPermissions.indices) {

            if (ContextCompat.checkSelfPermission(this, requestPermissions[i]) != PackageManager.PERMISSION_GRANTED) {
                return false
                break
            }
        }
        return true
    }

    /**
     * 录音
     */
    fun record(){
        // 如果当前是初始化状态，就录音。是录音状态，就恢复到初始状态。
        if(stateRecord == State.INIT){

            //停止播放录音
            stop_play()
            
            //设置录音来源为主麦克风
            //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            //设置录音输入格式为MPEG_4
            //mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            //设置编解码方式为AAC，相比较mp3文件更小，更清晰
            //mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            //文件名称
            fileName = DateFormat.format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA)).toString() + ".m4a"
            //创建目录
            val destDir = File(Environment.getExternalStorageDirectory().toString()+"/test/")
            if (!destDir.exists()) {
                destDir.mkdirs()
            }
            //存储路径
            filePath = Environment.getExternalStorageDirectory().toString() + "/test/" + fileName
            //设置输出路径即为存储路径
            //mMediaRecorder.setOutputFile(filePath)

            //录音准备
            //mMediaRecorder.prepare()
            //录音开始
            //mMediaRecorder.start()
            mMediaRecorderTask?.startRecorder(filePath)

            //改变录音控件的图标为暂停
            speakImg.setImageResource(android.R.drawable.ic_media_pause)
            //改变状态为录音状态
            stateRecord = State.RECORING

            //计时器清零
            timer.setBase(SystemClock.elapsedRealtime())
            //计算当前时间与初始时间差
            val hour : Int =  ((SystemClock.elapsedRealtime() - timer.getBase()) / 1000 / 60).toInt()
            //按照设定的格式显示
            timer.setFormat("0"+hour+":%s")
            //计时器启动
            timer.start()

        }else if(stateRecord == State.RECORING){
            //改变录音控件的图标为录音
            speakImg.setImageResource(android.R.drawable.ic_btn_speak_now)
            //改变状态为等待录音状态
            stateRecord = State.INIT

            //录音停止
            //mMediaRecorder.stop()
            mMediaRecorderTask?.stop()

            //计时器停止
            timer.stop()
            //计时器清零
            timer.setBase(SystemClock.elapsedRealtime())


            //弹窗显示录音保存的路径
            Toast.makeText(MainActivity@this,"录音存储路径；"+filePath,Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 播放录音
     */
    fun play(){
        // 如果处于初始化状态，同时又没有在录音，那么就播放
        if(statePlay == State.INIT && stateRecord != State.RECORING){
            if (this::filePath.isInitialized) { //如果录音文件已经存在
                //改变播放图标为暂停
                playImg.setImageResource(android.R.drawable.ic_media_pause)
                //改变播放状态为正在播放
                statePlay = State.PLAYING
                //为了防止重复加载播放资源
                mMediaPlayer.reset()
                //加载录音文件的地址
                mMediaPlayer.setDataSource(filePath)
                //播放准备
                mMediaPlayer.prepare()
                //开始播放
                mMediaPlayer.start()

                //播放完毕后
                mMediaPlayer.setOnCompletionListener {
                    //停止播放
                    stop_play()
                }
            }
        }else if(statePlay == State.PLAYING){  //如果处于播放状态，那么就暂停

            //停止播放
            stop_play()
        }
    }

    /**
     * 停止播放
     */
    fun stop_play(){
        //将播放置回初始状态
        statePlay = State.INIT
        //改变图标为播放图标
        playImg.setImageResource(android.R.drawable.ic_media_play)
        //停止播放
        mMediaPlayer.stop()
    }

}
