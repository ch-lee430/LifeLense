package com.example.test.presentation.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.domain.model.ReferenceItem
import com.example.test.presentation.screen.CardStyle

@Composable
fun CallTextCardContent(item: ReferenceItem, style: CardStyle) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = style.icon,
            contentDescription = style.typeName,
            tint = style.iconColor.copy(alpha = 0.8f),
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = item.displayValue,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}