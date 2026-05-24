package com.example

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun TransitScreen(
    viewModel: TransitViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val textMeasurer = rememberTextMeasurer()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 720

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TransitTopBar()
        },
        containerColor = SpaceBackground
    ) { innerPadding ->
        if (isTablet) {
            // Adaptive design: Columns side-by-side on wide screens
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Simulators + Graphs (60% width)
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PresetSection(
                        selectedId = uiState.selectedPresetId,
                        onSelect = { viewModel.selectPresetById(it) }
                    )

                    SimulationVisualizers(
                        uiState = uiState,
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                    )

                    LightCurvePlotCard(
                        uiState = uiState,
                        textMeasurer = textMeasurer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Right Column: Scientific Panel & Sliders (40% width)
                Column(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ScienceMetricsGrid(diagnostics = uiState.diagnostics)
                    
                    ControlSlidersCard(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else {
            // Pocket layout: fully stacked
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PresetSection(
                    selectedId = uiState.selectedPresetId,
                    onSelect = { viewModel.selectPresetById(it) }
                )

                SimulationVisualizers(
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )

                LightCurvePlotCard(
                    uiState = uiState,
                    textMeasurer = textMeasurer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )

                ScienceMetricsGrid(diagnostics = uiState.diagnostics)

                ControlSlidersCard(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransitTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "EXOPLANET TRANSIT MONITOR",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = OnSpaceBackground
                )
                Text(
                    text = "Stellar Photometry & Physics Simulator",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        },
        navigationIcon = {
            Icon(
                imageVector = Icons.Default.Public,
                contentDescription = "Planet Icon",
                tint = PlanetSecondary,
                modifier = Modifier.padding(start = 16.dp)
            )
        },
        actions = {
            Icon(
                imageVector = Icons.Default.Flare,
                contentDescription = "Star Icon",
                tint = SolarPrimary,
                modifier = Modifier.padding(end = 16.dp)
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = SpaceBackground,
            titleContentColor = OnSpaceBackground
        )
    )
}

@Composable
fun PresetSection(
    selectedId: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "PHYICAL SYSTEM PRESET",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(TransitPhysics.PRESETS) { system ->
                val isSelected = system.id == selectedId
                Card(
                    modifier = Modifier
                        .width(220.dp)
                        .testTag("preset_${system.id}")
                        .clickable { onSelect(system.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) CosmicSurfaceVariant else CosmicSurface
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) PlanetSecondary else BorderColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = system.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) PlanetSecondary else OnCosmicSurface
                        )
                        Text(
                            text = system.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 3,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimulationVisualizers(
    uiState: TransitUiState,
    viewModel: TransitViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Visualizer control bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CosmicSurfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector = if (uiState.isSimulationRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = OnCosmicSurface,
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { viewModel.toggleSimulation() }
                    )
                    Text(
                        text = if (uiState.showTopDownView) "Double Viewport" else "Transit Viewport",
                        style = MaterialTheme.typography.titleSmall,
                        color = OnCosmicSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.toggleTopDownView() },
                        modifier = Modifier.testTag("toggle_viewport")
                    ) {
                        Icon(
                            imageVector = if (uiState.showTopDownView) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle top-down assist",
                            tint = PlanetSecondary
                        )
                    }
                    
                    // Simulation speed selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Speed:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        listOf(0.5, 1.0, 2.0).forEach { speed ->
                            val active = uiState.simulationSpeed == speed
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (active) PlanetSecondary else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable { viewModel.setSimulationSpeed(speed) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${speed}x",
                                    color = if (active) SpaceBackground else OnCosmicSurface,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // The Canvas viewports
            Row(
                modifier = Modifier
                    .fillCardWidth()
                    .weight(1f)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Viewport 1: Transit Alignment (Always shown)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Transit Alignment (Focal Plane)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                            .background(SpaceBackground, RoundedCornerShape(8.dp))
                    ) {
                        TransitViewportCanvas(uiState = uiState)
                    }
                }

                // Viewport 2: Top-down physics support (Optional)
                if (uiState.showTopDownView) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Orbital Mechanics (Top-Down Plane)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Box(
                            modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                        .background(SpaceBackground, RoundedCornerShape(8.dp))
                        ) {
                            TopDownViewportCanvas(uiState = uiState)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransitViewportCanvas(uiState: TransitUiState) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2
        val baseRadius = min(width, height) / 5f

        // Star properties scaled
        val starRadiusPx = baseRadius * uiState.starRadiusSolar.toFloat()
        // Planetary radius in solar units
        val rpSolar = (uiState.planetRadiusJupiter * TransitPhysics.JUPITER_RAD_IN_SOLAR_RAD).toFloat()
        val planetRadiusPx = baseRadius * rpSolar

        // Draw star background glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    SolarPrimary.copy(alpha = 0.4f),
                    SolarPrimary.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = starRadiusPx * 2.2f
            ),
            radius = starRadiusPx * 2.2f,
            center = Offset(centerX, centerY)
        )

        // Draw orbital path projection (dashed ellipse based on inclination)
        val iRad = uiState.planetInclinationDegrees * PI / 180.0
        val visualOrbitA = starRadiusPx * 1.8f
        val visualOrbitB = visualOrbitA * cos(iRad).toFloat()
        
        drawOrbitEllipse(
            centerX = centerX,
            centerY = centerY,
            radiusX = visualOrbitA,
            radiusY = visualOrbitB,
            color = BorderColor.copy(alpha = 0.8f)
        )

        // Calculate planet position
        val phase = uiState.currentPhaseRad
        val pX = centerX + visualOrbitA * sin(phase).toFloat()
        val pY = centerY - visualOrbitA * cos(phase).toFloat() * cos(iRad).toFloat()
        
        val planetIsInFront = cos(phase) > 0 // Z-direction where Z is positive behind star

        // Function to draw host star with quadratic limb-darkening aesthetic
        val drawHostStarDisk = {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFF176), // Hot core
                        SolarPrimary,       // Star disk
                        SolarPrimary.copy(alpha = 0.8f - (uiState.planetLimbDarkeningCoef * 0.4).toFloat()), // Darkened edges
                        Color(0xFFE55300).copy(alpha = 0.2f) // Atmosphere edge
                    ),
                    center = Offset(centerX, centerY),
                    radius = starRadiusPx
                ),
                radius = starRadiusPx,
                center = Offset(centerX, centerY)
            )
            
            // Draw neat coronal loops or subtle rings
            drawCircle(
                color = SolarPrimary.copy(alpha = 0.2f),
                radius = starRadiusPx * 1.05f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Layer order determination (3D clipping)
        if (planetIsInFront) {
            // Planet is in front (Transiting)
            drawHostStarDisk()
            
            // Check if planet overlaps stellar disk to draw transit shadow detail
            val distFromCenter = sqrt((pX - centerX) * (pX - centerX) + (pY - centerY) * (pY - centerY))
            val isOverlapping = distFromCenter < (starRadiusPx + planetRadiusPx)
            
            if (isOverlapping && distFromCenter < (starRadiusPx - planetRadiusPx)) {
                // Fully inside stellar disk: draw pitch black transit silhouette with neon glow edge
                drawCircle(
                    color = Color.Black,
                    radius = planetRadiusPx,
                    center = Offset(pX, pY)
                )
                drawCircle(
                    color = PlanetSecondary.copy(alpha = 0.4f),
                    radius = planetRadiusPx + 1.dp.toPx(),
                    center = Offset(pX, pY),
                    style = Stroke(width = 1.dp.toPx())
                )
            } else if (isOverlapping) {
                // Partial overlap (ingress/egress)
                drawCircle(
                    color = Color.Black.copy(alpha = 0.9f),
                    radius = planetRadiusPx,
                    center = Offset(pX, pY)
                )
            } else {
                // Not overlapping, fully off-disk but in front
                drawPlanetDisk(pX, pY, planetRadiusPx)
            }
        } else {
            // Planet is behind star (eclipsed)
            // Only draw planet if it is visible beyond the stellar disk edges
            val distFromCenter = sqrt((pX - centerX) * (pX - centerX) + (pY - centerY) * (pY - centerY))
            val isEclipsed = distFromCenter <= starRadiusPx
            
            if (!isEclipsed) {
                drawPlanetDisk(pX, pY, planetRadiusPx)
            }
            drawHostStarDisk()
        }

        // Draw instrument target crosshairs to emphasize scientific recording
        drawTargetCrosshairs(centerX, centerY, starRadiusPx)
    }
}

@Composable
fun TopDownViewportCanvas(uiState: TransitUiState) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2
        val baseRadius = min(width, height) / 5f

        val starRadiusPx = baseRadius * uiState.starRadiusSolar.toFloat() * 0.7f // slightly scaled-down for top-down comfort
        val rpSolar = (uiState.planetRadiusJupiter * TransitPhysics.JUPITER_RAD_IN_SOLAR_RAD).toFloat()
        val planetRadiusPx = baseRadius * rpSolar * 0.7f

        // Star
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(SolarPrimary, SolarPrimary.copy(alpha = 0.4f), Color.Transparent),
                center = Offset(centerX, centerY),
                radius = starRadiusPx * 1.5f
            ),
            radius = starRadiusPx * 1.5f,
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = SolarPrimary,
            radius = starRadiusPx,
            center = Offset(centerX, centerY)
        )

        // Orbital Path (circular top-down)
        val orbitRadiusPx = starRadiusPx * 2.3f
        drawCircle(
            color = BorderColor,
            radius = orbitRadiusPx,
            center = Offset(centerX, centerY),
            style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
        )

        // Line Of Sight indicator towards Earth (Earth is looking from bottom, i.e. +Y)
        // Draw sweeping sector pointing to observer
        drawLine(
            color = TextMuted.copy(alpha = 0.4f),
            start = Offset(centerX, centerY),
            end = Offset(centerX, height),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f),
            strokeWidth = 1.dp.toPx()
        )

        // Calculate planet position
        // Top down view looks straight flat down.
        // x = orbitRadius * sin(phase), y = orbitRadius * cos(phase) [meaning transit is at front, cos(p) > 0, which is at standard bottom direction]
        val phase = uiState.currentPhaseRad
        val pX = centerX + orbitRadiusPx * sin(phase).toFloat()
        val pY = centerY + orbitRadiusPx * cos(phase).toFloat() // +Y is down on Android

        // Draw Planet representation with shadow tail pointing away from the star (Z-direction)
        val shadowVectorX = (pX - centerX) / orbitRadiusPx
        val shadowVectorY = (pY - centerY) / orbitRadiusPx
        
        // Draw shadow tail
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(PlanetSecondary.copy(alpha = 0.5f), Color.Transparent),
                center = Offset(pX, pY),
                radius = planetRadiusPx * 2.5f
            ),
            radius = planetRadiusPx * 2.5f,
            center = Offset(pX, pY)
        )
        // Planet body
        drawCircle(
            color = PlanetSecondary,
            radius = planetRadiusPx,
            center = Offset(pX, pY)
        )

        // Draw Direction pointer arrow
        val dirX = cos(phase).toFloat()
        val dirY = -sin(phase).toFloat()
        val arrowStartX = pX + planetRadiusPx * 1.5f * dirX
        val arrowStartY = pY + planetRadiusPx * 1.5f * dirY
        val arrowEndX = pX + planetRadiusPx * 3f * dirX
        val arrowEndY = pY + planetRadiusPx * 3f * dirY
        
        drawLine(
            color = PlanetSecondary.copy(alpha = 0.7f),
            start = Offset(arrowStartX, arrowStartY),
            end = Offset(arrowEndX, arrowEndY),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}

