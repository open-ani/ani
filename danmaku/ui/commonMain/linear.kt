package me.him188.ani.danmaku.ui

//fun <T> linear(
//    pixelPerSec: Float,
//): AnimationSpec<T> = LinearAnimationSpec(pixelPerSec)
//
//private class LinearAnimationSpec<T>(
//    private val velocity: Float,
//) : AnimationSpec<T> {
//    override fun <V : AnimationVector> vectorize(converter: TwoWayConverter<T, V>): VectorizedAnimationSpec<V> =
//        LinearVectorizedAnimationSpec(converter, velocity)
//}
//
//private class LinearVectorizedAnimationSpec<T, V : AnimationVector>(
//    private val converter: TwoWayConverter<T, V>,
//    private val pixelPerSec: Float,
//) : VectorizedAnimationSpec<V> {
//    override val isInfinite: Boolean get() = false
//
//    override fun getVelocityFromNanos(playTimeNanos: Long, initialValue: V, targetValue: V, initialVelocity: V): V {
//        val t = converter.convertFromVector(initialVelocity)
//        
//        return converter.convertToVector(pixelPerSec * 1e-9f)
//    }
//
//    override fun getValueFromNanos(playTimeNanos: Long, initialValue: V, targetValue: V, initialVelocity: V): V {
//        val initial = converter.convertFromVector(initialVelocity)
//        val target = converter.convertFromVector(targetValue)
//        return converter.convertToVector((initial + playTimeNanos * pixelPerSec * 1e9f).coerceAtMost(target))
//    }
//
//    override fun getDurationNanos(initialValue: V, targetValue: V, initialVelocity: V): Long {
//        val initial = converter.convertFromVector(initialValue)
//        val target = converter.convertFromVector(targetValue)
//        return ((target - initial) / pixelPerSec * 1e9).toLong()
//    }
//}
