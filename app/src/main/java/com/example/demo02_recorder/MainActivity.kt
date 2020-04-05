package com.example.demo02_recorder

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

            //改变录音控件的图标为暂停
            speakImg.setImageResource(android.R.drawable.ic_media_pause)
            //改变状态为录音状态
            stateRecord = State.RECORING

        }else if(stateRecord == State.RECORING){

            speakImg.setImageResource(android.R.drawable.ic_btn_speak_now)
            stateRecord = State.INIT

        }
    }

}
