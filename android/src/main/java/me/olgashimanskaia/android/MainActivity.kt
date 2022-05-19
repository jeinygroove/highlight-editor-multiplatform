package me.olgashimanskaia.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import me.olgashimanskaia.common.App
import me.olgashimanskaia.common.GrazieTextAnalyzer
import me.olgashimanskaia.common.rememberApplicationState

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                    App(rememberApplicationState(GrazieTextAnalyzer()))
            }
        }
    }
}