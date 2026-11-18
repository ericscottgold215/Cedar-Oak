package com.example.data.pairing

import com.example.model.Beverage
import com.example.model.BeverageCategory
import com.example.model.BodyLevel
import com.example.model.Cigar
import com.example.model.CuratedPairing
import com.example.model.FlavorAxes
import com.example.model.PairingBreakdown
import com.example.model.PairingRatingSummary
import com.example.model.PairingStrategy
import com.example.model.PairingSuggestion
import com.example.model.StrengthLevel
import com.example.model.WrapperShade
import com.example.model.buildPairingId
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Multi-factor sommelier pairing engine evaluating cigar-to-beverage and beverage-to-cigar
 * compatibility across four dimensions:
 * 1. Flavor Harmony (8-axis aromatic cosine alignment + shared bridge peaks)
 * 2. Body & Strength Balance (weight matching with overpower protection)
 * 3. Wrapper, Cask & Pour Affinity (domain-specific leaf + beverage category chemistry)
 * 4. Session Pacing (smoke time vs. pour tempo & serving temperature)
 */
object PairingEngine {

    fun evaluate(
        cigar: Cigar,
        beverage: Beverage,
        curated: CuratedPairing? = null,
        ratingSummary: PairingRatingSummary? = null,
        strategy: PairingStrategy = PairingStrategy.BALANCED,
    ): PairingSuggestion {
        val pid = buildPairingId(cigar.id, beverage.id)
        val bridgeNotes = computeBridgeNotes(cigar.flavorAxes, beverage.flavorAxes)
        val flavorHarmony = computeFlavorHarmony(cigar.flavorAxes, beverage.flavorAxes, bridgeNotes)
        val (bodyBalance, balanceNote) = computeBodyBalance(cigar, beverage)
        val (wrapperAffinity, affinityRule) = computeWrapperAffinity(cigar, beverage)
        val sessionPacing = computeSessionPacing(cigar, beverage)
        val counterpointNotes = computeCounterpointNotes(cigar, beverage)
        val bestThirdWindow = computeBestThirdWindow(cigar, beverage)
        val cleanserAffinity = computePalateCleanserScore(cigar, beverage)
        val contrastAffinity = computeContrastScore(cigar, beverage, counterpointNotes)

        val sommelierRule = buildSommelierRule(
            cigar = cigar,
            beverage = beverage,
            affinityRule = affinityRule,
            balanceNote = balanceNote,
            strategy = strategy,
        )

        val breakdown = PairingBreakdown(
            flavorHarmony = flavorHarmony,
            bodyBalance = bodyBalance,
            wrapperAffinity = wrapperAffinity,
            sessionPacing = sessionPacing,
            counterpointNotes = counterpointNotes,
            bestThirdWindow = bestThirdWindow,
            sommelierRule = sommelierRule,
        )

        val strategyAdjustedScore = when (strategy) {
            PairingStrategy.BALANCED -> {
                val computed = (
                    flavorHarmony * 0.38 +
                        bodyBalance * 0.25 +
                        wrapperAffinity * 0.22 +
                        sessionPacing * 0.15
                    ).roundToInt()
                if (curated != null) {
                    maxOf(curated.matchScore, computed.coerceIn(72, 98))
                } else {
                    computed.coerceIn(58, 96)
                }
            }

            PairingStrategy.COMPLEMENTARY -> {
                val bridgeBoost = bridgeNotes.size * 4
                val base = (flavorHarmony * 0.50 + wrapperAffinity * 0.25 + bodyBalance * 0.15 + sessionPacing * 0.10 + bridgeBoost).roundToInt()
                val curatedBoost = if (curated != null && bridgeNotes.size >= 2) 3 else 0
                (base + curatedBoost).coerceIn(55, 99)
            }

            PairingStrategy.CONTRASTING -> {
                val base = (contrastAffinity * 0.45 + bodyBalance * 0.25 + wrapperAffinity * 0.20 + sessionPacing * 0.10).roundToInt()
                base.coerceIn(55, 98)
            }

            PairingStrategy.PALATE_CLEANSER -> {
                val base = (cleanserAffinity * 0.52 + bodyBalance * 0.23 + sessionPacing * 0.15 + flavorHarmony * 0.10).roundToInt()
                base.coerceIn(52, 98)
            }
        }

        if (curated != null && strategy == PairingStrategy.BALANCED) {
            return PairingSuggestion(
                pairingId = pid,
                cigar = cigar,
                beverage = beverage,
                matchScore = curated.matchScore,
                pairingStyle = curated.pairingStyle,
                whyItWorks = curated.whyItWorks,
                servingTip = curated.servingTip,
                isCurated = true,
                bridgeNotes = bridgeNotes,
                breakdown = breakdown,
                averageRating = ratingSummary?.averageRating,
                reviewCount = ratingSummary?.reviewCount ?: 0,
            )
        }

        val pairingStyle = determinePairingStyle(
            cigar = cigar,
            beverage = beverage,
            strategy = strategy,
            bridgeNotes = bridgeNotes,
            counterpointNotes = counterpointNotes,
            cleanserAffinity = cleanserAffinity,
            curated = curated,
        )

        val whyItWorks = buildWhyItWorks(
            cigar = cigar,
            beverage = beverage,
            strategy = strategy,
            pairingStyle = pairingStyle,
            bridgeNotes = bridgeNotes,
            counterpointNotes = counterpointNotes,
            sommelierRule = sommelierRule,
            curated = curated,
        )

        val servingTip = buildServingTip(cigar, beverage, curated)

        return PairingSuggestion(
            pairingId = pid,
            cigar = cigar,
            beverage = beverage,
            matchScore = strategyAdjustedScore,
            pairingStyle = pairingStyle,
            whyItWorks = whyItWorks,
            servingTip = servingTip,
            isCurated = curated != null,
            bridgeNotes = bridgeNotes,
            breakdown = breakdown,
            averageRating = ratingSummary?.averageRating,
            reviewCount = ratingSummary?.reviewCount ?: 0,
        )
    }

