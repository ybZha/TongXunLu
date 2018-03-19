package com.testgit.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.NonNull
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import com.testgit.tongxunlu.MainActivity
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.async
import java.util.*


/**
 * Author ZYB
 * Created Time 2018/3/15.
 * 短信列表
 */
class PhoneMessageActivitys : AppCompatActivity() {
    // 读取短信权限
    private var permissions = arrayOf(Manifest.permission.RECEIVE_SMS)
    var listview: ListView? =null
    private var tv: Button? = null
    var adapter:adapterMessage?=null
    private var datalistView  = ArrayList<MessageBean>()
    private var datas  = ArrayList<Map<String, Any>>()
    var dataAdapter:SimpleAdapter?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions,
                        123);//申请权限
            } else {//拥有当前权限
            }
        }
        verticalLayout{
            tv=button("读取通讯录")
            tv!!.setOnClickListener{
                startActivity<MainActivity>()
            }
            button("NO")

            listview= listView {  }.lparams(
                    width =  ViewGroup.LayoutParams.MATCH_PARENT,
                    height = ViewGroup.LayoutParams.MATCH_PARENT)
        }
        onStarts()
    }
    var utilp=null;
    @SuppressLint("NewApi")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==123){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                    // 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
                    var b = shouldShowRequestPermissionRationale(permissions[0]);
                    if (!b){
                        Toast.makeText(this, "dialog提示去页面手动提示", Toast.LENGTH_SHORT).show();
                        // 用户还是想用我的 APP 的
                        // 提示用户去应用设置界面手动开启权限
                        alert ("dialog提示去页面手动提示"){
                            title="提示"
                            positiveButton("确认"){
                                goToAppSetting(this@PhoneMessageActivitys)
                            }
                            negativeButton("取消"){
                                finish()
                            }
                        }.show()
                    }else{
                        finish()
                    }
                }
                else{
                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }
    private fun onStarts() {
        async {
            // 读取短信
            getSmsFromPhone()
            runOnUiThread {
                adapter= adapterMessage(this@PhoneMessageActivitys,datalistView)
                listview!!.setAdapter(adapter)
                listview!!.setOnItemClickListener {
                    parent, view, position, id ->
                    startActivity<DetailsMessage>("name" to datalistView.get(position).name, "phone" to  datalistView.get(position).phone,"msg" to  datalistView.get(position).messages)
                }
                /*   dataAdapter = SimpleAdapter(this@PhoneMessageActivity, datas, R.layout.item_phone, arrayOf("name", "phone"), intArrayOf(R.id.tv_name, R.id.tv_telephone))
                   listview!!.setAdapter(dataAdapter)*/
            }
        }
    }

    private val SMS_INBOX = Uri.parse("content://sms/")
    fun getSmsFromPhone() {
        val cr = contentResolver
        val projection = arrayOf("_id", "address", "person", "body", "date", "type")
        var cur = cr.query(SMS_INBOX, projection, null, null, "date desc")
        if (null == cur) {
            cur!!.close()
            Log.i("ooc", "************cur == null")
            return
        }

        while (cur.moveToNext()) {

            val number = cur.getString(cur.getColumnIndex("address"))//手机号
            val name = cur.getString(cur.getColumnIndex("person"))//联系人姓名列表
            val body = cur.getString(cur.getColumnIndex("body"))//短信内容
            //至此就获得了短信的相关的内容, 以下是把短信加入map中，构建listview,非必要。
            if (number.isNullOrEmpty ()){
                continue
            }
            val map = HashMap<String, Any>()
            map["num"] = number+""
            map["mess"] = body+""
            //   datalistView.add(map)
            datas.add(map)
            var msg = MessageBean(name, number, body)
            datalistView.add(msg)
            Log.w("第" + cur.position, "  \"第\"+cur.position,")
        }
        cur.close()
        Log.w("ccd", "关闭游标")
        Log.w("ccd","结束了")
    }

    // 跳转到当前应用的设置界面
    fun goToAppSetting(context: Context) {
        val intent = Intent()

        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        startActivityForResult(intent, 123);
    }
}