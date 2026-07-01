package com.poke.dex.presentation.pokedex

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.Image
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.poke.dex.core.service.MyFirebaseNotificationService
import com.poke.dex.presentation.pokedex.PokeDexViewModel //how is this unused?
import com.poke.dex.data.model.PokemonBrief
import com.poke.dex.presentation.map.MapActivity
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.poke.dex.ui.theme.PokedexRed
import com.poke.dex.ui.theme.PokedexYellow
import com.poke.dex.ui.theme.PokedexBlue
import com.poke.dex.ui.theme.PokemonSlate
import com.poke.dex.ui.theme.SurfaceDarkText
import com.poke.dex.ui.theme.getPokemonTypeColor
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokeDexScreen(
    viewModel: PokeDexViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var selectedPokemon by remember { mutableStateOf<PokemonBrief?>(null) }
    var showWebView by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredPokemonList = remember(uiState, searchQuery) {
        val currentState = uiState
        if (currentState is PokemonUiState.Success) {
            if (searchQuery.isBlank()) {
                currentState.data
            } else {
                currentState.data.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
        } else {
            emptyList()
        }
    }

    ObserveForegroundNotifications()

    if (showWebView && selectedPokemon != null) {
        PokemonWebView(
            url = selectedPokemon!!.url,
            onBack = { showWebView = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("PokéDex", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                )
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(text = "Search for a Pokémon") },
                    modifier = Modifier
                        .fillMaxWidth()
                )

                PartialBottomSheet()

                when (val state = uiState) {
                    is PokemonUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is PokemonUiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize().weight(1f).padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = state.message, color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { viewModel.fetchPokemonList() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }

                    is PokemonUiState.Success -> {
                        // FIXED: Single clean LazyColumn that works safely out of top-scope variables
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(filteredPokemonList) { pokemon ->
                                PokemonCard(
                                    pokemon = pokemon,
                                    onClick = { selectedPokemon = pokemon }
                                )
                            }
                        }
                    }
                }

                if (selectedPokemon != null) {
                    AlertDialog(
                        onDismissRequest = { selectedPokemon = null },
                        title = { Text(text = selectedPokemon!!.name.capitalizeFirst()) },
                        text = { Text("How would you like to view this Pokémon?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val intent =
                                        Intent(Intent.ACTION_VIEW, Uri.parse(selectedPokemon!!.url))
                                    context.startActivity(intent)
                                    selectedPokemon = null
                                }
                            ) {
                                Text("External Browser")
                            }
                            Button(
                                onClick = {
                                    val intent = Intent(context, MapActivity::class.java)
                                    context.startActivity(intent)
                                    selectedPokemon = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Navigate to Maps")
                            }
                        },
                        dismissButton = {
                            OutlinedButton(
                                onClick = {
                                    showWebView = true
                                    selectedPokemon = null
                                }
                            ) {
                                Text("View in App")
                            }
                        }
                    )
                }
            }
        }
    }
}
@Composable
fun PokemonCard(
    pokemon: PokemonBrief,
    // Mocking an array of type strings. Replace this with your actual detailed parsed API payload array later!
    pokemonTypes: List<String> = listOf("Grass", "Poison"),
    onClick: () -> Unit
) {
    val pokemonId = pokemon.url.trimEnd('/').split("/").last()
    val imageUrl = "https://githubusercontent.com"
    val formattedId = "#${pokemonId.padStart(3, '0')}"

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp), // Styled with curved corners resembling official trading cards
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 1. Image Container with Loading Spinner Framework
            Box(
                modifier = Modifier
                    .size(85.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "${pokemon.name} sprite representation graphic",
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        // Shows a themed progress spinner while the image downloads over the network
                        CircularProgressIndicator(
                            color = PokedexRed,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    error = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Error loading image resource",
                            tint = Color.Gray
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Information Display Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formattedId,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PokemonSlate,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = pokemon.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SurfaceDarkText
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Dynamic Elemental Type Badges Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    pokemonTypes.forEach { type ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(getPokemonTypeColor(type))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = type.uppercase(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // 4. Subtle Pokédex navigation indicator button
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Navigate to Pokemon detail page view",
                tint = PokedexRed.copy(alpha = 0.7f),
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}

@Composable
fun PokemonWebView(url: String, onBack: () ->Unit) {
    BackHandler(onBack = onBack)

    AndroidView(factory = {
        WebView(it).apply {
            webViewClient = WebViewClient()
            loadUrl(url)
        }
    },
        update = {
            it.loadUrl(url)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartialBottomSheet() {
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    var galleryImageUri by remember { mutableStateOf<Uri?>(null)}
    var capturedImage by remember { mutableStateOf<Bitmap?>(null)}
    var showImageDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        capturedImage = bitmap
        galleryImageUri = null
        showBottomSheet = false
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        galleryImageUri = uri
        capturedImage = null
        showBottomSheet = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted){
            cameraLauncher.launch()
        } else {
            Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            showBottomSheet = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = { showBottomSheet = true }
        ) {
            Text("Update Photo")
        }

        capturedImage?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = "Camera Result", modifier = Modifier.size(100.dp))
        }
        galleryImageUri?.let{
            Text("Selected Image: ${it.lastPathSegment}")
        }

        if(showBottomSheet){
            ModalBottomSheet(
                onDismissRequest = {showBottomSheet = false},
                sheetState = sheetState
            ) {

                capturedImage?.let { bitmap ->
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clickable { showImageDialog = true }
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Camera Result Thumbnail",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                if (showImageDialog && capturedImage != null) {
                    AlertDialog(
                        onDismissRequest = { showImageDialog = false },
                        confirmButton = {
                            TextButton(onClick = { showImageDialog = false }) {
                                Text("Close")
                            }
                        },
                        title = { Text("Photo Preview") },
                        text = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = capturedImage!!.asImageBitmap(),
                                    contentDescription = "Full Screen Preview",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Select an option", style = MaterialTheme.typography.titleLarge)
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Take a picture!")
                    }
                }

                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select from gallery")
                }
            }
        }
    }
}

@Composable
fun ObserveForegroundNotifications() {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val title = intent?.getStringExtra(MyFirebaseNotificationService.EXTRA_TITLE) ?: ""
                val body = intent?.getStringExtra(MyFirebaseNotificationService.EXTRA_BODY) ?: ""
                Toast.makeText(context, "PokeAlert! $title: $body", Toast.LENGTH_LONG).show()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(
                receiver,
                IntentFilter(MyFirebaseNotificationService.ACTION_FOREGROUND_NOTIFICATION),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter(MyFirebaseNotificationService.ACTION_FOREGROUND_NOTIFICATION),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
}


fun String.capitalizeFirst() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString()}