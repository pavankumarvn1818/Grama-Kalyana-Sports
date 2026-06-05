package com.example.gramakalyanasports

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object FirebaseManager {
    private val database = Firebase.database("https://gramakalyana-57772-default-rtdb.firebaseio.com")
    private val matchesRef = database.getReference("matches")
    private val historyRef = database.getReference("history")
    private val playersRef = database.getReference("players")

    fun startMatch(match: Match, onComplete: (String?) -> Unit) {
        try {
            val key = matchesRef.push().key
            if (key == null) {
                onComplete(null)
                return
            }
            match.matchId = key
            match.live = true
            
            matchesRef.child(key).setValue(match)
                .addOnSuccessListener { onComplete(key) }
                .addOnFailureListener { onComplete(null) }
        } catch (e: Exception) {
            onComplete(null)
        }
    }

    fun syncMatch(match: Match) {
        if (match.matchId.isEmpty()) return
        matchesRef.child(match.matchId).setValue(match)
    }

    fun updateScore(matchId: String, score: Score) {
        if (matchId.isEmpty()) return
        matchesRef.child(matchId).child("currentScore").setValue(score)
    }

    fun updateTimer(matchId: String, startTime: Long, elapsed: Long, paused: Boolean) {
        if (matchId.isEmpty()) return
        val updates = mapOf(
            "startTimeMillis" to startTime,
            "elapsedTimeMillis" to elapsed,
            "paused" to paused
        )
        matchesRef.child(matchId).updateChildren(updates)
    }

    fun finalizeMatch(matchId: String, motmName: String) {
        if (matchId.isEmpty()) return
        
        matchesRef.child(matchId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val match = snapshot.getValue(Match::class.java) ?: return
                
                val winner = when {
                    match.currentScore.teamAScore > match.currentScore.teamBScore -> match.teamA.teamName
                    match.currentScore.teamBScore > match.currentScore.teamAScore -> match.teamB.teamName
                    else -> "Draw"
                }
                
                match.live = false
                match.winnerName = winner
                match.manOfTheMatch = motmName
                
                historyRef.child(matchId).setValue(match)
                updateAllPlayerStats(match, motmName)
                matchesRef.child(matchId).removeValue()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateAllPlayerStats(match: Match, motmName: String) {
        match.teamA.playerScores.forEach { (name, score) ->
            addMatchStatsToPlayer(name, match.sportType, score, isMotm = (name == motmName))
        }
        match.teamB.playerScores.forEach { (name, score) ->
            addMatchStatsToPlayer(name, match.sportType, score, isMotm = (name == motmName))
        }
    }

    private fun addMatchStatsToPlayer(playerName: String, sport: String, matchPoints: Int, isMotm: Boolean) {
        playersRef.orderByChild("playerName").equalTo(playerName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (playerSnapshot in snapshot.children) {
                        val player = playerSnapshot.getValue(Player::class.java) ?: continue
                        // Ensure we only update the player in the correct sport
                        if (player.sportType.equals(sport, ignoreCase = true)) {
                            val updates = mutableMapOf<String, Any>(
                                "careerPoints" to player.careerPoints + matchPoints
                            )
                            if (isMotm) {
                                updates["manOfTheMatchCount"] = player.manOfTheMatchCount + 1
                            }
                            playerSnapshot.ref.updateChildren(updates)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun addPointsToPlayer(playerName: String, sport: String, points: Int) {
        if (playerName.isEmpty() || points == 0) return
        playersRef.orderByChild("playerName").equalTo(playerName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (playerSnapshot in snapshot.children) {
                        val player = playerSnapshot.getValue(Player::class.java) ?: continue
                        if (player.sportType.equals(sport, ignoreCase = true)) {
                            playerSnapshot.ref.child("careerPoints").setValue(player.careerPoints + points)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun listenToMatches(onUpdate: (List<Match>) -> Unit) {
        matchesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val matchesList = mutableListOf<Match>()
                for (matchSnapshot in snapshot.children) {
                    val match = matchSnapshot.getValue(Match::class.java)
                    if (match != null) matchesList.add(match)
                }
                onUpdate(matchesList)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    
    fun listenToHistory(onUpdate: (List<Match>) -> Unit) {
        historyRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val matchesList = mutableListOf<Match>()
                for (matchSnapshot in snapshot.children) {
                    val match = matchSnapshot.getValue(Match::class.java)
                    if (match != null) matchesList.add(match)
                }
                onUpdate(matchesList)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun listenToMatch(matchId: String, onUpdate: (Match?) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onUpdate(snapshot.getValue(Match::class.java))
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        matchesRef.child(matchId).addValueEventListener(listener)
        return listener
    }

    fun removeMatchListener(matchId: String, listener: ValueEventListener) {
        matchesRef.child(matchId).removeEventListener(listener)
    }

    fun listenToPlayers(onUpdate: (List<Player>) -> Unit) {
        playersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val playersList = mutableListOf<Player>()
                for (playerSnapshot in snapshot.children) {
                    val player = playerSnapshot.getValue(Player::class.java)
                    if (player != null) playersList.add(player)
                }
                onUpdate(playersList)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun savePlayer(player: Player) {
        // Find player with same name AND sport
        playersRef.orderByChild("playerName").equalTo(player.playerName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var exists = false
                    for (child in snapshot.children) {
                        val existing = child.getValue(Player::class.java)
                        if (existing != null && existing.sportType.equals(player.sportType, ignoreCase = true)) {
                            exists = true
                            break
                        }
                    }
                    if (!exists) {
                        val key = playersRef.push().key ?: return
                        playersRef.child(key).setValue(player.copy(playerId = key))
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
