package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.example.features.customer.domain.modal.Users
import com.example.src.modal.RequestLoginBody
import com.example.utils.Constant
import io.ktor.server.config.*
import java.security.SecureRandom
import java.util.*

data class JwtConfig(val secret: String, val issuer: String, val audience: String)

fun generateToken(user: Users, jwtConfig: JwtConfig): String {
    val now = Date()
    val expiresAt = Date(now.time + (24 * 60 * 60 * 1000 )) // Token expires in 24 hours  into 30 days

    return JWT.create()
        .withSubject(user.userId)
        .withAudience(jwtConfig.audience)
        .withIssuer(jwtConfig.issuer)
        .withExpiresAt(expiresAt)
        .sign(Algorithm.HMAC256(jwtConfig.secret))
}
fun generateTokenAdmin(user: RequestLoginBody, jwtConfig: JwtConfig): String {
    val now = Date()
    val expiresAt = Date(now.time + (24 * 60 * 60 * 1000 )) // Token expires in 24 hours  into 30 days

    return JWT.create()
        .withSubject(user.email)
        .withAudience(jwtConfig.audience)
        .withIssuer(jwtConfig.issuer)
        .withExpiresAt(expiresAt)
        .sign(Algorithm.HMAC256(jwtConfig.secret))
}

fun generateRandomString(length: Int): String {
    val random = SecureRandom()
    val bytes = ByteArray(length)
    random.nextBytes(bytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
}

val secretKey = generateRandomString(32)

val jwtConfig = JwtConfig(
    secret = secretKey,
    issuer = Constant.ISSUER,
    audience = Constant.AUDIENCE
)