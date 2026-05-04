package net.emite.androidtv_project.core.utils

object PhpSerializerUtils {
    // Regex to extract string values from a PHP serialized array.
    // Example: a:7:{i:0;s:1:"1";i:1;s:1:"2";} extracts ["1", "2"]
    private val stringValueRegex = Regex("""s:\d+:"([^"]+)"""")

    fun parsePhpStringArray(serialized: String?): List<String> {
        if (serialized.isNullOrBlank() || !serialized.startsWith("a:")) return emptyList()
        return stringValueRegex.findAll(serialized).map { it.groupValues[1] }.toList()
    }
}
