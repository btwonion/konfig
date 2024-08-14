package dev.nyon.konfig.internal

@Retention(value = AnnotationRetention.BINARY)
@Target(AnnotationTarget.TYPEALIAS, AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
@RequiresOptIn(
    message = "This is an internal Konfig API that " + "should not be used from the outside. No compatibility guarantees are provided. " + "It is recommended to report your use-case of internal API to the konfig developer.",
    level = RequiresOptIn.Level.ERROR
)
@Suppress("SpellCheckingInspection")
annotation class InternalKonfigApi
