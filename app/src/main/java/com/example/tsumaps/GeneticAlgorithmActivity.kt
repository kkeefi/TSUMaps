package com.example.tsumaps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsumaps.algorithms.genetic.GeneticAlgorithm
import com.example.tsumaps.algorithms.genetic.Individual
import com.example.tsumaps.algorithms.genetic.Place
import com.example.tsumaps.ui.theme.TSUMapsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GeneticAlgorithmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TSUMapsTheme {
                GeneticAlgorithmScreen(onBack = { finish() })
            }
        }
    }
}

val breakfastItems = listOf("Кофе", "Чай", "Блины", "Пирожное", "Сырники", "Булочка")

val lunchItems = listOf(
    "Кофе", "Чай", "Газировка", "Сок", "Компот", "Блины", "Пирожное", "Сырники",
    "Хот дог", "Сэндвич", "Суп", "Салат", "Гарнир с мясом", "Бизнес ланч",
    "Шаурма", "Картошка фри", "Батончик", "Булочка", "Наггетсы", "Бургер", "Пласт. посуда"
)

val dinnerItems = listOf(
    "Чай", "Газировка", "Сок", "Компот", "Блины", "Пирожное", "Сырники",
    "Хот дог", "Сэндвич", "Суп", "Салат", "Гарнир с мясом", "Шаурма",
    "Картошка фри", "Батончик", "Булочка", "Наггетсы", "Бургер", "Пластиковая посуда"
)

val allPlaces = listOf(
    Place("Сибирские блины",    1, 420, 310, listOf("Блины", "Кофе", "Чай", "Газировка"),                          480, 1320),
    Place("Старбукс (главный)", 2, 390, 280, listOf("Кофе", "Чай", "Пирожное", "Сырники", "Хот дог", "Сэндвич"),   480, 1320),
    Place("Столовая №1",        3, 460, 340, listOf("Суп", "Салат", "Чай", "Гарнир с мясом", "Компот"),             480, 1320),
    Place("Кафе Минутка",       4, 430, 320, listOf("Бизнес ланч", "Салат", "Суп", "Гарнир с мясом", "Компот"),     480, 1320),
    Place("Безумно",            5, 500, 360, listOf("Шаурма", "Сок", "Газировка", "Картошка фри"),                  600, 1380),
    Place("Абрикос",            6, 480, 350, listOf("Сок", "Газировка", "Салат", "Батончик", "Булочка"),             480, 1320),
    Place("Rostic's",           7, 550, 380, listOf("Бургер", "Кофе", "Газировка", "Картошка фри", "Наггетсы"),     600, 1380),
    Place("Ярче",               8, 410, 300, listOf("Сок", "Газировка", "Батончик", "Булочка", "Пластиковая посуда"), 480, 1320),
    Place("Гастроном НАШ",      9, 460, 330, listOf("Пластиковая посуда", "Булочка", "Батончик", "Газировка", "Сок"), 480, 1380),
    Place("Пекарня XO",        10, 400, 290, listOf("Чай", "Кофе", "Булочка"),                                       480, 1320)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneticAlgorithmScreen(onBack: () -> Unit) {
    val tsuBlue = colorResource(id = R.color.tsu_blue_primary)
    var selectedMealType by remember { mutableStateOf("Обед") }
    val selectedFood = remember { mutableStateListOf<String>() }
    var bestIndividual by remember { mutableStateOf<Individual?>(null) }
    var isRunning by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Выберите блюда и нажмите «Запустить»") }
    val coroutineScope = rememberCoroutineScope()

    val currentMenu = when (selectedMealType) {
        "Завтрак" -> breakfastItems
        "Ужин" -> dinnerItems
        else -> lunchItems
    }

    LaunchedEffect(selectedMealType) {
        selectedFood.clear()
        bestIndividual = null
        statusText = "Выберите блюда и нажмите «Запустить»"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Генетический алгоритм", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tsuBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Выберите приём пищи:", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Завтрак", "Обед", "Ужин").forEach { type ->
                    ElevatedFilterChip(
                        selected = selectedMealType == type,
                        onClick = { selectedMealType = type },
                        label = { Text(type) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider()

            Text("Что хотите съесть? ($selectedMealType)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            FoodSelector(
                allItems = currentMenu,
                selected = selectedFood,
                onToggle = { item ->
                    if (selectedFood.contains(item)) selectedFood.remove(item)
                    else selectedFood.add(item)
                }
            )

            HorizontalDivider()

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(statusText, fontSize = 14.sp, color = Color.DarkGray)
                    if (isRunning) {
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = tsuBlue)
                    }
                }
            }

            bestIndividual?.let { ind ->
                if (!isRunning) {
                    Text("Лучший маршрут:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (ind.finalRoute.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Text(
                                "Ни одно заведение с выбранными блюдами сейчас не работает",
                                modifier = Modifier.padding(16.dp),
                                color = Color(0xFFC62828),
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Text("Расстояние: ~${ind.totalDistance} м", fontSize = 14.sp, color = Color.Gray)
                        ind.finalRoute.forEach { place ->
                            RouteStopCard(place = place, tsuBlue = tsuBlue)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (selectedFood.isEmpty()) {
                        statusText = "Сначала выберите хотя бы одно блюдо!"
                        return@Button
                    }
                    isRunning = true
                    bestIndividual = null
                    statusText = "Алгоритм работает..."

                    coroutineScope.launch {
                        val calendar = java.util.Calendar.getInstance()
                        val currentMinutes = calendar.get(java.util.Calendar.HOUR_OF_DAY) * 60 +
                                calendar.get(java.util.Calendar.MINUTE)

                        val result = withContext(Dispatchers.Default) {
                            val ga = GeneticAlgorithm(
                                allPlaces = allPlaces,
                                neededFood = selectedFood.toList(),
                                populationSize = 50,
                                mutationProbability = 0.05,
                                startX = 450,
                                startY = 320
                            )
                            var best: Individual? = null
                            for (batch in 0 until 10) {
                                best = ga.execute(10, currentMinutes)
                                withContext(Dispatchers.Main) {
                                    bestIndividual = best
                                }
                            }
                            best
                        }

                        isRunning = false
                        statusText = if (result?.finalRoute?.isEmpty() == true) {
                            "Маршрут не найден: все подходящие места закрыты."
                        } else {
                            "Готово! Найден оптимальный маршрут."
                        }
                        bestIndividual = result
                    }
                },
                enabled = !isRunning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tsuBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isRunning) "Работает..." else "Запустить алгоритм", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun FoodSelector(allItems: List<String>, selected: List<String>, onToggle: (String) -> Unit) {
    val tsuBlue = colorResource(id = R.color.tsu_blue_primary)
    val rows = allItems.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { item ->
                    val isSelected = selected.contains(item)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(
                                color = if (isSelected) tsuBlue else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(1.5.dp, tsuBlue, RoundedCornerShape(8.dp))
                            .clickable { onToggle(item) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else tsuBlue,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
                repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun RouteStopCard(place: Place, tsuBlue: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FF)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(tsuBlue, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("📍", fontSize = 16.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(place.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text("Закрывается: ${formatTime(place.closeTime)}", fontSize = 12.sp, color = Color(0xFFD32F2F))
                Text("Меню: ${place.menu.joinToString(", ")}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

fun formatTime(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return String.format("%02d:%02d", h, m)
}
