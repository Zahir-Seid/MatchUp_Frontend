package et.matchup.app.data

import et.matchup.app.platform.KeyValueStore
import et.matchup.app.platform.isOnlineNow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.days

class CatalogRepository(
    private val api: HttpApi,
    private val session: SessionManager,
    private val cache: JsonCache,
) {
    private var memory: CatalogSnapshot? = null
    private var memorySavedAt: Long = 0L

    suspend fun loadCatalog(force: Boolean = false): CachedValue<CatalogSnapshot> {
        memory?.takeIf { !force }?.let {
            return CachedValue(it, memorySavedAt, fromCache = false)
        }
        if (!isOnlineNow()) {
            return cachedCatalog() ?: throw offlineError()
        }
        return try {
            val fields = api.fields().fields
            val games = loadAllGames()
                .filter { it.status == "scheduled" }
                .sortedBy { it.startsAt }
            val snapshot = CatalogSnapshot(fields = fields, games = games)
            memory = snapshot
            memorySavedAt = nowMillis()
            cache.put(cacheKey, CatalogSnapshot.serializer(), snapshot)
            CachedValue(snapshot, memorySavedAt, fromCache = false)
        } catch (error: Throwable) {
            val saved = cachedCatalog()
            if (saved != null) saved else throw error
        }
    }

    suspend fun game(id: String): CachedValue<GameDto> {
        if (!isOnlineNow()) {
            return cachedGame(id) ?: throw offlineError()
        }
        return try {
            val game = api.game(id)
            CachedValue(game, nowMillis(), fromCache = false)
        } catch (error: Throwable) {
            // A real API answer (404, auth, validation) must reach the user; only
            // transport/server failures fall back to the last known game.
            if (error is ApiException && error.status != 0 && error.status < 500) throw error
            cachedGame(id) ?: throw error
        }
    }

    suspend fun quote(gameId: String, bookingType: String, spotCount: Int): QuoteDto =
        session.authenticated { token ->
            api.quote(token, QuoteRequest(gameId, bookingType, spotCount))
        }

    fun clearMemory() {
        memory = null
    }

    private fun cachedGame(id: String): CachedValue<GameDto>? {
        memory?.games?.firstOrNull { it.id == id }?.let {
            return CachedValue(it, memorySavedAt, fromCache = true)
        }
        val saved = cache.get(cacheKey, CatalogSnapshot.serializer()) ?: return null
        memory = saved.value
        memorySavedAt = saved.savedAt
        val game = saved.value.games.firstOrNull { it.id == id } ?: return null
        return CachedValue(game, saved.savedAt, fromCache = true)
    }

    private fun cachedCatalog(): CachedValue<CatalogSnapshot>? {
        val saved = cache.get(cacheKey, CatalogSnapshot.serializer()) ?: return null
        memory = saved.value
        memorySavedAt = saved.savedAt
        return saved
    }

    private suspend fun loadAllGames(): List<GameDto> {
        val now = Clock.System.now()
        val from = now.toString()
        val to = (now + 120.days).toString()
        val games = mutableListOf<GameDto>()
        var offset = 0
        var pageCount = 0
        while (pageCount < 5) {
            val page = api.games(from = from, to = to, limit = 100, offset = offset)
            games += page.games
            if (page.games.size < 100) break
            offset += 100
            pageCount++
        }
        return games
    }

    private companion object {
        const val cacheKey = "catalog.v2"
    }
}

