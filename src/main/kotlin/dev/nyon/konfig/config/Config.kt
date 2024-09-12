package dev.nyon.konfig.config

import dev.nyon.konfig.internal.InternalKonfigApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

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
inline fun <reified T : @Serializable Any> config(
    path: Path,
    currentVersion: Int,
    defaultConfig: T,
    crossinline jsonBuilder: JsonBuilder.() -> Unit = {},
    noinline migration: Migration<T>
) {
    val json = Json {
        prettyPrint = true
        encodeDefaults = true
        jsonBuilder()
    }
    configFiles.add(ConfigFile(T::class, ConfigSettings(path, currentVersion, migration), defaultConfig, json))
}

/**
 * This is the function that loads the config from disk.
 * If the configSettings are not applied via the [config] function the config does not load.
 *
 * @return the config
 * @throws IllegalArgumentException if no file config for the class has been set
 */
@Suppress("unused", "unchecked_cast")
inline fun <reified T : @Serializable Any> loadConfig(): @Serializable T {
    val configFile = configFiles.find { it.type == T::class } as? ConfigFile<T>
    if (configFile == null) throw IllegalArgumentException("No config for class ${T::class.simpleName} found!")

    val json = configFile.json
    val defaultInstance = configFile.defaultInstance
    val path = configFile.settings.path

    if (path.configInstantiated(defaultInstance)) return defaultInstance

    val text = path.readText()
    return try {
        json.decodeFromString<Konfig<T>>(text).config
    } catch (e: Throwable) {
        handleException(text, configFile)
    }
}

/**
 * This is the function that saves the config to disk.
 * If the configSettings are not applied via the [config] function the config does not save.
 *
 * @param config the config to save
 * @throws IllegalArgumentException if no file config for the class has been set
 */
@Suppress("unchecked_cast")
inline fun <reified T : @Serializable Any> saveConfig(config: @Serializable T) {
    val file = configFiles.find { it.type == T::class } as? ConfigFile<T>
    if (file == null) throw IllegalArgumentException("No config for class ${T::class.simpleName} found!")
    val path = file.settings.path

    path.writeText(file.json.encodeToString(Konfig(file.settings.currentVersion, config)))
}

/**
 * Either instantiate the default config or return false if the config already existed.
 *
 * @param defaultInstance the default instance of the config
 * @return true if the config was instantiated or false if the config already existed
 */
@InternalKonfigApi
inline fun <reified T : @Serializable Any> Path.configInstantiated(defaultInstance: T): Boolean {
    if (exists()) return false
    parent.createDirectories()
    createFile()

    saveConfig<T>(defaultInstance)
    return true
}

/**
 * Handles migration or reset after faulty deserialization of the config.
 *
 * @param fileText the raw config text
 * @param configFile the [ConfigFile] holding all the required data for migration
 * @return the config after migration of reset of the config
 */
@InternalKonfigApi
inline fun <reified T : @Serializable Any> handleException(
    fileText: String, configFile: ConfigFile<T>
): @Serializable T {
    val jsonTree = runCatching { configFile.json.parseToJsonElement(fileText) }.getOrNull() ?: return resetConfig(configFile)
    val version = jsonTree.jsonObject["version"]?.jsonPrimitive?.intOrNull
    if (version == configFile.settings.currentVersion) return resetConfig(configFile)

    val config = configFile.settings.migration.invoke(
        if (version == null) {
            jsonTree
        } else {
            jsonTree.jsonObject["config"] ?: jsonTree
        }, version
    ) as? T
    saveConfig(config ?: configFile.defaultInstance)
    return config ?: configFile.defaultInstance
}

/**
 * Resets the config to the default instance.
 *
 * @param configFile the [ConfigFile] holding all the required data for migration
 * @return the default instance of the config
 */
@InternalKonfigApi
inline fun <reified T : @Serializable Any> resetConfig(configFile: ConfigFile<T>): T {
    saveConfig(configFile.defaultInstance)
    return configFile.defaultInstance
}
