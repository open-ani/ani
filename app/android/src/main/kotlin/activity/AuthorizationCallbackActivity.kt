package me.him188.ani.android.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.him188.ani.app.ui.foundation.AniApp
import me.him188.ani.app.ui.foundation.AniTopAppBar
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.home.currentAuthViewModel


class AuthorizationCallbackActivity : AniComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Get the intent that started this activity
        val intent = intent
        val data = intent.data

        if (data == null) {
            finish()
            return
        }

        if (!data.queryParameterNames.contains("code")) {
            finish()
            return
        }

        enableDrawingToSystemBars()

        val vm = currentAuthViewModel ?: return
        vm.launchInBackground {
            val code = data.getQueryParameter("code")!!
            setCode(code)
            finish()
        }

        setContent {
            AniApp(currentColorScheme) {
                Scaffold(
                    Modifier
                        .systemBarsPadding()
                        .fillMaxSize(),
                    topBar = {
                        AniTopAppBar(
                            title = { Text(text = "登录 Bangumi") }
                        )
                    }
                ) { contentPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "正在处理...")
                    }
                }
            }
        }
    }
}