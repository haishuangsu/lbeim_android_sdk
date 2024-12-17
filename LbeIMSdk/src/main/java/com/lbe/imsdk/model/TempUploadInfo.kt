package com.lbe.imsdk.model

import com.lbe.imsdk.model.req.MsgBody

data class TempUploadInfo(
    var sendBody: MsgBody, var mediaMessage: MediaMessage
)