package com.thepitch.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.thepitch.R
import com.thepitch.api.model.UserInfo
import com.thepitch.helper.Functions
import com.thepitch.helper.MyApplication
import com.thepitch.helper.RoundImageView
import com.thepitch.helper.SessionManager
import com.thepitch.ui.navigationFragment.*
import com.thepitch.ui.navigationFragment.searcher.BookmarkProduct
import com.thepitch.ui.navigationFragment.searcher.SearcherHomeFragment
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap


class MainActivityUSINGHASHPARAM : AppCompatActivity() {

  /*  var loader: Dialog? = null
    private var sessionManager: SessionManager? = null
    private var pitcherProductPitchAdapter : PitcherProductPitchAdapter? = null
    private var rvPitchList : RecyclerView? = null
    private var productDetail: ArrayList<ProductDetail>? = null*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(activity!!)
        loader = Functions.getLoader(activity!!)


        rvPitchList = view.findViewById(R.id.rvPitchList)

        rvPitchList!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        productDetail = ArrayList();
        pitcherProductPitchAdapter = PitcherProductPitchAdapter(context, productDetail)
        rvPitchList!!.adapter = pitcherProductPitchAdapter



        if (Functions.isConnected(context)) {
            callGetPitchList()
        } else {
            Functions.showToast(context, getString(R.string.internet_error))
        }

    }

    fun callGetPitchList() {
        loader!!.show()
        val params: MutableMap<String, String> = HashMap()
        params["token"] = sessionManager!!.getUserData()!!.token
        params["pitcher_type"] = sessionManager!!.getUserData()!!.pitcher_type.toString()

        val api = MyApplication.getRetrofit().create(ApiInterface::class.java)
        api.product_pitcher_dashboard(params).enqueue(object : Callback<GetPitchResponse> {
            override fun onFailure(call: Call<GetPitchResponse>, t: Throwable) {
                loader!!.dismiss()
                Functions.showToast(context, "Something want wrong")
            }

            override fun onResponse(call: Call<GetPitchResponse>, response: Response<GetPitchResponse>) {
                loader!!.dismiss()
                val getPitchResponse: GetPitchResponse = response.body()!!

                if (getPitchResponse.flag == 1){
                    if (getPitchResponse.product_details.size > 0) {
                        rvPitchList!!.visibility = View.VISIBLE
                        txtEmptyView!!.visibility = View.GONE
                        productDetail = getPitchResponse.product_details
                        pitcherProductPitchAdapter!!.setTotalOrderList(productDetail!!)
                    }else{
                        rvPitchList!!.visibility = View.GONE
                        txtEmptyView!!.visibility = View.VISIBLE
                    }
                }else{
                    //Functions.showToast(context, getPitchResponse.message)
                    rvPitchList!!.visibility = View.GONE
                    txtEmptyView!!.visibility = View.VISIBLE
                    txtEmptyView.text = getPitchResponse.message
                }
            }
        })

    }
}
