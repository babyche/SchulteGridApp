package com.example.schulte

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import com.example.schulte.model.GameMode
import com.example.schulte.model.SchulteRecord
import com.example.schulte.model.TrendSlot
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

class SchulteViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences =
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val backupFile: File = File(
        application.getExternalFilesDir(null) ?: application.filesDir,
        BACKUP_FILE_NAME,
    )

    init {
        restoreFromBackupIfNeeded()
        syncBackup()
    }

    fun bestTime(mode: GameMode): Long? {
        val ms = prefs.getLong(mode.name, -1L)
        return if (ms < 0) null else ms
    }

    /** Returns true if this is a new record (or first ever). */
    fun submitResult(mode: GameMode, elapsedMs: Long): Boolean {
        val current = bestTime(mode)
        if (current == null || elapsedMs < current) {
            prefs.edit().putLong(mode.name, elapsedMs).apply()
            return true
        }
        return false
    }

    fun addRecord(record: SchulteRecord) {
        val all = loadRecords().toMutableList() + record
        val arr = JSONArray()
        all.forEach { r ->
            arr.put(toJson(r))
        }
        prefs.edit().putString(KEY_RECORDS, arr.toString()).apply()
        syncBackup()
    }

    fun loadRecords(): List<SchulteRecord> {
        val raw = prefs.getString(KEY_RECORDS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.getJSONObject(i)
                runCatching {
                    SchulteRecord(
                        mode = GameMode.valueOf(o.getString("mode")),
                        elapsedMs = o.getLong("ms"),
                        mistakes = o.getInt("mistakes"),
                        timestamp = o.getLong("ts"),
                    )
                }.getOrNull()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun clearRecords() {
        prefs.edit().remove(KEY_RECORDS).apply()
        runCatching { backupFile.delete() }
    }

    /** Average elapsed time per training day (last 30 days with records), split by mode. */
    fun dailyTrend(): List<TrendSlot> = buildTrend(
        keyPattern = "yyyy-MM-dd",
        labelPattern = "M/d",
        lastBuckets = 30,
        aggregate = ::averageMs,
    )

    /** Average elapsed time per month (last 12 months with records), split by mode. */
    fun monthlyTrend(): List<TrendSlot> = buildTrend(
        keyPattern = "yyyy-MM",
        labelPattern = "yyyy-MM",
        lastBuckets = 12,
        aggregate = ::averageMs,
    )

    /** Best elapsed time per training day (last 30 days with records), split by mode. */
    fun dailyBestTrend(): List<TrendSlot> = buildTrend(
        keyPattern = "yyyy-MM-dd",
        labelPattern = "M/d",
        lastBuckets = 30,
        aggregate = ::bestMs,
    )

    /** Best elapsed time per month (last 12 months with records), split by mode. */
    fun monthlyBestTrend(): List<TrendSlot> = buildTrend(
        keyPattern = "yyyy-MM",
        labelPattern = "yyyy-MM",
        lastBuckets = 12,
        aggregate = ::bestMs,
    )

    /**
     * Recent individual training times for a single mode, each session as one
     * slot ordered oldest → newest (label = ordinal within the window).
     */
    fun recentSessions(mode: GameMode, count: Int): List<TrendSlot> {
        val sessions = loadRecords()
            .filter { it.mode == mode }
            .sortedBy { it.timestamp }
            .takeLast(count)
        return sessions.mapIndexed { i, r ->
            TrendSlot(
                label = "${i + 1}",
                fourMs = if (mode == GameMode.FOUR) r.elapsedMs else null,
                fiveMs = if (mode == GameMode.FIVE) r.elapsedMs else null,
            )
        }
    }

    fun deleteRecord(timestamp: Long) {
        val all = loadRecords().filterNot { it.timestamp == timestamp }
        val arr = JSONArray()
        all.forEach { r ->
            arr.put(toJson(r))
        }
        prefs.edit().putString(KEY_RECORDS, arr.toString()).apply()
        syncBackup()
    }

    private fun restoreFromBackupIfNeeded() {
        if (prefs.getString(KEY_RECORDS, null) != null) return
        val json = runCatching { backupFile.readText() }.getOrNull() ?: return
        if (json.isBlank()) return
        prefs.edit().putString(KEY_RECORDS, json).apply()
    }

    private fun syncBackup() {
        val json = prefs.getString(KEY_RECORDS, null) ?: return
        runCatching { backupFile.writeText(json) }
    }

    private fun buildTrend(
        keyPattern: String,
        labelPattern: String,
        lastBuckets: Int,
        aggregate: (List<SchulteRecord>) -> Long,
    ): List<TrendSlot> {
        val records = loadRecords()
        if (records.isEmpty()) return emptyList()

        val keyFormat = SimpleDateFormat(keyPattern, Locale.CHINA)
        val labelFormat = SimpleDateFormat(labelPattern, Locale.CHINA)
        val buckets = LinkedHashMap<String, MutableList<SchulteRecord>>()
        records.forEach { r ->
            val key = keyFormat.format(Date(r.timestamp))
            buckets.getOrPut(key) { mutableListOf() }.add(r)
        }

        return buckets.keys.toList().takeLast(lastBuckets).map { key ->
            val list = buckets.getValue(key)
            val byMode = list.groupBy { it.mode }
            TrendSlot(
                label = labelFormat.format(Date(list.first().timestamp)),
                fourMs = byMode[GameMode.FOUR]?.let(aggregate),
                fiveMs = byMode[GameMode.FIVE]?.let(aggregate),
            )
        }
    }

    private fun averageMs(records: List<SchulteRecord>): Long =
        (records.sumOf { it.elapsedMs }.toDouble() / records.size).roundToLong()

    private fun bestMs(records: List<SchulteRecord>): Long =
        records.minOf { it.elapsedMs }

    private fun toJson(r: SchulteRecord): JSONObject = JSONObject().apply {
        put("mode", r.mode.name)
        put("ms", r.elapsedMs)
        put("mistakes", r.mistakes)
        put("ts", r.timestamp)
    }

    companion object {
        private const val PREFS_NAME = "schulte_prefs"
        private const val KEY_RECORDS = "history_records"
        private const val BACKUP_FILE_NAME = "schulte_records_backup.json"
    }
}