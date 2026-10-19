package et.matchup.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.matchup.app.ui.LocalStrings
import et.matchup.app.ui.theme.MatchUpColors
import et.matchup.app.util.initials
import kotlin.math.absoluteValue

@Composable
fun BrandMark(modifier: Modifier = Modifier, size: Dp = 40.dp, dark: Boolean = false) {
    Canvas(modifier.size(size)) {
        val radius = this.size.minDimension * 0.28f
        drawRoundRect(
            color = if (dark) MatchUpColors.OnLime else MatchUpColors.Lime,
            cornerRadius = CornerRadius(radius, radius),
        )
        val inset = this.size.width * 0.19f
        val w = this.size.width
        val h = this.size.height
        val line = Stroke(width = w * 0.085f, cap = StrokeCap.Round)
        drawLine(
            color = if (dark) MatchUpColors.Lime else MatchUpColors.OnLime,
            start = Offset(inset, h - inset),
            end = Offset(inset, inset + h * 0.16f),
            strokeWidth = line.width,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = if (dark) MatchUpColors.Lime else MatchUpColors.OnLime,
            start = Offset(inset, inset + h * 0.16f),
            end = Offset(w / 2, h * 0.62f),
            strokeWidth = line.width,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = if (dark) MatchUpColors.Lime else MatchUpColors.OnLime,
            start = Offset(w / 2, h * 0.62f),
            end = Offset(w - inset, inset + h * 0.16f),
            strokeWidth = line.width,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = if (dark) MatchUpColors.Lime else MatchUpColors.OnLime,
            start = Offset(w - inset, inset + h * 0.16f),
            end = Offset(w - inset, h - inset),
            strokeWidth = line.width,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun Wordmark(
    modifier: Modifier = Modifier,
    markSize: Dp = 28.dp,
    showMark: Boolean = true,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (showMark) {
            BrandMark(size = markSize)
            Spacer(Modifier.width(9.dp))
        }
        Text(
            text = "MATCH",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp,
            ),
            color = MatchUpColors.Text,
        )
        Text(
            text = "UP",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp,
            ),
            color = MatchUpColors.Lime,
        )
    }
}

@Composable
fun FieldArt(
    seed: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val palettes = listOf(
        listOf(Color(0xFF1A3D2C), Color(0xFF0D1D15), Color(0xFF6B9E3E)),
        listOf(Color(0xFF193247), Color(0xFF0C1720), Color(0xFF4A8C8E)),
        listOf(Color(0xFF3D2B18), Color(0xFF1D140C), Color(0xFFC07A3A)),
        listOf(Color(0xFF32203F), Color(0xFF160F1D), Color(0xFF8A5B9E)),
        listOf(Color(0xFF3B2323), Color(0xFF1C1111), Color(0xFFB2604F)),
    )
    val palette = palettes[(seed.hashCode() and Int.MAX_VALUE) % palettes.size]
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawRect(
                Brush.linearGradient(
                    colors = listOf(palette[0], palette[1]),
                    start = Offset.Zero,
                    end = Offset(w, h),
                ),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(palette[2].copy(alpha = 0.34f), Color.Transparent),
                    center = Offset(w * 0.18f, h * 0.15f),
                    radius = w * 0.85f,
                ),
                radius = w * 0.85f,
                center = Offset(w * 0.18f, h * 0.15f),
            )
            val white = Color.White.copy(alpha = 0.10f)
            val pad = w * 0.06f
            drawRoundRect(
                color = white,
                topLeft = Offset(pad, pad * 0.6f),
                size = Size(w - pad * 2, h - pad * 1.2f),
                cornerRadius = CornerRadius(w * 0.025f, w * 0.025f),
                style = Stroke(width = 3f),
            )
            drawLine(
                color = white,
                start = Offset(w / 2, pad * 0.6f),
                end = Offset(w / 2, h - pad * 0.6f),
                strokeWidth = 3f,
            )
            drawCircle(
                color = white,
                radius = h * 0.15f,
                center = Offset(w / 2, h / 2),
                style = Stroke(width = 3f),
            )
            drawCircle(
                color = white,
                radius = h * 0.025f,
                center = Offset(w / 2, h / 2),
            )
            drawRoundRect(
                color = white,
                topLeft = Offset(pad, h * 0.31f),
                size = Size(w * 0.12f, h * 0.38f),
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(width = 3f),
            )
            drawRoundRect(
                color = white,
                topLeft = Offset(w - pad - w * 0.12f, h * 0.31f),
                size = Size(w * 0.12f, h * 0.38f),
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(width = 3f),
            )
            drawRect(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, MatchUpColors.Background.copy(alpha = 0.82f)),
                    startY = h * 0.3f,
                    endY = h,
                ),
            )
        }
        content()
    }
}

