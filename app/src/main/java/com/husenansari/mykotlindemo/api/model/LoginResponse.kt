package com.thepitch.api.model

data class LoginResponse(
    val flag: Int,
    val message: String,
    val user_info: UserInfo
)