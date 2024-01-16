package dev.nyon.konfig.config

import dev.nyon.konfig.internal.InternalKonfigApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Path
import kotlin.io.path.*

@InternalKonfigApi
var configSettings: ConfigSettings? = null

@InternalKonfigApi
var defaultInstance: @Serializable Any? = null

@InternalKonfigApi
val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/**
 * This is the function that defines the base properties for config serialization.
 *
 * @param path the path, the config file should hold, e.g. '~/.minecraft/config/autodrop/autodrop.json'
 * @param currentVersion the up-to-date config version
 * @param defaultConfig the default config instance for initial config creation
 * @param migration the consumer used to migrate configs to newer versions
 */
fun <T> config(
    path: Path,
    currentVersion: Int,
    defaultConfig: T,
    migration: Migration<T>
) {
    configSettings = ConfigSettings(path, currentVersion, migration)
    defaultInstance = defaultConfig
}

/**
 * This is the function that loads the config from disk and applies a migration if necessary.
 *
 * @return the encoded config or null if the configSettings, which are defined with [config], are null
 */
@Suppress("unused")
inline fun <reified T> loadConfig(): @Serializable T? {
    if (configSettings == null) return null
    val path = configSettings!!.path
    if (path.notExists()) {
        path.parent.createDirectories()
        path.createFile()
        saveConfig(defaultInstance as T)
        return defaultInstance as T
    }
    val text = path.readText()
    try {
        return json.decodeFromString<Konfig<T>>(text).config
    } catch (e: Throwable) {
        val jsonTree = json.parseToJsonElement(text)
        val version = jsonTree.jsonObject["version"]?.jsonPrimitive?.content?.toIntOrNull()
        if (version == configSettings!!.currentVersion) {
            saveConfig(defaultInstance as T)
            return defaultInstance as T
        }
        val config = configSettings!!.migration.invoke(
            if (version == null) jsonTree
            else jsonTree.jsonObject["config"] ?: jsonTree, version
        ) as? T
        saveConfig(config ?: defaultInstance as T)
        return config
    }
}

/**
 * This is the function that saves the config to disk.
 * If the configSettings are not applied via the [config] function the config does not save.
 *
 * @param config the config
 */
inline fun <reified T> saveConfig(config: @Serializable T) {
    if (configSettings == null) return
    val path = configSettings!!.path

    path.writeText(json.encodeToString(Konfig(configSettings!!.currentVersion, config)))
}

@InternalKonfigApi
@Suppress("SpellCheckingInspection")
@Serializable
data class Konfig<T>(val version: Int, val config: @Serializable T)