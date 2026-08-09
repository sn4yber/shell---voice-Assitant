package com.shell.app.domain.music

object MusicCommandParser {
    fun parse(input: String): MusicCommand {
        val normalized = input.trim().lowercase()
        val searchQuery = extractSearchQuery(normalized)

        return when {
            normalized.contains("volumen") && (normalized.contains("sube") || normalized.contains("subir") || normalized.contains("arriba")) -> MusicCommand.VolumeUp
            normalized.contains("volumen") && (normalized.contains("baja") || normalized.contains("bajar") || normalized.contains("abajo")) -> MusicCommand.VolumeDown
            normalized.contains("siguiente") -> MusicCommand.Next
            normalized.contains("anterior") || normalized.contains("retrocede") || normalized.contains("atrás") -> MusicCommand.Previous
            normalized.contains("pausa") || normalized.contains("detén") || normalized.contains("detener") || normalized.contains("stop") -> MusicCommand.Pause
            searchQuery != null -> {
                if (searchQuery == "musica" || searchQuery == "música") {
                    MusicCommand.Play
                } else {
                    MusicCommand.Search(searchQuery)
                }
            }
            normalized.contains("play") || normalized.contains("pon música") || normalized.contains("pon musica") || normalized.contains("música") || normalized.contains("musica") -> MusicCommand.Play
            else -> MusicCommand.Unknown
        }
    }

    private fun extractSearchQuery(normalized: String): String? {
        val triggers = listOf("reproduce ", "pon ", "play ", "busca ", "busca la ", "busca el ")
        val trigger = triggers.firstOrNull { normalized.contains(it) } ?: return null
        val candidate = normalized.substringAfter(trigger).trim()
        return candidate.takeIf { it.isNotBlank() }
    }
}
