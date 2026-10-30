// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/data/mapper/InstantParsing.kt
package com.castrodev.task_manager_kmp.data.mapper

import kotlin.time.Instant

/** Convierte un texto ISO-8601 en [Instant], o devuelve `null` si no tiene un formato valido. */
internal fun parseInstantOrNull(value: String): Instant? =
    try {
        Instant.parse(value)
    } catch (e: IllegalArgumentException) {
        null
    }