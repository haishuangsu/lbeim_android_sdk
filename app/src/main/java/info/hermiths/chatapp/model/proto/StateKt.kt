// Generated by the protocol buffer compiler. DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: msggateway.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")

import info.hermiths.chatapp.model.proto.IMMsg

@kotlin.jvm.JvmName("-initializestate")
public inline fun state(block: StateKt.Dsl.() -> kotlin.Unit): IMMsg.State =
  StateKt.Dsl._create(IMMsg.State.newBuilder()).apply { block() }._build()
/**
 * Protobuf type `State`
 */
public object StateKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: IMMsg.State.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
      internal fun _create(builder: IMMsg.State.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
  @kotlin.PublishedApi
    internal fun _build(): IMMsg.State = _builder.build()

    /**
     * `int32 code = 1;`
     */
    public var code: kotlin.Int
      @JvmName("getCode")
      get() = _builder.code
      @JvmName("setCode")
      set(value) {
        _builder.code = value
      }
    /**
     * `int32 code = 1;`
     */
    public fun clearCode() {
      _builder.clearCode()
    }

    /**
     * `string message = 2;`
     */
    public var message: kotlin.String
      @JvmName("getMessage")
      get() = _builder.message
      @JvmName("setMessage")
      set(value) {
        _builder.message = value
      }
    /**
     * `string message = 2;`
     */
    public fun clearMessage() {
      _builder.clearMessage()
    }
  }
}
@kotlin.jvm.JvmSynthetic
public inline fun IMMsg.State.copy(block: StateKt.Dsl.() -> kotlin.Unit): IMMsg.State =
  StateKt.Dsl._create(this.toBuilder()).apply { block() }._build()

