package br.com.listado.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CollectMessages(
    messages: Flow<String>,
    snackbarHostState: SnackbarHostState,
) {
    LaunchedEffect(messages) {
        messages.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            Text(text = value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun SimpleLineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
) {
    if (values.isEmpty()) {
        EmptyStateCard(
            title = "Sem pontos para exibir",
            message = "Finalize compras deste item para gerar um histórico visual.",
            modifier = modifier,
        )
        return
    }

    val max = values.maxOrNull() ?: 0f
    val min = values.minOrNull() ?: 0f
    val range = (max - min).takeIf { it > 0f } ?: 1f
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Card(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(20.dp),
        ) {
            val stepX = if (values.size == 1) 0f else size.width / (values.lastIndex.coerceAtLeast(1))
            val points = values.mapIndexed { index, value ->
                val normalized = (value - min) / range
                Offset(
                    x = index * stepX,
                    y = size.height - (normalized * size.height),
                )
            }

            drawLine(
                color = outlineColor,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 2.dp.toPx(),
            )

            points.zipWithNext().forEach { (start, end) ->
                drawLine(
                    color = primaryColor,
                    start = start,
                    end = end,
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }

            points.forEach { point ->
                drawCircle(
                    color = primaryColor,
                    radius = 5.dp.toPx(),
                    center = point,
                )
                drawCircle(
                    color = surfaceColor,
                    radius = 2.dp.toPx(),
                    center = point,
                )
            }

            drawRect(
                color = borderColor,
                style = Stroke(width = 1.dp.toPx()),
            )
        }
    }
}
