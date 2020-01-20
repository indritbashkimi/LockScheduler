package com.ibashkimi.lockscheduler.extention


fun List<CharSequence>.concatenate(separator: String = ", "): String {
    if (size == 0) return ""
    val res = StringBuilder()
    res.append(this[0])
    if (size > 1) {
        res.append(separator)
        for (i in 1 until size - 1) res.append(this[i]).append(separator)
        res.append(this[size - 1])
    }
    return res.toString()
}
