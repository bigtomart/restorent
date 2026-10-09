package com.example.data.util

import java.security.MessageDigest
import java.security.SecureRandom

object PasswordHasher {
  fun generateSalt(): String {
    val random = SecureRandom()
    val salt = ByteArray(16)
    random.nextBytes(salt)
    return salt.joinToString("") { "%02x".format(it) }
  }

  fun hashPassword(password: String, salt: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val input = (salt + password).toByteArray(Charsets.UTF_8)
    val bytes = md.digest(input)
    return bytes.joinToString("") { "%02x".format(it) }
  }

  fun verify(password: String, salt: String, hash: String): Boolean {
    if (salt.isEmpty() || hash.isEmpty() || password.isEmpty()) return false
    val calculated = hashPassword(password, salt)
    return calculated.equals(hash, ignoreCase = true)
  }
}
