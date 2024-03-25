package me.him188.ani.android.activity

import android.os.Bundle


class AuthorizationCallbackActivity : AniComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Get the intent that started this activity
//        val intent = intent
//        val data = intent.data
//
//        if (data == null) {
//            finish()
//            return
//        }
//
//        if (!data.queryParameterNames.contains("code")) {
//            finish()
//            return
//        }
//
//        enableDrawingToSystemBars()
//
//        val vm = currentAuthViewModel ?: return
//        vm.launchInBackground {
//            val code = data.getQueryParameter("code")!!
//            setCode(code, BangumiAuthorizationConstants.CALLBACK_URL)
//            finish()
//        }
//
//        setContent {
//            AniApp(currentColorScheme) {
//                Scaffold(
//                    Modifier
//                        .systemBarsPadding()
//                        .fillMaxSize(),
//                    topBar = {
//                        AniTopAppBar(
//                            title = { Text(text = "登录 Bangumi") }
//                        )
//                    }
//                ) { contentPadding ->
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(contentPadding),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(text = "正在处理...")
//                    }
//                }
//            }
//        }
    }
}