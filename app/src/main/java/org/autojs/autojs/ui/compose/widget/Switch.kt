package org.autojs.autojs.ui.compose.widget

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.autojs.autojs.ui.compose.theme.AutoXJsTheme

@Composable
fun MySwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: SwitchColors = SwitchDefaults.colors(uncheckedThumbColor = AutoXJsTheme.colors.switchUncheckedThumbColor)
) {
    Switch(
        checked,
        onCheckedChange,
        modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        colors = colors
    )
}