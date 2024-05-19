package me.him188.ani.test

fun <T> List<T>.permutedSequence(): Sequence<List<T>> {
    if (size == 1) return sequenceOf(this)
    return sequence {
        for (i in indices) {
            val first = this@permutedSequence[i]
            val remaining = toMutableList()
            remaining.removeAt(i)
            val remainingPermutations = remaining.permutedSequence()
            for (p in remainingPermutations) {
                yield(listOf(first) + p)
            }
        }
    }
}