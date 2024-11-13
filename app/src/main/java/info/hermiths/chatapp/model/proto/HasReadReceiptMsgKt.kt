// Generated by the protocol buffer compiler. DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: msggateway.proto

// Generated files should ignore deprecation warnings
@file:Suppress("DEPRECATION")

import info.hermiths.chatapp.model.proto.IMMsg

@kotlin.jvm.JvmName("-initializehasReadReceiptMsg")
public inline fun hasReadReceiptMsg(block: HasReadReceiptMsgKt.Dsl.() -> kotlin.Unit): IMMsg.HasReadReceiptMsg =
  HasReadReceiptMsgKt.Dsl._create(IMMsg.HasReadReceiptMsg.newBuilder()).apply { block() }._build()
/**
 * Protobuf type `HasReadReceiptMsg`
 */
public object HasReadReceiptMsgKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  public class Dsl private constructor(
    private val _builder: IMMsg.HasReadReceiptMsg.Builder
  ) {
    public companion object {
      @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
      internal fun _create(builder: IMMsg.HasReadReceiptMsg.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
  @kotlin.PublishedApi
    internal fun _build(): IMMsg.HasReadReceiptMsg = _builder.build()

    /**
     * `string sessionID = 1;`
     */
    public var sessionID: kotlin.String
      @JvmName("getSessionID")
      get() = _builder.sessionID
      @JvmName("setSessionID")
      set(value) {
        _builder.sessionID = value
      }
    /**
     * `string sessionID = 1;`
     */
    public fun clearSessionID() {
      _builder.clearSessionID()
    }

    /**
     * An uninstantiable, behaviorless type to represent the field in
     * generics.
     */
    @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
    public class HasReadSeqsProxy private constructor() : com.google.protobuf.kotlin.DslProxy()
    /**
     * `repeated int64 hasReadSeqs = 2;`
     */
     public val hasReadSeqs: com.google.protobuf.kotlin.DslList<kotlin.Long, HasReadSeqsProxy>
      @kotlin.jvm.JvmSynthetic
      get() = com.google.protobuf.kotlin.DslList(
        _builder.hasReadSeqsList
      )
    /**
     * `repeated int64 hasReadSeqs = 2;`
     * @param value The hasReadSeqs to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addHasReadSeqs")
    public fun com.google.protobuf.kotlin.DslList<kotlin.Long, HasReadSeqsProxy>.add(value: kotlin.Long) {
      _builder.addHasReadSeqs(value)
    }/**
     * `repeated int64 hasReadSeqs = 2;`
     * @param value The hasReadSeqs to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignHasReadSeqs")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<kotlin.Long, HasReadSeqsProxy>.plusAssign(value: kotlin.Long) {
      add(value)
    }/**
     * `repeated int64 hasReadSeqs = 2;`
     * @param values The hasReadSeqs to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addAllHasReadSeqs")
    public fun com.google.protobuf.kotlin.DslList<kotlin.Long, HasReadSeqsProxy>.addAll(values: kotlin.collections.Iterable<kotlin.Long>) {
      _builder.addAllHasReadSeqs(values)
    }/**
     * `repeated int64 hasReadSeqs = 2;`
     * @param values The hasReadSeqs to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignAllHasReadSeqs")
    @Suppress("NOTHING_TO_INLINE")
    public inline operator fun com.google.protobuf.kotlin.DslList<kotlin.Long, HasReadSeqsProxy>.plusAssign(values: kotlin.collections.Iterable<kotlin.Long>) {
      addAll(values)
    }/**
     * `repeated int64 hasReadSeqs = 2;`
     * @param index The index to set the value at.
     * @param value The hasReadSeqs to set.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("setHasReadSeqs")
    public operator fun com.google.protobuf.kotlin.DslList<kotlin.Long, HasReadSeqsProxy>.set(index: kotlin.Int, value: kotlin.Long) {
      _builder.setHasReadSeqs(index, value)
    }/**
     * `repeated int64 hasReadSeqs = 2;`
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("clearHasReadSeqs")
    public fun com.google.protobuf.kotlin.DslList<kotlin.Long, HasReadSeqsProxy>.clear() {
      _builder.clearHasReadSeqs()
    }}
}
@kotlin.jvm.JvmSynthetic
public inline fun IMMsg.HasReadReceiptMsg.copy(block: HasReadReceiptMsgKt.Dsl.() -> kotlin.Unit): IMMsg.HasReadReceiptMsg =
  HasReadReceiptMsgKt.Dsl._create(this.toBuilder()).apply { block() }._build()

