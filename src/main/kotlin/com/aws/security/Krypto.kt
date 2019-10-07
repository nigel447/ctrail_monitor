package com.aws.security

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.reactivex.observers.ResourceObserver
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.encoders.Hex
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec


data class KeyStoreWrapper(var symKePsswd: String, var keyStorePasswd: String, var keyStore:String )
data class CipherWithParams(val iv: ByteArray, val data: ByteArray)

object KryptoArtifactsLoader {

    fun keyStoreWrapper(): KeyStoreWrapper {
        return AppArtifacts.objectMapper.readValue(finFileInClassPath("/krypto.json"), KeyStoreWrapper::class.java )
    }

    fun monitor(): Map<String, String> {
        return AppArtifacts.objectMapper.readValue(finFileInClassPath("/monitor.json"),
            object : TypeReference<Map<String, String>>() {})
    }

    fun finFileInClassPath(fileName: String ): InputStream {
        return  object {}.javaClass.getResourceAsStream(fileName)
    }

}

sealed class Artifact {
    class access() : Artifact()
    class secret() : Artifact()
    class alias() : Artifact()
}

class KryptoSubscriber : ResourceObserver<Pair<Artifact, String>>() {
    override fun onComplete() {
        AppArtifacts.appLogger.info ("kryptoSubscriber onComplete ")
    }

    override fun onNext(t: Pair<Artifact, String>) {
        AppArtifacts.appLogger.info ("kryptoSubscriber on next ${t.second}")
        when (t.first) {
            is Artifact.access ->  KryptoStore.ACCESS_ID = t.second
            is Artifact.secret -> KryptoStore.SECRET = t.second
            is Artifact.alias -> KryptoStore.ALIAS = t.second
        }
    }

    override fun onError(e: Throwable) { }
}


object KryptoProvider {

    private fun getSecretKey(storePass: CharArray, keyPass: CharArray, JKS: String): SecretKey {

        val keyStore = KeyStore.getInstance("BKS", "BC")
        val inStream = ByteArrayInputStream(Base64.decode(JKS))
        keyStore.load(inStream, storePass);
        val keyPassProtection = KeyStore.PasswordProtection(keyPass)
        if(KryptoStore.ALIAS.isEmpty()) {
            AppArtifacts.appLogger.info ("KryptoProvider KryptoStore.ALIAS.isEmpty exiting")
            System.exit(1)
        }
        val entry: KeyStore.SecretKeyEntry =
            keyStore.getEntry(KryptoStore.ALIAS, keyPassProtection) as KeyStore.SecretKeyEntry;
        return entry.secretKey
    }


    private fun doDeCrypt(key: SecretKey, data: CipherWithParams): ByteArray {
        val cipher = Cipher.getInstance("AES/CFB/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(data.iv))
        val bytesD = cipher.doFinal(data.data)
        return bytesD
    }


    fun deCryptAWSKeys(access_secret: Pair<String, String>): Pair<String, String> {

        if(KryptoStore.JKS.isEmpty() ) {
            val artifacts  =  KryptoArtifactsLoader.keyStoreWrapper()
            KryptoStore.JKS =artifacts.keyStore
            KryptoStore.KEY_STORE_PASSWD =artifacts.keyStorePasswd
            KryptoStore.SYM_KEY_PSSWD =artifacts.symKePsswd
        }
        val aResultCP = AppArtifacts.objectMapper.readValue(access_secret.first, CipherWithParams::class.java)
        val sResultCP = AppArtifacts.objectMapper.readValue(access_secret.second, CipherWithParams::class.java)
        val key = getSecretKey(KryptoStore.KEY_STORE_PASSWD.toCharArray(), KryptoStore.SYM_KEY_PSSWD.toCharArray(), KryptoStore.JKS)
        val accessResult = doDeCrypt(key, aResultCP)
        val secretResult = doDeCrypt(key, sResultCP)
        return Pair(String(accessResult), String(secretResult))
    }

    fun basicPasswordCheck(password:String):Boolean {
        val md = MessageDigest.getInstance("SHA-512")
        md.update(password.trim().toByteArray(Charset.forName("UTF-8")))
        return AppArtifacts.BASIC_PSSWD.equals(String(Hex.encode(md.digest())))
    }

}
