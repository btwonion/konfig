package dev.nyon.konfig.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Consumer for migrating old configs to the current config file
 *
 * @param JsonElement the config encoded as a JsonElement
 * @param Int the saved config's version. if Int is null the config has the old config system (will be removed in Minecraft version 1.21)
 * @param T the encoded config class
 */
typealias Migration<T> = (JsonElement, Int?) -> @Serializable T