package com.waytofit.global.common

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.ContextualDeserializer

class PatchDeserializer<T>(private val valueType: JavaType? = null) : JsonDeserializer<Patch<T>>(), ContextualDeserializer {

    @Suppress("UNCHECKED_CAST")
    override fun getNullValue(ctxt: DeserializationContext): Patch<T> = Patch.Set(null)

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Patch<T> {
        val value = valueType?.let { ctxt.readValue<Any>(p, it) } as T?
        return Patch.Set(value)
    }

    override fun createContextual(ctxt: DeserializationContext, property: BeanProperty?): JsonDeserializer<*> {
        val containedType = property?.type?.containedType(0) ?: return PatchDeserializer<Any>()
        return PatchDeserializer<Any>(containedType)
    }
}
