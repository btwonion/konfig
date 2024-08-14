package dev.nyon.konfig.config

import dev.nyon.konfig.internal.InternalKonfigApi
import kotlinx.serialization.Serializable

/**
 * The model of the json being stored.
 *
 * @param version the version of the config. Used for migration
 * @param config the config
 */
@InternalKonfigApi
@Suppress("SpellCheckingInspection")
@Serializable
data class Konfig<T>(val version: Int, val config: @Serializable T)
