package com.gemwallet.android.calculator

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gemwallet.android.MainActivity

class CalculatorActivity : ComponentActivity() {
    private lateinit var codeStore: CalculatorCodeStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        codeStore = CalculatorCodeStore(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalculatorScreen(
                        codeStore = codeStore,
                        onUnlockSuccess = {
                            UnlockState.isUnlocked = true
                            val intent = Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(codeStore: CalculatorCodeStore, onUnlockSuccess: () -> Unit) {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("0") }
    val history = remember { mutableStateOf(listOf<String>()) }
    var showHistory by remember { mutableStateOf(false) }

    fun append(char: String) {
        expression += char
    }

    fun clear() {
        expression = ""
        result = "0"
    }

    fun equals() {
        if (expression.isBlank()) return
        if (codeStore.hasCode() && expression.all { it.isDigit() }) {
            if (codeStore.verifyCode(expression)) {
                onUnlockSuccess()
                return
            }
        }
        val evaluated = CalculatorEngine.evaluate(expression)
        result = evaluated
        if (evaluated != "Error") {
            val record = "$expression = $evaluated"
            history.value = (listOf(record) + history.value).take(20)
        }
    }

    fun toggleSign() {
        if (expression.startsWith("-")) {
            expression = expression.removePrefix("-")
        } else if (expression.isNotEmpty()) {
            expression = "-$expression"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculator") },
                actions = {
                    TextButton(onClick = { showHistory = !showHistory }) {
                        Text("History")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = expression.ifEmpty { "0" },
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result,
                        fontSize = 48.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val buttons = listOf(
                    listOf("C", "(", ")", "÷"),
                    listOf("7", "8", "9", "×"),
                    listOf("4", "5", "6", "-"),
                    listOf("1", "2", "3", "+"),
                    listOf("±", "0", ".", "=")
                )

                buttons.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { label ->
                            Button(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp),
                                onClick = {
                                    when (label) {
                                        "C" -> clear()
                                        "=" -> equals()
                                        "±" -> toggleSign()
                                        "×" -> append("×")
                                        "÷" -> append("÷")
                                        else -> append(label)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (label == "=") MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (label == "=") MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(text = label, fontSize = 24.sp)
                            }
                        }
                    }
                }
            }

            if (showHistory) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Calculation History", fontSize = 20.sp)
                            TextButton(onClick = { showHistory = false }) {
                                Text("Close")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (history.value.isEmpty()) {
                            Text("No history yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(history.value) { item ->
                                    Text(
                                        text = item,
                                        fontSize = 18.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val parts = item.split(" = ")
                                                if (parts.isNotEmpty()) {
                                                    expression = parts[0]
                                                    showHistory = false
                                                }
                                            }
                                            .padding(vertical = 12.dp)
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
