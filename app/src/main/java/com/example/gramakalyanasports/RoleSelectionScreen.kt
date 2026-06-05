package com.example.gramakalyanasports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.tooling.preview.Preview
import com.example.gramakalyanasports.ui.theme.*

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (String) -> Unit
) {
    var showPinDialog by remember { mutableStateOf(false) }
    var selectedRoleName by remember { mutableStateOf("") }
    var enteredPin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🏏 GRAMA KALYANA 🏐",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = OrangePrimary
        )
        Text(
            text = "SPORTS PORTAL",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = GrayText,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // Admin Card - Orange Gradient
        RoleCard(
            roleName = "ADMIN",
            roleIcon = "🔐",
            description = "Manage matches and live scores",
            gradientColors = listOf(OrangeSecondary, OrangePrimary)
        ) {
            selectedRoleName = "Admin"
            showPinDialog = true
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Player Card - Darker Orange Gradient
        RoleCard(
            roleName = "PLAYER",
            roleIcon = "👤",
            description = "View stats and performance history",
            gradientColors = listOf(Color(0xFFE65100), OrangeSecondary)
        ) {
            onRoleSelected("Player")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Viewer Card - Black/Gray Gradient
        RoleCard(
            roleName = "VIEWER",
            roleIcon = "📡",
            description = "Watch live matches in real-time",
            gradientColors = listOf(Color(0xFF424242), BlackContainer)
        ) {
            onRoleSelected("Viewer")
        }
    }

    if (showPinDialog) {
        Dialog(onDismissRequest = { showPinDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BlackSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔐 Admin Verification",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Enter your secret 4-digit PIN", fontSize = 14.sp, color = GrayText)

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                enteredPin = it
                                showError = false
                            }
                        },
                        label = { Text("PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = showError,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = GrayText,
                            focusedLabelColor = OrangePrimary,
                            cursorColor = OrangePrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = { showPinDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = GrayText)
                        }

                        Button(
                            onClick = {
                                if (enteredPin == "1234") { 
                                    onRoleSelected(selectedRoleName)
                                    showPinDialog = false
                                    enteredPin = ""
                                } else {
                                    showError = true
                                    enteredPin = ""
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) {
                            Text("Unlock", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoleCard(
    roleName: String,
    roleIcon: String,
    description: String,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(gradientColors))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = roleIcon,
                    fontSize = 36.sp,
                    modifier = Modifier.padding(end = 20.dp)
                )
                Column {
                    Text(
                        text = roleName,
                        color = if (roleName == "VIEWER") WhiteText else Color.Black,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = description,
                        color = if (roleName == "VIEWER") GrayText else Color.Black.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RoleSelectionPreview() {
    GramaKalyanaSportsTheme(darkTheme = true) {
        RoleSelectionScreen(onRoleSelected = {})
    }
}
