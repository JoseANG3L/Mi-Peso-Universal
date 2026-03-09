package com.joseang3l.mipesouniversal

import android.content.Context
import android.graphics.Matrix
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.SurfaceView
import android.view.TextureView
import android.view.ViewGroup
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.joseang3l.mipesouniversal.ui.theme.MiPesoUniversalTheme
import androidx.core.net.toUri
import androidx.xr.compose.testing.toDp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// 1. Modelo de Datos
data class Planeta(
	val nombre: String,
	val gravedad: Double,
	val color: Color,
	val videoRes: Int
)

val listaPlanetas = listOf(
	Planeta("Sol", 274.0, Color(0xFFFFD600), R.raw.sol_zoom),
	Planeta("Mercurio", 3.7, Color(0xFFBDBDBD), R.raw.mercurio_zoom),
	Planeta("Venus", 8.87, Color(0xFFFFECB3), R.raw.venus_zoom),
	Planeta("Tierra", 9.81, Color(0xFF2196F3), R.raw.tierra_zoom),
	Planeta("Marte", 3.71, Color(0xFFFF5722), R.raw.marte_zoom),
	Planeta("Júpiter", 24.79, Color(0xFFD7CCC8), R.raw.jupiter_zoom),
	Planeta("Saturno", 10.44, Color(0xFFFFF176), R.raw.saturno_zoom),
	Planeta("Urano", 8.69, Color(0xFFB2EBF2), R.raw.urano_zoom),
	Planeta("Neptuno", 11.15, Color(0xFF3F51B5), R.raw.neptuno_zoom),
)

@OptIn(UnstableApi::class)
@Composable
fun VideoBackground(videoRes: Int, esPaginaActiva: Boolean) {
	val context = LocalContext.current
	val lifecycle = LocalLifecycleOwner.current.lifecycle

	val exoPlayer = remember(videoRes) {
		ExoPlayer.Builder(context).build().apply {
			val uri = "android.resource://${context.packageName}/$videoRes"
			setMediaItem(MediaItem.fromUri(uri))
			repeatMode = Player.REPEAT_MODE_ALL
			volume = 0f
			prepare()
			playWhenReady = true
		}
	}

	DisposableEffect(lifecycle, videoRes) {
		val observer = LifecycleEventObserver { _, event ->
			when (event) {
				Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
				Lifecycle.Event.ON_RESUME -> exoPlayer.play()
				else -> {}
			}
		}
		lifecycle.addObserver(observer)

		onDispose {
			lifecycle.removeObserver(observer)
			exoPlayer.release()
		}
	}

	// Usamos Surface con clipForced = true
	Surface(
		modifier = Modifier
			.fillMaxSize(),
		color = Color.Black
	) {
		AndroidView(
			factory = { ctx ->
				PlayerView(ctx).apply {
					player = exoPlayer
					useController = false
					resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

					layoutParams = ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT
					)

					// Forzar clipping
					clipToOutline = true
				}
			},
			modifier = Modifier
				.fillMaxSize()
		)
	}
}

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			VideoSimpleCorregido()
		}
	}
}

