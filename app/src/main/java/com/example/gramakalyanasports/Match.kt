package com.example.gramakalyanasports

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName

@IgnoreExtraProperties
data class Match(
    var matchId: String = "",
    var tournamentName: String = "",
    var sportType: String = "",
    var teamA: Team = Team(),
    var teamB: Team = Team(),
    var currentScore: Score = Score(),
    @get:PropertyName("live")
    @set:PropertyName("live")
    var live: Boolean = false,
    var winnerName: String = "",
    var manOfTheMatch: String = "",
    var startTimeMillis: Long = 0L,
    var elapsedTimeMillis: Long = 0L,
    var isPaused: Boolean = true,
    var timestamp: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class Team(
    var teamId: String = "",
    var teamName: String = "",
    var players: List<String> = emptyList(),
    var playerScores: Map<String, Int> = emptyMap()
)

@IgnoreExtraProperties
data class Score(
    var teamAScore: Int = 0,
    var teamBScore: Int = 0,
    var teamAWickets: Int = 0,
    var teamBWickets: Int = 0,
    var teamAOvers: String = "0.0",
    var teamBOvers: String = "0.0",
    var teamASets: Int = 0,
    var teamBSets: Int = 0,
    var currentInnings: Int = 1,
    var battingTeam: String = "",
    var targetScore: Int = 0,
    var matchStatus: String = "Match Started",
    var commentary: List<CommentaryEntry> = emptyList(),
    var lastUpdated: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class CommentaryEntry(
    var id: String = "",
    var message: String = "",
    var timeLabel: String = "",
    var timestamp: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class Player(
    var playerId: String = "",
    var playerName: String = "",
    var sportType: String = "", // Added to filter players by sport
    var jerseyNumber: Int = 0,
    var careerPoints: Int = 0,
    var manOfTheMatchCount: Int = 0
)
