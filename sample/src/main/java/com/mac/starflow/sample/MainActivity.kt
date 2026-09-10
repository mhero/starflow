package com.mac.starflow.sample

import android.graphics.Typeface
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.mac.starflow.BackgroundShape
import com.mac.starflow.StarFlowGraph
import com.mac.starflow.bitmapToByteArray
import com.mac.starflow.getScaledIcon
import com.mac.starflow.model.ArrowData
import com.mac.starflow.model.IconData
import com.mac.starflow.model.StarShapeData

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StarFlowShowcase()
                }
            }
        }
    }
}

@Composable
private fun StarFlowShowcase() {
    val context = LocalContext.current
    val typeface = ResourcesCompat.getFont(context, R.font.shooting_star)!!
    val textColor = Color.Gray.toArgb()

    fun icon(res: Int, scaled: Boolean = false) = if (scaled) {
        bitmapToByteArray(getScaledIcon(context, res))
    } else {
        bitmapToByteArray(ContextCompat.getDrawable(context, res)!!.toBitmap())
    }

    val demos = listOf(
        "Graph 1" to @Composable {
            StarFlowGraph(
                starShape = StarShapeData(0xFFDBDFE3.toInt()),
                icons = listOf(
                    IconData(icon(R.drawable.ic_demo_solar), "14,2 units"),
                    IconData(icon(R.drawable.ic_demo_bulb), "10,2 units"),
                    IconData(icon(R.drawable.ic_demo_grid, scaled = true), "2,0 units"),
                ),
                arrows = listOf(
                    ArrowData(fromTo = 0 to 1, thickness = 30f, colorPair = 0xFFFBE8A5.toInt() to 0xFFEFA139.toInt()),
                    ArrowData(fromTo = 0 to 2, thickness = 10f, colorPair = 0xFFFBE8A5.toInt() to 0xFFEFA139.toInt()),
                ),
                typefaceText = typeface,
                iconTextColor = textColor,
            )
        },
        "Graph 2" to @Composable {
            StarFlowGraph(
                starShape = StarShapeData(0xFFDBDFE3.toInt()),
                icons = listOf(
                    IconData(icon(R.drawable.ic_demo_solar), "4,2 units"),
                    IconData(icon(R.drawable.ic_demo_charger), "1,1 units", "56%"),
                    IconData(icon(R.drawable.ic_demo_bulb, scaled = true), "6,5 units"),
                    IconData(icon(R.drawable.ic_demo_grid, scaled = true), "3,4 units"),
                ),
                arrows = listOf(
                    ArrowData(fromTo = 0 to 1, colorPair = 0xFFFBE8A5.toInt() to 0xFFEFA139.toInt()),
                    ArrowData(fromTo = 0 to 2, thickness = 15f, colorPair = 0xFFFBE8A5.toInt() to 0xFFEFA139.toInt()),
                    ArrowData(fromTo = 3 to 2, thickness = 15f, colorPair = 0xFFD5D3EA.toInt() to 0xFF315AE7.toInt()),
                ),
                typefaceText = typeface,
                iconTextColor = textColor,
            )
        },
        "Graph 3(star)" to @Composable {
            StarFlowGraph(
                starShape = StarShapeData(0xFFDBDFE3.toInt()),
                backgroundShapeType = BackgroundShape.Star,
                icons = listOf(
                    IconData(icon(R.drawable.ic_demo_solar), "0 units"),
                    IconData(icon(R.drawable.ic_demo_battery), "0 units", "56%"),
                    IconData(icon(R.drawable.ic_demo_charger), "0 units"),
                    IconData(icon(R.drawable.ic_demo_charger), "4,1 units"),
                    IconData(icon(R.drawable.ic_demo_bulb, scaled = true), "5,2 units"),
                    IconData(icon(R.drawable.ic_demo_grid, scaled = true), "0 units"),
                ),
                arrows = listOf(
                    ArrowData(fromTo = 1 to 4, colorPair = 0xFFC4A7F2.toInt() to 0xFF9747FF.toInt()),
                    ArrowData(fromTo = 3 to 4, thickness = 30f, colorPair = 0xFFC4A7F2.toInt() to 0xFF9747FF.toInt()),
                ),
                typefaceText = typeface,
                iconTextColor = textColor,
            )
        },
        "Graph 4(star)" to @Composable {
            StarFlowGraph(
                starShape = StarShapeData(0xFFDBDFE3.toInt()),
                backgroundShapeType = BackgroundShape.Star,
                icons = listOf(
                    IconData(icon(R.drawable.ic_demo_solar), "4,5 units"),
                    IconData(icon(R.drawable.ic_demo_battery), "1,1 units", "56%"),
                    IconData(icon(R.drawable.ic_demo_charger), "3,1 units"),
                    IconData(icon(R.drawable.ic_demo_bulb, scaled = true), "0,2 units"),
                    IconData(icon(R.drawable.ic_demo_grid, scaled = true), "0 units"),
                ),
                arrows = listOf(
                    ArrowData(fromTo = 0 to 1, colorPair = 0xFFFBE8A5.toInt() to 0xFFEFA139.toInt()),
                    ArrowData(fromTo = 0 to 2, thickness = 20f, colorPair = 0xFFFBE8A5.toInt() to 0xFFEFA139.toInt()),
                    ArrowData(fromTo = 0 to 3, thickness = 5f, colorPair = 0xFFFBE8A5.toInt() to 0xFFEFA139.toInt()),
                ),
                typefaceText = typeface,
                iconTextColor = textColor,
            )
        },
        "Graph 5(star)" to @Composable {
            StarFlowGraph(
                starShape = StarShapeData(0xFFDBDFE3.toInt()),
                backgroundShapeType = BackgroundShape.Star,
                icons = listOf(
                    IconData(icon(R.drawable.ic_demo_solar), "0 units"),
                    IconData(icon(R.drawable.ic_demo_battery), "3,1 units", "56%"),
                    IconData(icon(R.drawable.ic_demo_charger), "10,2 units"),
                    IconData(icon(R.drawable.ic_demo_bulb, scaled = true), "1,2 units"),
                    IconData(icon(R.drawable.ic_demo_grid, scaled = true), "14,5 units"),
                ),
                arrows = listOf(
                    ArrowData(fromTo = 4 to 1, colorPair = 0xFFD5D3EA.toInt() to 0xFF315AE7.toInt()),
                    ArrowData(fromTo = 4 to 2, thickness = 20f, colorPair = 0xFFD5D3EA.toInt() to 0xFF315AE7.toInt()),
                    ArrowData(fromTo = 4 to 3, thickness = 5f, colorPair = 0xFFD5D3EA.toInt() to 0xFF315AE7.toInt()),
                ),
                typefaceText = typeface,
                iconTextColor = textColor,
            )
        },
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(56.dp)
    ) {
        items(demos) { (label, content) ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = label, style = MaterialTheme.typography.titleMedium)
                content()
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun StarFlowShowcasePreview() {
    MaterialTheme {
        Surface {
            StarFlowShowcase()
        }
    }
}
