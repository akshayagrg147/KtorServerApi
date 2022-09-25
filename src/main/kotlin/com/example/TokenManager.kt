package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.example.src.data.Users
import io.ktor.server.config.*
import java.util.*

class TokenManager(val config: HoconApplicationConfig) {
    val audience = config.property("audience").getString()
    val secret = config.property("secret").getString()
    val issuer = config.property("issuer").getString()
    val expirationDate = System.currentTimeMillis() + 600000;

    fun generateJWTToken(user: Users): String {

        val token = JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("username", user.email)
            .withExpiresAt(Date(expirationDate))
            .sign(Algorithm.HMAC256(secret))
        return token
    }
    fun generateJWTExistToken(mobileNumber: String): String {

        val token = JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("JwtMobile", mobileNumber)
            .withExpiresAt(Date(expirationDate))
            .sign(Algorithm.HMAC256(secret))
        return token
    }

    fun verifyJWTToken(): JWTVerifier {
        return JWT.require(Algorithm.HMAC256(secret))
            .withAudience(audience)
            .withIssuer(issuer)
            .build()
    }
}
