package com.example.tsumaps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsumaps.algorithms.Decision_tree.Node
import com.example.tsumaps.algorithms.Decision_tree.buildDecisionTree
import com.example.tsumaps.algorithms.Decision_tree.parseCSV
class DecisionTreeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DecisionTreeScreen()
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

@Composable
fun DecisionTreeScreen() {
    val csvData = """
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
bus_stop;low;medium;pancakes;low_que;good;Siberian_Pancakes
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
bus_stop;low_bud;medium;full_meal;high;good;Second_Building_Cafe
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
second_building;high;medium;full_meal;low_que;good;Blizhe_Gastro
campus_center;high;medium;full_meal;low_que;good;Blizhe_Gastro
main_building;high;medium;full_meal;low_que;good;Blizhe_Gastro
bus_stop;high;medium;full_meal;low_que;good;Blizhe_Gastro
second_building;high;medium;full_meal;medium_que;good;Baguette_Omelet
campus_center;high;medium;full_meal;medium_que;bad;Baguette_Omelet
main_building;high;medium;full_meal;medium_que;good;Baguette_Omelet
bus_stop;high;medium;full_meal;medium_que;bad;Baguette_Omelet
second_building;low_bud;medium;snack;medium_que;good;Yarche
campus_center;low_bud;medium;snack;medium_que;good;Yarche
main_building;low_bud;short;snack;medium_que;good;Yarche
bus_stop;low_bud;medium;snack;medium_que;good;Yarche
second_building;high;medium;full_meal;medium_que;good;Vechniy_Zov
campus_center;high;medium;full_meal;medium_que;good;Vechniy_Zov
main_building;high;medium;full_meal;medium_que;good;Vechniy_Zov
bus_stop;high;medium;full_meal;medium_que;good;Vechniy_Zov
""".trimIndent()

    var csvInput by remember { mutableStateOf(csvData) }
    var rootNode by remember { mutableStateOf<Node?>(null) }
    var currentNode by remember { mutableStateOf<Node?>(null) }
    var path by remember { mutableStateOf(listOf<String>()) }

    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        if (rootNode == null) {
            Text("1. Обучение дерева CSV", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = csvInput,
                onValueChange = { csvInput = it },
                modifier = Modifier.fillMaxWidth().height(500.dp).padding(vertical = 8.dp)
            )
            Button(onClick = {
                val (features, data) = parseCSV(csvInput)
                rootNode = buildDecisionTree(data, features.toSet())
                currentNode = rootNode
            },
                modifier = Modifier
                .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.tsu_blue_primary)
                ),
            ) {
                Text("Построить дерево решений", fontSize = 20.sp
                )
            }
        } else {
            Text("2. Выбор места", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            val node = currentNode!!
            if (node.isLeaf) {
                val englishResult = node.label ?: ""
                val russianResult = translationMap[englishResult] ?: englishResult
                Card(colors = CardDefaults.cardColors(containerColor = Color.LightGray)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Рекомендуется: $russianResult", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Путь: ${path.joinToString(" -> ")}", fontSize = 19.sp)
                    }
                }
                Button(onClick = {
                    rootNode = null
                    path = emptyList()
                },
                    modifier = Modifier
                    .padding(top = 16.dp)
                    .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.tsu_blue_primary)
                    )
                ) {
                    Text("Сбросить", fontSize = 20.sp)
                }
            } else {
                val englishFeature = node.feature ?: ""
                val russianQuestion = translationMap[englishFeature] ?: englishFeature
                Text("Вопрос: $russianQuestion", fontSize = 22.sp)
                node.branches?.forEach { (englishChoise, next) ->
                    val russianChoise = translationMap[englishChoise] ?: englishChoise

                    Button(
                        onClick = {
                            path = path + russianChoise
                            currentNode = next
                        },
                        modifier = Modifier.fillMaxWidth().height(65.dp).padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.tsu_blue_primary)
                        )
                    ) {Text(
                        text = russianChoise, fontSize = 20.sp,
                    )}
                }
            }
        }
    }
}