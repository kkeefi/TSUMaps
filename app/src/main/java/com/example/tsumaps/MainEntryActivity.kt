package com.example.tsumaps

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import com.example.tsumaps.ui.theme.TSUMapsTheme

class MainEntryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TSUMapsTheme {
                MainScreen(
                    onRouteClick = { startActivity(Intent(this, RouteActivity::class.java)) },
                    onClustersClick = { startActivity(Intent(this, ClustersActivity::class.java)) },
                    onDecisionTreeClick = { startActivity(Intent(this, DecisionTreeActivity::class.java)) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onRouteClick: () -> Unit, onClustersClick: () -> Unit, onDecisionTreeClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.tsu_logo),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).padding(end = 8.dp)
                        )
                        Text("ТГУ Навигатор", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.tsu_blue_primary)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MenuButton("А* Маршрут (Карта из JSON)", onRouteClick)
            MenuButton("Кластеры (K-Means)", onClustersClick)
            MenuButton("Дерево решений", onDecisionTreeClick)

            MenuButton("Генетический алгоритм", {}, enabled = false)
            MenuButton("Муравьиный алгоритм", {}, enabled = false)
        }
    }
}

@Composable
fun MenuButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(80.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.tsu_blue_primary)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
    }
}