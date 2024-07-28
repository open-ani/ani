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


# Keep the specified classes and all their members
-keep class io.ktor.client.network.sockets.TimeoutExceptionsCommonKt { *; }
-keep class io.ktor.client.plugins.HttpRedirect$Config { *; }
-keep class io.ktor.client.plugins.HttpRedirect$Plugin { *; }
-keep class io.ktor.client.plugins.HttpRedirect { *; }
-keep class io.ktor.client.plugins.HttpRequestRetry$Configuration { *; }
-keep class io.ktor.client.plugins.HttpRequestRetry$DelayContext { *; }
-keep class io.ktor.client.plugins.HttpRequestRetry$Plugin { *; }
-keep class io.ktor.client.plugins.HttpRequestRetry { *; }
-keep class io.ktor.client.plugins.HttpTimeout$HttpTimeoutCapabilityConfiguration { *; }
-keep class io.ktor.client.plugins.HttpTimeout$Plugin { *; }
-keep class io.ktor.client.plugins.HttpTimeout { *; }
-keep class io.ktor.client.plugins.UserAgent$Config { *; }
-keep class io.ktor.client.plugins.UserAgent$Plugin { *; }
-keep class io.ktor.client.plugins.UserAgent { *; }
-keep class io.ktor.util.InternalAPI { *; }
-keep class io.ktor.util.KtorDsl { *; }
-keep class io.ktor.utils.io.ByteReadChannelJVMKt { *; }
-keep class io.ktor.utils.io.CoroutinesKt { *; }
-keep class io.ktor.utils.io.ReadSessionKt { *; }
-keep class io.ktor.utils.io.core.Buffer$Companion { *; }
-keep class io.ktor.utils.io.core.Buffer { *; }
-keep class io.ktor.utils.io.core.ByteBuffersKt { *; }
-keep class io.ktor.utils.io.core.BytePacketBuilder { *; }
-keep class io.ktor.utils.io.core.ByteReadPacket$Companion { *; }
-keep class io.ktor.utils.io.core.ByteReadPacket { *; }
-keep class io.ktor.utils.io.core.CloseableJVMKt { *; }
-keep class io.ktor.utils.io.core.Input { *; }
-keep class io.ktor.utils.io.core.InputArraysKt { *; }
-keep class io.ktor.utils.io.core.InputPrimitivesKt { *; }
-keep class io.ktor.utils.io.core.Output { *; }
-keep class io.ktor.utils.io.core.OutputPrimitivesKt { *; }
-keep class io.ktor.utils.io.core.PreviewKt { *; }
-keep class io.ktor.utils.io.jvm.nio.WritingKt { *; }
-keep class io.ktor.utils.io.streams.InputKt { *; }

-dontwarn io.ktor.client.network.sockets.TimeoutExceptionsCommonKt
-dontwarn io.ktor.client.plugins.HttpRedirect$Config
-dontwarn io.ktor.client.plugins.HttpRedirect$Plugin
-dontwarn io.ktor.client.plugins.HttpRedirect
-dontwarn io.ktor.client.plugins.HttpRequestRetry$Configuration
-dontwarn io.ktor.client.plugins.HttpRequestRetry$DelayContext
-dontwarn io.ktor.client.plugins.HttpRequestRetry$Plugin
-dontwarn io.ktor.client.plugins.HttpRequestRetry
-dontwarn io.ktor.client.plugins.HttpTimeout$HttpTimeoutCapabilityConfiguration
-dontwarn io.ktor.client.plugins.HttpTimeout$Plugin
-dontwarn io.ktor.client.plugins.HttpTimeout
-dontwarn io.ktor.client.plugins.UserAgent$Config
-dontwarn io.ktor.client.plugins.UserAgent$Plugin
-dontwarn io.ktor.client.plugins.UserAgent
-dontwarn io.ktor.util.InternalAPI
-dontwarn io.ktor.util.KtorDsl
-dontwarn io.ktor.utils.io.ByteReadChannelJVMKt
-dontwarn io.ktor.utils.io.CoroutinesKt
-dontwarn io.ktor.utils.io.ReadSessionKt
-dontwarn io.ktor.utils.io.core.Buffer$Companion
-dontwarn io.ktor.utils.io.core.Buffer
-dontwarn io.ktor.utils.io.core.ByteBuffersKt
-dontwarn io.ktor.utils.io.core.BytePacketBuilder
-dontwarn io.ktor.utils.io.core.ByteReadPacket$Companion
-dontwarn io.ktor.utils.io.core.ByteReadPacket
-dontwarn io.ktor.utils.io.core.CloseableJVMKt
-dontwarn io.ktor.utils.io.core.Input
-dontwarn io.ktor.utils.io.core.InputArraysKt
-dontwarn io.ktor.utils.io.core.InputPrimitivesKt
-dontwarn io.ktor.utils.io.core.Output
-dontwarn io.ktor.utils.io.core.OutputPrimitivesKt
-dontwarn io.ktor.utils.io.core.PreviewKt
-dontwarn io.ktor.utils.io.jvm.nio.WritingKt
-dontwarn io.ktor.utils.io.streams.InputKt