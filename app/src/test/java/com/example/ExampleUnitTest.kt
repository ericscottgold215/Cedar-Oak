package com.example

import com.example.data.catalog.PairingCatalog
import com.example.data.history.CigarHistoryCatalog
import com.example.model.PairingRatingSummary
import com.example.model.PairingStrategy
import com.example.model.buildPairingId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun catalogHasExpandedUniqueCigarsAndBeverages() {
        assertTrue("Expected >= 590 cigars, got ${PairingCatalog.cigars.size}", PairingCatalog.cigars.size >= 590)
        assertTrue("Expected >= 105 beverages, got ${PairingCatalog.beverages.size}", PairingCatalog.beverages.size >= 105)
        assertEquals(PairingCatalog.cigars.size, PairingCatalog.cigars.map { it.id }.toSet().size)
        assertEquals(PairingCatalog.beverages.size, PairingCatalog.beverages.map { it.id }.toSet().size)
    }

    @Test
    fun catalogIncludesAllRequestedCigarBrands() {
        val brandCounts = PairingCatalog.cigars.groupingBy { it.brand }.eachCount()
        val expectedBrands = listOf(
            "CAO",
            "Cohiba",
            "Macanudo",
            "Alec Bradley",
            "Diesel",
            "Man O' War",
            "Ave Maria",
            "Nica Libre",
            "ACID",
            "AJ Fernandez",
            "Caldwell",
            "Casa Turrent",
            "Casdagli",
            "Cavalier Genève",
            "Curivari",
            "Dapper",
            "Davidoff",
            "Deadwood",
            "Diplomáticos",
            "E.P. Carrillo",
            "Foundation",
            "Gurkha",
            "Illusione",
            "Joya de Nicaragua",
            "Juan López",
            "Kristoff",
            "La Aroma de Cuba",
            "La Flor Dominicana",
            "La Palina",
            "My Father",
            "Nub",
            "Oliva",
            "Padrón",
            "Patina",
            "Perdomo",
            "Plasencia",
            "Por Larrañaga",
            "Quintero",
            "Ramón Allones",
            "Rocky Patel",
            "RoMa Craft",
            "San Cristóbal",
            "Southern Draw",
            "Tatuaje",
            "Te-Amo",
            "VegaFina",
            "Warped",
            "Zino",
        )
        expectedBrands.forEach { brand ->
            assertTrue(
                "Expected at least 2 cigars for brand $brand, found ${brandCounts[brand] ?: 0}",
                (brandCounts[brand] ?: 0) >= 2
            )
        }
    }

    @Test
    fun everyCigarHasAlcoholicAndNonAlcoholicPairings() {
        PairingCatalog.cigars.forEach { cigar ->
            val suggestions = PairingCatalog.pairingsForCigar(cigar.id, emptyMap())
            assertTrue("${cigar.name} needs >= 4 alcoholic pairings", suggestions.count { it.beverage.isAlcoholic } >= 4)
            assertTrue("${cigar.name} needs >= 4 zero-proof pairings", suggestions.count { !it.beverage.isAlcoholic } >= 4)
            suggestions.forEach { suggestion ->
                val breakdown = suggestion.breakdown
                assertNotNull("Expected PairingBreakdown for ${suggestion.pairingId}", breakdown)
                breakdown!!
                assertTrue(breakdown.flavorHarmony in 0..100)
                assertTrue(breakdown.bodyBalance in 0..100)
                assertTrue(breakdown.wrapperAffinity in 0..100)
                assertTrue(breakdown.sessionPacing in 0..100)
                assertTrue(breakdown.counterpointNotes.isNotEmpty())
                assertTrue(breakdown.bestThirdWindow.isNotBlank())
                assertTrue(breakdown.sommelierRule.isNotBlank())
            }
        }
    }

    @Test
    fun everyBeverageHasCigarPairings() {
        PairingCatalog.beverages.forEach { bev ->
            val suggestions = PairingCatalog.pairingsForBeverage(bev.id, emptyMap())
            assertTrue("${bev.name} needs >= 5 cigar pairings", suggestions.size >= 5)
        }
    }

    @Test
    fun pairingEngineGuardsAgainstOverpoweringMildClaroCigars() {
        val mildClaro = PairingCatalog.cigarById("davidoff_sig2")!!
        val oolong = PairingCatalog.beverageById("roasted_oolong")!!
        val islay = PairingCatalog.beverageById("islay_peated")!!

        val gentleMatch = PairingCatalog.evaluatePairing(mildClaro, oolong)
        val heavyMatch = PairingCatalog.evaluatePairing(mildClaro, islay)

        assertTrue(
            "Mild Claro should score higher body balance with Roasted Oolong (${gentleMatch.breakdown!!.bodyBalance}) than Islay (${heavyMatch.breakdown!!.bodyBalance})",
            gentleMatch.breakdown!!.bodyBalance > heavyMatch.breakdown!!.bodyBalance
        )
        assertTrue(
            "Overpower alert expected in sommelierRule",
            heavyMatch.breakdown!!.sommelierRule.contains("Overpower", ignoreCase = true)
        )
    }

    @Test
    fun pairingEngineRewardsCaskAgedAffinityAndSupportsStrategies() {
        val sherryDiesel = PairingCatalog.cigarById("diesel_whiskey_row_sherry")!!
        val speyside = PairingCatalog.beverageById("speyside_sherry")!!
        val gingerBeer = PairingCatalog.beverageById("fiery_ginger_beer")!!
        val cacao = PairingCatalog.beverageById("drinking_cacao")!!

        val caskSuggestion = PairingCatalog.evaluatePairing(sherryDiesel, speyside)
        assertTrue(caskSuggestion.breakdown!!.wrapperAffinity >= 88)

        val cleanserGinger = PairingCatalog.evaluatePairing(
            sherryDiesel,
            gingerBeer,
            strategy = PairingStrategy.PALATE_CLEANSER,
        )
        val cleanserCacao = PairingCatalog.evaluatePairing(
            sherryDiesel,
            cacao,
            strategy = PairingStrategy.PALATE_CLEANSER,
        )
        assertTrue(
            "Palate Reset strategy should score ginger beer (${cleanserGinger.matchScore}) above heavy drinking cacao (${cleanserCacao.matchScore})",
            cleanserGinger.matchScore > cleanserCacao.matchScore
        )
    }

    @Test
    fun everyCigarAndBrandHasCompleteHistoryLesson() {
        val uniqueBrands = PairingCatalog.cigars.map { it.brand }.toSet()
        uniqueBrands.forEach { brand ->
            val company = CigarHistoryCatalog.companyForBrand(brand)
            assertNotNull("Missing company history for brand: $brand", company)
            company!!
            assertTrue("$brand missing foundedYear", company.foundedYear.isNotBlank())
            assertTrue("$brand missing founder", company.founder.isNotBlank())
            assertTrue("$brand missing currentOwner", company.currentOwner.isNotBlank())
            assertTrue("$brand missing headquartersAndFactory", company.headquartersAndFactory.isNotBlank())
            assertTrue("$brand missing heritageStory", company.heritageStory.isNotBlank())
        }

        PairingCatalog.cigars.forEach { cigar ->
            val release = CigarHistoryCatalog.releaseForCigar(cigar.id)
            assertNotNull("Missing release history for cigar: ${cigar.id}", release)
            release!!
            assertTrue("${cigar.id} missing launchYear", release.launchYear.isNotBlank())
            assertTrue("${cigar.id} missing blenderAndFactory", release.blenderAndFactory.isNotBlank())
            assertTrue("${cigar.id} missing productionLocation", release.productionLocation.isNotBlank())
            assertTrue("${cigar.id} missing storyAndLegacy", release.storyAndLegacy.isNotBlank())
        }
    }

    @Test
    fun evaluatePairingIncludesAverageRatingWhenPresent() {
        val cigar = PairingCatalog.cigars.first()
        val bev = PairingCatalog.beverages.last()
        val pid = buildPairingId(cigar.id, bev.id)
        val summary = PairingRatingSummary(pairingId = pid, averageRating = 4.6, reviewCount = 5)
        val suggestion = PairingCatalog.evaluatePairing(cigar, bev, summary)

        assertTrue(suggestion.matchScore in 50..99)
        assertTrue(suggestion.averageRating == 4.6)
        assertTrue(suggestion.reviewCount == 5)
    }
}
