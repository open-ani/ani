package me.him188.ani.danmaku.ui

import androidx.annotation.UiThread
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.him188.ani.danmaku.api.DanmakuLocation
import me.him188.ani.danmaku.api.DanmakuPresentation
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import kotlin.concurrent.Volatile
import kotlin.math.roundToInt

@Stable
class DanmakuHostState(
    danmakuConfigState: State<DanmakuConfig> = mutableStateOf(DanmakuConfig.Default), // state 
    private val danmakuTrackProperties: DanmakuTrackProperties = DanmakuTrackProperties.Default, // state
) {
    private val logger = logger<DanmakuHostState>()
    
    private val danmakuConfig by danmakuConfigState
    /**
     * DanmakuHost 显示大小, 在显示时修改
     */
    private val hostWidthState = mutableIntStateOf(0)
    internal var hostWidth by hostWidthState
    private val hostHeightState = mutableIntStateOf(0)
    internal var hostHeight by hostHeightState
    
    @Volatile
    internal lateinit var baseStyle: TextStyle
    @Volatile
    internal lateinit var textMeasurer: TextMeasurer
    @Volatile
    internal lateinit var density: Density

    private val trackHeightState = mutableIntStateOf(0)
    internal var trackHeight by trackHeightState
        private set
    // currently not configurable
    private val floatingSpeedMultiplierState = mutableFloatStateOf(danmakuTrackProperties.speedMultiplier)
    
    internal val canvasAlpha by derivedStateOf { danmakuConfig.style.alpha }
    internal var paused by mutableStateOf(false)
    internal val isDebug by derivedStateOf { danmakuConfig.isDebug }
    
    private val elapsedFrameTimeNanoState = mutableLongStateOf(0)
    /**
     * 已经过的帧时间
     */
    internal var elapsedFrameTimeNanos by elapsedFrameTimeNanoState
    internal val avgFrameTimeNanos = FastLongSumQueue(120)

    /**
     * 弹幕轨道
     */
    internal val floatingTrack: MutableList<FloatingDanmakuTrack> = mutableListOf()
    internal val topTrack: MutableList<FixedDanmakuTrack> = mutableListOf()
    internal val bottomTrack: MutableList<FixedDanmakuTrack> = mutableListOf()
    
    /**
     * All presented danmaku which should be shown on screen.
     */
    private val mutablePresentDanmaku: MutableList<PositionedDanmakuState> = mutableStateListOf()
    val presentDanmaku: List<PositionedDanmakuState> = mutablePresentDanmaku
    
    /**
     * 监听 轨道数量, 轨道高度 和 弹幕配置项目 的变化
     */
    internal suspend fun observeTrack(measurer: TextMeasurer, density: Density) {
        combine(
            snapshotFlow { danmakuConfig }.distinctUntilChanged(),
            snapshotFlow { hostHeight }.debounce(500)
        ) { config, height ->
            val dummyTextLayout = dummyDanmaku(measurer, baseStyle, config.style, "哈哈哈哈").solidTextLayout
            val verticalPadding = with(density) { (danmakuTrackProperties.verticalPadding * 2).dp.toPx() }

            val trackHeight = dummyTextLayout.size.height + verticalPadding
            val trackCount = height / trackHeight * config.displayArea

            Triple(
                trackCount.roundToInt().coerceAtLeast(1),
                Pair(trackHeight.toInt(), dummyTextLayout.size.width),
                danmakuConfig
            )
        }
            .distinctUntilChanged()
            .collect { (trackCount, sizeProp, config) ->
                val (trackHeight, baseWidth) = sizeProp
                if (trackHeight != this@DanmakuHostState.trackHeight) {
                    this@DanmakuHostState.trackHeight = trackHeight
                }
                updateTrack(trackCount, config, baseWidth, density)
            }
    }

    /**
     * 更新弹幕轨道信息, 更新完成后调用 [invalidate] 显示新的信息.
     */
    @UiThread
    private suspend fun updateTrack(
        count: Int, 
        config: DanmakuConfig, 
        floatingBaseTextLengthForSpeed: Int,
        density: Density,
    ) {
        val newFloatingTrackSpeed = with(density) { danmakuConfig.speed.dp.toPx() }
        val newFloatingTrackSafeSeparation = with(density) { danmakuConfig.safeSeparation.toPx() }
        floatingTrack.setTrackCountImpl(if (config.enableFloating) count else 0) { index ->
            FloatingDanmakuTrack(
                trackIndex = index,
                frameTimeNanos = elapsedFrameTimeNanoState,
                trackHeight = trackHeightState,
                screenWidth = hostWidthState, 
                speedPxPerSecond = newFloatingTrackSpeed,
                safeSeparation = newFloatingTrackSafeSeparation,
                baseTextLength = floatingBaseTextLengthForSpeed,
                speedMultiplier = floatingSpeedMultiplierState,
                onRemoveDanmaku = { removed -> mutablePresentDanmaku.remove(removed) }
            )
        }
        floatingTrack.forEach {
            it.speedPxPerSecond = newFloatingTrackSpeed
            it.safeSeparation = newFloatingTrackSafeSeparation
            it.baseTextLength = floatingBaseTextLengthForSpeed
        }
        topTrack.setTrackCountImpl(if (config.enableTop) count else 0) { index ->
            FixedDanmakuTrack(
                trackIndex = index,
                frameTimeNanos = elapsedFrameTimeNanoState,
                trackHeight = trackHeightState,
                screenWidth = hostWidthState,
                screenHeight = hostHeightState,
                fromBottom = false,
                onRemoveDanmaku = { removed -> mutablePresentDanmaku.remove(removed) }
            )
        }
        bottomTrack.setTrackCountImpl(if (config.enableBottom) count else 0) { index ->
            FixedDanmakuTrack(
                trackIndex = index,
                frameTimeNanos = elapsedFrameTimeNanoState,
                trackHeight = trackHeightState,
                screenWidth = hostWidthState,
                screenHeight = hostHeightState,
                fromBottom = true,
                onRemoveDanmaku = { removed -> mutablePresentDanmaku.remove(removed) }
            )
        }
        invalidate()
    }

    private fun <T : DanmakuTrack> MutableList<T>.setTrackCountImpl(
        count: Int,
        newInstance: (index: Int) -> T,
    ) {
        when {
            size == count -> return
            // 清除 track 的同时要把 track 里的 danmaku 也要清除
            count < size -> repeat(size - count) { removeLast().clearAll() }
            else -> addAll(List(count - size) { newInstance(size + it) })
        }
    }

    @UiThread
    internal suspend fun interpolateFrameLoop() {
        coroutineScope {
            launch {
                var currentFrameTimeNanos = withFrameNanos {
                    // 使用了这一帧来获取时间, 需要补偿平均帧时间
                    elapsedFrameTimeNanos += avgFrameTimeNanos.avg()
                    it
                }

                while (true) {
                    withFrameNanos { nanos ->
                        val delta = nanos - currentFrameTimeNanos
                        elapsedFrameTimeNanos += delta
                        avgFrameTimeNanos += delta
                        currentFrameTimeNanos = nanos
                    }
                }
            }
        }
    }

    /**
     * 逻辑帧 tick, 主要用于移除超出屏幕外或超过时间的弹幕
     */
    @UiThread
    internal suspend fun tickLoop() {
        coroutineScope { 
            launch {
                while (isActive) {
                    floatingTrack.forEach { it.tick() }
                    topTrack.forEach { it.tick() }
                    bottomTrack.forEach { it.tick() }

                    delay(1000 / 30) // 30 fps
                }
            }
        }
    }

    /**
     * 发送弹幕到屏幕, 从最右侧开始向左滚动
     */
    suspend fun trySend(danmaku: DanmakuPresentation): PositionedDanmakuState? {
        return withContext(Dispatchers.Main.immediate) {
            trySendWithTime(danmaku, elapsedFrameTimeNanos)
        }
    }

    // 向指定帧时间的位置发送弹幕
    @UiThread
    private suspend fun trySendWithTime(
        danmaku: DanmakuPresentation, 
        placeFrameTimeNanos: Long
    ): PositionedDanmakuState? {
        while (!::baseStyle.isInitialized || !::textMeasurer.isInitialized) {
            delay(100)
        }
        
        fun createDanmakuState(): DanmakuState {
            return DanmakuState(
                presentation = danmaku,
                measurer = textMeasurer,
                baseStyle = baseStyle,
                style = danmakuConfig.style,
                enableColor = danmakuConfig.enableColor,
                isDebug = danmakuConfig.isDebug,
            )
        }
        
        return withContext(Dispatchers.Main.immediate) {
            val positionedDanmakuState: PositionedDanmakuState? = when (danmaku.danmaku.location) {
                DanmakuLocation.NORMAL -> floatingTrack.firstNotNullOfOrNull { track ->
                    track.tryPlace(createDanmakuState(), placeFrameTimeNanos)
                }
                else -> (if (danmaku.danmaku.location == DanmakuLocation.TOP) topTrack else bottomTrack)
                    .firstNotNullOfOrNull { track -> track.tryPlace(createDanmakuState(), placeFrameTimeNanos) }
            }
            // if danmakuState is not null, it means successfully placed.
            positionedDanmakuState?.also(mutablePresentDanmaku::add)
        }
    }

    /**
     * 清空屏幕并填充 [list] 里的弹幕到屏幕
     */
    suspend fun repopulate(list: List<DanmakuPresentation>) {
        if (list.isEmpty()) return
        while (!::density.isInitialized || hostWidth == 0 ||
            !::baseStyle.isInitialized || !::textMeasurer.isInitialized) delay(100)
        
        withContext(Dispatchers.Main.immediate) {
            clearPresentDanmaku()

            val floatingDanmaku = list.filter { it.danmaku.location == DanmakuLocation.NORMAL }
            if (floatingDanmaku.isNotEmpty()) {
                // list 弹幕的所有时间
                val danmakuDurationMillis = list.last().danmaku.playTimeMillis - list.first().danmaku.playTimeMillis
                // 弹幕从左滑倒右边需要的时间(毫秒)
                val screenDurationMillis = hostWidth / with(density) { danmakuConfig.speed.dp.toPx().toLong() } * 1_000

                // 我们先把 list 中第一条弹幕放到最左边
                val firstDanmakuTimeMillis = list.first().danmaku.playTimeMillis
                
                val firstDanmakuPlaceTimeNanos = elapsedFrameTimeNanos + // 屏幕最右侧
                        when {
                            // 如果 repopulate 中的弹幕有可以显示在开始的, 例如用户跳到刚开始播放
                            // 那需要把弹幕按时间放置到屏幕上, 这也是比较符合直觉的
                            firstDanmakuTimeMillis < screenDurationMillis -> {
                                -firstDanmakuTimeMillis * 1_000_000L // seek to 第一条弹幕出现时间
                            }
                            // 没有跳到最开始, 放到屏幕中间即可
                            else -> (-screenDurationMillis + // seek to 屏幕左侧 
                                    ((screenDurationMillis - danmakuDurationMillis)
                                        .coerceAtLeast(0) / 2) // seek to 屏幕中间
                                    ) * 1_000_000L
                        }

                floatingDanmaku.map { danmaku ->
                    val playTimeNanos = firstDanmakuPlaceTimeNanos +
                            (danmaku.danmaku.playTimeMillis - firstDanmakuTimeMillis) * 1_000_000L
                    trySendWithTime(danmaku, playTimeNanos)
                }
            }

            // 非浮动弹幕直接放到当前时间即可
            list
                .filter { it.danmaku.location != DanmakuLocation.NORMAL }
                .forEach { danmaku -> trySend(danmaku) }
        }
    }
    
    // 放置弹幕并指定放置的帧时间位置
    @UiThread
    private suspend fun repopulatePositioned(list: List<PositionedDanmakuState>) {
        while (!::baseStyle.isInitialized || !::textMeasurer.isInitialized) { delay(100) }
        
        withContext(Dispatchers.Main.immediate) {
            clearPresentDanmaku()
            for (danmaku in list) { 
                trySendWithTime(danmaku.state.presentation, danmaku.placeFrameTimeNanos) 
            }
        }
    }
    
    @UiThread
    private fun clearPresentDanmaku() {
        floatingTrack.forEach { it.clearAll() }
        topTrack.forEach { it.clearAll() }
        bottomTrack.forEach { it.clearAll() }

        if (mutablePresentDanmaku.isNotEmpty()) {
            logger.warn { "presentDanmaku is not totally cleared after releasing track. This may cause memory leak" }
            mutablePresentDanmaku.clear()
        }
    }

    /**
     * 重置当前弹幕状态, 重新绘制弹幕
     */
    @UiThread
    private suspend fun invalidate() {
        while (!::baseStyle.isInitialized || !::textMeasurer.isInitialized) {
            delay(100)
        }

        withContext(Dispatchers.Main.immediate) {
            if (mutablePresentDanmaku.isEmpty()) return@withContext
            val mapped = mutablePresentDanmaku.map { OverridePlaceTimeDanmakuState(it, it.placeFrameTimeNanos) }
            repopulatePositioned(mapped)
        }
    }
    
    @UiThread
    fun setPause(pause: Boolean) {
        paused = pause
    }
    
    inner class OverridePlaceTimeDanmakuState(
        private val correspondingState: PositionedDanmakuState,
        override val placeFrameTimeNanos: Long
    ) : PositionedDanmakuState by correspondingState {
        override fun equals(other: Any?): Boolean {
            if (other == null) return false
            if (other is FloatingDanmakuTrack.FloatingDanmaku) return other.state === this.state
            if (other is FixedDanmakuTrack.FixedDanmaku) return other.state === this.state
            return (other as? OverridePlaceTimeDanmakuState) === this
        }

        override fun hashCode(): Int {
            var result = correspondingState.state.hashCode()
            result = 31 * result + placeFrameTimeNanos.hashCode()
            return result
        }
    }

    /**
     * DanmakuState which is positioned and can be placed on [Canvas].
     */
    interface PositionedDanmakuState {
        val state: DanmakuState
        val placeFrameTimeNanos: Long
        
        fun calculatePosX(): Float
        fun calculatePosY(): Float
    }
}