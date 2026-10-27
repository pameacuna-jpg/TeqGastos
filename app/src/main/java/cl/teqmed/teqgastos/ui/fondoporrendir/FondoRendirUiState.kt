package cl.teqmed.teqgastos.ui.fondorendir

data class FondoRendirUiState(

    // Datos generales del Fondo por Rendir
    val proyecto: String = "Proyecto 1001",
    val correlativo: String = "FXR N.º 0001",

    // Montos del fondo
    val fondoAsignado: Int = 250000,
    val totalRendido: Int = 0,

    // Categoría seleccionada
    val categoriaSeleccionada: String = "",
    val errorCategoria: String? = null,

    // Topes demostrativos por categoría
    val topeEstacionamiento: Int = 30000,
    val topePeajes: Int = 50000,
    val topeAlimentacion: Int = 50000,
    val topeMateriales: Int = 100000
) {

    val saldoDisponible: Int
        get() = fondoAsignado - totalRendido
}