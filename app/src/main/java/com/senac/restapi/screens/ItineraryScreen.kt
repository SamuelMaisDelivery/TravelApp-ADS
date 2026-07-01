package com.senac.restapi.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.senac.restapi.ui.theme.*
import com.senac.restapi.viewmodel.ItineraryState
import com.senac.restapi.viewmodel.ItineraryTripInfo
import com.senac.restapi.viewmodel.ItineraryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Domain models ─────────────────────────────────────────────────────────────

private enum class SectionType(
    val displayName: String,
    val emoji: String,
    val color: Color,
    val bgColor: Color
) {
    MANHA("Manhã",  "☀️", Color(0xFFE65100), Color(0xFFFFF3E0)),
    ALMOCO("Almoço", "🍽️", Color(0xFF2E7D32), Color(0xFFE8F5E9)),
    TARDE("Tarde",  "🌤️", Color(0xFF01579B), Color(0xFFE1F5FE)),
    JANTAR("Jantar", "🍷", Color(0xFF4A148C), Color(0xFFF3E5F5)),
    NOITE("Noite",  "🌙", Color(0xFF0D47A1), Color(0xFFE8EAF6))
}

private data class DaySection(val type: SectionType, val rawContent: String)
private data class ItineraryDay(val header: String, val sections: List<DaySection>)
private data class ParsedItinerary(val days: List<ItineraryDay>, val summary: String)

// ── Parser ────────────────────────────────────────────────────────────────────

private fun String.clean(): String =
    replace(Regex("#{1,6}\\s*"), "")
        .replace(Regex("\\*{1,3}([^*\n]+)\\*{1,3}"), "$1")
        .replace(Regex("_+([^_\n]+)_+"), "$1")
        .trim()

private val BULLET_REGEX = Regex("^[*\\-•]\\s")
private val BULLET_PREFIX = Regex("^[*\\-•]\\s+")
private val BOLD_REGEX = Regex("\\*{1,3}([^*\n]+)\\*{1,3}")
private val DAY_HEADER_REGEX = Regex("^Dia\\s+\\d+", RegexOption.IGNORE_CASE)
private val SUMMARY_REGEX = Regex("^Resumo", RegexOption.IGNORE_CASE)
private val SECTION_PATTERNS = linkedMapOf(
    Regex("^Manh[aã]$", RegexOption.IGNORE_CASE) to SectionType.MANHA,
    Regex("^Almo[cç]o$", RegexOption.IGNORE_CASE) to SectionType.ALMOCO,
    Regex("^Tarde$", RegexOption.IGNORE_CASE) to SectionType.TARDE,
    Regex("^Jantar$", RegexOption.IGNORE_CASE) to SectionType.JANTAR,
    Regex("^Noite$", RegexOption.IGNORE_CASE) to SectionType.NOITE
)

private fun parseItinerary(raw: String): ParsedItinerary {
    val days = mutableListOf<ItineraryDay>()
    val summaryBuilder = StringBuilder()
    var currentDayHeader = ""
    var currentSections = mutableListOf<DaySection>()
    var currentSectionType: SectionType? = null
    var currentContent = StringBuilder()
    var inSummary = false

    fun flushSection() {
        val content = currentContent.toString().trimEnd()
        if (currentSectionType != null && content.isNotEmpty()) {
            currentSections.add(DaySection(currentSectionType!!, content))
        }
        currentContent = StringBuilder()
        currentSectionType = null
    }

    fun flushDay() {
        flushSection()
        if (currentDayHeader.isNotEmpty() && currentSections.isNotEmpty()) {
            days.add(ItineraryDay(currentDayHeader, currentSections.toList()))
        }
        currentSections = mutableListOf()
        currentDayHeader = ""
    }

    for (line in raw.lines()) {
        val cleaned = line.clean()
        when {
            DAY_HEADER_REGEX.containsMatchIn(cleaned) -> {
                flushDay()
                currentDayHeader = cleaned
                inSummary = false
            }
            SUMMARY_REGEX.containsMatchIn(cleaned) && !inSummary -> {
                flushDay()
                inSummary = true
                summaryBuilder.append(cleaned).append("\n")
            }
            inSummary -> summaryBuilder.append(cleaned).append("\n")
            else -> {
                val matched = SECTION_PATTERNS.entries.firstOrNull { it.key.matches(cleaned) }
                if (matched != null && currentDayHeader.isNotEmpty()) {
                    flushSection()
                    currentSectionType = matched.value
                } else if (currentSectionType != null) {
                    currentContent.append(cleaned).append("\n")
                }
            }
        }
    }
    flushDay()

    return ParsedItinerary(days, summaryBuilder.toString().trim())
}

