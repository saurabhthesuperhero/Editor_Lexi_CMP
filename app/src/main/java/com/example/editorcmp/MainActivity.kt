package com.example.editorcmp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import java.util.*

data class EditorBlock(
    val id: String,
    val type: BlockType,
    var content: String,
    val contentStyles: MutableList<String> = mutableListOf()
)

private const val TAG = "MainActivity"

enum class BlockType {
    INPUT,
    BULLET_POINT
}

@Composable
fun BlockBasedEditor() {
    var blocks by remember {
        mutableStateOf(
            mutableListOf(
                EditorBlock(UUID.randomUUID().toString(), BlockType.INPUT, "")
            )
        )
    }
    val focusRequesters = remember { MutableList(blocks.size) { FocusRequester() } }
    val focusTrigger = remember { mutableStateOf(0) }

    LaunchedEffect(focusTrigger.value) {
        focusRequesters.getOrNull(focusTrigger.value)?.requestFocus()
    }

    LazyColumn {
        itemsIndexed(blocks) { index, block ->
            val focusRequester = focusRequesters.getOrNull(index) ?: FocusRequester().also {
                focusRequesters.add(index, it)
            }

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                when (block.type) {
                    BlockType.INPUT ->
                        EditorTextField(
                            block = block,
                            textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                            onBlockUpdated = {        updatedContent, updatedType ->
                                val updatedBlock = block.copy(content = updatedContent, type = updatedType)
                                blocks[index] = updatedBlock
                            },
                            onEnterPressed = {
                                Log.d(TAG, "BlockBasedEditor() called BlockType.INPUT onEnterPressed")
                                val newBlock = EditorBlock(UUID.randomUUID().toString(), BlockType.INPUT, "")
                                blocks.add(index + 1, newBlock)
                                focusRequesters.add(index + 1, FocusRequester())
                                focusTrigger.value = index + 1
                            },
                            focusRequester = focusRequester
                        )

                    BlockType.BULLET_POINT -> {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
//                            Text("•", style = TextStyle(fontSize = 16.sp))
                            EditorTextField(
                                block = block,
                                textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                                onBlockUpdated = {
                                    updatedContent, updatedType ->
                                    val updatedBlock = block.copy(content = updatedContent, type = updatedType)
                                    blocks[index] = updatedBlock
                                },
                                onEnterPressed = {
                                    Log.d(TAG, "BlockBasedEditor() called BlockType.BULLET_POINT onEnterPressed")
                                    if (block.content.trim() == "-") {
                                        // If only "-" is there, then create a new block
                                        val newBlock = EditorBlock(UUID.randomUUID().toString(), BlockType.INPUT, "")
                                        blocks.add(index + 1, newBlock)
                                        focusRequesters.add(index + 1, FocusRequester())
                                        focusTrigger.value = index + 1
                                    } else {
                                        // Add a new bullet point in the same block
                                        block.content = "${block.content}\n- "
                                        blocks[index] = block
                                    }
                                },
                                focusRequester = focusRequester
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditorTextField(
    block: EditorBlock,
    textStyle: TextStyle,
    onBlockUpdated: (String, BlockType) -> Unit,
    onEnterPressed: () -> Unit,
    focusRequester: FocusRequester
) {
    var text by remember { mutableStateOf(block.content) }
    var updatedType by remember { mutableStateOf(block.type) }  // Add this line

    // Keep 'text' and 'block.content' in sync
    LaunchedEffect(block.content) {
        text = block.content
    }

    BasicTextField(
        value = text,
        onValueChange = { newValue ->
            text = newValue

            if (newValue.startsWith("- ") && updatedType != BlockType.BULLET_POINT) {
                updatedType = BlockType.BULLET_POINT
            }

            if (newValue.endsWith("\n")) {
                text = newValue.removeSuffix("\n")
                if (updatedType == BlockType.BULLET_POINT) {  // Change this line
                    if (newValue.trim() == "-") {
                        updatedType = BlockType.INPUT
                        onEnterPressed()
                    } else {
                        val updatedContent = "$text- "
                        text = updatedContent
                    }
                } else {
                    onEnterPressed()
                }
            }
            onBlockUpdated(text, updatedType)
        },
        textStyle = textStyle,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Default
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onEnterPressed()
            }
        ),
        modifier = Modifier
            .focusRequester(focusRequester)
            .fillMaxWidth()
    )
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            BlockBasedEditor()
        }
    }
}
