package com.husenansari.mykotlindemo.ui

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.hashtechhub.mrhomeexpert.R
import com.hashtechhub.mrhomeexpert.api.ApiInterface
import com.hashtechhub.mrhomeexpert.api.model.*
import com.hashtechhub.mrhomeexpert.helper.MyApplication
import com.hashtechhub.mrhomeexpert.helper.SharedPrefManager
import com.timechart.tctracker.helper.Functions
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.fragment_signup.*
import kotlinx.android.synthetic.main.item_viewpager_slider.view.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.util.*


class EditProfileActivityIMAGEUPLOAD : BaseActivity() {
    val REQUEST_IMAGE_CAPTURE = 485
    val REQUEST_COMPANY_LOGO = 369
    private val REQUEST_START_CAMERA = 456
    private val REQUEST_READ_STORAGE_PERMISSION = 324
    var base64Image : String? = null
    var selectedLogoPath: String? = null

    var loader: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initToolbar()
        initView()

    }

    private fun initView() {
        loader = Functions.getLoader(this)

        if (Functions.isConnected(this)) {
            callGetProfile()
        } else {
            Functions.showToast(this, getString(R.string.internet_error))
        }

        btnUpdateProfile.setOnClickListener {
            checkValidation()
        }

        imgUser.setOnClickListener {
            askForChoosingFrom()
        }
    }

    private fun askForChoosingFrom() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Image From")
        val items = arrayOf("Camera", "Gallery")
        builder.setItems(items) { dialogInterface, i ->
            if (i == 0) {
                selectPictureUsingCamera()
            } else {
                requestToSelectPicture()
            }
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun selectPictureUsingCamera() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_START_CAMERA
            )
            return
        }
        startCamera()
    }

    private fun requestToSelectPicture() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_READ_STORAGE_PERMISSION
            )
            return
        }
        startToSelectLogoFromGallery()
    }

    fun startToSelectLogoFromGallery() {
        var intent: Intent? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        } else {
            intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
        }
        intent.type = "image/*"
        startActivityForResult(
            Intent.createChooser(intent, "Select Using"),
            REQUEST_COMPANY_LOGO
        )
    }

    private fun startCamera() {
        /*if (intent.resolveActivity(this.getPackageManager()) != null) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        } else {
            Functions.showToast(this, "Camera couldn't started!")
        }*/

       /* val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(this.getPackageManager()) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (photoFile != null) {
                val uri: Uri = FileProvider.getUriForFile(
                    this,
                    "com.hashtechhub.mrhomeexpert.android.fileprovider",
                    photoFile
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                startActivityForResult(
                    intent,
                    REQUEST_IMAGE_CAPTURE
                )
            } else {
                Functions.showToast(this, "Unable to access storage!")
            }
        } else {
            Functions.showToast(this, "Camera couldn't started!")
        }*/

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_START_CAMERA) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) === PackageManager.PERMISSION_GRANTED) {
                startCamera()
            }
        }
        if (requestCode == REQUEST_READ_STORAGE_PERMISSION && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startToSelectLogoFromGallery()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_COMPANY_LOGO)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_IMAGE_CAPTURE)
                 onCaptureImageResult(data!!);
        }


    }

    private fun onSelectFromGalleryResult(data: Intent?) {
        Log.e("onSelectFromGallery", "onSelectFromGalleryResult")
       // lateinit var bitmap : Bitmap
        if (data != null) {
            val selectedImage: Uri? = data.data
            try {

                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
                val bytes = ByteArrayOutputStream()
                bitmap!!.compress(Bitmap.CompressFormat.JPEG, 90, bytes)

                //val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
                imgUser.setImageBitmap(bitmap)
                base64Image = encodeTobase64(bitmap)
               // longLog(base64Image!!)
            } catch (e: IOException) {
                e.printStackTrace()
            }

          /*  try {
                selectedImage?.let {
                    if(Build.VERSION.SDK_INT < 28) {
                        val bitmap = MediaStore.Images.Media.getBitmap(
                            this.contentResolver,
                            selectedImage
                        )
                        imgUser.setImageBitmap(bitmap)
                    } else {
                        val source = ImageDecoder.createSource(this.contentResolver, selectedImage)
                        val bitmap = ImageDecoder.decodeBitmap(source)
                        val converted: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
                        imgUser.setImageBitmap(converted)

                        *//*imgUser.setImageBitmap(
                            decodeSampledBitmapFromResource(resources, R.id.imgUser, 100, 100)
                        )*//*
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }*/

        }

    }

   
    private fun onCaptureImageResult(data: Intent) {
        Log.e("onCaptureImageResult", "onCaptureImageResult")
        val bitmap = data.extras!!["data"] as Bitmap?
        val bytes = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val destination = File(
            Environment.getExternalStorageDirectory(),
            System.currentTimeMillis().toString() + ".jpg"
        )
        val fo: FileOutputStream
        try {
            destination.createNewFile()
            fo = FileOutputStream(destination)
            fo.write(bytes.toByteArray())
            fo.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        imgUser.setImageBitmap(bitmap)
        base64Image = encodeTobase64(bitmap)
        Log.e("TAG", base64Image!!)
    }

    private fun checkValidation() {
        if (edtUserName.text!!.isEmpty()) {
            Functions.showToast(this, "Please enter name")
            return
        }

        if (edtUserMobile.text!!.isEmpty()) {
            Functions.showToast(this, "Please enter mobile")
            return
        }

        if (edtUserEmail.text!!.isEmpty()) {
            Functions.showToast(this, "Please enter email")
            return
        }


        if (Functions.isConnected(this)) {
            callUpdateProfile()
        } else {
            Functions.showToast(this, getString(R.string.internet_error))
        }
    }

    private fun callUpdateProfile() {
        loader!!.show()

        val editProfileRequest = EditProfileRequest()
        editProfileRequest.user_id = SharedPrefManager.getUserID(this).toString()
        editProfileRequest.name = edtUserName.text!!.toString()
        editProfileRequest.email = edtUserEmail.text!!.toString()
        editProfileRequest.phone = edtUserMobile.text!!.toString()
        editProfileRequest.profile_pic = base64Image

        //longLog(base64Image!!)


        val api = MyApplication.getRetrofit().create(ApiInterface::class.java)
        api.edit_profile(editProfileRequest).enqueue(object : Callback<EditProfileResponse> {
            override fun onResponse(
                call: Call<EditProfileResponse>,
                response: Response<EditProfileResponse>
            ) {
                loader!!.dismiss()
                val editProfileResponse: EditProfileResponse = response.body()!!

                if (editProfileResponse.status_code == 200) {
                    Functions.showToast(this@EditProfileActivityIMAGEUPLOAD, editProfileResponse.message)
                    onBackPressed()
                } else {
                    Functions.showToast(this@EditProfileActivityIMAGEUPLOAD, editProfileResponse.message)
                }
            }

            override fun onFailure(call: Call<EditProfileResponse>, t: Throwable) {
                loader!!.dismiss()
                Functions.showToast(
                    this@EditProfileActivityIMAGEUPLOAD,
                    getString(R.string.someting_want_wrong)
                )
            }
        })
    }

    private fun callGetProfile() {
        loader!!.show()

        val api = MyApplication.getRetrofit().create(ApiInterface::class.java)
        api.profile(SharedPrefManager.getUserID(this)).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(
                call: Call<ProfileResponse>,
                response: Response<ProfileResponse>
            ) {
                loader!!.dismiss()
                val profileResponse: ProfileResponse = response.body()!!

                if (profileResponse.status_code == 200) {
                    setUserProfile(profileResponse.data.profile)
                } else {
                    Functions.showToast(this@EditProfileActivityIMAGEUPLOAD, profileResponse.message)
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                loader!!.dismiss()
                Functions.showToast(
                    this@EditProfileActivityIMAGEUPLOAD,
                    getString(R.string.someting_want_wrong)
                )
            }
        })
    }

    private fun setUserProfile(profile: Profile) {
        edtUserName.setText(profile.name)
        edtUserMobile.setText(profile.phone)
        edtUserEmail.setText(profile.email)

        Glide.with(this)
            .load(profile.profile_picture)
            .placeholder(R.drawable.ic_profile_user)
            .into(imgUser)
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Edit Profile"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    fun encodeTobase64(image: Bitmap): String {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        val imageEncoded: String = Base64.encodeToString(b, Base64.DEFAULT)
        return imageEncoded
    }
}