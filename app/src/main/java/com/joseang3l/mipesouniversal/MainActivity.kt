package com.joseang3l.mipesouniversal

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.joseang3l.mipesouniversal.ui.theme.MiPesoUniversalTheme

// 1. Modelo de Datos
data class Planeta(
	val nombre: String,
	val gravedad: Double,
	val color: Color,
	val imagenRes: Int // <--- Referencia al drawable
)

val listaPlanetas = listOf(
	Planeta("Sol", 274.0, Color(0xFFFFD600), R.drawable.sol),
	Planeta("Mercurio", 3.7, Color(0xFFBDBDBD), R.drawable.mercurio),
	Planeta("Venus", 8.87, Color(0xFFFFECB3), R.drawable.venus),
	Planeta("Tierra", 9.81, Color(0xFF2196F3), R.drawable.tierra),
	Planeta("Marte", 3.71, Color(0xFFFF5722), R.drawable.marte),
	Planeta("Júpiter", 24.79, Color(0xFFD7CCC8), R.drawable.jupiter),
	Planeta("Saturno", 10.44, Color(0xFFFFF176), R.drawable.saturno),
	Planeta("Urano", 8.69, Color(0xFFB2EBF2), R.drawable.urano),
	Planeta("Neptuno", 11.15, Color(0xFF3F51B5), R.drawable.neptuno),
)

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			MiApp()
		}
	}
}

@Preview(showBackground = true)
@Composable
fun MiApp() {
	MiPesoUniversalTheme {
		val navController = rememberNavController()

		Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
			NavHost(
				navController = navController,
				startDestination = "introduccion", // introduccion  pager/65
				modifier = Modifier.padding(innerPadding),
				// Animaciones Globales de transición entre ventanas
				enterTransition = { fadeIn(tween(500)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500)) },
				exitTransition = { fadeOut(tween(500)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500)) },
				popEnterTransition = { fadeIn(tween(500)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500)) },
				popExitTransition = { fadeOut(tween(500)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500)) }
			) {
				// VENTANA 1: INTRODUCCIÓN
				composable("introduccion") {
					PantallaIntro { navController.navigate("formulario") }
				}

				// VENTANA 2: FORMULARIO
				composable("formulario") {
					PantallaFormulario { peso ->
						navController.navigate("pager/$peso")
					}
				}

				// VENTANA 3: PAGER DE PLANETAS
				composable(
					route = "pager/{peso}",
					arguments = listOf(navArgument("peso") { type = NavType.FloatType })
				) { backStackEntry ->
					val peso = backStackEntry.arguments?.getFloat("peso") ?: 0f
					PantallaPagerPlanetas(peso, onRegresar = { navController.popBackStack() })
				}
			}
		}
	}
}

