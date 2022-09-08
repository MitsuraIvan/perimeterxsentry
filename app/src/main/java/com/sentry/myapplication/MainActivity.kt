package com.sentry.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {
    private var scopeRunner = CoroutineScope(Dispatchers.IO)
    private val perimeterX: PerimeterXImpl = PerimeterXImpl(TestApplication.instance)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.go).setOnClickListener {
            scopeRunner.cancel()

            scopeRunner = CoroutineScope(Dispatchers.Default)
            scopeRunner.launch {

                while (perimeterX.vid.isEmpty()) {
                    Log.d("request", "perimeterX is not ready yet")
                    delay(500)
                }
                Log.d("request", "perimeterX is ready: ${perimeterX.vid}")

                val testUrl = "https://services.avaapiweb.com/api/OptionsMobile/Login"
                val testHeader = ArrayList<Pair<String, String>>().apply {
                    add(Pair<String, String>(first = "User-Agent", second = "PhantomJS"))
                    add(Pair<String, String>(first = "x-px-block", second = "1"))
                    addAll(perimeterX.headers.map {
                        Pair(first = it.key, second = it.value)
                    })
                }

                val json = "{ \"username\" : \"daasdasdfsadf@gmail.com\",  \"password\" : \"asdASD1234as\",  \"whitelabel\" : \"SENTRY\",  \"appsrc\" : \"Options\",  \"lang\" : \"en\",  \"decryptExternal\" : \"false\"}"

                var response = ""
                try {
                    //base POST
                    val url = java.net.URL(testUrl)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.readTimeout = 60000
                    conn.connectTimeout = 60000
                    conn.requestMethod = "POST"
                    conn.doOutput = true
                    conn.doInput = true

                    //headers
                    testHeader.forEach {
                        conn.setRequestProperty(it.first, it.second)
                    }

                    //json
                    conn.setRequestProperty("Content-Type", "application/json")
                    val os = conn.outputStream
                    val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
                    writer.write(json)
                    writer.flush()
                    writer.close()
                    os.close()

                    //read
                    val responseCode = conn.responseCode

                    val stream = if (responseCode == HttpsURLConnection.HTTP_OK) conn.inputStream else conn.errorStream
                    val br = BufferedReader(InputStreamReader(stream))
                    br.lineSequence().forEach { response += it }
                    br.close()

                    Log.d("request", "response from async task: $response")

                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        MainScope().launch { Toast.makeText(this@MainActivity, "Logined", Toast.LENGTH_LONG).show() }
                        return@launch
                    }
                    throw RuntimeException(response)
                } catch (e: Exception) {
                    val error = e.message!!
                    MainScope().launch { Toast.makeText(this@MainActivity, "Crashed with: $error", Toast.LENGTH_LONG).show() }
                    if (error.contains("\"action\":\"captcha\",")) {
                        val isHandledByPerimeterX = perimeterX.managePerimeterLoginError(error)
                        if (!isHandledByPerimeterX) {
                            Log.d("request", "Was NOT handled by perimeterX")
                        } else {
                            Log.d("request", "Was handled by perimeterX")
                        }
                    }
                    Log.d("request", "catch done")
                }

            }
        }
    }
}