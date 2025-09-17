// GestorPedidos.kt
import kotlinx.coroutines.delay

class GestorPedidos(private val ivaPorc: Double = 0.19) {


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
            tipoCliente.equals("VIP", true) -> (subtotal * 0.10).toInt()
            tipoCliente.equals("PREMIUM", true) -> (subtotal * 0.15).toInt()
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
            validarPedido(items)  // puede lanzar IllegalArgumentException

            onEstado(EstadoPedido.Pendiente)
            delay(500)

            onEstado(EstadoPedido.EnPreparacion)
            delay(items.sumOf { it.tiempoPrepSeg } * 1000L)

            onEstado(EstadoPedido.Listo)
            EstadoPedido.Listo

        } catch (e: Exception) {
            val err = EstadoPedido.Error(e.message ?: "Error desconocido")
            onEstado(err)
            err
        }
    }

}
