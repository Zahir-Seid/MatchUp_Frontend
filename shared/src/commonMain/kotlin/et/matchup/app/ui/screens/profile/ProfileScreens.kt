package et.matchup.app.ui.screens.profile

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import et.matchup.app.AppState
import et.matchup.app.config.GeneratedConfig
import et.matchup.app.data.ApiException
import et.matchup.app.data.UserDto
import et.matchup.app.data.apiExceptionMessage
import et.matchup.app.ui.LocalStrings
import et.matchup.app.ui.components.Avatar
import et.matchup.app.ui.components.DividerLine
import et.matchup.app.ui.components.EmptyState
import et.matchup.app.ui.components.InfoBanner
import et.matchup.app.ui.components.MatchUpTextField
import et.matchup.app.ui.components.OfflineBanner
import et.matchup.app.ui.components.PasswordField
import et.matchup.app.ui.components.PrimaryButton
import et.matchup.app.ui.components.SecondaryButton
import et.matchup.app.ui.components.SurfaceCard
import et.matchup.app.ui.components.TopBar
import et.matchup.app.ui.theme.MatchUpColors
import et.matchup.app.util.isValidEmail
import et.matchup.app.util.passwordProblem
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    state: AppState,
    user: UserDto,
    online: Boolean,
    onEditProfile: () -> Unit,
    onSavedTeammates: () -> Unit,
    onChangePassword: () -> Unit,
    onVerifyEmail: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    var dialog by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 28.dp),
    ) {
        item {
            Column(Modifier.padding(horizontal = 18.dp)) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = strings.profile,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MatchUpColors.Text,
                )
                Text(
                    text = strings.accountSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MatchUpColors.TextMuted,
                )
                Spacer(Modifier.height(18.dp))
                OfflineBanner(visible = !online)
                if (!online) Spacer(Modifier.height(10.dp))
                SurfaceCard(color = MatchUpColors.SurfaceElevated) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(name = user.fullName.ifBlank { strings.appName }, size = 66.dp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = user.fullName.ifBlank { strings.appName },
                                style = MaterialTheme.typography.titleLarge,
                                color = MatchUpColors.Text,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MatchUpColors.TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(7.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                EmailVerifiedPill(verified = user.emailVerified)
                                if (!user.hasPassword) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MatchUpColors.SurfaceHigh,
                                    ) {
                                        Text(
                                            text = strings.googleAccount,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MatchUpColors.TextMuted,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (user.phone != null) {
                        Spacer(Modifier.height(14.dp))
                        DividerLine()
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Phone,
                                contentDescription = null,
                                tint = MatchUpColors.TextFaint,
                                modifier = Modifier.size(15.dp),
                            )
                            Spacer(Modifier.width(7.dp))
                            Text(
                                text = user.phone.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MatchUpColors.TextMuted,
                            )
                        }
                    }
                    if (!user.emailVerified) {
                        Spacer(Modifier.height(14.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(onClick = onVerifyEmail),
                            shape = RoundedCornerShape(14.dp),
                            color = MatchUpColors.Amber.copy(alpha = 0.08f),
                        ) {
                            Row(
                                Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Outlined.Mail,
                                    contentDescription = null,
                                    tint = MatchUpColors.Amber,
                                    modifier = Modifier.size(17.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = strings.verifyEmail,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MatchUpColors.Amber,
                                    modifier = Modifier.weight(1f),
                                )
                                Icon(
                                    Icons.Outlined.ChevronRight,
                                    contentDescription = null,
                                    tint = MatchUpColors.Amber,
                                    modifier = Modifier.size(17.dp),
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                SectionLabel(strings.account)
                Spacer(Modifier.height(8.dp))
                SurfaceCard {
                    ProfileRow(
                        icon = Icons.Outlined.Edit,
                        title = strings.editProfile,
                        onClick = onEditProfile,
                    )
                    RowDivider()
                    ProfileRow(
                        icon = Icons.Outlined.Groups,
                        title = strings.savedTeammatesTitle,
                        onClick = onSavedTeammates,
                    )
                    if (user.hasPassword) {
                        RowDivider()
                        ProfileRow(
                            icon = Icons.Outlined.Lock,
                            title = strings.changePassword,
                            onClick = onChangePassword,
                        )
                    }
                    RowDivider()
                    ProfileRow(
                        icon = Icons.Outlined.Notifications,
                        title = strings.notifications,
                        onClick = onOpenNotifications,
                    )
                }
                Spacer(Modifier.height(20.dp))
                SectionLabel(strings.offlineData)
                Spacer(Modifier.height(8.dp))
                SurfaceCard {
                    Text(
                        text = strings.offlineDataBody,
                        style = MaterialTheme.typography.bodySmall,
                        color = MatchUpColors.TextMuted,
                    )
                    Spacer(Modifier.height(12.dp))
                    SecondaryButton(
                        text = strings.clearCachedData,
                        onClick = { dialog = "clear" },
                        icon = Icons.Outlined.Delete,
                        danger = true,
                    )
                }
                Spacer(Modifier.height(20.dp))
                SectionLabel(strings.appInfo)
                Spacer(Modifier.height(8.dp))
                SurfaceCard {
                    InfoRow(strings.versionLabel.replace("%s", GeneratedConfig.APP_VERSION), "")
                    Spacer(Modifier.height(8.dp))
                    InfoRow(
                        strings.platformLabel,
                        if (state.isWeb) strings.platformWeb else strings.platformAndroid,
                    )
                }
                Spacer(Modifier.height(20.dp))
                SecondaryButton(
                    text = strings.signOut,
                    onClick = { dialog = "signout" },
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    danger = true,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = strings.signOutEverywhere,
                    style = MaterialTheme.typography.labelMedium,
                    color = MatchUpColors.TextMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { dialog = "signoutall" }
                        .padding(vertical = 12.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = strings.deleteAccountHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MatchUpColors.TextFaint,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    when (dialog) {
        "signout", "signoutall" -> {
            val all = dialog == "signoutall"
            AlertDialog(
                onDismissRequest = { dialog = null },
                title = {
                    Text(if (all) strings.signOutEverywhereConfirmTitle else strings.signOutConfirmTitle)
                },
                text = {
                    Text(if (all) strings.signOutEverywhereConfirmBody else strings.signOutConfirmBody)
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dialog = null
                            scope.launch {
                                if (all) state.logoutEverywhere() else state.logout()
                            }
                        },
                    ) {
                        Text(strings.confirmSignOut, color = MatchUpColors.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { dialog = null }) {
                        Text(strings.cancel, color = MatchUpColors.TextMuted)
                    }
                },
            )
        }
        "clear" -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text(strings.clearCachedData) },
            text = { Text(strings.offlineDataBody) },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.clearCachedData()
                        state.showMessage(strings.cacheCleared)
                        dialog = null
                    },
                ) {
                    Text(strings.clearCachedData, color = MatchUpColors.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { dialog = null }) {
                    Text(strings.cancel, color = MatchUpColors.TextMuted)
                }
            },
        )
    }
}

@Composable
private fun EmailVerifiedPill(verified: Boolean) {
    val strings = LocalStrings.current
    Surface(
        shape = CircleShape,
        color = if (verified) {
            MatchUpColors.Mint.copy(alpha = 0.12f)
        } else {
            MatchUpColors.Amber.copy(alpha = 0.12f)
        },
    ) {
        Row(
            Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (verified) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = if (verified) MatchUpColors.Mint else MatchUpColors.Amber,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = if (verified) strings.emailVerified else strings.emailNotVerified,
                style = MaterialTheme.typography.labelSmall,
                color = if (verified) MatchUpColors.Mint else MatchUpColors.Amber,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MatchUpColors.TextFaint,
    )
}

@Composable
private fun RowDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MatchUpColors.OutlineSoft),
    )
}

@Composable
private fun ProfileRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    tint: Color = MatchUpColors.Lime,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(35.dp)
                .background(tint.copy(alpha = 0.1f), RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MatchUpColors.Text)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MatchUpColors.TextFaint)
            }
        }
        Icon(
            Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MatchUpColors.TextFaint,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MatchUpColors.TextFaint,
            modifier = Modifier.weight(1f),
        )
        if (value.isNotBlank()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MatchUpColors.TextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun EditProfileScreen(
    state: AppState,
    user: UserDto,
    online: Boolean,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    var fullName by remember(user.id) { mutableStateOf(user.fullName) }
    var phone by remember(user.id) { mutableStateOf(user.phone.orEmpty()) }
    var saving by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var fieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val nameError = when {
        fieldErrors["full_name"] != null -> fieldErrors["full_name"]
        submitted && fullName.trim().length < 2 -> strings.fullNameRequired
        else -> null
    }
    val phoneError = when {
        fieldErrors["phone"] != null -> fieldErrors["phone"]
        submitted && phone.length > 30 -> strings.phoneTooLong
        else -> null
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(title = strings.editProfile, onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
        ) {
            item {
                OfflineBanner(visible = !online)
                if (!online) Spacer(Modifier.height(12.dp))
                if (error != null) {
                    InfoBanner(
                        title = strings.errorGeneric,
                        body = error.orEmpty(),
                        tint = MatchUpColors.Red,
                    )
                    Spacer(Modifier.height(12.dp))
                }
                SurfaceCard {
                    Text(
                        text = strings.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MatchUpColors.TextFaint,
                    )
                    Spacer(Modifier.height(5.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Email,
                            contentDescription = null,
                            tint = MatchUpColors.TextMuted,
                            modifier = Modifier.size(17.dp),
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MatchUpColors.Text,
                            modifier = Modifier.weight(1f),
                        )
                        EmailVerifiedPill(user.emailVerified)
                    }
                }
                Spacer(Modifier.height(18.dp))
                MatchUpTextField(
                    value = fullName,
                    onValueChange = { fullName = it; fieldErrors = fieldErrors - "full_name"; error = null },
                    label = strings.fullName,
                    isError = nameError != null,
                    supportingText = nameError,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                )
                Spacer(Modifier.height(14.dp))
                MatchUpTextField(
                    value = phone,
                    onValueChange = { phone = it; fieldErrors = fieldErrors - "phone"; error = null },
                    label = "${strings.phone} (${strings.optional})",
                    placeholder = strings.phonePlaceholder,
                    isError = phoneError != null,
                    supportingText = phoneError ?: strings.phoneHelper,
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                )
                Spacer(Modifier.height(22.dp))
                PrimaryButton(
                    text = if (saving) strings.updating else strings.save,
                    onClick = {
                        submitted = true
                        error = null
                        if (fullName.trim().length < 2) {
                            return@PrimaryButton
                        }
                        scope.launch {
                            saving = true
                            try {
                                state.dependencies.profile.update(
                                    fullName = fullName,
                                    phone = phone.ifBlank { null },
                                )
                                state.showMessage(strings.profileSaved)
                                onBack()
                            } catch (throwable: Throwable) {
                                if (throwable is ApiException) {
                                    fieldErrors = throwable.fields
                                    if (throwable.isMissingEndpoint) {
                                        error = strings.profileSaveUnsupported
                                    } else {
                                        error = throwable.message
                                    }
                                } else {
                                    error = apiExceptionMessage(throwable)
                                }
                            } finally {
                                saving = false
                            }
                        }
                    },
                    enabled = online,
                    loading = saving,
                )
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
fun SavedTeammatesScreen(
    state: AppState,
    user: UserDto,
    online: Boolean,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    var names by remember { mutableStateOf<List<String>>(emptyList()) }
    var newName by remember { mutableStateOf("") }
    var working by remember { mutableStateOf(false) }

    LaunchedEffect(user.id) {
        names = state.dependencies.teammates.list(user.id)
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(title = strings.savedTeammatesTitle, onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
        ) {
            item {
                Text(
                    text = strings.savedTeammatesSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MatchUpColors.TextMuted,
                )
                Spacer(Modifier.height(16.dp))
                MatchUpTextField(
                    value = newName,
                    onValueChange = { newName = it.take(120) },
                    label = strings.addTeammate,
                    placeholder = strings.teammateNamePlaceholder,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                )
                Spacer(Modifier.height(10.dp))
                PrimaryButton(
                    text = strings.addTeammate,
                    onClick = {
                        val clean = newName.trim()
                        if (clean.isBlank()) return@PrimaryButton
                        scope.launch {
                            working = true
                            names = state.dependencies.teammates.add(user.id, clean)
                            newName = ""
                            state.showMessage(strings.teammateAdded)
                            working = false
                        }
                    },
                    enabled = newName.isNotBlank() && !working,
                    icon = Icons.Outlined.PersonAdd,
                )
                Spacer(Modifier.height(20.dp))
            }
            if (names.isEmpty()) {
                item {
                    EmptyState(
                        title = strings.noSavedTeammates,
                        body = strings.noSavedTeammatesBody,
                        icon = Icons.Outlined.Groups,
                    )
                }
            } else {
                item {
                    SurfaceCard {
                        names.forEachIndexed { index, name ->
                            if (index > 0) RowDivider()
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Avatar(name = name, size = 36.dp)
                                Spacer(Modifier.width(11.dp))
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MatchUpColors.Text,
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            names = state.dependencies.teammates.remove(user.id, name)
                                        }
                                    },
                                ) {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = strings.deleteTeammate,
                                        tint = MatchUpColors.TextFaint,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChangePasswordScreen(
    state: AppState,
    user: UserDto,
    online: Boolean,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    var current by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var fieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val currentError = when {
        fieldErrors["current_password"] != null -> fieldErrors["current_password"]
        submitted && current.isBlank() -> strings.currentPasswordRequired
        else -> null
    }
    val newError = when {
        fieldErrors["new_password"] != null -> fieldErrors["new_password"]
        submitted -> passwordProblem(newPassword, strings)
        else -> null
    }
    val confirmError = when {
        submitted && confirm.isBlank() -> strings.fieldRequired
        submitted && confirm != newPassword -> strings.passwordsDontMatch
        else -> null
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(title = strings.changePassword, onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
        ) {
            item {
                Text(
                    text = strings.changePasswordSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MatchUpColors.TextMuted,
                )
                Spacer(Modifier.height(18.dp))
                OfflineBanner(visible = !online)
                if (!online) Spacer(Modifier.height(12.dp))
                if (error != null) {
                    InfoBanner(
                        title = strings.errorGeneric,
                        body = error.orEmpty(),
                        tint = MatchUpColors.Red,
                    )
                    Spacer(Modifier.height(12.dp))
                }
                if (!user.hasPassword) {
                    InfoBanner(
                        title = strings.googleAccount,
                        body = strings.changePasswordSubtitle,
                        tint = MatchUpColors.Blue,
                        icon = Icons.Outlined.Security,
                    )
                    Spacer(Modifier.height(14.dp))
                }
                PasswordField(
                    value = current,
                    onValueChange = { current = it; fieldErrors = fieldErrors - "current_password"; error = null },
                    label = strings.currentPassword,
                    isError = currentError != null,
                    supportingText = currentError,
                )
                Spacer(Modifier.height(14.dp))
                PasswordField(
                    value = newPassword,
                    onValueChange = { newPassword = it; fieldErrors = fieldErrors - "new_password"; error = null },
                    label = strings.newPassword,
                    supportingText = newError ?: strings.passwordHint,
                    isError = newError != null,
                )
                Spacer(Modifier.height(14.dp))
                PasswordField(
                    value = confirm,
                    onValueChange = { confirm = it; error = null },
                    label = strings.confirmNewPassword,
                    supportingText = confirmError,
                    isError = confirmError != null,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                )
                Spacer(Modifier.height(22.dp))
                PrimaryButton(
                    text = if (saving) strings.updating else strings.updatePasswordButton,
                    onClick = {
                        submitted = true
                        error = null
                        if (
                            current.isBlank() ||
                            passwordProblem(newPassword, strings) != null ||
                            confirm != newPassword
                        ) {
                            return@PrimaryButton
                        }
                        scope.launch {
                            saving = true
                            try {
                                state.dependencies.session.changePassword(current, newPassword)
                                state.showMessage(strings.passwordUpdated)
                                onBack()
                            } catch (throwable: Throwable) {
                                if (throwable is ApiException) {
                                    fieldErrors = throwable.fields
                                    error = throwable.message
                                } else {
                                    error = apiExceptionMessage(throwable)
                                }
                            } finally {
                                saving = false
                            }
                        }
                    },
                    enabled = online && user.hasPassword,
                    loading = saving,
                    icon = Icons.Outlined.Lock,
                )
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
fun EmailVerificationScreen(
    state: AppState,
    user: UserDto,
    online: Boolean,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    var token by remember { mutableStateOf(state.initialAuthLink?.verificationToken.orEmpty()) }
    var sending by remember { mutableStateOf(false) }
    var verifying by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }
    var verified by remember { mutableStateOf(user.emailVerified) }
    var message by remember { mutableStateOf<String?>(null) }
    var unsupported by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize()) {
        TopBar(title = strings.verifyEmail, onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
        ) {
            item {
                if (verified) {
                    SurfaceCard(
                        color = MatchUpColors.Mint.copy(alpha = 0.07f),
                        borderColor = MatchUpColors.Mint.copy(alpha = 0.23f),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = MatchUpColors.Mint,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(Modifier.width(9.dp))
                            Text(
                                text = strings.emailVerified,
                                style = MaterialTheme.typography.titleMedium,
                                color = MatchUpColors.Text,
                            )
                        }
                    }
                } else {
                    Text(
                        text = strings.emailVerificationTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MatchUpColors.Text,
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = strings.emailVerificationBody,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MatchUpColors.TextMuted,
                    )
                    Spacer(Modifier.height(18.dp))
                    OfflineBanner(visible = !online)
                    if (!online) Spacer(Modifier.height(12.dp))
                    if (unsupported) {
                        InfoBanner(
                            title = strings.verifyEmail,
                            body = strings.verificationUnsupported,
                            tint = MatchUpColors.Amber,
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    if (sent) {
                        InfoBanner(
                            title = strings.verificationSent,
                            body = strings.emailVerificationBody,
                            tint = MatchUpColors.Mint,
                            icon = Icons.Outlined.Mail,
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    if (message != null) {
                        InfoBanner(
                            title = strings.verificationSuccess,
                            body = message.orEmpty(),
                            tint = MatchUpColors.Mint,
                            icon = Icons.Outlined.CheckCircle,
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    if (error != null) {
                        InfoBanner(
                            title = strings.errorGeneric,
                            body = error.orEmpty(),
                            tint = MatchUpColors.Red,
                            icon = Icons.Outlined.ErrorOutline,
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    PrimaryButton(
                        text = if (sending) strings.sending else strings.sendVerificationEmail,
                        onClick = {
                            scope.launch {
                                sending = true
                                error = null
                                unsupported = false
                                try {
                                    state.dependencies.profile.requestEmailVerification()
                                    sent = true
                                } catch (throwable: Throwable) {
                                    if (throwable is ApiException && throwable.isMissingEndpoint) {
                                        unsupported = true
                                    } else {
                                        error = apiExceptionMessage(throwable)
                                    }
                                } finally {
                                    sending = false
                                }
                            }
                        },
                        enabled = online && !sending,
                        loading = sending,
                        icon = Icons.Outlined.Mail,
                    )
                    Spacer(Modifier.height(24.dp))
                    DividerLine()
                    Spacer(Modifier.height(20.dp))
                    MatchUpTextField(
                        value = token,
                        onValueChange = { token = it; error = null },
                        label = strings.verificationToken,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                    )
                    Spacer(Modifier.height(12.dp))
                    SecondaryButton(
                        text = if (verifying) strings.loading else strings.verify,
                        onClick = {
                            if (token.isBlank()) {
                                error = strings.resetTokenRequired
                                return@SecondaryButton
                            }
                            scope.launch {
                                verifying = true
                                error = null
                                unsupported = false
                                try {
                                    val response = state.dependencies.profile.confirmEmailVerification(token.trim())
                                    message = response
                                    verified = true
                                    state.showMessage(strings.verificationSuccess)
                                    runCatching { state.dependencies.session.refreshProfile() }
                                } catch (throwable: Throwable) {
                                    if (throwable is ApiException && throwable.isMissingEndpoint) {
                                        unsupported = true
                                    } else {
                                        error = apiExceptionMessage(throwable)
                                    }
                                } finally {
                                    verifying = false
                                }
                            }
                        },
                        enabled = online && !verifying,
                        icon = Icons.Outlined.CheckCircle,
                    )
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}
