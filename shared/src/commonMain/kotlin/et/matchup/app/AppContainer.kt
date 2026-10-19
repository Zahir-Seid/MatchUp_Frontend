package et.matchup.app

import et.matchup.app.data.AppJson
import et.matchup.app.data.BookingRepository
import et.matchup.app.data.CatalogRepository
import et.matchup.app.data.HttpApi
import et.matchup.app.data.JsonCache
import et.matchup.app.data.NotificationRepository
import et.matchup.app.data.PaymentRepository
import et.matchup.app.data.ProfileRepository
import et.matchup.app.data.SavedTeammatesRepository
import et.matchup.app.data.SessionManager
import et.matchup.app.data.createHttpClient
import et.matchup.app.platform.apiBaseUrl
import et.matchup.app.platform.platformStore

class AppContainer {
    val store = platformStore()
    val json = AppJson
    val cache = JsonCache(store, json)
    val api = HttpApi(
        client = createHttpClient(),
        baseUrl = apiBaseUrl(),
        json = json,
    )
    val session = SessionManager(api, store, cache, json)
    val catalog = CatalogRepository(api, session, cache)
    val bookings = BookingRepository(api, session, cache)
    val notifications = NotificationRepository(api, session, cache)
    val payments = PaymentRepository(api, session, cache)
    val profile = ProfileRepository(session)
    val teammates = SavedTeammatesRepository(store, json)

    fun clearMemory() {
        catalog.clearMemory()
        bookings.clearMemory()
        notifications.clearMemory()
        payments.clearMemory()
    }

    fun close() {
        api.close()
    }
}
