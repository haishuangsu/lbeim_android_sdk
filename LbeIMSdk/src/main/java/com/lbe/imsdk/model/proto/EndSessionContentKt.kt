// Generated by the protocol buffer compiler. DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: msggateway.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")

import com.lbe.imsdk.model.proto.IMMsg

@kotlin.jvm.JvmName("-initializeendSessionContent")
public inline fun endSessionContent(block: EndSessionContentKt.Dsl.() -> kotlin.Unit): IMMsg.EndSessionContent =
  EndSessionContentKt.Dsl._create(IMMsg.EndSessionContent.newBuilder()).apply { block() }._build()
/**
 * Protobuf type `EndSessionContent`
 */
public object EndSessionContentKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: IMMsg.EndSessionContent.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
      internal fun _create(builder: IMMsg.EndSessionContent.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
  @kotlin.PublishedApi
    internal fun _build(): IMMsg.EndSessionContent = _builder.build()

    /**
     * ```
     * 结束时间(毫秒)
     * ```
     *
     * `int64 endTime = 1;`
     */
    public var endTime: kotlin.Long
      @JvmName("getEndTime")
      get() = _builder.endTime
      @JvmName("setEndTime")
      set(value) {
        _builder.endTime = value
      }
    /**
     * ```
     * 结束时间(毫秒)
     * ```
     *
     * `int64 endTime = 1;`
     */
    public fun clearEndTime() {
      _builder.clearEndTime()
    }
  }
}
@kotlin.jvm.JvmSynthetic
public inline fun IMMsg.EndSessionContent.copy(block: EndSessionContentKt.Dsl.() -> kotlin.Unit): IMMsg.EndSessionContent =
  EndSessionContentKt.Dsl._create(this.toBuilder()).apply { block() }._build()

