// Generated by the protocol buffer compiler. DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: msggateway.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")

import com.lbe.imsdk.model.proto.IMMsg

@kotlin.jvm.JvmName("-initializesessionBasic")
public inline fun sessionBasic(block: SessionBasicKt.Dsl.() -> kotlin.Unit): IMMsg.SessionBasic =
  SessionBasicKt.Dsl._create(IMMsg.SessionBasic.newBuilder()).apply { block() }._build()
/**
 * Protobuf type `SessionBasic`
 */
public object SessionBasicKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: IMMsg.SessionBasic.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
      internal fun _create(builder: IMMsg.SessionBasic.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
  @kotlin.PublishedApi
    internal fun _build(): IMMsg.SessionBasic = _builder.build()

    /**
     * ```
     * 会话ID
     * ```
     *
     * `string sessionId = 1;`
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
     * 会话ID
     * ```
     *
     * `string sessionId = 1;`
     */
    public fun clearSessionId() {
      _builder.clearSessionId()
    }

    /**
     * ```
     * 头标ICon
     * ```
     *
     * `string headIcon = 2;`
     */
    public var headIcon: kotlin.String
      @JvmName("getHeadIcon")
      get() = _builder.headIcon
      @JvmName("setHeadIcon")
      set(value) {
        _builder.headIcon = value
      }
    /**
     * ```
     * 头标ICon
     * ```
     *
     * `string headIcon = 2;`
     */
    public fun clearHeadIcon() {
      _builder.clearHeadIcon()
    }

    /**
     * ```
     * uid
     * ```
     *
     * `string uid = 3;`
     */
    public var uid: kotlin.String
      @JvmName("getUid")
      get() = _builder.uid
      @JvmName("setUid")
      set(value) {
        _builder.uid = value
      }
    /**
     * ```
     * uid
     * ```
     *
     * `string uid = 3;`
     */
    public fun clearUid() {
      _builder.clearUid()
    }

    /**
     * ```
     * 来源APP
     * ```
     *
     * `string source = 4;`
     */
    public var source: kotlin.String
      @JvmName("getSource")
      get() = _builder.source
      @JvmName("setSource")
      set(value) {
        _builder.source = value
      }
    /**
     * ```
     * 来源APP
     * ```
     *
     * `string source = 4;`
     */
    public fun clearSource() {
      _builder.clearSource()
    }

    /**
     * ```
     * 简称
     * ```
     *
     * `string nickName = 5;`
     */
    public var nickName: kotlin.String
      @JvmName("getNickName")
      get() = _builder.nickName
      @JvmName("setNickName")
      set(value) {
        _builder.nickName = value
      }
    /**
     * ```
     * 简称
     * ```
     *
     * `string nickName = 5;`
     */
    public fun clearNickName() {
      _builder.clearNickName()
    }

    /**
     * ```
     * 设备号
     * ```
     *
     * `string devNo = 6;`
     */
    public var devNo: kotlin.String
      @JvmName("getDevNo")
      get() = _builder.devNo
      @JvmName("setDevNo")
      set(value) {
        _builder.devNo = value
      }
    /**
     * ```
     * 设备号
     * ```
     *
     * `string devNo = 6;`
     */
    public fun clearDevNo() {
      _builder.clearDevNo()
    }

    /**
     * ```
     * 创建时间
     * ```
     *
     * `int64 createTime = 7;`
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
     * 创建时间
     * ```
     *
     * `int64 createTime = 7;`
     */
    public fun clearCreateTime() {
      _builder.clearCreateTime()
    }

    /**
     * ```
     * 接入语言
     * ```
     *
     * `string language = 8;`
     */
    public var language: kotlin.String
      @JvmName("getLanguage")
      get() = _builder.language
      @JvmName("setLanguage")
      set(value) {
        _builder.language = value
      }
    /**
     * ```
     * 接入语言
     * ```
     *
     * `string language = 8;`
     */
    public fun clearLanguage() {
      _builder.clearLanguage()
    }

    /**
     * ```
     * 额外保留信息
     * ```
     *
     * `string extra = 9;`
     */
    public var extra: kotlin.String
      @JvmName("getExtra")
      get() = _builder.extra
      @JvmName("setExtra")
      set(value) {
        _builder.extra = value
      }
    /**
     * ```
     * 额外保留信息
     * ```
     *
     * `string extra = 9;`
     */
    public fun clearExtra() {
      _builder.clearExtra()
    }

    /**
     * ```
     * 最新消息
     * ```
     *
     * `.MsgBody latestMsg = 10;`
     */
    public var latestMsg: IMMsg.MsgBody
      @JvmName("getLatestMsg")
      get() = _builder.latestMsg
      @JvmName("setLatestMsg")
      set(value) {
        _builder.latestMsg = value
      }
    /**
     * ```
     * 最新消息
     * ```
     *
     * `.MsgBody latestMsg = 10;`
     */
    public fun clearLatestMsg() {
      _builder.clearLatestMsg()
    }
    /**
     * ```
     * 最新消息
     * ```
     *
     * `.MsgBody latestMsg = 10;`
     * @return Whether the latestMsg field is set.
     */
    public fun hasLatestMsg(): kotlin.Boolean {
      return _builder.hasLatestMsg()
    }

    public val SessionBasicKt.Dsl.latestMsgOrNull: IMMsg.MsgBody?
      get() = _builder.latestMsgOrNull
  }
}
@kotlin.jvm.JvmSynthetic
public inline fun IMMsg.SessionBasic.copy(block: SessionBasicKt.Dsl.() -> kotlin.Unit): IMMsg.SessionBasic =
  SessionBasicKt.Dsl._create(this.toBuilder()).apply { block() }._build()

public val IMMsg.SessionBasicOrBuilder.latestMsgOrNull: IMMsg.MsgBody?
  get() = if (hasLatestMsg()) getLatestMsg() else null

