package et.matchup.app.data

import et.matchup.app.platform.KeyValueStore
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
private data class CacheEnvelope(
    val savedAt: Long,
    val payload: JsonElement,
)

/**
 * Small JSON document cache. Only real server responses are written here; the
 * UI never manufactures cached content. Keys are namespaced by account where
 * the data is owner-scoped.
 */
class JsonCache(
    private val store: KeyValueStore,
    private val json: Json,
) {
    fun <T> put(key: String, serializer: KSerializer<T>, value: T) {
        val envelope = CacheEnvelope(
            savedAt = nowMillis(),
            payload = json.encodeToJsonElement(serializer, value),
        )
        store.putString(prefix + key, json.encodeToString(CacheEnvelope.serializer(), envelope))
    }

    fun <T> get(key: String, serializer: KSerializer<T>): CachedValue<T>? {
        val raw = store.getString(prefix + key) ?: return null
        return runCatching {
            val envelope = json.decodeFromString(CacheEnvelope.serializer(), raw)
            CachedValue(
                value = json.decodeFromJsonElement(serializer, envelope.payload),
                savedAt = envelope.savedAt,
                fromCache = true,
            )
        }.getOrNull()
    }

    fun remove(key: String) {
        store.remove(prefix + key)
    }

    fun clear() {
        store.keys()
            .filter { it.startsWith(prefix) }
            .forEach(store::remove)
    }

    private companion object {
        const val prefix = "cache."
    }
}

internal fun nowMillis(): Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
