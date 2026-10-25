package dev.jdtech.jellyfin.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas as ComposeCanvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jdtech.jellyfin.core.R as CoreR
import dev.jdtech.jellyfin.presentation.theme.LocalSystemS
import dev.jdtech.jellyfin.presentation.theme.SystemSButtonShape
import dev.jdtech.jellyfin.presentation.theme.pixelStyle
import dev.jdtech.jellyfin.presentation.theme.systemS
import kotlinx.coroutines.launch

/*
 * sombra System S's widgets, as sombrAMP draws them (music/sombrAMP/lib/sombra/system_s_widgets.dart and the shapes
 * in system_s.dart): windows with an ink edge and an extruded shadow, pinstriped title bars, the dotted desk, and
 * buttons that invert while pressed.
 */

/**
 * The hard shadow System S puts under windows: [stepped] draws the extruded shadow of windows and
 * dialogs (copies offset 1dp at a time, `--ext-on`), otherwise one solid copy, as under toasts and
 * menus. It's drawn outside the bounds, below and to the right, so leave room for it.
 */
fun Modifier.systemSShadow(color: Color, depth: Dp, stepped: Boolean = true): Modifier =
    drawBehind {
        val step = 1.dp.toPx()
        val total = depth.toPx()
        if (stepped) {
            var offset = step
            while (offset <= total + 0.01f) {
                drawRect(color, topLeft = Offset(offset, offset), size = size)
                offset += step
            }
        } else {
            drawRect(color, topLeft = Offset(total, total), size = size)
        }
    }

/** A window's frame: the shadow, a pane, and a 1dp ink edge. */
@Composable
fun Modifier.systemSWindowFrame(active: Boolean = true, depth: Dp? = null): Modifier {
    val palette = MaterialTheme.systemS.palette
    return this.systemSShadow(
            color = if (active) palette.ink else palette.idle,
            depth = depth ?: if (active) 6.dp else 5.dp,
        )
        .background(palette.pane)
        .border(1.dp, palette.ink)
}

/**
 * An alert or dialog window: the deep extruded shadow, a 1dp ink edge and, inside it, a 2dp ink
 * ring after a 3dp gap. It pads itself on the right and bottom to make room for the shadow, so put
 * it on the outermost container of the dialog.
 */
@Composable
fun Modifier.systemSDialogFrame(): Modifier {
    val palette = MaterialTheme.systemS.palette
    return this.padding(end = 6.dp, bottom = 6.dp)
        .systemSShadow(palette.ink, 6.dp)
        .border(1.dp, palette.ink)
        .drawWithContent {
            drawContent()
            val inset = 5.dp.toPx()
            drawRect(
                color = palette.ink,
                topLeft = Offset(inset, inset),
                size = Size(size.width - 2 * inset, size.height - 2 * inset),
                style = Stroke(2.dp.toPx()),
            )
        }
}

/** A frame without a shadow: a pane with a 1dp ink edge, as around covers and cards. */
@Composable
fun Modifier.systemSFrame(): Modifier {
    val palette = MaterialTheme.systemS.palette
    return this.background(palette.pane).border(1.dp, palette.ink)
}

/**
 * A System S window: a pane with an ink edge, a title bar (pinstriped while active) and the
 * extruded shadow. It pads itself on the right and bottom to make room for the shadow.
 */
@Composable
fun SystemSWindow(
    title: String,
    modifier: Modifier = Modifier,
    active: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = MaterialTheme.systemS.palette
    Box(
        modifier =
            modifier.padding(end = if (active) 6.dp else 5.dp, bottom = if (active) 6.dp else 5.dp)
    ) {
        Column(modifier = Modifier.systemSWindowFrame(active = active).padding(1.dp)) {
            SystemSTitleBar(title = title, active = active)
            HorizontalDivider(thickness = 1.dp, color = palette.ink)
            content()
        }
    }
}

