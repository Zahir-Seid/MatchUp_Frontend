package et.matchup.app.util

import et.matchup.app.ui.Strings
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.absoluteValue
import kotlin.random.Random

private val timeZone: TimeZone
    get() = TimeZone.currentSystemDefault()

fun Instant.localDateTime(): LocalDateTime = toLocalDateTime(timeZone)

fun Instant.localDate(): LocalDate = localDateTime().date

fun formatTime(value: String, strings: Strings? = null): String = runCatching {
    val time = Instant.parse(value).localDateTime()
    val hour = when {
        time.hour == 0 -> 12
        time.hour > 12 -> time.hour - 12
        else -> time.hour
    }
    val minute = time.minute.toString().padStart(2, '0')
    val suffix = if (time.hour < 12) "AM" else "PM"
    "$hour:$minute $suffix"
}.getOrDefault(strings?.timeTba ?: "Time TBA")

fun formatTimeRange(start: String, end: String, strings: Strings? = null): String =
    "${formatTime(start, strings)} – ${formatTime(end, strings)}"

fun formatDateShort(value: String, strings: Strings): String = runCatching {
    val date = Instant.parse(value).localDate()
    "${strings.monthsShort[date.monthNumber - 1]} ${date.dayOfMonth}"
}.getOrDefault(strings.dateTba)

fun formatDateMedium(value: String, strings: Strings): String = runCatching {
    val date = Instant.parse(value).localDate()
    "${weekdayShort(date, strings)} ${strings.monthsShort[date.monthNumber - 1]} ${date.dayOfMonth}"
}.getOrDefault(strings.dateTba)

fun formatDateLong(value: String, strings: Strings): String = runCatching {
    val date = Instant.parse(value).localDate()
    "${weekdayShort(date, strings)}, ${strings.monthsShort[date.monthNumber - 1]} ${date.dayOfMonth}, ${date.year}"
}.getOrDefault(strings.dateTba)

fun formatDateTime(value: String, strings: Strings): String =
    "${formatDateMedium(value, strings)} · ${formatTime(value, strings)}"

fun dateBucketLabel(value: String, strings: Strings): String {
    val date = runCatching { Instant.parse(value).localDate() }.getOrNull() ?: return strings.dateTba
    val today = Clock.System.now().localDate()
    return when (date) {
        today -> strings.today
        today.plus(1, DateTimeUnit.DAY) -> strings.tomorrow
        else -> formatDateMedium(value, strings)
    }
}

fun weekdayShort(date: LocalDate, strings: Strings): String =
    strings.weekdaysShort.getOrElse(date.dayOfWeek.ordinal) { date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() } }

fun relativeTime(value: String, strings: Strings): String {
    val then = runCatching { Instant.parse(value) }.getOrNull() ?: return ""
    val seconds = (Clock.System.now() - then).inWholeSeconds.absoluteValue
    return when {
        seconds < 60 -> strings.justNow
        seconds < 3_600 -> strings.minutesAgo((seconds / 60).toInt())
        seconds < 86_400 -> strings.hoursAgo((seconds / 3_600).toInt())
        seconds < 172_800 -> strings.yesterday
        seconds < 604_800 -> strings.daysAgo((seconds / 86_400).toInt())
        else -> formatDateShort(value, strings)
    }
}

fun savedAtLabel(savedAt: Long, strings: Strings): String {
    val then = Instant.fromEpochMilliseconds(savedAt)
    val seconds = (Clock.System.now() - then).inWholeSeconds.coerceAtLeast(0)
    return when {
        seconds < 60 -> strings.savedJustNow
        seconds < 3_600 -> strings.savedMinutesAgo((seconds / 60).toInt())
        seconds < 86_400 -> strings.savedHoursAgo((seconds / 3_600).toInt())
        else -> strings.savedDaysAgo((seconds / 86_400).toInt())
    }
}

fun formatCountdown(totalSeconds: Long): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val minutes = (safe / 60).toString().padStart(2, '0')
    val seconds = (safe % 60).toString().padStart(2, '0')
    return "$minutes:$seconds"
}

fun moneyAmount(value: String): String =
    value.removeSuffix(" ETB").trim().ifBlank { "0.00" }

fun formatSantim(santim: Long): String {
    val whole = santim / 100
    val fraction = (santim % 100).absoluteValue.toString().padStart(2, '0')
    return "${groupThousands(whole)}.$fraction"
}

private fun groupThousands(value: Long): String {
    val raw = value.absoluteValue.toString()
    val builder = StringBuilder()
    raw.forEachIndexed { index, ch ->
        if (index > 0 && (raw.length - index) % 3 == 0) builder.append(',')
        builder.append(ch)
    }
    return if (value < 0) "-$builder" else builder.toString()
}

fun formatLabel(format: String, strings: Strings): String = when (format) {
    "5_a_side" -> strings.fiveASide
    "7_a_side" -> strings.sevenASide
    else -> format.replace('_', ' ').replaceFirstChar { it.uppercase() }
}

fun formatLabelShort(format: String): String = when (format) {
    "5_a_side" -> "5-A-SIDE"
    "7_a_side" -> "7-A-SIDE"
    else -> format.replace('_', ' ').uppercase()
}

fun statusLabel(status: String, strings: Strings): String = when (status) {
    "pending_payment" -> strings.bookingPendingPayment
    "verifying" -> strings.bookingVerifying
    "confirmed" -> strings.bookingConfirmed
    "pending_review" -> strings.bookingPendingReview
    "expired" -> strings.bookingExpired
    "cancelled" -> strings.bookingCancelled
    else -> status.replace('_', ' ').replaceFirstChar { it.uppercase() }
}

fun isValidEmail(value: String): Boolean =
    Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(value.trim())

fun passwordProblem(password: String, strings: Strings): String? = when {
    password.length < 10 -> strings.passwordTooShort
    password.encodeToByteArray().size > 72 -> strings.passwordTooLong
    password.none { it.isLetter() } ||
        password.none { it.isDigit() || !it.isLetterOrDigit() } -> strings.passwordNeedsMix
    else -> null
}

fun randomIdempotencyKey(): String {
    val bytes = ByteArray(16)
    Random.nextBytes(bytes)
    return bytes.joinToString("") { byte ->
        (byte.toInt() and 0xff).toString(16).padStart(2, '0')
    }
}

fun initials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "M"
        parts.size == 1 -> parts.first().take(1).uppercase()
        else -> (parts.first().take(1) + parts.last().take(1)).uppercase()
    }
}

fun gameIsPast(startsAt: String, endsAt: String): Boolean {
    val end = runCatching { Instant.parse(endsAt) }.getOrNull()
        ?: runCatching { Instant.parse(startsAt) }.getOrNull()
        ?: return false
    return end < Clock.System.now()
}

fun holdRemainingSeconds(holdExpiresAt: String?): Long? {
    val expiry = holdExpiresAt?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: return null
    return (expiry - Clock.System.now()).inWholeSeconds.coerceAtLeast(0)
}