private fun DrawScope.drawOrbitEllipse(
    centerX: Float,
    centerY: Float,
    radiusX: Float,
    radiusY: Float,
    color: Color
) {
    val path = Path()
    val steps = 100
    for (i in 0..steps) {
        val angle = i * 2.0 * PI / steps
        val x = centerX + radiusX * sin(angle).toFloat()
        val y = centerY - radiusY * cos(angle).toFloat()
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 12f), 0f)
        )
    )
}

private fun DrawScope.drawPlanetDisk(pX: Float, pY: Float, planetRadiusPx: Float) {
    // Planet body with lit crescent atmosphere reflection
    drawCircle(
        color = PlanetSecondary.copy(alpha = 0.2f),
        radius = planetRadiusPx * 1.4f,
        center = Offset(pX, pY)
    )
    drawCircle(
        color = PlanetSecondary,
        radius = planetRadiusPx,
        center = Offset(pX, pY)
    )
}

private fun DrawScope.drawTargetCrosshairs(centerX: Float, centerY: Float, radius: Float) {
    val color = TextMuted.copy(alpha = 0.25f)
    val length = 12.dp.toPx()
    val gap = radius + 3.dp.toPx()
    
    // Top
    drawLine(color, Offset(centerX, centerY - gap), Offset(centerX, centerY - gap - length), strokeWidth = 1.dp.toPx())
    // Bottom
    drawLine(color, Offset(centerX, centerY + gap), Offset(centerX, centerY + gap + length), strokeWidth = 1.dp.toPx())
    // Left
    drawLine(color, Offset(centerX - gap, centerY), Offset(centerX - gap - length, centerY), strokeWidth = 1.dp.toPx())
    // Right
    drawLine(color, Offset(centerX + gap, centerY), Offset(centerX + gap + length, centerY), strokeWidth = 1.dp.toPx())
}

