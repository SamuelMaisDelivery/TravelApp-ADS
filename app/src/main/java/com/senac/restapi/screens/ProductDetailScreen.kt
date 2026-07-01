package com.senac.restapi.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.senac.restapi.ui.theme.*
import com.senac.restapi.viewmodel.ProductViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Int,
    productViewModel: ProductViewModel,
    onNavigateBack: () -> Unit,
) {
    val products by productViewModel.products.collectAsState()
    val product = products.find { it.id == productId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = product?.title ?: "Detalhes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TravelOnPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TravelGreen,
                    titleContentColor = TravelOnPrimary
                )
            )
        }
    ) { innerPadding ->
        if (product == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Produto não encontrado", color = TravelGray)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TravelBackground)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Imagem Hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                AsyncImage(
                    model = product.thumbnail,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    androidx.compose.ui.graphics.Color.Transparent,
                                    TravelOnSurface.copy(alpha = 0.5f)
                                ),
                                startY = 100f
                            )
                        )
                )
                // Preço badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .background(TravelGreen, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "R$ ${String.format(Locale.getDefault(), "%.2f", product.price)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TravelOnPrimary
                    )
                }
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título
                Text(
                    text = product.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TravelGreenDark
                )

                // Tags info row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoChip(icon = "✈️", label = "Pacote Turístico")
                    InfoChip(icon = "⭐", label = "Destaque")
                }

                // Divider
                HorizontalDivider(color = TravelGreenLight.copy(alpha = 0.4f))

                // Descrição
                Text(
                    text = "Sobre este pacote",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TravelBlueDark
                )
                Text(
                    text = product.description,
                    fontSize = 14.sp,
                    color = TravelOnSurface.copy(alpha = 0.75f),
                    lineHeight = 22.sp
                )

                // Detalhes card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TravelBlueLight.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Informações do Pacote", fontWeight = FontWeight.Bold, color = TravelBlueDark, fontSize = 15.sp)
                        DetailRow(icon = "🆔", label = "Código", value = "#${product.id}")
                        DetailRow(icon = "💰", label = "Valor", value = "R$ ${String.format(Locale.getDefault(), "%.2f", product.price)}")
                        DetailRow(icon = "📦", label = "Categoria", value = product.title.take(20))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botão reservar
                Button(
                    onClick = { /* futuro: reservar */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TravelGreen
                    )
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reservar Agora", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TravelGray),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TravelGreenLight)
                ) {
                    Text("Voltar para lista", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun InfoChip(icon: String, label: String) {
    Row(
        modifier = Modifier
            .background(TravelGreenLight.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(icon, fontSize = 12.sp)
        Text(label, fontSize = 12.sp, color = TravelGreenDark, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DetailRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(icon, fontSize = 14.sp)
            Text(label, fontSize = 13.sp, color = TravelGray)
        }
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TravelOnSurface)
    }
}
