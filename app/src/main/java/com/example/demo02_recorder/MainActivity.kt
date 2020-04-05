package com.example.demo02_recorder

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.format.DateFormat
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    val PERMISSION_FILE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    val PERMISSION_RECORD = Manifest.permission.RECORD_AUDIO

    private val requestPermissions = arrayOf<String>(
        PERMISSION_FILE, PERMISSION_RECORD
    )

    enum class State{
        INIT,RECORING
    }

    var stateRecord : State = State.INIT
    lateinit var speakImg : ImageView

    lateinit var mMediaRecorder : MediaRecorder
    lateinit var fileName : String
    lateinit var filePath : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mMediaRecorder = MediaRecorder()

        speakImg= findViewById(R.id.speak_img)
        speakImg.setOnClickListener {

            if(checkPermission()){
                record()
            }
            else{
                requestPermission()
            }

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

            //设置录音来源为主麦克风
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            //设置录音输入格式为MPEG_4
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            //设置编解码方式为AAC，相比较mp3文件更小，更清晰
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

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
            mMediaRecorder.setOutputFile(filePath)

            //录音准备
            mMediaRecorder.prepare()
            //录音开始
            mMediaRecorder.start()

            //改变录音控件的图标为暂停
            speakImg.setImageResource(android.R.drawable.ic_media_pause)
            //改变状态为录音状态
            stateRecord = State.RECORING

        }else if(stateRecord == State.RECORING){
            //改变录音控件的图标为录音
            speakImg.setImageResource(android.R.drawable.ic_btn_speak_now)
            //改变状态为等待录音状态
            stateRecord = State.INIT

            //录音停止
            mMediaRecorder.stop()

            //弹窗显示录音保存的路径
            Toast.makeText(MainActivity@this,"录音存储路径；"+filePath,Toast.LENGTH_SHORT).show()
        }
    }


}
