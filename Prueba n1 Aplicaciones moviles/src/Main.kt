import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val gestor = GestorPedidos()
    val catalogo = gestor.catalogoInicial()

    println("=== SISTEMA FOODEXPRESS===")
    println("Catálogo disponible:")
    catalogo.forEachIndexed { i, p ->
        println("${i + 1}. ${p.nombre} - ${p.precioBase} (${p.categoria})")
    }

    // Leer selección de productos
    print("\nSeleccione productos (números separados por coma): ")
    val seleccion = readln().split(",").map { it.trim().toInt() - 1 }
    val carrito = seleccion.map { catalogo[it] }

    // Leer tipo de cliente
    print("Cliente tipo (regular/vip/premium): ")
    val tipoCliente = readln()

    println("\nProcesando pedido...")

    // Mostrar estados paso a paso
    val estadoFinal = gestor.procesarPedidoAsync(carrito) { estado ->
        println("Estado: $estado")
    }

    // Calcular totales
    val resumen = gestor.calcularTotales(carrito, tipoCliente)

    println("\n=== RESUMEN DEL PEDIDO===")
    resumen.productos.forEach { println("- ${it.nombre}: ${it.precioBase}") }
    println("Subtotal: $${resumen.subtotal}")
    println("Descuento: -$${resumen.descuento}")
    println("IVA (19%): $${resumen.iva}")
    println("TOTAL: $${resumen.total}")

    println("\nEstado final: $estadoFinal")
}
