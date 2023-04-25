package com.xayah.libpickyou.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.xayah.libpickyou.ui.theme.LibPickYouTheme

internal class LibPickYouActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LibPickYouTheme {
            }
        }
    }
}