@Composable
fun TopBar(
    title: String? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(62.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = LocalStrings.current.back,
                    tint = MatchUpColors.Text,
                )
            }
        } else {
            Spacer(Modifier.width(48.dp))
        }
        Text(
            text = title.orEmpty(),
            style = MaterialTheme.typography.titleLarge,
            color = MatchUpColors.Text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        actions()
    }
}

@Composable
fun ScreenTitle(
    eyebrow: String? = null,
    title: String,
    body: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        if (eyebrow != null) {
            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MatchUpColors.Lime,
            )
            Spacer(Modifier.height(8.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MatchUpColors.Text,
        )
        if (body != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MatchUpColors.TextMuted,
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MatchUpColors.Text,
            modifier = Modifier.weight(1f),
        )
        action?.invoke()
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(17.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MatchUpColors.Lime,
            contentColor = MatchUpColors.OnLime,
            disabledContainerColor = MatchUpColors.SurfaceHigh,
            disabledContentColor = MatchUpColors.TextFaint,
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 14.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MatchUpColors.OnLime,
            )
            Spacer(Modifier.width(10.dp))
        } else if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(9.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    danger: Boolean = false,
) {
    val tint = if (danger) MatchUpColors.Red else MatchUpColors.Text
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(17.dp),
        border = BorderStroke(1.dp, if (danger) MatchUpColors.Red.copy(alpha = 0.45f) else MatchUpColors.Outline),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = tint,
            disabledContentColor = MatchUpColors.TextFaint,
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 13.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(9.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun GoogleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(17.dp),
        border = BorderStroke(1.dp, MatchUpColors.Outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MatchUpColors.Text),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MatchUpColors.Lime,
            )
        } else {
            Box(
                Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "G",
                    color = Color(0xFF4285F4),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun MatchUpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 6,
) {
    Column(modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MatchUpColors.TextMuted,
            )
            Spacer(Modifier.height(7.dp))
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            textStyle = MaterialTheme.typography.bodyLarge,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            isError = isError,
            shape = RoundedCornerShape(16.dp),
            leadingIcon = leadingIcon?.let { vector ->
                {
                    Icon(
                        imageVector = vector,
                        contentDescription = null,
                        tint = MatchUpColors.TextFaint,
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
            trailingIcon = trailingIcon,
            placeholder = placeholder?.let { hint ->
                {
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MatchUpColors.TextFaint,
                    )
                }
            },
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MatchUpColors.Lime.copy(alpha = 0.75f),
                unfocusedBorderColor = MatchUpColors.Outline,
                disabledBorderColor = MatchUpColors.OutlineSoft,
                errorBorderColor = MatchUpColors.Red.copy(alpha = 0.7f),
                focusedContainerColor = MatchUpColors.SurfaceElevated,
                unfocusedContainerColor = MatchUpColors.SurfaceElevated,
                disabledContainerColor = MatchUpColors.Surface,
                errorContainerColor = MatchUpColors.SurfaceElevated,
                focusedTextColor = MatchUpColors.Text,
                unfocusedTextColor = MatchUpColors.Text,
                disabledTextColor = MatchUpColors.TextFaint,
                cursorColor = MatchUpColors.Lime,
                errorCursorColor = MatchUpColors.Red,
                focusedPlaceholderColor = MatchUpColors.TextFaint,
                unfocusedPlaceholderColor = MatchUpColors.TextFaint,
            ),
        )
        if (supportingText != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) MatchUpColors.Red else MatchUpColors.TextFaint,
            )
        }
    }
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
) {
    var visible by remember { mutableStateOf(false) }
    MatchUpTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardType = KeyboardType.Password,
        imeAction = imeAction,
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (visible) LocalStrings.current.hidePassword else LocalStrings.current.showPassword,
                    tint = MatchUpColors.TextMuted,
                    modifier = Modifier.size(20.dp),
                )
            }
        },
    )
}