class BookingRepository(
    private val api: HttpApi,
    private val session: SessionManager,
    private val cache: JsonCache,
) {
    private var listMemory: Pair<String, List<BookingDto>>? = null

    suspend fun loadBookings(force: Boolean = false): CachedValue<List<BookingDto>> {
        val userId = session.currentUser()?.id ?: return CachedValue(emptyList(), nowMillis(), false)
        listMemory?.takeIf { it.first == userId && !force }?.let {
            return CachedValue(it.second, nowMillis(), false)
        }
        val key = bookingsKey(userId)
        if (!isOnlineNow()) {
            val saved = cache.get(key, BookingListDto.serializer())
                ?: throw offlineError()
            listMemory = userId to saved.value.bookings
            return CachedValue(saved.value.bookings, saved.savedAt, true)
        }
        return try {
            val bookings = mutableListOf<BookingDto>()
            var offset = 0
            var pageCount = 0
            while (pageCount < 5) {
                val page = session.authenticated { api.bookings(it, limit = 100, offset = offset) }
                bookings += page.bookings
                if (page.bookings.size < 100) break
                offset += 100
                pageCount++
            }
            listMemory = userId to bookings
            cache.put(key, BookingListDto.serializer(), BookingListDto(bookings))
            CachedValue(bookings, nowMillis(), false)
        } catch (error: Throwable) {
            val saved = cache.get(key, BookingListDto.serializer())
            if (saved != null) {
                listMemory = userId to saved.value.bookings
                CachedValue(saved.value.bookings, saved.savedAt, true)
            } else {
                throw error
            }
        }
    }

    suspend fun loadBooking(id: String, force: Boolean = false): CachedValue<BookingDto> {
        val userId = session.currentUser()?.id ?: throw ApiException(
            401, "unauthorized", "Please sign in again.",
        )
        if (!isOnlineNow()) {
            return savedBooking(userId, id) ?: throw offlineError()
        }
        return try {
            val booking = session.authenticated { api.booking(it, id) }
            remember(booking, userId)
            CachedValue(booking, nowMillis(), false)
        } catch (error: Throwable) {
            if (error is ApiException && error.status in 400..499 && error.status != 429) throw error
            savedBooking(userId, id) ?: throw error
        }
    }

    suspend fun createBooking(
        gameId: String,
        bookingType: String,
        spotCount: Int,
        participants: List<ParticipantInputDto>,
    ): BookingDto {
        val booking = session.authenticated { token ->
            api.createBooking(
                token,
                CreateBookingRequest(
                    gameId = gameId,
                    bookingType = bookingType,
                    spotCount = spotCount,
                    participants = participants,
                ),
            )
        }
        val userId = session.currentUser()?.id
        if (userId != null) remember(booking, userId)
        listMemory = null
        return booking
    }

    suspend fun submitPayment(
        bookingId: String,
        method: String,
        reference: String,
        accountSuffix: String?,
        idempotencyKey: String,
    ): PaymentSubmissionDto = session.authenticated { token ->
        api.submitPayment(
            token,
            bookingId,
            PaymentRequest(
                method = method,
                transactionReference = reference,
                accountSuffix = accountSuffix?.takeIf { it.isNotBlank() },
                idempotencyKey = idempotencyKey,
            ),
        )
    }

    suspend fun cancelBooking(bookingId: String): BookingDto {
        val booking = session.authenticated { api.cancelBooking(it, bookingId) }
        session.currentUser()?.id?.let { remember(booking, it) }
        listMemory = null
        return booking
    }

    fun clearMemory() {
        listMemory = null
    }

    private fun remember(booking: BookingDto, userId: String) {
        cache.put(bookingKey(userId, booking.id), BookingDto.serializer(), booking)
    }

    private fun savedBooking(userId: String, id: String): CachedValue<BookingDto>? {
        listMemory?.takeIf { it.first == userId }?.second?.firstOrNull { it.id == id }?.let {
            return CachedValue(it, nowMillis(), true)
        }
        val saved = cache.get(bookingKey(userId, id), BookingDto.serializer()) ?: return null
        return CachedValue(saved.value, saved.savedAt, true)
    }

    private fun bookingsKey(userId: String) = "bookings.$userId"
    private fun bookingKey(userId: String, id: String) = "booking.$userId.$id"
}

