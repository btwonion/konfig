package dev.nyon.konfig.config

import dev.nyon.konfig.internal.InternalKonfigApi
import kotlinx.serialization.Serializable

@InternalKonfigApi
@Suppress("SpellCheckingInspection")
@Serializable
data class Konfig<T>(val version: Int, val config: @Serializable T)
