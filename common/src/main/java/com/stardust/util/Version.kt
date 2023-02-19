package com.stardust.util

/**
 * 比较两个版本号的大小。
 *
 * @param version1 第一个版本号
 * @param version2 第二个版本号
 * @return 如果第一个版本号小于第二个版本号，则返回负整数；
 *         如果第一个版本号大于第二个版本号，则返回正整数；
 *         如果两个版本号相等，则返回0。
 *         Compares two version numbers.
 *
 * @param version1 the first version number
 * @param version2 the second version number
 * @return a negative integer if version1 is less than version2,
 *         a positive integer if version1 is greater than version2,
 *         or zero if they are equal.
 */
fun compareVersions(version1: String, version2: String): Int {
    val arr1 = version1.split(".")
    val arr2 = version2.split(".")
    val size = maxOf(arr1.size, arr2.size)

    for (i in 0 until size) {
        val num1Str = if (i < arr1.size) arr1[i] else "0"
        val num2Str = if (i < arr2.size) arr2[i] else "0"
        val num1 = getNumericPart(num1Str)
        val num2 = getNumericPart(num2Str)
        val preRelease1 = getPreReleasePart(num1Str)
        val preRelease2 = getPreReleasePart(num2Str)

        if (num1 < num2) {
            return -1
        } else if (num1 > num2) {
            return 1
        } else if (preRelease1.isEmpty() && preRelease2.isNotEmpty()) {
            return 1
        } else if (preRelease1.isNotEmpty() && preRelease2.isEmpty()) {
            return -1
        } else if (preRelease1.isNotEmpty() && preRelease2.isNotEmpty()) {
            val preReleaseResult = comparePreReleaseVersions(preRelease1, preRelease2)
            if (preReleaseResult != 0) {
                return preReleaseResult
            }
        }
    }
    return 0
}

/**
 * 获取版本号字符串的数字部分。
 * 如果版本号字符串中包含预发布版本，则去除预发布版本部分。
 *
 * @param str 版本号字符串
 * @return 版本号的数字部分
 *         Gets the numeric part of a version number string.
 *         If the version number string contains a pre-release version, removes the pre-release part.
 *
 * @param str the version number string
 * @return the numeric part of the version number
 */
private fun getNumericPart(str: String): Int {
    val index = str.indexOf('-')
    return if (index == -1) str.toInt() else str.substring(0, index).toInt()
}

/**
 * 获取版本号字符串的预发布版本部分。
 * 如果版本号字符串中不包含预发布版本，则返回空字符串。
 *
 * @param str 版本号字符串
 * @return 版本号的预发布版本部分
 *         Gets the pre-release part of a version number string.
 *         If the version number string does not contain a pre-release version, returns an empty string.
 *
 * @param str the version number string
 * @return the pre-release part of the version number
 */
private fun getPreReleasePart(str: String): String {
    val index = str.indexOf('-')
    return if (index == -1) "" else str.substring(index + 1)
}

/**
 * 比较两个版本号的预发布版本部分。
 *
 * @param str1 第一个版本号的预发布版本部分
 * @param str2 第二个版本号的预发布版本部分
 * @return 如果第一个版本号的预发布版本部分小于第二个版本号的预发布版本部分，则返回负整数；
 *         如果第一个版本号的预发布版本部分大于第二个版本号的预发布版本部分，则返回正整数；
 *         如果两个版本号的预发布版本部分相等，则返回0。
 *         Compares the pre-release parts of two version numbers.
 *
 * @param str1 the pre-release part of the first version number
 * @param str2 the pre-release part of the second version number
 * @return a negative integer if the pre-release part of str1 is less than that of str2,
 *         a positive integer if the pre-release part of str1 is greater than that of str2,
 *         or zero if they are equal.
 */
private fun comparePreReleaseVersions(str1: String, str2: String): Int {
    val arr1 = str1.split(".")
    val arr2 = str2.split(".")
    val size = maxOf(arr1.size, arr2.size)

    for (i in 0 until size) {
        val num1 = if (i < arr1.size) arr1[i] else ""
        val num2 = if (i < arr2.size) arr2[i] else ""
        val num1Int = if (num1.all { it.isDigit() }) num1.toInt() else -1
        val num2Int = if (num2.all { it.isDigit() }) num2.toInt() else -1

        if (num1Int != -1 && num2Int != -1) {
            if (num1Int < num2Int) {
                return -1
            } else if (num1Int > num2Int) {
                return 1
            }
        } else {
            val result = num1.compareTo(num2)
            if (result != 0) {
                return result
            }
        }
    }
    return 0
}
