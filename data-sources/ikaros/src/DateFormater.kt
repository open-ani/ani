import java.text.SimpleDateFormat

class DateFormater {
    companion object {
        /**
         * UTC Date Str.
         * Such as: 2023-10-13T00:00:00
         */
        const val PATTERN_UTC = "yyyy-MM-dd'T'HH:mm:ss"
        fun dateStr2timeStamp(dateStr: String, pattern:String): Long {
            if (dateStr.isEmpty()) {
                return 0
            }
            val simpleDateFormat = SimpleDateFormat(pattern)
            val date = simpleDateFormat.parse(dateStr)
            val timeStamp = date.time
            return timeStamp
        }
    }
}
