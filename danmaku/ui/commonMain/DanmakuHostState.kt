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
import me.him188.ani.danmaku.api.DanmakuLocation
import me.him188.ani.danmaku.api.DanmakuPresentation
import kotlin.math.roundToInt

@Stable
class DanmakuHostState(
    danmakuConfigState: State<DanmakuConfig> = mutableStateOf(DanmakuConfig.Default), // state 
    private val danmakuTrackProperties: DanmakuTrackProperties = DanmakuTrackProperties.Default,
) {
    private val danmakuConfig by danmakuConfigState
    private val uiContextDeferred: CompletableDeferred<UIContext> = CompletableDeferred()
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
        uiContextDeferred.complete(UIContext(baseStyle, textMeasurer, density))
    }
    
    /**
     * 监听 轨道数量, 轨道高度 和 弹幕配置项目 的变化
     */
    internal suspend fun observeTrack(measurer: TextMeasurer) {
        val uiContext = uiContextDeferred.await()
        combine(
            snapshotFlow { danmakuConfig }.distinctUntilChanged(),
            snapshotFlow { hostHeight }.distinctUntilChanged()
        ) { config, height ->
            val dummyTextLayout = 
                dummyDanmaku(measurer, uiContext.baseStyle, config.style, "哈哈哈哈").solidTextLayout
            val verticalPadding = with(uiContext.density) { (danmakuTrackProperties.verticalPadding * 2).dp.toPx() }

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
        val uiContext = uiContextDeferred.await()
        
        val newFloatingTrackSpeed = with(uiContext.density) { danmakuConfig.speed.dp.toPx() }
        val newFloatingTrackSafeSeparation = with(uiContext.density) { danmakuConfig.safeSeparation.toPx() }
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
        val uiContext = uiContextDeferred.await()
        val track = when (danmaku.danmaku.location) {
            DanmakuLocation.NORMAL -> floatingTrack
            DanmakuLocation.TOP -> topTrack
            DanmakuLocation.BOTTOM -> bottomTrack
        }
        val styledDanmaku = StyledDanmaku(
            presentation = danmaku,
            measurer = uiContext.textMeasurer,
            baseStyle = uiContext.baseStyle,
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
     * 清空屏幕并填充 [list] 到屏幕, 弹幕 [list] 必须已经按发送时间排序. 此方法不会重新排序弹幕.
     * 
     * 通过设置 [firstPlace] 来指定首条弹幕期望显示的位置.
     * 
     * - 如果 [firstPlace] 为 `null`, 则所有弹幕会被放置到屏幕中间的位置. 
     *   若 [list] 的首条弹幕时间戳小于一条弹幕从左到右的运动时间, 则会尝试将首条弹幕按此时间放到对应的位置以符合直觉. 
     *   
     *   例如, 弹幕从左到右的运动时间为 `4s`, [list] 的首条弹幕时间戳为 `3s`, 
     *   那么首条弹幕将会放到屏幕左侧 1/4 的位置, 其他弹幕依次按时间排放.
     *   
     *   ```
     *   |-------(--------)----------------|
     *           ^ 左侧为 1/4 屏幕位置
     *   ```
     *  
     * - 如果 [firstPlace] 不为 `null`, 则其范围必须为 `0.0 - 1.0`, 表示首条弹幕放到距屏幕左侧百分之多少的位置.
     *   
     *   例如, `firstPlace` 为 `0.75`, 则首条弹幕会放置到距离屏幕左侧 `75%` 的位置.
     *   ```
     *   |------------------------(--------|
     *                            ^ 左侧为 75% 屏幕位置
     *   ```
     *   
     * @param list 要填充到屏幕的弹幕, 必须按发送时间戳排序.
     * @param firstPlace 首条弹幕出现的屏幕位置百分比. 为 `null` 则让方法自动决定.
     */
    suspend fun repopulate(list: List<DanmakuPresentation>, firstPlace: Float? = null) {
        if (list.isEmpty()) return
        val uiContext = uiContextDeferred.await()
        
        withContext(Dispatchers.Main.immediate) { clearPresentDanmaku() }
        
        val isFloatingDanmaku = { danmaku: DanmakuPresentation -> 
            danmaku.danmaku.location == DanmakuLocation.NORMAL
        }
        
        // list 中有没有浮动弹幕是两种处理过程
        // 这么做是为了在 presentDanmaku 中的弹幕可以保证按发送时间戳排序
        if (list.any(isFloatingDanmaku)) {
            // 首条浮动弹幕发送时间戳
            val firstDanmakuTimeMillis = list.first(isFloatingDanmaku).danmaku.playTimeMillis
            // list 中的所有弹幕时间间隔
            val danmakuDurationMillis = list.last(isFloatingDanmaku).danmaku.playTimeMillis - firstDanmakuTimeMillis
            // 弹幕从左滑倒右边需要的时间(毫秒)
            val trackDurationMillis = 
                hostWidth / with(uiContext.density) { danmakuConfig.speed.dp.toPx().toLong() } * 1_000

            // 浮动弹幕首条弹幕放置的位置对应的帧时间
            val firstDanmakuPlaceTimeNanos = elapsedFrameTimeNanos + // 屏幕最右侧
                    when {
                        // 指定 firstPlace 就按照参数指定位置
                        firstPlace != null -> {
                            ((firstPlace.coerceIn(0f, 1f) - 1f) * trackDurationMillis).toLong()
                        }
                        // 如果 list 里的弹幕有可以显示在开始的, 例如用户跳到刚开始播放
                        // 那需要把弹幕按时间放置到屏幕上, 这也是比较符合直觉的
                        firstDanmakuTimeMillis < trackDurationMillis -> {
                            -firstDanmakuTimeMillis * 1_000_000L // seek to 第一条弹幕出现时间
                        }
                        // 没有跳到最开始, 放到屏幕中间即可
                        else -> (-trackDurationMillis + // seek to 屏幕左侧 
                                ((trackDurationMillis - danmakuDurationMillis)
                                    .coerceAtLeast(0) / 2) // seek to 屏幕中间
                                ) * 1_000_000L
                    }
            
            
            list.forEach { danmaku -> 
                if (isFloatingDanmaku(danmaku)) {
                    val playTimeNanos = firstDanmakuPlaceTimeNanos + 
                            (danmaku.danmaku.playTimeMillis - firstDanmakuTimeMillis) * 1_000_000L
                    trySend(danmaku, playTimeNanos)
                } else {
                    trySend(danmaku) // send fixed danmaku immediately
                }
            }
        } else {
            // send fixed danmaku immediately
            list.forEach { danmaku -> trySend(danmaku) }
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
    
    private class UIContext(
        val baseStyle: TextStyle,
        val textMeasurer: TextMeasurer,
        val density: Density
    )
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