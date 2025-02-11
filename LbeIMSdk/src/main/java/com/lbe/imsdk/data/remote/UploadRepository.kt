package com.lbe.imsdk.data.remote

import com.lbe.imsdk.model.req.CompleteMultiPartUploadReq
import com.lbe.imsdk.model.req.InitMultiPartUploadBody
import com.lbe.imsdk.model.resp.CompleteMultiPartUploadRep
import com.lbe.imsdk.model.resp.InitMultiPartUploadRep
import com.lbe.imsdk.model.resp.SingleUploadRep
import com.lbe.imsdk.service.RetrofitInstance
import okhttp3.MultipartBody
import okhttp3.RequestBody

object UploadRepository {
    private val uploadService = RetrofitInstance.uploadService

    suspend fun singleUpload(
        file: MultipartBody.Part,
        signType: Int = 1,
        token: String,
    ): SingleUploadRep {
        return uploadService.singleUpload(file = file, signType = signType, token = token)
    }

    suspend fun initMultiPartUpload(
        body: InitMultiPartUploadBody,
        token: String,
    ): InitMultiPartUploadRep {
        return uploadService.initMultiPartUpload(body = body, token = token)
    }

    suspend fun uploadBinary(url: String, body: RequestBody) {
        uploadService.uploadBinary(url = url, requestBody = body)
    }

    suspend fun completeMultiPartUpload(
        body: CompleteMultiPartUploadReq,
        token: String,
    ): CompleteMultiPartUploadRep {
        return uploadService.completeMultiPartUpload(body = body, token = token)
    }
}