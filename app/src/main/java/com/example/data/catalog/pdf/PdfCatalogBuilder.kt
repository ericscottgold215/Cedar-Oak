package com.example.data.catalog.pdf

import com.example.model.Cigar
import com.example.model.CigarReleaseHistory
import com.example.model.FlavorAxes
import com.example.model.StrengthLevel
import com.example.model.WrapperShade

data class PdfCigarEntry(
    val id: String,
    val name: String,
    val brand: String,
    val origin: String,
    val wrapper: String,
    val bodyTier: String,
    val vitola: String,
    val dimensions: String,
    val smokeTimeMinutes: Int,
    val notes: List<String>,
    val summary: String,
    val launchYear: String,
    val blenderAndFactory: String,
    val productionLocation: String,
    val storyAndLegacy: String,
    val shadeOverride: WrapperShade? = null,
) {
    fun toCigar(): Cigar {
        val shade = shadeOverride ?: inferWrapperShade(wrapper)
        val strength = inferStrength(bodyTier, wrapper, name)
        val axes = inferFlavorAxes(notes, wrapper, shade, bodyTier, summary)
        val thirdProgression = buildThirdProgression(notes, wrapper, strength, bodyTier)
        return Cigar(
            id = id,
            name = name,
            brand = brand,
            origin = origin,
            wrapper = wrapper,
            wrapperShade = shade,
            vitola = vitola,
            dimensions = dimensions,
            strength = strength,
            smokeTimeMinutes = smokeTimeMinutes,
            flavorAxes = axes,
            tastingNotes = notes,
            thirdProgression = thirdProgression,
            summary = summary,
        )
    }

    fun toReleaseHistory(): CigarReleaseHistory = CigarReleaseHistory(
        cigarId = id,
        launchYear = launchYear,
        blenderAndFactory = blenderAndFactory,
        productionLocation = productionLocation,
        storyAndLegacy = storyAndLegacy,
    )
}

private fun inferWrapperShade(wrapper: String): WrapperShade {
    val w = wrapper.lowercase()
    return when {
        "oscuro" in w -> WrapperShade.OSCURO
        "broadleaf" in w && ("mad" in w || "ct broadleaf" in w || "pa broadleaf" in w || "honduran broadleaf" in w) -> WrapperShade.OSCURO
        "green river" in w -> WrapperShade.OSCURO
        "maduro" in w || "mad." in w || "mata fina" in w || "arapiraca" in w || "san andrés" in w || "san andres" in w || "kentucky" in w -> WrapperShade.MADURO
        "candela" in w || "ct shade" in w || "ecu. ct" in w || "ct/" in w || "u.s. ct" in w || "deflorada" in w || "htl" in w -> {
            if ("broadleaf" in w || "habano" in w || "corojo" in w) WrapperShade.COLORADO_CLARO else WrapperShade.CLARO
        }
        "cameroon" in w || "sumatra" in w && "oscuro" !in w -> WrapperShade.COLORADO_CLARO
        "sun grown ct" in w -> WrapperShade.COLORADO_CLARO
        "habano" in w || "corojo" in w || "criollo" in w || "rosado" in w || "colorado" in w || "negrito" in w || "yamasá" in w || "hybrid" in w || "sun grown" in w || "olanch" in w || "jamastran" in w || "jalapa" in w || "esteli" in w || "indonesian" in w || "tradic" in w -> WrapperShade.COLORADO
        else -> WrapperShade.COLORADO_CLARO
    }
}

private fun inferStrength(bodyTier: String, wrapper: String, name: String): StrengthLevel {
    val b = bodyTier.uppercase().trim()
    val w = wrapper.lowercase()
    val n = name.lowercase()
    return when {
        b.startsWith("FULL") -> StrengthLevel.FULL
        b == "MED-FULL" || b == "MED-PLUS" -> StrengthLevel.MEDIUM_FULL
        b == "MEDIUM" -> StrengthLevel.MEDIUM
        b == "MILD-MED" -> {
            if ("maduro" in w || "broadleaf" in w || "san andrés" in w || "san andres" in w || "corojo" in w) {
                StrengthLevel.MEDIUM
            } else {
                StrengthLevel.MILD
            }
        }
        b == "MILD" || b == "SWEET" || b == "SWEET TIP" || b == "FLAVORED" -> {
            if ("maduro" in w || "san andrés" in w || "san andres" in w) StrengthLevel.MEDIUM else StrengthLevel.MILD
        }
        b == "INFUSED" || b == "AROMATIC" -> {
            when {
                "opulence" in n || "leather rose" in n || "midnight" in n -> StrengthLevel.MEDIUM_FULL
                "c-notes" in n || "latte" in n || "nuance" in n || "sunrise" in n || "cherry" in n -> StrengthLevel.MILD
                else -> StrengthLevel.MEDIUM
            }
        }
        else -> StrengthLevel.MEDIUM
    }
}

