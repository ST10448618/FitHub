package com.example.fithub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.fithub.ui.theme.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


@Composable
fun FitHubTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    placeholder: String? = null,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = FitHubPrimary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = singleLine,
            isError = isError,
            placeholder = {
                if (placeholder != null) {
                    Text(placeholder, color = TextHint)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FitHubPrimary,
                unfocusedBorderColor = FitHubPrimary.copy(alpha = 0.6f),
                focusedContainerColor = FitHubLightBlue.copy(alpha = 0.35f),
                unfocusedContainerColor = FitHubLightBlue.copy(alpha = 0.25f),
                disabledContainerColor = FitHubLightBlue.copy(alpha = 0.15f),
                errorBorderColor = ErrorRed,
                cursorColor = FitHubPrimary
            )
        )
    }
}

@Composable
fun FitHubPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "PASSWORD",
    placeholder: String = "Enter your Password",
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    FitHubTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        modifier = modifier,
        keyboardType = KeyboardType.Password,
        visualTransformation = PasswordVisualTransformation(),
        isError = isError
    )
}

@Composable
fun DropdownField(
    label: String,
    current: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = FitHubPrimary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        androidx.compose.material3.OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Text(current, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
            Text("▾")
        }
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}