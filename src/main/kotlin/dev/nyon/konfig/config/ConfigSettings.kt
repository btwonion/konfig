package dev.nyon.konfig.config

import dev.nyon.konfig.internal.InternalKonfigApi
import java.nio.file.Path

@InternalKonfigApi
data class ConfigSettings(
    val path: Path,
    val currentVersion: Int,
    val migration: Migration<*>
)
