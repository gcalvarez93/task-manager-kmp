package com.castrodev.task_manager_kmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform