import dev.nyon.konfig.config.config
import dev.nyon.konfig.config.loadConfig
import dev.nyon.konfig.config.saveConfig
import kotlinx.serialization.Serializable
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class Test {
    @Serializable
    data class TestClass(val jo: String = "asdadwda5", val intt: Int = 1)

    @Test
    fun test() {
        val testConfig = TestClass()

        config(Path("ok.json"), 1, testConfig) { _, _ -> null }
        saveConfig(testConfig)
        val loaded = loadConfig<TestClass>()

        assertEquals(testConfig, loaded)
    }
}