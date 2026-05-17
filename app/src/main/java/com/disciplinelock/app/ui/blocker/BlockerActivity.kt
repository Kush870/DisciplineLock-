package com.disciplinelock.app.ui.blocker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.disciplinelock.app.ui.theme.DisciplineLockTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlockerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DisciplineLockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BlockerScreen(
                        onEmergencyUnlock = {
                            // Redirect to home screen after unlock
                            finish()
                        },
                        onGoBack = {
                            // Redirect to android home screen
                            val startMain = android.content.Intent(android.content.Intent.ACTION_MAIN)
                            startMain.addCategory(android.content.Intent.CATEGORY_HOME)
                            startMain.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(startMain)
                            finish()
                        }
                    )
                }
            }
        }
    }
    
    // Prevent back button from escaping the blocker easily
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing, force user to use the buttons
    }
}