    fun computeBridgeNotes(a: FlavorAxes, b: FlavorAxes): List<String> {
        val listA = a.asLabeledList()
        val listB = b.asLabeledList()
        return listA.zip(listB)
            .map { (axisA, axisB) ->
                val minShared = minOf(axisA.second, axisB.second)
                val combined = (axisA.second + axisB.second) / 2f
                Triple(axisA.first, minShared, combined)
            }
            .filter { it.second >= 0.50f }
            .sortedByDescending { it.third }
            .take(3)
            .map { it.first }
    }

    private fun computeFlavorHarmony(
        cigarAxes: FlavorAxes,
        bevAxes: FlavorAxes,
        bridgeNotes: List<String>,
    ): Int {
        val cVec = cigarAxes.asLabeledList().map { it.second }
        val bVec = bevAxes.asLabeledList().map { it.second }
        val dot = cVec.zip(bVec).sumOf { (a, b) -> (a * b).toDouble() }
        val magC = sqrt(cVec.sumOf { (it * it).toDouble() })
        val magB = sqrt(bVec.sumOf { (it * it).toDouble() })
        val cosine = if (magC > 0 && magB > 0) dot / (magC * magB) else 0.7
        val bridgeBonus = when (bridgeNotes.size) {
            3 -> 6
            2 -> 3
            1 -> 1
            else -> -4
        }
        return (cosine * 92.0 + bridgeBonus).roundToInt().coerceIn(50, 99)
    }

