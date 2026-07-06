package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

sealed interface Screen {
    object Splash : Screen
    object Welcome : Screen
    object Home : Screen
    data class LanguageSection(val language: String) : Screen
    data class CategoryDetail(val language: String, val category: String) : Screen
    data class NumberDetail(val number: Int, val language: String) : Screen
    object Counting : Screen
    object Tracing : Screen
    object QuizMenu : Screen
    data class QuizActive(val type: QuizType) : Screen
    object GamesMenu : Screen
    data class GameActive(val gameType: GameType) : Screen
    object FlashCards : Screen
    object Progress : Screen
    object Settings : Screen
    object NumberStories : Screen
    object ParentsDashboard : Screen
    object About : Screen
}

enum class QuizType {
    MCQ, MISSING_NUMBER, ARRANGE_NUMBERS, COMPARE_NUMBERS, EVEN_ODD, ARITHMETIC
}

enum class GameType {
    DRAG_DROP, COUNT_OBJECTS, BALLOON_POP, MEMORY_GAME
}

// ---------------- QUIZ & GAME QUESTIONS ----------------

data class McqQuestion(
    val number: Int,
    val text: String,
    val options: List<String>,
    val correctIndex: Int
)

data class MissingNumberQuestion(
    val sequence: List<String>, // e.g. ["2", "4", "?", "8", "10"]
    val correctAnswer: String,
    val options: List<String>
)

data class ArrangeQuestion(
    val numbers: List<Int>,
    val correctSorted: List<Int>
)

data class CompareQuestion(
    val num1: Int,
    val num2: Int,
    val correctRelation: String // "<", ">", "="
)

data class EvenOddQuestion(
    val number: Int,
    val isEven: Boolean
)

data class ArithmeticQuestion(
    val expr: String, // e.g. "5 + 3"
    val correctAnswer: Int,
    val options: List<Int>
)

