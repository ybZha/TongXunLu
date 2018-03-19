package com.testgit.myapplication

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import java.util.ArrayList
import android.view.LayoutInflater
import android.widget.TextView
import com.testgit.tongxunlu.R


/**
 * Author ZYB
 * Created Time 2018/3/16.
 *
 */
class adapterMessage(var context:Context,  var datas:ArrayList<MessageBean>): BaseAdapter() {
    private var inflater: LayoutInflater? = null
    var data:ArrayList<MessageBean>?=ArrayList()
    init {
        inflater=LayoutInflater.from(context);
        data=datas
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val view = inflater!!.inflate(R.layout.item_phone, null)
        var tv=view.findViewById(R.id.tv_name)as TextView
        var tv1=view.findViewById(R.id.tv_telephone)as TextView
        tv.setText(data!!.get(position).name)
        tv1.setText(data!!.get(position).phone)
        return view
    }

    override fun getItem(position: Int): Any {
        return data!!.get(position)
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
      return data!!.size
    }
}