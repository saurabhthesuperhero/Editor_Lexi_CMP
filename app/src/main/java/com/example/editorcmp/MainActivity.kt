package com.example.editorcmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction

sealed class Token {
    data class Text(var content: String) : Token()
    data class BoldText(var content: String) : Token()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableTextBlock(token: Token) {
    var isEditing by remember { mutableStateOf(false) }
    when (token) {
        is Token.Text -> {
            if (isEditing) {
                TextField(
                    value = token.content,
                    onValueChange = { token.content = it },
                    modifier = Modifier.clickable { isEditing = true },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { isEditing = false }
                    )
                )
            } else {
                Text(
                    text = token.content,
                    modifier = Modifier.clickable { isEditing = true }
                )
            }
        }
        is Token.BoldText -> {
            if (isEditing) {
                TextField(
                    value = token.content,
                    onValueChange = { token.content = it },
                    modifier = Modifier.clickable { isEditing = true },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { isEditing = false }
                    )
                )
            } else {
                Text(
                    text = token.content,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { isEditing = true }
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditorWithLexing() {
    var text by remember { mutableStateOf("Type *bold* text here") }
    val tokens = lex(text)

    val keyboardController = LocalSoftwareKeyboardController.current

    BasicTextField(
        value = text,
        onValueChange = { text = it },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
            }
        )
    )

    Column {
        tokens.forEach { token ->
            EditableTextBlock(token)
        }
    }
}

fun lex(input: String): List<Token> {
    val tokens = mutableListOf<Token>()
    var buffer = StringBuilder()
    var isBold = false

    for (char in input) {
        when (char) {
            '*' -> {
                if (isBold) {
                    tokens.add(Token.BoldText(buffer.toString()))
                } else {
                    tokens.add(Token.Text(buffer.toString()))
                }
                buffer.clear()
                isBold = !isBold
            }
            else -> {
                buffer.append(char)
            }
        }
    }

    if (buffer.isNotEmpty()) {
        tokens.add(if (isBold) Token.BoldText(buffer.toString()) else Token.Text(buffer.toString()))
    }

    return tokens
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EditorWithLexing()
        }
    }
}