private fun inferFlavorAxes(
    notes: List<String>,
    wrapper: String,
    shade: WrapperShade,
    bodyTier: String,
    summary: String,
): FlavorAxes {
    val text = (notes.joinToString(" ") + " " + wrapper + " " + summary).lowercase()
    val b = bodyTier.uppercase()

    var cedar = when (shade) {
        WrapperShade.CLARO -> 0.64f
        WrapperShade.COLORADO_CLARO -> 0.72f
        WrapperShade.COLORADO -> 0.74f
        WrapperShade.MADURO -> 0.66f
        WrapperShade.OSCURO -> 0.64f
    }
    var cocoa = when (shade) {
        WrapperShade.CLARO -> 0.28f
        WrapperShade.COLORADO_CLARO -> 0.46f
        WrapperShade.COLORADO -> 0.64f
        WrapperShade.MADURO -> 0.82f
        WrapperShade.OSCURO -> 0.88f
    }
    var spice = when (b) {
        "MILD", "SWEET", "SWEET TIP", "FLAVORED" -> 0.24f
        "MILD-MED" -> 0.36f
        "MEDIUM" -> 0.58f
        "MED-PLUS", "MED-FULL" -> 0.74f
        "FULL" -> 0.86f
        "FULL+" -> 0.94f
        else -> 0.48f
    }
    var caramel = if (shade >= WrapperShade.MADURO) 0.66f else 0.56f
    var earth = when (shade) {
        WrapperShade.CLARO -> 0.28f
        WrapperShade.COLORADO_CLARO -> 0.44f
        WrapperShade.COLORADO -> 0.62f
        WrapperShade.MADURO -> 0.70f
        WrapperShade.OSCURO -> 0.78f
    }
    var cream = when (shade) {
        WrapperShade.CLARO -> 0.84f
        WrapperShade.COLORADO_CLARO -> 0.64f
        WrapperShade.COLORADO -> 0.50f
        WrapperShade.MADURO -> 0.46f
        WrapperShade.OSCURO -> 0.40f
    }
    var nut = when (shade) {
        WrapperShade.CLARO -> 0.78f
        WrapperShade.COLORADO_CLARO -> 0.74f
        WrapperShade.COLORADO -> 0.68f
        WrapperShade.MADURO -> 0.64f
        WrapperShade.OSCURO -> 0.58f
    }
    var fruit = 0.36f

    if ("cedar" in text || "oak" in text || "wood" in text || "hickory" in text) cedar += 0.14f
    if ("cocoa" in text || "chocolate" in text || "mocha" in text || "fudge" in text || "espresso" in text || "coffee" in text) cocoa += 0.12f
    if ("pepper" in text || "cayenne" in text || "chili" in text || "spice" in text || "cinnamon" in text || "nutmeg" in text || "clove" in text) spice += 0.10f
    if ("caramel" in text || "molasses" in text || "honey" in text || "sugar" in text || "toffee" in text || "maple" in text || "syrup" in text || "sweet" in text) caramel += 0.18f
    if ("earth" in text || "soil" in text || "loam" in text || "peat" in text || "leather" in text || "campfire" in text || "smoke" in text || "meat" in text) earth += 0.14f
    if ("cream" in text || "butter" in text || "vanilla" in text || "brioche" in text || "pastry" in text || "milk" in text || "latte" in text || "marshmallow" in text) cream += 0.14f
    if ("almond" in text || "cashew" in text || "hazelnut" in text || "walnut" in text || "nut" in text || "peanut" in text || "bread" in text || "toast" in text || "graham" in text) nut += 0.14f
    if ("fruit" in text || "cherry" in text || "berry" in text || "berries" in text || "plum" in text || "fig" in text || "raisin" in text || "citrus" in text || "orange" in text || "apricot" in text || "date" in text) fruit += 0.34f
    if ("candela" in text || "green tea" in text || "grass" in text || "floral" in text || "herbal" in text) {
        fruit += 0.22f
        cedar += 0.08f
        earth -= 0.08f
    }
    if ("anise" in text || "licorice" in text || "mint" in text || "peppermint" in text || "rum" in text || "cognac" in text || "bourbon" in text || "sherry" in text) {
        caramel += 0.14f
        spice += 0.08f
    }
    if (b in listOf("INFUSED", "AROMATIC", "SWEET", "SWEET TIP", "FLAVORED")) {
        caramel += 0.14f
        cream += 0.08f
    }

    return FlavorAxes(
        woodCedar = cedar.coerceIn(0.18f, 0.98f),
        cocoaRoast = cocoa.coerceIn(0.18f, 0.98f),
        spicePepper = spice.coerceIn(0.14f, 0.98f),
        caramelSweet = caramel.coerceIn(0.24f, 0.98f),
        earthSmoke = earth.coerceIn(0.18f, 0.98f),
        creamVanilla = cream.coerceIn(0.20f, 0.98f),
        nutMalt = nut.coerceIn(0.24f, 0.96f),
        fruitZest = fruit.coerceIn(0.20f, 0.94f),
    )
}

private fun buildThirdProgression(
    notes: List<String>,
    wrapper: String,
    strength: StrengthLevel,
    bodyTier: String,
): Triple<String, String, String> {
    val n0 = notes.getOrElse(0) { "toasted cedar" }.lowercase()
    val n1 = notes.getOrElse(1) { "roasted nut" }.lowercase()
    val n2 = notes.getOrElse(2) { "warm spice" }.lowercase()
    val n3 = notes.getOrElse(3) { "smooth earth" }.lowercase()
    val isInfused = bodyTier.uppercase() in listOf("INFUSED", "AROMATIC", "SWEET", "SWEET TIP", "FLAVORED")

    val first = if (isInfused) {
        "1st Third: Immediate sweet-cap aroma of $n0 layered over $wrapper leaf."
    } else {
        "1st Third: Opens with crisp $n0 and $n1 lifted by clean $wrapper aromatics."
    }
    val second = "2nd Third: Transitions smoothly into warm $n2 interwoven with lingering $n1."
    val final = when (strength) {
        StrengthLevel.MILD -> "Final Third: Mellow $n3 and soft $n0 taper off with zero bitterness."
        StrengthLevel.MEDIUM -> "Final Third: Balanced $n3 and $n2 deepen cleanly through the final inch."
        StrengthLevel.MEDIUM_FULL -> "Final Third: Concentrated $n2 and savory $n3 build satisfying depth."
        StrengthLevel.FULL -> "Final Third: Full-throttle $n3, dense $n0, and warming $n2 linger on the finish."
    }
    return Triple(first, second, final)
}