    private fun computeBodyBalance(cigar: Cigar, beverage: Beverage): Pair<Int, String> {
        val cigarWeight = when (cigar.strength) {
            StrengthLevel.MILD -> 1.0
            StrengthLevel.MEDIUM -> 2.1
            StrengthLevel.MEDIUM_FULL -> 3.1
            StrengthLevel.FULL -> 4.0
        } + when (cigar.wrapperShade) {
            WrapperShade.CLARO -> -0.2
            WrapperShade.COLORADO_CLARO -> 0.0
            WrapperShade.COLORADO -> 0.15
            WrapperShade.MADURO -> 0.35
            WrapperShade.OSCURO -> 0.5
        }

        val bevWeight = when (beverage.body) {
            BodyLevel.LIGHT -> 1.1
            BodyLevel.MEDIUM -> 2.3
            BodyLevel.FULL -> 3.9
        } + if (beverage.isAlcoholic) 0.25 else -0.1

        val diff = bevWeight - cigarWeight
        val absDiff = abs(diff)
        val isRefreshingPour = isCrispPalateCleanser(beverage)

        return when {
            // Heavy drink burying a mild/delicate cigar
            cigar.strength == StrengthLevel.MILD && bevWeight >= 3.4 -> {
                val score = (68 - (absDiff * 6.0)).roundToInt().coerceIn(48, 70)
                score to "Overpower alert: heavy pour may mask delicate ${cigar.wrapper} nuances"
            }
            // Light refreshing palate cleanser pairing with a fuller cigar (classic sommelier reset)
            cigarWeight >= 3.0 && bevWeight <= 2.0 && isRefreshingPour -> {
                84 to "Intentional contrast: crisp refreshment lifts heavy leaf oils"
            }
            absDiff <= 0.6 -> {
                96 to "Ideal weight match between ${cigar.strength.label.lowercase()} leaf and ${beverage.body.label.lowercase()} body"
            }
            absDiff <= 1.25 -> {
                88 to "Well-proportioned weight; neither smoke nor pour dominates"
            }
            absDiff <= 2.0 -> {
                77 to "Noticeable weight gap; take smaller sips to preserve finish balance"
            }
            else -> {
                64 to "Wide intensity gap; best suited for deliberate palate contrast"
            }
        }
    }

