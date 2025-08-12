package com.jeswaim.apphub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jeswaim.apphub.llm.LlmBridge
import com.jeswaim.apphub.modules.ChatStore
import org.json.JSONObject

data class Message(val text: String, val isUser: Boolean)

class ChatHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerChat: RecyclerView
    private lateinit var editChatMessage: EditText
    private lateinit var btnSendMessage: Button
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<Message>()
    private lateinit var persona: String
    private lateinit var llmBridge: LlmBridge

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        persona = intent.getStringExtra("persona") ?: "Mary"
        title = "AI Friend - $persona"

        llmBridge = LlmBridge(this)

        recyclerChat = findViewById(R.id.recyclerChat)
        editChatMessage = findViewById(R.id.editChatMessage)
        btnSendMessage = findViewById(R.id.btnSendMessage)

        chatAdapter = ChatAdapter(messages)
        recyclerChat.adapter = chatAdapter
        recyclerChat.layoutManager = LinearLayoutManager(this)

        loadChatHistory()

        btnSendMessage.setOnClickListener {
            val messageText = editChatMessage.text.toString()
            if (messageText.isNotBlank()) {
                sendMessage(messageText)
                editChatMessage.text.clear()
            }
        }
    }

    private fun loadChatHistory() {
        val history = ChatStore.get(this, persona)
        for (i in 0 until history.length()) {
            val item = history.getJSONObject(i)
            messages.add(Message(item.getString("u"), true))
            messages.add(Message(item.getString("f"), false))
        }
        chatAdapter.notifyDataSetChanged()
        recyclerChat.scrollToPosition(messages.size - 1)
    }

    private fun sendMessage(messageText: String) {
        val userMessage = Message(messageText, true)
        messages.add(userMessage)
        chatAdapter.notifyItemInserted(messages.size - 1)
        recyclerChat.scrollToPosition(messages.size - 1)

        // Get AI response
        Thread {
            val response = llmBridge.generateCommentary("As $persona, respond to: $messageText")
            val aiMessage = Message(response, false)
            runOnUiThread {
                messages.add(aiMessage)
                chatAdapter.notifyItemInserted(messages.size - 1)
                recyclerChat.scrollToPosition(messages.size - 1)
                ChatStore.add(this, persona, messageText, response)
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        llmBridge.unload()
    }
}

class ChatAdapter(private val messages: List<Message>) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = if (viewType == 0) android.R.layout.simple_list_item_1 else android.R.layout.simple_list_item_2
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.text
        if (message.isUser) {
            holder.messageText.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
        } else {
            holder.messageText.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        }
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) 0 else 1
    }
}
