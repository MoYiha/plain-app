package com.ismartcoding.plain.ui.page.root


enum class RootTabType(val value: Int) {
    HOME(0),
    IMAGES(1),
    AUDIO(2),
    VIDEOS(3);

    companion object {
        fun fromValue(value: Int): RootTabType {
            return entries.find { it.value == value } ?: HOME
        }
    }
}