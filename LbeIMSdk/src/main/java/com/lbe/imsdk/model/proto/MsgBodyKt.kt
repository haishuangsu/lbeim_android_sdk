// Generated by the protocol buffer compiler. DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: msggateway.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")

import com.lbe.imsdk.model.proto.IMMsg

@kotlin.jvm.JvmName("-initializemsgBody")
public inline fun msgBody(block: MsgBodyKt.Dsl.() -> kotlin.Unit): IMMsg.MsgBody =
  MsgBodyKt.Dsl._create(IMMsg.MsgBody.newBuilder()).apply { block() }._build()
/**
 * Protobuf type `MsgBody`
 */
public object MsgBodyKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: IMMsg.MsgBody.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
      internal fun _create(builder: IMMsg.MsgBody.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
  @kotlin.PublishedApi
    internal fun _build(): IMMsg.MsgBody = _builder.build()

    /**
     * ```
     * 发送者ID
     * ```
     *
     * `string senderUid = 1;`
     */
    public var senderUid: kotlin.String
      @JvmName("getSenderUid")
      get() = _builder.senderUid
      @JvmName("setSenderUid")
      set(value) {
        _builder.senderUid = value
      }
    /**
     * ```
     * 发送者ID
     * ```
     *
     * `string senderUid = 1;`
     */
    public fun clearSenderUid() {
      _builder.clearSenderUid()
    }

    /**
     * ```
     * 接收者ID
     * ```
     *
     * `string receiverUid = 2;`
     */
    public var receiverUid: kotlin.String
      @JvmName("getReceiverUid")
      get() = _builder.receiverUid
      @JvmName("setReceiverUid")
      set(value) {
        _builder.receiverUid = value
      }
    /**
     * ```
     * 接收者ID
     * ```
     *
     * `string receiverUid = 2;`
     */
    public fun clearReceiverUid() {
      _builder.clearReceiverUid()
    }

    /**
     * ```
     * 消息类型
     * ```
     *
     * `.ContentType msgType = 3;`
     */
    public var msgType: IMMsg.ContentType
      @JvmName("getMsgType")
      get() = _builder.msgType
      @JvmName("setMsgType")
      set(value) {
        _builder.msgType = value
      }
    public var msgTypeValue: kotlin.Int
      @JvmName("getMsgTypeValue")
      get() = _builder.msgTypeValue
      @JvmName("setMsgTypeValue")
      set(value) {
        _builder.msgTypeValue = value
      }
    /**
     * ```
     * 消息类型
     * ```
     *
     * `.ContentType msgType = 3;`
     */
    public fun clearMsgType() {
      _builder.clearMsgType()
    }

    /**
     * ```
     * 当前消息序列号
     * ```
     *
     * `int32 msgSeq = 4;`
     */
    public var msgSeq: kotlin.Int
      @JvmName("getMsgSeq")
      get() = _builder.msgSeq
      @JvmName("setMsgSeq")
      set(value) {
        _builder.msgSeq = value
      }
    /**
     * ```
     * 当前消息序列号
     * ```
     *
     * `int32 msgSeq = 4;`
     */
    public fun clearMsgSeq() {
      _builder.clearMsgSeq()
    }

    /**
     * ```
     * 消息解密Key
     * ```
     *
     * `string encKey = 5;`
     */
    public var encKey: kotlin.String
      @JvmName("getEncKey")
      get() = _builder.encKey
      @JvmName("setEncKey")
      set(value) {
        _builder.encKey = value
      }
    /**
     * ```
     * 消息解密Key
     * ```
     *
     * `string encKey = 5;`
     */
    public fun clearEncKey() {
      _builder.clearEncKey()
    }

    /**
     * ```
     * 消息体内容
     * ```
     *
     * `string msgBody = 6;`
     */
    public var msgBody: kotlin.String
      @JvmName("getMsgBody")
      get() = _builder.msgBody
      @JvmName("setMsgBody")
      set(value) {
        _builder.msgBody = value
      }
    /**
     * ```
     * 消息体内容
     * ```
     *
     * `string msgBody = 6;`
     */
    public fun clearMsgBody() {
      _builder.clearMsgBody()
    }

    /**
     * ```
     * 0-未读， 1-已读
     * ```
     *
     * `int32 status = 7;`
     */
    public var status: kotlin.Int
      @JvmName("getStatus")
      get() = _builder.status
      @JvmName("setStatus")
      set(value) {
        _builder.status = value
      }
    /**
     * ```
     * 0-未读， 1-已读
     * ```
     *
     * `int32 status = 7;`
     */
    public fun clearStatus() {
      _builder.clearStatus()
    }

    /**
     * ```
     * 消息创建时间
     * ```
     *
     * `int64 createTime = 8;`
     */
    public var createTime: kotlin.Long
      @JvmName("getCreateTime")
      get() = _builder.createTime
      @JvmName("setCreateTime")
      set(value) {
        _builder.createTime = value
      }
    /**
     * ```
     * 消息创建时间
     * ```
     *
     * `int64 createTime = 8;`
     */
    public fun clearCreateTime() {
      _builder.clearCreateTime()
    }

    /**
     * ```
     * 客户端自定义消息ID
     * ```
     *
     * `string clientMsgID = 9;`
     */
    public var clientMsgID: kotlin.String
      @JvmName("getClientMsgID")
      get() = _builder.clientMsgID
      @JvmName("setClientMsgID")
      set(value) {
        _builder.clientMsgID = value
      }
    /**
     * ```
     * 客户端自定义消息ID
     * ```
     *
     * `string clientMsgID = 9;`
     */
    public fun clearClientMsgID() {
      _builder.clearClientMsgID()
    }

    /**
     * ```
     * sessionId
     * ```
     *
     * `string sessionId = 10;`
     */
    public var sessionId: kotlin.String
      @JvmName("getSessionId")
      get() = _builder.sessionId
      @JvmName("setSessionId")
      set(value) {
        _builder.sessionId = value
      }
    /**
     * ```
     * sessionId
     * ```
     *
     * `string sessionId = 10;`
     */
    public fun clearSessionId() {
      _builder.clearSessionId()
    }

    /**
     * ```
     * 客户端创建的发送时间
     * ```
     *
     * `string sendTime = 11;`
     */
    public var sendTime: kotlin.String
      @JvmName("getSendTime")
      get() = _builder.sendTime
      @JvmName("setSendTime")
      set(value) {
        _builder.sendTime = value
      }
    /**
     * ```
     * 客户端创建的发送时间
     * ```
     *
     * `string sendTime = 11;`
     */
    public fun clearSendTime() {
      _builder.clearSendTime()
    }

    /**
     * ```
     * 标题
     * ```
     *
     * `string title = 12;`
     */
    public var title: kotlin.String
      @JvmName("getTitle")
      get() = _builder.title
      @JvmName("setTitle")
      set(value) {
        _builder.title = value
      }
    /**
     * ```
     * 标题
     * ```
     *
     * `string title = 12;`
     */
    public fun clearTitle() {
      _builder.clearTitle()
    }

    /**
     * ```
     * 发送人昵称
     * ```
     *
     * `string senderNickname = 13;`
     */
    public var senderNickname: kotlin.String
      @JvmName("getSenderNickname")
      get() = _builder.senderNickname
      @JvmName("setSenderNickname")
      set(value) {
        _builder.senderNickname = value
      }
    /**
     * ```
     * 发送人昵称
     * ```
     *
     * `string senderNickname = 13;`
     */
    public fun clearSenderNickname() {
      _builder.clearSenderNickname()
    }

    /**
     * ```
     * 发送人头像
     * ```
     *
     * `string senderFaceURL = 14;`
     */
    public var senderFaceURL: kotlin.String
      @JvmName("getSenderFaceURL")
      get() = _builder.senderFaceURL
      @JvmName("setSenderFaceURL")
      set(value) {
        _builder.senderFaceURL = value
      }
    /**
     * ```
     * 发送人头像
     * ```
     *
     * `string senderFaceURL = 14;`
     */
    public fun clearSenderFaceURL() {
      _builder.clearSenderFaceURL()
    }

    /**
     * ```
     * 服务端自定义消息ID
     * ```
     *
     * `string serverMsgID = 15;`
     */
    public var serverMsgID: kotlin.String
      @JvmName("getServerMsgID")
      get() = _builder.serverMsgID
      @JvmName("setServerMsgID")
      set(value) {
        _builder.serverMsgID = value
      }
    /**
     * ```
     * 服务端自定义消息ID
     * ```
     *
     * `string serverMsgID = 15;`
     */
    public fun clearServerMsgID() {
      _builder.clearServerMsgID()
    }
  }
}
@kotlin.jvm.JvmSynthetic
public inline fun IMMsg.MsgBody.copy(block: MsgBodyKt.Dsl.() -> kotlin.Unit): IMMsg.MsgBody =
  MsgBodyKt.Dsl._create(this.toBuilder()).apply { block() }._build()

