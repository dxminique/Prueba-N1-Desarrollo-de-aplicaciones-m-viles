import kotlinx.coroutines.delay

data class Calculo(
    val productos: List<Producto>,
    val subtotal: Int,
    val descuento: Int,
    val iva: Int,
    val total: Int
)




class GestorPedidos(private val ivaPorc: Double = 0.19) {


    private val historico = mutableListOf<Calculo>()


    fun registrar(calculo: Calculo) {
        historico += calculo
    }

    fun generarReporteTexto(): String {
        val totalPedidos = historico.size
        val ingresosBrutos = historico.sumOf { it.subtotal }
        val descuentoTotal = historico.sumOf { it.descuento }
        val ivaTotal = historico.sumOf { it.iva }
        val ingresosNetos = historico.sumOf { it.total }
        val ticketPromedio = if (totalPedidos > 0) ingresosNetos / totalPedidos else 0


        val topProductos = historico
            .flatMap { it.productos }
            .groupingBy { it.nombre }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(5)


        val ventasPorCategoria = historico
            .flatMap { it.productos }
            .groupingBy { it.categoria }
            .eachCount()


        val ingresosBrutosPorCategoria = historico
            .flatMap { it.productos }
            .groupBy { it.categoria }
            .mapValues { (_, prods) -> prods.sumOf { it.precioFinal() } }

        return buildString {
            appendLine("=== REPORTE DE VENTAS ===")
            appendLine("Total de pedidos: $totalPedidos")
            appendLine("Ingresos brutos: $ingresosBrutos")
            appendLine("Descuento total: $descuentoTotal")
            appendLine("IVA total:       $ivaTotal")
            appendLine("Ingresos netos:  $ingresosNetos")
            appendLine("Ticket promedio: $ticketPromedio")

            appendLine("\nTop productos:")
            topProductos.forEachIndexed { i, e -> appendLine("${i + 1}. ${e.key} — ${e.value} und.") }

            appendLine("\nVentas por categoría (unid.):")
            ventasPorCategoria.forEach { (cat, n) -> appendLine("$cat: $n und.") }

            appendLine("\nIngresos brutos por categoría:")
            ingresosBrutosPorCategoria.forEach { (cat, sum) -> appendLine("$cat: $sum") }
        }
    }


    fun catalogoInicial(): List<Producto> = listOf(
        ProductoComida("Hamburguesa Clásica", 5990, false, 8),
        ProductoComida("Salmón Premium", 12990, true, 12),
        ProductoBebida("Coca Cola", 1800, "MEDIANO", 3),
        ProductoBebida("Jugo Natural", 2800, "GRANDE", 4)
    )

    fun validarPedido(items: List<Producto>) {
        require(items.isNotEmpty()) { "El pedido está vacío" }
        require(items.size <= 10) { "Máximo 10 productos por pedido" }
    }

    fun calcularTotales(items: List<Producto>, tipoCliente: String): Calculo {
        val subtotal = items.sumOf { it.precioFinal() }
        val descuento = when {
            tipoCliente.equals("REGULAR", true) -> (subtotal * 0.05).toInt()
            tipoCliente.equals("VIP", true)      -> (subtotal * 0.10).toInt()
            tipoCliente.equals("PREMIUM", true)  -> (subtotal * 0.15).toInt()
            else -> 0
        }
        val base = subtotal - descuento
        val iva = (base * ivaPorc).toInt()
        val total = base + iva
        return Calculo(items, subtotal, descuento, iva, total)
    }

    suspend fun procesarPedidoAsync(
        items: List<Producto>,
        onEstado: (EstadoPedido) -> Unit = {}
    ): EstadoPedido {
        return try {
            validarPedido(items)
            onEstado(EstadoPedido.Pendiente);    delay(500)
            onEstado(EstadoPedido.EnPreparacion); delay(items.sumOf { it.tiempoPrepSeg } * 1000L)
            onEstado(EstadoPedido.Listo)
            EstadoPedido.Listo
        } catch (e: Exception) {
            val err = EstadoPedido.Error(e.message ?: "Error desconocido")
            onEstado(err)
            err
        }
    }
}
