package com.example.saktonato.navigation

import androidx.compose.runtime.mutableStateListOf

class Nav(start: String) {
    val stack = mutableStateListOf(start)
    val route: String get() = stack.last()
    fun go(r: String) { stack.add(r) }
    fun tab(r: String) { stack.clear(); stack.add(r) }
    fun back() { if (stack.size > 1) stack.removeAt(stack.lastIndex) }
}
