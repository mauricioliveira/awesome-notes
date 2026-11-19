package com.mauricioliveira.awesomenotes

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.mauricioliveira.awesomenotes.di.appModules
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

class KoinModulesTest {

    /** Fails if any definition asks for a type that no module provides. */
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `every dependency in the app graph can be resolved`() {
        module { includes(appModules) }.verify(
            extraTypes = listOf(
                // Provided by Android at runtime, not by our modules.
                Context::class,
                SavedStateHandle::class,
                // Arguments of HttpClient's constructor, which HttpClientFactory passes in itself.
                HttpClientEngine::class,
                HttpClientConfig::class,
            ),
        )
    }
}
