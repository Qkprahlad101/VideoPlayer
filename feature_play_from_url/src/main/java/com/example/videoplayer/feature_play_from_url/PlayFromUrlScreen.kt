package com.example.videoplayer.feature_play_from_url

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A screen that allows the user to input a video URL and play it.
 * @param onPlayClick A lambda that is invoked with the video URL when the user clicks the play button.
 * @param isLoading Whether the screen is currently in a loading state.
 * @param errorMessage An optional error message to display.
 */
@Composable
fun PlayFromUrlScreen(
    onPlayClick: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {

    var url by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Video URL (YouTube, Dailymotion, etc.)") },
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null,
            enabled = !isLoading
        )
        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { onPlayClick(url) },
                enabled = url.isNotBlank()
            ) {
                Text("Get Video and Play")
            }
        }
    }
}