@Composable
fun LightCurvePlotCard(
    uiState: TransitUiState,
    textMeasurer: TextMeasurer,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIGHT CURVE (RELATIVE STELLAR FLUX)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                
                Text(
                    text = if (uiState.diagnostics.hasTransit) {
                        "Transit Event Active"
                    } else {
                        "No Mutual Geometrical Transit"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (uiState.diagnostics.hasTransit) PlanetSecondary else TextMuted
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                if (uiState.lightCurvePoints.isNotEmpty()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawLightCurveGraph(
                            curvePoints = uiState.lightCurvePoints,
                            currentPhase = uiState.currentPhaseRad,
                            hasTransit = uiState.diagnostics.hasTransit,
                            textMeasurer = textMeasurer
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PlanetSecondary)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawLightCurveGraph(
    curvePoints: List<Pair<Double, Double>>,
    currentPhase: Double,
    hasTransit: Boolean,
    textMeasurer: TextMeasurer
) {
    val width = size.width
    val height = size.height

    // Calculate margins
    val marginL = 55.dp.toPx()
    val marginR = 20.dp.toPx()
    val marginT = 15.dp.toPx()
    val marginB = 30.dp.toPx()

    val graphW = width - marginL - marginR
    val graphH = height - marginT - marginB

    // Draw background boundary
    drawRect(
        color = SpaceBackground,
        topLeft = Offset(marginL, marginT),
        size = Size(graphW, graphH)
    )
    drawRect(
        color = BorderColor,
        topLeft = Offset(marginL, marginT),
        size = Size(graphW, graphH),
        style = Stroke(width = 1.dp.toPx())
    )

    // Calculate vertical scaling based on the actual dip to make shallow dips fully readable.
    val minFlux = curvePoints.minOf { it.second }
    val maxFlux = 1.002 // slight breathing room above 1.0

    // Prevent divide-by-zero or flatlines
    val yMin = if (minFlux >= 1.0) 0.999 else minFlux - (1.0 - minFlux) * 0.15
    val yMax = maxFlux

    // Map phase coordinates (-1.0 to 1.0)
    val xMinPlot = -1.0
    val xMaxPlot = 1.0

    val getCanvasX = { phase: Double ->
        val fract = (phase - xMinPlot) / (xMaxPlot - xMinPlot)
        (marginL + fract * graphW).toFloat()
    }

    val getCanvasY = { flux: Double ->
        val fract = (flux - yMin) / (yMax - yMin)
        // Invert Y because top-left is (0,0) in Android drawing space
        (marginT + (1.0 - fract) * graphH).toFloat()
    }

    // Draw grid lines
    val yLevels = listOf(1.0, 1.0 - (1.0 - yMin)/2, yMin)
    val textStyle = TextStyle(
        color = TextMuted,
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace
    )

    yLevels.forEach { lvl ->
        val yVal = getCanvasY(lvl)
        drawLine(
            color = BorderColor.copy(alpha = 0.5f),
            start = Offset(marginL, yVal),
            end = Offset(marginL + graphW, yVal),
            strokeWidth = 1.dp.toPx()
        )
        
        // Label on left margin
        val formattedFlux = String.format("%.4f", lvl)
        val textLayout = textMeasurer.measure(
            text = AnnotatedString(formattedFlux),
            style = textStyle
        )
        drawText(
            textLayoutResult = textLayout,
            color = TextSecondary,
            topLeft = Offset(marginL - textLayout.size.width - 6.dp.toPx(), yVal - textLayout.size.height / 2f)
        )
    }

    // Draw X Axis marks (Phase: -1.0, -0.5, 0.0, 0.5, 1.0)
    val xMarkers = listOf(-1.0, -0.5, 0.0, 0.5, 1.0)
    xMarkers.forEach { phaseMarker ->
        val xVal = getCanvasX(phaseMarker)
        drawLine(
            color = BorderColor.copy(alpha = 0.5f),
            start = Offset(xVal, marginT),
            end = Offset(xVal, marginT + graphH),
            strokeWidth = 1.dp.toPx()
        )

        // Label below border
        val labelText = String.format("%.1f φ", phaseMarker)
        val xTextLayout = textMeasurer.measure(
            text = AnnotatedString(labelText),
            style = textStyle
        )
        drawText(
            textLayoutResult = xTextLayout,
            color = TextSecondary,
            topLeft = Offset(xVal - xTextLayout.size.width / 2f, marginT + graphH + 4.dp.toPx())
        )
    }

    // Draw baseline 1.flux limit
    val baselineY = getCanvasY(1.0)
    drawLine(
        color = BorderColor,
        start = Offset(marginL, baselineY),
        end = Offset(marginL + graphW, baselineY),
        strokeWidth = 1.5.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 5f), 0f)
    )

    // Plot transit points (the curve)
    val plotPath = Path()
    curvePoints.forEachIndexed { index, pair ->
        val xVal = getCanvasX(pair.first)
        val yVal = getCanvasY(pair.second)
        
        if (xVal >= marginL && xVal <= marginL + graphW) {
            if (index == 0) {
                plotPath.moveTo(xVal, yVal)
            } else {
                plotPath.lineTo(xVal, yVal)
            }
        }
    }

    drawPath(
        path = plotPath,
        color = PlanetSecondary,
        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
    )

    // Sweeping time-marker (indicates the current phase relative to simulation)
    val curX = getCanvasX(currentPhase)
    if (curX >= marginL && curX <= marginL + graphW) {
        // Find current flux value at the phase to draw a small intersecting node
        val closestPoint = curvePoints.minByOrNull { abs(it.first - currentPhase) }
        val curY = if (closestPoint != null) getCanvasY(closestPoint.second) else getCanvasY(1.0)

        // Sweeping indicator line
        drawLine(
            color = NebulaTertiary.copy(alpha = 0.7f),
            start = Offset(curX, marginT),
            end = Offset(curX, marginT + graphH),
            strokeWidth = 1.5.dp.toPx()
        )

        // Intersecting Node
        drawCircle(
            color = NebulaTertiary,
            radius = 5.dp.toPx(),
            center = Offset(curX, curY)
        )
        drawCircle(
            color = Color.White,
            radius = 2.dp.toPx(),
            center = Offset(curX, curY)
        )
    }
}

@Composable
fun ScienceMetricsGrid(
    diagnostics: TransitScientificResults,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "SCIENTIFIC LABORATORY METRICS",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )

        // Double row of parameters
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricCard(
                    title = "Semi-major Axis (a)",
                    value = String.format("%.3f AU", diagnostics.semiMajorAxisAU),
                    subtitle = String.format("≈ %.1f R*", diagnostics.semiMajorAxisStarRadii),
                    icon = Icons.Default.LinearScale,
                    modifier = Modifier.weight(1f)
                )
                
                MetricCard(
                    title = "Transit Depth",
                    value = if (diagnostics.transitDepthPercent < 0.01) {
                        String.format("%.0f ppm", diagnostics.transitDepthPpm)
                    } else {
                        String.format("%.4f %%", diagnostics.transitDepthPercent)
                    },
                    subtitle = String.format("Relative area blockage (k²)", diagnostics.ratioRpRs),
                    icon = Icons.Default.QueryStats,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricCard(
                    title = "Transit Duration",
                    value = if (diagnostics.hasTransit) {
                        String.format("%.2f Hours", diagnostics.transitDurationHours)
                    } else {
                        "0.00 Hours"
                    },
                    subtitle = "Time in stellar occultation",
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(1f)
                )
                
                MetricCard(
                    title = "Impact Parameter (b)",
                    value = String.format("%.3f", diagnostics.impactParameter),
                    subtitle = if (!diagnostics.hasTransit) {
                        "Missed stellar disk (b > 1+k)"
                    } else if (diagnostics.impactParameter > (1.0 - diagnostics.ratioRpRs)) {
                        "Partial graze transit"
                    } else {
                        "Centered stellar cross"
                    },
                    icon = Icons.Default.GpsFixed,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricCard(
                    title = "Geometric Probability",
                    value = String.format("%.2f %%", diagnostics.transitProbabilityPercent),
                    subtitle = "Likelihood of aligned line-of-sight",
                    icon = Icons.Default.TrackChanges,
                    modifier = Modifier.weight(1f)
                )
                
                MetricCard(
                    title = "Transit Status",
                    value = if (diagnostics.hasTransit) "ALIGNED" else "INERT",
                    subtitle = if (diagnostics.hasTransit) "Observatories recording dips" else "Orbit tilted outside sightline",
                    icon = Icons.Default.FactCheck,
                    iconColor = if (diagnostics.hasTransit) PlanetSecondary else TextMuted,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconColor: Color = PlanetSecondary
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        border = BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(CosmicSurfaceVariant, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Black,
                    color = OnCosmicSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 9.sp,
                    lineHeight = 11.sp
                )
            }
        }
    }
}

@Composable
fun ControlSlidersCard(
    uiState: TransitUiState,
    viewModel: TransitViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "PHYSICAL SIMULATION CONTROLS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            HorizontalDivider(color = BorderColor)

            // Star Radius
            AstronomicalSlider(
                title = "Host Star Radius (Rs)",
                value = uiState.starRadiusSolar,
                range = 0.1..2.5,
                unit = "R☉",
                ticks = 24,
                onValueChange = { viewModel.updateStarRadius(it) }
            )

            // Star Mass
            AstronomicalSlider(
                title = "Host Star Mass (Ms)",
                value = uiState.starMassSolar,
                range = 0.1..3.0,
                unit = "M☉",
                ticks = 29,
                onValueChange = { viewModel.updateStarMass(it) }
            )

            // Planet Radius
            AstronomicalSlider(
                title = "Exoplanet Radius (Rp)",
                value = uiState.planetRadiusJupiter,
                range = 0.05..2.5,
                unit = "R_Jup",
                ticks = 25,
                secondaryUnitAndVal = String.format("≈ %.1f R_Earth", uiState.planetRadiusJupiter * TransitPhysics.JUPITER_RAD_IN_EARTH_RAD),
                onValueChange = { viewModel.updatePlanetRadius(it) }
            )

            // Orbital Period
            AstronomicalSlider(
                title = "Orbital Period (P)",
                value = uiState.planetPeriodDays,
                range = 0.5..40.0,
                unit = "Days",
                ticks = 40,
                onValueChange = { viewModel.updatePlanetPeriod(it) }
            )

            // Orbital Inclination
            AstronomicalSlider(
                title = "Orbit Inclination (i)",
                value = uiState.planetInclinationDegrees,
                range = 80.0..90.0,
                unit = "°",
                ticks = 100,
                secondaryUnitAndVal = "90° = Perfectly Centered Line-of-Sight",
                onValueChange = { viewModel.updatePlanetInclination(it) }
            )

            // Limb Darkening Coef
            AstronomicalSlider(
                title = "Stellar Limb Darkening Coef (u)",
                value = uiState.planetLimbDarkeningCoef,
                range = 0.0..1.0,
                unit = "",
                ticks = 20,
                secondaryUnitAndVal = "Sets edge opacity (flat box to u-curve profiles)",
                onValueChange = { viewModel.updateLimbDarkening(it) }
            )
            
            // Manual advance control for research
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Manual Orbital Adjuster:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.manualAdvancePhase(-0.05) },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Text(text = "← Rewind", style = MaterialTheme.typography.labelSmall, color = PlanetSecondary)
                    }
                    Button(
                        onClick = { viewModel.manualAdvancePhase(0.05) },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Text(text = "Advance →", style = MaterialTheme.typography.labelSmall, color = PlanetSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun AstronomicalSlider(
    title: String,
    value: Double,
    range: ClosedRange<Double>,
    unit: String,
    ticks: Int,
    secondaryUnitAndVal: String? = null,
    onValueChange: (Double) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = OnCosmicSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = String.format("%.3f %s", value, unit),
                style = MaterialTheme.typography.bodySmall,
                color = PlanetSecondary,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
        
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toDouble()) },
            valueRange = range.start.toFloat()..range.endInclusive.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = PlanetSecondary,
                activeTrackColor = PlanetSecondary,
                inactiveTrackColor = BorderColor,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            modifier = Modifier
                .height(28.dp)
                .testTag("slider_${title.replace(" ", "_").lowercase()}")
        )
        
        if (secondaryUnitAndVal != null) {
            Text(
                text = secondaryUnitAndVal,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 9.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

// Custom extension to avoid fillMaxWidth stretching awkwardly on expansive displays
@Composable
fun Modifier.fillCardWidth(): Modifier {
    return this.fillMaxWidth().widthIn(max = 680.dp)
}
