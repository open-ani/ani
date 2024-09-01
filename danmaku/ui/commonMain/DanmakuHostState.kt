package me.him188.ani.danmaku.ui

import androidx.annotation.UiThread
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuLocation
import me.him188.ani.danmaku.api.DanmakuPresentation
import kotlin.math.roundToInt

@Stable
class DanmakuHostState(
    danmakuConfigState: State<DanmakuConfig> = mutableStateOf(DanmakuConfig.Default), // state 
    private val danmakuTrackProperties: DanmakuTrackProperties = DanmakuTrackProperties.Default,
) {
    private val danmakuConfig by danmakuConfigState
    private val uiContext: UIContext = UIContext()
    /**
     * DanmakuHost 显示大小, 在显示时修改
     */
    private val hostWidthState = mutableIntStateOf(0)
    internal var hostWidth by hostWidthState
    private val hostHeightState = mutableIntStateOf(0)
    internal var hostHeight by hostHeightState

    private val trackHeightState = mutableIntStateOf(0)
    internal var trackHeight by trackHeightState
        private set
    // currently not configurable
    // private val floatingSpeedMultiplierState = mutableFloatStateOf(danmakuTrackProperties.speedMultiplier)
    // currently not configurable
    private val fixedDanmakuPresentDuration = mutableLongStateOf(danmakuTrackProperties.fixedDanmakuPresentDuration)
    
    internal val canvasAlpha by derivedStateOf { danmakuConfig.style.alpha }
    internal var paused by mutableStateOf(false)
    internal val isDebug by derivedStateOf { danmakuConfig.isDebug }
    
    private val elapsedFrameTimeNanoState = mutableLongStateOf(0)
    /**
     * 已经过的帧时间, 在 [setPaused] 设置暂停时此帧时间也会暂停
     */
    internal var elapsedFrameTimeNanos by elapsedFrameTimeNanoState
    // internal val avgFrameTimeNanos = FastLongSumQueue(120)

    /**
     * 弹幕轨道
     */
    internal val floatingTrack: MutableList<FloatingDanmakuTrack<StyledDanmaku>> = mutableListOf()
    internal val topTrack: MutableList<FixedDanmakuTrack<StyledDanmaku>> = mutableListOf()
    internal val bottomTrack: MutableList<FixedDanmakuTrack<StyledDanmaku>> = mutableListOf()
    
    /**
     * 所有在 [floatingTrack], [topTrack] 和 [bottomTrack] 弹幕.
     * 在这里保留一个引用, 方便在 [invalidate] 的时候重新计算所有弹幕位置.
     * 大部分弹幕是按时间排序的, 确保 [removeFirst] 操作能消耗较低的时间.
     */
    internal val presentDanmaku: MutableList<PositionedDanmakuState<StyledDanmaku>> = mutableListOf()
    
    fun setUIContext(
        baseStyle: TextStyle,
        textMeasurer: TextMeasurer,
        density: Density
    ) {
        uiContext.set(baseStyle, textMeasurer, density)
    }
    
    /**
     * 监听 轨道数量, 轨道高度 和 弹幕配置项目 的变化
     */
    internal suspend fun observeTrack(measurer: TextMeasurer) {
        combine(
            snapshotFlow { danmakuConfig }.distinctUntilChanged(),
            snapshotFlow { hostHeight }.distinctUntilChanged()
        ) { config, height ->
            val dummyTextLayout = 
                dummyDanmaku(measurer, uiContext.getBaseStyle(), config.style, "哈哈哈哈").solidTextLayout
            val verticalPadding = with(uiContext.getDensity()) { (danmakuTrackProperties.verticalPadding * 2).dp.toPx() }

            val trackHeight = dummyTextLayout.size.height + verticalPadding
            val trackCount = height / trackHeight * config.displayArea

            Triple(
                trackCount.roundToInt().coerceAtLeast(1),
                trackHeight.toInt(), /*Pair(trackHeight.toInt(), dummyTextLayout.size.width)*/
                danmakuConfig,
            )
        }
            .distinctUntilChanged()
            .collect { (trackCount, trackHeight, config) ->
                if (trackHeight != this@DanmakuHostState.trackHeight) {
                    this@DanmakuHostState.trackHeight = trackHeight
                }
                updateTrack(trackCount, config)
            }
    }

    /**
     * 更新弹幕轨道信息, 更新完成后调用 [invalidate] 显示新的信息.
     */
    @UiThread
    private suspend fun updateTrack(
        count: Int, 
        config: DanmakuConfig,
    ) {
        val newFloatingTrackSpeed = with(uiContext.getDensity()) { danmakuConfig.speed.dp.toPx() }
        val newFloatingTrackSafeSeparation = with(uiContext.getDensity()) { danmakuConfig.safeSeparation.toPx() }
        floatingTrack.setTrackCountImpl(if (config.enableFloating) count else 0) { index ->
            FloatingDanmakuTrack(
                trackIndex = index,
                frameTimeNanosState = elapsedFrameTimeNanoState,
                trackHeight = trackHeightState,
                trackWidth = hostWidthState, 
                speedPxPerSecond = newFloatingTrackSpeed,
                safeSeparation = newFloatingTrackSafeSeparation,
                // baseTextLength = floatingBaseTextLengthForSpeed,
                // speedMultiplier = floatingSpeedMultiplierState,
                onRemoveDanmaku = { removed -> presentDanmaku.removeFirst { it.danmaku == removed.danmaku } }
            )
        }
        floatingTrack.forEach {
            it.speedPxPerSecond = newFloatingTrackSpeed
            it.safeSeparation = newFloatingTrackSafeSeparation
            // it.baseTextLength = floatingBaseTextLengthForSpeed
        }
        topTrack.setTrackCountImpl(if (config.enableTop) count else 0) { index ->
            FixedDanmakuTrack(
                trackIndex = index,
                frameTimeNanosState = elapsedFrameTimeNanoState,
                trackHeight = trackHeightState,
                hostWidth = hostWidthState,
                hostHeight = hostHeightState,
                fromBottom = false,
                durationMillis = fixedDanmakuPresentDuration,
                onRemoveDanmaku = { removed -> presentDanmaku.removeFirst { it.danmaku == removed.danmaku } }
            )
        }
        bottomTrack.setTrackCountImpl(if (config.enableBottom) count else 0) { index ->
            FixedDanmakuTrack(
                trackIndex = index,
                frameTimeNanosState = elapsedFrameTimeNanoState,
                trackHeight = trackHeightState,
                hostWidth = hostWidthState,
                hostHeight = hostHeightState,
                fromBottom = true,
                durationMillis = fixedDanmakuPresentDuration,
                onRemoveDanmaku = { removed -> presentDanmaku.removeFirst { it.danmaku == removed.danmaku } }
            )
        }
        invalidate()
    }

    @UiThread
    internal suspend fun interpolateFrameLoop() {
        var currentFrameTimeNanos = withFrameNanos {
            // 使用了这一帧来获取时间, 需要补偿平均帧时间
            // elapsedFrameTimeNanos += avgFrameTimeNanos.avg()
            it
        }

        while (true) {
            withFrameNanos { nanos ->
                val delta = nanos - currentFrameTimeNanos
                
                elapsedFrameTimeNanos += delta
                // avgFrameTimeNanos += delta
                currentFrameTimeNanos = nanos
                
                for (danmaku in presentDanmaku) {
                    // 没放置的弹幕要立即放置并计算位置
                    if (danmaku.placeFrameTimeNanos == PositionedDanmakuState.NOT_PLACED) {
                        danmaku.placeFrameTimeNanos = elapsedFrameTimeNanos
                        danmaku.calculatePosX()
                    } else when (danmaku) {
                        // 浮动弹幕只需要重新计算 X
                        is FloatingDanmakuTrack.FloatingDanmaku -> 
                            danmaku.calculatePos(calculateY = false)
                        // 固定弹幕什么都不用重新计算
                        is FixedDanmakuTrack.FixedDanmaku -> 
                            danmaku.calculatePos(calculateX = false, calculateY = false)
                    }
                }
            }
        }
    }

    /**
     * 逻辑帧 tick, 主要用于移除超出屏幕外或超过时间的弹幕
     */
    @UiThread
    internal fun tick() {
        floatingTrack.forEach { it.tick() }
        topTrack.forEach { it.tick() }
        bottomTrack.forEach { it.tick() }
    }

    /**
     * 发送弹幕到屏幕, 并将弹幕的引用加入 [presentDanmaku]
     */
    suspend fun trySend(
        danmaku: DanmakuPresentation, 
        placeFrameTimeNanos: Long = PositionedDanmakuState.NOT_PLACED
    ): PositionedDanmakuState<StyledDanmaku>? {
        val track = when (danmaku.danmaku.location) {
            DanmakuLocation.NORMAL -> floatingTrack
            DanmakuLocation.TOP -> topTrack
            DanmakuLocation.BOTTOM -> bottomTrack
        }
        val styledDanmaku = StyledDanmaku(
            presentation = danmaku,
            measurer = uiContext.getTextMeasurer(),
            baseStyle = uiContext.getBaseStyle(),
            style = danmakuConfig.style,
            enableColor = danmakuConfig.enableColor,
            isDebug = danmakuConfig.isDebug,
        )
        
        return withContext(Dispatchers.Main.immediate) { // avoid ConcurrentModificationException
            val positionedDanmakuState = track.firstNotNullOfOrNull {
                it.tryPlace(styledDanmaku, placeFrameTimeNanos)
            }
            positionedDanmakuState?.also(presentDanmaku::add)
        }
    }

    /**
     * 清空屏幕并填充 [list] 到屏幕.
     * 
     * **此方法不会重新排序 [list], 所以弹幕必须已经按发送时间排序**.
     * 
     * **在通常情况下, 此方法假设 [list] 中弹幕是在 repopulate 时的[帧时间][elapsedFrameTimeNanos]之前发送的弹幕**.
     * 因此, [list] 最后一条浮动弹幕会被放到屏幕的最右侧. 
     * 其他弹幕将以其[发送时间戳][Danmaku.playTimeMillis]为基准依次**向前**排列.
     * 
     * 但是如果 [list] 中的第一条浮动弹幕是可以显示在最开始 trackDurationMillis 内的时间
     * (trackDurationMillis 表示一条浮动的那幕从屏幕最右侧滚动到最左侧的时间),
     * 那么 [list] 中的第一条弹幕会被放到屏幕对应位置来模拟最开始的弹幕滚动过程,
     * 其他弹幕将以其[发送时间戳][Danmaku.playTimeMillis]为基准依次**向后**排列.
     * 
     * 例如 trackDurationMillis 为 `10000ms`, [list] 中的第一条弹幕的发送时间为 `5000ms`,
     * 那么第一条弹幕就会被放置在轨道的中间位置.
     * 
     * 通过设置 [timeOffsetMillis] 来指定整体的弹幕放置时间偏移.
     * 若 `timeOffsetMillis < 0L` 则弹幕放置的位置会向左偏移 [timeOffsetMillis] 的[帧时间][elapsedFrameTimeNanos].
     * 
     * @param timeOffsetMillis 弹幕放置偏移
     * @param list 要填充到屏幕的弹幕, 必须按发送时间戳排序.
     */
    suspend fun repopulate(list: List<DanmakuPresentation>, timeOffsetMillis: Long = 0L) {
        if (list.isEmpty()) return
        withContext(Dispatchers.Main.immediate) { clearPresentDanmaku() }
        
        val isFloatingDanmaku = { danmaku: DanmakuPresentation -> 
            danmaku.danmaku.location == DanmakuLocation.NORMAL
        }
        
        val floatingDanmaku = list.filter(isFloatingDanmaku)
        if (floatingDanmaku.isNotEmpty()) {
            // 第一条和最后一条浮动弹幕发送时间戳
            val firstDanmakuTimeMillis = list.first(isFloatingDanmaku).danmaku.playTimeMillis
            val danmakuDurationMillis = list.last(isFloatingDanmaku).danmaku.playTimeMillis - firstDanmakuTimeMillis
            // 弹幕从左滑倒右边需要的时间(毫秒)
            val trackDurationMillis =
                hostWidth / with(uiContext.getDensity()) { danmakuConfig.speed.dp.toPx().toLong() } * 1_000

            val firstDanmakuPlaceTimeNanos = if (firstDanmakuTimeMillis <= trackDurationMillis) {
                // repopulate 了最开始的弹幕, 要向后排列.
                // 首条弹幕出现的时间在屏幕对应位置
                elapsedFrameTimeNanos - firstDanmakuTimeMillis * 1_000_000L
            } else {
                // 最后一条弹幕在屏幕最右侧, 所以首条弹幕出现的位置在前 danmakuDurationMillis 的帧时间.
                // 如果超过了 elapsedFrameTimeNanos - trackDurationMillis, 那在 trySend 的时候也不会被放置.
                elapsedFrameTimeNanos - danmakuDurationMillis * 1_000_000L
            }

            floatingDanmaku.forEach { danmaku ->
                val playTimeNanos = firstDanmakuPlaceTimeNanos +
                        (danmaku.danmaku.playTimeMillis - firstDanmakuTimeMillis) * 1_000_000L
                trySend(danmaku, playTimeNanos + timeOffsetMillis * 1_000_000L)
            }
        }
        
        val fixedDanmaku = list.filterNot(isFloatingDanmaku)
        if (fixedDanmaku.isNotEmpty()) {
            val lastDanmakuTimeMillis = fixedDanmaku.last().danmaku.playTimeMillis
            val lastDanmakuPlaceTimeNanos = elapsedFrameTimeNanos
            // 浮动弹幕倒序 place 进 presentDanmaku 里
            fixedDanmaku.asReversed().forEach { danmaku ->
                val playTimeNanos = lastDanmakuPlaceTimeNanos -
                        (lastDanmakuTimeMillis - danmaku.danmaku.playTimeMillis) * 1_000_000L
                trySend(danmaku, playTimeNanos + timeOffsetMillis * 1_000_000L)
            }
        }
    }
    
    @UiThread
    private fun clearPresentDanmaku() {
        floatingTrack.forEach { it.clearAll() }
        topTrack.forEach { it.clearAll() }
        bottomTrack.forEach { it.clearAll() }

        check(presentDanmaku.size == 0) {
            "presentDanmaku is not totally cleared after releasing track."
        }
    }

    /**
     * 重置当前弹幕样式属性
     */
    @UiThread
    private suspend fun invalidate() {
        if (presentDanmaku.isEmpty()) return
        val presentDanmakuCopied = presentDanmaku.toList()
        
        clearPresentDanmaku()
        
        for (danmaku in presentDanmakuCopied) {
            trySend(danmaku.danmaku.presentation, danmaku.placeFrameTimeNanos)
        }
    }
    
    @UiThread
    fun setPaused(pause: Boolean) {
        paused = pause
    }
    
    private class UIContext {
        private lateinit var baseStyle: TextStyle
        private lateinit var textMeasurer: TextMeasurer
        private lateinit var density: Density
        
        private val setDeferred: CompletableDeferred<Unit> = CompletableDeferred()
        
        fun set(baseStyle: TextStyle, textMeasurer: TextMeasurer, density: Density) {
            this.baseStyle = baseStyle
            this.textMeasurer = textMeasurer
            this.density = density
            
            setDeferred.complete(Unit)
        }
        
        suspend fun getBaseStyle(): TextStyle {
            setDeferred.await()
            return baseStyle
        }

        suspend fun getTextMeasurer(): TextMeasurer {
            setDeferred.await()
            return textMeasurer
        }

        suspend fun getDensity(): Density {
            setDeferred.await()
            return density
        }
    }
}

/**
 * 发送弹幕, 在发送成功之前一直挂起
 * 
 * TODO: 保证必须优先发送出去, 现在的实现会导致 trySend 和 delay 抢调度
 */
suspend inline fun DanmakuHostState.send(danmaku: DanmakuPresentation) {
    while (trySend(danmaku) == null) delay(50)
}

private fun <D : SizeSpecifiedDanmaku, T : DanmakuTrack<D>> MutableList<T>.setTrackCountImpl(
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

private inline fun <T> MutableList<T>.removeFirst(predicate: (T) -> Boolean): T? {
    val index = indexOfFirst(predicate)
    if (index == -1) return null
    return removeAt(index)
}