package com.example.menuku.core.utils

import android.content.Context

object GoogleSignInConfig {
    fun webClientId(context: Context): String {
        val id = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        check(id != 0) { "Login Google belum dikonfigurasi. Aktifkan provider Google di Firebase, tambahkan SHA sertifikat, lalu unduh ulang google-services.json. Login email tetap dapat digunakan." }
        return context.getString(id)
    }
}
