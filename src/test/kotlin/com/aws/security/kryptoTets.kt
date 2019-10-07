package com.aws.security

import org.bouncycastle.util.encoders.Hex
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.charset.Charset
import java.security.MessageDigest

class KryptoTest : ClientTestBase() {

    val psswd = "simplepassword"
    val alias = "thisisanalias"

    @Test
    fun hashTest() {
        val md = MessageDigest.getInstance("SHA-512")
        md.update(psswd.toByteArray(Charset.forName("UTF-8")))
        val digest = md.digest()
        Assertions.assertEquals(AppArtifacts.BASIC_PSSWD, String(Hex.encode(digest)))
    }

}