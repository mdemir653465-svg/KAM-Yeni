
package com.bedrettin.kam

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.tts.TextToSpeech
import android.Manifest
import java.util.Locale
import java.net.URL
import java.net.HttpURLConnection
import java.util.concurrent.Executors
import org.json.JSONObject

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var durum: TextView
    private lateinit var mesaj: EditText
    private lateinit var tts: TextToSpeech
    private var recognizer: SpeechRecognizer? = null
    private val izinKodu = 100

    private val sunucuAdresi =
        "https://soft-unit-304b.mdemir653465.workers.dev"

    private val ag = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        val ekran = LinearLayout(this)
        ekran.orientation = LinearLayout.VERTICAL
        ekran.gravity = Gravity.CENTER
        ekran.setPadding(24, 30, 24, 24)
        ekran.setBackgroundColor(Color.rgb(5, 8, 18))

        val baslik = TextView(this)
        baslik.text = "KAM"
        baslik.textSize = 32f
        baslik.setTextColor(Color.WHITE)
        baslik.gravity = Gravity.CENTER

        val yuz = TextView(this)
        yuz.text = "◉       ◉"
        yuz.textSize = 45f
        yuz.setTextColor(Color.rgb(40, 150, 255))
        yuz.gravity = Gravity.CENTER
        yuz.setPadding(0, 50, 0, 20)

        durum = TextView(this)
        durum.text = "Merhaba! Ben KAM."
        durum.textSize = 20f
        durum.setTextColor(Color.WHITE)
        durum.gravity = Gravity.CENTER
        durum.setPadding(0, 20, 0, 40)

        mesaj = EditText(this)
        mesaj.hint = "KAM'a bir şey yaz..."
        mesaj.setHintTextColor(Color.GRAY)
        mesaj.setTextColor(Color.WHITE)
        mesaj.textSize = 16f
        mesaj.setSingleLine(true)

        val gonder = Button(this)
        gonder.text = "Gönder"
        gonder.setOnClickListener {
            val yazi = mesaj.text.toString().trim()
            if (yazi.isNotEmpty()) {
                mesaj.text.clear()
                kam'aSor(yazi)
            }
        }

        val mikrofon = Button(this)
        mikrofon.text = "🎤 Konuş"
        mikrofon.setOnClickListener {
            mikrofonuBaslat()
        }

        ekran.addView(baslik)
        ekran.addView(yuz)
        ekran.addView(durum)
        ekran.addView(mesaj)
        ekran.addView(gonder)
        ekran.addView(mikrofon)

        setContentView(ekran)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("tr", "TR")
        }
    }

    private fun kam'aSor(yazi: String) {
        durum.text = "KAM düşünüyor..."

        ag.execute {
            var baglanti: HttpURLConnection? = null

            try {
                baglanti = (URL(sunucuAdresi).openConnection()
                        as HttpURLConnection)

                baglanti.requestMethod = "POST"
                baglanti.connectTimeout = 20000
                baglanti.readTimeout = 60000
                baglanti.doOutput = true
                baglanti.setRequestProperty(
                    "Content-Type", "application/json"
                )

                val veri = JSONObject()
                veri.put("message", yazi)

                baglanti.outputStream.use {
                    it.write(veri.toString().toByteArray(Charsets.UTF_8))
                }

                val kod = baglanti.responseCode
                val akis = if (kod in 200..299) {
                    baglanti.inputStream
                } else {
                    baglanti.errorStream
                }

                val sonuc = akis.bufferedReader().use {
                    it.readText()
                }

                val cevap = JSONObject(sonuc)

                if (kod in 200..299) {
                    val yanit = cevap.optString("reply")

                    runOnUiThread {
                        if (yanit.isNotBlank()) {
                            cevapVer(yanit)
                        } else {
                            cevapVer("Bir cevap alamadım.")
                        }
                    }
                } else {
                    runOnUiThread {
                        cevapVer(
                            "Sunucu hatası: " +
                            cevap.optString("error", "Bağlantı başarısız.")
                        )
                    }
                }

            } catch (e: Exception) {
                runOnUiThread {
                    cevapVer("KAM sunucuya bağlanamadı. İnternet bağlantını kontrol et.")
                }
            } finally {
                baglanti?.disconnect()
            }
        }
    }

    private fun cevapVer(yazi: String) {
        durum.text = yazi
        tts.speak(
            yazi,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "KAM_CEVAP"
        )
    }

    private fun mikrofonuBaslat() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                izinKodu
            )
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            durum.text = "Telefonda ses tanıma hizmeti bulunamadı."
            return
        }

        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)

        recognizer?.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) {
                durum.text = "Seni dinliyorum..."
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                durum.text = "Söylediklerini anlıyorum..."
            }

            override fun onError(error: Int) {
                durum.text = "Seni duyamadım. Tekrar deneyebilirsin."
            }

            override fun onResults(results: Bundle?) {
                val liste = results?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )

                val sozcuk = liste?.firstOrNull()

                if (!sozcuk.isNullOrBlank()) {
                    mesaj.setText(sozcuk)
                    kamSor(sozcuk)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE, "tr-TR"
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "tr-TR"
        )

        recognizer?.startListening(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )

        if (requestCode == izinKodu &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mikrofonuBaslat()
        } else {
            durum.text = "Mikrofon izni gerekli."
        }
    }

    override fun onDestroy() {
        recognizer?.destroy()
        tts.stop()
        tts.shutdown()
        ag.shutdownNow()
        super.onDestroy()
    }
}
