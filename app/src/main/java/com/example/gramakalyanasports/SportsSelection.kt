package com.example.gramakalyanasports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.gramakalyanasports.ui.theme.*

@Composable
fun SportsSelection(onBackClicked: () -> Unit, onSportSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp)
    ) {
        IconButton(onClick = onBackClicked) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = OrangePrimary
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SELECT SPORT",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = OrangePrimary
            )
            
            Text(
                text = "Choose a discipline to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = GrayText
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Sport 1: Cricket
            SportCard(
                name = "CRICKET",
                icon = "🏏",
                gradientColors = listOf(Color(0xFFD32F2F), Color(0xFFB71C1C)),
                description = "Runs, Wickets & Overs",
                onClick = { onSportSelected("Cricket") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sport 2: Kabaddi
            SportCard(
                name = "KABADDI",
                icon = "🤼",
                gradientColors = listOf(OrangeSecondary, OrangePrimary),
                description = "Raids, Tackles & Bonus",
                onClick = { onSportSelected("Kabaddi") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sport 3: Volleyball
            SportCard(
                name = "VOLLEYBALL",
                icon = "🏐",
                gradientColors = listOf(Color(0xFF1976D2), Color(0xFF0D47A1)),
                description = "Points, Sets & Service",
                onClick = { onSportSelected("Volleyball") }
            )
        }
    }
}

@Composable
fun SportCard(name: String, icon: String, gradientColors: List<Color>, description: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                    text = icon,
                    fontSize = 44.sp,
                    modifier = Modifier.padding(end = 20.dp)
                )
                Column {
                    Text(
                        text = name,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = description,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SportsSelectionPreview() {
    GramaKalyanaSportsTheme(darkTheme = true) {
        SportsSelection(onBackClicked = {}, onSportSelected = {})
    }
}
