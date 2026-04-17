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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
            TSUMapsTheme {
                MainScreen(
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.tsu_logo),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp).padding(end = 8.dp)
                        )
                        Text("ТГУ Навигатор", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionLabel("Навигация")
            AlgoButton("А* — Маршрут по карте", "Построить путь между двумя точками", onRouteClick)

            SectionLabel("Анализ данных")
            AlgoButton("K-Means — Кластеры", "Расставить точки и найти кластеры", onClustersClick)
            AlgoButton("Дерево решений", "Подобрать заведение по предпочтениям", onDecisionTreeClick)

            SectionLabel("Оптимизация маршрута")
            AlgoButton("Генетический алгоритм", "Маршрут для покупки нескольких блюд", onGeneticClick)
            AlgoButton("Муравьиный алгоритм", "Обход достопримечательностей кампуса", onAntColonyClick)

            SectionLabel("Оценка заведения")
            AlgoButton("Нейросеть — распознавание цифры", "Нарисуйте оценку от 0 до 9", onNeuralNetClick)
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
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
    )
}

@Composable
fun AlgoButton(text: String, subtitle: String, onClick: () -> Unit) {
    val tsuBlue = colorResource(id = R.color.tsu_blue_primary)
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(68.dp),
        colors = ButtonDefaults.buttonColors(containerColor = tsuBlue),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
            Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(text = subtitle, fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f))
        }
    }
}
