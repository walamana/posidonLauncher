/*
 * Copyright (c) 2019 Leo Shneyderis
 * All rights reserved
 */

package posidon.launcher.customizations

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.tools.Settings
import posidon.launcher.tools.Tools
import java.io.FileNotFoundException
import kotlin.system.exitProcess


class CustomOther : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Tools.applyFontSetting(this)
        setContentView(R.layout.custom_other)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)
        (findViewById<View>(R.id.hidestatus) as Switch).isChecked = Settings.getBool("hidestatus", false)
        (findViewById<View>(R.id.mnmlstatus) as Switch).isChecked = Settings.getBool("mnmlstatus", false)
        val hapticbar = findViewById<SeekBar>(R.id.hapticbar)
        hapticbar.progress = Settings.getInt("hapticfeedback", 14)
        hapticbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Settings.put("hapticfeedback", seekBar.progress)
                Tools.vibrate(this@CustomOther)
            }
        })
        findViewById<Spinner>(R.id.animationOptions).setSelection(when(Settings.getString("anim:app_open", "posidon")) {
            "scale_up" -> 2
            "clip_reveal" -> 1
            else -> 0
        })
        Main.customized = true
    }

    override fun onPause() {
        Settings.apply {
            putNotSave("hidestatus", (findViewById<View>(R.id.hidestatus) as Switch).isChecked)
            putNotSave("mnmlstatus", (findViewById<View>(R.id.mnmlstatus) as Switch).isChecked)
            putNotSave("anim:app_open", when(findViewById<Spinner>(R.id.animationOptions).selectedItemPosition) {
                2 -> "scale_up"
                1 -> "clip_reveal"
                else -> "posidon"
            })
            apply()
        }
        super.onPause()
    }

    fun openHideApps(v: View) = startActivity(Intent(this, CustomHiddenApps::class.java))
    fun stop(v: View): Unit = exitProcess(0)
    fun mkBackup(v: View) = Settings.saveBackup()
    fun useBackup(v: View) {
        val intent = Intent()
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/*"
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                try { data?.data?.let {
                    Settings.restoreFromBackup(it)
                    Toast.makeText(this, "Backup restored!", Toast.LENGTH_LONG).show()
                }}
                catch (e: FileNotFoundException) { e.printStackTrace() }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun chooseLauncher(v: View) {
        val packageManager: PackageManager = packageManager
        val componentName = ComponentName(this, FakeLauncherActivity::class.java)
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        val selector = Intent(Intent.ACTION_MAIN)
        selector.addCategory(Intent.CATEGORY_HOME)
        selector.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(selector)
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP)
    }
}