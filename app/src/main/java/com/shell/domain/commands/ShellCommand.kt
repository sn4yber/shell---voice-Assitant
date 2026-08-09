package com.shell.app.domain.commands

import com.shell.app.domain.music.MusicCommand

sealed interface ShellCommand {
    data object TellTime : ShellCommand
    data class CallContact(val query: String) : ShellCommand
    data class Music(val command: MusicCommand) : ShellCommand
    data object Unknown : ShellCommand
}
