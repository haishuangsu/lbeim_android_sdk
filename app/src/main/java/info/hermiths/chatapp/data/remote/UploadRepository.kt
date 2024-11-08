package info.hermiths.chatapp.data.remote

import info.hermiths.chatapp.model.req.CompleteMultiPartUploadReq
import info.hermiths.chatapp.model.req.InitMultiPartUploadBody
import info.hermiths.chatapp.model.resp.CompleteMultiPartUploadRep
import info.hermiths.chatapp.model.resp.InitMultiPartUploadRep
import info.hermiths.chatapp.model.resp.SingleUploadRep
import info.hermiths.chatapp.service.RetrofitInstance
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