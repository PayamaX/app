package no1.payamax.cleanCompose.core.presentation.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes()

object ShapesExtension {
    
    val Shapes.toast: RoundedCornerShape get() = RoundedCornerShape(size = 12.dp)
    val Shapes.iconClickArea: RoundedCornerShape get() = RoundedCornerShape(size = 4.dp)
    val Shapes.button: RoundedCornerShape get() = RoundedCornerShape(size = 12.dp)
    val Shapes.checkBox: RoundedCornerShape get() = RoundedCornerShape(size = 6.dp)
}