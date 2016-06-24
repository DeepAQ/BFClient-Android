package cn.imaq.brainfuck

import android.content.Context

/**
 * Created by adn55 on 16/6/24.
 */
object DataMgr {
    var username = ""
    var password = ""
    var host = ""

    fun load(context: Context) {
        val pref = context.getSharedPreferences("BFData", Context.MODE_PRIVATE)
        username = pref.getString("username", "")
        password = pref.getString("pwdhash", "")
        host = pref.getString("host", "http://localhost:8081")
    }

    fun save(context: Context) {
        with (context.getSharedPreferences("BFData", Context.MODE_PRIVATE).edit()) {
            putString("username", username)
            putString("pwdhash", password)
            putString("host", host)
            apply()
        }
    }
}