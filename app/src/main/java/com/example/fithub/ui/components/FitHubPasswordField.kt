package com.example.fithub.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.fithub.R
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.SurfaceGray
import com.example.fithub.ui.theme.TextHint
import com.example.fithub.ui.theme.TextSecondary

@Composable
fun FitHubPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    isError: Boolean = false
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = {
            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        },
        placeholder = {
            Text(
                text = stringResource(R.string.common_password_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = TextHint
            )
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            androidx.compose.material3.TextButton(onClick = { visible = !visible }) {
                Text(
                    text = if (visible) stringResource(R.string.common_password_hide)
                    else stringResource(R.string.common_password_show),
                    style = MaterialTheme.typography.labelSmall,
                    color = FitHubPrimary
                )
            }
        },
        isError = isError,
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FitHubPrimary,
            unfocusedBorderColor = SurfaceGray,
            focusedLabelColor = FitHubPrimary,
            unfocusedLabelColor = TextSecondary,
            cursorColor = FitHubPrimary
        )
    )
}