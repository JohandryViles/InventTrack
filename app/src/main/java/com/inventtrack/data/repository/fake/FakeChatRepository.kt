package com.inventtrack.data.repository.fake

import com.inventtrack.data.model.TipoMovimiento
import com.inventtrack.data.repository.ChatRepository
import com.inventtrack.data.repository.MovementRepository
import com.inventtrack.data.repository.ProductRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.Normalizer
import java.time.LocalDate

/** InvenBot simulado: responde con reglas simples usando los datos reales de los repositorios. */
class FakeChatRepository(
    private val productRepository: ProductRepository,
    private val movementRepository: MovementRepository,
    private val ioDispatcher: CoroutineDispatcher
) : ChatRepository {

    override suspend fun ask(pregunta: String): String = withContext(ioDispatcher) {
        delay(CHAT_DELAY_MS)
        val texto = normalizar(pregunta)
        val productos = productRepository.observeProducts().first()

        when {
            "bajo" in texto || "critico" in texto -> {
                val bajos = productos.filter { it.tieneStockBajo }
                if (bajos.isEmpty()) "No hay productos con stock bajo en este momento."
                else "Productos con stock bajo:\n" + bajos.joinToString("\n") {
                    "• ${it.nombre}: ${it.stockActual} (mínimo ${it.stockMinimo})"
                }
            }

            "entrada" in texto || "salida" in texto -> {
                val tipo = if ("entrada" in texto) TipoMovimiento.ENTRADA else TipoMovimiento.SALIDA
                val etiqueta = if (tipo == TipoMovimiento.ENTRADA) "entradas" else "salidas"
                val hoy = LocalDate.now()
                val deHoy = movementRepository.observeMovements().first()
                    .filter { it.tipo == tipo && it.fecha.toLocalDate() == hoy }
                if (deHoy.isEmpty()) "Hoy no se registraron $etiqueta."
                else "Hoy hubo ${deHoy.size} $etiqueta:\n" + deHoy.joinToString("\n") {
                    "• ${it.productoNombre}: ${it.cantidad} unidades (${it.usuario})"
                }
            }

            else -> {
                // Coincide si la primera palabra del producto aparece en la pregunta, p. ej. "paracetamol".
                val producto = productos.find { normalizar(it.nombre).substringBefore(" ") in texto }
                if (producto != null) {
                    "Actualmente tienes ${producto.stockActual} unidades de ${producto.nombre} " +
                        "(stock mínimo ${producto.stockMinimo})."
                } else {
                    "Puedo ayudarte con el stock de un producto, los productos con stock bajo " +
                        "o las entradas y salidas de hoy."
                }
            }
        }
    }

    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")
}
