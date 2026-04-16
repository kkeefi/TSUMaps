package com.example.tsumaps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsumaps.algorithms.Decision_tree.Node
import com.example.tsumaps.algorithms.Decision_tree.buildDecisionTree
import com.example.tsumaps.algorithms.Decision_tree.parseCSV
import com.example.tsumaps.ui.theme.TSUMapsTheme

class DecisionTreeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TSUMapsTheme {
                DecisionTreeScreen(onBack = { finish() })
            }
        }
    }
}

val translationMap = mapOf(
    "location" to "Где вы находитесь?",
    "budget" to "Какой у вас бюджет?",
    "time_available" to "Сколько у вас времени?",
    "food_type" to "Что вы предпочитаете?",
    "queue_tolerance" to "Готовы ли вы стоять в очереди?",
    "weather" to "Какая сейчас погода?",
    "second_building" to "Второй корпус",
    "campus_center" to "Роща",
    "main_building" to "Главный корпус",
    "bus_stop" to "Автобусная остановка",
    "low" to "Низкий",
    "high" to "Высокий",
    "short" to "Мало",
    "medium" to "Достаточно",
    "medium_bud" to "Средний",
    "low_bud" to "Низкий",
    "high_bud" to "Высокий",
    "medium_que" to "Недолго",
    "low_que" to "Нет",
    "high_que" to "Да",
    "very_short" to "Очень мало",
    "full_meal" to "Полноценный обед",
    "pancakes" to "Блины",
    "coffee" to "Кофе",
    "snack" to "Перекус",
    "shawarma" to "Шаурма",
    "good" to "Хорошая",
    "bad" to "Плохая",
    "Stolovaya_1" to "Столовая №1",
    "Minutka_Cafe" to "Кафе Минутка",
    "Siberian_Pancakes" to "Сибирские блины",
    "Starbooks_Main" to "Старбукс (главный)",
    "Starbooks_SibGMU" to "Старбукс (СИБГМУ)",
    "Second_Building_Cafe" to "Кафе во 2-ом корпусе",
    "Vending_Machine" to "Автомат",
    "Abrikos" to "Абрикос",
    "Bezumno" to "Безумно",
    "Nash_Gastronom" to "Гастроном НАШ",
    "Baba_Roma" to "Баба Рома",
    "Blizhe_Gastro" to "Ближе (ресто-место)",
    "Yarche" to "Ярче",
    "Vechniy_Zov" to "Вечный зов",
    "Baguette_Omelet" to "Багет омлет",
    "XO_Bakery" to "Пекарня XO",
    "Belka_Coffee" to "Кофе «Белка»",
    "Rostics" to "Rostic's"
)