// ── Main Screen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryScreen(
    tripId: Int,
    itineraryViewModel: ItineraryViewModel,
    onNavigateBack: () -> Unit
) {
    val state by itineraryViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(tripId) { itineraryViewModel.generateItinerary(tripId) }
    DisposableEffect(Unit) { onDispose { itineraryViewModel.reset() } }

    val isSuccess = state is ItineraryState.Success
    val isError = state is ItineraryState.Error

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🗺️", fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Roteiro da Viagem", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TravelOnPrimary)
                    }
                },
                actions = {
                    if (isSuccess || isError) {
                        IconButton(onClick = { itineraryViewModel.retry(tripId) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Regerar", tint = TravelOnPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TravelGreen,
                    titleContentColor = TravelOnPrimary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TravelBackground)
                .padding(innerPadding)
        ) {
            when (val s = state) {
                is ItineraryState.Idle -> Unit
                is ItineraryState.Loading -> LoadingContent()
                is ItineraryState.Success -> {
                    val parsed = remember(s.itinerary) { parseItinerary(s.itinerary) }
                    ItineraryContent(
                        tripInfo = s.tripInfo,
                        parsed = parsed,
                        onRetry = { itineraryViewModel.retry(tripId) }
                    )
                }
                is ItineraryState.Error -> ErrorContent(
                    message = s.message,
                    onRetry = { itineraryViewModel.retry(tripId) }
                )
            }
        }
    }
}

