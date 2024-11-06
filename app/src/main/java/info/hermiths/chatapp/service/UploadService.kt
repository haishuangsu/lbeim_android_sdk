package info.hermiths.chatapp.service

import info.hermiths.chatapp.model.req.InitMultiPartUploadBody
import info.hermiths.chatapp.model.resp.InitMultiPartUploadRep
import okhttp3.RequestBody
import retrofit2.http.Body

import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url

interface UploadService {

    @POST
    suspend fun initMultiPartUpload(
        @Url url: String, @Body body: InitMultiPartUploadBody
    ): InitMultiPartUploadRep

    @PUT
    suspend fun uploadBinary(
        @Url url: String,
//        @Header("Authorization") auth: String,
//        @Header("X-Amz-Content-Sha256") auth: String,
//        @Header("X-Amz-Date") auth: String,
        @Body requestBody: RequestBody
    )
}