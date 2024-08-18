package me.him188.ani.app.tools

///**
// * 使用历史的值 + 当前的值
// */
//@Serializable
//data class StatsHistory(
//    private val value1Id: Int,
//    private val value1: Long,
//
//    private val value2Id: Int,
//    private val value2: Long,
//
//    private val savedIndex: Int, // 1 or 2, 表示哪一个 value 存着历史数据
//) {
//    companion object {
//        fun newId(): Int = Random.nextInt()
//
//        val Empty = StatsHistory(-1, 0, -1, 0, savedIndex = 0)
//    }
//
//    fun getPreviousValue(
//        currentId: Int,
//        default: Long = 0,
//    ): Long {
//        // 如果 currentId 与任意 valueId 匹配, 说明另外一个是历史值
//        return when (currentId) {
//            value1Id -> {
//                if (value2Id != -1) {
//                    value2
//                } else {
//                    default
//                }
//            }
//
//            value2Id -> {
//                if (value1Id != -1) {
//                    value1
//                } else {
//                    default
//                }
//            }
//
//            else -> when (savedIndex) {
//                1 -> value1
//                2 -> value2
//                else -> default
//            }
//        }
//    }
//
//    fun update(currentId: Int, value: Long): StatsHistory {
//        return when (currentId) {
//            value1Id -> return copy(value1 = value)
//            value2Id -> return copy(value2 = value)
//            else -> {
//                when (currentIndex) {
//                    1 -> copy(value1 = value, currentIndex = 1)
//                    2 -> copy(value1 = value, currentIndex = 1)
//                    3 -> copy(value1 = value, currentIndex = 1)
//                    else -> copy(value1 = value, currentIndex = 1)
//                }
//            }
//        }
//    }
//}
