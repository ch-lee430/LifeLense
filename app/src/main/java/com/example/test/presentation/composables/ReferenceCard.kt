package com.example.test.presentation.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.domain.model.ReferenceItem
import com.example.test.domain.model.ReferenceType
import com.example.test.presentation.screen.formatTimestamp
import com.example.test.presentation.screen.getCardStyle

@Composable
fun ReferenceCard(
    item: ReferenceItem,
    onCardClick: (ReferenceItem) -> Unit
) {
    val style = getCardStyle(item.type)

    // 전화 기록(CALL)만 클릭 불가능
    val isClickable = item.type != ReferenceType.CALL

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = style.backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .width(130.dp)
            .height(140.dp)
            .then(
                if (isClickable) {
                    Modifier.clickable { onCardClick(item) }
                } else {
                    Modifier // 클릭 불가능
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = style.typeName,
                color = style.iconColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            when (item.type) {
                // PHOTO는 display/functional이 같음
                ReferenceType.PHOTO -> PhotoCardContent(item)
                ReferenceType.CALL, ReferenceType.SMS -> CallTextCardContent(item, style)
                // CALENDAR도 functional/display가 같음
                ReferenceType.CALENDAR -> CalendarCardContent(item, style)
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = formatTimestamp(item.date),
                fontSize = 10.sp,
                color = style.iconColor.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}