package dev.nyon.konfig.config

import dev.nyon.konfig.internal.InternalKonfigApi

@InternalKonfigApi
data class ConfigSettings(
    val name: String, val currentVersion: Int, val migration: Migration<*>
)