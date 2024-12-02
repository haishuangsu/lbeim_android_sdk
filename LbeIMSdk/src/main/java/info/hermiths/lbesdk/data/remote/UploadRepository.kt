package info.hermiths.lbesdk.data.remote

import info.hermiths.lbesdk.model.req.CompleteMultiPartUploadReq
import info.hermiths.lbesdk.model.req.InitMultiPartUploadBody
import info.hermiths.lbesdk.model.resp.CompleteMultiPartUploadRep
import info.hermiths.lbesdk.model.resp.InitMultiPartUploadRep
import info.hermiths.lbesdk.model.resp.SingleUploadRep
import info.hermiths.lbesdk.service.RetrofitInstance
import okhttp3.MultipartBody
import okhttp3.RequestBody

object UploadRepository {
    private val uploadService = RetrofitInstance.uploadService

    suspend fun singleUpload(file: MultipartBody.Part, signType: Int = 1): SingleUploadRep {
        return uploadService.singleUpload(file = file, signType = signType)
    }

    suspend fun initMultiPartUpload(
        body: InitMultiPartUploadBody
    ): InitMultiPartUploadRep {
        return uploadService.initMultiPartUpload(body = body)
    }

    suspend fun uploadBinary(url: String, body: RequestBody) {
        uploadService.uploadBinary(url = url, requestBody = body)
    }

    suspend fun completeMultiPartUpload(
        body: CompleteMultiPartUploadReq
    ): CompleteMultiPartUploadRep {
        return uploadService.completeMultiPartUpload(body = body)
    }
}