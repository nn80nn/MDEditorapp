package n.learn.mdeditorapp.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<MessageResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>

    @GET("documents")
    suspend fun getDocuments(@Header("Authorization") token: String): Response<List<RemoteDocument>>

    @GET("documents/{id}")
    suspend fun downloadDocument(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ResponseBody>

    @Multipart
    @POST("documents")
    suspend fun uploadDocument(
        @Header("Authorization") token: String,
        @Part("name") name: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<RemoteDocument>

    @DELETE("documents/{id}")
    suspend fun deleteDocument(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<MessageResponse>
}
