package com.cs407.boppinit

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.boppinit.databinding.ActivityStatsBinding
import com.cs407.boppinit.databinding.ItemGameHistoryBinding
import com.cs407.boppinit.databinding.ItemLeaderboardEntryBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatsBinding

    // Stats
    private lateinit var gameHistoryAdapter: GameHistoryAdapter
    private lateinit var gameHistoryDatabase: AppDatabase

    // Leaderboard
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val leaderboardRepository = LeaderboardRepository()
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    companion object { private const val RC_SIGN_IN = 9001 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase setup
        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        gameHistoryDatabase = AppDatabase.getDatabase(this)
        setupTabs()
        setupRecyclerView()
        loadLocalStats()
        setupGoogleSignIn()
    }


    // Basic setup
    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> { // Local
                        binding.localStatsLayout.visibility = View.VISIBLE
                        binding.leaderboardLayout.visibility = View.GONE
                    }
                    1 -> { // Leaderboard
                        binding.localStatsLayout.visibility = View.GONE
                        binding.leaderboardLayout.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        // Setup local stats adapter
        gameHistoryAdapter = GameHistoryAdapter()
        binding.rvGameHistory.apply {
            layoutManager = LinearLayoutManager(this@StatsActivity)
            adapter = this@StatsActivity.gameHistoryAdapter
        }

        // Setup leaderboard adapter
        leaderboardAdapter = LeaderboardAdapter()
        binding.leaderboardRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@StatsActivity)
            adapter = this@StatsActivity.leaderboardAdapter
        }
    }


    // Local stats
    private fun loadLocalStats() {
        lifecycleScope.launch(Dispatchers.IO) {
            val games = gameHistoryDatabase.gameHistoryDao().getAllGames()

            val totalGames = games.size
            val totalActivities = games.sumOf { it.activitiesCompleted }

            // Sort games by score in descending order
            val sortedGames = games.sortedByDescending { it.finalScore }

            withContext(Dispatchers.Main) {
                // Update the top stats
                binding.tvTotalGames.text = totalGames.toString()
                binding.tvTotalActivities.text = totalActivities.toString()

                // Update the RecyclerView
                gameHistoryAdapter.submitList(sortedGames)
            }
        }
    }


    // Leaderboard setup
    private fun setupGoogleSignIn() {
        binding.btnSignIn.setOnClickListener {
            startGoogleSignIn()
        }
    }

    private fun startGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Don't care LOL")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                updateUI(null)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        lifecycleScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = withContext(Dispatchers.IO) {
                    auth.signInWithCredential(credential).await()
                }
                updateUI(authResult.user)
            } catch (e: Exception) {
                Toast.makeText(this@StatsActivity, "Authentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
                updateUI(null)
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // User is signed in
            binding.apply {
                btnSignIn.visibility = View.GONE
                leaderboardContent.visibility = View.VISIBLE

                loadLeaderboardData()
                setupScrollButton(user.uid)
            }
        } else {
            binding.apply {
                btnSignIn.visibility = View.VISIBLE
                leaderboardContent.visibility = View.GONE
            }
        }
    }

    private fun setupScrollButton(currentUserId: String) {
        binding.btnScrollToMe.setOnClickListener {
            val position = leaderboardAdapter.getPositionForUser(currentUserId)
            if (position != -1) {
                binding.leaderboardRecyclerView.smoothScrollToPosition(position)
            }
        }
    }

    private fun loadLeaderboardData() {
        lifecycleScope.launch {
            val result = leaderboardRepository.getAllLeaderboardEntries()
            if (result.isSuccess) {
                val entries = result.getOrNull() ?: emptyList()
                leaderboardAdapter.submitList(entries)

                // Find and display user's rank
                val currentUserId = auth.currentUser?.uid
                if (currentUserId != null) {
                    val userPosition = entries.indexOfFirst { it.userId == currentUserId }
                    if (userPosition != -1) {
                        val rank = userPosition + 1
                        binding.tvRankMessage.text = "You are ranked #$rank out of ${entries.size} players"
                    } else {
                        binding.tvRankMessage.text = "You haven't played any games yet"
                    }
                }
            } else if (result.isFailure) {
                Toast.makeText(
                    this@StatsActivity,
                    "Failed to load leaderboard: ${result.exceptionOrNull()?.message ?: "Unknown error"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

class GameHistoryAdapter : RecyclerView.Adapter<GameHistoryAdapter.GameHistoryViewHolder>() {
    private var games = listOf<GameHistory>()

    class GameHistoryViewHolder(private val binding: ItemGameHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(game: GameHistory) {
            binding.apply {
                tvScore.text = game.finalScore.toString()
                tvActivities.text = "${game.activitiesCompleted} activities"
                tvDifficulty.text = game.difficulty.name.lowercase().capitalize()
                tvGameMode.text = when(game.gameMode) {
                    GameMode.SOLO -> "Solo"
                    GameMode.COOP -> "Co-op (${game.numPlayers} players)"
                }
                tvDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(Date(game.date))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameHistoryViewHolder {
        val binding = ItemGameHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GameHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameHistoryViewHolder, position: Int) {
        holder.bind(games[position])
    }

    override fun getItemCount() = games.size

    fun submitList(newGames: List<GameHistory>) {
        games = newGames
        notifyDataSetChanged()
    }
}

class LeaderboardAdapter : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {
    private var entries = listOf<LeaderboardEntry>()

    class ViewHolder(private val binding: ItemLeaderboardEntryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: LeaderboardEntry, position: Int) {
            binding.apply {
                rankTextView.text = "${position + 1}"  // Add rank based on position
                nameTextView.text = entry.displayName
                scoreTextView.text = entry.score.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardEntryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(entries[position], position)
    }

    override fun getItemCount() = entries.size

    fun submitList(newEntries: List<LeaderboardEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }

    fun getPositionForUser(userId: String): Int {
        return entries.indexOfFirst { it.userId == userId }
    }
}
