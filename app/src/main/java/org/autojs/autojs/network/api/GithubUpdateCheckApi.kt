package org.autojs.autojs.network.api

import org.autojs.autojs.network.entity.GithubReleaseInfo
import org.autojs.autojs.network.entity.GithubReleaseInfoList
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface GithubUpdateCheckApi {
    @GET("/repos/openautojs/openautojs/releases/latest")
    @Headers("Cache-Control: no-cache")
    suspend fun getGithubLastReleaseInfo(): GithubReleaseInfo

    @GET("/repos/openautojs/openautojs/releases/tags/{tag}")
    @Headers("Cache-Control: no-cache")
    suspend fun getGithubLastReleaseInfo(@Path("tag") tag: String): Response<GithubReleaseInfo>

    @GET("/repos/openautojs/openautojs/releases")
    @Headers("Cache-Control: no-cache")
    suspend fun getGithubReleaseInfoList(): GithubReleaseInfoList
}
