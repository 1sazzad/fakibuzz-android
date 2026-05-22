package com.qarena.android.core.session

import com.qarena.android.data.remote.dto.UserResponse
import com.qarena.android.util.AcademicProfile

val UserResponse.isProfileComplete: Boolean
    get() = AcademicProfile.isProfileComplete(this)
