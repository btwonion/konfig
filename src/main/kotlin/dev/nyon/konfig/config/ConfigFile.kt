package dev.nyon.konfig.config

import dev.nyon.konfig.internal.InternalKonfigApi
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

@InternalKonfigApi
data class ConfigFile<T : Any>(val type: KClass<T>, val settings: ConfigSettings, val defaultInstance: T, val json: Json)
