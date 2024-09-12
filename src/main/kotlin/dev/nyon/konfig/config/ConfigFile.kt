package dev.nyon.konfig.config

import dev.nyon.konfig.internal.InternalKonfigApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

/**
 * This is the data class that holds the config file properties.
 *
 * @param type the class type of the config
 * @param settings the settings for the config
 * @param defaultInstance the default instance of the config
 * @param json the json serializer
 */
@InternalKonfigApi
data class ConfigFile<T : @Serializable Any>(
    val type: KClass<T>, val settings: ConfigSettings, val defaultInstance: T, val json: Json
)
