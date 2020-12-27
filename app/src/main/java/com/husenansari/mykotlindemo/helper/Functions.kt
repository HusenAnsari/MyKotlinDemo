package com.thepitch.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.provider.Settings
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Patterns
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.Transformation
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import com.thepitch.R
import com.wang.avi.AVLoadingIndicatorView


object Functions {

    private const val FILE_UNIT_SIZE = 1024
    const val DEVICE = "1"
    private val webData: String? = null
    @SuppressLint("StaticFieldLeak")
    private val sessionManager: SessionManager? = null
    const val AUTHORITY = "com.bizcardscanner.android.fileprovider"

    fun setWebdata(webData: String): String {
        var webData = webData
        webData = "<html><head>" +
                "<link rel=\"stylesheet\" href=\"http://192.168.0.116/goalfundz/assets/css/bootstrap.min.css\" type=\"text/css\" />" +
                "<link rel=\"stylesheet\" href=\"http://192.168.0.116/goalfundz/assets/css/bootstrap.min.css\" type=\"text/css\" />" +
                "<link rel=\"stylesheet\" href=\"http://192.168.0.116/goalfundz/assets/frameworks/domprojects/css/style.css\" type=\"text/css\"/></head>" +
                "<body>" + webData + "</body></html>"
        return webData
    }

    fun getJsonToStr(objects: Any): String {
        return MyApplication.getGson().toJson(objects)
    }

    fun showToast(context: Context?, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    @SuppressLint("HardwareIds")
    fun getMobileId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver,
                Settings.Secure.ANDROID_ID)
    }

    fun isConnected(context: Context?): Boolean {

        val connectivityManager = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun toLength(mobile: String): Int {
        return mobile.trim { it <= ' ' }.length
    }

    fun isValidEmail(target: String): Boolean {
        val pattern = Patterns.EMAIL_ADDRESS
        val matcher = pattern.matcher(target)
        return matcher.matches()
    }

    fun expand(v: View) {
        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val targetHeight = v.measuredHeight

        v.layoutParams.height = 1
        v.visibility = View.VISIBLE
        val a = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                v.layoutParams.height = if (interpolatedTime == 1f)
                    LinearLayout.LayoutParams.WRAP_CONTENT
                else
                    (targetHeight * interpolatedTime).toInt()
                v.requestLayout()
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }


        a.duration = (targetHeight / v.context.resources.displayMetrics.density).toInt().toLong()
        v.startAnimation(a)
    }

    fun collapse(v: View) {
        val initialHeight = v.measuredHeight
        val a = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                if (interpolatedTime == 1f) {
                    v.visibility = View.GONE
                } else {
                    v.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                    v.requestLayout()
                }
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }
        a.duration = (initialHeight / v.context.resources.displayMetrics.density).toInt().toLong()
        v.startAnimation(a)
    }

    fun convertDpToPixel(dp: Float, context: Context): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }


    fun convertPixelsToDp(px: Float, context: Context): Float {
        return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun isValidEmailMain(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }


    fun getLoader(context: Context): Dialog {
        val loaderDialog = Dialog(context)
        val window = loaderDialog.window
        window!!.requestFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawableResource(R.drawable.dialog_basic_transparent)
        loaderDialog.setContentView(R.layout.loader)
        window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        loaderDialog.setCancelable(false)
        val loadingIndicatorView = loaderDialog.findViewById<View>(R.id.loader) as AVLoadingIndicatorView
        loaderDialog.setOnShowListener { loadingIndicatorView.show() }
        loaderDialog.setOnDismissListener { loadingIndicatorView.hide() }
        return loaderDialog
    }

    fun getFileSizeInMB(fileLength: Long?): Long {
        return fileLength!! / (FILE_UNIT_SIZE * FILE_UNIT_SIZE)
    }

    fun hideSoftKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)

    }


}