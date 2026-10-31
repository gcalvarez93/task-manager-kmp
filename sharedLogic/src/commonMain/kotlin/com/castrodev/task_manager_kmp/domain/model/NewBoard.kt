// Path: sharedLogic/src/commonMain/kotlin/com/castrodev/task_manager_kmp/domain/model/NewBoard.kt
@file:OptIn(ExperimentalObjCName::class)

package com.castrodev.task_manager_kmp.domain.model

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/** Datos necesarios para crear un tablero. */
data class NewBoard(
    val name: String,
    // En Swift `description` choca con NSObject.description; se expone como `descriptionText`.
    @property:ObjCName("descriptionText")
    val description: String = "",
    val color: String,
)