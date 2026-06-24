package com.senac.restapi.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.senac.restapi.database.TripPhotoEntity
import com.senac.restapi.ui.theme.*
import com.senac.restapi.viewmodel.TripPhotoViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripPhotosScreen(
    tripId: Int,
    tripTitle: String,
    tripPhotoViewModel: TripPhotoViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val photos by tripPhotoViewModel.photos.collectAsStateWithLifecycle()

    var showAddOptionsSheet by remember { mutableStateOf(false) }
    var photoToDelete by remember { mutableStateOf<TripPhotoEntity?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(tripId) {
        tripPhotoViewModel.setTripId(tripId)
    }

    // Launcher para captura de foto com a câmera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let { uri ->
                tripPhotoViewModel.addPhoto(tripId, uri.toString())
            }
        }
        pendingCameraUri = null
        showAddOptionsSheet = false
    }

    // Launcher de permissão de câmera — após concedida, cria o arquivo e abre a câmera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createPhotoFileUri(context)
            if (uri != null) {
                pendingCameraUri = uri
                cameraLauncher.launch(uri)
            }
        }
    }

    // Launcher para seleção de foto da galeria
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) { /* URI não suporta persistable permission */ }
            tripPhotoViewModel.addPhoto(tripId, it.toString())
        }
        showAddOptionsSheet = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Fotos da Viagem",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = tripTitle,
                            fontSize = 12.sp,
                            color = TravelOnPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = TravelOnPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TravelGreen,
                    titleContentColor = TravelOnPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddOptionsSheet = true },
                containerColor = TravelGreen,
                contentColor = TravelOnPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar foto")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TravelBackground)
                .padding(innerPadding)
        ) {
            if (photos.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = TravelGreenLight,
                        modifier = Modifier.size(72.dp)
                    )
                    Text(
                        text = "Nenhuma foto ainda",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = TravelGray
                    )
                    Text(
                        text = "Toque no + para adicionar fotos",
                        fontSize = 14.sp,
                        color = TravelGray.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    items(photos, key = { it.id }) { photo ->
                        PhotoGridItem(
                            photo = photo,
                            onLongClick = { photoToDelete = photo }
                        )
                    }
                }
            }
        }
    }

    // Bottom Sheet para escolher origem da foto
    if (showAddOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddOptionsSheet = false },
            containerColor = TravelCardBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Adicionar Foto",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TravelGreenDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("Câmera", color = TravelGreenDark, fontWeight = FontWeight.Medium) },
                    supportingContent = { Text("Tirar uma nova foto", color = TravelGray, fontSize = 13.sp) },
                    leadingContent = {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TravelGreen)
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(TravelGreen.copy(alpha = 0.08f))
                        .clickable {
                            launchCamera(
                                context = context,
                                cameraPermissionLauncher = { perm ->
                                    cameraPermissionLauncher.launch(perm)
                                },
                                onUriReady = { uri ->
                                    pendingCameraUri = uri
                                    cameraLauncher.launch(uri)
                                }
                            )
                        },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                Spacer(modifier = Modifier.height(8.dp))

                ListItem(
                    headlineContent = { Text("Galeria", color = TravelGreenDark, fontWeight = FontWeight.Medium) },
                    supportingContent = { Text("Escolher da galeria do dispositivo", color = TravelGray, fontSize = 13.sp) },
                    leadingContent = {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = TravelGreen)
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(TravelGreen.copy(alpha = 0.08f))
                        .clickable { galleryLauncher.launch("image/*") },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Confirmação de exclusão de foto
    photoToDelete?.let { photo ->
        AlertDialog(
            onDismissRequest = { photoToDelete = null },
            title = { Text("Excluir foto?", color = TravelGreenDark) },
            text = { Text("Esta foto será removida da viagem.", color = TravelGray) },
            confirmButton = {
                Button(
                    onClick = {
                        tripPhotoViewModel.deletePhoto(photo)
                        photoToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TravelError)
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { photoToDelete = null }) {
                    Text("Cancelar", color = TravelGray)
                }
            },
            containerColor = TravelCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoGridItem(
    photo: TripPhotoEntity,
    onLongClick: () -> Unit
) {
    val model: Any = remember(photo.photoUri) {
        if (photo.photoUri.startsWith("content://")) Uri.parse(photo.photoUri)
        else File(photo.photoUri)
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
    ) {
        AsyncImage(
            model = model,
            contentDescription = "Foto da viagem",
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(onClick = {}, onLongClick = onLongClick),
            contentScale = ContentScale.Crop
        )
    }
}

private fun launchCamera(
    context: Context,
    cameraPermissionLauncher: (String) -> Unit,
    onUriReady: (Uri) -> Unit
) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        == PackageManager.PERMISSION_GRANTED
    ) {
        createPhotoFileUri(context)?.let { onUriReady(it) }
    } else {
        cameraPermissionLauncher(Manifest.permission.CAMERA)
    }
}

private fun createPhotoFileUri(context: Context): Uri? {
    return try {
        val photosDir = File(context.filesDir, "trip_photos").apply { mkdirs() }
        val photoFile = File(photosDir, "photo_${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    } catch (e: Exception) {
        null
    }
}