private val DEFAULT_CSV = """
location;budget;time_available;food_type;queue_tolerance;weather;recommended_place
second_building;medium_bud;short;full_meal;medium_que;good;Stolovaya_1
campus_center;medium_bud;short;full_meal;medium_que;good;Stolovaya_1
main_building;medium_bud;short;full_meal;medium_que;bad;Stolovaya_1
bus_stop;medium_bud;short;full_meal;medium_que;good;Stolovaya_1
second_building;medium_bud;medium;full_meal;medium_que;good;Minutka_Cafe
campus_center;medium_bud;medium;full_meal;medium_que;good;Minutka_Cafe
main_building;medium_bud;short;full_meal;low_que;bad;Minutka_Cafe
bus_stop;medium_bud;medium;full_meal;medium_que;good;Minutka_Cafe
second_building;low_bud;short;pancakes;low_que;good;Siberian_Pancakes
campus_center;low_bud;medium;pancakes;low_que;good;Siberian_Pancakes
main_building;low_bud;very_short;pancakes;low_que;bad;Siberian_Pancakes
bus_stop;low_bud;medium;pancakes;low_que;good;Siberian_Pancakes
second_building;low_bud;short;coffee;low_que;good;Starbooks_Main
campus_center;low_bud;short;coffee;low_que;good;Starbooks_Main
main_building;low_bud;very_short;coffee;low_que;good;Starbooks_Main
bus_stop;low_bud;medium;coffee;low_que;good;Starbooks_Main
second_building;low_bud;medium;coffee;medium_que;good;Starbooks_SibGMU
campus_center;low_bud;medium;coffee;medium_que;good;Starbooks_SibGMU
main_building;low_bud;medium;coffee;low_que;good;Starbooks_SibGMU
bus_stop;low_bud;medium;coffee;medium_que;good;Starbooks_SibGMU
second_building;low_bud;very_short;full_meal;low_que;bad;Second_Building_Cafe
campus_center;low_bud;medium;full_meal;medium_que;good;Second_Building_Cafe
main_building;low_bud;medium;full_meal;medium_que;good;Second_Building_Cafe
bus_stop;low_bud;medium;full_meal;high_que;good;Second_Building_Cafe
second_building;low_bud;very_short;coffee;low_que;bad;XO_Bakery
campus_center;low_bud;medium;coffee;low_que;good;XO_Bakery
main_building;low_bud;medium;coffee;low_que;good;XO_Bakery
bus_stop;low_bud;medium;coffee;low_que;good;XO_Bakery
second_building;low_bud;very_short;snack;low_que;good;Vending_Machine
campus_center;low_bud;medium;snack;low_que;good;Vending_Machine
main_building;low_bud;medium;snack;low_que;good;Vending_Machine
bus_stop;low_bud;medium;snack;low_que;good;Vending_Machine
second_building;low_bud;medium;full_meal;high_que;good;Abrikos
campus_center;medium_bud;medium;full_meal;high_que;good;Abrikos
main_building;medium_bud;medium;full_meal;high_que;good;Abrikos
bus_stop;medium_bud;medium;full_meal;high_que;good;Abrikos
second_building;low_bud;medium;shawarma;high_que;good;Bezumno
campus_center;low_bud;medium;shawarma;high_que;good;Bezumno
main_building;low_bud;medium;shawarma;high_que;good;Bezumno
bus_stop;low_bud;medium;shawarma;high_que;good;Bezumno
second_building;low_bud;medium;coffee;low_que;good;Belka_Coffee
campus_center;low_bud;short;coffee;low_que;good;Belka_Coffee
main_building;low_bud;medium;coffee;low_que;good;Belka_Coffee
bus_stop;low_bud;very_short;coffee;low_que;good;Belka_Coffee
second_building;medium_bud;medium;full_meal;high_que;good;Rostics
campus_center;medium_bud;medium;full_meal;high_que;good;Rostics
main_building;medium_bud;medium;full_meal;high_que;good;Rostics
bus_stop;medium_bud;medium;full_meal;high_que;good;Rostics
second_building;low_bud;medium;snack;low_que;good;Nash_Gastronom
campus_center;low_bud;medium;snack;low_que;good;Nash_Gastronom
main_building;low_bud;medium;snack;low_que;good;Nash_Gastronom
bus_stop;low_bud;short;snack;low_que;good;Nash_Gastronom
second_building;low_bud;medium;coffee;low_que;good;Baba_Roma
campus_center;low_bud;short;coffee;low_que;good;Baba_Roma
main_building;low_bud;medium;coffee;low_que;good;Baba_Roma
bus_stop;low_bud;short;coffee;low_que;good;Baba_Roma
second_building;high_bud;medium;full_meal;low_que;good;Blizhe_Gastro
campus_center;high_bud;medium;full_meal;low_que;good;Blizhe_Gastro
main_building;high_bud;medium;full_meal;low_que;good;Blizhe_Gastro
bus_stop;high_bud;medium;full_meal;low_que;good;Blizhe_Gastro
second_building;high_bud;medium;full_meal;medium_que;good;Baguette_Omelet
campus_center;high_bud;medium;full_meal;medium_que;bad;Baguette_Omelet
main_building;high_bud;medium;full_meal;medium_que;good;Baguette_Omelet
bus_stop;high_bud;medium;full_meal;medium_que;bad;Baguette_Omelet
second_building;low_bud;medium;snack;medium_que;good;Yarche
campus_center;low_bud;medium;snack;medium_que;good;Yarche
main_building;low_bud;short;snack;medium_que;good;Yarche
bus_stop;low_bud;medium;snack;medium_que;good;Yarche
second_building;high_bud;medium;full_meal;medium_que;good;Vechniy_Zov
campus_center;high_bud;medium;full_meal;medium_que;good;Vechniy_Zov
main_building;high_bud;medium;full_meal;medium_que;good;Vechniy_Zov
bus_stop;high_bud;medium;full_meal;medium_que;good;Vechniy_Zov
""".trimIndent()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecisionTreeScreen(onBack: () -> Unit) {
    val tsuBlue = colorResource(id = R.color.tsu_blue_primary)
    var csvInput by remember { mutableStateOf(DEFAULT_CSV) }
    var rootNode by remember { mutableStateOf<Node?>(null) }
    var currentNode by remember { mutableStateOf<Node?>(null) }
    var path by remember { mutableStateOf(listOf<String>()) }
    var buildError by remember { mutableStateOf("") }
    var showFullTree by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Дерево решений", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = tsuBlue
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (rootNode == null) {
                Text(
                    "Шаг 1: Обучающая выборка (CSV)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Разделитель столбцов — точка с запятой. Последний столбец — рекомендуемое заведение.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                OutlinedTextField(
                    value = csvInput,
                    onValueChange = {
                        csvInput = it
                        buildError = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    label = { Text("CSV данные") }
                )

                if (buildError.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEEEE))
                    ) {
                        Text(
                            buildError,
                            modifier = Modifier.padding(12.dp),
                            color = Color(0xFFB00020),
                            fontSize = 13.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        try {
                            val (features, data) = parseCSV(csvInput)
                            if (data.isEmpty()) {
                                buildError = "Ошибка: данные пусты или формат CSV неверный."
                                return@Button
                            }
                            rootNode = buildDecisionTree(data, features.toSet())
                            currentNode = rootNode
                            buildError = ""
                        } catch (e: Exception) {
                            buildError = "Ошибка разбора CSV: ${e.message}"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = tsuBlue),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Построить дерево решений", fontSize = 16.sp)
                }

            } else {
                Text("Работа с деревом в формате:", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Button(
                        onClick = { showFullTree = false },
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!showFullTree) colorResource(id = R.color.tsu_blue_primary) else Color.Gray
                        )
                    ) { Text("Вопрос-ответ") }

                    Button(
                        onClick = { showFullTree = true },
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showFullTree) colorResource(id = R.color.tsu_blue_primary) else Color.Gray
                        )
                    ) { Text("Структура дерева") }
                }

                if (showFullTree) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        val treeStructure =
                            com.example.tsumaps.algorithms.Decision_tree.printTreeRecursive(
                                rootNode!!,
                                "",
                                translationMap
                            )
                        Text(
                            text = treeStructure,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 16.sp,
                            lineHeight = 19.sp
                        )
                    }
                } else {
                    Text(
                        "Шаг 2: Подбор заведения",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (path.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF))
                        ) {
                            Text(
                                "Путь: ${path.joinToString(" -> ")}",
                                modifier = Modifier.padding(12.dp),
                                fontSize = 13.sp,
                                color = Color.DarkGray
                            )
                        }
                    }

                    val node = currentNode!!
                    if (node.isLeaf) {
                        val englishResult = node.label ?: ""
                        val russianResult = translationMap[englishResult] ?: englishResult

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(Modifier.padding(20.dp)) {
                                Text(
                                    "Рекомендуется:",
                                    fontSize = 14.sp,
                                    color = Color(0xFF388E3C)
                                )
                                Text(
                                    russianResult,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                rootNode = null
                                currentNode = null
                                path = emptyList()
                                showFullTree = false
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red.copy(
                                    alpha = 0.7f
                                )
                            )
                        ) {
                            Text("Сбросить", fontSize = 16.sp)
                        }

                    } else {
                        val englishFeature = node.feature ?: ""
                        val russianQuestion = translationMap[englishFeature] ?: englishFeature

                        Text(
                            russianQuestion,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        node.branches?.forEach { (englishChoice, next) ->
                            val russianChoice = translationMap[englishChoice] ?: englishChoice
                            Button(
                                onClick = {
                                    path = path + russianChoice
                                    currentNode = next
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = tsuBlue),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(russianChoice, fontSize = 16.sp)
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                rootNode = null
                                currentNode = null
                                path = emptyList()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Изменить данные CSV", color = tsuBlue)
                        }
                    }
                }
            }
        }
    }
}
