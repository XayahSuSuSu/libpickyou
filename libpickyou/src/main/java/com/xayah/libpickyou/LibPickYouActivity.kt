package com.xayah.libpickyou

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.xayah.libpickyou.ui.theme.LibPickYouTheme

class LibPickYouActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LibPickYouTheme {
            }
        }
    }
}
