package com.example.utils


import java.util.regex.Pattern




fun validateEmail(email: String) = Pattern.compile(Constant.EMAIL_PATTERN).matcher(email).matches()
