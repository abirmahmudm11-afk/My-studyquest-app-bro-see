package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.AppLanguage
import com.example.core.OfflineVaultManager
import com.example.core.StudyQuestStrings
import com.example.ui.components.StudyQuestCard
import com.example.ui.theme.BrandTealPrimary
import com.example.ui.theme.EditorialSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MineralTeal
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

private data class StemLabSpec(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val subtitleEn: String,
    val subtitleBn: String
)

private data class ChemElementMeta(val z: Int, val symbol: String, val nameEn: String, val nameBn: String)
private data class BioOrganelleSpec(val id: String, val nameEn: String, val nameBn: String, val roleEn: String, val roleBn: String)

private val FIRST_20_ELEMENTS = listOf(
    ChemElementMeta(1, "H", "Hydrogen", "হাইড্রোজেন"),
    ChemElementMeta(2, "He", "Helium", "হিলিয়াম"),
    ChemElementMeta(3, "Li", "Lithium", "লিথিয়াম"),
    ChemElementMeta(4, "Be", "Beryllium", "বেরিলিয়াম"),
    ChemElementMeta(5, "B", "Boron", "বোরন"),
    ChemElementMeta(6, "C", "Carbon", "কার্বন"),
    ChemElementMeta(7, "N", "Nitrogen", "নাইট্রোজেন"),
    ChemElementMeta(8, "O", "Oxygen", "অক্সিজেন"),
    ChemElementMeta(9, "F", "Fluorine", "ফ্লোরিন"),
    ChemElementMeta(10, "Ne", "Neon", "নিয়ন"),
    ChemElementMeta(11, "Na", "Sodium", "সোডিয়াম"),
    ChemElementMeta(12, "Mg", "Magnesium", "ম্যাগনেসিয়াম"),
    ChemElementMeta(13, "Al", "Aluminium", "অ্যালুমিনিয়াম"),
    ChemElementMeta(14, "Si", "Silicon", "সিলিকন"),
    ChemElementMeta(15, "P", "Phosphorus", "ফসফরাস"),
    ChemElementMeta(16, "S", "Sulfur", "সালফার"),
    ChemElementMeta(17, "Cl", "Chlorine", "ক্লোরিন"),
    ChemElementMeta(18, "Ar", "Argon", "আর্গন"),
    ChemElementMeta(19, "K", "Potassium", "পটাসিয়াম"),
    ChemElementMeta(20, "Ca", "Calcium", "ক্যালসিয়াম")
)

