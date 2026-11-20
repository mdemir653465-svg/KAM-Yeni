
package com.example.sampleapp

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        val durum = TextView(this)
        durum.text = "Merhaba! Ben KAM."
        durum.textSize = 20f
        durum.setTextColor(Color.WHITE)
        durum.gravity = Gravity.CENTER
        durum.setPadding(0, 20, 0, 50)

        val mesaj = EditText(this)
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
                durum.text = "Mesajını aldım: $yazi"
                mesaj.text.clear()
            }
        }

        ekran.addView(baslik)
        ekran.addView(yuz)
        ekran.addView(durum)
        ekran.addView(mesaj,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))
        ekran.addView(gonder)

        setContentView(ekran)
    }
}
