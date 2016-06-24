package cn.imaq.brainfuck

import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fabRun.setOnClickListener { v ->
            Snackbar.make(v, "test", Snackbar.LENGTH_LONG).show()
        }
        editCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                modified = !editCode.text.toString().equals(originalCode)
                updateTitle()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        DataMgr.load(applicationContext)
        newAction()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private var fileName = ""
    private var fileVersion = ""
    private var originalCode = ""
    private var modified = false

    private fun updateTitle() {
        val modFlag = if (modified) "* " else ""
        if (fileName.isEmpty())
            toolbar.subtitle = "${modFlag}Untitled.bf"
        else
            toolbar.subtitle = "$modFlag$fileName.bf ($fileVersion)"
    }

    fun newAction() {
        editCode.text.clear()
        fileName = ""
        fileVersion = ""
        updateTitle()
    }

    fun saveAction() {
        if (!modified) return
        
    }

    fun saveAsAction() {
        val inputName = EditText(this)
        AlertDialog.Builder(this)
                .setTitle("Filename :")
                .setView(inputName)
                .setPositiveButton("Save", DialogInterface.OnClickListener { dialog, i ->
                    fileName = inputName.text.toString()
                    dialog.dismiss()
                    saveAction()
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, i ->
                    dialog.dismiss()
                })
                .show()
    }

}