data class MemoryCard(
    val id: Int,
    val value: Int,
    val display: String, // Digit, Word, or Illustration
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

class MainViewModel(private val repository: NumberRepository) : ViewModel() {

    // --- NAVIGATION ---
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val navigationHistory = mutableListOf<Screen>()

    fun navigateTo(screen: Screen) {
        navigationHistory.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun goBack(): Boolean {
        if (navigationHistory.isNotEmpty()) {
            _currentScreen.value = navigationHistory.removeAt(navigationHistory.size - 1)
            return true
        }
        return false
    }

    // --- SOUND & PREFERENCES ---
    val soundEnabled = MutableStateFlow(true)
    val darkMode = MutableStateFlow(false)
    val fontSizeMultiplier = MutableStateFlow(1.0f) // 0.8f, 1.0f, 1.2f

    // --- ROOM reactive state flows ---
    val favorites: StateFlow<List<FavoriteNumber>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recents: StateFlow<List<RecentNumber>> = repository.recents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userStats: StateFlow<UserStats?> = repository.stats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- SOUND PLAYBACK FLOW ---
    private val _speakEvent = MutableSharedFlow<Pair<String, Locale>>(extraBufferCapacity = 1)
    val speakEvent = _speakEvent.asSharedFlow()

    fun speak(text: String, language: String) {
        if (!soundEnabled.value) return
        val locale = when (language.lowercase()) {
            "bangla" -> Locale("bn", "BD")
            "hindi" -> Locale("hi", "IN")
            "arabic" -> Locale("ar")
            else -> Locale.US
        }
        viewModelScope.launch {
            _speakEvent.tryEmit(Pair(text, locale))
        }
    }

    // --- FAVORITES TOGGLE ---
    fun toggleFavorite(number: Int, language: String) {
        viewModelScope.launch {
            repository.toggleFavorite(number, language)
        }
    }

    // --- RECORD RECENT VIEW ---
    fun viewNumber(number: Int, language: String) {
        viewModelScope.launch {
            repository.addRecent(number, language)
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.resetProgress()
        }
    }

    // --- DAILY CHALLENGE STATE ---
    private val _dailyChallengeNumbers = MutableStateFlow<List<Int>>(emptyList())
    val dailyChallengeNumbers: StateFlow<List<Int>> = _dailyChallengeNumbers.asStateFlow()

    init {
        // Generate a pseudo-random list of 10 numbers based on current calendar day
        val today = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        val rand = Random(today.toLong())
        val list = mutableSetOf<Int>()
        while (list.size < 10) {
            list.add(rand.nextInt(1, 200))
        }
        _dailyChallengeNumbers.value = list.sorted()
    }

    // --- QUIZ GAME LOGIC ---
    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizQuestionCount = MutableStateFlow(0)
    val quizQuestionCount: StateFlow<Int> = _quizQuestionCount.asStateFlow()

    // Active questions state
    val activeMcq = MutableStateFlow<McqQuestion?>(null)
    val activeMissing = MutableStateFlow<MissingNumberQuestion?>(null)
    val activeArrange = MutableStateFlow<ArrangeQuestion?>(null)
    val activeCompare = MutableStateFlow<CompareQuestion?>(null)
    val activeEvenOdd = MutableStateFlow<EvenOddQuestion?>(null)
    val activeArithmetic = MutableStateFlow<ArithmeticQuestion?>(null)

    fun startQuiz(type: QuizType) {
        _quizScore.value = 0
        _quizQuestionCount.value = 0
        generateNextQuizQuestion(type)
        navigateTo(Screen.QuizActive(type))
    }

    fun answerQuiz(correct: Boolean, type: QuizType) {
        _quizQuestionCount.value++
        if (correct) {
            _quizScore.value++
        }
        if (_quizQuestionCount.value >= 10) {
            // Quiz completed
            viewModelScope.launch {
                repository.updateQuizScore(_quizScore.value)
            }
        } else {
            generateNextQuizQuestion(type)
        }
    }

    private fun generateNextQuizQuestion(type: QuizType) {
        val rand = Random.Default
        when (type) {
            QuizType.MCQ -> {
                val num = rand.nextInt(1, 100)
                val isToEnglish = rand.nextBoolean()
                val correctName = NumberTranslator.toWords(num, "english")
                val options = mutableListOf(correctName)
                while (options.size < 4) {
                    val wrongNum = rand.nextInt(1, 100)
                    val wrongName = NumberTranslator.toWords(wrongNum, "english")
                    if (!options.contains(wrongName)) options.add(wrongName)
                }
                options.shuffle()
                activeMcq.value = McqQuestion(
                    number = num,
                    text = if (isToEnglish) "Which number is $correctName?" else "What is the English spelling of $num?",
                    options = options,
                    correctIndex = options.indexOf(correctName)
                )
            }
            QuizType.MISSING_NUMBER -> {
                val step = rand.nextInt(1, 5)
                val start = rand.nextInt(1, 20)
                val seq = (0 until 5).map { start + it * step }
                val missingIndex = rand.nextInt(1, 4)
                val missingVal = seq[missingIndex]
                val seqDisplay = seq.mapIndexed { idx, v -> if (idx == missingIndex) "?" else v.toString() }

                val options = mutableListOf(missingVal.toString())
                while (options.size < 4) {
                    val wrongVal = rand.nextInt(1, 50)
                    if (wrongVal != missingVal && !options.contains(wrongVal.toString())) {
                        options.add(wrongVal.toString())
                    }
                }
                options.shuffle()
                activeMissing.value = MissingNumberQuestion(
                    sequence = seqDisplay,
                    correctAnswer = missingVal.toString(),
                    options = options
                )
            }
            QuizType.ARRANGE_NUMBERS -> {
                val list = mutableSetOf<Int>()
                while (list.size < 4) {
                    list.add(rand.nextInt(1, 50))
                }
                val rawList = list.toList()
                val sorted = rawList.sorted()
                activeArrange.value = ArrangeQuestion(rawList, sorted)
            }
            QuizType.COMPARE_NUMBERS -> {
                val num1 = rand.nextInt(1, 50)
                val num2 = rand.nextInt(1, 50)
                val relation = if (num1 < num2) "<" else if (num1 > num2) ">" else "="
                activeCompare.value = CompareQuestion(num1, num2, relation)
            }
            QuizType.EVEN_ODD -> {
                val num = rand.nextInt(1, 100)
                activeEvenOdd.value = EvenOddQuestion(num, num % 2 == 0)
            }
            QuizType.ARITHMETIC -> {
                val op = rand.nextInt(0, 4) // 0: +, 1: -, 2: *, 3: /
                var num1 = 1
                var num2 = 1
                var symbol = "+"
                var ans = 2

                when (op) {
                    0 -> { // Add
                        num1 = rand.nextInt(1, 30)
                        num2 = rand.nextInt(1, 30)
                        symbol = "+"
                        ans = num1 + num2
                    }
                    1 -> { // Sub
                        num1 = rand.nextInt(10, 50)
                        num2 = rand.nextInt(1, num1)
                        symbol = "-"
                        ans = num1 - num2
                    }
                    2 -> { // Mul
                        num1 = rand.nextInt(2, 10)
                        num2 = rand.nextInt(1, 10)
                        symbol = "×"
                        ans = num1 * num2
                    }
                    3 -> { // Div
                        num2 = rand.nextInt(2, 10)
                        ans = rand.nextInt(1, 10)
                        num1 = num2 * ans
                        symbol = "÷"
                    }
                }

                val options = mutableListOf(ans)
                while (options.size < 4) {
                    val wrong = ans + rand.nextInt(-5, 6)
                    if (wrong >= 0 && wrong != ans && !options.contains(wrong)) {
                        options.add(wrong)
                    }
                }
                options.shuffle()
                activeArithmetic.value = ArithmeticQuestion("$num1 $symbol $num2", ans, options)
            }
        }
    }

    // --- GAME LOGIC ---
    val memoryCards = MutableStateFlow<List<MemoryCard>>(emptyList())
    val memoryMatches = MutableStateFlow(0)
    val gameActiveStatus = MutableStateFlow(false)

    fun startMemoryGame() {
        val rand = Random.Default
        val selectedNumbers = mutableSetOf<Int>()
        while (selectedNumbers.size < 6) {
            selectedNumbers.add(rand.nextInt(1, 20))
        }

        val cardsList = mutableListOf<MemoryCard>()
        var id = 0
        selectedNumbers.forEach { num ->
            // Card 1: Digit Representation (e.g. "5")
            cardsList.add(MemoryCard(id++, num, num.toString()))
            // Card 2: Bangla spelling representation (e.g. "পাচ")
            cardsList.add(MemoryCard(id++, num, NumberTranslator.toWords(num, "bangla")))
        }
        cardsList.shuffle()
        memoryCards.value = cardsList
        memoryMatches.value = 0
        gameActiveStatus.value = true
        navigateTo(Screen.GameActive(GameType.MEMORY_GAME))
    }

    private var firstFlippedCardIdx: Int? = null

    fun flipMemoryCard(index: Int) {
        val list = memoryCards.value.toMutableList()
        val card = list[index]
        if (card.isFlipped || card.isMatched) return

        card.isFlipped = true
        memoryCards.value = list

        val firstIdx = firstFlippedCardIdx
        if (firstIdx == null) {
            firstFlippedCardIdx = index
        } else {
            // Second card flipped
            val firstCard = list[firstIdx]
            if (firstCard.value == card.value) {
                // Match!
                firstCard.isMatched = true
                card.isMatched = true
                memoryMatches.value += 1
                firstFlippedCardIdx = null
                viewModelScope.launch {
                    if (memoryMatches.value == 6) {
                        repository.unlockGameBadge()
                    }
                }
            } else {
                // No match, turn back both after brief delay handled in UI or coroutine
                firstFlippedCardIdx = null
                viewModelScope.launch {
                    kotlinx.coroutines.delay(1000)
                    val updatedList = memoryCards.value.toMutableList()
                    updatedList[firstIdx].isFlipped = false
                    updatedList[index].isFlipped = false
                    memoryCards.value = updatedList
                }
            }
        }
    }

    // --- SEARCH QUERY ---
    val searchQuery = MutableStateFlow("")
    val searchResults = searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            flow {
                if (query.isEmpty()) {
                    emit(emptyList<Int>())
                    return@flow
                }
                val list = mutableSetOf<Int>()
                val isNum = query.toIntOrNull()
                if (isNum != null && isNum in 0..100000) {
                    list.add(isNum)
                } else {
                    // Match words offline in 4 languages
                    val qClean = query.trim().lowercase()
                    for (i in 0..100) {
                        val wordEn = NumberTranslator.toWords(i, "english").lowercase()
                        val wordBn = NumberTranslator.toWords(i, "bangla").lowercase()
                        val wordHi = NumberTranslator.toWords(i, "hindi").lowercase()
                        val wordAr = NumberTranslator.toWords(i, "arabic").lowercase()
                        if (wordEn.contains(qClean) || wordBn.contains(qClean) || wordHi.contains(qClean) || wordAr.contains(qClean)) {
                            list.add(i)
                        }
                    }
                }
                emit(list.toList().sorted())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- PARENTS DASHBOARD SIMULATION DATA ---
    val parentsWorksheetsCount = MutableStateFlow(0)
    fun generateWorksheet() {
        parentsWorksheetsCount.value++
    }
}

class MainViewModelFactory(private val repository: NumberRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
