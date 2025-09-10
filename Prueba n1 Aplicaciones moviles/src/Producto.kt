open class Producto (val nombre : String, val precioBase : Int, val categoria: String, val tiempoPrepSeg: Int) {

    open fun precioFinal(): Int = precioBase

}