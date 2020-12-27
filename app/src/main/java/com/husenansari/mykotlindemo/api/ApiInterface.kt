package com.thepitch.api

import com.thepitch.api.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

/*
interface ApiInterface {

    @FormUrlEncoded
    @POST(ApiConstant.LOGIN)
    fun login(@FieldMap params: Map<String, String>): Call<LoginResponse>

    @Headers("Content-Type:application/json")
    @POST(ApiConstant.EDIT_ADDRESS)
    fun edit_address(@Body editAddressRequest: EditAddressRequest?): Call<AddEditAddressResponse>

    @Headers("Content-Type:application/json")
    @GET(ApiConstant.GETCATEGORYTERMS)
    fun getcategoryterms(@Query("category_id") category_id: Int): Call<CategoryTerms>


    @Headers("Content-Type:application/json")
    @GET(ApiConstant.HESAFETY)
    fun hesafety(): Call<HESafeResponse>

     @Multipart
     @POST(ApiConstant.ADD_SERVICE)
     fun add_service(
         @Part("token") token: RequestBody?,
         @Part("service_id") service_id: RequestBody?,
         @Part("p_name") p_name: RequestBody?,
         @Part("description") description: RequestBody?,
         @Part("service_t") product_t: RequestBody?,
         @Part("selectcat") selectcat: RequestBody?,
         @Part("option") option: RequestBody?,
         @Part("url") url: RequestBody?,
         @Part vedio: MultipartBody.Part?,
         @Part pimage: MultipartBody.Part?,
         @Part image: ArrayList<MultipartBody.Part>?
     ): Call<DeleteResponse>


}*/
