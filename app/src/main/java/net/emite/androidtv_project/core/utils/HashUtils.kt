package net.emite.androidtv_project.core.utils

import java.security.MessageDigest

object HashUtils {
    fun sha256(input: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}
