// Path: androidApp/src/test/kotlin/com/castrodev/task_manager_kmp/ui/testing/MainDispatcherExtension.kt
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.castrodev.task_manager_kmp.ui.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Sustituye `Dispatchers.Main` por un dispatcher de test, porque `viewModelScope` lo usa
 * y en un test JVM no existe el hilo principal de Android. Con [UnconfinedTestDispatcher]
 * las coroutinas se ejecutan al instante, asi que el estado esta listo justo despues de cada accion.
 */
class MainDispatcherExtension(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : BeforeEachCallback, AfterEachCallback {

    override fun beforeEach(context: ExtensionContext) {
        Dispatchers.setMain(dispatcher)
    }

    override fun afterEach(context: ExtensionContext) {
        Dispatchers.resetMain()
    }
}