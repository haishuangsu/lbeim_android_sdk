package info.hermiths.chatapp.data.remote

import info.hermiths.chatapp.model.req.CompleteMultiPartUploadReq
import info.hermiths.chatapp.model.req.InitMultiPartUploadBody
import info.hermiths.chatapp.model.resp.CompleteMultiPartUploadRep
import info.hermiths.chatapp.model.resp.InitMultiPartUploadRep
import info.hermiths.chatapp.service.RetrofitInstance
import okhttp3.RequestBody

object UploadRepository {
    val uploadService = RetrofitInstance.uploadService

    suspend fun initMultiPartUpload(
        url: String, body: InitMultiPartUploadBody
    ): InitMultiPartUploadRep {
        return uploadService.initMultiPartUpload(url = url, body = body)
    }

    suspend fun uploadBinary(url: String, body: RequestBody) {
        uploadService.uploadBinary(url = url, requestBody = body)
    }

    suspend fun completeMultiPartUpload(
        url: String, body: CompleteMultiPartUploadReq
    ): CompleteMultiPartUploadRep {
        return uploadService.completeMultiPartUpload(url = url, body = body)
    }
}