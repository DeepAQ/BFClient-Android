package cn.imaq.brainfuck

import android.app.ProgressDialog
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editHost.setText(DataMgr.host)
        editUsername.setText(DataMgr.username)
        editPassword.setText(DataMgr.password)
        if (DataMgr.host.isNotBlank() && DataMgr.username.isNotBlank() && DataMgr.password.isNotEmpty())
            buttonLogin.callOnClick()
    }

    override fun onDestroy() {
        if (!SessionMgr.isLoggedin()) System.exit(0)
        super.onDestroy()
    }

    fun onLoginAction(v: View) {
        val host = editHost.text.toString()
        val username = editUsername.text.toString()
        val password = editPassword.text.toString()
        if (host.isBlank() || username.isBlank() || password.isEmpty()) {
            Snackbar.make(v, "Please fill in all blanks!", Snackbar.LENGTH_SHORT).show()
            return
        }
        val pDialog = ProgressDialog(this)
        pDialog.setTitle("Please wait")
        pDialog.setMessage("Logging in …")
        pDialog.show()
        object: AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                try {
                    SessionMgr.host = host
                    SessionMgr.login(username, password)
                    DataMgr.host = host
                    DataMgr.username = username
                    DataMgr.password = password
                    DataMgr.save(applicationContext)
                } catch (e: Exception) {
                    Snackbar.make(v, e.toString(), Snackbar.LENGTH_LONG).show()
                }
                return null
            }
            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)
                pDialog.dismiss()
                if (SessionMgr.isLoggedin())
                    finish()
            }
        }.execute()
    }

}
