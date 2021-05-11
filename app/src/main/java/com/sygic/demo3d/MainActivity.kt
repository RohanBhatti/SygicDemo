package com.sygic.demo3d

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sygic.aura.ResourceManager
import com.sygic.sdk.api.ApiNavigation
import com.sygic.sdk.api.exception.GeneralException


class MainActivity : AppCompatActivity() {

    private var fgm: SygicNaviFragment? = null

    var neverAskMeAgain = false

    // for Runtime Permissions
    private val STORAGE_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermissions()
    }

    private fun checkPermissions() {
        // Check if the Storage permission has been granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            checkSygicResources()
        } else {
            requestPermissions()
        }
    }


    private fun requestPermissions() {
        if (neverAskMeAgain) {
            AlertDialog.Builder(this)
                .setTitle(resources.getString(R.string.premission_title))
                .setMessage(resources.getString(R.string.permission_manual))
                .setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss() }
                .setPositiveButton("Agree") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    openPermissionSettings(this)
                }
                .create().show()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) ||
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_PHONE_STATE
                ) ||
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle(resources.getString(R.string.premission_title))
                    .setMessage(resources.getString(R.string.permission_desc))
                    .setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss() }
                    .setPositiveButton("Agree") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ), STORAGE_PERMISSION_REQUEST_CODE
                        )
                    }
                    .create().show()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), STORAGE_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    fun openPermissionSettings(activity: Activity) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + activity.packageName)
        )
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(intent)
    }

    private fun checkSygicResources() {
        val resourceManager = ResourceManager(this, null)
        if (resourceManager.shouldUpdateResources()) {
            Toast.makeText(
                this,
                "Please wait while Sygic resources are being updated",
                Toast.LENGTH_LONG
            ).show()
            resourceManager.updateResources(object : ResourceManager.OnResultListener {
                override fun onError(code: Int, message: String) {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to update resources: $message",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }

                override fun onSuccess() {
                    initUI()
                }
            })
        } else {
            initUI()
        }
    }

    private fun initUI() {
        setContentView(R.layout.activity_main)
        fgm = SygicNaviFragment()
        supportFragmentManager.beginTransaction().replace(R.id.sygicmap, fgm!!)
            .commitAllowingStateLoss()

        val address = findViewById<View>(R.id.edit1) as EditText

        val btn: Button = findViewById<View>(R.id.button1) as Button

        btn.setOnClickListener {
            object : Thread() {
                override fun run() {
                    try {
                        ApiNavigation.navigateToAddress(address.text.toString(), false, 0, 0)
                    } catch (e: GeneralException) {
                        e.printStackTrace()
                    }
                }
            }.start()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {

        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            // Request for camera permission.
            if (grantResults.size > 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                grantResults[2] == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission has been granted. Start camera preview Activity.
                checkSygicResources()
            } else {
                if (!(ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ))
                ) {
                    neverAskMeAgain = true
                }
                requestPermissions()
            }
        }
        /*for (res in grantResults) {
            if (res != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "You have to allow all permissions", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        }

        // all permissions are granted
        checkSygicResources()*/
    }

    override fun onCreateDialog(id: Int): Dialog? {
        return fgm!!.onCreateDialog(id) ?: return super.onCreateDialog(id)
    }

    override fun onPrepareDialog(id: Int, dialog: Dialog?) {
        super.onPrepareDialog(id, dialog)
        fgm!!.onPrepareDialog(id, dialog)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        fgm!!.onNewIntent(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fgm!!.onActivityResult(requestCode, resultCode, data)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return fgm!!.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return fgm!!.onKeyUp(keyCode, event)
    }
}