    private fun computeWrapperAffinity(cigar: Cigar, beverage: Beverage): Pair<Int, String> {
        var score = 74
        var rule = "Balanced leaf-to-pour chemistry"
        val wrapperLower = cigar.wrapper.lowercase()
        val summaryLower = cigar.summary.lowercase()
        val bevText = "${beverage.name} ${beverage.subtitle} ${beverage.summary}".lowercase()

        // Cask / Barrel aged synergy
        val isCaskAgedCigar = "whisky" in summaryLower || "whiskey" in summaryLower ||
            "cask" in summaryLower || "barrel" in summaryLower || "cognac" in wrapperLower ||
            "sherry" in summaryLower || "sherry" in cigar.name.lowercase()
        if (isCaskAgedCigar) {
            when (beverage.category) {
                BeverageCategory.WHISKEY, BeverageCategory.BRANDY, BeverageCategory.COCKTAILS -> {
                    score += 18
                    rule = "Cask-to-glass echo: barrel-aged leaf locks directly onto oak spirit character"
                }
                BeverageCategory.WINE -> {
                    if ("sherry" in bevText || "port" in bevText) {
                        score += 20
                        rule = "Sherry & cask kinship amplifies dried fig, walnut, and oak"
                    }
                }
                else -> score += 4
            }
        }

        // Wrapper shade & varietal affinity rules
        when (cigar.wrapperShade) {
            WrapperShade.CLARO -> {
                when (beverage.category) {
                    BeverageCategory.TEA, BeverageCategory.COFFEE -> {
                        if ("espresso" !in bevText && "ristretto" !in bevText) {
                            score += 16
                            rule = "Claro leaf & gentle roast/tea rule: creaminess shines without bitter clash"
                        } else {
                            score -= 4
                        }
                    }
                    BeverageCategory.CRAFT_SODA -> {
                        score += 14
                        rule = "Mild Claro & botanical fizz keep cedar and almond bright"
                    }
                    BeverageCategory.WINE -> {
                        if ("champagne" in bevText || "prosecco" in bevText || "chablis" in bevText || "white" in bevText) {
                            score += 18
                            rule = "Champagne & Claro benchmark: brioche lees mirror toasted almond cream"
                        }
                    }
                    BeverageCategory.WHISKEY -> {
                        if ("wheated" in bevText || "speyside" in bevText || "irish" in bevText || "japanese" in bevText) {
                            score += 16
                            rule = "Soft malt & wheated bourbon preserve delicate Connecticut cream"
                        } else if ("islay" in bevText || "peat" in bevText) {
                            score -= 14
                            rule = "Heavy peat overwhelms mild Claro aromatics"
                        }
                    }
                    BeverageCategory.BEER -> {
                        if ("stout" in bevText || "imperial" in bevText) score -= 10
                        else score += 8
                    }
                    else -> Unit
                }
            }

            WrapperShade.COLORADO_CLARO -> {
                when (beverage.category) {
                    BeverageCategory.BRANDY -> {
                        score += 18
                        rule = "Cameroon/Colorado Claro & Cognac affinity: baking spice meets rancio orange"
                    }
                    BeverageCategory.WHISKEY -> {
                        if ("speyside" in bevText || "japanese" in bevText || "bourbon" in bevText) {
                            score += 14
                            rule = "Cedar-spice harmony with honeyed oak spirits"
                        }
                    }
                    BeverageCategory.TEA, BeverageCategory.CRAFT_SODA -> {
                        score += 14
                        rule = "Aromatic spice & root botanicals lift Cameroon cedar sweetness"
                    }
                    else -> score += 6
                }
            }

            WrapperShade.COLORADO -> {
                when (beverage.category) {
                    BeverageCategory.WHISKEY, BeverageCategory.COCKTAILS, BeverageCategory.AGAVE -> {
                        score += 15
                        rule = "Habano/Corojo pepper meets rye, oak, or aged agave warmth"
                    }
                    BeverageCategory.COFFEE -> {
                        score += 14
                        rule = "Roasted espresso crema anchors medium-full Habano spice"
                    }
                    BeverageCategory.ZERO_PROOF_ELIXIRS, BeverageCategory.CRAFT_SODA -> {
                        score += 12
                        rule = "Zero-proof spice & citrus cut clean across peppery Habano oils"
                    }
                    else -> score += 6
                }
            }

            WrapperShade.MADURO, WrapperShade.OSCURO -> {
                when (beverage.category) {
                    BeverageCategory.CACAO, BeverageCategory.COFFEE -> {
                        score += 19
                        rule = "Maduro & dark roast synergy: bittersweet cacao and espresso interlock"
                    }
                    BeverageCategory.RUM -> {
                        score += 17
                        rule = "Aged molasses rum rounds Maduro earth into dark fudge and caramel"
                    }
                    BeverageCategory.WINE -> {
                        if ("port" in bevText || "px" in bevText || "sherry" in bevText || "cabernet" in bevText || "malbec" in bevText) {
                            score += 18
                            rule = "Fortified fig & dark fruit soften Broadleaf/San Andrés earth"
                        }
                    }
                    BeverageCategory.BEER -> {
                        if ("stout" in bevText || "porter" in bevText || "quadrupel" in bevText) {
                            score += 18
                            rule = "Dark roasted malt & abbey sugar match Maduro density"
                        }
                    }
                    BeverageCategory.WHISKEY -> {
                        if ("single barrel" in bevText || "bourbon" in bevText || "islay" in bevText || "rye" in bevText) {
                            score += 15
                            rule = "Charred American oak & peat embrace Oscuro molasses depth"
                        }
                    }
                    BeverageCategory.TEA -> {
                        if ("pu-erh" in bevText || "chai" in bevText || "lapsang" in bevText) {
                            score += 16
                            rule = "Aged Pu-erh & spiced tea match Maduro leather without bitterness"
                        }
                    }
                    else -> score += 5
                }
            }
        }

        // Extra wrapper-specific nudges
        if ("cameroon" in wrapperLower && ("root beer" in bevText || "cognac" in bevText || "chai" in bevText)) {
            score += 6
        }
        if (("broadleaf" in wrapperLower || "san andrés" in wrapperLower || "san andres" in wrapperLower) &&
            ("cacao" in bevText || "chocolate" in bevText || "bourbon" in bevText || "stout" in bevText || "olla" in bevText)
        ) {
            score += 6
        }

        // Infused / coffee / sweet-cap / flavored chemistry
        val isSweetOrInfused = "infused" in wrapperLower || "sweet" in wrapperLower ||
            "flavored" in wrapperLower || "rum" in wrapperLower || "berry" in wrapperLower ||
            "infused" in summaryLower || "sweet cap" in summaryLower || "sweet-tip" in summaryLower ||
            cigar.brand in setOf("ACID", "Deadwood", "Java", "Tatiana", "Trader Jack's", "Swisher Sweets", "White Owl", "Dark Desires")
        if (isSweetOrInfused) {
            when (beverage.category) {
                BeverageCategory.COFFEE, BeverageCategory.CACAO, BeverageCategory.RUM -> {
                    score += 14
                    rule = "Aromatic infusion match: sweet cap and botanical oils harmonize with creamy roast or cane rum"
                }
                BeverageCategory.CRAFT_SODA, BeverageCategory.ZERO_PROOF_ELIXIRS -> {
                    score += 12
                    rule = "Botanical fizz & sweet aromatics elevate infused tobacco notes cleanly"
                }
                BeverageCategory.WHISKEY -> {
                    if ("islay" in bevText || "peat" in bevText) score -= 10
                }
                else -> Unit
            }
        }

        // Candela herbal / green-tea synergy
        if ("candela" in wrapperLower) {
            when (beverage.category) {
                BeverageCategory.TEA, BeverageCategory.CRAFT_SODA, BeverageCategory.ZERO_PROOF_ELIXIRS -> {
                    score += 14
                    rule = "Candela & herbal lift: bright chlorophyll grassiness mirrors crisp botanical tea and tonic"
                }
                BeverageCategory.WINE -> {
                    if ("champagne" in bevText || "prosecco" in bevText || "sauvignon" in bevText || "chablis" in bevText || "white" in bevText) {
                        score += 14
                        rule = "Crisp white wine acidity refreshes sweet grassy candela leaf"
                    }
                }
                else -> Unit
            }
        }

        // Fire-cured Kentucky / Italian cheroot affinity
        if ("kentucky" in wrapperLower || "fire-cured" in wrapperLower || "fire-cured" in summaryLower) {
            when (beverage.category) {
                BeverageCategory.WHISKEY -> {
                    if ("bourbon" in bevText || "rye" in bevText || "islay" in bevText || "peat" in bevText) {
                        score += 16
                        rule = "Fire-cured hickory smoke locks seamlessly onto charred bourbon oak or Islay peat"
                    }
                }
                BeverageCategory.COFFEE -> {
                    score += 14
                    rule = "Dark roast espresso balances rustic Italian campfire tobacco smoke"
                }
                BeverageCategory.BEER -> {
                    if ("stout" in bevText || "porter" in bevText) score += 14
                }
                else -> Unit
            }
        }

        return score.coerceIn(54, 99) to rule
    }

