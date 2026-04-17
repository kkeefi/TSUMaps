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
import com.example.tsumaps.algorithms.ant_colony.AntColony
import com.example.tsumaps.algorithms.ant_colony.AntPlace
import com.example.tsumaps.ui.theme.TSUMapsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class AntColonyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TSUMapsTheme {
                AntColonyScreen(onBack = { finish() })
            }
        }
    }
}

val campusAttractions = listOf(
    AntPlace("Главный корпус ТГУ",    450.0, 320.0),
    AntPlace("Научная библиотека",     420.0, 290.0),
    AntPlace("Университетская роща",   500.0, 280.0),
    AntPlace("Памятник Крылову",       460.0, 310.0),
    AntPlace("Ботанический сад",       380.0, 350.0),
    AntPlace("Второй корпус",          490.0, 340.0),
    AntPlace("Спортивный комплекс",    530.0, 360.0),
    AntPlace("Студенческий клуб",      440.0, 330.0),
    AntPlace("Физический факультет",   410.0, 305.0),
    AntPlace("Химический корпус",      475.0, 355.0)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AntColonyScreen(onBack: () -> Unit) {
    val tsuBlue = colorResource(id = R.color.tsu_blue_primary)
    val selected = remember { mutableStateListOf<AntPlace>() }
    var bestRoute by remember { mutableStateOf<List<AntPlace>>(emptyList()) }
    var bestDistance by remember { mutableStateOf(0.0) }
    var isRunning by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Выберите достопримечательности для обхода") }
    var iteration by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Муравьиный алгоритм", color = Color.White) },
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
            Text("Выберите точки маршрута:", fontSize = 18.sp, fontWeight = FontWeight.Bold)

            campusAttractions.forEach { place ->
                val isSelected = selected.contains(place)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isSelected) Color(0xFFE8F0FE) else Color(0xFFF8F9FF),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) tsuBlue else Color(0xFFDDDDDD),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            if (isSelected) selected.remove(place) else selected.add(place)
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        place.name,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) tsuBlue else Color.DarkGray
                    )
                    if (isSelected) {
                        Text("✓", color = tsuBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

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
                        Text("Итерация: $iteration / 100", fontSize = 12.sp, color = Color.Gray)
                    }
                    if (bestRoute.isNotEmpty() && !isRunning) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Длина маршрута: ${"%.0f".format(bestDistance)} ед.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = tsuBlue
                        )
                    }
                }
            }

            if (bestRoute.isNotEmpty()) {
                Text("Оптимальный маршрут:", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                bestRoute.forEachIndexed { index, place ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FF)),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(tsuBlue, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${index + 1}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(place.name, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (selected.size < 2) {
                        statusText = "Выберите минимум 2 точки!"
                        return@Button
                    }
                    isRunning = true
                    bestRoute = emptyList()
                    iteration = 0
                    statusText = "Муравьи ищут маршрут..."

                    coroutineScope.launch {
                        val iterCounter = AtomicInteger(0)
                        val distRef = AtomicReference(0.0)

                        val result = withContext(Dispatchers.Default) {
                            val colony = AntColony(places = selected.toList())
                            colony.run { iter, dist ->
                                iterCounter.set(iter)
                                distRef.set(dist)
                            }
                        }

                        bestRoute = result
                        bestDistance = distRef.get()
                        iteration = iterCounter.get()
                        isRunning = false
                        statusText = "Готово! Найден оптимальный обходной маршрут."
                    }
                },
                enabled = !isRunning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tsuBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isRunning) "Муравьи работают..." else "Запустить алгоритм", fontSize = 16.sp)
            }
        }
    }
}
