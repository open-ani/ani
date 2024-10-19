# Keep `Companion` object fields of serializable classes.
# This avoids serializer lookup through `getDeclaredClasses` as done for named companion objects.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects (both default and named) of serializable classes.
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `INSTANCE.serializer()` of serializable objects.
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# @Serializable and @Polymorphic are used at runtime for polymorphic serialization.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Don't print notes about potential mistakes or omissions in the configuration for kotlinx-serialization classes
# See also https://github.com/Kotlin/kotlinx.serialization/issues/1900
-dontnote kotlinx.serialization.**

# Serialization core uses `java.lang.ClassValue` for caching inside these specified classes.
# If there is no `java.lang.ClassValue` (for example, in Android), then R8/ProGuard will print a warning.
# However, since in this case they will not be used, we can disable these warnings
-dontwarn kotlinx.serialization.internal.ClassValueReferences


-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault


# Keep API interfaces
-keep class org.openapitools.** {
	*;
}

-keep class ** extends me.him188.ani.datasources.api.subject.SubjectProvider {}
-keep class ** extends me.him188.ani.datasources.api.source.MediaSource {}
-keep class ** extends me.him188.ani.datasources.api.source.MediaSourceFactory {}

# Torrent4j
-keep class org.libtorrent4j.swig.libtorrent_jni {*;}
-keep class me.him188.ani.app.ui.settings.tabs.** {*;} # 否则设置页切换 tab 会 crash, #367
-keep class me.him188.ani.app.navigation.** {*;} # 否则启动 APP 时会 crash
-keep class me.him188.ani.app.ui.subject.cache.** {*;} # 否则点击缓存管理会 crash


# logback-android
-keepclassmembers class ch.qos.logback.classic.pattern.* { <init>(); }
# The following rules should only be used if you plan to keep
# the logging calls in your released app.
-keepclassmembers class ch.qos.logback.** { *; } #java.io.IOException: Failed to load asset path /data/app/~~2FXqiqIwzpvJbysP7TCLHQ==/me.him188.ani-fqpPfM4QmpABXA7iaUY_Cw==/base.apk
-keepclassmembers class org.slf4j.impl.** { *; }
# TODO 上面两条看起会少 optimize 非常多东西, 可以考虑优化下
-keep class ch.qos.logback.classic.android.LogcatAppender
-keep class ch.qos.logback.core.rolling.RollingFileAppender
-keep class ch.qos.logback.core.rolling.TimeBasedRollingPolicy
#-keepattributes *Annotation* # logback-android 推荐添加, 但测试可以不用添加这个
-dontwarn javax.mail.**


# anitorrent
-keep class me.him188.ani.app.torrent.anitorrent.binding.** { *; }
