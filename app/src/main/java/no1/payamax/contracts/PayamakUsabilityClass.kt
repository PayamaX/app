package no1.payamax.contracts

import androidx.compose.ui.graphics.Color

enum class PayamakUsabilityClass(val color: Color) {
    Important(Color.Black),
    Usable(Color.Gray),
    Unknown(Color.Magenta),
    Spam(Color.Red),
    ;
}