package dev.nyon.konfig.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Consumer for migrating old configs to the current config file
 *
 * @param jsonTree the config encoded as a JsonElement
 * @param version the saved config's version. if Int is null the config has the old config system (will be removed in Minecraft version 1.21)
 * @param T the encoded config class or null if the default instance should be applied
 */
typealias Migration<T> = (jsonTree: JsonElement, version: Int?) -> @Serializable T?
