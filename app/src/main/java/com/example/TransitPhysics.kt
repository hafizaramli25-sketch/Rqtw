package com.example

import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.PI

/**
 * Host Star physical parameters
 */
data class HostStar(
    val name: String,
    val radiusSolar: Double, // in R_sun
    val massSolar: Double,   // in M_sun
)

/**
 * Exoplanet physical parameters
 */
data class Exoplanet(
    val name: String,
    val radiusJupiter: Double,  // in R_jupiter
    val periodDays: Double,      // in days
    val inclinationDegrees: Double, // orbit tilt in degrees
    val limbDarkeningCoef: Double,  // u coefficient for linear limb darkening (0.0 to 1.0)
)

/**
 * Model of a complete Exoplanetary System
 */
data class ExoplanetSystem(
    val id: String,
    val name: String,
    val star: HostStar,
    val planet: Exoplanet,
    val description: String
)

/**
 * Results of scientific and mathematical calculations
 */
data class TransitScientificResults(
    val semiMajorAxisAU: Double,     // semi-major axis in AU
    val semiMajorAxisStarRadii: Double, // semi-major axis in stellar radii
    val transitDepthPercent: Double,     // dip depth in %
    val transitDepthPpm: Double,         // dip depth in parts-per-million (ppm)
    val transitDurationHours: Double,     // transit duration in hours
    val transitProbabilityPercent: Double, // geometric probability of transit in %
    val impactParameter: Double,           // impact parameter b
    val ratioRpRs: Double,                 // Rp / Rs
    val hasTransit: Boolean                // whether a transit actually occurs
)

object TransitPhysics {

    // Conversions
    const val AU_IN_SOLAR_RADII = 215.032
    const val JUPITER_RAD_IN_SOLAR_RAD = 0.10049
    const val EARTH_RAD_IN_JUPITER_RAD = 0.0911
    const val JUPITER_RAD_IN_EARTH_RAD = 10.973

    // Presets
    val PRESETS = listOf(
        ExoplanetSystem(
            id = "hd209458b",
            name = "HD 209458 b (Osiris)",
            star = HostStar("HD 209458", radiusSolar = 1.20, massSolar = 1.15),
            planet = Exoplanet("Osiris", radiusJupiter = 1.38, periodDays = 3.525, inclinationDegrees = 86.1, limbDarkeningCoef = 0.45),
            description = "The first transiting exoplanet discovered (1999). A 'Hot Jupiter' with a significant transit dip, orbiting very close to its sun-like star."
        ),
        ExoplanetSystem(
            id = "trappist1b",
            name = "TRAPPIST-1 b",
            star = HostStar("TRAPPIST-1", radiusSolar = 0.12, massSolar = 0.09),
            planet = Exoplanet("TRAPPIST-1 b", radiusJupiter = 0.103, periodDays = 1.51, inclinationDegrees = 89.6, limbDarkeningCoef = 0.65),
            description = "An Earth-sized world orbiting an ultra-cool red dwarf. Because the star is tiny, the transit signal is exceptionally pronounced for a small planet."
        ),
        ExoplanetSystem(
            id = "kepler186f",
            name = "Kepler-186 f",
            star = HostStar("Kepler-186", radiusSolar = 0.52, massSolar = 0.54),
            planet = Exoplanet("Kepler-186 f", radiusJupiter = 0.107, periodDays = 129.9, inclinationDegrees = 89.9, limbDarkeningCoef = 0.55),
            description = "The first Earth-sized planet found in the habitable zone of another star. It yields a shallow transit and has a long, cool orbit."
        ),
        ExoplanetSystem(
            id = "wasp12b",
            name = "WASP-12 b",
            star = HostStar("WASP-12", radiusSolar = 1.57, massSolar = 1.35),
            planet = Exoplanet("WASP-12 b", radiusJupiter = 1.79, periodDays = 1.09, inclinationDegrees = 86.0, limbDarkeningCoef = 0.35),
            description = "An extreme, egg-shaped Hot Jupiter. It is actively being torn apart by tidal forces. Huge size leads to a massive, easily observed dip."
        ),
        ExoplanetSystem(
            id = "kepler452b",
            name = "Kepler-452 b",
            star = HostStar("Kepler-452", radiusSolar = 1.11, massSolar = 1.04),
            planet = Exoplanet("Kepler-452 b", radiusJupiter = 0.150, periodDays = 384.8, inclinationDegrees = 89.8, limbDarkeningCoef = 0.58),
            description = "Often called 'Earth 2.0', orbiting a solar twin. High orbital distance makes transit events rare and extremely hard to detect (ppm signals)."
        )
    )

