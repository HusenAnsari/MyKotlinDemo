package com.husenansari.mykotlindemo.ui

import android.app.ProgressDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hashtechhub.mrhomeexpert.R
import com.hashtechhub.mrhomeexpert.adaptor.NotificationListRecycleAdapter
import com.hashtechhub.mrhomeexpert.api.ApiInterface
import com.hashtechhub.mrhomeexpert.api.model.*
import com.hashtechhub.mrhomeexpert.helper.MyApplication
import com.hashtechhub.mrhomeexpert.helper.SharedPrefManager
import com.timechart.tctracker.helper.Functions
import kotlinx.android.synthetic.main.activity_notification.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class NotificationActivityUSINGREQUST : BaseActivity() {

    private var notificationListRecycleAdapter: NotificationListRecycleAdapter? = null
    private var notificationInfo: ArrayList<Notification>? = null
    private var rvNotification: RecyclerView? = null
    var loader: ProgressDialog? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        initToolbar()
        initView()
    }

    private fun initView() {
        loader = Functions.getLoader(this!!)
        rvNotification = findViewById<RecyclerView>(R.id.rvNotification)
        rvNotification!!.setHasFixedSize(true);
        rvNotification!!.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        notificationInfo = ArrayList()

        notificationListRecycleAdapter = NotificationListRecycleAdapter(this, notificationInfo!!)
        rvNotification!!.adapter = notificationListRecycleAdapter

        if (Functions.isConnected(this!!)) {
            callNotification()
        } else {
            Functions.showToast(this!!, getString(R.string.internet_error))
        }

    }

    private fun callNotification() {
        loader!!.show()

        val userIdRequest = UserIdRequest()
        userIdRequest.user_id =  SharedPrefManager.getUserID(this).toString()


        val api = MyApplication.getRetrofit().create(ApiInterface::class.java)
        api.notifications(userIdRequest).enqueue(object : Callback<NotificationResponse>{
            override fun onResponse(
                call: Call<NotificationResponse>,
                response: Response<NotificationResponse>
            ) {
                loader!!.dismiss()
                val notificationResponse: NotificationResponse = response.body()!!

                if (notificationResponse.status_code == 200) {
                    if (notificationResponse.data.notifications.isNotEmpty()){
                        rvNotification!!.visibility = View.VISIBLE
                        txtEmptyView.visibility = View.GONE
                        notificationListRecycleAdapter!!.setService(notificationResponse.data.notifications)
                    }else{
                        rvNotification!!.visibility = View.GONE
                        txtEmptyView.visibility = View.VISIBLE
                    }

                } else {
                    Functions.showToast(this@NotificationActivityUSINGREQUST, notificationResponse.message)
                }
            }

            override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                loader!!.dismiss()
                Functions.showToast(this@NotificationActivityUSINGREQUST, getString(R.string.someting_want_wrong))
            }
        })
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle("Notification")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->  onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}