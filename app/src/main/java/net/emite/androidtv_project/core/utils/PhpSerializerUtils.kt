package net.emite.androidtv_project.core.utils

/**
 * Utilidades para procesar datos serializados por PHP que se reciben desde el backend.
 */
object PhpSerializerUtils {
    /** 
     * Expresión regular para extraer valores de cadena de un array serializado de PHP.
     * Ejemplo: a:7:{i:0;s:1:"1";i:1;s:1:"2";} extrae ["1", "2"]
     */
    private val stringValueRegex = Regex("""s:\d+:"([^"]+)"""")

    /**
     * Parsea una cadena serializada de PHP que representa un array de strings.
     * 
     * @param serialized La cadena serializada recibida (ej: "a:2:{i:0;s:5:"Lunes";...}").
     * @return Lista de strings con los valores extraídos o lista vacía si el formato no es válido.
     */
    fun parsePhpStringArray(serialized: String?): List<String> {
        if (serialized.isNullOrBlank() || !serialized.startsWith("a:")) return emptyList()
        return stringValueRegex.findAll(serialized).map { it.groupValues[1] }.toList()
    }
}