@Preview(showBackground = true)
@Composable
fun MiApp() {
	MiPesoUniversalTheme {
		val navController = rememberNavController()

		NavHost(
			navController = navController,
			startDestination = "pager/65", // introduccion  pager/65
			enterTransition = {
				fadeIn(
					tween(500)
				) + slideIntoContainer(
					AnimatedContentTransitionScope.SlideDirection.Left,
					tween(500)
				)
			},
			exitTransition = {
				fadeOut(
					tween(500)
				) + slideOutOfContainer(
					AnimatedContentTransitionScope.SlideDirection.Left,
					tween(500)
				)
		 	},
			popEnterTransition = {
				fadeIn(
					tween(500)
				) + slideIntoContainer(
					AnimatedContentTransitionScope.SlideDirection.Right,
					tween(500)
				)
		 	},
			popExitTransition = {
				fadeOut(
					tween(500)
				) + slideOutOfContainer(
					AnimatedContentTransitionScope.SlideDirection.Right,
					tween(500)
				)
			}
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
	val indiceTierra = listaPlanetas.indexOfFirst { it.nombre == "Tierra" }.coerceAtLeast(0)
	val pagerState = rememberPagerState(
		initialPage = indiceTierra,
		pageCount = { listaPlanetas.size }
	)

	Box(
		modifier = Modifier
		.fillMaxSize()
		.background(Color.Black)
	) {
		HorizontalPager(
			state = pagerState,
			modifier = Modifier.fillMaxSize().clipToBounds(),
			pageSpacing = 0.dp,
			beyondViewportPageCount = 0, // Cargar solo la página siguiente/anterior
			pageSize = PageSize.Fill
		) { page ->
			val planeta = listaPlanetas[page]
			val masa = pesoTierra / 9.81f
			val pesoCalculado = masa * planeta.gravedad

			if (page == pagerState.currentPage) {
				Surface(
					modifier = Modifier
						.fillMaxSize(),
					color = Color.Black
				) {
					// VIDEO SIEMPRE VISIBLE (se reproduce durante el arrastre)
					// VIDEO CON CLIP ESTRICTO
					Box(
						modifier = Modifier
							.fillMaxSize()
							.clipToBounds() // Clip del contenedor de video
					) {
						key(planeta.nombre) {
							VideoBackground(
								videoRes = planeta.videoRes,
								esPaginaActiva = true
							)
						}
					}

					// Capa oscura gradual (se mantiene siempre)
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

					// Contenido (también con clip)
					Box(
						modifier = Modifier
							.fillMaxSize()
							.clipToBounds()
					) {
						Column(
							modifier = Modifier
								.fillMaxSize()
								.safeDrawingPadding()
								.padding(24.dp),
							verticalArrangement = Arrangement.Center,
							horizontalAlignment = Alignment.CenterHorizontally
						) {
							// Título
							Text(
								text = planeta.nombre.uppercase(),
								style = MaterialTheme.typography.titleLarge.copy(
									shadow = Shadow(
										color = Color.Black.copy(alpha = 0.7f),
										offset = Offset(2f, 4f),
										blurRadius = 16f
									)
								),
								fontSize = 48.sp,
								color = Color.White,
								fontWeight = FontWeight.Black,
								letterSpacing = 4.sp,
								textAlign = TextAlign.Center
							)

							Spacer(modifier = Modifier.height(10.dp))

							// Texto "TU PESO AQUÍ ES DE" con su propia animación
							Text(
								text = "TU PESO AQUÍ ES DE",
								style = MaterialTheme.typography.labelMedium.copy(
									shadow = Shadow(
										color = Color.Black.copy(alpha = 0.7f),
										offset = Offset(2f, 4f),
										blurRadius = 16f
									)
								),
								fontSize = 18.sp,
								color = Color.White,
								fontWeight = FontWeight.Bold,
							)

							// Peso calculado con animación de escala
							Text(
								text = "${"%.2f".format(pesoCalculado)} kg",
								style = MaterialTheme.typography.displayLarge.copy(
									shadow = Shadow(
										color = Color.Black.copy(alpha = 0.7f),
										offset = Offset(2f, 4f),
										blurRadius = 16f
									)
								),
								color = Color.White,
								fontWeight = FontWeight.ExtraBold,
								lineHeight = 70.sp,
								textAlign = TextAlign.Center,
							)

							Spacer(modifier = Modifier.height(16.dp))

							// Badge de gravedad con animación
							Surface(
								color = Color.White.copy(alpha = 0.2f),
								shape = CircleShape,
								border = BorderStroke(
									1.dp,
									Color.White.copy(alpha = 0.5f)
								)
							) {
								Text(
									text = "GRAVEDAD: ${planeta.gravedad} m/s²",
									modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
									style = MaterialTheme.typography.labelMedium,
									color = Color.White
								)
							}

							Spacer(modifier = Modifier.height(82.dp))
						}
					}
				}
			}
		}

		// Botón de regresar
		IconButton(
			onClick = onRegresar,
			modifier = Modifier
				.statusBarsPadding()
				.padding(12.dp)
				.align(Alignment.TopStart)
				.background(Color.White.copy(alpha = 0.2f), CircleShape)
				.size(42.dp)
		) {
			Icon(
				imageVector = Icons.Default.ArrowBack,
				contentDescription = "Regresar",
				tint = Color.White,
				modifier = Modifier.size(24.dp)
			)
		}

		// Indicador de páginas
		Row(
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.padding(bottom = 32.dp),
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically
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


@Composable
fun VideoSimpleCorregido() {
	val context = LocalContext.current
	val configuration = LocalConfiguration.current
	val density = LocalDensity.current

	// Obtener dimensiones exactas de la pantalla
	val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }.toInt()
	val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }.toInt()

	val video1 = Uri.parse("android.resource://${context.packageName}/raw/marte_zoom")
	val video2 = Uri.parse("android.resource://${context.packageName}/raw/jupiter_zoom")

	val pagerState = rememberPagerState { 2 }

	// Crear players
	val player1 = remember {
		ExoPlayer.Builder(context).build().apply {
			setMediaItem(MediaItem.fromUri(video1))
			prepare()
			repeatMode = ExoPlayer.REPEAT_MODE_ALL
			volume = 0f
		}
	}

	val player2 = remember {
		ExoPlayer.Builder(context).build().apply {
			setMediaItem(MediaItem.fromUri(video2))
			prepare()
			repeatMode = ExoPlayer.REPEAT_MODE_ALL
			volume = 0f
		}
	}

	// Controlar reproducción
	LaunchedEffect(pagerState.currentPage) {
		player1.playWhenReady = pagerState.currentPage == 0
		player2.playWhenReady = pagerState.currentPage == 1
	}

	// Liberar recursos
	DisposableEffect(Unit) {
		onDispose {
			player1.release()
			player2.release()
		}
	}

	Box(
		modifier = Modifier
			.fillMaxSize()
			.clipToBounds() // Clip principal
	) {
		HorizontalPager(
			state = pagerState,
			modifier = Modifier.fillMaxSize()
		) { page ->
			// FrameLayout personalizado que fuerza el recorte
			AndroidView(
				factory = { ctx ->
					// Usar un FrameLayout como contenedor forzado
					android.widget.FrameLayout(ctx).apply {
						layoutParams = android.view.ViewGroup.LayoutParams(
							screenWidthPx,
							screenHeightPx
						)

						// Forzar clip
						clipToPadding = true
						clipChildren = true

						// Crear PlayerView
						val playerView = PlayerView(ctx).apply {
							player = if (page == 0) player1 else player2
							useController = false
							resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

							// Forzar que ocupe todo el FrameLayout
							layoutParams = android.view.ViewGroup.LayoutParams(
								android.view.ViewGroup.LayoutParams.MATCH_PARENT,
								android.view.ViewGroup.LayoutParams.MATCH_PARENT
							)
						}

						// Añadir PlayerView al FrameLayout
						addView(playerView)
					}
				},
				modifier = Modifier
					.fillMaxSize()
					.clipToBounds() // Clip adicional en Compose
			)
		}
	}
}