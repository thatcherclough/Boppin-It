package com.cs407.boppinit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.boppinit.databinding.ActivityStatsBinding
import com.cs407.boppinit.databinding.ItemGameHistoryBinding
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatsBinding
    private lateinit var adapter: GameHistoryAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        setupTabs()
        setupRecyclerView()
        loadLocalStats()
    }

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
        adapter = GameHistoryAdapter()
        binding.rvGameHistory.apply {
            layoutManager = LinearLayoutManager(this@StatsActivity)
            adapter = this@StatsActivity.adapter
        }
    }

    private fun loadLocalStats() {
        lifecycleScope.launch(Dispatchers.IO) {
            val games = database.gameHistoryDao().getAllGames()

            val totalGames = games.size
            val totalActivities = games.sumOf { it.activitiesCompleted }

            // Sort games by score in descending order
            val sortedGames = games.sortedByDescending { it.finalScore }

            withContext(Dispatchers.Main) {
                // Update the top stats
                binding.tvTotalGames.text = totalGames.toString()
                binding.tvTotalActivities.text = totalActivities.toString()

                // Update the RecyclerView
                adapter.submitList(sortedGames)
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