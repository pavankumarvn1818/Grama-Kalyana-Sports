package com.example.gramakalyanasports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gramakalyanasports.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FanViewScreen(
    onBackClicked: () -> Unit,
    onMatchClicked: (Match) -> Unit
) {
    var liveMatches by remember { mutableStateOf(emptyList<Match>()) }
    var pastMatches by remember { mutableStateOf(emptyList<Match>()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedSportFilter by remember { mutableStateOf("All") }
    
    val tabs = listOf("LIVE SCORES", "PAST RESULTS")
    val sports = listOf("All", "Cricket", "Kabaddi", "Volleyball")

    LaunchedEffect(Unit) {
        FirebaseManager.listenToMatches { updatedLive ->
            liveMatches = updatedLive.sortedByDescending { it.timestamp }
        }
        FirebaseManager.listenToHistory { updatedHistory ->
            pastMatches = updatedHistory.sortedByDescending { it.timestamp }
        }
    }

    val displayedMatches = if (selectedTab == 0) liveMatches else pastMatches
    val filteredMatches = displayedMatches.filter { match ->
        if (selectedSportFilter == "All") true 
        else match.sportType.equals(selectedSportFilter, ignoreCase = true)
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
                text = "SPORTS PORTAL",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = OrangePrimary
            )
        }

        TabRow(
            selectedTabIndex = selectedTab, 
            containerColor = Color.Transparent,
            contentColor = OrangePrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = OrangePrimary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { 
                        Text(
                            text = title, 
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == index) OrangePrimary else GrayText
                        ) 
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List, 
                contentDescription = null, 
                modifier = Modifier.size(20.dp), 
                tint = GrayText
            )
            Spacer(modifier = Modifier.width(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sports) { sport ->
                    FilterChip(
                        selected = selectedSportFilter == sport,
                        onClick = { selectedSportFilter = sport },
                        label = { Text(sport) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OrangePrimary,
                            selectedLabelColor = Color.Black,
                            labelColor = GrayText,
                            containerColor = BlackContainer
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedSportFilter == sport,
                            borderColor = GrayText,
                            selectedBorderColor = OrangePrimary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredMatches.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (selectedTab == 0) "No live $selectedSportFilter matches." else "No $selectedSportFilter results found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GrayText
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredMatches) { match ->
                    LiveMatchCard(
                        match = match,
                        onClick = { onMatchClicked(match) }
                    )
                }
            }
        }
    }
}

@Composable
fun LiveMatchCard(
    match: Match,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = BlackSurface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (match.live) {
                    Surface(
                        color = Color.Red,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "● LIVE",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "FINISHED",
                        style = MaterialTheme.typography.labelSmall,
                        color = GrayText,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = match.tournamentName.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = OrangePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = match.sportType.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = GrayText,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val isCricket = match.sportType.equals("Cricket", ignoreCase = true)
            val isVolleyball = match.sportType.equals("Volleyball", ignoreCase = true)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FanTeamScoreBox(
                    name = match.teamA.teamName,
                    score = match.currentScore.teamAScore,
                    wickets = if (isCricket) match.currentScore.teamAWickets else null,
                    overs = if (isCricket) match.currentScore.teamAOvers else null,
                    sets = if (isVolleyball) match.currentScore.teamASets else null
                )
                
                Text(
                    text = "VS", 
                    fontWeight = FontWeight.Black, 
                    fontSize = 20.sp, 
                    color = OrangeSecondary.copy(alpha = 0.5f)
                )
                
                FanTeamScoreBox(
                    name = match.teamB.teamName,
                    score = match.currentScore.teamBScore,
                    wickets = if (isCricket) match.currentScore.teamBWickets else null,
                    overs = if (isCricket) match.currentScore.teamBOvers else null,
                    sets = if (isVolleyball) match.currentScore.teamBSets else null,
                    alignEnd = true
                )
            }

            if (!match.live) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp), 
                    thickness = 1.dp, 
                    color = BlackContainer
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (match.winnerName == "Draw") "MATCH DRAW" else "🏆 WINNER: ${match.winnerName.uppercase()}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = OrangePrimary
                    )
                    
                    if (match.manOfTheMatch.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                            Icon(Icons.Default.Star, null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
                            Text(
                                text = " MOTM: ${match.manOfTheMatch}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = WhiteText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FanTeamScoreBox(
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
            fontWeight = FontWeight.Bold,
            color = WhiteText
        )
        
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = if (wickets != null) "$score/$wickets" else score.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = OrangePrimary
            )
        }
        
        if (overs != null) {
            Text(
                text = "($overs ov)",
                style = MaterialTheme.typography.bodySmall,
                color = GrayText
            )
        }

        if (sets != null) {
            Text(
                text = "Sets: $sets",
                style = MaterialTheme.typography.bodySmall,
                color = OrangeSecondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