/** Five 1dp lines, 3dp apart, starting 4dp down behind the title: the active title bar. */
@Composable
fun SystemSTitleBar(title: String, modifier: Modifier = Modifier, active: Boolean = true) {
    val palette = MaterialTheme.systemS.palette
    Box(
        modifier =
            modifier.fillMaxWidth().height(21.dp).drawBehind {
                if (active) {
                    var y = 4.dp.toPx()
                    while (y < 17.dp.toPx()) {
                        drawRect(palette.ink, Offset(0f, y), Size(size.width, 1.dp.toPx()))
                        y += 3.dp.toPx()
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style =
                pixelStyle(
                    15.sp,
                    if (active) FontWeight.SemiBold else FontWeight.Medium,
                    lineHeight = 21.sp,
                ),
            color = if (active) palette.ink else palette.muted,
            modifier =
                Modifier.semantics { heading() }
                    .background(palette.pane)
                    .padding(horizontal = 10.dp),
        )
    }
}

/** The desktop: the desk colour with its 4dp dot grid. */
@Composable
fun Modifier.systemSDesk(): Modifier {
    val palette = MaterialTheme.systemS.palette
    return this.drawWithCache {
        val cell = 4.dp.toPx().toInt().coerceAtLeast(2)
        val tile = ImageBitmap(cell, cell)
        val canvas = ComposeCanvas(tile)
        canvas.drawRect(
            0f,
            0f,
            cell.toFloat(),
            cell.toFloat(),
            Paint().apply { color = palette.desk },
        )
        canvas.drawCircle(
            Offset(cell / 2f, cell / 2f),
            1.2.dp.toPx(),
            Paint().apply { color = palette.deskDot },
        )
        val brush = ShaderBrush(ImageShader(tile, TileMode.Repeated, TileMode.Repeated))
        onDrawBehind { drawRect(brush) }
    }
}

/**
 * System S's buttons: ink-outlined panes that invert while pressed. The focus edges a button in the
 * phosphor. The default button of a group ([isDefault]) gets an extra ring, drawn 4dp outside its
 * bounds.
 */
@Composable
fun SystemSButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isDefault: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val systemS = MaterialTheme.systemS
    val palette = systemS.palette
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val focused by interactionSource.collectIsFocusedAsState()

    val containerColor = if (enabled && pressed) palette.ink else palette.pane
    val contentColor =
        when {
            !enabled -> palette.faint
            pressed -> palette.pane
            else -> palette.ink
        }
    val border =
        when {
            focused -> BorderStroke(2.dp, systemS.phosphorInk)
            !enabled -> BorderStroke(1.dp, palette.faint)
            else -> BorderStroke(1.dp, palette.ink)
        }

    Row(
        modifier =
            modifier
                .then(
                    if (isDefault) Modifier.defaultButtonRing(palette.pane, palette.ink)
                    else Modifier
                )
                .defaultMinSize(minWidth = 64.dp, minHeight = 40.dp)
                .clip(SystemSButtonShape)
                .background(containerColor)
                .border(border, SystemSButtonShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(contentPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor,
            LocalTextStyle provides MaterialTheme.typography.labelLarge,
        ) {
            content()
        }
    }
}

/** The default button's outer ring: a 2dp gap in [gap], then a 2dp ring in [ring]. */
private fun Modifier.defaultButtonRing(gap: Color, ring: Color): Modifier = drawBehind {
    val radius = 10.dp.toPx()
    val width = 2.dp.toPx()
    fun outline(inflate: Float, color: Color) =
        drawRoundRect(
            color = color,
            topLeft = Offset(-inflate, -inflate),
            size = Size(size.width + 2 * inflate, size.height + 2 * inflate),
            cornerRadius = CornerRadius(radius + inflate),
            style = Stroke(width),
        )
    outline(1.dp.toPx(), gap)
    outline(3.dp.toPx(), ring)
}

/**
 * A square System S button holding one icon. [live] draws the icon in the phosphor, for states that
 * are on, like a favourite or a played item.
 */
@Composable
fun SystemSIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    live: Boolean = false,
    content: @Composable () -> Unit,
) {
    val systemS = MaterialTheme.systemS
    SystemSButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        enabled = enabled,
        contentPadding = PaddingValues(0.dp),
    ) {
        if (live && enabled) {
            CompositionLocalProvider(LocalContentColor provides systemS.phosphorInk) { content() }
        } else {
            content()
        }
    }
}

/** System S's colours for Material's components. */
object SystemSDefaults {
    /** The menu bar: a pane, with an ink rule under it (see [systemSMenuBar]). */
    @Composable
    fun topAppBarColors(): TopAppBarColors {
        val palette = MaterialTheme.systemS.palette
        return TopAppBarDefaults.topAppBarColors(
            containerColor = palette.pane,
            scrolledContainerColor = palette.pane,
            navigationIconContentColor = palette.ink,
            titleContentColor = palette.ink,
            actionIconContentColor = palette.ink,
        )
    }

    /** Text fields: square wells with an ink edge, edged in the phosphor while focused. */
    @Composable
    fun textFieldColors(): TextFieldColors {
        val systemS = MaterialTheme.systemS
        val palette = systemS.palette
        return OutlinedTextFieldDefaults.colors(
            focusedContainerColor = palette.well,
            unfocusedContainerColor = palette.well,
            disabledContainerColor = palette.well,
            errorContainerColor = palette.well,
            focusedBorderColor = systemS.phosphorInk,
            unfocusedBorderColor = palette.ink,
            disabledBorderColor = palette.faint,
            errorBorderColor = palette.ink,
            cursorColor = palette.ink,
            focusedLabelColor = palette.ink,
            unfocusedLabelColor = palette.muted,
            errorLabelColor = palette.ink,
            errorSupportingTextColor = palette.ink,
            errorTrailingIconColor = palette.ink,
        )
    }