class NotificationRepository(
    private val api: HttpApi,
    private val session: SessionManager,
    private val cache: JsonCache,
) {
    private var memory: Pair<String, NotificationListDto>? = null

    suspend fun load(force: Boolean = false): CachedValue<NotificationListDto> {
        val userId = session.currentUser()?.id ?: return CachedValue(NotificationListDto(), nowMillis(), false)
        memory?.takeIf { it.first == userId && !force }?.let {
            return CachedValue(it.second, nowMillis(), false)
        }
        if (!isOnlineNow()) {
            val saved = cache.get(key(userId), NotificationListDto.serializer())
                ?: throw offlineError()
            memory = userId to saved.value
            return CachedValue(saved.value, saved.savedAt, true)
        }
        return try {
            val page = session.authenticated { api.notifications(it, limit = 100) }
            memory = userId to page
            cache.put(key(userId), NotificationListDto.serializer(), page)
            CachedValue(page, nowMillis(), false)
        } catch (error: Throwable) {
            val saved = cache.get(key(userId), NotificationListDto.serializer())
            if (saved != null) {
                memory = userId to saved.value
                CachedValue(saved.value, saved.savedAt, true)
            } else {
                throw error
            }
        }
    }

    /** Light poll used by the shell and notification badge. */
    suspend fun unreadCount(): Int {
        val page = session.authenticated { api.notifications(it, limit = 1) }
        val userId = session.currentUser()?.id ?: return page.unreadCount
        val current = memory?.takeIf { it.first == userId }?.second
        if (current != null) {
            val refreshed = current.copy(unreadCount = page.unreadCount)
            memory = userId to refreshed
            cache.put(key(userId), NotificationListDto.serializer(), refreshed)
        }
        return page.unreadCount
    }

    suspend fun markRead(id: String) {
        session.authenticated { api.markNotificationRead(it, id) }
        val userId = session.currentUser()?.id ?: return
        val current = memory?.takeIf { it.first == userId }?.second ?: return
        val changed = current.copy(
            notifications = current.notifications.map {
                if (it.id == id) it.copy(read = true) else it
            },
            unreadCount = (current.unreadCount - 1).coerceAtLeast(0),
        )
        memory = userId to changed
        cache.put(key(userId), NotificationListDto.serializer(), changed)
    }

    suspend fun markAllRead() {
        session.authenticated { api.markAllNotificationsRead(it) }
        val userId = session.currentUser()?.id ?: return
        val current = memory?.takeIf { it.first == userId }?.second ?: return
        val changed = current.copy(
            notifications = current.notifications.map { it.copy(read = true) },
            unreadCount = 0,
        )
        memory = userId to changed
        cache.put(key(userId), NotificationListDto.serializer(), changed)
    }

    fun clearMemory() {
        memory = null
    }

    private fun key(userId: String) = "notifications.$userId"
}

class PaymentRepository(
    private val api: HttpApi,
    private val session: SessionManager,
    private val cache: JsonCache,
) {
    private var memory: PaymentInstructionsDto? = null

    suspend fun instructions(force: Boolean = false): CachedValue<PaymentInstructionsDto> {
        memory?.takeIf { !force }?.let {
            return CachedValue(it, nowMillis(), false)
        }
        if (!isOnlineNow()) {
            val saved = cache.get(key, PaymentInstructionsDto.serializer())
                ?: throw offlineError()
            memory = saved.value
            return saved
        }
        return try {
            val instructions = session.authenticated { api.paymentInstructions(it) }
            memory = instructions
            cache.put(key, PaymentInstructionsDto.serializer(), instructions)
            CachedValue(instructions, nowMillis(), false)
        } catch (error: Throwable) {
            val saved = cache.get(key, PaymentInstructionsDto.serializer())
            if (saved != null) {
                memory = saved.value
                saved
            } else {
                throw error
            }
        }
    }

    fun clearMemory() {
        memory = null
    }

    private companion object {
        const val key = "payment.instructions.v1"
    }
}

class ProfileRepository(
    private val session: SessionManager,
) {
    suspend fun update(fullName: String, age: Int?, phone: String?): UserDto =
        session.updateProfile(
            UpdateProfileRequest(
                fullName = fullName.trim(),
                age = age,
                phone = phone?.trim()?.takeIf { it.isNotEmpty() },
            ),
        )

    suspend fun requestEmailVerification() = session.requestEmailVerification()

    suspend fun confirmEmailVerification(token: String): String =
        session.confirmEmailVerification(token)
}

@Serializable
private data class SavedNames(val names: List<String> = emptyList())

class SavedTeammatesRepository(
    private val store: KeyValueStore,
    private val json: Json = AppJson,
) {
    private val mutex = Mutex()

    suspend fun list(userId: String): List<String> = mutex.withLock { read(userId) }

    suspend fun add(userId: String, name: String): List<String> = mutex.withLock {
        val clean = name.trim().take(120)
        if (clean.isBlank()) return@withLock read(userId)
        val names = read(userId)
        if (names.any { it.equals(clean, ignoreCase = true) }) return@withLock names
        val updated = names + clean
        write(userId, updated)
        updated
    }

    suspend fun remove(userId: String, name: String): List<String> = mutex.withLock {
        val updated = read(userId).filterNot { it == name }
        write(userId, updated)
        updated
    }

    private fun read(userId: String): List<String> {
        val raw = store.getString(key(userId)) ?: return emptyList()
        return runCatching {
            json.decodeFromString(SavedNames.serializer(), raw).names
        }.getOrDefault(emptyList())
    }

    private fun write(userId: String, names: List<String>) {
        store.putString(
            key(userId),
            json.encodeToString(SavedNames.serializer(), SavedNames(names)),
        )
    }

    private fun key(userId: String) = "local.teammates.$userId"
}

private fun offlineError() = ApiException(
    status = 0,
    code = "offline",
    message = "You're offline. Connect to the internet to refresh.",
)
