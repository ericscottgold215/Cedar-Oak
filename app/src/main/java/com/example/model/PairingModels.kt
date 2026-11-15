package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.WrapperClaro
import com.example.ui.theme.WrapperColorado
import com.example.ui.theme.WrapperColoradoClaro
import com.example.ui.theme.WrapperMaduro
import com.example.ui.theme.WrapperOscuro

data class FlavorAxes(
    val woodCedar: Float,
    val cocoaRoast: Float,
    val spicePepper: Float,
    val caramelSweet: Float,
    val earthSmoke: Float,
    val creamVanilla: Float,
    val nutMalt: Float,
    val fruitZest: Float,
) {
    fun asLabeledList(): List<Pair<String, Float>> = listOf(
        "Cedar" to woodCedar,
        "Cocoa" to cocoaRoast,
        "Spice" to spicePepper,
        "Caramel" to caramelSweet,
        "Earth" to earthSmoke,
        "Cream" to creamVanilla,
        "Nut/Malt" to nutMalt,
        "Fruit" to fruitZest,
    )

    fun topAxes(count: Int = 3): List<String> =
        asLabeledList()
            .sortedByDescending { it.second }
            .take(count)
            .map { it.first }
}

enum class StrengthLevel(val label: String, val dots: Int) {
    MILD("Mild", 2),
    MEDIUM("Medium", 3),
    MEDIUM_FULL("Medium-Full", 4),
    FULL("Full", 5),
}

enum class WrapperShade(val label: String, val swatchColor: Color) {
    CLARO("Claro", WrapperClaro),
    COLORADO_CLARO("Colorado Claro", WrapperColoradoClaro),
    COLORADO("Colorado", WrapperColorado),
    MADURO("Maduro", WrapperMaduro),
    OSCURO("Oscuro", WrapperOscuro),
}

enum class BeverageCategory(val label: String, val isAlcoholic: Boolean) {
    WHISKEY("Whiskey & Scotch", true),
    RUM("Rum & Cachaça", true),
    BRANDY("Cognac & Brandy", true),
    COCKTAILS("Classic Cocktails", true),
    WINE("Wine, Port & Sherry", true),
    BEER("Craft Beer & Cider", true),
    AGAVE("Tequila & Mezcal", true),
    COFFEE("Coffee & Espresso", false),
    TEA("Artisanal Tea", false),
    CRAFT_SODA("Craft Sodas", false),
    CACAO("Cacao & Warm", false),
    ZERO_PROOF_ELIXIRS("Zero-Proof Cocktails", false),
}

enum class BeverageFilter(val label: String) {
    ALL("All"),
    ALCOHOLIC("Alcoholic"),
    NON_ALCOHOLIC("Zero-Proof"),
}

enum class BodyLevel(val label: String, val dots: Int) {
    LIGHT("Light", 2),
    MEDIUM("Medium", 3),
    FULL("Full", 5),
}

data class Cigar(
    val id: String,
    val name: String,
    val brand: String,
    val origin: String,
    val wrapper: String,
    val wrapperShade: WrapperShade,
    val vitola: String,
    val dimensions: String,
    val strength: StrengthLevel,
    val smokeTimeMinutes: Int,
    val flavorAxes: FlavorAxes,
    val tastingNotes: List<String>,
    val thirdProgression: Triple<String, String, String>,
    val summary: String,
)

data class Beverage(
    val id: String,
    val name: String,
    val subtitle: String,
    val category: BeverageCategory,
    val abvLabel: String,
    val servingTemp: String,
    val glassware: String,
    val body: BodyLevel,
    val flavorAxes: FlavorAxes,
    val tastingNotes: List<String>,
    val palateBreakdown: Triple<String, String, String>,
    val summary: String,
) {
    val isAlcoholic: Boolean get() = category.isAlcoholic
}

enum class PairingStrategy(
    val label: String,
    val shortDescription: String,
) {
    BALANCED(
        label = "All Styles",
        shortDescription = "Balanced sommelier scoring across harmony, body weight, wrapper affinity, and pacing.",
    ),
    COMPLEMENTARY(
        label = "Complementary",
        shortDescription = "Flavor mirrors matching shared cocoa, cedar, honey, or smoky oak peaks.",
    ),
    CONTRASTING(
        label = "Contrasting",
        shortDescription = "Sweetness, cream, or bright spice countering pepper, earth, and Maduro intensity.",
    ),
    PALATE_CLEANSER(
        label = "Palate Reset",
        shortDescription = "Crisp carbonation, citrus, ginger, or brisk tea to lift oils between thirds.",
    ),
}

data class PairingBreakdown(
    val flavorHarmony: Int,
    val bodyBalance: Int,
    val wrapperAffinity: Int,
    val sessionPacing: Int,
    val counterpointNotes: List<String>,
    val bestThirdWindow: String,
    val sommelierRule: String,
)

data class CuratedPairing(
    val cigarId: String,
    val beverageId: String,
    val matchScore: Int,
    val pairingStyle: String,
    val whyItWorks: String,
    val servingTip: String,
) {
    val pairingId: String get() = buildPairingId(cigarId, beverageId)
}

data class PairingSuggestion(
    val pairingId: String,
    val cigar: Cigar,
    val beverage: Beverage,
    val matchScore: Int,
    val pairingStyle: String,
    val whyItWorks: String,
    val servingTip: String,
    val isCurated: Boolean,
    val bridgeNotes: List<String>,
    val breakdown: PairingBreakdown? = null,
    val averageRating: Double? = null,
    val reviewCount: Int = 0,
)

data class PairingRatingSummary(
    val pairingId: String,
    val averageRating: Double,
    val reviewCount: Int,
)

data class PairingReview(
    val id: Long = 0L,
    val pairingId: String,
    val cigarId: String,
    val beverageId: String,
    val rating: Int,
    val reviewText: String,
    val reviewerName: String,
    val isUserOwn: Boolean,
    val createdAt: Long,
)

fun buildPairingId(cigarId: String, beverageId: String): String = "${cigarId}__${beverageId}"
