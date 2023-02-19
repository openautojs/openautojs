package org.autojs.autojs.devplugin.message

import com.google.gson.annotations.SerializedName
import com.stardust.util.compareVersions

data class Message(
    @SerializedName("type")
    val type: String,
    @SerializedName("data")
    val data: Any?,
)

data class HelloResponse(
    @SerializedName("data")
    val data: String,
    @SerializedName("debug")
    val debug: Boolean,
    @SerializedName("message_id")
    val messageId: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("version")
    val version: String? = "0"
) {
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
    fun compareVersions() =
        version?.let { it1 -> compareVersions(it1, "1.109.0") } ?: -1

}

data class LogData(
    @SerializedName("log")
    val log: String
)

data class Hello(
    @SerializedName("device_name")
    val deviceName: String,
    @SerializedName("app_version")
    val appVersion: String,
    @SerializedName("app_version_code")
    val appVersionCode: Int,
    @SerializedName("client_version")
    val clientVersion: Int,
)