    /**
     * Compute all scientific and orbital outcomes of a Star + Planet combination
     */
    fun calculateDiagnostics(star: HostStar, planet: Exoplanet): TransitScientificResults {
        // 1. Kepler's Third Law: a^3 = M_star * (Period_days / 365.25)^2
        val periodYears = planet.periodDays / 365.25
        val aCubed = star.massSolar * (periodYears * periodYears)
        val semiMajorAxisAU = Math.pow(aCubed, 1.0 / 3.0)

        // Convert to stellar radii: a / R_star
        val semiMajorAxisSolarRadii = semiMajorAxisAU * AU_IN_SOLAR_RADII
        val semiMajorAxisStarRadii = semiMajorAxisSolarRadii / star.radiusSolar

        // Planetary radius in solar units
        val rpSolar = planet.radiusJupiter * JUPITER_RAD_IN_SOLAR_RAD
        val ratioRpRs = rpSolar / star.radiusSolar

        // Transit depth: (Rp / Rs)^2
        val transitDepthRaw = ratioRpRs * ratioRpRs
        val transitDepthPercent = transitDepthRaw * 100.0
        val transitDepthPpm = transitDepthRaw * 1_000_000.0

        // Impact parameter b = (a / R_star) * cos(i)
        val inclinationRad = planet.inclinationDegrees * PI / 180.0
        val impactParameter = semiMajorAxisStarRadii * cos(inclinationRad)

        // Transit occurs if b < 1 + Rp/Rs
        val hasTransit = impactParameter < (1.0 + ratioRpRs)

        // Transit probability = (R_star + R_planet) / a
        val probRaw = (star.radiusSolar + rpSolar) / semiMajorAxisSolarRadii
        val transitProbabilityPercent = (probRaw * 100.0).coerceIn(0.0, 100.0)

        // Transit duration in hours (exact geometric formula)
        val transitDurationHours = if (hasTransit && semiMajorAxisStarRadii > 1.0) {
            val k = ratioRpRs
            val b = impactParameter
            val arg = sqrt(((1.0 + k) * (1.0 + k) - b * b).coerceAtLeast(0.0)) / (semiMajorAxisStarRadii * sin(inclinationRad))
            if (arg in -1.0..1.0) {
                val durationFraction = asin(arg) / PI
                durationFraction * planet.periodDays * 24.0
            } else {
                0.0
            }
        } else {
            0.0
        }

        return TransitScientificResults(
            semiMajorAxisAU = semiMajorAxisAU,
            semiMajorAxisStarRadii = semiMajorAxisStarRadii,
            transitDepthPercent = transitDepthPercent,
            transitDepthPpm = transitDepthPpm,
            transitDurationHours = transitDurationHours,
            transitProbabilityPercent = transitProbabilityPercent,
            impactParameter = impactParameter,
            ratioRpRs = ratioRpRs,
            hasTransit = hasTransit
        )
    }

