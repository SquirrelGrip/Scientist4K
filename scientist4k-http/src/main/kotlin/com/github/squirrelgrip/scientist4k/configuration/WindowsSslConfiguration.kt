package com.github.squirrelgrip.scientist4k.configuration

class WindowsSslConfiguration: SslConfiguration(
    keyStoreType = "Windows-MY",
    trustStoreType = "Windows-ROOT"
)