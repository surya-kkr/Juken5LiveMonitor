package com.kkr.juken5livemonitor.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun GlassButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    containerColor: Color = Color(0xFFE53935),
    onClick: () -> Unit
) {

    val scope = rememberCoroutineScope()

    val scale = remember {
        Animatable(1f)
    }

    val buttonColor by animateColorAsState(
        targetValue = if (enabled)
            containerColor
        else
            Color(0xFF7A7A7A),
        label = ""
    )

    Button(
        onClick = {

            scope.launch {

                scale.animateTo(
                    0.94f,
                    animationSpec = tween(60)
                )

                scale.animateTo(
                    1f,
                    animationSpec = tween(120)
                )

            }

            onClick()

        },
        enabled = enabled,

        modifier = modifier
            .defaultMinSize(minWidth = 120.dp)
            .height(42.dp)
            .scale(scale.value),

        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            disabledContainerColor = Color(0xFF555555)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp,
            hoveredElevation = 10.dp
        )
    ) {

        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

    }

}