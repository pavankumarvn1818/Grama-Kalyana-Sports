package com.example.gramakalyanasports

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.gramakalyanasports.ui.theme.GramaKalyanaSportsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GramaKalyanaSportsTheme {
                AppScreen()
            }
        }
    }
}

@Composable
fun AppScreen() {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf<Screen>(Screen.RoleSelection) }
    var selectedRole by remember { mutableStateOf("") }
    var selectedSport by remember { mutableStateOf("") }
    
    // UI State for loading
    var isStartingMatch by remember { mutableStateOf(false) }
    
    // Match State
    var currentMatchId by remember { mutableStateOf("") }
    var teamAName by remember { mutableStateOf("") }
    var teamBName by remember { mutableStateOf("") }
    var tournamentName by remember { mutableStateOf("") }
    var teamAPlayers by remember { mutableStateOf(emptyList<String>()) }
    var teamBPlayers by remember { mutableStateOf(emptyList<String>()) }
    
    // Selected match for detail view
    var selectedMatchData by remember { mutableStateOf<Match?>(null) }

    when (currentScreen) {
        Screen.RoleSelection -> {
            RoleSelectionScreen { role ->
                selectedRole = role
                currentScreen = Screen.SportsSelection
            }
        }

        Screen.SportsSelection -> {
            SportsSelection(
                onBackClicked = { currentScreen = Screen.RoleSelection },
                onSportSelected = { sport ->
                    selectedSport = sport
                    when (selectedRole) {
                        "Admin" -> currentScreen = Screen.AdminMatchSetup
                        "Player" -> currentScreen = Screen.PlayerStats
                        "Viewer" -> currentScreen = Screen.FanView
                    }
                }
            )
        }

        Screen.AdminMatchSetup -> {
            MatchSetupScreen(
                sport = selectedSport,
                isStarting = isStartingMatch,
                onBackClicked = { currentScreen = Screen.SportsSelection },
                onMatchStarted = { a, b, t, playersA, playersB ->
                    isStartingMatch = true
                    
                    // Save players to database with the specific sport type
                    playersA.forEach { name -> 
                        FirebaseManager.savePlayer(Player(playerName = name, sportType = selectedSport)) 
                    }
                    playersB.forEach { name -> 
                        FirebaseManager.savePlayer(Player(playerName = name, sportType = selectedSport)) 
                    }

                    // Create match with data entered by user
                    val newMatch = Match(
                        tournamentName = t,
                        sportType = selectedSport,
                        teamA = Team(teamName = a, players = playersA),
                        teamB = Team(teamName = b, players = playersB),
                        live = true
                    )

                    FirebaseManager.startMatch(newMatch) { id ->
                        isStartingMatch = false
                        if (id != null) {
                            currentMatchId = id
                            teamAName = a
                            teamBName = b
                            tournamentName = t
                            teamAPlayers = playersA
                            teamBPlayers = playersB
                            currentScreen = Screen.LiveScoring
                            Toast.makeText(context, "Match Started Successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to connect to Firebase. Check internet and Database rules.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )
        }

        Screen.LiveScoring -> {
            LiveScoringScreen(
                matchId = currentMatchId,
                sport = selectedSport,
                teamA = teamAName,
                teamB = teamBName,
                tournament = tournamentName,
                teamAPlayers = teamAPlayers,
                teamBPlayers = teamBPlayers,
                onMatchEnd = {
                    currentScreen = Screen.RoleSelection
                    Toast.makeText(context, "Match Completed", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Screen.PlayerStats -> {
            PlayerStatsScreen(
                sportType = selectedSport, // Now passing the sport filter
                onBackClicked = { currentScreen = Screen.SportsSelection }
            )
        }

        Screen.FanView -> {
            FanViewScreen(
                onBackClicked = { currentScreen = Screen.SportsSelection },
                onMatchClicked = { match ->
                    selectedMatchData = match
                    currentScreen = Screen.FanMatchDetail
                }
            )
        }

        Screen.FanMatchDetail -> {
            selectedMatchData?.let { match ->
                FanMatchDetailScreen(
                    matchId = match.matchId,
                    onBackClicked = { currentScreen = Screen.FanView }
                )
            }
        }
    }
}

enum class Screen {
    RoleSelection,
    SportsSelection,
    AdminMatchSetup,
    LiveScoring,
    PlayerStats,
    FanView,
    FanMatchDetail
}