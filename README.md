# konfig

> Library for saving, loading and migrating configs

## Implementing
Replace `VERSION` with the latest available version.

`build.gradle.kts`
```kotlin
repositories {
    maven("https://repo.nyon.dev/releases")
}

dependencies {
  include(implementation("dev.nyon:konfig:VERSION")!!)
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
If you need help with any of my mods, just join my [discord server](https://nyon.dev/discord).
