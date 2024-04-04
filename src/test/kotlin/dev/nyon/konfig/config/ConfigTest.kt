package dev.nyon.konfig.config

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConfigTest {
    private val testPath = Path("run/config/test.json")

    @Serializable
    data class ConfigClass(val integer: Int = 4, val string: String = "some cool string")

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun internalConfigurationFilesShouldEqualProvided() {
        val configSettings =
            ConfigSettings(
                testPath,
                1,
                { _: JsonElement, _: Int? ->
                    ConfigClass()
                } as Migration<ConfigClass>
            )

        val testFile =
            ConfigFile(
                ConfigClass::class,
                configSettings,
                ConfigClass(),
                Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    allowTrailingComma = true
                }
            )

        config(configSettings.path, configSettings.currentVersion, ConfigClass(), { allowTrailingComma = true }) { _, _ ->
            null
        }

        val addedConfig = configFiles.first()
        assertEquals(testFile.type, addedConfig.type)
        assertEquals(testFile.defaultInstance, addedConfig.defaultInstance)

        assertEquals(testFile.settings.path, addedConfig.settings.path)
        assertEquals(testFile.settings.currentVersion, addedConfig.settings.currentVersion)

        configFiles.clear()
    }

    @Test
    fun newlyLoadedConfigShouldEqualDefault() {
        val default = ConfigClass()
        config(testPath, 1, default) { _, _ -> null }
        val config = loadConfig<ConfigClass>()

        assertEquals(default, config)

        testPath.deleteIfExists()
        configFiles.clear()
    }

    @Test
    fun secondLoadedConfigShouldEqualDefault() {
        val default = ConfigClass()
        config(testPath, 1, default) { _, _ -> null }
        loadConfig<ConfigClass>()
        val secondLoad = loadConfig<ConfigClass>()

        assertEquals(default, secondLoad)

        testPath.deleteIfExists()
        configFiles.clear()
    }

    @Test
    fun savedKonfigTextShouldEqualExpected() {
        val default = ConfigClass()
        config(testPath, 1, default) { _, _ -> null }
        saveConfig(default)

        val expectedKonfig = Konfig(1, default)
        val loadedKonfig = configFiles.first().json.decodeFromString<Konfig<ConfigClass>>(testPath.readText())

        assertEquals(expectedKonfig, loadedKonfig)

        testPath.deleteIfExists()
        configFiles.clear()
    }

    @Test
    fun loadWithUnsetProviderShouldThrowException() {
        assertFailsWith<IllegalArgumentException> {
            loadConfig()
        }
    }

    @Test
    fun saveWithUnsetProviderShouldThrowException() {
        assertFailsWith<IllegalArgumentException> {
            saveConfig(ConfigClass())
        }
    }

    @Test
    fun wrongConfigFileShouldCreateDefault() {
        val default = ConfigClass()
        config(testPath, 1, default) { _, _ -> null }
        testPath.writeText(Json.encodeToString(mapOf("okay" to "test1", "sollte halt nichs bringen" to "ok")))
        val loaded = loadConfig<ConfigClass>()
        assertEquals(default, loaded)

        testPath.deleteIfExists()
        configFiles.clear()
    }

    @Test
    fun migrationShouldWorkWithoutVersion() {
        val old: Map<String, JsonElement> = mapOf("int" to JsonPrimitive(78), "old_string" to JsonPrimitive("ich bin alt"))
        val expectedConfig = ConfigClass(78, "ich bin alt")
        testPath.writeText(Json.encodeToString(old))

        config(testPath, 1, ConfigClass()) { element, version ->
            val jsonObject = element.jsonObject
            return@config if (version == null) {
                ConfigClass(jsonObject["int"]!!.jsonPrimitive.int, jsonObject["old_string"]!!.jsonPrimitive.content)
            } else {
                null
            }
        }

        val loaded = loadConfig<ConfigClass>()
        assertEquals(expectedConfig, loaded)

        testPath.deleteIfExists()
        configFiles.clear()
    }

    @Test
    fun migrationShouldWorkWithVersion() {
        val old: Map<String, JsonElement> =
            mapOf(
                "version" to JsonPrimitive(2),
                "config" to JsonObject(mapOf("int" to JsonPrimitive(78), "old_string" to JsonPrimitive("ich bin alt")))
            )
        val expectedConfig = ConfigClass(78, "ich bin alt")
        testPath.writeText(Json.encodeToString(old))

        config(testPath, 3, ConfigClass()) { element, version ->
            val jsonObject = element.jsonObject
            return@config if (version == 2) {
                ConfigClass(jsonObject["int"]!!.jsonPrimitive.int, jsonObject["old_string"]!!.jsonPrimitive.content)
            } else {
                null
            }
        }

        val loaded = loadConfig<ConfigClass>()
        assertEquals(expectedConfig, loaded)

        testPath.deleteIfExists()
        configFiles.clear()
    }

    @Test
    fun createNewDefaultConfigWhenVersionIsUpToDate() {
        val corrupted: Map<String, JsonElement> = mapOf("version" to JsonPrimitive(1))
        val default = ConfigClass()
        testPath.writeText(Json.encodeToString(corrupted))

        config(testPath, 1, default) { _, _ -> null }

        val loaded = loadConfig<ConfigClass>()
        assertEquals(default, loaded)

        testPath.deleteIfExists()
        configFiles.clear()
    }
}
