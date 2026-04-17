package com.example.tsumaps

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.tsumaps.ui.theme.TSUMapsTheme

class MainEntryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            var darkTheme by remember { mutableStateOf(true) }
            TSUMapsTheme(darkTheme = darkTheme) {
                MainScreen(
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme },
                    onRouteClick        = { startActivity(Intent(this, RouteActivity::class.java)) },
                    onClustersClick     = { startActivity(Intent(this, ClustersActivity::class.java)) },
                    onDecisionTreeClick = { startActivity(Intent(this, DecisionTreeActivity::class.java)) },
                    onGeneticClick      = { startActivity(Intent(this, GeneticAlgorithmActivity::class.java)) },
                    onAntColonyClick    = { startActivity(Intent(this, AntColonyActivity::class.java)) },
                    onNeuralNetClick    = { startActivity(Intent(this, NeuralNetworkActivity::class.java)) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onRouteClick: () -> Unit,
    onClustersClick: () -> Unit,
    onDecisionTreeClick: () -> Unit,
    onGeneticClick: () -> Unit,
    onAntColonyClick: () -> Unit,
    onNeuralNetClick: () -> Unit
) {
    val tsuBlue = colorResource(id = R.color.tsu_blue_primary)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.tsu_logo),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                "ТГУ Навигатор",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                lineHeight = 20.sp
                            )
                            Text(
                                "Томский государственный университет",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp,
                                lineHeight = 13.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (darkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = if (darkTheme) "Светлая тема" else "Тёмная тема",
                            tint = Color.White
                        )
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionLabel("Навигация")
            AlgoButton(
                text = "А* — Маршрут по карте",
                subtitle = "Бонус: препятствия и анимация поиска",
                onClick = onRouteClick
            )

            SectionLabel("Анализ данных")
            AlgoButton(
                text = "K-Means — Кластеры",
                subtitle = "Бонус: евклидова и манхэттенская метрики",
                onClick = onClustersClick
            )
            AlgoButton(
                text = "Дерево решений",
                subtitle = "Подобрать заведение по предпочтениям",
                onClick = onDecisionTreeClick
            )

            SectionLabel("Оптимизация маршрута")
            AlgoButton(
                text = "Генетический алгоритм",
                subtitle = "Маршрут для покупки нескольких блюд",
                onClick = onGeneticClick
            )
            AlgoButton(
                text = "Муравьиный алгоритм",
                subtitle = "Обход достопримечательностей кампуса",
                onClick = onAntColonyClick
            )

            SectionLabel("Оценка заведения")
            AlgoButton(
                text = "Нейросеть — распознавание цифры",
                subtitle = "Бонус: сетка 50×50, рисование пальцем",
                onClick = onNeuralNetClick
            )

            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = tsuBlue.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🎓", fontSize = 24.sp)
                    Column {
                        Text(
                            "Проект по курсу «Алгоритмы»",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            "Томский государственный университет, 2025",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(top = 6.dp, start = 2.dp)
    )
}

@Composable
fun AlgoButton(text: String, subtitle: String, onClick: () -> Unit) {
    val tsuBlue = colorResource(id = R.color.tsu_blue_primary)
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(66.dp),
        colors = ButtonDefaults.buttonColors(containerColor = tsuBlue),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(text = subtitle, fontSize = 11.sp, color = Color.White.copy(alpha = 0.72f))
        }
    }
}
