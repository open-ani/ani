package me.him188.ani.app.platform

import android.util.Log
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.util.StatusPrinter
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream

object AndroidLoggingConfigurator {
    @Language("xml")
    private fun getXML(logsDir: String) = """
<configuration>
    <!-- Add `debug='true'` to the appender to enable logback debug logs -->
    <appender name="ROLLING" class="ch.qos.logback.core.rolling.RollingFileAppender" debug='true'>
        <file>$logsDir/app.log</file> 
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>$logsDir/app.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>3</maxHistory>
        </rollingPolicy>

        <encoder>
            <pattern>%date{yyyy-MM-dd HH:mm:ss} %-5level %logger{10} [%file:%line] %msg%n%ex
            </pattern>
        </encoder>
    </appender>

    <appender name="LOGCAT" class="ch.qos.logback.classic.android.LogcatAppender">
        <encoder>
            <pattern>%msg%n%ex%n</pattern>
        </encoder>
    </appender>

    <logger name="io.ktor.client.plugins" level="OFF" />

    <root level="TRACE">
        <appender-ref ref="ROLLING" />
        <appender-ref ref="LOGCAT" />
    </root>

    <logger name="logcatDebug" level="DEBUG">
        <appender-ref ref="LOGCAT" />
    </logger>
</configuration>
"""

    fun configure(logsDir: String) {
        Log.i("AndroidLoggingConfigurator", "Configuring logback, logsDir=$logsDir")
        LoggerFactory.getILoggerFactory().also { factory ->
            factory as LoggerContext
            factory.stop()
            kotlin.runCatching {
                JoranConfigurator().apply {
                    context = factory
                    doConfigure(ByteArrayInputStream(getXML(logsDir).encodeToByteArray()))
                }
            }.onFailure {
                Log.e("AndroidLoggingConfigurator", "Failed to configure logback", it)
            }
            StatusPrinter.print(factory)
        }
    }
}