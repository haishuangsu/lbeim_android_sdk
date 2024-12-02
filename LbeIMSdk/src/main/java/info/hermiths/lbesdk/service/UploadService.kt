package info.hermiths.lbesdk.service

import info.hermiths.lbesdk.model.req.CompleteMultiPartUploadReq
import info.hermiths.lbesdk.model.req.InitMultiPartUploadBody
import info.hermiths.lbesdk.model.resp.CompleteMultiPartUploadRep
import info.hermiths.lbesdk.model.resp.InitMultiPartUploadRep
import info.hermiths.lbesdk.model.resp.SingleUploadRep
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Url

const val SINGLE_UPLOAD = "api/single/fileupload"
const val INIT_MULTI_PART_UPLOAD = "api/multi/initiate-multipart_upload"
const val COMPLETE_MULTI_PART_UPLOAD = "api/multi/complete-multipart-upload"


interface UploadService {

    @POST(SINGLE_UPLOAD)
    @Multipart
    suspend fun singleUpload(
        @Part file: MultipartBody.Part,
        @Part("sign_type") signType: Int,
    ): SingleUploadRep

    @POST(INIT_MULTI_PART_UPLOAD)
    suspend fun initMultiPartUpload(
        @Body body: InitMultiPartUploadBody
    ): InitMultiPartUploadRep

    @PUT
    suspend fun uploadBinary(
        @Url url: String,
//        @Header("Authorization") auth: String,
//        @Header("X-Amz-Content-Sha256") auth: String,
//        @Header("X-Amz-Date") auth: String,
        @Body requestBody: RequestBody
    )

    @POST(COMPLETE_MULTI_PART_UPLOAD)
    suspend fun completeMultiPartUpload(
        @Body body: CompleteMultiPartUploadReq
    ): CompleteMultiPartUploadRep
}