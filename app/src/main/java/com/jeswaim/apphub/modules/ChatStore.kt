package com.jeswaim.apphub.modules

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ChatStore {
    private const val PREF = "chat_history"

    fun add(ctx: Context, persona: String, user: String, friend: String) {
        val prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val key = "friend_" + persona.lowercase()
        val cur = prefs.getString(key, "[]") ?: "[]"
        val arr = JSONArray(cur)
        val obj = JSONObject().put("t", System.currentTimeMillis()).put("u", user).put("f", friend)
        arr.put(obj)
        // Keep last 200
        val trimmed = JSONArray()
        val start = if (arr.length() > 200) arr.length() - 200 else 0
        for (i in start until arr.length()) trimmed.put(arr.getJSONObject(i))
        prefs.edit().putString(key, trimmed.toString()).apply()
    }

    fun get(ctx: Context, persona: String): JSONArray {
        val prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val key = "friend_" + persona.lowercase()
        val cur = prefs.getString(key, "[]") ?: "[]"
        return JSONArray(cur)
    }

    fun clear(ctx: Context, persona: String) {
        val prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val key = "friend_" + persona.lowercase()
        prefs.edit().putString(key, "[]").apply()
    }
}
