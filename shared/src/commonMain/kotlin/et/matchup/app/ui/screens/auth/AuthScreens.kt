package et.matchup.app.ui.screens.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import et.matchup.app.AppState
import et.matchup.app.data.ApiException
import et.matchup.app.platform.GoogleSignInProblem
import et.matchup.app.platform.rememberGoogleSignIn
import et.matchup.app.ui.LocalStrings
import et.matchup.app.ui.Strings
import et.matchup.app.ui.components.DividerLine
import et.matchup.app.ui.components.GoogleButton
import et.matchup.app.ui.components.MatchUpTextField
import et.matchup.app.ui.components.OfflineBanner
import et.matchup.app.ui.components.PasswordField
import et.matchup.app.ui.components.PasswordStrengthMeter
import et.matchup.app.ui.components.PrimaryButton
import et.matchup.app.ui.components.SmallTextLink
import et.matchup.app.ui.components.TopBar
import et.matchup.app.ui.components.Wordmark
import et.matchup.app.ui.theme.MatchUpColors
import et.matchup.app.util.isValidEmail
import et.matchup.app.util.passwordProblem
import kotlinx.coroutines.launch

@Composable
private fun AuthPage(
    title: String = "",
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MatchUpColors.Background),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(MatchUpColors.Lime.copy(alpha = 0.085f), Color.Transparent),
                    center = Offset(size.width * 0.9f, size.height * 0.04f),
                    radius = size.width * 0.8f,
                ),
                radius = size.width * 0.8f,
                center = Offset(size.width * 0.9f, size.height * 0.04f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(MatchUpColors.Mint.copy(alpha = 0.035f), Color.Transparent),
                    center = Offset(size.width * 0.05f, size.height * 0.8f),
                    radius = size.width * 0.7f,
                ),
                radius = size.width * 0.7f,
                center = Offset(size.width * 0.05f, size.height * 0.8f),
            )
        }
        Column(Modifier.fillMaxSize()) {
            if (onBack != null) {
                TopBar(title = title, onBack = onBack)
            } else {
                Spacer(
                    Modifier
                        .statusBarsPadding()
                        .height(10.dp),
                )
            }
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 22.dp)
                    .padding(top = if (onBack == null) 8.dp else 0.dp, bottom = 28.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun InlineError(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        color = MatchUpColors.Red.copy(alpha = 0.1f),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = MatchUpColors.Red,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MatchUpColors.Red,
            )
        }
    }
}

@Composable
private fun SuccessBanner(title: String, body: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MatchUpColors.Mint.copy(alpha = 0.09f),
    ) {
        Row(
            Modifier.padding(15.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = MatchUpColors.Mint,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MatchUpColors.Text,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MatchUpColors.TextMuted,
                )
            }
        }
    }
}

private fun Throwable.formMessage(fallback: String): String =
    if (this is ApiException) message else fallback

