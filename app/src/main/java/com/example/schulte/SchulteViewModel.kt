package com.example.schulte

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import com.example.schulte.model.GameMode
import com.example.schulte.model.SchulteRecord
import org.json.JSONArray
import org.json.JSONObject

class SchulteViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences =
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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
    }

    private fun toJson(r: SchulteRecord): JSONObject = JSONObject().apply {
        put("mode", r.mode.name)
        put("ms", r.elapsedMs)
        put("mistakes", r.mistakes)
        put("ts", r.timestamp)
    }

    companion object {
        private const val PREFS_NAME = "schulte_prefs"
        private const val KEY_RECORDS = "history_records"
    }
}