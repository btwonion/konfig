# konfig

> Library for saving, loading and migrating configs and, in the future, useful extensions and helpers for Minecaft
modding in Fabric environments.

## Implementing
This implementation uses utility of the fabric-loom gradle plugin, so make sure you have it added to your build.
Keep in mind to replace `VERSION` with the suitable version for you. It can be found on the releases tab.

`build.gradle.kts`
```kotlin
repositories {
    maven("https://repo.nyon.dev/releases")
}

dependencies {
  include(modImplementation("dev.nyon:konfig:VERSION")!!)
}
```

### Simple Config

```kotlin
import net.fabricmc.api.ModInitializer
import dev.nyon.konfig.config.config
import dev.nyon.konfig.config.loadConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.fabricmc.loader.api.FabricLoader

object YourModInitializer : ModInitializer {
  var config: Config = Config()
  
  override fun onInitialize() {
    config(
      FabricLoader.getInstance().configDir.resolve("my_cool_config.json"), 
      1, 
      Config()
    ) { jsonElement: JsonElement, version: Int? ->
      return@config if (version == null) {
        val jsonObject = jsonElement.jsonObject
        Config(jsonObject["old_integer"]!!.jsonPrimitive.int)
      } else {
        null
      }
    }
    
    config = loadConfig()
  }
}

@Serializable
data class Config(
  val integer: Int = 5
)

```

On shutdown or save in a config screen you can call the `saveConfig` function to save the config to disk.

### Other
⚠️ The development version is always the latest stable release of Minecraft.
Therefore, new features will only be available for the current and following Minecraft versions.

If you need help with any of my mods, just join my [discord server](https://nyon.dev/discord)
