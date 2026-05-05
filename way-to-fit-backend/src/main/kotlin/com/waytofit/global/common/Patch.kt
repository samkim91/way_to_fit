package com.waytofit.global.common

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize(using = PatchDeserializer::class)
sealed class Patch<out T> {
    object Absent : Patch<Nothing>()
    data class Set<out T>(val value: T?) : Patch<T>()
}

fun <T> Patch<T>.applyTo(current: T?): T? = when (this) {
    is Patch.Absent -> current
    is Patch.Set -> value
}
