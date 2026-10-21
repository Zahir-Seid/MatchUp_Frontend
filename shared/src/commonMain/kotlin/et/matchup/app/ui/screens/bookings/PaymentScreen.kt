package et.matchup.app.ui.screens.bookings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import et.matchup.app.AppState
import et.matchup.app.data.ApiException
import et.matchup.app.data.BookingDto
import et.matchup.app.data.PaymentInstructionsDto
import et.matchup.app.data.PaymentMethodDto
import et.matchup.app.data.PaymentSubmissionDto
import et.matchup.app.data.apiExceptionMessage
import et.matchup.app.platform.PlatformBackHandler
import et.matchup.app.platform.hapticTick
import et.matchup.app.ui.LocalStrings
import et.matchup.app.ui.components.BottomBar
import et.matchup.app.ui.components.CheckLine
import et.matchup.app.ui.components.EmptyState
import et.matchup.app.ui.components.ErrorState
import et.matchup.app.ui.components.FieldArt
import et.matchup.app.ui.components.InfoBanner
import et.matchup.app.ui.components.LoadingState
import et.matchup.app.ui.components.MatchUpTextField
import et.matchup.app.ui.components.OfflineBanner
import et.matchup.app.ui.components.PrimaryButton
import et.matchup.app.ui.components.SurfaceCard
import et.matchup.app.ui.components.TopBar
import et.matchup.app.ui.theme.MatchUpColors
import et.matchup.app.util.formatCountdown
import et.matchup.app.util.formatDateLong
import et.matchup.app.util.formatTimeRange
import et.matchup.app.util.holdRemainingSeconds
import et.matchup.app.util.moneyAmount
import et.matchup.app.util.randomIdempotencyKey
import et.matchup.app.util.savedAtLabel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// LocalClipboardManager is deprecated in favour of LocalClipboard, but the
// replacement needs a platform-specific ClipEntry there is no common way to
// build yet. Keep the working cross-platform API until one lands.
@Suppress("DEPRECATION")
@Composable
fun PaymentScreen(
    state: AppState,
    bookingId: String,
    online: Boolean,
    onBack: () -> Unit,
    onSubmitted: (String) -> Unit,
    onBookAgain: (String?) -> Unit,
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    var booking by remember { mutableStateOf<BookingDto?>(null) }
    var instructions by remember { mutableStateOf<PaymentInstructionsDto?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var fromCache by remember { mutableStateOf(false) }
    var savedAt by remember { mutableStateOf(0L) }
    var loadKey by remember { mutableStateOf(0) }

    var selectedMethod by remember { mutableStateOf<String?>(null) }
    var reference by remember { mutableStateOf("") }
    var suffix by remember { mutableStateOf("") }
    var attempted by remember { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var fieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var paymentDone by remember { mutableStateOf(false) }
    var submission by remember { mutableStateOf<PaymentSubmissionDto?>(null) }
    var idempotencyKey by remember { mutableStateOf(randomIdempotencyKey()) }
    var remainingSeconds by remember { mutableStateOf<Long?>(null) }
    var showLeaveConfirm by remember { mutableStateOf(false) }
    var warnedHaptic by remember { mutableStateOf(false) }

    LaunchedEffect(bookingId, loadKey) {
        if (booking == null) loading = true
        error = null
        try {
            val result = state.dependencies.bookings.loadBooking(bookingId, force = loadKey > 0)
            booking = result.value
            fromCache = result.fromCache
            savedAt = result.savedAt
            runCatching { state.dependencies.payments.instructions() }
                .onSuccess { instructions = it.value }
        } catch (throwable: Throwable) {
            error = apiExceptionMessage(throwable)
            runCatching { state.dependencies.payments.instructions() }
                .onSuccess { instructions = it.value }
        } finally {
            loading = false
        }
    }

    LaunchedEffect(instructions) {
        if (selectedMethod == null) {
            selectedMethod = instructions?.methods?.firstOrNull()?.method
        }
    }

    LaunchedEffect(booking?.holdExpiresAt, booking?.holdSecondsRemaining) {
        if (booking == null) return@LaunchedEffect
        remainingSeconds = holdRemainingSeconds(booking?.holdExpiresAt)
            ?: booking?.holdSecondsRemaining
        while ((remainingSeconds ?: 0) > 0) {
            delay(1_000)
            remainingSeconds = holdRemainingSeconds(booking?.holdExpiresAt)
                ?: ((remainingSeconds ?: 0) - 1).coerceAtLeast(0)
            val left = remainingSeconds ?: 0
            if (!warnedHaptic && left in 1..60) {
                warnedHaptic = true
                hapticTick()
            }
        }
    }

    LaunchedEffect(selectedMethod, reference, suffix) {
        if (!submitting) idempotencyKey = randomIdempotencyKey()
    }

    val methods = instructions?.methods.orEmpty()
    val selected = methods.firstOrNull { it.method == selectedMethod } ?: methods.firstOrNull()
    val expired = booking?.status == "pending_payment" &&
        remainingSeconds != null &&
        remainingSeconds!! <= 0
    val canPay = booking?.status == "pending_payment" && !expired && !paymentDone &&
        methods.isNotEmpty()

    val referenceError = when {
        fieldErrors["transaction_reference"] != null -> fieldErrors["transaction_reference"]
        attempted && reference.isBlank() -> strings.referenceRequired
        else -> null
    }
    val suffixError = when {
        fieldErrors["account_suffix"] != null -> fieldErrors["account_suffix"]
        attempted && selected?.method == "cbe" && suffix.isNotBlank() && !Regex("^\\d{8}$").matches(suffix) ->
            strings.suffixInvalid
        else -> null
    }

    PlatformBackHandler(enabled = canPay) {
        showLeaveConfirm = true
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(
            title = strings.completePayment,
            onBack = {
                if (canPay) showLeaveConfirm = true else onBack()
            },
        )
        Box(Modifier.weight(1f)) {
            when {
                loading && booking == null -> LoadingState()
                booking == null -> ErrorState(
                    title = if (!online) strings.noConnectionTitle else strings.paymentDetails,
                    body = error ?: strings.errorGeneric,
                    onRetry = { loadKey += 1 },
                )
                else -> {
                    val current = booking!!
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 22.dp),
                    ) {
                        item {
                            Column(Modifier.padding(horizontal = 18.dp)) {
                                Spacer(Modifier.height(8.dp))
                                OfflineBanner(visible = !online)
                                if (!online) Spacer(Modifier.height(10.dp))
                                if (fromCache) {
                                    Text(
                                        text = strings.savedDataNotice.replace(
                                            "%s",
                                            savedAtLabel(savedAt, strings),
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MatchUpColors.Amber,
                                    )
                                    Spacer(Modifier.height(8.dp))
                                }
                                if (error != null) {
                                    InfoBanner(
                                        title = strings.couldNotRefresh,
                                        body = error.orEmpty(),
                                        tint = MatchUpColors.Amber,
                                    )
                                    Spacer(Modifier.height(10.dp))
                                }
                                PaymentHero(booking = current)
                                Spacer(Modifier.height(14.dp))
                                when {
                                    paymentDone -> PaymentSuccessCard(
                                        submission = submission,
                                        booking = current,
                                    )
                                    current.status != "pending_payment" -> PaymentStatusCard(current)
                                    expired -> ExpiredCard()
                                    else -> {
                                        if (remainingSeconds != null) {
                                            HoldCountdown(remainingSeconds = remainingSeconds ?: 0)
                                            Spacer(Modifier.height(14.dp))
                                        }
                                        StepHeader(number = "1", title = strings.stepSend)
                                        Spacer(Modifier.height(10.dp))
                                        if (methods.isEmpty()) {
                                            InfoBanner(
                                                title = strings.paymentUnavailable,
                                                body = strings.paymentUnavailableBody,
                                                tint = MatchUpColors.Red,
                                                icon = Icons.Outlined.ErrorOutline,
                                            )
                                        } else {
                                            methods.forEach { method ->
                                                PaymentMethodRow(
                                                    method = method,
                                                    selected = method.method == (selected?.method ?: selectedMethod),
                                                    onClick = { selectedMethod = method.method },
                                                    modifier = Modifier.padding(bottom = 8.dp),
                                                )
                                            }
                                            if (selected != null) {
                                                Spacer(Modifier.height(6.dp))
                                                AccountDetails(
                                                    method = selected,
                                                    onCopy = {
                                                        clipboard.setText(AnnotatedString(selected.account))
                                                        state.showMessage(strings.copied)
                                                    },
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(18.dp))
                                        StepHeader(number = "2", title = strings.stepSubmit)
                                        Spacer(Modifier.height(10.dp))
                                        if (submitError != null) {
                                            InfoBanner(
                                                title = strings.errorGeneric,
                                                body = submitError.orEmpty(),
                                                tint = MatchUpColors.Red,
                                                icon = Icons.Outlined.ErrorOutline,
                                            )
                                            Spacer(Modifier.height(10.dp))
                                        }
                                        MatchUpTextField(
                                            value = reference,
                                            onValueChange = {
                                                reference = it
                                                fieldErrors = fieldErrors - "transaction_reference"
                                                submitError = null
                                            },
                                            label = strings.paymentReference,
                                            placeholder = strings.paymentReferencePlaceholder,
                                            supportingText = referenceError ?: strings.paymentReferenceHint,
                                            isError = referenceError != null,
                                            imeAction = ImeAction.Next,
                                        )
                                        if (selected?.method == "cbe") {
                                            Spacer(Modifier.height(12.dp))
                                            MatchUpTextField(
                                                value = suffix,
                                                onValueChange = { value ->
                                                    if (value.all { it.isDigit() } && value.length <= 8) {
                                                        suffix = value
                                                    }
                                                    fieldErrors = fieldErrors - "account_suffix"
                                                    submitError = null
                                                },
                                                label = "${strings.accountSuffix} (${strings.optional})",
                                                supportingText = suffixError ?: strings.accountSuffixHint,
                                                isError = suffixError != null,
                                                keyboardType = KeyboardType.Number,
                                                imeAction = ImeAction.Done,
                                            )
                                        }
                                        Spacer(Modifier.height(16.dp))
                                        InfoBanner(
                                            title = strings.paymentSafety,
                                            body = strings.holdNote,
                                            tint = MatchUpColors.Blue,
                                            icon = Icons.Outlined.Security,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (booking != null) {
            val current = booking!!
            BottomBar {
                if (current.status == "pending_payment" && !paymentDone) {
                    if (expired) {
                        Text(
                            text = strings.holdExpiredBody,
                            style = MaterialTheme.typography.bodySmall,
                            color = MatchUpColors.Amber,
                        )
                        Spacer(Modifier.height(8.dp))
                        PrimaryButton(
                            text = strings.bookAgain,
                            onClick = { onBookAgain(current.games.firstOrNull()?.game?.id) },
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = strings.amountDue,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MatchUpColors.TextMuted,
                                )
                                Text(
                                    text = "${moneyAmount(current.total)} ${strings.currency}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MatchUpColors.Text,
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            PrimaryButton(
                                text = if (submitting) strings.submitting else strings.submitPayment,
                                onClick = {
                                    attempted = true
                                    submitError = null
                                    fieldErrors = emptyMap()
                                    if (!online) {
                                        submitError = strings.offlineWriteBody
                                        return@PrimaryButton
                                    }
                                    if (reference.isBlank()) return@PrimaryButton
                                    if (
                                        selected?.method == "cbe" &&
                                        suffix.isNotBlank() &&
                                        !Regex("^\\d{8}$").matches(suffix)
                                    ) {
                                        return@PrimaryButton
                                    }
                                    val method = selected?.method ?: return@PrimaryButton
                                    scope.launch {
                                        submitting = true
                                        try {
                                            val result = state.dependencies.bookings.submitPayment(
                                                bookingId = bookingId,
                                                method = method,
                                                reference = reference.trim(),
                                                accountSuffix = if (selected.method == "cbe") {
                                                    suffix.ifBlank { null }
                                                } else {
                                                    null
                                                },
                                                idempotencyKey = idempotencyKey,
                                            )
                                            submission = result
                                            paymentDone = true
                                            try {
                                                booking = state.dependencies.bookings
                                                    .loadBooking(bookingId, force = true)
                                                    .value
                                            } catch (_: Throwable) {
                                                // The success card already has the server's response.
                                            }
                                        } catch (throwable: Throwable) {
                                            if (throwable is ApiException) {
                                                fieldErrors = throwable.fields
                                            }
                                            submitError = apiExceptionMessage(throwable)
                                            state.showError(throwable)
                                        } finally {
                                            submitting = false
                                        }
                                    }
                                },
                                enabled = online && canPay && !submitting,
                                loading = submitting,
                                modifier = Modifier.weight(1.2f),
                            )
                        }
                    }
                } else {
                    PrimaryButton(
                        text = strings.viewDetails,
                        onClick = { onSubmitted(bookingId) },
                    )
                }
            }
        }
    }

    if (showLeaveConfirm) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirm = false },
            title = { Text(strings.leavePaymentTitle) },
            text = {
                Text(
                    strings.leavePaymentBody +
                        (remainingSeconds?.let { "\n\n${strings.expiresIn} ${formatCountdown(it)}" } ?: ""),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLeaveConfirm = false
                    onBack()
                }) {
                    Text(strings.leaveAnyway, color = MatchUpColors.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirm = false }) {
                    Text(strings.keepPaying, color = MatchUpColors.Lime)
                }
            },
        )
    }
}

@Composable
private fun PaymentHero(booking: BookingDto) {
    val strings = LocalStrings.current
    val firstGame = booking.games.firstOrNull()?.game
    FieldArt(
        seed = booking.id + (firstGame?.field?.name ?: ""),
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(24.dp)),
    ) {
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = strings.reservedFor.replace("%s", firstGame?.field?.name ?: strings.gameDetails),
                style = MaterialTheme.typography.labelSmall,
                color = MatchUpColors.Lime,
            )
            Spacer(Modifier.height(4.dp))
            if (firstGame != null) {
                Text(
                    text = formatDateLong(firstGame.startsAt, strings),
                    style = MaterialTheme.typography.titleMedium,
                    color = MatchUpColors.Text,
                )
                Text(
                    text = formatTimeRange(firstGame.startsAt, firstGame.endsAt, strings),
                    style = MaterialTheme.typography.bodySmall,
                    color = MatchUpColors.TextMuted,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = moneyAmount(booking.total),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MatchUpColors.Text,
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = strings.currency,
                    style = MaterialTheme.typography.labelMedium,
                    color = MatchUpColors.Lime,
                    modifier = Modifier.padding(bottom = 5.dp),
                )
                Spacer(Modifier.weight(1f))
                Surface(
                    shape = CircleShape,
                    color = MatchUpColors.SurfaceHigh,
                ) {
                    Text(
                        text = booking.referenceCode,
                        style = MaterialTheme.typography.labelSmall,
                        color = MatchUpColors.TextMuted,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HoldCountdown(remainingSeconds: Long) {
    val strings = LocalStrings.current
    val urgent = remainingSeconds < 30
    val warning = remainingSeconds < 60
    val tint = when {
        urgent -> MatchUpColors.Red
        warning -> MatchUpColors.Amber
        else -> MatchUpColors.Lime
    }
    val progress = (remainingSeconds / 300f).coerceIn(0f, 1f)
    val label = "${strings.expiresIn} ${formatCountdown(remainingSeconds)}"
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = label },
        shape = RoundedCornerShape(18.dp),
        color = tint.copy(alpha = if (MatchUpColors.isDark) 0.1f else 0.08f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.3f)),
    ) {
        Row(
            Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(58.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = 5.dp.toPx()
                    val inset = stroke / 2f
                    drawArc(
                        color = tint.copy(alpha = 0.18f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - stroke, size.height - stroke),
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = tint,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - stroke, size.height - stroke),
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                }
                Text(
                    text = formatCountdown(remainingSeconds),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = tint,
                )
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = strings.expiresIn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MatchUpColors.TextMuted,
                )
                Text(
                    text = strings.holdNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MatchUpColors.TextFaint,
                )
            }
            Icon(
                imageVector = Icons.Outlined.HourglassTop,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun StepHeader(number: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(24.dp)
                .background(MatchUpColors.Lime, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall,
                color = MatchUpColors.OnLime,
            )
        }
        Spacer(Modifier.width(9.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MatchUpColors.Text,
        )
    }
}

@Composable
private fun PaymentMethodRow(
    method: PaymentMethodDto,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MatchUpColors.SurfaceHigh else MatchUpColors.SurfaceElevated,
        border = BorderStroke(
            1.dp,
            if (selected) MatchUpColors.Lime.copy(alpha = 0.6f) else MatchUpColors.OutlineSoft,
        ),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(19.dp)
                    .background(
                        if (selected) MatchUpColors.Lime else MatchUpColors.Outline,
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .background(MatchUpColors.OnLime, CircleShape),
                    )
                }
            }
            Spacer(Modifier.width(11.dp))
            Column {
                Text(
                    text = method.label.ifBlank { method.method.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    color = MatchUpColors.Text,
                )
                Text(
                    text = method.accountName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MatchUpColors.TextMuted,
                )
            }
        }
    }
}

@Composable
private fun AccountDetails(method: PaymentMethodDto, onCopy: () -> Unit) {
    val strings = LocalStrings.current
    SurfaceCard(color = MatchUpColors.SurfaceElevated) {
        Text(
            text = method.label.ifBlank { method.method.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = MatchUpColors.Lime,
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = strings.accountName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MatchUpColors.TextFaint,
                )
                Text(
                    text = method.accountName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MatchUpColors.Text,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = strings.accountNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = MatchUpColors.TextFaint,
                )
                Text(
                    text = method.account,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                    color = MatchUpColors.Text,
                )
            }
            TextButton(onClick = onCopy) {
                Icon(
                    Icons.Outlined.ContentCopy,
                    contentDescription = strings.copyAccount,
                    tint = MatchUpColors.Lime,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(5.dp))
                Text(strings.copy, color = MatchUpColors.Lime, style = MaterialTheme.typography.labelMedium)
            }
        }
        if (method.instructions.isNotEmpty()) {
            Spacer(Modifier.height(11.dp))
            method.instructions.forEach { instruction ->
                CheckLine(instruction)
                Spacer(Modifier.height(5.dp))
            }
        }
        Spacer(Modifier.height(3.dp))
        CheckLine(strings.paymentSafety)
    }
}

@Composable
private fun PaymentSuccessCard(
    submission: PaymentSubmissionDto?,
    booking: BookingDto,
) {
    val strings = LocalStrings.current
    SurfaceCard(color = MatchUpColors.Mint.copy(alpha = 0.06f), borderColor = MatchUpColors.Mint.copy(alpha = 0.24f)) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .size(38.dp)
                    .background(MatchUpColors.Mint.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Payments,
                    contentDescription = null,
                    tint = MatchUpColors.Mint,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = strings.paymentSubmitted,
                    style = MaterialTheme.typography.titleLarge,
                    color = MatchUpColors.Text,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = submission?.message?.takeIf { it.isNotBlank() } ?: strings.paymentSubmittedBody,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MatchUpColors.TextMuted,
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Row {
            Column(Modifier.weight(1f)) {
                Text(strings.referenceCode, style = MaterialTheme.typography.bodySmall, color = MatchUpColors.TextFaint)
                Text(booking.referenceCode, style = MaterialTheme.typography.titleMedium, color = MatchUpColors.Text)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(strings.status, style = MaterialTheme.typography.bodySmall, color = MatchUpColors.TextFaint)
                Text(
                    text = submission?.status?.replace('_', ' ')?.replaceFirstChar { it.uppercase() }
                        ?: strings.bookingVerifying,
                    style = MaterialTheme.typography.titleMedium,
                    color = MatchUpColors.Mint,
                )
            }
        }
    }
}

@Composable
private fun PaymentStatusCard(booking: BookingDto) {
    val strings = LocalStrings.current
    val confirmed = booking.status == "confirmed"
    SurfaceCard(
        color = if (confirmed) MatchUpColors.Mint.copy(alpha = 0.06f) else MatchUpColors.Blue.copy(alpha = 0.06f),
        borderColor = if (confirmed) MatchUpColors.Mint.copy(alpha = 0.22f) else MatchUpColors.Blue.copy(alpha = 0.22f),
    ) {
        Text(
            text = et.matchup.app.util.statusLabel(booking.status, strings),
            style = MaterialTheme.typography.titleLarge,
            color = if (confirmed) MatchUpColors.Mint else MatchUpColors.Blue,
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = if (confirmed) strings.paymentConfirmedBody else strings.paymentUnderReviewBody,
            style = MaterialTheme.typography.bodyMedium,
            color = MatchUpColors.TextMuted,
        )
        Spacer(Modifier.height(12.dp))
        Row {
            Text(
                text = "${strings.referenceCode}: ${booking.referenceCode}",
                style = MaterialTheme.typography.bodySmall,
                color = MatchUpColors.TextFaint,
            )
        }
    }
}

@Composable
private fun ExpiredCard() {
    val strings = LocalStrings.current
    SurfaceCard(
        color = MatchUpColors.Amber.copy(alpha = 0.07f),
        borderColor = MatchUpColors.Amber.copy(alpha = 0.23f),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.HourglassTop,
                contentDescription = null,
                tint = MatchUpColors.Amber,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = strings.holdExpired,
                    style = MaterialTheme.typography.titleMedium,
                    color = MatchUpColors.Text,
                )
                Text(
                    text = strings.holdExpiredBody,
                    style = MaterialTheme.typography.bodySmall,
                    color = MatchUpColors.TextMuted,
                )
            }
        }
    }
}