private val BIO_ORGANELLES = listOf(
    BioOrganelleSpec(
        "nucleus",
        "Nucleus",
        "নিউক্লিয়াস",
        "Controls gene expression, DNA replication, and cell division (mitosis/meiosis).",
        "কোষের জিন নিয়ন্ত্রণ, ডিএনএ প্রতিলিপি ও কোষ বিভাজন পরিচালনা করে।"
    ),
    BioOrganelleSpec(
        "mitochondria",
        "Mitochondria",
        "মাইটোকন্ড্রিয়া",
        "Powerhouse of cellular aerobic respiration converting glucose into ATP energy.",
        "সবাত শ্বসনের মাধ্যমে গ্লুকোজ থেকে এটিপি (ATP) শক্তি উৎপাদন করে।"
    ),
    BioOrganelleSpec(
        "ribosome",
        "Ribosomes",
        "রাইবোজোম",
        "Translates mRNA chains into structural proteins and enzymes across cytoplasm & ER.",
        "এমআরএনএ থেকে প্রোটিন ও এনজাইম সংশ্লেষণের প্রধান কেন্দ্র।"
    ),
    BioOrganelleSpec(
        "golgi",
        "Golgi Apparatus",
        "গলগি বডি",
        "Packages, modifies, and secretes synthesized proteins & lipids into vesicles.",
        "সংশ্লেষিত প্রোটিন ও লিপিড প্রক্রিয়াজাত ও পরিবহন করে।"
    ),
    BioOrganelleSpec(
        "chloroplast",
        "Chloroplast (Plant)",
        "ক্লোরোপ্লাস্ট (উদ্ভিদ)",
        "Captures solar photons via chlorophyll: 6CO2 + 6H2O -> C6H12O6 + 6O2.",
        "ক্লোরোফিলের সাহায্যে সালোকসংশ্লেষণ প্রক্রিয়ায় শর্করা তৈরি করে।"
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InteractiveStemLabScreen(
    uiLanguage: AppLanguage,
    initialLabId: String = "trig",
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val labs = remember {
        listOf(
            StemLabSpec("trig", "Trigonometry", "ত্রিকোণমিতি", "Unit Circle & Polar Ratios", "একক বৃত্ত ও অনুপাত"),
            StemLabSpec("physics", "Kinematics", "গতিবিদ্যা", "Projectile Trajectory Simulator", "প্রাস ও গতিপথ"),
            StemLabSpec("chem", "Atomic Shells", "পরমাণু গঠন", "Bohr Model (Z = 1..20)", "বোর মডেল ও ইলেকট্রন বিন্যাস"),
            StemLabSpec("bio", "Cell Atlas", "কোষ অ্যাটলাস", "Eukaryotic Organelle Map", "অঙ্গাণু ও কার্যপ্রণালী")
        )
    }
    var activeLabId by remember(initialLabId) {
        mutableStateOf(labs.firstOrNull { it.id == initialLabId }?.id ?: "trig")
    }
    val activeSpec = remember(activeLabId) { labs.first { it.id == activeLabId } }

    // Load bundled high-DPI reference plate from Study Vault
    val highDpiPlate by produceState<Bitmap?>(initialValue = null, key1 = activeLabId) {
        value = OfflineVaultManager.loadLabPlate(context, activeLabId, sampleSize = 2)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = StudyQuestStrings.t(uiLanguage, "Offline STEM Lab", "অফলাইন STEM ল্যাব"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = StudyQuestStrings.t(uiLanguage, "Interactive NCTB Simulation Studio", "ইন্টারঅ্যাক্টিভ এনসিটিবি সিমুলেশন"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("stem_lab_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = StudyQuestStrings.t(uiLanguage, "Back", "ফিরে যাও")
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 960.dp)
                    .fillMaxSize()
                    .testTag("stem_lab_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(112.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_vault_stem_lab),
                                    contentDescription = StudyQuestStrings.t(uiLanguage, "STEM Lab Artwork", "STEM ল্যাব চিত্র"),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                labs.forEach { spec ->
                                    FilterChip(
                                        selected = activeLabId == spec.id,
                                        onClick = { activeLabId = spec.id },
                                        leadingIcon = {
                                            Icon(Icons.Filled.Science, contentDescription = null, modifier = Modifier.size(16.dp))
                                        },
                                        label = { Text(StudyQuestStrings.t(uiLanguage, spec.titleEn, spec.titleBn)) },
                                        modifier = Modifier.testTag("lab_chip_${spec.id}")
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    when (activeLabId) {
                        "trig" -> TrigonometryInteractiveLab(uiLanguage)
                        "physics" -> ProjectileInteractiveLab(uiLanguage)
                        "chem" -> BohrAtomInteractiveLab(uiLanguage)
                        else -> CellAtlasInteractiveLab(uiLanguage)
                    }
                }

                if (highDpiPlate != null) {
                    item {
                        StudyQuestCard(modifier = Modifier.testTag("stem_lab_reference_plate")) {
                            Text(
                                text = StudyQuestStrings.t(
                                    uiLanguage,
                                    "Bundled High-DPI Reference Plate — ${activeSpec.titleEn}",
                                    "অফলাইন উচ্চ-রেজোলিউশন প্লেট — ${activeSpec.titleBn}"
                                ),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = StudyQuestStrings.t(uiLanguage, activeSpec.subtitleEn, activeSpec.subtitleBn),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                            Image(
                                bitmap = highDpiPlate!!.asImageBitmap(),
                                contentDescription = StudyQuestStrings.t(
                                    uiLanguage,
                                    "Offline high-resolution diagram plate for ${activeSpec.titleEn}",
                                    "${activeSpec.titleBn} অফলাইন রেফারেন্স ডায়াগ্রাম"
                                ),
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.5f)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrigonometryInteractiveLab(uiLanguage: AppLanguage) {
    var angleDeg by remember { mutableFloatStateOf(45f) }
    val rad = angleDeg.toDouble() * PI / 180.0
    val sinV = sin(rad)
    val cosV = cos(rad)
    val tanV = if (kotlin.math.abs(cosV) < 0.001) Double.NaN else tan(rad)

    val axisColor = MaterialTheme.colorScheme.outlineVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val accentCos = MineralTeal
    val accentSin = EditorialSuccess

    StudyQuestCard(modifier = Modifier.testTag("lab_card_trig")) {
        Text(
            text = StudyQuestStrings.t(uiLanguage, "Unit Circle Trigonometry", "একক বৃত্ত ও ত্রিকোণমিতি"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = StudyQuestStrings.t(uiLanguage, "Drag theta to inspect sin, cos & Pythagorean identity", "কোণ পরিবর্তন করে sin, cos ও অভেদ পর্যবেক্ষণ করো"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = (minOf(size.width, size.height) * 0.40f)
                drawLine(axisColor, Offset(cx - r * 1.2f, cy), Offset(cx + r * 1.2f, cy), strokeWidth = 2f)
                drawLine(axisColor, Offset(cx, cy - r * 1.2f), Offset(cx, cy + r * 1.2f), strokeWidth = 2f)
                drawCircle(color = axisColor, radius = r, center = Offset(cx, cy), style = Stroke(width = 3f))

                val px = (cx + r * cosV).toFloat()
                val py = (cy - r * sinV).toFloat()
                drawLine(accentCos, Offset(cx, cy), Offset(px, cy), strokeWidth = 6f, cap = StrokeCap.Round)
                drawLine(accentSin, Offset(px, cy), Offset(px, py), strokeWidth = 6f, cap = StrokeCap.Round)
                drawLine(primaryColor, Offset(cx, cy), Offset(px, py), strokeWidth = 5f, cap = StrokeCap.Round)
                drawCircle(primaryColor, radius = 10f, center = Offset(px, py))
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = "theta = ${angleDeg.toInt()}° (${String.format(Locale.US, "%.2f", rad)} rad)",
            fontFamily = JetBrainsMonoFontFamily,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Slider(
            value = angleDeg,
            onValueChange = { angleDeg = it },
            valueRange = 0f..360f,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("lab_trig_slider")
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = String.format(Locale.US, "sin = %+.4f   |   cos = %+.4f", sinV, cosV),
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (tanV.isNaN()) "tan = undefined (cos = 0)" else String.format(Locale.US, "tan = %+.4f   |   sin^2 + cos^2 = %.4f", tanV, sinV * sinV + cosV * cosV),
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProjectileInteractiveLab(uiLanguage: AppLanguage) {
    var velocity by remember { mutableFloatStateOf(30f) }
    var angleDeg by remember { mutableFloatStateOf(45f) }
    val g = 9.81
    val rad = angleDeg.toDouble() * PI / 180.0
    val flightTime = (2.0 * velocity * sin(rad)) / g
    val maxHeight = (velocity * velocity * sin(rad) * sin(rad)) / (2.0 * g)
    val range = (velocity * velocity * sin(2.0 * rad)) / g

    val axisColor = MaterialTheme.colorScheme.outlineVariant
    val curveColor = MaterialTheme.colorScheme.primary
    val peakColor = EditorialSuccess

    StudyQuestCard(modifier = Modifier.testTag("lab_card_physics")) {
        Text(
            text = StudyQuestStrings.t(uiLanguage, "Projectile Kinematics Simulator", "প্রাস ও গতিপথ সিমুলেটর"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = StudyQuestStrings.t(uiLanguage, "Adjust initial speed and launch angle under Earth gravity g = 9.81 m/s^2", "আদিবেগ ও প্রক্ষেপণ কোণ পরিবর্তন করে গতিপথ দেখো"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
        ) {
            val padX = 24f
            val baseY = size.height - 24f
            val usableW = size.width - padX * 2f
            val usableH = size.height - 44f
            drawLine(axisColor, Offset(padX, baseY), Offset(size.width - padX, baseY), strokeWidth = 3f)
            drawLine(axisColor, Offset(padX, 16f), Offset(padX, baseY), strokeWidth = 2f)

            val maxPlotRange = 370.0 // scale fixed at ~370m so widening range looks visibly wider
            val maxPlotHeight = 185.0
            val path = Path()
            val steps = 48
            for (i in 0..steps) {
                val frac = i.toDouble() / steps
                val t = frac * flightTime
                val xMeters = velocity * cos(rad) * t
                val yMeters = (velocity * sin(rad) * t) - (0.5 * g * t * t)
                val px = (padX + (xMeters / maxPlotRange) * usableW).toFloat().coerceIn(padX, size.width - padX)
                val py = (baseY - (yMeters / maxPlotHeight) * usableH).toFloat().coerceIn(16f, baseY)
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            drawPath(path = path, color = curveColor, style = Stroke(width = 6f, cap = StrokeCap.Round))
            val apexX = (padX + ((range * 0.5) / maxPlotRange) * usableW).toFloat().coerceIn(padX, size.width - padX)
            val apexY = (baseY - (maxHeight / maxPlotHeight) * usableH).toFloat().coerceIn(16f, baseY)
            drawCircle(color = peakColor, radius = 9f, center = Offset(apexX, apexY))
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "v0 = ${velocity.toInt()} m/s",
            fontFamily = JetBrainsMonoFontFamily,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Slider(
            value = velocity,
            onValueChange = { velocity = it },
            valueRange = 10f..60f,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("lab_physics_vel_slider")
        )
        Text(
            text = "Launch Angle = ${angleDeg.toInt()}°",
            fontFamily = JetBrainsMonoFontFamily,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Slider(
            value = angleDeg,
            onValueChange = { angleDeg = it },
            valueRange = 15f..75f,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("lab_physics_angle_slider")
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = String.format(Locale.US, "Range R = %.1f m   |   H_max = %.1f m", range, maxHeight),
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format(Locale.US, "Flight Time T = %.2f s   |   vx = %.1f m/s", flightTime, velocity * cos(rad)),
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BohrAtomInteractiveLab(uiLanguage: AppLanguage) {
    var atomicZ by remember { mutableIntStateOf(6) }
    val elem = FIRST_20_ELEMENTS[(atomicZ - 1).coerceIn(0, FIRST_20_ELEMENTS.lastIndex)]
    val shells = remember(atomicZ) { computeBohrShells(atomicZ) }
    val outer = shells.lastOrNull() ?: 0
    val valency = when {
        atomicZ == 2 -> 0
        atomicZ <= 2 -> (2 - atomicZ).coerceAtLeast(0)
        outer <= 4 -> outer
        else -> (8 - outer).coerceAtLeast(0)
    }

    val orbitColor = MaterialTheme.colorScheme.outlineVariant
    val electronColor = BrandTealPrimary
    val nucleusColor = EditorialSuccess

    StudyQuestCard(modifier = Modifier.testTag("lab_card_chem")) {
        Text(
            text = StudyQuestStrings.t(uiLanguage, "Bohr Atomic Shell Explorer (Z = 1..20)", "বোর পরমাণু মডেল ও ইলেকট্রন বিন্যাস"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = StudyQuestStrings.t(uiLanguage, "Slide atomic number to inspect K, L, M, N shell distributions", "পারমাণবিক সংখ্যা পরিবর্তন করে K, L, M, N শক্তিস্তর দেখো"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(215.dp)
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val maxR = minOf(size.width, size.height) * 0.43f
            drawCircle(color = nucleusColor, radius = 16f, center = Offset(cx, cy))

            val shellCount = shells.size.coerceAtLeast(1)
            shells.forEachIndexed { idx, eCount ->
                val radius = maxR * ((idx + 1).toFloat() / shellCount.coerceAtLeast(3).toFloat())
                drawCircle(color = orbitColor, radius = radius, center = Offset(cx, cy), style = Stroke(width = 2.5f))
                for (k in 0 until eCount) {
                    val theta = (2.0 * PI * k / eCount.coerceAtLeast(1)) - PI / 2.0
                    val ex = (cx + radius * cos(theta)).toFloat()
                    val ey = (cy + radius * sin(theta)).toFloat()
                    drawCircle(color = electronColor, radius = 7f, center = Offset(ex, ey))
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "Z = ${elem.z} • ${elem.symbol} (${StudyQuestStrings.t(uiLanguage, elem.nameEn, elem.nameBn)})",
            fontFamily = JetBrainsMonoFontFamily,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Slider(
            value = atomicZ.toFloat(),
            onValueChange = { atomicZ = it.toInt().coerceIn(1, 20) },
            valueRange = 1f..20f,
            steps = 18,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("lab_chem_slider")
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth()
        ) {
            val shellNames = listOf("K", "L", "M", "N")
            val shellText = shells.mapIndexed { i, cnt -> "${shellNames.getOrElse(i) { "S$i" }}=$cnt" }.joinToString("  ")
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Config: $shellText",
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Rule 2n^2 | Outer electrons: $outer | Valency: $valency",
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CellAtlasInteractiveLab(uiLanguage: AppLanguage) {
    var selectedOrganelleId by remember { mutableStateOf("nucleus") }
    val selected = BIO_ORGANELLES.first { it.id == selectedOrganelleId }

    val membraneColor = MaterialTheme.colorScheme.outlineVariant
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val highlightColor = EditorialSuccess

    StudyQuestCard(modifier = Modifier.testTag("lab_card_bio")) {
        Text(
            text = StudyQuestStrings.t(uiLanguage, "Eukaryotic Cell Organelle Atlas", "ইউক্যারিওটিক কোষ অঙ্গাণু মানচিত্র"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = StudyQuestStrings.t(uiLanguage, "Tap an organelle to inspect its structure and function", "কোনো অঙ্গাণু ট্যাপ করে অবস্থান ও কাজ দেখো"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val rx = size.width * 0.42f
            val ry = size.height * 0.42f

            // Outer membrane
            drawOval(
                color = membraneColor,
                topLeft = Offset(cx - rx, cy - ry),
                size = Size(rx * 2f, ry * 2f),
                style = Stroke(width = 5f)
            )
            // Nucleus
            drawCircle(
                color = if (selectedOrganelleId == "nucleus") highlightColor else mutedColor,
                radius = minOf(rx, ry) * 0.34f,
                center = Offset(cx - rx * 0.12f, cy - ry * 0.05f),
                style = Stroke(width = 6f)
            )
            drawCircle(
                color = if (selectedOrganelleId == "nucleus") highlightColor.copy(alpha = 0.25f) else mutedColor.copy(alpha = 0.15f),
                radius = minOf(rx, ry) * 0.20f,
                center = Offset(cx - rx * 0.12f, cy - ry * 0.05f)
            )
            // Mitochondria
            drawOval(
                color = if (selectedOrganelleId == "mitochondria") highlightColor else mutedColor,
                topLeft = Offset(cx + rx * 0.24f, cy - ry * 0.48f),
                size = Size(rx * 0.38f, ry * 0.28f),
                style = Stroke(width = 5f)
            )
            // Golgi Apparatus
            for (step in 0..2) {
                val offY = step * 14f
                drawArc(
                    color = if (selectedOrganelleId == "golgi") highlightColor else mutedColor,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(cx - rx * 0.58f, cy + ry * 0.16f + offY),
                    size = Size(rx * 0.42f, 24f),
                    style = Stroke(width = 4.5f, cap = StrokeCap.Round)
                )
            }
            // Ribosomes
            val riboPts = listOf(
                Offset(cx + rx * 0.34f, cy + ry * 0.25f),
                Offset(cx + rx * 0.46f, cy + ry * 0.15f),
                Offset(cx + rx * 0.25f, cy + ry * 0.40f),
                Offset(cx - rx * 0.36f, cy - ry * 0.44f)
            )
            riboPts.forEach { p ->
                drawCircle(
                    color = if (selectedOrganelleId == "ribosome") highlightColor else mutedColor,
                    radius = if (selectedOrganelleId == "ribosome") 8f else 5.5f,
                    center = p
                )
            }
            // Chloroplast
            drawOval(
                color = if (selectedOrganelleId == "chloroplast") highlightColor else mutedColor,
                topLeft = Offset(cx - rx * 0.64f, cy - ry * 0.35f),
                size = Size(rx * 0.30f, ry * 0.22f),
                style = Stroke(width = 5f)
            )
        }

        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            BIO_ORGANELLES.forEach { org ->
                FilterChip(
                    selected = selectedOrganelleId == org.id,
                    onClick = { selectedOrganelleId = org.id },
                    label = { Text(StudyQuestStrings.t(uiLanguage, org.nameEn, org.nameBn)) },
                    modifier = Modifier.testTag("bio_org_${org.id}")
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = StudyQuestStrings.t(uiLanguage, selected.nameEn, selected.nameBn),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = StudyQuestStrings.t(uiLanguage, selected.roleEn, selected.roleBn),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun computeBohrShells(atomicZ: Int): List<Int> {
    var rem = atomicZ.coerceIn(1, 20)
    val caps = listOf(2, 8, 8, 2)
    return buildList {
        for (cap in caps) {
            if (rem <= 0) break
            val take = minOf(rem, cap)
            add(take)
            rem -= take
        }
    }
}
