package com.example.gramakalyanasports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gramakalyanasports.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun LiveScoringScreen(
    matchId: String,
    sport: String,
    teamA: String,
    teamB: String,
    tournament: String,
    teamAPlayers: List<String>,
    teamBPlayers: List<String>,
    onMatchEnd: () -> Unit
) {
    var matchState by remember { mutableStateOf(Match(
        matchId = matchId,
        tournamentName = tournament,
        sportType = sport,
        teamA = Team(teamName = teamA, players = teamAPlayers),
        teamB = Team(teamName = teamB, players = teamBPlayers)
    )) }
    
    val scoreHistory = remember { mutableStateListOf<Match>() }
    var showEndDialog by remember { mutableStateOf(false) }

    var elapsedTime by remember { mutableLongStateOf(0L) }
    var isTimerRunning by remember { mutableStateOf(false) }
    
    val haptic = LocalHapticFeedback.current

    val isCricket = sport.equals("Cricket", ignoreCase = true)

    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            val startTime = System.currentTimeMillis() - elapsedTime
            while (isTimerRunning) {
                elapsedTime = System.currentTimeMillis() - startTime
                if (elapsedTime % 5000 < 1000) {
                    FirebaseManager.updateTimer(matchId, startTime, elapsedTime, !isTimerRunning)
                }
                delay(1000)
            }
        }
    }

    fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    fun formatOvers(balls: Int): String {
        val overs = balls / 6
        val remainingBalls = balls % 6
        return "$overs.$remainingBalls"
    }

    fun updateMatchState(newMatch: Match, timeLabel: String, actionMsg: String) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        scoreHistory.add(matchState.copy())
        
        val newEntry = CommentaryEntry(
            id = System.currentTimeMillis().toString(),
            message = actionMsg,
            timeLabel = timeLabel
        )
        val updatedCommentary = listOf(newEntry) + newMatch.currentScore.commentary

        matchState = newMatch.copy().apply {
            currentScore.commentary = updatedCommentary.take(50)
            currentScore.lastUpdated = System.currentTimeMillis()
        }
    }

    fun undo() {
        if (scoreHistory.isNotEmpty()) {
            matchState = scoreHistory.removeAt(scoreHistory.size - 1)
        }
    }

    LaunchedEffect(matchState) {
        if (matchId.isNotEmpty()) {
            FirebaseManager.syncMatch(matchState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(), 
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tournament, style = MaterialTheme.typography.titleMedium, color = GrayText)
                Text(text = "LIVE: $sport", color = OrangePrimary, fontWeight = FontWeight.ExtraBold)
            }
            
            Card(
                colors = CardDefaults.cardColors(containerColor = BlackContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Text(
                        text = formatTime(elapsedTime), 
                        style = MaterialTheme.typography.headlineSmall, 
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    IconButton(onClick = { isTimerRunning = !isTimerRunning }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow, 
                            contentDescription = null,
                            tint = WhiteText
                        )
                    }
                }
            }

            IconButton(onClick = { undo() }, enabled = scoreHistory.isNotEmpty()) {
                Icon(Icons.Default.Refresh, null, tint = if(scoreHistory.isNotEmpty()) OrangePrimary else GrayText)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ScoreColumn(
                teamName = teamA,
                score = matchState.currentScore.teamAScore,
                wickets = if (isCricket) matchState.currentScore.teamAWickets else null,
                overs = if (isCricket) matchState.currentScore.teamAOvers else null,
                sets = if (sport == "Volleyball") matchState.currentScore.teamASets else null,
                sport = sport,
                players = teamAPlayers,
                onUpdate = { addedScore, addedWicket, addedBall, addedSet, action, playerName ->
                    val currentBalls = matchState.currentScore.teamAOvers.split(".").let { if(it.size == 2) it[0].toInt() * 6 + it[1].toInt() else 0 }
                    val updatedPlayerScores = matchState.teamA.playerScores.toMutableMap()
                    if (playerName != null && addedScore > 0) {
                        updatedPlayerScores[playerName] = (updatedPlayerScores[playerName] ?: 0) + addedScore
                        FirebaseManager.addPointsToPlayer(playerName, sport, addedScore)
                    }

                    val nextMatch = matchState.copy(
                        teamA = matchState.teamA.copy(playerScores = updatedPlayerScores),
                        currentScore = matchState.currentScore.copy(
                            teamAScore = matchState.currentScore.teamAScore + addedScore,
                            teamAWickets = matchState.currentScore.teamAWickets + addedWicket,
                            teamAOvers = if (isCricket) formatOvers(currentBalls + addedBall) else matchState.currentScore.teamAOvers,
                            teamASets = matchState.currentScore.teamASets + addedSet
                        )
                    )
                    updateMatchState(nextMatch, if(isCricket) formatOvers(currentBalls + addedBall) else formatTime(elapsedTime), "$playerName: $action")
                }
            )

            ScoreColumn(
                teamName = teamB,
                score = matchState.currentScore.teamBScore,
                wickets = if (isCricket) matchState.currentScore.teamBWickets else null,
                overs = if (isCricket) matchState.currentScore.teamBOvers else null,
                sets = if (sport == "Volleyball") matchState.currentScore.teamBSets else null,
                sport = sport,
                players = teamBPlayers,
                onUpdate = { addedScore, addedWicket, addedBall, addedSet, action, playerName ->
                    val currentBalls = matchState.currentScore.teamBOvers.split(".").let { if(it.size == 2) it[0].toInt() * 6 + it[1].toInt() else 0 }
                    val updatedPlayerScores = matchState.teamB.playerScores.toMutableMap()
                    if (playerName != null && addedScore > 0) {
                        updatedPlayerScores[playerName] = (updatedPlayerScores[playerName] ?: 0) + addedScore
                        FirebaseManager.addPointsToPlayer(playerName, sport, addedScore)
                    }

                    val nextMatch = matchState.copy(
                        teamB = matchState.teamB.copy(playerScores = updatedPlayerScores),
                        currentScore = matchState.currentScore.copy(
                            teamBScore = matchState.currentScore.teamBScore + addedScore,
                            teamBWickets = matchState.currentScore.teamBWickets + addedWicket,
                            teamBOvers = if (isCricket) formatOvers(currentBalls + addedBall) else matchState.currentScore.teamBOvers,
                            teamASets = matchState.currentScore.teamASets + addedSet
                        )
                    )
                    updateMatchState(nextMatch, if(isCricket) formatOvers(currentBalls + addedBall) else formatTime(elapsedTime), "$playerName: $action")
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = { showEndDialog = true }, 
            modifier = Modifier.fillMaxWidth().height(56.dp), 
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Text("FINISH MATCH", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = WhiteText)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showEndDialog) {
        EndMatchDialog(
            teamAName = teamA, 
            teamBName = teamB, 
            teamAScore = matchState.currentScore.teamAScore, 
            teamBScore = matchState.currentScore.teamBScore,
            players = teamAPlayers + teamBPlayers, 
            onConfirm = { motm: String -> FirebaseManager.finalizeMatch(matchId, motm); onMatchEnd() },
            onDismiss = { showEndDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreColumn(
    teamName: String, score: Int, wickets: Int?, overs: String?, sets: Int?, sport: String, players: List<String>,
    onUpdate: (Int, Int, Int, Int, String, String?) -> Unit
) {
    val isCricket = sport.equals("Cricket", ignoreCase = true)
    
    var selectedPlayer by remember { mutableStateOf(if(players.isNotEmpty()) players[0] else "Unknown") }
    var expanded by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(160.dp)) {
        Text(text = teamName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = WhiteText)
        
        if (players.isNotEmpty()) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                Surface(
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = BlackContainer
                ) {
                    Text(text = "$selectedPlayer ▾", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.bodySmall, color = OrangePrimary)
                }
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    players.forEach { p -> DropdownMenuItem(text = { Text(p) }, onClick = { selectedPlayer = p; expanded = false }) }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isCricket) {
            Text(text = "$score / ${wickets ?: 0}", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = WhiteText)
            Text(text = "($overs ov)", style = MaterialTheme.typography.bodyMedium, color = GrayText)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ScoringButton("1", OrangePrimary) { onUpdate(1, 0, 1, 0, "1 Run", selectedPlayer) }
                ScoringButton("4", OrangePrimary) { onUpdate(4, 0, 1, 0, "4 Runs", selectedPlayer) }
                ScoringButton("6", OrangePrimary) { onUpdate(6, 0, 1, 0, "6 Runs", selectedPlayer) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onUpdate(0, 1, 1, 0, "Wicket", selectedPlayer) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), modifier = Modifier.weight(1f)) { Text("W") }
            }
        } else if (sport == "Volleyball") {
            Text(text = score.toString(), fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = WhiteText)
            Text(text = "Sets: ${sets ?: 0}", color = GrayText)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onUpdate(1, 0, 0, 0, "Point", selectedPlayer) }, colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)) { Text("+1 POINT", color = Color.Black) }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onUpdate(0, 0, 0, 1, "Set", null) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) { Text("WIN SET", color = WhiteText) }
        } else if (sport == "Kabaddi") {
            Text(text = score.toString(), fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = WhiteText)
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(onClick = { onUpdate(1, 0, 0, 0, "Raid", selectedPlayer) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)) { Text("RAID", color = Color.Black) }
                    Button(onClick = { onUpdate(1, 0, 0, 0, "Tack", selectedPlayer) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)) { Text("TACK", color = Color.Black) }
                }
                Button(onClick = { onUpdate(2, 0, 0, 0, "All Out", null) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("ALL OUT (+2)") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndMatchDialog(
    teamAName: String,
    teamBName: String,
    teamAScore: Int,
    teamBScore: Int,
    players: List<String>,
    onConfirm: (motm: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMotm by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BlackSurface,
        title = { Text("🏆 MATCH ENDED", color = OrangePrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Final Score:", color = GrayText)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(teamAName, color = WhiteText)
                    Text(teamAScore.toString(), color = OrangePrimary, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(teamBName, color = WhiteText)
                    Text(teamBScore.toString(), color = OrangePrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Select Man of the Match:", color = OrangePrimary, fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedMotm,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Winner") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = WhiteText, unfocusedTextColor = WhiteText)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        players.forEach { p -> DropdownMenuItem(text = { Text(p) }, onClick = { selectedMotm = p; expanded = false }) }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedMotm) }, enabled = selectedMotm.isNotEmpty(), colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)) {
                Text("FINISH", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = GrayText) }
        }
    )
}

@Composable
fun ScoringButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(label, color = Color.Black, fontWeight = FontWeight.Bold)
    }
}
