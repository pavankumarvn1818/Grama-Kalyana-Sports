package com.example.gramakalyanasports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gramakalyanasports.ui.theme.*

@Composable
fun FanMatchDetailScreen(
    matchId: String,
    onBackClicked: () -> Unit
) {
    var match by remember { mutableStateOf<Match?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(matchId) {
        val listener = FirebaseManager.listenToMatch(matchId) { updatedMatch ->
            match = updatedMatch
            isLoading = false
        }
        onDispose {
            FirebaseManager.removeMatchListener(matchId, listener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                    contentDescription = "Back",
                    tint = OrangePrimary
                )
            }
            Text(
                text = "MATCH CENTER",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = OrangePrimary
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangePrimary)
            }
        } else if (match == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Match data not found.", color = GrayText)
            }
        } else {
            val currentMatch = match!!
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            text = currentMatch.tournamentName.uppercase(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = OrangePrimary,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = currentMatch.sportType.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = GrayText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    ScoreOverviewCard(currentMatch)
                }

                if (!currentMatch.live) {
                    item {
                        MatchResultCard(currentMatch)
                    }
                }

                // Player Scorecard Section
                item {
                    Text(
                        text = "PLAYER SCORECARD",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = OrangePrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ScorecardColumn(title = currentMatch.teamA.teamName, scores = currentMatch.teamA.playerScores, modifier = Modifier.weight(1f))
                        ScorecardColumn(title = currentMatch.teamB.teamName, scores = currentMatch.teamB.playerScores, modifier = Modifier.weight(1f))
                    }
                }

                item {
                    Text(
                        text = "LIVE COMMENTARY",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = OrangePrimary,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                if (currentMatch.currentScore.commentary.isEmpty()) {
                    item {
                        Text("Waiting for match actions...", color = GrayText, style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    items(currentMatch.currentScore.commentary) { entry ->
                        CommentaryItem(entry)
                    }
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun ScorecardColumn(title: String, scores: Map<String, Int>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BlackSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title.uppercase(), 
                style = MaterialTheme.typography.labelLarge, 
                fontWeight = FontWeight.Black, 
                color = OrangeSecondary,
                maxLines = 1
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BlackContainer)
            if (scores.isEmpty()) {
                Text("No scores", style = MaterialTheme.typography.bodySmall, color = GrayText)
            } else {
                scores.forEach { (name, score) ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = name, style = MaterialTheme.typography.bodySmall, color = WhiteText, maxLines = 1, modifier = Modifier.weight(1f))
                        Text(text = score.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = OrangePrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreOverviewCard(match: Match) {
    val isCricket = match.sportType.equals("Cricket", ignoreCase = true)
    val isVolleyball = match.sportType.equals("Volleyball", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = BlackSurface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DetailTeamBox(
                    name = match.teamA.teamName,
                    score = match.currentScore.teamAScore,
                    wickets = if (isCricket) match.currentScore.teamAWickets else null,
                    overs = if (isCricket) match.currentScore.teamAOvers else null,
                    sets = if (isVolleyball) match.currentScore.teamASets else null
                )

                Text(
                    text = "VS", 
                    fontWeight = FontWeight.Black, 
                    fontSize = 24.sp, 
                    color = OrangeSecondary.copy(alpha = 0.3f)
                )

                DetailTeamBox(
                    name = match.teamB.teamName,
                    score = match.currentScore.teamBScore,
                    wickets = if (isCricket) match.currentScore.teamBWickets else null,
                    overs = if (isCricket) match.currentScore.teamBOvers else null,
                    sets = if (isVolleyball) match.currentScore.teamBSets else null,
                    alignEnd = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                color = if (match.live) Color.Red else Color.DarkGray,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (match.live) "● LIVE" else "FINISHED",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = match.currentScore.matchStatus,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = OrangeTertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MatchResultCard(match: Match) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, OrangeSecondary.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🏆 FINAL RESULT", style = MaterialTheme.typography.labelMedium, color = GrayText, fontWeight = FontWeight.Bold)
            Text(
                text = if (match.winnerName == "Draw") "MATCH DRAW" else "${match.winnerName.uppercase()} WON!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = OrangePrimary
            )
            
            if (match.manOfTheMatch.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
                    Text(text = " PLAYER OF THE MATCH: ${match.manOfTheMatch.uppercase()}", fontWeight = FontWeight.Bold, color = WhiteText, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun CommentaryItem(entry: CommentaryEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BlackSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = BlackContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = entry.timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = OrangePrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = entry.message,
                style = MaterialTheme.typography.bodyMedium,
                color = WhiteText
            )
        }
    }
}

@Composable
fun DetailTeamBox(
    name: String,
    score: Int,
    wickets: Int? = null,
    overs: String? = null,
    sets: Int? = null,
    alignEnd: Boolean = false
) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(
            text = name, 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.Black, 
            color = WhiteText
        )
        
        Text(
            text = if (wickets != null) "$score/$wickets" else score.toString(),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = OrangePrimary
        )
        
        if (overs != null) {
            Text(text = "$overs ov", style = MaterialTheme.typography.bodyMedium, color = GrayText)
        }

        if (sets != null) {
            Text(text = "$sets Sets", style = MaterialTheme.typography.bodyMedium, color = OrangeSecondary, fontWeight = FontWeight.Bold)
        }
    }
}
