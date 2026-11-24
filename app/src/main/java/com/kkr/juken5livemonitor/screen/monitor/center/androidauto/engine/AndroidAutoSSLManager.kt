package com.kkr.juken5livemonitor.screen.monitor.center.androidauto.engine

import android.content.Context
import android.util.Base64
import android.util.Log
import com.kkr.juken5livemonitor.R
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.security.KeyFactory
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import javax.net.ssl.*

class AndroidAutoSSLManager(private val context: Context) {

    private var sslContext: SSLContext? = null
    private var sslEngine: SSLEngine? = null
    
    // HUR uses direct buffers for better performance and alignment with native SSL
    private var txBuffer: ByteBuffer? = null
    private var rxBuffer: ByteBuffer? = null

    fun initialize() {
        try {
            Log.i("AAP_SSL", "Initializing HUR-Identical SSL Engine...")
            
            // 1. Load Certificate and Private Key (HUR Identity)
            val certFactory = CertificateFactory.getInstance("X.509")
            val certificate = certFactory.generateCertificate(context.resources.openRawResource(R.raw.cert)) as X509Certificate
            
            val keyContent = context.resources.openRawResource(R.raw.privkey).bufferedReader().use { it.readText() }
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\n", "").replace("\r", "").trim()
            
            val privateKey = KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(Base64.decode(keyContent, Base64.DEFAULT)))

            // 2. Setup KeyStore and Managers
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, null)
            keyStore.setKeyEntry("aa-client", privateKey, charArrayOf(), arrayOf(certificate))

            val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            kmf.init(keyStore, charArrayOf())

            val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            // 3. Initialize Context and Engine
            sslContext = SSLContext.getInstance("TLSv1.2")
            sslContext?.init(kmf.keyManagers, trustAll, SecureRandom())
            
            val engine = sslContext?.createSSLEngine("android-auto", 5277)!!
            engine.useClientMode = true
            sslEngine = engine
            
            // 4. Allocate Direct Buffers (HUR alignment)
            val session = engine.session
            txBuffer = ByteBuffer.allocateDirect(session.packetBufferSize)
            // HUR uses appBufferSize + 50, but at least 0x20008 (128KB)
            val appBufSize = Math.max(0x20008, session.applicationBufferSize + 50)
            rxBuffer = ByteBuffer.allocateDirect(appBufSize)
            
            engine.beginHandshake()
            Log.i("AAP_SSL", "SSL Manager Ready. Buffers: TX=${session.packetBufferSize}, RX=$appBufSize")
            
        } catch (e: Exception) {
            Log.e("AAP_SSL", "SSL Init Failed: ${e.message}")
        }
    }

    fun encrypt(src: ByteArray): ByteArray? {
        val engine = sslEngine ?: return null
        val tx = txBuffer ?: return null
        synchronized(this) {
            try {
                tx.clear()
                val result = engine.wrap(ByteBuffer.wrap(src), tx)
                handleHandshakeTasks(result.handshakeStatus)
                
                if (result.bytesProduced() > 0) {
                    tx.flip()
                    val out = ByteArray(tx.remaining())
                    tx.get(out)
                    return out
                }
            } catch (e: Exception) {
                Log.e("AAP_SSL", "Encrypt Error: ${e.message}")
            }
            return null
        }
    }

    fun decrypt(src: ByteArray): ByteArray? = decrypt(src, 0, src.size)

    fun decrypt(src: ByteArray, offset: Int, length: Int): ByteArray? {
        val engine = sslEngine ?: return null
        val rx = rxBuffer ?: return null
        synchronized(this) {
            try {
                rx.clear()
                val result = engine.unwrap(ByteBuffer.wrap(src, offset, length), rx)
                handleHandshakeTasks(result.handshakeStatus)
                
                if (result.bytesProduced() > 0) {
                    rx.flip()
                    val out = ByteArray(rx.remaining())
                    rx.get(out)
                    return out
                }
            } catch (e: Exception) {
                Log.e("AAP_SSL", "Decrypt Error: ${e.message}")
            }
            return null
        }
    }

    /**
     * High-Performance Direct Decryption
     * Decrypts from a ByteBuffer directly into another ByteBuffer.
     */
    fun decryptDirect(src: ByteBuffer, dst: ByteBuffer): Int {
        val engine = sslEngine ?: return -1
        synchronized(this) {
            try {
                val result = engine.unwrap(src, dst)
                handleHandshakeTasks(result.handshakeStatus)
                // Log.v("AAP_SSL", "Decrypted: ${result.bytesProduced()} bytes")
                return result.bytesProduced()
            } catch (e: Exception) {
                Log.e("AAP_SSL", "decryptDirect ERROR: ${e.message}")
                return -1
            }
        }
    }

    fun unwrapHandshake(src: ByteBuffer, onData: (ByteArray) -> Unit) {
        val engine = sslEngine ?: return
        val rx = rxBuffer ?: return
        try {
            while (src.hasRemaining()) {
                rx.clear()
                val result = engine.unwrap(src, rx)
                handleHandshakeTasks(result.handshakeStatus)
                doHandshakeAction(onData)
            }
        } catch (e: Exception) {
            Log.e("AAP_SSL", "Handshake Unwrap Error: ${e.message}")
        }
    }

    fun getHandshakeStatus(): SSLEngineResult.HandshakeStatus? = sslEngine?.handshakeStatus
    
    fun getSession(): SSLSession? = sslEngine?.session

    private fun handleHandshakeTasks(status: SSLEngineResult.HandshakeStatus) {
        if (status == SSLEngineResult.HandshakeStatus.NEED_TASK) {
            var task: Runnable?
            while (sslEngine?.delegatedTask.also { task = it } != null) {
                task?.run()
            }
        }
    }

    fun doHandshakeAction(onData: (ByteArray) -> Unit) {
        val engine = sslEngine ?: return
        val tx = txBuffer ?: return
        while (true) {
            val status = engine.handshakeStatus
            when (status) {
                SSLEngineResult.HandshakeStatus.NEED_WRAP -> {
                    tx.clear()
                    val result = engine.wrap(ByteBuffer.allocate(0), tx)
                    if (result.bytesProduced() > 0) {
                        tx.flip()
                        val out = ByteArray(tx.remaining())
                        tx.get(out)
                        onData(out)
                    }
                    handleHandshakeTasks(result.handshakeStatus)
                }
                SSLEngineResult.HandshakeStatus.NEED_TASK -> handleHandshakeTasks(status)
                else -> return
            }
        }
    }
}
