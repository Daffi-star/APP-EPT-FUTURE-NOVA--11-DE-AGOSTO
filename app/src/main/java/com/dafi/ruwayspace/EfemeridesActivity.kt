package com.dafi.ruwayspace

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip

class EfemeridesActivity : AppCompatActivity() {

    data class Efemeride(val fecha: String, val titulo: String, val categoria: String, val descripcion: String)

    private val listaCompleta = listOf(
        // --- OCCIDENTAL / PERÚ ---
        Efemeride("Febrero", "Carnavales Peruanos", "Occidental", "Festividad llena de danzas y juegos con agua y pintura en todo el país."),
        Efemeride("Marzo/Abril", "Semana Santa", "Occidental", "Conmemoración religiosa con procesiones emblemáticas, especialmente en Ayacucho y Cusco."),
        Efemeride("24 de Junio", "Inti Raymi", "Occidental", "La Fiesta del Sol en Cusco, reviviendo el esplendor del Imperio Incaico."),
        Efemeride("29 de Junio", "San Pedro y San Pablo", "Occidental", "Festividad de los pescadores, con procesiones marítimas en el Callao y Chorrillos."),
        Efemeride("28 de Julio", "Fiestas Patrias", "Occidental", "Aniversario de la Independencia del Perú; civismo, desfiles y orgullo nacional."),
        Efemeride("30 de Agosto", "Santa Rosa de Lima", "Occidental", "Día de la santa patrona de Lima, América y las Filipinas."),
        Efemeride("Octubre", "Señor de los Milagros", "Occidental", "Mes morado en Lima, una de las procesiones más grandes del mundo."),
        Efemeride("1 de Noviembre", "Día de Todos los Santos", "Occidental", "Visita a los cementerios y preparación de 'tanta wawas' (panes decorados)."),
        Efemeride("25 de Diciembre", "Navidad Andina", "Occidental", "Celebración del nacimiento de Jesús con toques tradicionales, nacimientos y danzas."),
        Efemeride("14 de Febrero", "Día de San Valentín", "Occidental", "Celebración global del amor y la amistad con profundas raíces en la cultura occidental."),
        Efemeride("Febrero/Marzo", "Carnaval de Río de Janeiro", "Occidental", "Una de las fiestas más alegres de Brasil y del mundo, famosa por sus desfiles de samba y carros alegóricos."),
        Efemeride("17 de Marzo", "Día de San Patricio", "Occidental", "Festividad tradicional irlandesa que conmemora al santo patrono de Irlanda con desfiles y el característico color verde."),
        Efemeride("14 de Julio", "Día de la Bastilla", "Occidental", "Fiesta Nacional de Francia que conmemora la Revolución Francesa con desfiles militares y fuegos artificiales."),
        Efemeride("Último miércoles de Agosto", "La Tomatina", "Occidental", "Tradicional festival español celebrado en Buñol, donde los participantes se arrojan tomates por diversión."),
        Efemeride("Septiembre/Octubre", "Oktoberfest", "Occidental", "La feria y fiesta de la cultura bávara más grande del mundo, celebrada en Múnich, Alemania."),
        Efemeride("31 de Octubre", "Halloween (Noche de Brujas)", "Occidental", "Celebración de origen celta muy popular en Occidente, caracterizada por disfraces, decoraciones y recolección de dulces."),
        Efemeride("1 y 2 de Noviembre", "Día de los Muertos", "Occidental", "Tradición mexicana declarada Patrimonio Cultural Inmaterial, que honra la memoria de los difuntos con altares y ofrendas."),
        Efemeride("Cuarto jueves de Noviembre", "Día de Acción de Gracias", "Occidental", "Festividad tradicional de Norteamérica para agradecer las cosechas y compartir una cena familiar en torno al pavo."),

        // --- ORIENTAL / JAPÓN ---
        Efemeride("1 de Enero", "Shogatsu (Año Nuevo)", "Oriental", "Tradición de visitar templos y recibir el año con comidas especiales como el osechi-ryori."),
        Efemeride("3 de Marzo", "Hinamatsuri", "Oriental", "Festival de las niñas, donde se exhiben muñecas tradicionales para pedir salud y felicidad."),
        Efemeride("Marzo/Abril", "Hanami", "Oriental", "La famosa costumbre de observar la floración de los cerezos (Sakura) en parques y jardines."),
        Efemeride("5 de Mayo", "Kodomo no Hi", "Oriental", "Día de los niños; se cuelgan banderas de carpas (koinobori) para simbolizar fuerza."),
        Efemeride("7 de Julio", "Tanabata", "Oriental", "Festival de las estrellas; se escriben deseos en cintas de papel (tanzaku) y se cuelgan en bambú."),
        Efemeride("Agosto", "Obon", "Oriental", "Festividad budista para honrar el espíritu de los ancestros, acompañada de danzas Bon Odori."),
        Efemeride("15 de Septiembre", "Tsukimi", "Oriental", "Celebración de la observación de la luna llena de otoño con pasteles de arroz (tsukimi dango)."),
        Efemeride("15 de Noviembre", "Shichi-Go-San", "Oriental", "Rito de paso para niños de 3, 5 y 7 años para celebrar su crecimiento y salud."),
        Efemeride("31 de Diciembre", "Omisoka", "Oriental", "La víspera de Año Nuevo, caracterizada por comer fideos toshikoshi soba para una larga vida."),
        Efemeride("Enero/Febrero", "Año Nuevo Chino (Festival de Primavera)", "Oriental", "La fiesta más grande de China; celebrada con danzas de dragones, fuegos artificiales y reuniones familiares para atraer la fortuna."),
        Efemeride("Marzo", "Holi (Festival de los Colores)", "Oriental", "Festividad india que da la bienvenida a la primavera lanzando polvos de colores vibrantes y celebrando la alegría y el amor."),
        Efemeride("13-15 de Abril", "Songkran (Año Nuevo Tailandés)", "Oriental", "El famoso festival del agua en Tailandia, donde se realizan batallas de agua amistosas para limpiar el pasado y atraer buena suerte."),
        Efemeride("Junio", "Festival del Bote del Dragón", "Oriental", "Tradición china que consiste en carreras de botes con forma de dragón y el consumo de 'zongzi' (arroz glutinoso envuelto en hojas)."),
        Efemeride("Septiembre/Octubre", "Chuseok (Festival de la Cosecha)", "Oriental", "Una de las fiestas más importantes en Corea, centrada en agradecer la cosecha, visitar ancestros y comer 'songpyeon' (pasteles de arroz)."),
        Efemeride("Octubre/Noviembre", "Diwali (Festival de las Luces)", "Oriental", "Festividad hindú que simboliza la victoria de la luz sobre la oscuridad, decorando hogares con lámparas de aceite y fuegos artificiales."),
        Efemeride("Noviembre", "Loy Krathong", "Oriental", "Festival de las linternas en Tailandia; las personas liberan cestas flotantes decoradas en los ríos para dejar ir la mala suerte y pedir deseos."),
    )

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_efemerides)

        container = findViewById(R.id.containerEfemerides)

        findViewById<ImageView>(R.id.btnBackEfemerides).setOnClickListener {
            finish()
        }

        // Mostrar todas por defecto
        mostrarTarjetas(listaCompleta)

        // Configurar filtros de los Chips
        findViewById<Chip>(R.id.chipTodos).setOnClickListener {
            mostrarTarjetas(listaCompleta)
        }
        findViewById<Chip>(R.id.chipOccidental).setOnClickListener {
            mostrarTarjetas(listaCompleta.filter { it.categoria == "Occidental" })
        }
        findViewById<Chip>(R.id.chipOriental).setOnClickListener {
            mostrarTarjetas(listaCompleta.filter { it.categoria == "Oriental" })
        }
    }

    private fun mostrarTarjetas(efemerides: List<Efemeride>) {
        container.removeAllViews()

        // Factor para convertir DP a Píxeles reales según la pantalla del celular
        val scale = resources.displayMetrics.density
        val marginHorizontal = (16 * scale).toInt()
        val marginBottom = (12 * scale).toInt()
        val paddingCard = (16 * scale).toInt()

        for (item in efemerides) {
            val card = MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(marginHorizontal, 0, marginHorizontal, marginBottom)
                }
                radius = 16f * scale
                cardElevation = 2f * scale
                setCardBackgroundColor(resources.getColor(android.R.color.white, theme))
                strokeWidth = 0
            }

            val layoutInterno = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(paddingCard, paddingCard, paddingCard, paddingCard)
            }

            val tvFecha = TextView(this).apply {
                text = "🗓️ ${item.fecha} (${item.categoria})"
                setTextColor(resources.getColor(R.color.purple_main, theme))
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12f)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val tvTitulo = TextView(this).apply {
                text = item.titulo
                setTextColor(resources.getColor(R.color.text_dark, theme))
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f)
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, (4 * scale).toInt(), 0, (4 * scale).toInt())
            }

            val tvDesc = TextView(this).apply {
                text = item.descripcion
                setTextColor(resources.getColor(android.R.color.darker_gray, theme))
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 13f)
            }

            layoutInterno.addView(tvFecha)
            layoutInterno.addView(tvTitulo)
            layoutInterno.addView(tvDesc)
            card.addView(layoutInterno)
            container.addView(card)
        }
    }
}