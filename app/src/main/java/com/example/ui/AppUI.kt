package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.NumberTranslator
import com.example.viewmodel.*
import kotlinx.coroutines.delay
import kotlin.random.Random

// Vibrant pastel colors for cards with custom borders and text matching the Vibrant Palette spec
data class LanguageCardData(
    val id: String,
    val flag: String,
    val sampleNumber: String,
    val nativeName: String,
    val englishName: String,
    val bgColor: Color,
    val borderColor: Color,
    val textColor: Color
)

val LanguagesData = listOf(
    LanguageCardData("Bangla", "🇧🇩", "১২", "বাংলা", "Bangla", Color(0xFFF1FBF5), Color(0xFFD1EBDD), Color(0xFF006A4E)),
    LanguageCardData("English", "🇺🇸", "12", "English", "Latin Script", Color(0xFFF0F4FF), Color(0xFFD9E2FF), Color(0xFF1A73E8)),
    LanguageCardData("Arabic", "🇸🇦", "١٢", "عربي", "Arabic", Color(0xFFFFF8F0), Color(0xFFFFE4CC), Color(0xFFFF9933)),
    LanguageCardData("Hindi", "🇮🇳", "१२", "हिंदी", "Hindi", Color(0xFFFDF2F2), Color(0xFFFCE2E2), Color(0xFFE53935))
)

data class ActivityCardData(
    val tag: String,
    val title: String,
    val bgColor: Color,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isDark by viewModel.darkMode.collectAsState()
    val sizeMultiplier by viewModel.fontSizeMultiplier.collectAsState()

    // Base custom typography
    val customTypography = Typography(
        headlineLarge = MaterialTheme.typography.headlineLarge.copy(
            fontSize = (32 * sizeMultiplier).sp,
            fontWeight = FontWeight.Bold
        ),
        titleLarge = MaterialTheme.typography.titleLarge.copy(
            fontSize = (22 * sizeMultiplier).sp,
            fontWeight = FontWeight.SemiBold
        ),
        bodyLarge = MaterialTheme.typography.bodyLarge.copy(
            fontSize = (16 * sizeMultiplier).sp
        )
    )

    MaterialTheme(
        colorScheme = if (isDark) darkColorScheme() else lightColorScheme(
            primary = Color(0xFF4A3AFF),
            secondary = Color(0xFF06B6D4),
            background = Color(0xFFFAF9F6),
            surface = Color.White
        ),
        typography = customTypography
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    is Screen.Splash -> SplashScreen(
                        onGetStarted = { viewModel.navigateTo(Screen.Welcome) },
                        isDark = isDark
                    )
                    is Screen.Welcome -> WelcomeScreen(
                        onNext = { viewModel.navigateTo(Screen.Home) }
                    )
                    is Screen.Home -> HomeScreen(viewModel = viewModel)
                    is Screen.LanguageSection -> LanguageSectionScreen(
                        viewModel = viewModel,
                        language = screen.language
                    )
                    is Screen.CategoryDetail -> CategoryDetailScreen(
                        viewModel = viewModel,
                        language = screen.language,
                        category = screen.category
                    )
                    is Screen.NumberDetail -> NumberDetailScreen(
                        viewModel = viewModel,
                        number = screen.number,
                        language = screen.language
                    )
                    is Screen.Counting -> CountingScreen(viewModel = viewModel)
                    is Screen.Tracing -> TracingScreen(viewModel = viewModel)
                    is Screen.QuizMenu -> QuizMenuScreen(viewModel = viewModel)
                    is Screen.QuizActive -> QuizActiveScreen(viewModel = viewModel, type = screen.type)
                    is Screen.GamesMenu -> GamesMenuScreen(viewModel = viewModel)
                    is Screen.GameActive -> GameActiveScreen(viewModel = viewModel, gameType = screen.gameType)
                    is Screen.FlashCards -> FlashCardsScreen(viewModel = viewModel)
                    is Screen.Progress -> ProgressScreen(viewModel = viewModel)
                    is Screen.Settings -> SettingsScreen(viewModel = viewModel)
                    is Screen.NumberStories -> NumberStoriesScreen(viewModel = viewModel)
                    is Screen.ParentsDashboard -> ParentsDashboardScreen(viewModel = viewModel)
                    is Screen.About -> AboutScreen(viewModel = viewModel)
                }
            }
        }
    }
}

// ---------------- SPLASH SCREEN ----------------

