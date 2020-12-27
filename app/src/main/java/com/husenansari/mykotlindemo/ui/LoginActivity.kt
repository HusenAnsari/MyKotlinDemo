package com.thepitch.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.thepitch.R
import com.thepitch.api.ApiInterface
import com.thepitch.api.model.ForgotPasswordResponse
import com.thepitch.api.model.LoginResponse
import com.thepitch.api.model.RegisterResponse
import com.thepitch.helper.Functions
import com.thepitch.helper.MyApplication
import com.thepitch.helper.SessionManager
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.custom_forgot_password_dialog.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class LoginActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {
    private var radioButonUserType: RadioButton? = null
    private var userType: String? = null
    private var sessionManager: SessionManager? = null
    var loader: Dialog? = null
    var context: Context? = null
    private var isPasswordVisible: Boolean = false
    private var loginManager: LoginManager? = null
    private var callbackManager: CallbackManager? = null
    var isPitcher: Boolean? = null

    var signInButton: SignInButton? = null
    private var googleApiClient: GoogleApiClient? = null
    private val RC_SIGN_IN = 1

    // var isPitcher: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        callbackManager = CallbackManager.Factory.create()
        loginManager = LoginManager.getInstance()
        context = this
        loader = Functions.getLoader(this)
        sessionManager = SessionManager(this)


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()


        llSIgnUp.setOnClickListener {
            //openRegisterAsADialog()
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        txtLogin.setOnClickListener {
            checkValidation()

        }

        imgFacebook.setOnClickListener {
            if (Functions.isConnected(this.context!!)) {
                //loader!!.show()
                loginManager!!.logInWithReadPermissions(
                    this@LoginActivity,
                    listOf("email", "public_profile")
                )
            } else {
                Functions.showToast(this.context!!, R.string.message_noInternet.toString())
            }
        }

        imgGoogle!!.setOnClickListener {
            val intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
            startActivityForResult(intent, RC_SIGN_IN)
        }

        imgLoginPasswordInvisible.setOnClickListener {
            val password = edt_login_password.text.toString()
            if (password.isNotEmpty()) {
                if (isPasswordVisible) {
                    edt_login_password.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                    edt_login_password.transformationMethod =
                        PasswordTransformationMethod.getInstance()
                    edt_login_password.setSelection(password.length)
                    isPasswordVisible = false
                    imgLoginPasswordInvisible.setImageResource(R.drawable.ic_visibility_off)
                } else {
                    edt_login_password.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    isPasswordVisible = true
                    edt_login_password.setSelection(password.length)
                    imgLoginPasswordInvisible.setImageResource(R.drawable.ic_visibility)
                }
            }
        }

        loginManager!!.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.e("token", AccessToken.getCurrentAccessToken().toString())
                //loader!!.dismiss()
                val request = GraphRequest.newMeRequest(loginResult.accessToken) { `object`, _ ->
                    Log.e("access--  ", loginResult.accessToken.toString())
                    setProfileData(`object`)
                }
                val parameters = Bundle()
                //parameters.putString("fields", "id,name,email,gender, birthday")
                parameters.putString("fields", "name, email, id")
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onCancel() {
                //loader!!.dismiss()
                Functions.showToast(context!!, "Login Cancelled")
            }

            override fun onError(error: FacebookException) {
                //loader!!.dismiss()
                Log.e("Login Error :", error.message);
            }
        })

        txt_forgot_login.setOnClickListener {
            openForgotPasswordDialog()
        }

        rbLoginPitcher.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                isPitcher = true
                llFacebook.visibility = View.GONE
            } else {
                isPitcher = false
                llFacebook.visibility = View.VISIBLE
            }
        }


    }

    private fun openForgotPasswordDialog() {
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_forgot_password_dialog)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        dialog.setCancelable(true)
        dialog.window!!.attributes = lp

        dialog.txtForgotPasswordSubmit.setOnClickListener {
            checkForgotValidation(dialog)
        }

        dialog.show()
    }

    private fun checkForgotValidation(dialog: Dialog) {

        if (dialog.edt_forgot_email.text.isEmpty()) {
            Functions.showToast(this, "Enter email")
            return
        }

        if (Functions.isConnected(this.context!!)) {
            callForgotAPI(dialog)
        } else {
            Functions.showToast(this.context!!, R.string.message_noInternet.toString())
        }
    }

    private fun callForgotAPI(dialogMain: Dialog) {
        loader!!.show()

        val params: MutableMap<String, String> = HashMap()
        params["email"] = dialogMain.edt_forgot_email.text.toString()

        val api = MyApplication.getRetrofit().create(ApiInterface::class.java)
        api.forgot_password(params).enqueue(object : Callback<ForgotPasswordResponse> {
            override fun onFailure(call: Call<ForgotPasswordResponse>, t: Throwable) {
                loader!!.dismiss()
                Functions.showToast(this@LoginActivity, "Something want wrong")
            }

            override fun onResponse(
                call: Call<ForgotPasswordResponse>,
                response: Response<ForgotPasswordResponse>
            ) {
                loader!!.dismiss()
                val forgotPasswordResponse: ForgotPasswordResponse = response.body()!!
                if (forgotPasswordResponse.flag == 1) {

                    val builder =
                        AlertDialog.Builder(this@LoginActivity)
                    builder.setMessage(forgotPasswordResponse.message)
                        .setCancelable(false)
                        .setPositiveButton(
                            "OK"
                        ) { dialog, id ->
                            dialogMain.dismiss()
                            dialog.dismiss()
                        }
                    val alert = builder.create()
                    alert.show()
                } else {
                    loader!!.dismiss()
                    Functions.showToast(this@LoginActivity, forgotPasswordResponse.message)
                }

            }
        })

    }

    private fun checkValidation() {
        sessionManager!!.logOut()
        val selectedId: Int = rgToggle.checkedRadioButtonId
        radioButonUserType = findViewById(selectedId) as? RadioButton

        if (radioButonUserType!!.text == "Pitcher Login") {
            userType = "2"
        } else if (radioButonUserType!!.text == "Searcher Login") {
            userType = "1"
        }

        if (edt_login_username.text.isEmpty()) {
            Functions.showToast(this, "Enter email")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(edt_login_username.text).matches()) {
            Functions.showToast(this, "Please enter proper email")
            return
        }

        if (edt_login_password.text.isEmpty()) {
            Functions.showToast(this, "Enter password")
            return
        }


        if (Functions.isConnected(this.context!!)) {
            callLoginAPI()
        } else {
            Functions.showToast(this.context!!, R.string.message_noInternet.toString())
        }
    }

    private fun callLoginAPI() {

        loader!!.show()

        val params: MutableMap<String, String> = HashMap()
        params["user_type"] = userType.toString()
        params["email"] = edt_login_username.text.toString()
        params["password"] = edt_login_password.text.toString()
        params["fcm_token"] = ""
        params["register_type"] = "0"   // 0 - simple registration    1- facebook

        //Log.e("login", Gson().toJson(params))
        val api = MyApplication.getRetrofit().create(ApiInterface::class.java)
        api.login(params).enqueue(object : Callback<LoginResponse> {
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                loader!!.dismiss()
                Functions.showToast(this@LoginActivity, "Something want wrong")
            }

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                loader!!.dismiss()
                val loginResponse: LoginResponse = response.body()!!
                if (loginResponse.flag == 1) {
                    sessionManager!!.setUserLogin()
                    sessionManager!!.setFirstTimeLaunch(false)
                    sessionManager!!.setUserData(loginResponse.user_info)
                    sessionManager!!.setUserID(loginResponse.user_info.u_type)

                    val intent = Intent(context, MainActivityUSINGHASHPARAM::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()

                } else {
                    val builder =
                        AlertDialog.Builder(this@LoginActivity)
                    builder.setMessage(loginResponse.message)
                        .setCancelable(false)
                        .setPositiveButton(
                            "OK"
                        ) { dialog, id ->
                            /* val intent = Intent(context, LoginActivity::class.java)
                             intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                             startActivity(intent)
                             finish()
 */
                            dialog.dismiss()
                        }
                    val alert = builder.create()
                    alert.show()
                }
            }
        })

    }

    private fun openRegisterAsADialog() {
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_dialog_register_as)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        dialog.window!!.attributes = lp
        val txtPitcher =
            dialog.findViewById<TextView>(R.id.txtPitcher)
        val txtSearcher = dialog.findViewById<TextView>(R.id.txtSearcher)

        txtPitcher.setOnClickListener {
            // gotoRegister("1")
            dialog.dismiss()
        }

        txtSearcher.setOnClickListener {
            // gotoRegister("2")
            dialog.dismiss()
        }

        dialog.show()

    }

    private fun gotoRegister(usertype: String) {
        val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
        intent.putExtra("userType", usertype)
        startActivity(intent)
    }

    private fun setProfileData(jsonObject: JSONObject) {
        Log.e("json ", jsonObject.toString())
        val profile =
            "https://graph.facebook.com/" + jsonObject.getString("id") + "/picture?width=200&height=150"
        val name = jsonObject.getString("name")
        val id = jsonObject.getString("id")
        var email: String? = null
        var mobile: String? = null

        Log.e("name", jsonObject.getString("name"))
        Log.e("id", jsonObject.getString("id"))
        Log.e("profile", profile)

        if (jsonObject.has("email")) {
            Log.e("email", jsonObject.getString("email"))
            email = jsonObject.getString("email")
        } else {
            email = ""
        }

        /* if (jsonObject.has("mobile_phone")) {
             Log.e("email", jsonObject.getString("mobile_phone"))
             mobile = jsonObject.getString("mobile_phone")
         } else {
             mobile = "Email not available"
         }
         Log.e("mobile", mobile)*/

        /*  Log.e("profile", profile)
          Log.e("name", name)
          Log.e("email", email)
  */
        if (Functions.isConnected(this.context!!)) {
            callRegisterAPI(profile, name, email, id, "1")
        } else {
            Functions.showToast(this.context!!, R.string.message_noInternet.toString())
        }

    }

    private fun callRegisterAPI(
        profile: String,
        name: String,
        email: String?,
        id: String,
        loginType: String   // 1 - facebook ,  2 - google
    ) {

        loader!!.show()

        val params: MutableMap<String, String> = HashMap()

        params["pitcher_type"] = ""
        params["user_type"] = "1"
        params["fullname"] = name
        params["phone"] = ""
        params["email"] = email.toString()
        params["username"] = name
        params["password"] = ""
        params["device_id"] = "1"
        params["fcm_token"] = ""
        params["register_type"] = loginType   // 0 - simple registration    1- facebook  2 - gmail
        params["register_id"] = id   // facebook | google login account id

        Log.e("reg", Gson().toJson(params))


        val api = MyApplication.getRetrofit().create(ApiInterface::class.java)
        api.register(params).enqueue(object : Callback<RegisterResponse> {
            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                loader!!.dismiss()
                Functions.showToast(this@LoginActivity, "Something want wrong")
            }

            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                loader!!.dismiss()
                val registerResponse: RegisterResponse = response.body()!!
                if (registerResponse.flag == 1) {
                    sessionManager!!.setUserLogin()
                    sessionManager!!.setFirstTimeLaunch(false)
                    sessionManager!!.setUserData(registerResponse.user_info)
                    sessionManager!!.setUserID(registerResponse.user_info.u_type)
                    fbSignOut()
                    googleSignOut()
                    val intent = Intent(context, MainActivityUSINGHASHPARAM::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()


                } else {
                    val builder =
                        AlertDialog.Builder(this@LoginActivity)
                    builder.setMessage(registerResponse.message)
                        .setCancelable(false)
                        .setPositiveButton(
                            "OK"
                        ) { dialog, id ->
                           /* val intent = Intent(context, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                            finish()
*/
                            dialog.dismiss()
                        }
                    val alert = builder.create()
                    alert.show()

                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode === RC_SIGN_IN) {

            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)

        }

    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            var profile: String? = null
            if (account!!.photoUrl.toString() != null) {
                profile = account!!.photoUrl.toString()
            }

            var name = account!!.displayName
            var id = account!!.id
            var email = account!!.email

            Log.e("name", account!!.displayName)
            Log.e("id", account!!.id)
            Log.e("email", account!!.email)
            Log.e("image", account!!.photoUrl.toString())



            if (Functions.isConnected(this.context!!)) {
                callRegisterAPI(profile!!, name!!, email!!, id!!, "2")
            } else {
                Functions.showToast(this.context!!, R.string.message_noInternet.toString())
            }


        } catch (e: ApiException) {

            Log.w("TAG", "signInResult:failed code=" + e.getStatusCode())

            Functions.showToast(context, "Something went wrong please try after sometime")
        }
    }

    private fun fbSignOut() {
        loginManager!!.logOut()
        //LoginManager.getInstance().logOut();
    }

    private fun googleSignOut() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context!!, gso)
        googleSignInClient.signOut()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }
}
