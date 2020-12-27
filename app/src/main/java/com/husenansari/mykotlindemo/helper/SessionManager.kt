package com.thepitch.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.thepitch.api.model.UserInfo

class SessionManager(context: Context) {

    private val filename = "BizCardPref"
    private val key = "user_key"
    private var preferences: SharedPreferences
    private var editor: SharedPreferences.Editor
    private val IS_LOGIN = "IsLoggedIn"
    private val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
    private val USER_KEY = "user_key"


    var isLoggedIn: Boolean
        get() = preferences.getBoolean("loggedIn", false)
        set(loggedIn) {
            editor.putBoolean("loggedIn", loggedIn).commit()
        }

    fun getUserData(): UserInfo? {
        return MyApplication.getGson().fromJson(
            preferences.getString(USER_KEY, ""),
            UserInfo::class.java
        )
    }

    fun setUserData(userData: UserInfo?) {
        editor = preferences.edit()
        val userToStr: String = Functions.getJsonToStr(userData!!)
        editor.putString(USER_KEY, userToStr)
        editor.apply()
    }


    var isFirstTime: Boolean
        get() = preferences.getBoolean("firstTime", true)
        set(firstTime) {
            editor.putBoolean("firstTime", firstTime).commit()
        }

    var isRememberMe: Boolean
        get() = preferences.getBoolean("rememberMe", true)
        set(rememberMe) {
            editor.putBoolean("rememberMe", rememberMe).commit()
        }

    var isDialogChecked: Boolean
        get() = preferences.getBoolean("dialogChecked", true)
        set(rememberMe) {
            editor.putBoolean("dialogChecked", rememberMe).commit()
        }


    init {
        preferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE)
        editor = preferences.edit()
        editor.apply()
    }

    fun isUserLogIn(): Boolean {
        return preferences.getBoolean(IS_LOGIN, false)
    }

    fun setUserLogin() {
        editor = preferences.edit()
        editor.putBoolean(IS_LOGIN, true)
        editor.apply()
    }

    fun setFirstTimeLaunch(isFirstTime: Boolean) {
        editor = preferences.edit()
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime)
        editor.apply()
    }

    fun isFirstTimeLaunch(): Boolean {
        return preferences.getBoolean(
            IS_FIRST_TIME_LAUNCH,
            true
        )
    }

    fun getUserID(): String? {
        return preferences.getString("userID", "")
    }

    fun setUserID(userID: String) {
        editor.putString("userID", userID).commit()
    }

    fun getToken(): String? {
        return preferences.getString("token", "")
    }

    fun setToken(token: String) {
        editor.putString("token", token).commit()
    }



    /*var userData: RegisterRequest
        get() = MyApplication.getGson().fromJson<RegisterRequest>(preferences.getString(key, ""), RegisterRequest::class.java)
        @SuppressLint("CommitPrefEdits")
        set(userData) {
            editor = preferences.edit()
            val userToStr = Functions.getJsonToStr(userData)
            editor.putString(key, userToStr)
            editor.apply()
        }*/

    fun getNotificationID(): Int {
        return preferences.getInt("notificationID", 0)
    }

    fun setNotificationID(notificationID: Int) {
        editor.putInt("notificationID", notificationID).commit()
    }

    @SuppressLint("CommitPrefEdits")
    fun logOut() {
        editor = preferences.edit()
        editor.clear()
        editor.apply()
    }
}