    /**
     * Compute circular physical positions of planet relative to Star
     * for orbital animations.
     * x, y, z where Star is at (0,0,0)
     * We scale the coordinates so the Star has radius 1.0
     */
    fun getPlanetSkyPosition(
        orbitalPhaseRad: Double, // 0 to 2*PI, where 0 is centered transit (mid-eclipse)
        semiMajorAxisStarRadii: Double,
        inclinationDegrees: Double
    ): Triple<Double, Double, Double> {
        val iRad = inclinationDegrees * PI / 180.0
        
        // Circular orbit
        // To make orbital phase 0 coincide with a transit in front (~earth line of sight at +Z):
        // At phase 0: x = 0, y = -a * cos(i) (projected vertical offset), z = -a * sin(i) (between star and us)
        val x = semiMajorAxisStarRadii * sin(orbitalPhaseRad)
        val y = -semiMajorAxisStarRadii * cos(orbitalPhaseRad) * cos(iRad)
        val z = -semiMajorAxisStarRadii * cos(orbitalPhaseRad) * sin(iRad)

        return Triple(x, y, z)
    }

    /**
     * Calculate Normalized Stellar Flux F(t) using geometric circle-circle intersection
     * and linear limb darkening.
     */
    fun calculateFluxAtPhase(
        orbitalPhaseRad: Double,
        semiMajorAxisStarRadii: Double,
        inclinationDegrees: Double,
        ratioRpRs: Double,
        limbDarkeningCoef: Double
    ): Double {
        val iRad = inclinationDegrees * PI / 180.0
        
        // Calculate projected distance on sky plane delta (in units of R_star)
        val x = semiMajorAxisStarRadii * sin(orbitalPhaseRad)
        val y = -semiMajorAxisStarRadii * cos(orbitalPhaseRad) * cos(iRad)
        val z = -semiMajorAxisStarRadii * cos(orbitalPhaseRad) * sin(iRad)
        
        // Planet is only transiting if it is in front of the star (z is NEGATIVE in our convention)
        if (z > 0) {
            return 1.0 // Planet is behind the star (secondary eclipse, flat for simple model)
        }

        val delta = sqrt(x * x + y * y) // Separation in stellar radii
        val k = ratioRpRs

        // No overlap
        if (delta >= (1.0 + k)) {
            return 1.0
        }

        // Full transit (planet completely within stellar disk)
        if (delta <= (1.0 - k)) {
            // Apply linear limb darkening formula
            // mu is the cosine of the angle of stellar radius to line of sight
            val mu = sqrt((1.0 - delta * delta).coerceAtLeast(0.0))
            val intensityRatio = (1.0 - limbDarkeningCoef * (1.0 - mu)) / (1.0 - limbDarkeningCoef / 3.0)
            val dip = k * k * intensityRatio
            return (1.0 - dip).coerceIn(0.0, 1.0)
        }

        // Ingress or Egress: partial overlap
        // Overlap area of two circles with radii 1 and k, separated by delta
        val overlapArea = calculateOverlapArea(1.0, k, delta)
        val overlapFraction = overlapArea / PI

        // Simple limb darkening scaling for the overlapping fraction
        val mu = sqrt((1.0 - delta * delta).coerceAtLeast(0.0))
        val intensityRatio = (1.0 - limbDarkeningCoef * (1.0 - mu)) / (1.0 - limbDarkeningCoef / 3.0)
        val dip = overlapFraction * intensityRatio

        return (1.0 - dip).coerceIn(0.0, 1.0)
    }

    /**
     * Geometric overlapping area of two intersecting circles of radius R1, R2 separated by d
     */
    private fun calculateOverlapArea(r1: Double, r2: Double, d: Double): Double {
        if (d >= r1 + r2) return 0.0
        if (d <= Math.abs(r1 - r2)) return PI * Math.min(r1, r2) * Math.min(r1, r2)

        val d1 = (r1 * r1 - r2 * r2 + d * d) / (2.0 * d)
        val d2 = (r2 * r2 - r1 * r1 + d * d) / (2.0 * d)

        val term1 = r1 * r1 * acos((d1 / r1).coerceIn(-1.0, 1.0)) - d1 * sqrt((r1 * r1 - d1 * d1).coerceAtLeast(0.0))
        val term2 = r2 * r2 * acos((d2 / r2).coerceIn(-1.0, 1.0)) - d2 * sqrt((r2 * r2 - d2 * d2).coerceAtLeast(0.0))

        return term1 + term2
    }
}