    private fun computeSessionPacing(cigar: Cigar, beverage: Beverage): Int {
        val bevText = "${beverage.name} ${beverage.subtitle} ${beverage.glassware}".lowercase()
        val isShortDrink = "demitasse" in bevText || "espresso" in bevText || "ristretto" in bevText || "flute" in bevText || "coupe" in bevText
        val isSlowSipper = "glencairn" in bevText || "snifter" in bevText || "rocks" in bevText || "teapot" in bevText || "port" in bevText || "mug" in bevText

        return when {
            cigar.smokeTimeMinutes <= 50 && isShortDrink -> 95
            cigar.smokeTimeMinutes >= 70 && isSlowSipper -> 95
            cigar.smokeTimeMinutes in 50..75 -> 88
            cigar.smokeTimeMinutes >= 75 && isShortDrink -> 74
            cigar.smokeTimeMinutes <= 45 && isSlowSipper -> 80
            else -> 84
        }
    }

    private fun computeCounterpointNotes(cigar: Cigar, beverage: Beverage): List<String> {
        val notes = mutableListOf<String>()
        val c = cigar.flavorAxes
        val b = beverage.flavorAxes

        if (c.spicePepper >= 0.62f && b.caramelSweet >= 0.65f) {
            notes += "Caramel sweetness softens peppercorn bite"
        }
        if ((c.earthSmoke >= 0.62f || c.cocoaRoast >= 0.65f) && b.creamVanilla >= 0.62f) {
            notes += "Creamy vanilla texture buffers smoky roast"
        }
        if (b.fruitZest >= 0.65f && (c.earthSmoke >= 0.55f || c.spicePepper >= 0.55f)) {
            notes += "Bright zest & acidity lift lingering wrapper oils"
        }
        if (c.cocoaRoast >= 0.68f && b.cocoaRoast >= 0.68f) {
            notes += "Shared cacao & espresso double down on dark richness"
        }
        if (c.woodCedar >= 0.68f && b.woodCedar >= 0.65f) {
            notes += "Toasted oak & Spanish cedar lock together cleanly"
        }
        if (c.creamVanilla >= 0.68f && b.nutMalt >= 0.65f) {
            notes += "Roasted malt & nuttiness enrich creamy smoke"
        }
        if (notes.isEmpty()) {
            notes += "Measured oak and toasted warmth bridge the finish"
        }
        return notes.take(3)
    }

