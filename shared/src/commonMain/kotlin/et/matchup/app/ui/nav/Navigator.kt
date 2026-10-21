package et.matchup.app.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

sealed interface AuthScreen {
    data object Login : AuthScreen
    data object Signup : AuthScreen
    data object Forgot : AuthScreen
    data class Reset(val token: String?) : AuthScreen
}

sealed interface MainScreen {
    data object Home : MainScreen
    data object Bookings : MainScreen
    data object Notifications : MainScreen
    data object Profile : MainScreen
    data class GameDetail(val gameId: String) : MainScreen
    data class Review(
        val gameId: String,
        val bookingType: String,
        val spots: Int,
        val names: List<String>,
    ) : MainScreen
    data class Verification(val bookingId: String) : MainScreen
    data class Payment(val bookingId: String) : MainScreen
    data class BookingDetail(val bookingId: String) : MainScreen
    data object EditProfile : MainScreen
    data object SavedTeammates : MainScreen
    data object ChangePassword : MainScreen
    data object EmailVerification : MainScreen
}

class Navigator<T : Any>(initial: T) {
    val stack = mutableStateListOf(initial)
    val current: T get() = stack.last()

    fun push(screen: T) {
        stack.add(screen)
    }

    fun replace(screen: T) {
        if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
        stack.add(screen)
    }

    fun replaceRoot(screen: T) {
        stack.clear()
        stack.add(screen)
    }

    fun pop(): Boolean {
        if (stack.size <= 1) return false
        stack.removeAt(stack.lastIndex)
        return true
    }

    fun popToRoot() {
        while (stack.size > 1) stack.removeAt(stack.lastIndex)
    }
}

@Composable
fun <T : Any> rememberNavigator(initial: T): Navigator<T> = remember { Navigator(initial) }