// ── States ────────────────────────────────────────────────────────────────────

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = TravelGreen, strokeWidth = 3.dp, modifier = Modifier.size(60.dp))
        Spacer(Modifier.height(28.dp))
        Text(
            text = "Criando seu roteiro personalizado",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TravelGreenDark,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "A IA está planejando cada detalhe da sua viagem. Isso pode levar alguns segundos.",
            fontSize = 14.sp,
            color = TravelGray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("☀️", "🍽️", "🌤️", "🍷", "🌙").forEach { emoji ->
                Surface(shape = CircleShape, color = TravelGreen.copy(alpha = 0.1f)) {
                    Text(emoji, fontSize = 20.sp, modifier = Modifier.padding(10.dp))
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(shape = CircleShape, color = TravelError.copy(alpha = 0.1f)) {
            Text("⚠️", fontSize = 36.sp, modifier = Modifier.padding(20.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Não foi possível gerar o roteiro",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TravelError
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = TravelGray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = TravelGreen),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Tentar novamente", fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Success content ───────────────────────────────────────────────────────────

@Composable
private fun ItineraryContent(
    tripInfo: ItineraryTripInfo,
    parsed: ParsedItinerary,
    onRetry: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TripHeaderCard(
                tripInfo = tripInfo,
                dayCount = parsed.days.size,
                dateFormat = dateFormat
            )
        }

        if (parsed.days.isEmpty() && parsed.summary.isEmpty()) {
            item { RawFallbackCard(text = "O Gemini retornou um formato inesperado. Tente novamente.", onRetry = onRetry) }
        } else {
            itemsIndexed(parsed.days, key = { i, _ -> i }) { index, day ->
                DayCard(day = day, dayIndex = index)
            }
            if (parsed.summary.isNotEmpty()) {
                item { SummaryCard(content = parsed.summary) }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ── Trip header card ──────────────────────────────────────────────────────────

@Composable
private fun TripHeaderCard(
    tripInfo: ItineraryTripInfo,
    dayCount: Int,
    dateFormat: SimpleDateFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(TravelGreen, TravelGreenDark))
                )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tripInfo.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TravelOnPrimary,
                            lineHeight = 28.sp
                        )
                        if (tripInfo.city.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = TravelOnPrimary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = tripInfo.city,
                                    fontSize = 14.sp,
                                    color = TravelOnPrimary.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = TravelOnPrimary.copy(alpha = 0.18f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$dayCount",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = TravelOnPrimary
                            )
                            Text(
                                text = if (dayCount == 1) "dia" else "dias",
                                fontSize = 11.sp,
                                color = TravelOnPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = TravelOnPrimary.copy(alpha = 0.2f))
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date range
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = TravelOnPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = TravelOnPrimary.copy(alpha = 0.85f),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = dateFormat.format(Date(tripInfo.startDate)),
                                fontSize = 12.sp,
                                color = TravelOnPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "  →  ",
                                fontSize = 12.sp,
                                color = TravelOnPrimary.copy(alpha = 0.7f)
                            )
                            Text(
                                text = dateFormat.format(Date(tripInfo.endDate)),
                                fontSize = 12.sp,
                                color = TravelOnPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Trip type chip
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = TravelOnPrimary.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (tripInfo.tripType == "Lazer") Icons.Default.BeachAccess
                                              else Icons.Default.BusinessCenter,
                                contentDescription = null,
                                tint = TravelOnPrimary.copy(alpha = 0.85f),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = tripInfo.tripType,
                                fontSize = 12.sp,
                                color = TravelOnPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = "✨ Gerado com Gemini AI",
                    fontSize = 11.sp,
                    color = TravelOnPrimary.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

// ── Day card ──────────────────────────────────────────────────────────────────

@Composable
private fun DayCard(day: ItineraryDay, dayIndex: Int) {
    var expanded by remember { mutableStateOf(true) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(300),
        label = "chevron"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(tween(300)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TravelCardBg),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        // Colored day indicator strip + header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clickable { expanded = !expanded }
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(listOf(TravelGreen, TravelGreenDark)),
                        RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(TravelGreen.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${dayIndex + 1}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TravelGreenDark
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = day.header,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TravelGreenDark,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Recolher" else "Expandir",
                    tint = TravelGray,
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(chevronRotation)
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(200)) + expandVertically(tween(300)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(250))
        ) {
            Column(
                modifier = Modifier.padding(start = 22.dp, end = 16.dp, bottom = 16.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                day.sections.forEach { section -> SectionBlock(section = section) }
            }
        }
    }
}

// ── Section block ─────────────────────────────────────────────────────────────

@Composable
private fun SectionBlock(section: DaySection) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(section.type.bgColor)
            .padding(12.dp)
    ) {
        // Section header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(section.type.emoji, fontSize = 16.sp)
            Text(
                text = section.type.displayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = section.type.color
            )
        }

        if (section.rawContent.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            SectionContentRows(content = section.rawContent, accentColor = section.type.color)
        }
    }
}

// ── Content rows ──────────────────────────────────────────────────────────────

@Composable
private fun SectionContentRows(content: String, accentColor: Color) {
    val lines = remember(content) {
        content.lines().filter { it.isNotBlank() }
    }

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        lines.forEach { line ->
            val isBullet = BULLET_REGEX.containsMatchIn(line)
            val cleanText = line
                .replace(BULLET_PREFIX, "")
                .let { BOLD_REGEX.replace(it, "$1") }
                .trim()

            if (cleanText.isEmpty()) return@forEach

            if (isBullet) {
                BulletRow(text = cleanText, accentColor = accentColor)
            } else {
                // Sub-header (e.g. activity name in bold from Gemini)
                Text(
                    text = cleanText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun BulletRow(text: String, accentColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(6.dp)
                .background(accentColor.copy(alpha = 0.7f), CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFF424242),
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

// ── Summary card ──────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TravelCardBg),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(TravelGreen.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = TravelGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Resumo da Viagem",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TravelGreenDark
                )
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = TravelGreenLight.copy(alpha = 0.5f))
            Spacer(Modifier.height(14.dp))
            SectionContentRows(content = content, accentColor = TravelGreen)
        }
    }
}

// ── Raw fallback ──────────────────────────────────────────────────────────────

@Composable
private fun RawFallbackCard(text: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TravelCardBg),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🤔", fontSize = 36.sp)
            Spacer(Modifier.height(12.dp))
            Text(text, fontSize = 14.sp, color = TravelGray, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onRetry,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TravelGreen)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Tentar novamente")
            }
        }
    }
}
