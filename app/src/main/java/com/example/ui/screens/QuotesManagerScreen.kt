package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BrainRotStage
import com.example.data.CustomMessageEntity
import com.example.data.ReelSessionState
import com.example.ui.components.PersistentBrainRotBanner
import com.example.ui.theme.AirbnbBorder
import com.example.ui.theme.AirbnbCharcoal
import com.example.ui.theme.AirbnbDivider
import com.example.ui.theme.AirbnbRausch
import com.example.ui.theme.AirbnbSlate
import com.example.ui.theme.AirbnbSurface
import com.example.ui.theme.AirbnbWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuotesManagerScreen(
    state: ReelSessionState,
    messages: List<CustomMessageEntity>,
    onAddMessage: (String, String, Int) -> Unit,
    onToggleMessage: (CustomMessageEntity) -> Unit,
    onDeleteMessage: (CustomMessageEntity) -> Unit,
    onExitClicked: () -> Unit,
    onShuffleQuote: () -> Unit,
    modifier: Modifier = Modifier
) {
    var quoteInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Mindful") }
    var selectedStageLevel by remember { mutableIntStateOf(1) }

    val categories = listOf("Mindful", "Reality Check", "Wake Up", "Direct", "Urgent")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AirbnbWhite)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            PersistentBrainRotBanner(
                state = state,
                onExitClicked = onExitClicked,
                onShuffleQuote = onShuffleQuote
            )
        }

        // Minimal Airbnb Composer Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AirbnbWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AirbnbDivider, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Add a wake-up message",
                        style = MaterialTheme.typography.titleLarge,
                        color = AirbnbCharcoal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Short, clear reminders work best in the compact top pill.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AirbnbSlate
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = quoteInput,
                        onValueChange = { quoteInput = it },
                        placeholder = {
                            Text(
                                text = "e.g., Close Instagram and take a walk",
                                color = AirbnbSlate
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AirbnbCharcoal,
                            unfocusedBorderColor = AirbnbBorder,
                            focusedTextColor = AirbnbCharcoal,
                            unfocusedTextColor = AirbnbCharcoal,
                            cursorColor = AirbnbRausch
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_quote_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Tone",
                        style = MaterialTheme.typography.labelMedium,
                        color = AirbnbSlate
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) AirbnbCharcoal else AirbnbSurface)
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color.White else AirbnbCharcoal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Minimum brain rot stage",
                        style = MaterialTheme.typography.labelMedium,
                        color = AirbnbSlate
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BrainRotStage.entries.forEach { stage ->
                            val isSelected = selectedStageLevel == stage.level
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) AirbnbCharcoal else AirbnbSurface)
                                    .clickable { selectedStageLevel = stage.level }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Lv.${stage.level}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color.White else AirbnbCharcoal,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (quoteInput.isNotBlank()) {
                                onAddMessage(quoteInput, selectedCategory, selectedStageLevel)
                                quoteInput = ""
                            }
                        },
                        enabled = quoteInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AirbnbRausch,
                            contentColor = Color.White,
                            disabledContainerColor = AirbnbSurface,
                            disabledContentColor = AirbnbSlate
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("add_quote_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save message",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Your messages (${messages.count { it.isEnabled }} active)",
                style = MaterialTheme.typography.titleLarge,
                color = AirbnbCharcoal
            )
        }

        items(messages, key = { it.id }) { item ->
            val stageObj = BrainRotStage.entries.firstOrNull { it.level == item.minStageLevel }
                ?: BrainRotStage.MINDFUL

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(stageObj.color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${stageObj.shortTag} (Lv.${stageObj.level}+) · ${item.category}",
                                style = MaterialTheme.typography.labelMedium,
                                color = AirbnbSlate
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.messageText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (item.isEnabled) AirbnbCharcoal else AirbnbSlate,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = item.isEnabled,
                        onCheckedChange = { onToggleMessage(item) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AirbnbCharcoal,
                            uncheckedThumbColor = AirbnbSlate,
                            uncheckedTrackColor = AirbnbSurface
                        )
                    )

                    IconButton(
                        onClick = { onDeleteMessage(item) },
                        modifier = Modifier.testTag("delete_quote_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove message",
                            tint = AirbnbSlate,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                HorizontalDivider(color = AirbnbDivider)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
