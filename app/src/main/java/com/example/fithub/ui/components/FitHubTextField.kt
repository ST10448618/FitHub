package com.example.fithub.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.SurfaceGray
import com.example.fithub.ui.theme.TextHint
import com.example.fithub.ui.theme.TextSecondary

@Composable
fun FitHubTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = TextHint
            )
        },
        isError = isError,
        singleLine = singleLine,
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FitHubPrimary,
            unfocusedBorderColor = SurfaceGray,
            focusedLabelColor = FitHubPrimary,
            unfocusedLabelColor = TextSecondary,
            cursorColor = FitHubPrimary
        )
    )
}