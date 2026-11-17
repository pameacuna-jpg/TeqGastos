
package cl.teqmed.teqgastos.ui.fondorendir

data class FondoRendirUiState(

    // Datos generales
    val proyecto: String = "",
    val correlativo: String = "FXR",

    // Fondo individual asignado por Administración
    val fondoAsignado: Int = 0,
    val totalRendido: Int = 0,

    // Categoría seleccionada
    val categoriaSeleccionada: String = "",
    val errorCategoria: String? = null,

    // Topes pendientes de configuración
    val topeEstacionamiento: Int = 0,
    val topePeajes: Int = 0,
    val topeAlimentacion: Int = 0,
    val topeMateriales: Int = 0

) {
    val saldoDisponible: Int
        get() = fondoAsignado - totalRendido
}
