package com.example.tsumaps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
val allFoodItems = listOf(
    "Блины", "Кофе", "Шаурма", "Бизнес-ланч", "Пицца",
    "Салат", "Сок", "Бургер", "Суп", "Чай"
)

val allPlaces = listOf(
    Place("Сибирские блины",  1, 420, 310, listOf("Блины", "Чай"),                   480, 1320),
    Place("Старбукс (главный)", 2, 390, 280, listOf("Кофе", "Чай"),                  480, 1320),
    Place("Столовая №1",      3, 460, 340, listOf("Бизнес-ланч", "Суп", "Салат"),    480, 1320),
    Place("Кафе Минутка",     4, 430, 320, listOf("Бизнес-ланч", "Салат", "Сок"),    480, 1320),
    Place("Безумно",          5, 500, 360, listOf("Шаурма", "Сок"),                  600, 1380),
    Place("Абрикос",          6, 480, 350, listOf("Бизнес-ланч", "Сок", "Чай"),      480, 1320),
    Place("Rostic's",         7, 550, 380, listOf("Бургер", "Кофе", "Сок"),          600, 1380),
    Place("Ярче",             8, 410, 300, listOf("Салат", "Сок", "Чай", "Пицца"),   480, 1320),
    Place("Гастроном НАШ",    9, 460, 330, listOf("Салат", "Сок", "Пицца"),          480, 1380),
    Place("Пекарня XO",      10, 400, 290, listOf("Кофе", "Чай", "Бизнес-ланч"),     480, 1320)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneticAlgorithmScreen(onBack: () -> Unit) {
    val tsuBlue = colorResource(id = R.color.tsu_blue_primary)
    val selectedFood = remember { mutableStateListOf<String>() }
    var bestIndividual by remember { mutableStateOf<Individual?>(null) }
    var isRunning by remember { mutableStateOf(false) }
    var currentGeneration by remember { mutableStateOf(0) }
    var statusText by remember { mutableStateOf("Выберите блюда и нажмите «Запустить»") }
    val coroutineScope = rememberCoroutineScope()

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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text("Что хотите съесть?", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            FoodSelector(
                allItems = allFoodItems,
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
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = tsuBlue
                        )
                        Text("Поколение: $currentGeneration", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            bestIndividual?.let { ind ->
                Text("Лучший маршрут:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Расстояние: ~${ind.totalDistance} м", fontSize = 14.sp, color = Color.Gray)
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(ind.path) { place ->
                        RouteStopCard(place = place, tsuBlue = tsuBlue)
                    }
                }
            } ?: Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (selectedFood.isEmpty()) {
                        statusText = "Сначала выберите хотя бы одно блюдо!"
                        return@Button
                    }
                    isRunning = true
                    bestIndividual = null
                    currentGeneration = 0
                    statusText = "Алгоритм работает..."

                    coroutineScope.launch {
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
                                best = ga.execute(10)
                                withContext(Dispatchers.Main) {
                                    currentGeneration = (batch + 1) * 10
                                    bestIndividual = best
                                }
                            }
                            best
                        }
                        isRunning = false
                        statusText = "Готово! Найден оптимальный маршрут."
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
fun FoodSelector(
    allItems: List<String>,
    selected: List<String>,
    onToggle: (String) -> Unit
) {
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
                            .border(
                                width = 1.5.dp,
                                color = tsuBlue,
                                shape = RoundedCornerShape(8.dp)
                            )
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
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun RouteStopCard(place: com.example.tsumaps.algorithms.genetic.Place, tsuBlue: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FF)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                Text(
                    "Меню: ${place.menu.joinToString(", ")}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
