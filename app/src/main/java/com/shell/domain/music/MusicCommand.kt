package com.shell.app.domain.music

sealed interface MusicCommand {
    data object Play : MusicCommand
    data object Pause : MusicCommand
    data object Next : MusicCommand
    data object Previous : MusicCommand
    data object VolumeUp : MusicCommand
    data object VolumeDown : MusicCommand
    data class Search(val query: String) : MusicCommand
    data object Unknown : MusicCommand
}
