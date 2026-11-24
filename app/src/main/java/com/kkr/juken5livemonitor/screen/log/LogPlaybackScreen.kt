package com.kkr.juken5livemonitor.screen.log

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.kkr.juken5livemonitor.data.db.*
import com.kkr.juken5livemonitor.ui.components.GlassBackground
import com.kkr.juken5livemonitor.viewmodel.LogViewModel
import kotlinx.coroutines.delay
import kotlin.math.max

private val Red = Color(0xFFFF2638)
private val Panel = Color(0xCC12171D)
private val Muted = Color(0xFFAFB6C2)

@Composable
fun LogPlaybackScreen(navController: NavHostController, sessionId: Long, vm: LogViewModel = viewModel()) {
    var entries by remember { mutableStateOf<List<LogEntry>>(emptyList()) }
    var session by remember { mutableStateOf<LogSession?>(null) }
    var index by remember { mutableIntStateOf(0) }; var playing by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf("RPM") }; var expanded by remember { mutableStateOf(false) }
    LaunchedEffect(sessionId) {
        entries = vm.getEntries(sessionId)
        session = vm.getSession(sessionId)
    }
    LaunchedEffect(playing, index) { if (playing && index < entries.lastIndex) { delay((entries[index + 1].timeOffset - entries[index].timeOffset).coerceIn(16L, 1000L)); index++ } else if (index >= entries.lastIndex && entries.isNotEmpty()) playing = false }
    val e = entries.getOrNull(index)
    val fileName = session?.title ?: "Log session #$sessionId"
    GlassBackground { BoxWithConstraints(Modifier.fillMaxSize()) { val landscape = maxWidth > 700.dp; Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { IconButton({ navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }; Column(Modifier.weight(1f)) { Text("Log Playback", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold); Text(fileName, color = Muted, fontSize = 16.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) }; IconButton({}) { Icon(Icons.Default.FolderOpen, null, tint = Color.White) }; IconButton({}) { Icon(Icons.Default.MoreVert, null, tint = Color.White) } }
        Spacer(Modifier.height(8.dp))
        if (e == null) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Red) } else if (expanded) Column(Modifier.fillMaxSize()) { Row(verticalAlignment = Alignment.CenterVertically) { IconButton({ expanded = false }) { Icon(Icons.Default.Close, null, tint = Color.White) }; Text("$selected chart", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold) }; Chart(entries, index, selected, Modifier.fillMaxSize().padding(16.dp)) } else if (landscape) { Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(14.dp)) { Sensors(e, Modifier.weight(1f)); ChartPanel(entries, index, selected, { selected = it }, { expanded = true }, Modifier.weight(1.2f)) }; Controls(entries, index, playing, { index = it }, { playing = !playing }) } else { Column(Modifier.weight(1f)) { Sensors(e, Modifier.weight(1f)); ChartPanel(entries, index, selected, { selected = it }, { expanded = true }, Modifier.height(270.dp)) }; Controls(entries, index, playing, { index = it }, { playing = !playing }) }
    } } }
}

private fun sensorValues(e: LogEntry) = listOf("RPM" to e.rpm.toString(), "TPS" to "${e.tps}%", "AFR" to "%.1f".format(e.afr), "Speed" to "${e.speed.toInt()} km/h", "Battery" to "%.1f V".format(e.battery), "Engine Temp" to "${e.eot.toInt()} °C", "Air Temp" to "${e.iat.toInt()} °C", "Ignition" to "%.1f°".format(e.ignition), "Manifold Abs. Pressure" to "${e.map} kPa", "Core" to e.fuel, "Base Map" to e.baseMap, "Injector" to e.injector)
@Composable private fun Sensors(e: LogEntry, m: Modifier) { LazyVerticalGrid(GridCells.Fixed(2), m, contentPadding = PaddingValues(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { items(sensorValues(e)) { (label, value) -> Card(colors = CardDefaults.cardColors(Panel), shape = RoundedCornerShape(18.dp)) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Red.copy(.12f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Tune, null, tint = Red) }; Column(Modifier.padding(start = 10.dp)) { Text(label, color = Color.White, fontSize = 14.sp); Text(value, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold) } } } } } }
@Composable private fun ChartPanel(es: List<LogEntry>, i: Int, selected: String, select: (String) -> Unit, expand: () -> Unit, m: Modifier) { Card(m.fillMaxWidth().clickable { expand() }, colors = CardDefaults.cardColors(Panel), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(10.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { listOf("RPM", "TPS", "AFR", "MAP", "Injection").forEach { t -> Text(t, Modifier.clip(RoundedCornerShape(12.dp)).clickable { select(t) }.background(if (t == selected) Red else Color.Transparent).padding(horizontal = 11.dp, vertical = 8.dp), color = Color.White, fontSize = 13.sp) }; Spacer(Modifier.weight(1f)); Icon(Icons.Default.Fullscreen, null, tint = Color.White) }; Chart(es, i, selected, Modifier.fillMaxWidth().weight(1f)) } } }
@Composable private fun Chart(es: List<LogEntry>, i: Int, metric: String, m: Modifier) { Canvas(m) { if (es.isEmpty()) return@Canvas; val p = es.map { when (metric) { "TPS" -> it.tps.toFloat(); "AFR" -> it.afr.toFloat(); "MAP" -> it.map.toFloat(); "Injection" -> it.injector.toFloatOrNull() ?: 0f; else -> it.rpm.toFloat() } }; val maxV = max(p.maxOrNull() ?: 1f, 1f); val step = size.width / max(1, p.lastIndex); for (n in 1..5) drawLine(Color.White.copy(.08f), Offset(0f, size.height*n/6), Offset(size.width, size.height*n/6)); val path = Path(); p.forEachIndexed { n, v -> val q = Offset(n*step, size.height-v/maxV*size.height*.9f); if (n == 0) path.moveTo(q.x,q.y) else path.lineTo(q.x,q.y) }; val fill = Path().apply { addPath(path); lineTo(size.width,size.height); lineTo(0f,size.height); close() }; drawPath(fill, Brush.verticalGradient(listOf(Red.copy(.45f), Color.Transparent))); drawPath(path, Red, style = androidx.compose.ui.graphics.drawscope.Stroke(3.dp.toPx())); val x = i.coerceIn(0,p.lastIndex)*step; drawLine(Color.White.copy(.5f), Offset(x,0f), Offset(x,size.height)) } }
@Composable private fun Controls(es: List<LogEntry>, i: Int, playing: Boolean, seek: (Int) -> Unit, toggle: () -> Unit) { Column { Slider(i.toFloat(), { seek(it.toInt()) }, valueRange = 0f..es.lastIndex.coerceAtLeast(1).toFloat(), colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Red)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { IconButton({ seek((i-1).coerceAtLeast(0)) }) { Icon(Icons.Default.SkipPrevious, null, tint = Color.White) }; IconButton(toggle, Modifier.size(64.dp).background(Red, RoundedCornerShape(32.dp))) { Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(36.dp)) }; IconButton({ seek((i+1).coerceAtMost(es.lastIndex)) }) { Icon(Icons.Default.SkipNext, null, tint = Color.White) } } } }