@Composable
fun SplashScreen(onGetStarted: () -> Unit, isDark: Boolean) {
    var startPulse by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startPulse) 1.1f else 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    LaunchedEffect(Unit) {
        startPulse = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = if (isDark) listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    else listOf(Color(0xFFEEF2F6), Color(0xFFE2E8F0))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Pulsing infinity logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Color(0xFF4A3AFF).copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.AllInclusive,
                    contentDescription = "Infinity Logo",
                    tint = Color(0xFF4A3AFF),
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Infinity Number",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color.White else Color(0xFF1E293B),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Learn Numbers Offline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF06B6D4),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Learn Numbers in 4 Languages",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) Color.LightGray else Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = Color(0xFF4A3AFF),
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("get_started_splash"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A3AFF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Get Started", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------------- WELCOME SCREEN ----------------

@Composable
fun WelcomeScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Hero banner image
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_numbers_banner),
                    contentDescription = "Numbers learning illustration",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Your Offline Number Partner",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Learn counting, spelling, tracing, and mathematics in Bangla (বাংলা), English, Hindi (हिंदी), and Arabic (العربية) completely offline!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("welcome_next"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Enter Dashboard", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ---------------- HOME SCREEN ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val stats by viewModel.userStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Infinity Number",
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4A3AFF),
                            fontSize = 22.sp
                        )
                        Text(
                            "4 Languages • Offline Platform".uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Settings) },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF4A3AFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            // Stats quick view banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .shadow(2.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circular progress gauge
                        Box(
                            modifier = Modifier.size(56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val progress = 0.65f // 65% as shown in design HTML
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Background Track
                                drawCircle(
                                    color = Color(0xFFE2E8F0),
                                    style = Stroke(width = 4.dp.toPx())
                                )
                                // Foreground Progress
                                drawArc(
                                    color = Color(0xFF4A3AFF),
                                    startAngle = -90f,
                                    sweepAngle = 360f * progress,
                                    useCenter = false,
                                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Daily Goal".uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            val learnedCount = stats?.totalLearned ?: 12
                            val goalCount = 20
                            Text(
                                "$learnedCount/$goalCount Numbers Learned",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                "Keep going! 🚀",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF16A34A) // text-green-600
                            )
                        }

                        // Display Streak Day
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFECE3))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                "${stats?.streakDays ?: 1} Days",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE53935)
                            )
                        }
                    }
                }
            }

            // Supported languages section
            item {
                Text(
                    text = "Select Language",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .height(260.dp)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(LanguagesData) { item ->
                        Card(
                            onClick = { viewModel.navigateTo(Screen.LanguageSection(item.id)) },
                            modifier = Modifier
                                .fillMaxHeight()
                                .testTag("lang_card_${item.id.lowercase()}"),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(2.dp, item.borderColor),
                            colors = CardDefaults.cardColors(containerColor = item.bgColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(item.flag, fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.sampleNumber,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = item.textColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.nativeName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = item.englishName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            // Learning & fun activities section
            item {
                Text(
                    text = "Interactive Activities",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            val utilities = listOf(
                ActivityCardData("Quiz Active", "📝 Quiz Mode", Color(0xFFECE9FF)) { viewModel.navigateTo(Screen.QuizMenu) },
                ActivityCardData("Games Active", "🎮 Game Room", Color(0xFFD6F6FF)) { viewModel.navigateTo(Screen.GamesMenu) },
                ActivityCardData("Counting Active", "🍎 Object Counting", Color(0xFFFFF0F5)) { viewModel.navigateTo(Screen.Counting) },
                ActivityCardData("Tracing Active", "✍️ Number Tracing", Color(0xFFE2F9EC)) { viewModel.navigateTo(Screen.Tracing) },
                ActivityCardData("Flash Cards", "🎴 Flash Cards", Color(0xFFFFF6D6)) { viewModel.navigateTo(Screen.FlashCards) },
                ActivityCardData("Number Stories", "📖 Number Stories", Color(0xFFFFECE3)) { viewModel.navigateTo(Screen.NumberStories) },
                ActivityCardData("Progress Tracking", "📊 Progress & Badges", Color(0xFFE8F5E9)) { viewModel.navigateTo(Screen.Progress) },
                ActivityCardData("Parents Board", "👨‍👩‍👦 Parents Hub", Color(0xFFECEFF1)) { viewModel.navigateTo(Screen.ParentsDashboard) }
            )

            gridItems(
                data = utilities,
                columnCount = 2,
                horizontalSpacing = 12.dp,
                verticalSpacing = 12.dp
            ) { item ->
                Card(
                    onClick = item.onClick,
                    modifier = Modifier
                        .height(100.dp)
                        .testTag(item.tag),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = item.bgColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = item.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// Helper to make grids inside LazyColumns easily
fun <T> LazyListScope.gridItems(
    data: List<T>,
    columnCount: Int,
    horizontalSpacing: androidx.compose.ui.unit.Dp,
    verticalSpacing: androidx.compose.ui.unit.Dp,
    itemContent: @Composable (T) -> Unit
) {
    val rows = data.chunked(columnCount)
    items(rows) { rowItems ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = verticalSpacing / 2),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
        ) {
            rowItems.forEach { item ->
                Box(modifier = Modifier.weight(1f)) {
                    itemContent(item)
                }
            }
            // Fill empty cells if the last row is incomplete
            if (rowItems.size < columnCount) {
                for (i in 0 until (columnCount - rowItems.size)) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ---------------- LANGUAGE SECTION SCREEN ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSectionScreen(viewModel: MainViewModel, language: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$language Section", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Select Category",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val categories = listOf(
                "0–9",
                "10–20",
                "21–50",
                "51–100",
                "101–1000",
                "1000+",
                "Ordinal Numbers",
                "Roman Numbers",
                "Large Numbers"
            )

            categories.forEach { category ->
                Card(
                    onClick = { viewModel.navigateTo(Screen.CategoryDetail(language, category)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("category_card_${category.replace(" ", "_").lowercase()}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(category, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Open")
                    }
                }
            }
        }
    }
}

// ---------------- CATEGORY DETAIL SCREEN ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(viewModel: MainViewModel, language: String, category: String) {
    // Generate ranges based on selection
    val numbersList = remember {
        when (category) {
            "0–9" -> (0..9).toList()
            "10–20" -> (10..20).toList()
            "21–50" -> (21..50).toList()
            "51–100" -> (51..100).toList()
            "101–1000" -> (101..1000).toList()
            "1000+" -> (1001..1100).toList() + listOf(1200, 1300, 1400, 1500, 2000, 5000, 10000, 50000, 100000)
            "Ordinal Numbers" -> (1..100).toList()
            "Roman Numbers" -> (1..100).toList()
            "Large Numbers" -> listOf(100000, 1000000, 10000000, 1000000000)
            else -> emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (category == "Ordinal Numbers" || category == "Roman Numbers" || category == "Large Numbers") {
            // Special Lists for Ordinals/Roman/Large
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                items(numbersList) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4F46E5)
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                when (category) {
                                    "Ordinal Numbers" -> {
                                        Text(NumberTranslator.getOrdinal(item, language), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                        Text(NumberTranslator.getOrdinal(item, "english"), fontSize = 12.sp, color = Color.Gray)
                                    }
                                    "Roman Numbers" -> {
                                        Text(NumberTranslator.getRomanNumeral(item), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                    "Large Numbers" -> {
                                        Text(NumberTranslator.getLargeNumberTerm(item.toLong(), language), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        Text(NumberTranslator.getLargeNumberTerm(item.toLong(), "english"), fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Standard dynamic grid of numbers
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(numbersList) { number ->
                    Card(
                        onClick = {
                            viewModel.viewNumber(number, language)
                            viewModel.navigateTo(Screen.NumberDetail(number, language))
                        },
                        modifier = Modifier
                            .aspectRatio(1f)
                            .shadow(2.dp, RoundedCornerShape(16.dp))
                            .testTag("num_cell_$number"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = NumberTranslator.toLocalizedDigits(number, language),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF4F46E5)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = number.toString(),
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- NUMBER DETAIL SCREEN ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberDetailScreen(viewModel: MainViewModel, number: Int, language: String) {
    val favorites by viewModel.favorites.collectAsState()
    val isFav = favorites.any { it.number == number && it.language == language }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleFavorite(number, language) },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (isFav) Color(0xFFF59E0B) else Color.Gray
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Big styled number visual card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .shadow(4.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = NumberTranslator.toLocalizedDigits(number, language),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF4F46E5)
                    )
                    Text(
                        text = "Standard: $number",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action: Play sound voice pronunciation
            Button(
                onClick = { viewModel.speak(NumberTranslator.toWords(number, language), language) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("play_sound_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Pronounce")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🔊 Play Sound", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Multi-language Translation dictionary block
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Language Spellings", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))

                    val trans = listOf(
                        Quadruple("English", "🇺🇸", NumberTranslator.toWords(number, "english"), "english"),
                        Quadruple("Bangla", "🇧🇩", NumberTranslator.toWords(number, "bangla"), "bangla"),
                        Quadruple("Arabic", "🇸🇦", NumberTranslator.toWords(number, "arabic"), "arabic"),
                        Quadruple("Hindi", "🇮🇳", NumberTranslator.toWords(number, "hindi"), "hindi")
                    )

                    trans.forEach { translation ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(translation.second, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(translation.first, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(translation.third, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text(
                                    "Pronunciation: ${NumberTranslator.getPronunciation(number, translation.fourth)}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }

            // Quick navigation slider / buttons to change number
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        if (number > 0) {
                            viewModel.navigateTo(Screen.NumberDetail(number - 1, language))
                        }
                    },
                    enabled = number > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Prev")
                    Text("Prev", color = Color.DarkGray)
                }

                Button(
                    onClick = {
                        viewModel.navigateTo(Screen.NumberDetail(number + 1, language))
                    }
                ) {
                    Text("Next")
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next")
                }
            }
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// ---------------- OBJECT COUNTING SCREEN ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountingScreen(viewModel: MainViewModel) {
    var rangeSelected by remember { mutableStateOf("1–10") }
    var currentAnswer by remember { mutableStateOf(-1) }
    var currentQuestionCount by remember { mutableStateOf(1) }
    var score by remember { mutableStateOf(0) }
    var randomTarget by remember { mutableStateOf(3) }
    var choices by remember { mutableStateOf(listOf<Int>()) }

    fun generateCountingQuestion() {
        val maxLimit = when (rangeSelected) {
            "1–10" -> 10
            "1–20" -> 20
            "1–50" -> 50
            else -> 100
        }
        val target = Random.nextInt(1, maxLimit + 1)
        randomTarget = target

        val options = mutableListOf(target)
        while (options.size < 4) {
            val wrong = target + Random.nextInt(-4, 5)
            if (wrong > 0 && wrong <= maxLimit && !options.contains(wrong)) {
                options.add(wrong)
            }
        }
        options.shuffle()
        choices = options
        currentAnswer = -1
    }

    LaunchedEffect(rangeSelected) {
        currentQuestionCount = 1
        score = 0
        generateCountingQuestion()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Object Counting Practice", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Range selector tabs
            val tabs = listOf("1–10", "1–20", "1–50", "1–100")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                tabs.forEach { tab ->
                    val isSelected = rangeSelected == tab
                    Card(
                        onClick = { rangeSelected = tab },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF4F46E5) else Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                tab,
                                color = if (isSelected) Color.White else Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Game progress indicator
            Text("Question $currentQuestionCount of 10 • Score: $score", fontSize = 14.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            // Counting graphics display grid
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .shadow(2.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    // Draw target number of items (e.g. Hearts)
                    val iconsGridSize = if (randomTarget > 25) 6 else if (randomTarget > 12) 5 else 4
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(iconsGridSize),
                        userScrollEnabled = false,
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.Center
                    ) {
                        items(randomTarget) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Heart item",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(36.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("How many items do you see? Tap your answer:", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // Multiple choice buttons
            choices.forEach { choice ->
                val isAnswered = currentAnswer != -1
                val isCorrect = choice == randomTarget
                val isSelected = choice == currentAnswer

                val btnColor = if (isAnswered) {
                    if (isCorrect) Color(0xFF10B981)
                    else if (isSelected) Color(0xFFEF4444)
                    else Color.LightGray
                } else {
                    Color(0xFF4F46E5)
                }

                Button(
                    onClick = {
                        if (!isAnswered) {
                            currentAnswer = choice
                            if (choice == randomTarget) {
                                score++
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .height(48.dp)
                        .testTag("choice_$choice"),
                    colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(choice.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (currentAnswer != -1) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (currentQuestionCount >= 10) {
                            // Finish round, restart
                            currentQuestionCount = 1
                            score = 0
                        } else {
                            currentQuestionCount++
                        }
                        generateCountingQuestion()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4))
                ) {
                    Text(if (currentQuestionCount >= 10) "Restart Practice" else "Next Question")
                }
            }
        }
    }
}

// ---------------- TRACING PRACTICE SCREEN ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracingScreen(viewModel: MainViewModel) {
    var selectedTraceNum by remember { mutableStateOf(0) }
    val pathPoints = remember { mutableStateListOf<Offset>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Write & Trace Numbers", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Select digit to trace (0-9)
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed((0..9).toList()) { idx, digit ->
                    val isSelected = selectedTraceNum == digit
                    Card(
                        onClick = {
                            selectedTraceNum = digit
                            pathPoints.clear()
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF4F46E5) else Color.White
                        ),
                        modifier = Modifier.size(50.dp).testTag("trace_select_$digit")
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                digit.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (isSelected) Color.White else Color.Black
                            )
                        }
                    }
                }
            }

            Text("Trace inside the dotted guide lines:", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas tracing area with dotted background text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(2.dp, Color.LightGray, RoundedCornerShape(24.dp))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                pathPoints.add(offset)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                pathPoints.add(change.position)
                            }
                        )
                    }
            ) {
                // Background guide number (Dotted)
                Text(
                    text = selectedTraceNum.toString(),
                    fontSize = 240.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.LightGray.copy(alpha = 0.4f),
                    modifier = Modifier.align(Alignment.Center)
                )

                // Tracing Strokes drawing canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (pathPoints.isNotEmpty()) {
                        val strokePath = Path().apply {
                            moveTo(pathPoints.first().x, pathPoints.first().y)
                            for (i in 1 until pathPoints.size) {
                                lineTo(pathPoints[i].x, pathPoints[i].y)
                            }
                        }
                        drawPath(
                            path = strokePath,
                            color = Color(0xFFEC4899),
                            style = Stroke(width = 16f, cap = StrokeCap.Round)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { pathPoints.clear() },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear")
                }

                Button(
                    onClick = {
                        pathPoints.clear()
                    },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("trace_finish_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Done")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Check Tracing")
                }
            }
        }
    }
}

// ---------------- QUIZ SYSTEM SCREENS ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizMenuScreen(viewModel: MainViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose a Quiz Theme", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            val quizzes = listOf(
                Pair(QuizType.MCQ, "📝 Multiple Choice Spelling"),
                Pair(QuizType.MISSING_NUMBER, "❓ Missing Number Sequences"),
                Pair(QuizType.ARRANGE_NUMBERS, "📈 Ascending Order Arranging"),
                Pair(QuizType.COMPARE_NUMBERS, "⚖️ Compare Numbers (<, >, =)"),
                Pair(QuizType.EVEN_ODD, "🔢 Even & Odd identification"),
                Pair(QuizType.ARITHMETIC, "🧮 Simple Arithmetic Math")
            )

            items(quizzes) { (type, label) ->
                Card(
                    onClick = { viewModel.startQuiz(type) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("quiz_choice_${type.name.lowercase()}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start")
                    }
                }
            }

            item {
                Text(
                    text = "Practice Tables (2 to 20)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
                )
            }

            item {
                var selectedTable by remember { mutableStateOf(2) }
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Grid of Table select
                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(2),
                        modifier = Modifier.height(110.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items((2..20).toList()) { tabNum ->
                            val isSel = tabNum == selectedTable
                            Card(
                                onClick = { selectedTable = tabNum },
                                modifier = Modifier.size(45.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSel) Color(0xFF4F46E5) else Color.White
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        tabNum.toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else Color.Black
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Display table grid
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Multiplication Table of $selectedTable", fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))
                            Spacer(modifier = Modifier.height(8.dp))
                            (1..10).forEach { mul ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("$selectedTable × $mul", fontWeight = FontWeight.Medium)
                                    Text("= ${selectedTable * mul}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizActiveScreen(viewModel: MainViewModel, type: QuizType) {
    val qCount by viewModel.quizQuestionCount.collectAsState()
    val score by viewModel.quizScore.collectAsState()

    // Retrieve active questions
    val mcqQuestion by viewModel.activeMcq.collectAsState()
    val missingQuestion by viewModel.activeMissing.collectAsState()
    val arrangeQuestion by viewModel.activeArrange.collectAsState()
    val compareQuestion by viewModel.activeCompare.collectAsState()
    val evenOddQuestion by viewModel.activeEvenOdd.collectAsState()
    val arithmeticQuestion by viewModel.activeArithmetic.collectAsState()

    var isAnswered by remember { mutableStateOf(false) }
    var selectedAnswerIdx by remember { mutableStateOf(-1) }
    var chosenOrderList = remember { mutableStateListOf<Int>() }

    fun handleAnswer(correct: Boolean) {
        isAnswered = true
        viewModel.answerQuiz(correct, type)
    }

    LaunchedEffect(qCount) {
        isAnswered = false
        selectedAnswerIdx = -1
        chosenOrderList.clear()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${type.name.replace("_", " ")} Quiz", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Home) }) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (qCount >= 10) {
                // Quiz completed finish display card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                        .shadow(4.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Success",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Quiz Completed!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Your score: $score out of 10", fontSize = 18.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.startQuiz(type) },
                            modifier = Modifier.fillMaxWidth().testTag("restart_quiz_btn")
                        ) {
                            Text("Try Again")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.navigateTo(Screen.Home) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Text("Back to Home")
                        }
                    }
                }
            } else {
                Text("Question ${qCount + 1} of 10", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = (qCount + 1) / 10f,
                    modifier = Modifier.fillMaxWidth().clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(24.dp))

                when (type) {
                    QuizType.MCQ -> {
                        mcqQuestion?.let { q ->
                            Text(q.text, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(24.dp))
                            q.options.forEachIndexed { idx, opt ->
                                val correct = idx == q.correctIndex
                                val isSelected = idx == selectedAnswerIdx
                                val cardColor = if (isAnswered) {
                                    if (correct) Color(0xFF10B981) else if (isSelected) Color(0xFFEF4444) else Color.White
                                } else Color.White

                                Card(
                                    onClick = {
                                        if (!isAnswered) {
                                            selectedAnswerIdx = idx
                                            handleAnswer(correct)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).testTag("mcq_opt_$idx"),
                                    colors = CardDefaults.cardColors(containerColor = cardColor),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        opt,
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                    QuizType.MISSING_NUMBER -> {
                        missingQuestion?.let { q ->
                            Text("Find the missing number in sequence:", fontSize = 16.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                q.sequence.forEach { item ->
                                    Card(
                                        modifier = Modifier.size(50.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (item == "?") Color(0xFF06B6D4) else Color.LightGray
                                        )
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text(item, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            q.options.forEach { opt ->
                                val correct = opt == q.correctAnswer
                                val isSelected = opt == selectedAnswerIdx.toString()
                                val cardColor = if (isAnswered) {
                                    if (correct) Color(0xFF10B981) else if (isSelected) Color(0xFFEF4444) else Color.White
                                } else Color.White

                                Card(
                                    onClick = {
                                        if (!isAnswered) {
                                            selectedAnswerIdx = opt.toIntOrNull() ?: 0
                                            handleAnswer(correct)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardColor)
                                ) {
                                    Text(
                                        opt,
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                    QuizType.ARRANGE_NUMBERS -> {
                        arrangeQuestion?.let { q ->
                            Text("Tap the numbers in Ascending Order:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                q.numbers.forEach { num ->
                                    val isPicked = chosenOrderList.contains(num)
                                    Card(
                                        onClick = {
                                            if (!isPicked) {
                                                chosenOrderList.add(num)
                                                // If all picked, check order
                                                if (chosenOrderList.size == q.numbers.size) {
                                                    val isCorrect = chosenOrderList == q.correctSorted
                                                    handleAnswer(isCorrect)
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(60.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isPicked) Color.LightGray else Color(0xFF4F46E5)
                                        )
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text(num.toString(), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Your Selection: ${chosenOrderList.joinToString(" → ")}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    QuizType.COMPARE_NUMBERS -> {
                        compareQuestion?.let { q ->
                            Text("Compare the values:", fontSize = 16.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Card(modifier = Modifier.size(70.dp)) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(q.num1.toString(), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                    }
                                }
                                Text(" ? ", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 16.dp))
                                Card(modifier = Modifier.size(70.dp)) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(q.num2.toString(), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            val syms = listOf("<", "=", ">")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                syms.forEach { sym ->
                                    val correct = sym == q.correctRelation
                                    val isSelected = sym == selectedAnswerIdx.toString()
                                    val btnColor = if (isAnswered) {
                                        if (correct) Color(0xFF10B981) else Color(0xFFEF4444)
                                    } else Color(0xFF4F46E5)

                                    Button(
                                        onClick = {
                                            if (!isAnswered) {
                                                selectedAnswerIdx = sym.hashCode()
                                                handleAnswer(correct)
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(56.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = btnColor)
                                    ) {
                                        Text(sym, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    QuizType.EVEN_ODD -> {
                        evenOddQuestion?.let { q ->
                            Text("Is this number Even or Odd?", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(q.number.toString(), fontSize = 64.sp, fontWeight = FontWeight.Black, color = Color(0xFF06B6D4))

                            Spacer(modifier = Modifier.height(32.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(
                                    onClick = { if (!isAnswered) handleAnswer(q.isEven) },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isAnswered && q.isEven) Color(0xFF10B981) else Color(0xFF4F46E5)
                                    )
                                ) {
                                    Text("Even", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { if (!isAnswered) handleAnswer(!q.isEven) },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isAnswered && !q.isEven) Color(0xFF10B981) else Color(0xFFEC4899)
                                    )
                                ) {
                                    Text("Odd", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    QuizType.ARITHMETIC -> {
                        arithmeticQuestion?.let { q ->
                            Text("Solve the expression:", fontSize = 16.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(q.expr, fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color(0xFF4F46E5))

                            Spacer(modifier = Modifier.height(32.dp))

                            q.options.forEach { opt ->
                                val correct = opt == q.correctAnswer
                                val isSelected = opt == selectedAnswerIdx
                                val cardColor = if (isAnswered) {
                                    if (correct) Color(0xFF10B981) else if (isSelected) Color(0xFFEF4444) else Color.White
                                } else Color.White

                                Card(
                                    onClick = {
                                        if (!isAnswered) {
                                            selectedAnswerIdx = opt
                                            handleAnswer(correct)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardColor)
                                ) {
                                    Text(
                                        opt.toString(),
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- GAME ROOM SCREEN ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesMenuScreen(viewModel: MainViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interactive Games Room", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Fun Educational Games",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Unlock your Game King Badge by matching all pairs!", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.startMemoryGame() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .testTag("memory_game_start_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
            ) {
                Text("🧠 Number Memory Match", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.navigateTo(Screen.GameActive(GameType.BALLOON_POP)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
            ) {
                Text("🎈 Balloon Pop Challenge", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameActiveScreen(viewModel: MainViewModel, gameType: GameType) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(gameType.name.replace("_", " "), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Home) }) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (gameType) {
                GameType.MEMORY_GAME -> {
                    val cards by viewModel.memoryCards.collectAsState()
                    val matches by viewModel.memoryMatches.collectAsState()

                    Text("Pairs Matched: $matches of 6", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (matches == 6) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "Win", tint = Color(0xFFF59E0B), modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Congratulations!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text("You matched all the memory cards!", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                                Button(onClick = { viewModel.startMemoryGame() }) {
                                    Text("Play Again")
                                }
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            itemsIndexed(cards) { idx, card ->
                                val isFlipped = card.isFlipped || card.isMatched
                                Card(
                                    onClick = { viewModel.flipMemoryCard(idx) },
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .shadow(1.dp, RoundedCornerShape(12.dp))
                                        .testTag("mem_card_$idx"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isFlipped) Color(0xFFE2F0D9) else Color(0xFF4F46E5)
                                    )
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        if (isFlipped) {
                                            Text(
                                                card.display,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(4.dp)
                                            )
                                        } else {
                                            Text("?", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                GameType.BALLOON_POP -> {
                    var poppedCount by remember { mutableStateOf(0) }
                    var currentTarget by remember { mutableStateOf(Random.nextInt(1, 10)) }
                    val balloonOptions = remember(currentTarget) {
                        (1..3).map {
                            if (it == 1) currentTarget
                            else {
                                var randVal = Random.nextInt(1, 10)
                                while (randVal == currentTarget) {
                                    randVal = Random.nextInt(1, 10)
                                }
                                randVal
                            }
                        }.shuffled()
                    }

                    Text("Pop the Balloon displaying: $currentTarget", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Score: $poppedCount", color = Color.Gray)

                    Spacer(modifier = Modifier.height(40.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        balloonOptions.forEachIndexed { index, number ->
                            Card(
                                onClick = {
                                    if (number == currentTarget) {
                                        poppedCount++
                                        currentTarget = Random.nextInt(1, 10)
                                    }
                                },
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .testTag("balloon_$index"),
                                colors = CardDefaults.cardColors(
                                    containerColor = when (index) {
                                        0 -> Color(0xFFEC4899)
                                        1 -> Color(0xFF3B82F6)
                                        else -> Color(0xFF10B981)
                                    }
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        number.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                    Button(onClick = { viewModel.navigateTo(Screen.Home) }) {
                        Text("Finish Game")
                    }
                }
                else -> {}
            }
        }
    }
}

// ---------------- FLASH CARDS SCREEN ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashCardsScreen(viewModel: MainViewModel) {
    val activeList = remember { (1..10).toList() }
    var currentIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interactive Study Cards", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Card ${currentIndex + 1} of 10 • Tap Card to Flip!", fontSize = 14.sp, fontWeight = FontWeight.Bold)

            val number = activeList[currentIndex]

            // Study Card with Flip Motion Simulation
            Card(
                onClick = { isFlipped = !isFlipped },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(vertical = 16.dp)
                    .shadow(4.dp, RoundedCornerShape(24.dp))
                    .testTag("flashcard_study_surface"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFlipped) Color(0xFFFFF0F5) else Color(0xFFECE9FF)
                )
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (!isFlipped) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(number.toString(), fontSize = 80.sp, fontWeight = FontWeight.Black, color = Color(0xFF4F46E5))
                            Text("Standard Digit", fontSize = 14.sp, color = Color.Gray)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(NumberTranslator.toWords(number, "english"), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEC4899))
                            Text("English Spelling", fontSize = 13.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Bangla: " + NumberTranslator.toWords(number, "bangla"), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text("Hindi: " + NumberTranslator.toWords(number, "hindi"), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text("Arabic: " + NumberTranslator.toWords(number, "arabic"), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Cards Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        if (currentIndex > 0) {
                            currentIndex--
                            isFlipped = false
                        }
                    },
                    enabled = currentIndex > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Prev")
                    Text("Prev", color = Color.DarkGray)
                }

                Button(
                    onClick = {
                        if (currentIndex < 9) {
                            currentIndex++
                            isFlipped = false
                        }
                    },
                    enabled = currentIndex < 9
                ) {
                    Text("Next")
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next")
                }
            }
        }
    }
}

// ---------------- PROGRESS & BADGES CABINET SCREEN ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(viewModel: MainViewModel) {
    val stats by viewModel.userStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress & Statistics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // High scores card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Active Streak", fontSize = 13.sp, color = Color.Gray)
                        Text("${stats?.streakDays ?: 0} Days", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Quiz High Score", fontSize = 13.sp, color = Color.Gray)
                        Text("${stats?.quizHighScore ?: 0} / 10", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Achievement Cabinet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            val bitmask = stats?.badgeUnlockBitmask ?: 0
            val badgesList = listOf(
                BadgeData(1, "First Steps", "Unlocked by studying your very first number!", Icons.Default.DirectionsRun),
                BadgeData(2, "Counting Master", "Learned 20 or more unique numbers!", Icons.Default.Grade),
                BadgeData(4, "Quiz Champion", "Scored 8 or more in any quiz mode!", Icons.Default.EmojiEvents),
                BadgeData(8, "Game King", "Finished the memory match challenges!", Icons.Default.SportsEsports)
            )

            badgesList.forEach { badge ->
                val isUnlocked = (bitmask and badge.id) != 0
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = badge.icon,
                            contentDescription = badge.name,
                            tint = if (isUnlocked) Color(0xFF2E7D32) else Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = badge.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isUnlocked) Color(0xFF2E7D32) else Color.Black
                            )
                            Text(badge.desc, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

data class BadgeData(val id: Int, val name: String, val desc: String, val icon: ImageVector)

// ---------------- PARENTS DASHBOARD SCREEN ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentsDashboardScreen(viewModel: MainViewModel) {
    val count by viewModel.parentsWorksheetsCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parents Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("Learning Activity Report", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Monitor your child's math progress and print direct customized worksheets.", fontSize = 13.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Subject Proficiency", fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Counting Practice: 85% Completed")
                    Text("Spelling Quiz: 70% Completed")
                    Text("Addition & Operations: 60% Completed")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Printable Worksheet Generator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Creates beautiful templates dynamically for pencil practice.", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.generateWorksheet() },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("generate_worksheet_btn")
            ) {
                Text("Generate Custom Trace Worksheet")
            }

            if (count > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE2F0D9))
                ) {
                    Text(
                        "Generated $count Worksheets! Your file has been cached to local memory for printing.",
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF375623)
                    )
                }
            }
        }
    }
}

// ---------------- NUMBER STORIES SCREEN ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberStoriesScreen(viewModel: MainViewModel) {
    val stories = listOf(
        StoryData(
            "The Great Zero Hero",
            "Zero used to feel empty, thinking he had no value. But when he paired up with number One, they became Ten! Zero realized that when helping friends, he can multiply their strength infinity times!",
            "0"
        ),
        StoryData(
            "Three Little Kittens count",
            "Once, three little kittens got lost. To find each other, they sang: One meow, Two meow, Three meow! When they heard all three counting meows, they grouped back happily!",
            "3"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Number Stories", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            items(stories) { story ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(story.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))
                            Text(story.numChar, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(story.text, fontSize = 14.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}

data class StoryData(val title: String, val text: String, val numChar: String)

// ---------------- SETTINGS SCREEN ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val isDark by viewModel.darkMode.collectAsState()
    val isSound by viewModel.soundEnabled.collectAsState()
    val scaleMultiplier by viewModel.fontSizeMultiplier.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            // Dark Mode switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dark Theme Mode", fontWeight = FontWeight.Medium)
                Switch(
                    checked = isDark,
                    onCheckedChange = { viewModel.darkMode.value = it },
                    modifier = Modifier.testTag("dark_mode_switch")
                )
            }

            HorizontalDivider()

            // Sound switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sound On/Off (TTS Audio)", fontWeight = FontWeight.Medium)
                Switch(
                    checked = isSound,
                    onCheckedChange = { viewModel.soundEnabled.value = it },
                    modifier = Modifier.testTag("sound_switch")
                )
            }

            HorizontalDivider()

            // Font scale multiplier slider
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text("Study Font Size", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                    value = scaleMultiplier,
                    onValueChange = { viewModel.fontSizeMultiplier.value = it },
                    valueRange = 0.8f..1.2f,
                    modifier = Modifier.testTag("font_size_slider")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Small", fontSize = 11.sp, color = Color.Gray)
                    Text("Standard", fontSize = 11.sp, color = Color.Gray)
                    Text("Large", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Data Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.resetProgress() },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("reset_progress_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Reset All Study Progress", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("About App", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                onClick = { viewModel.navigateTo(Screen.About) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_app_nav_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF)),
                border = BorderStroke(1.dp, Color(0xFFD9E2FF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4A3AFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "About Developer",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Prince AR Abdur Rahman", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text("Tap to view Developer & Company Info", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Open About Screen", tint = Color.Gray)
                }
            }
        }
    }
}

// ---------------- ABOUT DEVELOPER & COMPANY SCREEN ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(viewModel: MainViewModel) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Developer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // DEVELOPER CARD (Bangla Palette Mapping - 0xFFF1FBF5 background, 0xFFD1EBDD border, 0xFF006A4E text)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, Color(0xFFD1EBDD)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1FBF5))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF006A4E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "PA",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Prince AR Abdur Rahman",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF006A4E)
                            )
                            Text(
                                text = "Independent App Developer",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF006A4E).copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Independent App Developer passionate about building modern Android applications, productivity tools, AI-powered experiences, media players, educational apps, and next-generation digital products.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFD1EBDD).copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Contact & Socials",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006A4E)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Contact Row 1 (WhatsApp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { uriHandler.openUri("https://wa.me/8801707424006") }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "WhatsApp 1",
                            tint = Color(0xFF006A4E),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "WhatsApp: 01707424006",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B)
                        )
                    }

                    // Contact Row 2 (WhatsApp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { uriHandler.openUri("https://wa.me/8801796951709") }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "WhatsApp 2",
                            tint = Color(0xFF006A4E),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "WhatsApp: 01796951709",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B)
                        )
                    }

                    // Contact Row 3 (Facebook)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { uriHandler.openUri("https://www.facebook.com/share/1BNn32qoJo/") }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Facebook",
                            tint = Color(0xFF006A4E),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Facebook Profile",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B)
                        )
                    }

                    // Contact Row 4 (Instagram)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { uriHandler.openUri("https://www.instagram.com/ur___abdur____rahman__2008") }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Instagram",
                            tint = Color(0xFF006A4E),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Instagram Profile",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B)
                        )
                    }
                }
            }

            // COMPANY CARD (Hindi Palette Mapping - 0xFFFDF2F2 background, 0xFFFCE2E2 border, 0xFFE53935 text)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, Color(0xFFFCE2E2)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF2F2))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = "Company Icon",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "About Company",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE53935)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "NexVora Lab's Ofc",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "NexVora Lab's Ofc focuses on creating innovative Android applications designed to improve productivity, entertainment, learning, and digital experiences.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Mission: Build fast, beautiful, privacy-friendly, and user-focused applications accessible to everyone.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE53935)
                    )
                }
            }

            // TECHNICAL INFORMATION (Arabic Palette Mapping - 0xFFFFF8F0 background, 0xFFFFE4CC border, 0xFFFF9933 text)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, Color(0xFFFFE4CC)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F0))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Technical Info Icon",
                            tint = Color(0xFFFF9933),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Technical Information",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF9933)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("App Version", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                        Text("1.0.0", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Build Platform", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                        Text("Offline Native (Kotlin/Compose)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }
                }
            }

            // CREDITS CARD (English Palette Mapping - 0xFFF0F4FF background, 0xFFD9E2FF border, 0xFF1A73E8 text)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, Color(0xFFD9E2FF)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Credits",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A73E8)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Developed by Prince AR Abdur Rahman",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Published by NexVora Lab's Ofc",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "© 2026 NexVora Lab's Ofc. All Rights Reserved.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
