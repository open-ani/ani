package me.him188.ani.danmaku.ui

/**
 * 保证队列元素的平均值计算是 O(1) 的队列
 */
class FastLongSumQueue(private val queueSize: Int) {
    private val queue: LongArray = LongArray(queueSize)

    private var tailPtr = 0 // tail index of the queue
    private var sum: Long = 0
    private var occupiedSize = 0
    
    init {
        require(queueSize > 0) { "queueSize must be positive." }
    }
    
    operator fun plusAssign(value: Long) {
        if (occupiedSize >= queueSize) sum -= queue[tailPtr] // remove if full
        
        sum += value
        queue[tailPtr] = value // save new value
        tailPtr = (tailPtr + 1) % queueSize // move to next ptr
        
        if (occupiedSize < queueSize) occupiedSize ++
    }
    
    fun avg(): Long {
        return sum / occupiedSize.coerceAtLeast(1)
    }
}