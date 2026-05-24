package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI

data class TransitUiState(
    // Selection state
    val selectedPresetId: String = "hd209458b",
    
    // Sliders
    val starName: String = "HD 209458",
    val starRadiusSolar: Double = 1.20,
    val starMassSolar: Double = 1.15,
    
    val planetName: String = "Osiris",
    val planetRadiusJupiter: Double = 1.38,
    val planetPeriodDays: Double = 3.525,
    val planetInclinationDegrees: Double = 86.1,
    val planetLimbDarkeningCoef: Double = 0.45,
    
    // Simulation Control
    val currentPhaseRad: Double = 0.0, // -PI to PI
    val isSimulationRunning: Boolean = true,
    val simulationSpeed: Double = 1.0, // multiplier
    val showTopDownView: Boolean = true, // assist visual understanding
    
    // Calculations
    val diagnostics: TransitScientificResults = TransitPhysics.calculateDiagnostics(
        HostStar("HD 209458", 1.20, 1.15),
        Exoplanet("Osiris", 1.38, 3.525, 86.1, 0.45)
    ),
    
    // Plot cache (200 points from -0.8 to 0.8 radians, centered around transit 0.0)
    val lightCurvePoints: List<Pair<Double, Double>> = emptyList()
)

class TransitViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TransitUiState())
    val uiState: StateFlow<TransitUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null

    init {
        // Load initial preset
        loadPreset(TransitPhysics.PRESETS[0])
        startSimulationTicker()
    }

    fun selectPresetById(id: String) {
        val preset = TransitPhysics.PRESETS.find { it.id == id }
        if (preset != null) {
            loadPreset(preset)
        }
    }

    private fun loadPreset(preset: ExoplanetSystem) {
        _uiState.update { state ->
            state.copy(
                selectedPresetId = preset.id,
                starName = preset.star.name,
                starRadiusSolar = preset.star.radiusSolar,
                starMassSolar = preset.star.massSolar,
                planetName = preset.planet.name,
                planetRadiusJupiter = preset.planet.radiusJupiter,
                planetPeriodDays = preset.planet.periodDays,
                planetInclinationDegrees = preset.planet.inclinationDegrees,
                planetLimbDarkeningCoef = preset.planet.limbDarkeningCoef,
                currentPhaseRad = 0.0 // reset orbital position
            )
        }
        recalculate()
    }

    // Interactive slider updates
    fun updateStarRadius(radius: Double) {
        _uiState.update { it.copy(starRadiusSolar = radius, selectedPresetId = "custom") }
        recalculate()
    }

    fun updateStarMass(mass: Double) {
        _uiState.update { it.copy(starMassSolar = mass, selectedPresetId = "custom") }
        recalculate()
    }

    fun updatePlanetRadius(radius: Double) {
        _uiState.update { it.copy(planetRadiusJupiter = radius, selectedPresetId = "custom") }
        recalculate()
    }

    fun updatePlanetPeriod(period: Double) {
        _uiState.update { it.copy(planetPeriodDays = period, selectedPresetId = "custom") }
        recalculate()
    }

    fun updatePlanetInclination(inclination: Double) {
        _uiState.update { it.copy(planetInclinationDegrees = inclination, selectedPresetId = "custom") }
        recalculate()
    }

    fun updateLimbDarkening(coef: Double) {
        _uiState.update { it.copy(planetLimbDarkeningCoef = coef, selectedPresetId = "custom") }
        recalculate()
    }

    fun toggleSimulation() {
        _uiState.update { it.copy(isSimulationRunning = !it.isSimulationRunning) }
    }

    fun toggleTopDownView() {
        _uiState.update { it.copy(showTopDownView = !it.showTopDownView) }
    }

    fun setSimulationSpeed(speed: Double) {
        _uiState.update { it.copy(simulationSpeed = speed) }
    }

    fun manualAdvancePhase(deltaRad: Double) {
        _uiState.update { state ->
            var newPhase = state.currentPhaseRad + deltaRad
            if (newPhase > PI) newPhase -= 2.0 * PI
            if (newPhase < -PI) newPhase += 2.0 * PI
            state.copy(currentPhaseRad = newPhase)
        }
    }

    /**
     * Compute diagnostics and cache the mathematical curve
     */
    private fun recalculate() {
        _uiState.update { state ->
            val star = HostStar(state.starName, state.starRadiusSolar, state.starMassSolar)
            val planet = Exoplanet(
                state.planetName,
                state.planetRadiusJupiter,
                state.planetPeriodDays,
                state.planetInclinationDegrees,
                state.planetLimbDarkeningCoef
            )
            
            val diags = TransitPhysics.calculateDiagnostics(star, planet)
            val curve = precalculateLightCurve(diags, planet.inclinationDegrees, planet.limbDarkeningCoef)
            
            state.copy(
                diagnostics = diags,
                lightCurvePoints = curve
            )
        }
    }

    /**
     * Generates standard high-density points around the transit zone.
     * We map the light curve in phase range [-1.0, 1.0] radians which focuses beautifully
     * on the transit event itself.
     */
    private fun precalculateLightCurve(
        diags: TransitScientificResults,
        inclinationDegrees: Double,
        limbDarkeningCoef: Double
    ): List<Pair<Double, Double>> {
        val points = mutableListOf<Pair<Double, Double>>()
        val startPhase = -1.0
        val endPhase = 1.0
        val pointsCount = 200
        val step = (endPhase - startPhase) / pointsCount
        
        for (idx in 0..pointsCount) {
            val phase = startPhase + idx * step
            val flux = TransitPhysics.calculateFluxAtPhase(
                orbitalPhaseRad = phase,
                semiMajorAxisStarRadii = diags.semiMajorAxisStarRadii,
                inclinationDegrees = inclinationDegrees,
                ratioRpRs = diags.ratioRpRs,
                limbDarkeningCoef = limbDarkeningCoef
            )
            points.add(Pair(phase, flux))
        }
        return points
    }

    /**
     * Corrected actual precalculation routine
     */
    init {
        recalculate()
    }

    private fun startSimulationTicker() {
        tickerJob = viewModelScope.launch {
            // dt = 16ms for roughly 60 fps matching standard screen refresh
            val intervalMs = 16L
            while (isActive) {
                if (_uiState.value.isSimulationRunning) {
                    _uiState.update { state ->
                        // Advance orbital phase: 2*PI is one full orbit.
                        // Speed is scaled down so that a 1.0 multiplier takes about 6 seconds for a full orbit
                        // deltaPhase = (2 * PI) * (dt_seconds / duration_of_orbit_seconds)
                        val dtSec = intervalMs / 1000.0
                        val orbitDurationSec = 6.0 / state.simulationSpeed
                        val dPhase = (2.0 * PI) * (dtSec / orbitDurationSec)
                        var newPhase = state.currentPhaseRad + dPhase
                        if (newPhase > PI) {
                            newPhase -= 2.0 * PI
                        }
                        state.copy(currentPhaseRad = newPhase)
                    }
                }
                delay(intervalMs)
            }
        }
    }

    override fun onCleared() {
        tickerJob?.cancel()
        super.onCleared()
    }
}