@Composable
fun LoginScreen(
    state: AppState,
    online: Boolean,
    onSignup: () -> Unit,
    onForgot: () -> Unit,
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var googleLoading by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var fieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val emailError = when {
        fieldErrors["email"] != null -> fieldErrors["email"]
        submitted && email.isBlank() -> strings.emailRequired
        submitted && !isValidEmail(email) -> strings.invalidEmail
        else -> null
    }
    val passwordError = when {
        fieldErrors["password"] != null -> fieldErrors["password"]
        submitted && password.isBlank() -> strings.passwordRequired
        else -> null
    }

    val google = rememberGoogleSignIn(
        onToken = { token ->
            scope.launch {
                googleLoading = true
                error = null
                try {
                    state.signInWithGoogle(token)
                } catch (throwable: Throwable) {
                    error = throwable.formMessage(strings.errorNetwork)
                } finally {
                    googleLoading = false
                }
            }
        },
        onError = { problem ->
            googleLoading = false
            error = when (problem) {
                GoogleSignInProblem.NotConfigured -> strings.googleNotConfigured
                GoogleSignInProblem.Unavailable -> strings.googleUnavailable
                GoogleSignInProblem.Cancelled -> strings.googleCancelled
                GoogleSignInProblem.NoAccount -> strings.googleNoAccount
                GoogleSignInProblem.Failed -> strings.googleFailed
            }
        },
    )

    AuthPage {
        Spacer(Modifier.height(14.dp))
        Wordmark(markSize = 38.dp)
        Spacer(Modifier.height(36.dp))
        Text(
            text = strings.welcomeBack,
            style = MaterialTheme.typography.displaySmall,
            color = MatchUpColors.Text,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = strings.signInSubtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MatchUpColors.TextMuted,
        )
        Spacer(Modifier.height(24.dp))
        OfflineBanner(visible = !online)
        if (!online) Spacer(Modifier.height(14.dp))
        if (error != null) {
            InlineError(error!!)
            Spacer(Modifier.height(14.dp))
        }
        MatchUpTextField(
            value = email,
            onValueChange = {
                email = it
                fieldErrors = fieldErrors - "email"
                error = null
            },
            label = strings.email,
            placeholder = strings.emailPlaceholder,
            isError = emailError != null,
            supportingText = emailError,
            leadingIcon = Icons.Outlined.Mail,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
        )
        Spacer(Modifier.height(16.dp))
        PasswordField(
            value = password,
            onValueChange = {
                password = it
                fieldErrors = fieldErrors - "password"
                error = null
            },
            label = strings.password,
            placeholder = strings.passwordPlaceholder,
            isError = passwordError != null,
            supportingText = passwordError,
            imeAction = ImeAction.Done,
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onForgot) {
                Text(
                    text = strings.forgotPassword,
                    style = MaterialTheme.typography.labelMedium,
                    color = MatchUpColors.Lime,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        PrimaryButton(
            text = if (submitting) strings.signingIn else strings.signIn,
            onClick = {
                submitted = true
                error = null
                if (email.isBlank() || !isValidEmail(email) || password.isBlank()) {
                    error = strings.pleaseFixFields
                    return@PrimaryButton
                }
                scope.launch {
                    submitting = true
                    try {
                        state.login(email.trim(), password)
                    } catch (throwable: Throwable) {
                        if (throwable is ApiException) fieldErrors = throwable.fields
                        error = throwable.formMessage(strings.errorNetwork)
                    } finally {
                        submitting = false
                    }
                }
            },
            enabled = online,
            loading = submitting,
        )
        Spacer(Modifier.height(20.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f)) { DividerLine() }
            Text(
                text = strings.or,
                style = MaterialTheme.typography.bodySmall,
                color = MatchUpColors.TextFaint,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            Box(Modifier.weight(1f)) { DividerLine() }
        }
        Spacer(Modifier.height(20.dp))
        GoogleButton(
            text = if (googleLoading) strings.googleSigningIn else strings.continueWithGoogle,
            onClick = google.launch,
            loading = googleLoading,
            enabled = online,
        )
        Spacer(Modifier.height(26.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = strings.noAccount,
                style = MaterialTheme.typography.bodyMedium,
                color = MatchUpColors.TextMuted,
            )
            SmallTextLink(text = strings.createAccount, onClick = onSignup)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = strings.authTerms,
            style = MaterialTheme.typography.bodySmall,
            color = MatchUpColors.TextFaint,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun SignupScreen(
    state: AppState,
    online: Boolean,
    onBack: () -> Unit,
    onSignIn: () -> Unit,
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var fieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val nameError = when {
        fieldErrors["full_name"] != null -> fieldErrors["full_name"]
        submitted && fullName.trim().length < 2 -> strings.nameTooShort
        else -> null
    }
    val emailError = when {
        fieldErrors["email"] != null -> fieldErrors["email"]
        submitted && email.isBlank() -> strings.emailRequired
        submitted && !isValidEmail(email) -> strings.invalidEmail
        else -> null
    }
    val phoneError = when {
        fieldErrors["phone"] != null -> fieldErrors["phone"]
        submitted && phone.length > 30 -> strings.phoneTooLong
        else -> null
    }
    val passwordError = when {
        fieldErrors["password"] != null -> fieldErrors["password"]
        submitted -> passwordProblem(password, strings)
        else -> null
    }
    val confirmError = when {
        submitted && confirm.isBlank() -> strings.fieldRequired
        submitted && confirm != password -> strings.passwordsDontMatch
        else -> null
    }

    AuthPage(title = strings.createAccount, onBack = onBack) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = strings.createAccount,
            style = MaterialTheme.typography.headlineLarge,
            color = MatchUpColors.Text,
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = strings.createAccountSubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MatchUpColors.TextMuted,
        )
        Spacer(Modifier.height(22.dp))
        OfflineBanner(visible = !online)
        if (!online) Spacer(Modifier.height(14.dp))
        if (error != null) {
            InlineError(error!!)
            Spacer(Modifier.height(14.dp))
        }
        SignupGoogleSection(
            state = state,
            strings = strings,
            online = online,
            onError = { error = it },
        )
        Spacer(Modifier.height(18.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f)) { DividerLine() }
            Text(
                text = strings.or,
                style = MaterialTheme.typography.bodySmall,
                color = MatchUpColors.TextFaint,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            Box(Modifier.weight(1f)) { DividerLine() }
        }
        Spacer(Modifier.height(18.dp))
        MatchUpTextField(
            value = fullName,
            onValueChange = { fullName = it; fieldErrors = fieldErrors - "full_name"; error = null },
            label = strings.fullName,
            placeholder = strings.fullNamePlaceholder,
            isError = nameError != null,
            supportingText = nameError,
            imeAction = ImeAction.Next,
        )
        Spacer(Modifier.height(14.dp))
        MatchUpTextField(
            value = email,
            onValueChange = { email = it; fieldErrors = fieldErrors - "email"; error = null },
            label = strings.email,
            placeholder = strings.emailPlaceholder,
            isError = emailError != null,
            supportingText = emailError,
            leadingIcon = Icons.Outlined.Mail,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
        )
        Spacer(Modifier.height(14.dp))
        MatchUpTextField(
            value = phone,
            onValueChange = { phone = it; fieldErrors = fieldErrors - "phone"; error = null },
            label = "${strings.phone} (${strings.optional})",
            placeholder = strings.phonePlaceholder,
            isError = phoneError != null,
            supportingText = phoneError ?: strings.phoneHelper,
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next,
        )
        Spacer(Modifier.height(14.dp))
        PasswordField(
            value = password,
            onValueChange = { password = it; fieldErrors = fieldErrors - "password"; error = null },
            label = strings.password,
            placeholder = strings.passwordMinPlaceholder,
            supportingText = passwordError ?: strings.passwordHint,
            isError = passwordError != null,
            imeAction = ImeAction.Next,
        )
        PasswordStrengthMeter(
            password = password,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )
        Spacer(Modifier.height(14.dp))
        PasswordField(
            value = confirm,
            onValueChange = { confirm = it; error = null },
            label = strings.confirmPassword,
            placeholder = strings.repeatPasswordPlaceholder,
            supportingText = confirmError,
            isError = confirmError != null,
            imeAction = ImeAction.Done,
        )
        Spacer(Modifier.height(22.dp))
        PrimaryButton(
            text = if (submitting) strings.signingUp else strings.signUp,
            onClick = {
                submitted = true
                error = null
                if (
                    fullName.trim().length < 2 ||
                    !isValidEmail(email) ||
                    passwordProblem(password, strings) != null ||
                    confirm != password
                ) {
                    error = strings.pleaseFixFields
                    return@PrimaryButton
                }
                scope.launch {
                    submitting = true
                    try {
                        state.signup(
                            fullName = fullName,
                            email = email,
                            password = password,
                            phone = phone.ifBlank { null },
                        )
                    } catch (throwable: Throwable) {
                        if (throwable is ApiException) fieldErrors = throwable.fields
                        error = throwable.formMessage(strings.errorNetwork)
                    } finally {
                        submitting = false
                    }
                }
            },
            enabled = online,
            loading = submitting,
        )
        Spacer(Modifier.height(18.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = strings.haveAccount,
                style = MaterialTheme.typography.bodyMedium,
                color = MatchUpColors.TextMuted,
            )
            SmallTextLink(text = strings.signIn, onClick = onSignIn)
        }
    }
}

@Composable
private fun SignupGoogleSection(
    state: AppState,
    strings: Strings,
    online: Boolean,
    onError: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    val google = rememberGoogleSignIn(
        onToken = { token ->
            scope.launch {
                loading = true
                try {
                    state.signInWithGoogle(token)
                } catch (throwable: Throwable) {
                    onError(throwable.formMessage(strings.errorNetwork))
                } finally {
                    loading = false
                }
            }
        },
        onError = { problem ->
            loading = false
            onError(
                when (problem) {
                    GoogleSignInProblem.NotConfigured -> strings.googleNotConfigured
                    GoogleSignInProblem.Unavailable -> strings.googleUnavailable
                    GoogleSignInProblem.Cancelled -> strings.googleCancelled
                    GoogleSignInProblem.NoAccount -> strings.googleNoAccount
                    GoogleSignInProblem.Failed -> strings.googleFailed
                },
            )
        },
    )
    GoogleButton(
        text = if (loading) strings.googleSigningIn else strings.continueWithGoogle,
        onClick = google.launch,
        loading = loading,
        enabled = online,
    )
}

@Composable
fun ForgotPasswordScreen(
    state: AppState,
    online: Boolean,
    onBack: () -> Unit,
    onResetToken: (String) -> Unit,
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val emailError = when {
        submitted && email.isBlank() -> strings.emailRequired
        submitted && !isValidEmail(email) -> strings.invalidEmail
        else -> null
    }

    AuthPage(title = strings.resetPassword, onBack = onBack) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = strings.resetPassword,
            style = MaterialTheme.typography.headlineLarge,
            color = MatchUpColors.Text,
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = strings.resetSubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MatchUpColors.TextMuted,
        )
        Spacer(Modifier.height(22.dp))
        OfflineBanner(visible = !online)
        if (!online) Spacer(Modifier.height(14.dp))
        if (sent) {
            SuccessBanner(strings.resetLinkSent, strings.resetLinkSentBody)
            Spacer(Modifier.height(16.dp))
        }
        if (error != null) {
            InlineError(error!!)
            Spacer(Modifier.height(14.dp))
        }
        MatchUpTextField(
            value = email,
            onValueChange = { email = it; error = null },
            label = strings.email,
            placeholder = strings.emailPlaceholder,
            isError = emailError != null,
            supportingText = emailError,
            leadingIcon = Icons.Outlined.Mail,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
        )
        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            text = if (sending) strings.sending else strings.sendResetLink,
            onClick = {
                submitted = true
                error = null
                if (!isValidEmail(email)) return@PrimaryButton
                scope.launch {
                    sending = true
                    try {
                        state.dependencies.session.requestPasswordReset(email)
                        sent = true
                        submitted = false
                    } catch (throwable: Throwable) {
                        error = throwable.formMessage(strings.errorNetwork)
                    } finally {
                        sending = false
                    }
                }
            },
            enabled = online,
            loading = sending,
        )
        Spacer(Modifier.height(26.dp))
        DividerLine()
        Spacer(Modifier.height(22.dp))
        Text(
            text = strings.resetToken,
            style = MaterialTheme.typography.titleMedium,
            color = MatchUpColors.Text,
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = strings.resetLinkSentBody,
            style = MaterialTheme.typography.bodySmall,
            color = MatchUpColors.TextMuted,
        )
        Spacer(Modifier.height(12.dp))
        MatchUpTextField(
            value = token,
            onValueChange = { token = it; error = null },
            placeholder = strings.resetToken,
            imeAction = ImeAction.Done,
        )
        Spacer(Modifier.height(14.dp))
        PrimaryButton(
            text = strings.updatePassword,
            onClick = {
                if (token.isBlank()) {
                    error = strings.resetTokenRequired
                } else {
                    onResetToken(token.trim())
                }
            },
        )
        Spacer(Modifier.height(14.dp))
        SmallTextLink(
            text = strings.backToSignIn,
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun ResetPasswordScreen(
    state: AppState,
    online: Boolean,
    token: String?,
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    var tokenValue by remember(token) { mutableStateOf(token.orEmpty()) }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val passwordError = when {
        submitted -> passwordProblem(password, strings)
        else -> null
    }
    val confirmError = when {
        submitted && confirm.isBlank() -> strings.fieldRequired
        submitted && confirm != password -> strings.passwordsDontMatch
        else -> null
    }
    val tokenError = when {
        submitted && tokenValue.isBlank() -> strings.resetTokenRequired
        else -> null
    }

    AuthPage(title = strings.resetPassword, onBack = onBack) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = strings.resetPassword,
            style = MaterialTheme.typography.headlineLarge,
            color = MatchUpColors.Text,
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = strings.resetSubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MatchUpColors.TextMuted,
        )
        Spacer(Modifier.height(22.dp))
        OfflineBanner(visible = !online)
        if (!online) Spacer(Modifier.height(14.dp))
        if (error != null) {
            InlineError(error!!)
            Spacer(Modifier.height(14.dp))
        }
        MatchUpTextField(
            value = tokenValue,
            onValueChange = { tokenValue = it; error = null },
            label = strings.resetToken,
            isError = tokenError != null,
            supportingText = tokenError,
            imeAction = ImeAction.Next,
        )
        Spacer(Modifier.height(14.dp))
        PasswordField(
            value = password,
            onValueChange = { password = it; error = null },
            label = strings.newPassword,
            placeholder = strings.passwordMinPlaceholder,
            supportingText = passwordError ?: strings.passwordHint,
            isError = passwordError != null,
            imeAction = ImeAction.Next,
        )
        Spacer(Modifier.height(14.dp))
        PasswordField(
            value = confirm,
            onValueChange = { confirm = it; error = null },
            label = strings.confirmPassword,
            placeholder = strings.repeatPasswordPlaceholder,
            supportingText = confirmError,
            isError = confirmError != null,
            imeAction = ImeAction.Done,
        )
        Spacer(Modifier.height(22.dp))
        PrimaryButton(
            text = if (submitting) strings.updating else strings.updatePassword,
            onClick = {
                submitted = true
                error = null
                if (
                    tokenValue.isBlank() ||
                    passwordProblem(password, strings) != null ||
                    confirm != password
                ) {
                    return@PrimaryButton
                }
                scope.launch {
                    submitting = true
                    try {
                        state.dependencies.session.confirmPasswordReset(tokenValue.trim(), password)
                        state.showMessage(strings.passwordChanged)
                        onDone()
                    } catch (throwable: Throwable) {
                        error = throwable.formMessage(strings.errorNetwork)
                    } finally {
                        submitting = false
                    }
                }
            },
            enabled = online,
            loading = submitting,
        )
    }
}
