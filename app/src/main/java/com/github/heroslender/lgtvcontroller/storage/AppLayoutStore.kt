package com.github.heroslender.lgtvcontroller.storage

import android.content.Context
import com.github.heroslender.lgtvcontroller.domain.model.App
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Como o usuário organizou os apps de uma TV: a ordem escolhida e quais
 * ficam escondidos na tela inicial. Apps novos (que não estão em [order])
 * aparecem no fim, na ordem que a TV informa.
 */
data class AppLayout(
    val order: List<String> = emptyList(),
    val hidden: Set<String> = emptySet(),
) {
    /** Todos os apps, na ordem escolhida (inclusive os escondidos). */
    fun sort(apps: List<App>): List<App> {
        if (order.isEmpty()) return apps
        val position = order.withIndex().associate { (i, id) -> id to i }
        return apps.withIndex()
            .sortedWith(compareBy({ position[it.value.id] ?: Int.MAX_VALUE }, { it.index }))
            .map { it.value }
    }
}

/**
 * Guarda o [AppLayout] de cada TV em SharedPreferences, separado do banco Room
 * para não exigir migração do esquema.
 */
@Singleton
class AppLayoutStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences("app_layout", Context.MODE_PRIVATE)
    private val layouts = MutableStateFlow<Map<String, AppLayout>>(emptyMap())

    fun layout(tvId: String): Flow<AppLayout> {
        if (tvId !in layouts.value) {
            val loaded = load(tvId)
            layouts.update { if (tvId in it) it else it + (tvId to loaded) }
        }
        return layouts.map { it[tvId] ?: AppLayout() }
    }

    fun save(tvId: String, layout: AppLayout) {
        layouts.update { it + (tvId to layout) }
        val json = JSONObject()
            .put("order", JSONArray(layout.order))
            .put("hidden", JSONArray(layout.hidden.toList()))
        prefs.edit().putString(key(tvId), json.toString()).apply()
    }

    fun reset(tvId: String) {
        layouts.update { it + (tvId to AppLayout()) }
        prefs.edit().remove(key(tvId)).apply()
    }

    private fun load(tvId: String): AppLayout {
        val raw = prefs.getString(key(tvId), null) ?: return AppLayout()
        return try {
            val json = JSONObject(raw)
            AppLayout(
                order = json.optJSONArray("order").toStringList(),
                hidden = json.optJSONArray("hidden").toStringList().toSet(),
            )
        } catch (e: Exception) {
            AppLayout()
        }
    }

    private fun JSONArray?.toStringList(): List<String> =
        if (this == null) emptyList() else List(length()) { getString(it) }

    private fun key(tvId: String) = "tv_$tvId"
}
