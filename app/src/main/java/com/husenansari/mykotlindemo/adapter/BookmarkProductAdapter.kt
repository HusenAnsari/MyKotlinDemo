package com.thepitch.adapter

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.thepitch.api.ApiInterface
import com.thepitch.api.model.AddBookmarkedResponse
import com.thepitch.api.model.DeleteBookmarkedResponse
import com.thepitch.api.model.ProductDetail
import com.thepitch.api.model.ProductDetailX
import com.thepitch.helper.Functions
import com.thepitch.helper.MyApplication
import com.thepitch.helper.SessionManager
import com.thepitch.ui.navigationFragment.searcher.BookmarkedSearchProduct
import com.thepitch.ui.navigationFragment.searcher.BookmarkedSearchResume
import com.thepitch.ui.navigationFragment.searcher.SearcherPitchDetailActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

class BookmarkProductAdapter(
    private var context: Context?,
    private var productDetail: ArrayList<ProductDetailX>?,
    private var searchProduct: BookmarkedSearchProduct

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var loader: Dialog? = null
    private var sessionManager: SessionManager? = null
    private var fragment: BookmarkedSearchProduct? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_product_list, parent, false)
        sessionManager = SessionManager(context!!)
        loader = Functions.getLoader(context!!)
        fragment = searchProduct
        return GetPitchViewHolder(view)

    }


    override fun getItemCount(): Int {
        return productDetail!!.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val getPitchViewHolder = holder as GetPitchViewHolder
        productDetail?.get(position).let { getPitchViewHolder.setValue(it) }
    }

    fun setTotalOrderList(productDetail: ArrayList<ProductDetailX>) {
        this.productDetail = productDetail
        notifyDataSetChanged()
    }

    fun setSearchList(dealInformationBeanArrayList: java.util.ArrayList<ProductDetailX>) {
        this.productDetail = dealInformationBeanArrayList
        notifyDataSetChanged()
    }

    private inner class GetPitchViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {

        private val txtProductName: TextView = view!!.findViewById(R.id.txtProductName)
        private val txtShareDeal: TextView = view!!.findViewById(R.id.txtShareDeal)
        private val txtViewDetail: TextView = view!!.findViewById(R.id.txtViewDetail)
        private val imgProduct: ImageView = view!!.findViewById(R.id.imgProduct)
        private val imgBookmark: ImageView = view!!.findViewById(R.id.imgBookmark)


        fun setValue(productDetail: ProductDetailX?) {

            Glide.with(context!!)
                .load(productDetail!!.product_image)
              //  .placeholder(R.drawable.no_image)
                .into(imgProduct)

            txtProductName.text = productDetail.product_name

            // if (productDetail.bookmarked == "1") {
            imgBookmark.setImageResource(R.drawable.bookmark_fill);
            /* } else {
                 imgBookmark.setImageResource(R.drawable.bookmark);
             }*/

            imgBookmark.setOnClickListener {

                 callDeleteBookmarkedApi(productDetail)

            }

            txtViewDetail.setOnClickListener {

               /* val intent = Intent(context, SearcherPitchDetailActivity::class.java)
                intent.putExtra("product_id",productDetail.id)
                context!!.startActivity(intent)*/

            }
        }
    }

    private fun callDeleteBookmarkedApi(productDetail: ProductDetailX) {
        loader!!.show()

        val params: MutableMap<String, String> = HashMap()
        params["token"] = sessionManager!!.getUserData()!!.token
        params["id"] = productDetail.bid


        val api = MyApplication.getRetrofit().create(ApiInterface::class.java)
        api.delete_bookmarked(params).enqueue(object : Callback<DeleteBookmarkedResponse> {
            override fun onFailure(call: Call<DeleteBookmarkedResponse>, t: Throwable) {
                loader!!.dismiss()
                Functions.showToast(context, "Something want wrong")
            }

            override fun onResponse(
                call: Call<DeleteBookmarkedResponse>,
                response: Response<DeleteBookmarkedResponse>
            ) {
                loader!!.dismiss()
                val deleteBookmarkedResponse: DeleteBookmarkedResponse = response.body()!!
                if (deleteBookmarkedResponse.flag == 1) {
                    Functions.showToast(context, deleteBookmarkedResponse.message)
                    fragment!!.callGetPitchList()
                } else {
                    Functions.showToast(context, deleteBookmarkedResponse.message)
                }
            }

        })
    }
}