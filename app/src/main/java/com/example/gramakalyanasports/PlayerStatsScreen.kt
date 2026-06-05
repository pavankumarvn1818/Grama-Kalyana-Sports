package com.example.gramakalyanasports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.gramakalyanasports.ui.theme.*

@Composable
fun PlayerStatsScreen(
    sportType: String,
    onBackClicked: () -> Unit
) {
    var players by remember { mutableStateOf(emptyList<Player>()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        FirebaseManager.listenToPlayers { updatedPlayers ->
            players = updatedPlayers.filter { 
                it.sportType.equals(sportType, ignoreCase = true) 
            }.sortedByDescending { it.careerPoints }
            isLoading = false
        }
    }

    val filteredPlayers = if (searchQuery.isBlank()) {
        players
    } else {
        players.filter { it.playerName.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                    contentDescription = "Back",
                    tint = OrangePrimary
                )
            }
            Text(
                text = "LEADERBOARD",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = OrangePrimary
            )
        }

        Text(
            text = sportType.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = GrayText,
            modifier = Modifier.padding(start = 48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = BlackContainer)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Career points are updated automatically when matches are finished.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search players...", color = GrayText) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = OrangePrimary) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangePrimary,
                unfocusedBorderColor = BlackContainer,
                focusedContainerColor = BlackSurface,
                unfocusedContainerColor = BlackSurface,
                focusedTextColor = WhiteText,
                unfocusedTextColor = WhiteText,
                cursorColor = OrangePrimary
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangePrimary)
            }
        } else if (filteredPlayers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (searchQuery.isBlank()) "No $sportType players found." else "No players match '$searchQuery'",
                    color = GrayText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredPlayers) { player ->
                    PlayerStatCard(player)
                }
            }
        }
    }
}

@Composable
fun PlayerStatCard(player: Player) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BlackSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.playerName.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = WhiteText
                )
                Text(
                    text = "ID: ${player.playerId.takeLast(6)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = GrayText
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${player.careerPoints}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = OrangePrimary,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "PTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = OrangeSecondary,
                    fontWeight = FontWeight.Bold
                )
                
                if (player.manOfTheMatchCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = " ${player.manOfTheMatchCount} MOTM",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GrayText
                        )
                    }
                }
            }
        }
    }
}
