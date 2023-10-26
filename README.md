# konfig

> Library for saving, loading and migrating configs and, in the future, useful extensions and helpers for Minecaft
modding in Fabric environments.

## Implementing
This implementation uses utility of the fabric-loom gradle plugin, so make sure you have it added to your build.
Keep in mind to replace `{version}` with the suitable version for you. It can be found on the releases tab.

##### `build.gradle.kts`
```kotlin
repositories {
    maven("https://repo.nyon.dev/releases")
}

dependencies {
    include("dev.nyon:konfig:{version}")
}
```

### Other
⚠️ The development version is always the latest stable release of Minecraft.
Therefore, new features will only be available for the current and following Minecraft versions.

If you need help with any of my mods, just join my [discord server](https://nyon.dev/discord)