    private fun computeContrastScore(
        cigar: Cigar,
        beverage: Beverage,
        counterpointNotes: List<String>,
    ): Int {
        val c = cigar.flavorAxes
        val b = beverage.flavorAxes
        val sweetVsSpice = minOf(c.spicePepper, b.caramelSweet) * 45f
        val creamVsEarth = minOf(maxOf(c.earthSmoke, c.cocoaRoast), b.creamVanilla) * 35f
        val zestVsBody = minOf(maxOf(c.earthSmoke, c.spicePepper), b.fruitZest) * 32f
        val counterpointBonus = counterpointNotes.size * 5
        return (56 + sweetVsSpice + creamVsEarth + zestVsBody + counterpointBonus).roundToInt().coerceIn(55, 98)
    }

    private fun computePalateCleanserScore(cigar: Cigar, beverage: Beverage): Int {
        val b = beverage.flavorAxes
        val bevText = "${beverage.name} ${beverage.subtitle} ${beverage.summary}".lowercase()
        val hasFizz = beverage.category == BeverageCategory.CRAFT_SODA ||
            beverage.category == BeverageCategory.ZERO_PROOF_ELIXIRS ||
            "champagne" in bevText || "prosecco" in bevText || "spritz" in bevText ||
            "tonic" in bevText || "ginger" in bevText || "sparkling" in bevText ||
            "pilsner" in bevText || "cider" in bevText || "ipa" in bevText || "highball" in bevText
        val isBriskTea = beverage.category == BeverageCategory.TEA && "chai" !in bevText
        val fizzBonus = if (hasFizz) 26 else if (isBriskTea) 18 else 0
        val zestScore = b.fruitZest * 36f
        val cleanFinishPenalty = if (b.creamVanilla >= 0.80f && !hasFizz) -10 else 0
        val heavyOilNeed = if (cigar.wrapperShade >= WrapperShade.COLORADO) 8 else 4
        return (44 + fizzBonus + zestScore + cleanFinishPenalty + heavyOilNeed).roundToInt().coerceIn(48, 98)
    }

    private fun isCrispPalateCleanser(beverage: Beverage): Boolean {
        val bevText = "${beverage.name} ${beverage.subtitle}".lowercase()
        return beverage.category == BeverageCategory.CRAFT_SODA ||
            beverage.category == BeverageCategory.TEA ||
            beverage.category == BeverageCategory.ZERO_PROOF_ELIXIRS ||
            "champagne" in bevText || "prosecco" in bevText || "cider" in bevText ||
            "pilsner" in bevText || "ginger" in bevText || "highball" in bevText ||
            beverage.flavorAxes.fruitZest >= 0.72f
    }

    private fun computeBestThirdWindow(cigar: Cigar, beverage: Beverage): String {
        return when {
            cigar.strength == StrengthLevel.MILD -> {
                "1st & 2nd Third · Gentle aromatics shine while palate is fresh"
            }
            cigar.strength == StrengthLevel.FULL && beverage.flavorAxes.caramelSweet >= 0.70f -> {
                "2nd & Final Third · Drink sweetness tames surging pepper and tar"
            }
            isCrispPalateCleanser(beverage) -> {
                "Between Every Third · Scrubs palate oils cleanly from light to nub"
            }
            beverage.body == BodyLevel.FULL -> {
                "2nd Third Onward · Matches peak body as wrapper oils warm up"
            }
            else -> {
                "All Three Thirds · Steady pacing from cold draw to final inch"
            }
        }
    }

    private fun buildSommelierRule(
        cigar: Cigar,
        beverage: Beverage,
        affinityRule: String,
        balanceNote: String,
        strategy: PairingStrategy,
    ): String {
        return when (strategy) {
            PairingStrategy.BALANCED -> "$affinityRule. $balanceNote."
            PairingStrategy.COMPLEMENTARY -> "Complementary Mirror Rule: Aligns shared ${cigar.flavorAxes.topAxes(2).joinToString(" & ").lowercase()} peaks between smoke and glass."
            PairingStrategy.CONTRASTING -> "Counterpoint Rule: Balances ${cigar.strength.label.lowercase()} tobacco against opposing ${if (beverage.flavorAxes.caramelSweet >= 0.65f) "sweetness" else "zesty lift"}."
            PairingStrategy.PALATE_CLEANSER -> "Palate Reset Rule: Clears ${cigar.wrapperShade.label.lowercase()} wrapper oils between draws to sharpen retrohale clarity."
        }
    }

