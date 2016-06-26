package cn.imaq.brainfuck

import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.json.JSONArray
import org.json.JSONTokener

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fabRun.setOnClickListener { v ->
            executeAction()
        }
        editCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                modified = !editCode.text.toString().equals(originalCode)
                updateTitle()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        DataMgr.load(applicationContext)
        if (!SessionMgr.isLoggedin())
            startActivity(Intent().setClass(this, LoginActivity::class.java))
        updateTitle()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(toolbar.windowToken, 0)
        when (item?.itemId) {
            R.id.action_new -> newAction()
            R.id.action_open -> openAction()
            R.id.action_save -> saveAction()
            R.id.action_saveas -> saveAsAction()
            R.id.action_execute -> executeAction()
            R.id.action_logout -> logoutAction()
            R.id.action_exit -> exitAction()
        }
        return super.onOptionsItemSelected(item)
    }

    private var fileName = ""
    private var fileVersion = ""
    private var originalCode = ""
    private var savedInput = ""
    private var modified = false

    private fun updateTitle() {
        val modFlag = if (modified) "* " else ""
        if (fileName.isBlank())
            toolbar.subtitle = "${modFlag}Untitled.bf"
        else
            toolbar.subtitle = "$modFlag$fileName.bf ($fileVersion)"
    }

    fun newAction() {
        checkSaved(View.OnClickListener { v ->
            editCode.text.clear()
            fileName = ""
            fileVersion = ""
            modified = false
            updateTitle()
        })
    }

    fun openAction() {
        checkSaved(View.OnClickListener { v ->
            val pDialog = ProgressDialog(this)
            pDialog.setTitle("Please wait")
            pDialog.setMessage("Fetching file list …")
            pDialog.show()
            object : AsyncTask<Void, Void, Void>() {
                var jsonList: JSONArray? = null;
                override fun doInBackground(vararg params: Void?): Void? {
                    try {
                        jsonList = JSONTokener(SessionMgr.getFileList()).nextValue() as JSONArray
                    } catch (e: Exception) {
                        Snackbar.make(toolbar, e.toString(), Snackbar.LENGTH_LONG).show()
                    }
                    return null
                }

                override fun onPostExecute(result: Void?) {
                    super.onPostExecute(result)
                    pDialog.dismiss()
                    if (jsonList is JSONArray && jsonList!!.length() > 0) {
                        val arrayFiles = arrayOfNulls<String>(jsonList!!.length())
                        for (i in 0 until jsonList!!.length()) {
                            val jsonFile = jsonList?.getJSONObject(i)
                            arrayFiles[i] = jsonFile!!.getString("filename")
                        }
                        AlertDialog.Builder(this@MainActivity)
                                .setTitle("Select a file :")
                                .setItems(arrayFiles, { dialog, i ->
                                    val selectedFile = arrayFiles[i]
                                    var arrayVersions = arrayOfNulls<String>(0)
                                    for (j in 0 until arrayFiles.size)
                                        if (arrayFiles[j].equals(selectedFile)) {
                                            val versions = jsonList!!.getJSONObject(j).getJSONArray("versions")
                                            arrayVersions = arrayOfNulls<String>(versions.length())
                                            for (k in 0 until versions.length())
                                                arrayVersions[k] = versions.getString(versions.length() - 1 - k)
                                            break
                                        }
                                    AlertDialog.Builder(this@MainActivity)
                                            .setTitle("Select a version :")
                                            .setItems(arrayVersions, { dialog2, i ->
                                                val selectedVersion = arrayVersions[i]
                                                val pDialog2 = ProgressDialog(this@MainActivity)
                                                pDialog2.setTitle("Please wait")
                                                pDialog2.setMessage("Opening file …")
                                                pDialog2.show()
                                                object : AsyncTask<Void, Void, Void>() {
                                                    override fun doInBackground(vararg params: Void?): Void? {
                                                        try {
                                                            fileName = selectedFile!!
                                                            fileVersion = selectedVersion!!
                                                            originalCode = SessionMgr.getFileContent(fileName, fileVersion)
                                                            modified = false
                                                        } catch (e: Exception) {
                                                            Snackbar.make(toolbar, e.toString(), Snackbar.LENGTH_LONG).show()
                                                        }
                                                        return null
                                                    }

                                                    override fun onPostExecute(result: Void?) {
                                                        super.onPostExecute(result)
                                                        editCode.setText(originalCode)
                                                        updateTitle()
                                                        pDialog2.dismiss()
                                                    }
                                                }.execute()
                                            })
                                            .show()
                                })
                                .show()
                    } else
                        Snackbar.make(toolbar, "No files saved, try to create a new one~", Snackbar.LENGTH_LONG).show()
                }
            }.execute()
        })
    }

    fun saveAction() {
        if (!modified) return
        if (fileName.isEmpty())
            saveAsAction()
        else {
            val pDialog = ProgressDialog(this)
            pDialog.setTitle("Please wait")
            pDialog.setMessage("Saving file …")
            pDialog.show()
            object : AsyncTask<Void, Void, Void>() {
                override fun doInBackground(vararg params: Void?): Void? {
                    try {
                        val code = editCode.text.toString()
                        val newVersion = SessionMgr.saveFile(code, fileName)
                        fileVersion = newVersion
                        originalCode = code
                        modified = false
                    } catch (e: Exception) {
                        Snackbar.make(toolbar, e.toString(), Snackbar.LENGTH_LONG).show()
                    }
                    return null
                }

                override fun onPostExecute(result: Void?) {
                    super.onPostExecute(result)
                    updateTitle()
                    pDialog.dismiss()
                    if (!modified)
                        Snackbar.make(toolbar, "Saved", Snackbar.LENGTH_LONG).show()
                }
            }.execute()
        }
    }

    fun saveAsAction() {
        val inputName = EditText(this)
        inputName.hint = "Filename"
        AlertDialog.Builder(this)
                .setTitle("Filename :")
                .setView(inputName)
                .setPositiveButton("Save", { dialog, i ->
                    fileName = inputName.text.toString()
                    modified = true
                    dialog.dismiss()
                    saveAction()
                })
                .setNegativeButton("Cancel", { dialog, i ->
                    dialog.dismiss()
                })
                .show()
    }

    fun executeAction() {
        if (!editCode.text.toString().contains(","))
            executeWithInput("")
        else {
            val editInput = EditText(this)
            editInput.setText(savedInput)
            AlertDialog.Builder(this)
                    .setTitle("Input :")
                    .setView(editInput)
                    .setPositiveButton("Execute", { dialog, i ->
                        dialog.dismiss()
                        savedInput = editInput.text.toString()
                        executeWithInput(editInput.text.toString())
                    })
                    .setNegativeButton("Cancel", { dialog, i ->
                        dialog.dismiss()
                    })
                    .show()
        }
    }

    fun executeWithInput(input: String) {
        val pDialog = ProgressDialog(this)
        pDialog.setTitle("Please wait")
        pDialog.setMessage("Executing …")
        pDialog.show()
        object : AsyncTask<Void, Void, Void>() {
            var executeResult = ""
            override fun doInBackground(vararg params: Void?): Void? {
                try {
                    val code = editCode.text.toString()
                    executeResult = SessionMgr.execute(code, input)
                    if (executeResult.isBlank())
                        executeResult = "[Output is empty]"
                } catch (e: Exception) {
                    executeResult = "Execution error:\n${e.toString()}"
                }
                return null
            }

            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)
                pDialog.dismiss()
                AlertDialog.Builder(this@MainActivity)
                        .setTitle("Result :")
                        .setMessage(executeResult)
                        .setPositiveButton("OK", { dialog, i ->
                            dialog.dismiss()
                        })
                        .show()
            }
        }.execute()
    }

    fun logoutAction() {
        checkSaved(View.OnClickListener { v ->
            SessionMgr.logout()
            DataMgr.password = ""
            DataMgr.save(applicationContext)
            startActivity(Intent().setClass(this, LoginActivity::class.java))
        })
    }

    fun exitAction() {
        checkSaved(View.OnClickListener { v ->
            System.exit(0)
        })
    }

    fun checkSaved(listener: View.OnClickListener) {
        if (!modified)
            listener.onClick(null)
        else
            AlertDialog.Builder(this)
                    .setTitle("Confirmation")
                    .setMessage("File not saved, save it?")
                    .setPositiveButton("Save", { dialog, i ->
                        saveAction()
                        dialog.dismiss()
                    })
                    .setNegativeButton("Discard", { dialog, i ->
                        editCode.text.clear()
                        fileName = ""
                        fileVersion = ""
                        modified = false
                        updateTitle()
                        dialog.dismiss()
                        listener.onClick(null)
                    })
                    .setNeutralButton("Cancel", { dialog, i ->
                        dialog.dismiss()
                    })
                    .show()
    }

}
