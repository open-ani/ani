package me.him188.ani.android.activity


//class AuthorizationActivity : AniComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val allowBack = intent.getBooleanExtra("allowBack", false)
//
//        enableDrawingToSystemBars()
//
//        setContent {
////            val vm = remember { AuthViewModel().also { currentAuthViewModel = it } }
//
//            AniApp(currentColorScheme) {
//                AuthRequestScreen(vm, allowBack)
//            }
//        }
//    }
//
//
//    override fun onDestroy() {
//        super.onDestroy()
//        AndroidAuthorizationNavigator.currentResult?.let {
//            if (it.isActive) {
//                it.complete(AuthorizationResult.CANCELLED)
//            }
//        }
//    }
//
//    companion object {
//        var currentAuthViewModel: AuthViewModel? = AuthViewModel()
//
//        fun getIntent(context: android.content.Context, allowBack: Boolean): android.content.Intent {
//            return android.content.Intent(context, AuthorizationActivity::class.java).apply {
//                putExtra("allowBack", allowBack)
//            }
//        }
//    }
//}