@Composable
fun PantallaIntro(onContinuar: () -> Unit) {
	var checked by remember { mutableStateOf(false) }

	Column(
		modifier = Modifier.fillMaxSize().padding(24.dp),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text("¿Qué hace esta App?", style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
		Spacer(modifier = Modifier.height(16.dp))
		Text(
			"Calcula tu peso exacto en diferentes cuerpos celestes del sistema solar usando leyes de gravedad física.",
			textAlign = TextAlign.Center
		)
		Spacer(modifier = Modifier.height(32.dp))

		Row(verticalAlignment = Alignment.CenterVertically) {
			Checkbox(checked = checked, onCheckedChange = { checked = it })
			Text("No volver a mostrar")
		}

		Button(onClick = onContinuar, modifier = Modifier.fillMaxWidth()) {
			Text("Comenzar")
		}
	}
}

@Composable
fun PantallaFormulario(onCalcular: (Float) -> Unit) {
	var pesoInput by remember { mutableStateOf("") }

	Column(
		modifier = Modifier.fillMaxSize().padding(24.dp),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text("Ingresa tu peso en la Tierra (kg)", style = MaterialTheme.typography.titleLarge)
		OutlinedTextField(
			value = pesoInput,
			onValueChange = { pesoInput = it },
			label = { Text("Peso en kg") },
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
			modifier = Modifier.fillMaxWidth()
		)
		Spacer(modifier = Modifier.height(24.dp))
		Button(
			onClick = { if (pesoInput.isNotEmpty()) onCalcular(pesoInput.toFloat()) },
			modifier = Modifier.fillMaxWidth()
		) {
			Text("Ver Planetas")
		}
	}
}

@Composable
fun PantallaPagerPlanetas(pesoTierra: Float, onRegresar: () -> Unit) {
	val pagerState = rememberPagerState(pageCount = { listaPlanetas.size })

	// Usamos un Box para que el Pager ocupe toda la pantalla
	Box(modifier = Modifier.fillMaxSize()) {

		HorizontalPager(
			state = pagerState,
			modifier = Modifier.fillMaxSize(),
			// Eliminamos el padding para que la imagen llegue a los bordes
			contentPadding = PaddingValues(0.dp),
			pageSpacing = 0.dp
		) { page ->
			val planeta = listaPlanetas[page]
			val masa = pesoTierra / 9.81f
			val pesoCalculado = masa * planeta.gravedad

			Box(modifier = Modifier.fillMaxSize()) {

				// 1. IMAGEN DE FONDO A PANTALLA COMPLETA
				Image(
					painter = painterResource(id = planeta.imagenRes),
					contentDescription = null,
					modifier = Modifier.fillMaxSize(),
					contentScale = ContentScale.Crop // Esto estira la imagen para cubrir todo
				)

				// 2. CAPA OSCURA GRADUAL (Para que el texto siempre sea legible)
				Box(
					modifier = Modifier
						.fillMaxSize()
						.background(
							brush = Brush.verticalGradient(
								colors = listOf(
									Color.Black.copy(alpha = 0.4f),
									Color.Black.copy(alpha = 0.2f),
									Color.Black.copy(alpha = 0.8f)
								)
							)
						)
				)

				// 3. CONTENIDO (Texto centrado o en la parte inferior)
				Column(
					modifier = Modifier
						.fillMaxSize()
						.padding(horizontal = 32.dp, vertical = 64.dp), // Margen para no chocar con bordes
					verticalArrangement = Arrangement.Center,
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					Text(
						text = planeta.nombre.uppercase(),
						style = MaterialTheme.typography.titleLarge.copy(
							shadow = Shadow(
								color = Color.Black.copy(alpha = 0.7f),
								offset = Offset(2f, 4f), // Desplazamiento en X e Y
								blurRadius = 16f          // Qué tan difuminada es la sombra
							)
						),
						fontSize = 48.sp,
						color = Color.White,
						fontWeight = FontWeight.Black,
						letterSpacing = 4.sp,
						textAlign = TextAlign.Center
					)

					Spacer(modifier = Modifier.height(10.dp))

					Text(
						text = "TU PESO AQUÍ ES DE",
						style = MaterialTheme.typography.labelMedium.copy(
							shadow = Shadow(
								color = Color.Black.copy(alpha = 0.7f),
								offset = Offset(2f, 4f), // Desplazamiento en X e Y
								blurRadius = 16f          // Qué tan difuminada es la sombra
							)
						),
						fontSize = 18.sp,
						color = Color.White,
						fontWeight = FontWeight.Bold,
					)

					Text(
						text = "${"%.2f".format(pesoCalculado)} kg",
						style = MaterialTheme.typography.displayLarge.copy(
							shadow = Shadow(
								color = Color.Black.copy(alpha = 0.7f),
								offset = Offset(2f, 4f), // Desplazamiento en X e Y
								blurRadius = 16f          // Qué tan difuminada es la sombra
							)
						),
						color = Color.White,
						fontWeight = FontWeight.ExtraBold,
						lineHeight = 70.sp,
						textAlign = TextAlign.Center,
					)

					Spacer(modifier = Modifier.height(16.dp))

					// Badge de gravedad
					Surface(
						color = Color.White.copy(alpha = 0.2f),
						shape = CircleShape,
						border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
					) {
						Text(
							text = "GRAVEDAD: ${planeta.gravedad} m/s²",
							modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
							style = MaterialTheme.typography.labelMedium,
							color = Color.White
						)
					}
				}
			}
		}

		// 2. BOTÓN DE REGRESAR (FLOTANTE ARRIBA A LA IZQUIERDA)
		IconButton(
			onClick = onRegresar,
			modifier = Modifier
				.statusBarsPadding()
				.padding(12.dp)
				.align(Alignment.TopStart)
				.background(Color.White.copy(alpha = 0.2f), CircleShape)
		) {
			Text(
				text = "‹", // Un símbolo de flecha elegante
				color = Color.White,
				style = MaterialTheme.typography.headlineMedium,
				modifier = Modifier.padding(bottom = 2.dp)
			)
		}

		// 4. INDICADOR DE PÁGINAS (Encima del Pager)
		Row(
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.padding(bottom = 48.dp),
			horizontalArrangement = Arrangement.Center
		) {
			repeat(listaPlanetas.size) { index ->
				val isSelected = pagerState.currentPage == index
				Box(
					modifier = Modifier
						.padding(4.dp)
						.size(if (isSelected) 12.dp else 8.dp)
						.clip(CircleShape)
						.background(if (isSelected) Color.White else Color.White.copy(alpha = 0.5f))
				)
			}
		}
	}
}