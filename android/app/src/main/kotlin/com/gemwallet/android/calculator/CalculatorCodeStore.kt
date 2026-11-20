package com.gemwallet.android.calculator

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class CalculatorCodeStore(private val prefs: SharedPreferences) {

    constructor(context: Context) : this(
        context.getSharedPreferences("calculator_code_prefs", Context.MODE_PRIVATE)
    )

    fun hasCode(): Boolean {
        return prefs.contains(KEY_HASH) && prefs.contains(KEY_SALT)
    }

    fun setCode(code: String): Boolean {
        if (!isValidCode(code)) return false
        val salt = generateSalt()
        val hash = hashPin(code, salt)
        prefs.edit()
            .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
            .apply()
        return true
    }

    fun verifyCode(code: String): Boolean {
        if (!isValidCode(code)) return false
        val saltBase64 = prefs.getString(KEY_SALT, null) ?: return false
        val hashBase64 = prefs.getString(KEY_HASH, null) ?: return false
        val salt = try { Base64.decode(saltBase64, Base64.DEFAULT) } catch (e: Exception) { return false }
        val expectedHash = try { Base64.decode(hashBase64, Base64.DEFAULT) } catch (e: Exception) { return false }
        val actualHash = hashPin(code, salt)
        return MessageDigest.isEqual(expectedHash, actualHash)
    }

    fun isValidCode(code: String): Boolean {
        return code.length in 4..10 && code.all { it.isDigit() }
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private fun hashPin(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, 120000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }

    companion object {
        private const val KEY_SALT = "key_salt"
        private const val KEY_HASH = "key_hash"
    }
}
