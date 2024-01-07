/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.animationgarden.app.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.him188.animationgarden.app.ui.framework.launchInBackground
import me.him188.animationgarden.utils.logging.info

@Composable
fun AuthPage(viewModel: AccountViewModel) {
    val errorFontSize = 14.sp
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val isRegister by viewModel.isRegister.collectAsState()
        val usernameError by viewModel.usernameError.collectAsState()
        val passwordError by viewModel.passwordError.collectAsState()
        val verifyPasswordError by viewModel.verifyPasswordError.collectAsState()
        Text(
            "Bangumi",
            modifier = Modifier.padding(bottom = 20.dp),
            fontSize = 30.sp,
            fontWeight = FontWeight.W800,
            fontStyle = FontStyle.Normal,
        )

        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = viewModel.username.value,
                onValueChange = { viewModel.setUsername(it) },
                isError = (usernameError != null),
                label = { Text("邮箱") },
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                ),
            )
        }
        AnimatedVisibility(usernameError != null) {
            usernameError?.let {
                Text(
                    text = it,
                    fontSize = errorFontSize,
                    color = Color.Red,
                )
            }
        }

        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isPasswordVisible by viewModel.isPasswordVisible
            OutlinedTextField(
                value = viewModel.password.value,
                onValueChange = { viewModel.setPassword(it) },
                trailingIcon = {
                    IconToggleButton(
                        checked = isPasswordVisible,
                        onCheckedChange = { viewModel.setPasswordVisible(it) },
                    ) {
                        Icon(
                            if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            null
                        )
                    }
                },
                isError = (passwordError != null),
                label = { Text("密码") },
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                ),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None
                else PasswordVisualTransformation('*')
            )
        }
        AnimatedVisibility(passwordError != null) {
            passwordError?.let {
                Text(
                    text = it,
                    fontSize = errorFontSize,
                    color = Color.Red,
                )
            }
        }

        AnimatedVisibility(isRegister) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = viewModel.verifyPassword.value,
                    onValueChange = { viewModel.setVerifyPassword(it) },
                    isError = (verifyPasswordError != null),
                    label = { Text("Verify Password") },
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                    ),
                    visualTransformation = PasswordVisualTransformation('*')
                )
            }
        }
        AnimatedVisibility(verifyPasswordError != null) {
            verifyPasswordError?.let {
                Text(
                    text = it,
                    fontSize = errorFontSize,
                    color = Color.Red,
                )
            }
        }

        Button(
            onClick = {
                viewModel.logger.info { "Click Login: ${viewModel.isProcessing.value}" }
                if (viewModel.isProcessing.compareAndSet(expect = false, update = true)) {
                    viewModel.launchInBackground {
                        try {
                            viewModel.onClickProceed()
                        } finally {
                            viewModel.isProcessing.compareAndSet(expect = true, update = false)
                        }
                    }
                }
            },
            enabled = !viewModel.isProcessing.value,
            modifier = Modifier.padding(10.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                if (isRegister) "注册" else "登录",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        val highlightColor = MaterialTheme.colorScheme.secondary
        val signUpMessage = remember(highlightColor) {
            buildAnnotatedString {
                append("")
                pushStyle(SpanStyle(color = highlightColor))
                append("sign up")
                pop()
            }
        }

        val loginMessage = remember(highlightColor) {
            buildAnnotatedString {
//                append("Already have an account? Please ")
//                pushStyle(SpanStyle(color = highlightColor))
//                append("登录")
//                pop()
            }
        }

        ClickableText(
            text = if (!isRegister) signUpMessage else loginMessage,
            onClick = { viewModel.onClickSwitch() },
            style = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onBackground),
        )
    }
}