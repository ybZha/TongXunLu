package com.testgit.myapplication

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout

/**
 * Author ZYB
 * Created Time 2018/3/16.
 * 查看短信详情
 */
 class DetailsMessage :AppCompatActivity(){
var name="";
    var phone="";
    var msg="";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
var bund=intent.getExtras() as Bundle
        name= bund.get("name").toString()
        phone= bund.get("phone").toString()
        msg= bund.get("msg").toString()
    verticalLayout {
        textView(name)

       textView(phone)


    textView(msg)

}
    }
}