package com.jeswaim.apphub

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class ChatHistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val persona = intent.getStringExtra("persona") ?: "Mary"
        val txt = findViewById<TextView>(R.id.txtHistory)
        val btnClear = findViewById<Button>(R.id.btnClearHistory)
        val btnExport = findViewById<Button>(R.id.btnExportHistory)

        title = "AI Friend History — $persona"
        render(persona, txt)

        btnClear.setOnClickListener {
            com.jeswaim.apphub.modules.ChatStore.clear(this, persona)
            render(persona, txt)
            Toast.makeText(this, "Cleared", Toast.LENGTH_SHORT).show()
        }

        btnExport.setOnClickListener {
            val clip = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val content = txt.text.toString()
            val item = android.content.ClipData.newPlainText("history", content)
            clip.setPrimaryClip(item)
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun render(persona: String, view: TextView) {
        val arr = com.jeswaim.apphub.modules.ChatStore.get(this, persona)
        val sb = StringBuilder()
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val t = fmt.format(Date(o.getLong("t")))
            sb.append(t).append("\nYou: ").append(o.getString("u")).append("\nFriend: ").append(o.getString("f")).append("\n\n")
        }
        view.text = sb.toString()
    }
}
