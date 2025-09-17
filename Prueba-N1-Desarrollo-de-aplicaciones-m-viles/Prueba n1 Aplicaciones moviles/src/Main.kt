import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val gestor = GestorPedidos()
    val catalogo = gestor.catalogoInicial()

    println("=== SISTEMA FOODEXPRESS===")
    println("Catálogo disponible:")
    catalogo.forEachIndexed { i, p ->
        val etiqueta = when (p) {
            is ProductoComida -> if (p.premium) "(Premium)" else ""
            is ProductoBebida -> "(${p.tamano.uppercase().replace("PEQUEÑO","Pequeño").replace("MEDIANO","Mediano").replace("GRANDE","Grande")})"
            else -> ""
        }
        println("${i + 1}. ${p.nombre} $etiqueta - $${p.precioFinal()} (${p.categoria})")
    }

    // --- Lectura con manejo de errores ---
    print("\nSeleccione productos (números separados por coma): ")
    val carrito = try {
        val seleccion = readln().split(",").map { it.trim().toInt() - 1 }
        seleccion.map { idx ->
            require(idx in catalogo.indices) { "Índice fuera de rango: ${idx + 1}" }
            catalogo[idx]
        }
    } catch (e: Exception) {
        println("Entrada inválida: ${e.message}. Saliendo.")
        return@runBlocking
    }

    print("Cliente tipo (regular/vip/premium): ")
    val tipoCliente = readln()

    println("\nProcesando pedido...")
    val estadoFinal = gestor.procesarPedidoAsync(carrito) { estado ->
        println("Estado: $estado")
    }

    val resumen = gestor.calcularTotales(carrito, tipoCliente)

    // === 1) Mostrar RESUMEN primero ===
    println("\n=== RESUMEN DEL PEDIDO===")
    resumen.productos.forEach { p ->
        val etiqueta = when (p) {
            is ProductoComida -> if (p.premium) "(Premium)" else ""
            is ProductoBebida -> "(${p.tamano.uppercase().replace("PEQUEÑO","Pequeño").replace("MEDIANO","Mediano").replace("GRANDE","Grande")})"
            else -> ""
        }
        println("- ${p.nombre} $etiqueta: $${p.precioFinal()}")
    }
    println("Subtotal: $${resumen.subtotal}")
    val etiquetaDesc = when {
        tipoCliente.equals("vip", true) -> "Descuento VIP (10%)"
        tipoCliente.equals("premium", true) -> "Descuento Premium (15%)"
        tipoCliente.equals("regular", true) -> "Descuento Regular (5%)"
        else -> "Descuento"
    }
    println("$etiquetaDesc: -$${resumen.descuento}")
    println("IVA (19%): $${resumen.iva}")
    println("TOTAL: $${resumen.total}")

    println("\nEstado final: $estadoFinal")

    // === 2) Registrar venta y luego mostrar REPORTE acumulado ===
    gestor.registrar(resumen)
    println()
    println(gestor.generarReporteTexto())
}
