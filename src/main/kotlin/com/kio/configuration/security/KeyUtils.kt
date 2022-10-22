package com.kio.configuration.security

import com.nimbusds.jose.jwk.RSAKey
import org.springframework.core.io.ClassPathResource
import java.io.IOException
import java.nio.file.Files
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

object KeyUtils {

    private val keyFactory = KeyFactory.getInstance("RSA")

    fun getRsaKey(): RSAKey {
        val privateKey = getPrivateKey() as RSAPrivateKey
        val publicKey = getPublicKey() as RSAPublicKey
        return RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID("kio")
            .build()
    }

    private fun getPrivateKey(): PrivateKey {
        val privateKeyResource = ClassPathResource("keys/private_key.der")

        try{
            val bytes = Files.readAllBytes(privateKeyResource.file.toPath())
            val keySpec = PKCS8EncodedKeySpec(bytes)
            return keyFactory.generatePrivate(keySpec)
        }catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun getPublicKey(): PublicKey {
        val publicKeyResource = ClassPathResource("keys/public_key.der")

        try{
            val bytes = Files.readAllBytes(publicKeyResource.file.toPath())
            val keySpec = X509EncodedKeySpec(bytes)
            return keyFactory.generatePublic(keySpec)
        }catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

}