package dev.nyon.konfig.config

import dev.nyon.konfig.internal.InternalKonfigApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Path
import kotlin.io.path.*

@InternalKonfigApi
var configFiles: MutableList<ConfigFile<*>> = mutableListOf()

/**
 * This is the function that defines the base properties for config serialization.
 *
 * @param path the path, the config file should hold, e.g. '~/.minecraft/config/autodrop/autodrop.json'
 * @param currentVersion the up-to-date config version
 * @param defaultConfig the default config instance for initial config creation
 * @param jsonBuilder the consumer used to configure the json serializer
 * @param migration the consumer used to migrate configs to newer versions
 */
inline fun <reified T : Any> config(
    path: Path,
    currentVersion: Int,
    defaultConfig: T,
    crossinline jsonBuilder: JsonBuilder.() -> Unit = {},
    noinline migration: Migration<T>
) {
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        jsonBuilder()
    }
    configFiles.add(ConfigFile(T::class, ConfigSettings(path, currentVersion, migration), defaultConfig, json))
}

/**
 * This is the function that loads the config from disk and applies a migration if necessary.
 *
 * @return the encoded config or null if the configSettings, which are defined with [config], are null
 */
@Suppress("unused", "unchecked_cast")
inline fun <reified T : Any> loadConfig(): @Serializable T? {
    val file = configFiles.find { it.type == T::class } as? ConfigFile<T>
    if (file == null) return null

    val json = file.json
    val defaultInstance = file.defaultInstance
    val path = file.settings.path

    if (path.notExists()) {
        path.parent.createDirectories()
        path.createFile()
        saveConfig<T>(defaultInstance)
        return defaultInstance
    }
    val text = path.readText()
    try {
        return json.decodeFromString<Konfig<T>>(text).config
    } catch (e: Throwable) {
        val jsonTree = json.parseToJsonElement(text)
        val version = jsonTree.jsonObject["version"]?.jsonPrimitive?.content?.toIntOrNull()
        if (version == file.settings.currentVersion) {
            saveConfig(defaultInstance)
            return defaultInstance
        }
        val config = file.settings.migration.invoke(
            if (version == null) jsonTree
            else jsonTree.jsonObject["config"] ?: jsonTree, version
        ) as? T
        saveConfig(config ?: defaultInstance)
        return config
    }
}

/**
 * This is the function that saves the config to disk.
 * If the configSettings are not applied via the [config] function the config does not save.
 *
 * @param config the config
 */
@Suppress("unchecked_cast")
inline fun <reified T : Any> saveConfig(config: @Serializable T) {
    val file = configFiles.find { it.type == T::class } as? ConfigFile<T>
    if (file == null) return
    val path = file.settings.path

    path.writeText(file.json.encodeToString(Konfig(file.settings.currentVersion, config)))
}

@InternalKonfigApi
@Suppress("SpellCheckingInspection")
@Serializable
data class Konfig<T>(val version: Int, val config: @Serializable T)