package com.example.gramakalyanasports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
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
fun MatchSetupScreen(
    sport: String,
    isStarting: Boolean,
    onBackClicked: () -> Unit,
    onMatchStarted: (String, String, String, List<String>, List<String>) -> Unit
) {
    var teamAName by remember { mutableStateOf("") }
    var teamBName by remember { mutableStateOf("") }
    var tournamentName by remember { mutableStateOf("") }
    
    var playerAName by remember { mutableStateOf("") }
    val teamAPlayers = remember { mutableStateListOf<String>() }
    
    var playerBName by remember { mutableStateOf("") }
    val teamBPlayers = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        IconButton(onClick = onBackClicked, enabled = !isStarting) {
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
                text = "🏆 SETUP MATCH",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = OrangePrimary
            )
            Text(
                text = sport.uppercase(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = GrayText
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = tournamentName,
                onValueChange = { tournamentName = it },
                label = { Text("Tournament Name") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isStarting,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangePrimary,
                    unfocusedBorderColor = GrayText,
                    focusedLabelColor = OrangePrimary,
                    cursorColor = OrangePrimary,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Team A Section
            TeamInputSection(
                label = "Team A Name",
                teamName = teamAName,
                onTeamNameChange = { teamAName = it },
                playerName = playerAName,
                onPlayerNameChange = { playerAName = it },
                playersList = teamAPlayers,
                onAddPlayer = {
                    if (playerAName.isNotBlank()) {
                        teamAPlayers.add(playerAName)
                        playerAName = ""
                    }
                },
                isStarting = isStarting
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Team B Section
            TeamInputSection(
                label = "Team B Name",
                teamName = teamBName,
                onTeamNameChange = { teamBName = it },
                playerName = playerBName,
                onPlayerNameChange = { playerBName = it },
                playersList = teamBPlayers,
                onAddPlayer = {
                    if (playerBName.isNotBlank()) {
                        teamBPlayers.add(playerBName)
                        playerBName = ""
                    }
                },
                isStarting = isStarting
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { 
                    onMatchStarted(teamAName, teamBName, tournamentName, teamAPlayers.toList(), teamBPlayers.toList()) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isStarting && tournamentName.isNotBlank() && teamAName.isNotBlank() && teamBName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                if (isStarting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                } else {
                    Text("START MATCH 🎯", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TeamInputSection(
    label: String,
    teamName: String,
    onTeamNameChange: (String) -> Unit,
    playerName: String,
    onPlayerNameChange: (String) -> Unit,
    playersList: List<String>,
    onAddPlayer: () -> Unit,
    isStarting: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BlackSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = teamName,
                onValueChange = onTeamNameChange,
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isStarting,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangePrimary,
                    unfocusedBorderColor = GrayText,
                    focusedLabelColor = OrangePrimary,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = playerName,
                    onValueChange = onPlayerNameChange,
                    label = { Text("Add Player") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = OrangePrimary) },
                    enabled = !isStarting,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = GrayText,
                        focusedLabelColor = OrangePrimary,
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onAddPlayer, 
                    enabled = !isStarting && playerName.isNotBlank(),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = OrangePrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Player")
                }
            }

            if (playersList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "PLAYERS",
                    style = MaterialTheme.typography.labelSmall,
                    color = OrangePrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = playersList.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = WhiteText
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MatchSetupPreview() {
    GramaKalyanaSportsTheme(darkTheme = true) {
        MatchSetupScreen(sport = "Cricket", isStarting = false, onBackClicked = {}, onMatchStarted = { _, _, _, _, _ -> })
    }
}