    private fun determinePairingStyle(
        cigar: Cigar,
        beverage: Beverage,
        strategy: PairingStrategy,
        bridgeNotes: List<String>,
        counterpointNotes: List<String>,
        cleanserAffinity: Int,
        curated: CuratedPairing?,
    ): String {
        if (curated != null && strategy == PairingStrategy.BALANCED) return curated.pairingStyle
        return when (strategy) {
            PairingStrategy.COMPLEMENTARY -> if (bridgeNotes.size >= 2) "Flavor Mirror" else "Aromatic Echo"
            PairingStrategy.CONTRASTING -> when {
                beverage.flavorAxes.caramelSweet >= 0.7f && cigar.flavorAxes.spicePepper >= 0.65f -> "Sweet Tames Spice"
                beverage.flavorAxes.creamVanilla >= 0.7f && cigar.flavorAxes.earthSmoke >= 0.6f -> "Velvet Counterpoint"
                else -> "Balanced Contrast"
            }
            PairingStrategy.PALATE_CLEANSER -> if (cleanserAffinity >= 82) "Crisp Palate Reset" else "Clean Refresh"
            PairingStrategy.BALANCED -> when {
                beverage.flavorAxes.caramelSweet >= 0.72f && cigar.flavorAxes.spicePepper >= 0.68f -> "Sweet Contrast"
                beverage.flavorAxes.fruitZest >= 0.75f && cigar.flavorAxes.spicePepper >= 0.6f -> "Zesty Palate Lift"
                bridgeNotes.size >= 2 -> "Flavor Mirror"
                counterpointNotes.isNotEmpty() -> "Balanced Synergy"
                else -> "Balanced Contrast"
            }
        }
    }

    private fun buildWhyItWorks(
        cigar: Cigar,
        beverage: Beverage,
        strategy: PairingStrategy,
        pairingStyle: String,
        bridgeNotes: List<String>,
        counterpointNotes: List<String>,
        sommelierRule: String,
        curated: CuratedPairing?,
    ): String {
        if (curated != null && strategy == PairingStrategy.BALANCED) return curated.whyItWorks
        val cigarLead = cigar.tastingNotes.take(2).joinToString(" & ").lowercase()
        val bevLead = beverage.tastingNotes.take(2).joinToString(" & ").lowercase()
        val bridgePhrase = if (bridgeNotes.isNotEmpty()) {
            bridgeNotes.joinToString(" and ").lowercase()
        } else {
            "toasted oak warmth"
        }
        val counterpoint = counterpointNotes.firstOrNull()?.replaceFirstChar { it.lowercase() }
            ?: "keeps the finish clean"

        return when (strategy) {
            PairingStrategy.COMPLEMENTARY ->
                "${cigar.brand} ${cigar.name}'s $cigarLead aligns with ${beverage.name}'s $bevLead, reinforcing shared $bridgePhrase across every draw."
            PairingStrategy.CONTRASTING ->
                "Using $pairingStyle logic, ${beverage.name}'s $bevLead offsets ${cigar.name}'s $cigarLead so $counterpoint."
            PairingStrategy.PALATE_CLEANSER ->
                "Built as a palate reset: ${beverage.name} refreshes ${cigar.wrapper} richness between puffs, preventing palate fatigue over a ${cigar.smokeTimeMinutes}-minute smoke."
            PairingStrategy.BALANCED ->
                "${cigar.name}'s $cigarLead meets ${beverage.name}'s $bevLead in a $pairingStyle centered on $bridgePhrase—$counterpoint."
        }.also {
            // Ensure sommelierRule stays utilized cleanly in breakdown
            sommelierRule.isNotEmpty()
        }
    }

    private fun buildServingTip(
        cigar: Cigar,
        beverage: Beverage,
        curated: CuratedPairing?,
    ): String {
        if (curated != null) return curated.servingTip
        val paceAdvice = if (cigar.smokeTimeMinutes >= 75) {
            "pace slowly across the ${cigar.smokeTimeMinutes}-minute session"
        } else {
            "take a measured sip right after the retrohale clears"
        }
        return "${beverage.servingTemp} in a ${beverage.glassware}; $paceAdvice."
    }
}