    /** Switches: ink and pane, no colour. */
    @Composable
    fun switchColors(): SwitchColors {
        val palette = MaterialTheme.systemS.palette
        return SwitchDefaults.colors(
            checkedThumbColor = palette.pane,
            checkedTrackColor = palette.ink,
            checkedBorderColor = palette.ink,
            checkedIconColor = palette.ink,
            uncheckedThumbColor = palette.ink,
            uncheckedTrackColor = palette.pane,
            uncheckedBorderColor = palette.ink,
            disabledCheckedTrackColor = palette.faint,
            disabledUncheckedThumbColor = palette.faint,
            disabledUncheckedBorderColor = palette.faint,
        )
    }

    @Composable
    fun checkboxColors(): CheckboxColors {
        val palette = MaterialTheme.systemS.palette
        return CheckboxDefaults.colors(
            checkedColor = palette.ink,
            uncheckedColor = palette.ink,
            checkmarkColor = palette.pane,
        )
    }

    /** Floating buttons are System S buttons: a pane with an ink edge, no elevation. */
    @Composable
    fun fabModifier(): Modifier =
        Modifier.border(1.dp, MaterialTheme.systemS.palette.ink, SystemSButtonShape)
}

/** The menu bar's ink rule along the bottom of a top app bar. */
@Composable
fun Modifier.systemSMenuBar(): Modifier {
    val ink = MaterialTheme.systemS.palette.ink
    return this.drawWithContent {
        drawContent()
        val width = 1.dp.toPx()
        drawRect(ink, Offset(0f, size.height - width), Size(size.width, width))
    }
}

/** A bottom sheet's edge: an ink rule along its top. */
@Composable
fun Modifier.systemSMenuBarTop(): Modifier {
    val ink = MaterialTheme.systemS.palette.ink
    return this.drawWithContent {
        drawContent()
        drawRect(ink, Offset.Zero, Size(size.width, 1.dp.toPx()))
    }
}

/** Something loading: loading is live, so it glows in the phosphor. */
@Composable
fun SystemSSpinner(modifier: Modifier = Modifier, size: Dp = 32.dp) {
    val systemS = MaterialTheme.systemS
    CircularProgressIndicator(
        color = systemS.phosphor,
        trackColor = systemS.palette.inset,
        strokeWidth = 3.dp,
        strokeCap = StrokeCap.Butt,
        modifier = modifier.size(size),
    )
}

/** The S mark, drawn in [color] (the content colour by default), as on sombra.wtf. */
@Composable
fun SombraMark(
    modifier: Modifier = Modifier,
    height: Dp = 24.dp,
    color: Color = LocalContentColor.current,
) {
    Icon(
        painter = painterResource(CoreR.drawable.ic_sombra_mark),
        contentDescription = null,
        tint = color,
        modifier = modifier.size(width = height * 61f / 116f, height = height),
    )
}

/** The sombra.wtf wordmark. */
@Composable
fun SombraWordmark(
    modifier: Modifier = Modifier,
    width: Dp = 120.dp,
    color: Color = LocalContentColor.current,
) {
    Icon(
        painter = painterResource(CoreR.drawable.ic_sombra_wordmark),
        contentDescription = "sombra.wtf",
        tint = color,
        modifier = modifier.size(width = width, height = width * 110f / 621f),
    )
}

/**
 * What pressing and focusing look like on plain clickable things, like list rows and covers: no
 * ripple, a flat ink tint while pressed and the phosphor tint while focused.
 */
object SystemSIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        SystemSIndicationNode(interactionSource)

    override fun equals(other: Any?): Boolean = other === this

    override fun hashCode(): Int = -1
}

private class SystemSIndicationNode(private val interactionSource: InteractionSource) :
    Modifier.Node(), DrawModifierNode, CompositionLocalConsumerModifierNode {
    private var pressed = false
    private var focused = false

    override fun onAttach() {
        coroutineScope.launch {
            var presses = 0
            var focuses = 0
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> presses++
                    is PressInteraction.Release -> presses--
                    is PressInteraction.Cancel -> presses--
                    is FocusInteraction.Focus -> focuses++
                    is FocusInteraction.Unfocus -> focuses--
                }
                val isPressed = presses > 0
                val isFocused = focuses > 0
                if (isPressed != pressed || isFocused != focused) {
                    pressed = isPressed
                    focused = isFocused
                    invalidateDraw()
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        val systemS = currentValueOf(LocalSystemS)
        if (pressed) {
            drawRect(systemS.palette.ink.copy(alpha = 0.16f))
        } else if (focused) {
            drawRect(systemS.focus)
        }
    }
}
