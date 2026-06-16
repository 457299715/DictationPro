// network/WordBankApi.kt
package com.example.localdictationpro.network

import com.example.localdictationpro.network.models.*
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WordBankApi {

    @GET("api/v1/wordbanks/categories")
    suspend fun getCategories(): ApiResponse<List<CategoryResponse>>

    @GET("api/v1/wordbanks/list")
    suspend fun getWordBankList(
        @Query("category_id") categoryId: Long? = null,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("keyword") keyword: String? = null,
        @Query("sort") sort: String? = null
    ): ApiResponse<WordBankListResponse>

    @GET("api/v1/wordbanks/{id}")
    suspend fun getWordBankDetail(@Path("id") id: Long): ApiResponse<WordBankDetail>

    @GET("api/v1/wordbanks/{id}/check-update")
    suspend fun checkUpdate(
        @Path("id") id: Long,
        @Query("local_version") localVersion: Int
    ): ApiResponse<UpdateCheckResponse>

    @POST("api/v1/wordbanks/{id}/download")
    suspend fun reportDownload(
        @Path("id") id: Long,
        @Query("client_version") clientVersion: String = "1.0.0"
    ): ApiResponse<Unit>
}