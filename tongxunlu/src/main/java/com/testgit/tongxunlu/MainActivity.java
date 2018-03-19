package com.testgit.tongxunlu;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Context mContext;
    private ListView listview;
    // 获取权限数组
    private String permission []= new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.CALL_PHONE};
    // 联系人
    private ArrayList datalistView  = new ArrayList< Map<String, String>>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext=this;
        listview=findViewById(R.id.listview);
        // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            boolean bb=true;
            for (String ss:permission){
                int i = ContextCompat.checkSelfPermission(mContext, ss);
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                if (i != PackageManager.PERMISSION_GRANTED) {
                    // 如果没有授予该权限，就去提示用户请求
                    bb=false;
                    break;
                }
            }
            if (!bb){
                // 申请权限
                ActivityCompat.requestPermissions(this, permission,1);//申请权限
            }else{
                Toast.makeText( this," 拥有当前权限  ",0).show();
            }
        }else{
            // 6.0以下不需要动态申请
        }
        onStarts();
    }
    // 权限回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==123){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                boolean b=true;
                if (grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                    // 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
                    for (String c:permissions){
                        b = shouldShowRequestPermissionRationale(c);
                        if (b=false) {
                            break;
                        }
                    }
                }
                if (!b){
                    Toast.makeText(this, "dialog提示去页面手动提示", Toast.LENGTH_SHORT).show();
                    // 用户还是想用我的 APP 的
                    // 提示用户去应用设置界面手动开启权限
                    new  AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 跳到设置手动开启权限
                                    goToAppSetting(mContext);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .show();
                }else{
                    finish();
                }
            }
            else{
                Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void onStarts() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                datalistView.addAll(readContact());
                handler.sendEmptyMessage(0);
            }
        }).start();

    }
    Handler handler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SimpleAdapter adapter = new SimpleAdapter(mContext, datalistView, R.layout.item_phone, new String[]{"name", "phone"}, new int[]{R.id.tv_name, R.id.tv_telephone});
            listview.setAdapter(adapter);
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Map map= (HashMap) datalistView.get(position);
                    callDirectly(map.get("phone").toString());
                }
            });
        }


    };
    /**
     * 得到联系人
     */
    private ArrayList<HashMap<String, String>>  readContact(){
        // 首先,从raw_contacts中读取联系人的id("contact_id")
        // 其次, 根据contact_id从data表中查询出相应的电话号码和联系人名称
        // 然后,根据mimetype来区分哪个是联系人,哪个是电话号码
        Uri rawContactsUri = Uri.parse("content://com.android.contacts/raw_contacts");
        Uri dataUri = Uri.parse("content://com.android.contacts/data");
        ArrayList list = new ArrayList<HashMap<String, String>>();
        // 从raw_contacts中读取所有联系人的id("contact_id")
        Cursor rawContactsCursor = getContentResolver().query(rawContactsUri,
                new String[]{"contact_id"}, null, null, null);
        if (rawContactsCursor != null) {
            while (rawContactsCursor.moveToNext()) {
                String contactId = rawContactsCursor.getString(0);
                // System.out.println("得到的contact_id="+contactId);
                // 根据contact_id从data表中查询出相应的电话号码和联系人名称, 实际上查询的是视图view_data
                Cursor dataCursor = getContentResolver().query(dataUri,
                        new String[]{"data1", "mimetype"}, "contact_id=?",
                        new String[]{contactId}, null);
                if (dataCursor != null) {
                    HashMap map = new HashMap<String, String>();
                    String data;//名字
                    StringBuffer data1=new StringBuffer();// 号码
                    while (dataCursor.moveToNext()) {
                        String mimetype = dataCursor.getString(1);
                        if ("vnd.android.cursor.item/phone_v2" .equals(mimetype) ) {//手机号码
                            data1 = data1.append(dataCursor.getString(0)+",");
                            map.put("phone",data1.toString());
                        } else if ("vnd.android.cursor.item/name".equals(mimetype)) {//联系人名字
                            data  =dataCursor.getString(0);
                            map.put("name",data.toString());
                        }
                    }
                    list.add(map);
                    dataCursor.close();
                }
            }
            rawContactsCursor.close();
        }
        return list;
    }
    // 跳转到当前应用的设置界面
    private void goToAppSetting( Context context) {
        Intent intent = new Intent();
        intent.setAction( Settings.ACTION_APPLICATION_DETAILS_SETTINGS) ;
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri) ;
        startActivityForResult(intent, 123);
        // 回调自己设置
    }
    // 拨号
    private void callDirectly (String mobile) {
        String tel="";
        tel=mobile;
        if(mobile.indexOf(",")>0){
            tel=mobile.substring(0,mobile.indexOf(","));
        }
        Intent intent = new Intent();
        intent.setAction("android.intent.action.CALL");
        intent.setData(Uri.parse("tel:" + tel));
        startActivity(intent);
    }
}
