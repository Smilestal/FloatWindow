package com.crystal.floatwindow

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun click(v: View) {
        when (v?.id) {
            R.id.btn_show -> {
                if (FloatWindowUtils.isShown) {
                    FloatWindowUtils.visible()
                } else {
                    if (window.decorView.windowToken != null) {
                        FloatWindowUtils.showWindow(this) {
                            startActivity(Intent(this, FloatActivity::class.java))
                        }
                    }
                }
            }
            R.id.btn_hide -> {
                if (FloatWindowUtils.isShown) FloatWindowUtils.invisible()
            }
        }
    }
}
