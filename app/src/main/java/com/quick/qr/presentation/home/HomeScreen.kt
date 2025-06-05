package com.quick.qr.presentation.home

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quick.qr.presentation.home.components.QRCodeDisplay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val viewModel = koinViewModel<HomeScreenViewModel>()
    var text by remember { mutableStateOf("") }
    var qrResult by remember { mutableStateOf<QRResult?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Enter Text") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 10,
                supportingText = {
                    Text(
                        text = "${text.length} characters",
                        color = Color.Gray
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        isGenerating = true
                        CoroutineScope(Dispatchers.IO).launch {
                            val result = viewModel.smartGenerateQRCode(text)
                            CoroutineScope(Dispatchers.Main).launch {
                                qrResult = result
                                isGenerating = false
                                when (result) {
                                    is QRResult.Single -> {
                                        if (result.bitmap == null) {
                                            Toast.makeText(context, "QR code could not be generated - text too long", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    is QRResult.Multiple -> {
                                        Toast.makeText(context, "${result.bitmaps.size} part(s) QR code generated", Toast.LENGTH_SHORT).show()
                                    }
                                    is QRResult.Compressed -> {
                                        Toast.makeText(context, "Compressed QR code generated", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Please enter some text", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isGenerating
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating...")
                } else {
                    Text("Generate QR Code")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        qrResult?.let { result ->
            when (result) {
                is QRResult.Single -> {
                    result.bitmap?.let { bitmap ->
                        viewModel.saveBitmapToRoom(viewModel.getCurrentDateTimeString(), bitmap)

                        item {
                            QRCodeDisplay(
                                bitmap = bitmap,
                                title = "QR Code",
                                onSave = {
                                    viewModel.saveBitmapToGallery(bitmap)
                                    Toast.makeText(context, "QR code saved to gallery", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                    }
                }

                is QRResult.Compressed -> {
                    result.bitmap?.let { bitmap ->
                        viewModel.saveBitmapToRoom(viewModel.getCurrentDateTimeString(), bitmap)

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "⚡ Compressed QR Code",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "This QR code contains compressed data",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            QRCodeDisplay(
                                bitmap = bitmap,
                                title = "Compressed QR Code",
                                onSave = {
                                    viewModel.saveBitmapToGallery(bitmap, "compressed_qr_${System.currentTimeMillis()}")
                                    Toast.makeText(context, "Compressed QR code saved", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                is QRResult.Multiple -> {
                    result.bitmaps.forEachIndexed { index, bitmap ->
                        viewModel.saveBitmapToRoom(
                            title = "${viewModel.getCurrentDateTimeString()}, ${index + 1}/${result.bitmaps.size}",
                            bitmap = bitmap
                        )
                    }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "📱 Multiple QR Code Set",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Generated in ${result.bitmaps.size} parts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    itemsIndexed(result.bitmaps) { index, bitmap ->
                        QRCodeDisplay(
                            bitmap = bitmap,
                            title = "QR Code ${index + 1}/${result.bitmaps.size}",
                            onSave = {
                                viewModel.saveBitmapToGallery(bitmap, "qr_part_${index + 1}_${System.currentTimeMillis()}")
                                Toast.makeText(context, "QR code ${index + 1} saved", Toast.LENGTH_SHORT).show()
                            }
                        )
                        if (index < result.bitmaps.size - 1) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}