@Composable
fun FilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Surface(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = if (selected) MatchUpColors.Lime else MatchUpColors.SurfaceElevated,
        contentColor = if (selected) MatchUpColors.OnLime else MatchUpColors.TextMuted,
        border = BorderStroke(
            1.dp,
            if (selected) MatchUpColors.Lime else MatchUpColors.OutlineSoft,
        ),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun <T> SegmentedControl(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(MatchUpColors.SurfaceElevated)
            .border(1.dp, MatchUpColors.OutlineSoft, RoundedCornerShape(17.dp))
            .padding(4.dp),
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(13.dp))
                    .background(if (isSelected) MatchUpColors.Lime else Color.Transparent)
                    .clickable { onSelect(value) }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) MatchUpColors.OnLime else MatchUpColors.TextMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun StatusPill(status: String, label: String, modifier: Modifier = Modifier) {
    val (fg, bg) = when (status) {
        "confirmed" -> MatchUpColors.Mint to MatchUpColors.Mint.copy(alpha = 0.13f)
        "verifying", "pending_review" -> MatchUpColors.Blue to MatchUpColors.Blue.copy(alpha = 0.13f)
        "pending_payment" -> MatchUpColors.Amber to MatchUpColors.Amber.copy(alpha = 0.13f)
        "cancelled" -> MatchUpColors.Red to MatchUpColors.Red.copy(alpha = 0.12f)
        "expired" -> MatchUpColors.TextMuted to MatchUpColors.SurfaceHigh
        else -> MatchUpColors.TextMuted to MatchUpColors.SurfaceHigh
    }
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = bg,
        contentColor = fg,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
fun Avatar(name: String, size: Dp = 44.dp, modifier: Modifier = Modifier) {
    val palettes = listOf(
        listOf(MatchUpColors.Lime, MatchUpColors.Mint),
        listOf(MatchUpColors.Amber, MatchUpColors.Red),
        listOf(MatchUpColors.Blue, MatchUpColors.Mint),
        listOf(Color(0xFFE4B5FF), MatchUpColors.Blue),
    )
    val palette = palettes[(name.hashCode() and Int.MAX_VALUE) % palettes.size]
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(palette)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials(name),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = MatchUpColors.OnLime,
        )
    }
}

@Composable
fun IconBubble(
    icon: ImageVector,
    tint: Color = MatchUpColors.Lime,
    size: Dp = 42.dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.11f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.48f))
    }
}

@Composable
fun InfoBanner(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Info,
    tint: Color = MatchUpColors.Amber,
    action: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = tint.copy(alpha = 0.09f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.24f)),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MatchUpColors.Text,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MatchUpColors.TextMuted,
                )
            }
            action?.invoke()
        }
    }
}

@Composable
fun OfflineBanner(visible: Boolean, modifier: Modifier = Modifier) {
    if (!visible) return
    val strings = LocalStrings.current
    InfoBanner(
        title = strings.offlineTitle,
        body = strings.offlineBody,
        modifier = modifier,
        icon = Icons.Outlined.WifiOff,
        tint = MatchUpColors.Amber,
    )
}

@Composable
fun LoadingState(text: String = LocalStrings.current.loading, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            color = MatchUpColors.Lime,
            strokeWidth = 2.5.dp,
            modifier = Modifier.size(30.dp),
        )
        Spacer(Modifier.height(14.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MatchUpColors.TextMuted)
    }
}

@Composable
fun EmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.ErrorOutline,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconBubble(icon = icon, tint = MatchUpColors.Lime, size = 60.dp)
        Spacer(Modifier.height(18.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MatchUpColors.Text,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MatchUpColors.TextMuted,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            Spacer(Modifier.height(20.dp))
            action()
        }
    }
}

@Composable
fun ErrorState(
    title: String,
    body: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconBubble(icon = Icons.Outlined.ErrorOutline, tint = MatchUpColors.Red, size = 60.dp)
        Spacer(Modifier.height(18.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MatchUpColors.Text,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MatchUpColors.TextMuted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        SecondaryButton(
            text = LocalStrings.current.retry,
            onClick = onRetry,
            icon = Icons.Outlined.Refresh,
            modifier = Modifier.widthIn(max = 220.dp),
        )
    }
}

@Composable
fun SpotCounter(
    count: Int,
    max: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MatchUpColors.SurfaceElevated)
            .border(1.dp, MatchUpColors.OutlineSoft, RoundedCornerShape(16.dp))
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CounterButton(enabled = count > 1, label = "−") { onChange((count - 1).coerceAtLeast(1)) }
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = MatchUpColors.Text,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(42.dp),
        )
        CounterButton(enabled = count < max, label = "+") { onChange((count + 1).coerceAtMost(max)) }
    }
}

@Composable
private fun CounterButton(enabled: Boolean, label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (enabled) MatchUpColors.SurfaceHigh else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = if (enabled) MatchUpColors.Text else MatchUpColors.TextFaint,
        )
    }
}

@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    color: Color = MatchUpColors.Surface,
    borderColor: Color = MatchUpColors.OutlineSoft,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = color,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun DividerLine(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MatchUpColors.OutlineSoft),
    )
}

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MatchUpColors.Surface,
        shadowElevation = 18.dp,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .imePadding(),
            content = content,
        )
    }
}

@Composable
fun ActionsRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
fun SmallTextLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MatchUpColors.Lime,
) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = color,
        )
    }
}

@Composable
fun CheckLine(text: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = Icons.Outlined.Check,
            contentDescription = null,
            tint = MatchUpColors.Lime,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp),
        )
        Spacer(Modifier.width(9.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MatchUpColors.TextMuted,
        )
    }
}

@Composable
fun CenterSpinner(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            color = MatchUpColors.Lime,
            strokeWidth = 2.5.dp,
            modifier = Modifier.size(28.dp),
        )
    }
}
