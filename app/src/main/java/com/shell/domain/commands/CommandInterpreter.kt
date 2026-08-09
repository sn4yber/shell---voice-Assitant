package com.shell.app.domain.commands

import com.shell.app.domain.music.MusicCommandParser

object CommandInterpreter {
    fun interpret(input: String): ShellCommand {
        val normalized = input.trim().lowercase()
        val musicCommand = MusicCommandParser.parse(normalized)

        return when {
            normalized.startsWith("llama ") ||
                normalized.startsWith("llamar ") ||
                normalized.contains("llama a ") ||
                normalized.contains("llamar a ") -> ShellCommand.CallContact(extractCallQuery(normalized))
            musicCommand != com.shell.app.domain.music.MusicCommand.Unknown -> ShellCommand.Music(musicCommand)
            normalized.contains("que hora es") ||
                normalized.contains("qué hora es") ||
                normalized.contains("hora") -> ShellCommand.TellTime
            else -> ShellCommand.Unknown
        }
    }

    private fun extractCallQuery(normalizedInput: String): String {
        val candidates = listOf("llama a ", "llamar a ", "llama ", "llamar ")
        val match = candidates.firstOrNull { normalizedInput.contains(it) } ?: return normalizedInput
        return normalizedInput.substringAfter(match).trim()
    }
}
