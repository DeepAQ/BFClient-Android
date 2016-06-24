package cn.imaq.brainfuck

import org.json.JSONObject
import org.json.JSONTokener
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection

/**
 * Created by adn55 on 16/6/24.
 */
object SessionMgr {
    var host = "http://localhost:8081"
    var username = ""
    private var sessionId = ""

    // User login & logout
    fun login(username: String, password: String) {
        loginWithPwdhash(username, hash(password))
    }

    fun loginWithPwdhash(username: String, pwdhash: String) {
        val serverResp = getURL("$host/user/login?username=$username&pwdhash=$pwdhash")
        val jsonObj = JSONTokener(serverResp).nextValue() as JSONObject
        if (jsonObj.getInt("result") < 0)
            throw Exception(jsonObj.getString("errmsg"))
        else {
            sessionId = jsonObj.getString("sessid")
            SessionMgr.username = username
        }
    }

    fun logout() {
        username = ""
        sessionId = ""
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        val serverResp = getURL("$host/user/changepassword?sessid=$sessionId&oldpwdhash=${hash(oldPassword)}&newpwdhash=${hash(newPassword)}")
        val jsonObj = JSONTokener(serverResp).nextValue() as JSONObject
        if (jsonObj.getInt("result") < 0)
            throw Exception(jsonObj.getString("errmsg"))
    }

    // File I/O
    fun getFileList(): String {
        return getURL("$host/io/list?sessid=$sessionId")
    }

    fun getFileContent(filename: String, version: String): String {
        val serverResp = getURL("$host/io/open?sessid=$sessionId&filename=$filename&version=$version")
        val jsonObj = JSONTokener(serverResp).nextValue() as JSONObject
        if (jsonObj.getInt("result") < 0)
            throw Exception(jsonObj.getString("errmsg"))
        else
            return jsonObj.getString("code")
    }

    fun saveFile(code: String, filename: String): String {
        val encCode = URLEncoder.encode(URLEncoder.encode(code, "utf-8"), "utf-8")
        val serverResp = getURL("$host/io/save?sessid=$sessionId&filename=$filename&code=$encCode")
        val jsonObj = JSONTokener(serverResp).nextValue() as JSONObject
        if (jsonObj.getInt("result") < 0)
            throw Exception(jsonObj.getString("errmsg"))
        else
            return jsonObj.getString("version")
    }

    // Code execute
    fun execute(code: String, input: String): String {
        val encCode = URLEncoder.encode(URLEncoder.encode(code, "utf-8"), "utf-8")
        val encInput = URLEncoder.encode(URLEncoder.encode(input, "utf-8"), "utf-8")
        val serverResp = getURL("$host/io/execute?sessid=$sessionId&code=$encCode&input=$encInput")
        val jsonObj = JSONTokener(serverResp).nextValue() as JSONObject
        if (jsonObj.getInt("result") < 0)
            throw Exception(jsonObj.getString("errmsg"))
        else
            return jsonObj.getString("output")
    }

    // Network functions
    fun getURL(url: String): String {
        val conn = URL(url).openConnection()
        with (conn) {
            connectTimeout = 5000
            readTimeout = 5000
        }
        if (conn !is HttpsURLConnection && conn !is HttpURLConnection) {
            return ""
        }
        return stringFromInputStream(conn.inputStream);
    }

    fun stringFromInputStream(input: InputStream): String {
        val os = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var len: Int
        len = input.read(buffer)
        while (len != -1) {
            os.write(buffer)
            len = input.read(buffer)
        }
        val result = os.toString()
        os.close()
        input.close()
        return result
    }

    fun hash(str: String): String {
        val digest = MessageDigest.getInstance("SHA1")
        val bytes = digest.digest(str.toByteArray())
        var result = ""
        for (b in bytes) {
            result += Integer.toString((b.toInt() and 0xff) + 0x100, 16).substring(1)
        }
        return result
    }
}