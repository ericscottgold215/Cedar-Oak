package com.example.data.catalog

import com.example.data.pairing.PairingEngine
import com.example.model.Beverage
import com.example.model.Cigar
import com.example.model.CuratedPairing
import com.example.model.PairingRatingSummary
import com.example.model.PairingStrategy
import com.example.model.PairingSuggestion
import com.example.model.buildPairingId

object PairingCatalog {

    val cigars: List<Cigar> = CigarCatalogData.cigars
    val beverages: List<Beverage> = BeverageCatalogData.beverages

    private val cigarMap: Map<String, Cigar> = cigars.associateBy { it.id }
    private val beverageMap: Map<String, Beverage> = beverages.associateBy { it.id }

    val curatedPairings: List<CuratedPairing> = listOf(
        // Mild & Connecticut Classics
        CuratedPairing(
            "davidoff_sig2", "speyside_sherry", 97, "Delicate Harmony",
            "Heather honey and silky barley malt complement the Davidoff's sweet cream and toasted almond core without overpowering its delicate Panetela draw.",
            "Pour 1.5 oz neat at 64°F in a Glencairn; take small sips after the first inch of ash forms.",
        ),
        CuratedPairing(
            "davidoff_sig2", "brut_champagne", 96, "Crisp Effervescence",
            "Toasted brioche lees and fine pinpoint bubbles lift Davidoff's almond creaminess and refresh the palate effortlessly.",
            "Serve well chilled at 46°F in a tulip flute.",
        ),
        CuratedPairing(
            "davidoff_sig2", "roasted_oolong", 96, "Zero-Proof Grace",
            "Wuyi Rock Oolong's toasted grain and stone-fruit sweetness echo the Davidoff's almond creaminess while leaving the palate crystal clear.",
            "Steep loose leaves at 195°F for 2 minutes; sip warm alongside the morning or afternoon smoke.",
        ),
        CuratedPairing(
            "davidoff_sig2", "earl_grey_creme", 93, "Botanical Cream",
            "Calabrian bergamot and vanilla bean lift the Panetela's white blossom aromatics cleanly.",
            "Serve hot without heavy sugar to keep the delicate floral balance intact.",
        ),
        CuratedPairing(
            "ashton_classic", "wheated_bourbon", 95, "Honeyed Wheat Velvet",
            "Soft red wheat bourbon wraps wildflower honey and butterscotch smoothly around Ashton's roasted cashew creaminess.",
            "Serve neat in a Glencairn; let the bourbon breathe for 5 minutes.",
        ),
        CuratedPairing(
            "ashton_classic", "masala_chai", 94, "Zero-Proof Velvet",
            "Steamed milk and green cardamom add exotic warmth to the Ashton's buttery cashew and vanilla profile.",
            "Dust lightly with cinnamon and sip warm during a relaxed afternoon lounge session.",
        ),
        CuratedPairing(
            "sobremesa_brulee", "bourbon_single_barrel", 96, "Caramel Mirror",
            "Crème brûlée sweetness on the Sobremesa tip finds its mirror image in burnt caramel, vanilla pod, and charred American oak.",
            "Pour over a single large rock; let the cigar's sweet cap introduce the first sip.",
        ),
        CuratedPairing(
            "sobremesa_brulee", "smoked_maple_cold_brew", 98, "Zero-Proof Classic",
            "Sweet maple cold foam and low-acid cold brew weave seamlessly into the Sobremesa's custard-and-butterscotch core.",
            "Serve at 38°F with light ice; stir once so maple foam coats each sip.",
        ),
        CuratedPairing(
            "sobremesa_brulee", "vanilla_cream_soda", 95, "Custard Effervescence",
            "Real Madagascar vanilla bean soda mirrors the cigar's caramel crust while crisp bubbles refresh the palate.",
            "Serve over clear ice in a tall Collins glass.",
        ),
        CuratedPairing(
            "illusione_epernay", "brut_champagne", 97, "Épernay Benchmark",
            "Blended expressly for Champagne—orchard pear, toasted brioche, and hazelnut praline mirror vintage Brut lees seamlessly.",
            "Serve chilled at 46°F; ideal celebratory pairing.",
        ),
        CuratedPairing(
            "illusione_epernay", "viennese_cortado", 95, "Zero-Proof Patisserie",
            "Equal parts espresso and steamed oat cream spotlight the Épernay's coffee-cream and brioche heart.",
            "Serve warm in a 4 oz Gibraltar glass.",
        ),
        CuratedPairing(
            "hoyo_epicure_2", "japanese_whisky", 96, "Incense & Cedar",
            "Mizunara sandalwood and yuzu zest intertwine effortlessly with Hoyo's fragrant Cuban cedar and floral honey.",
            "Serve neat or with a tiny dash of cool spring water.",
        ),

        // Medium Classics & Cubans
        CuratedPairing(
            "cohiba_siglo6", "xo_cognac", 98, "Grand Cru Synergy",
            "Triple-fermented Manuka honey and cedar cream in the Siglo VI find aristocratic depth in XO Cognac's candied orange and rancio oak.",
            "Hand-warm a tulip snifter gently to unlock floral citrus aromatics.",
        ),
        CuratedPairing(
            "cohiba_siglo6", "brut_champagne", 96, "Honey & Brioche",
            "Vintage Champagne's toasted brioche and crisp acidity highlight Cohiba's lemongrass and creamy cedar layers.",
            "Serve at 46°F alongside the cigar's honeyed opening third.",
        ),
        CuratedPairing(
            "cohiba_siglo6", "earl_grey_creme", 95, "Zero-Proof Luxury",
            "Bergamot citrus and vanilla cream mirror Siglo VI's lemongrass and Manuka honey poise.",
            "Sip warm from a porcelain cup during a leisurely afternoon smoke.",
        ),
        CuratedPairing(
            "fuente_short_story", "xo_cognac", 95, "Spiced Rancio",
            "African Cameroon's sweet cedar and nutmeg spice resonate beautifully with XO Cognac's hazelnut praline and orange peel.",
            "Ideal 40-minute pairing—pour a 1.5 oz snifter neat alongside the tapered foot.",
        ),
        CuratedPairing(
            "fuente_short_story", "artisanal_root_beer", 96, "Zero-Proof Botanical",
            "Sassafras bark, anise, and vanilla echo the Cameroon leaf's trademark sweet-spicy cedar character.",
            "Serve ice-cold in a frosted glass; the carbonation clears the palate every few puffs.",
        ),
        CuratedPairing(
            "ryj_1875", "speyside_sherry", 92, "Oak & Walnut",
            "Indonesian Sumatra's toasted oak and walnut notes pair effortlessly with Speyside's nutty sherry cask finish.",
            "Serve neat at 64°F; pairs well through the hearty Robusto middle third.",
        ),
        CuratedPairing(
            "ryj_1875", "fiery_ginger_beer", 91, "Crisp Palate Reset",
            "Sharp ginger snap and lime zest slice cleanly through woody Sumatra tannins and refresh the palate.",
            "Serve over crushed ice with a fresh expressed lime twist.",
        ),
        CuratedPairing(
            "montecristo_no2", "xo_cognac", 98, "Grand Cru Benchmark",
            "Cinnamon, aged cedar, and orange zest in the Cuban torpedo find their aristocratic soulmate in Grande Champagne rancio oak and candied citrus.",
            "Pour neat at 68°F; savor slowly as the torpedo narrows and concentrates flavor.",
        ),
        CuratedPairing(
            "montecristo_no2", "cuban_cafecito", 97, "Zero-Proof Heritage",
            "Whipped demerara crema and syrupy dark espresso double down on Montecristo's cocoa-espresso heart.",
            "Serve hot in a small porcelain demitasse; take short sips between puffs.",
        ),
        CuratedPairing(
            "partagas_d4", "manhattan_cocktail", 96, "Rye & Cuban Earth",
            "100-proof rye spice and sweet vermouth cherry frame Partagás's bold black pepper, cedar, and savory leather.",
            "Stirred with a Luxardo cherry in a chilled coupe.",
        ),
        CuratedPairing(
            "partagas_d4", "double_ristretto", 95, "Zero-Proof Muscle",
            "Unsweetened dark ristretto crema matches Partagás Serie D's dense espresso-and-pepper authority.",
            "Pull a fresh double shot right before lighting the Robusto.",
        ),

        // Medium-Full & Full Legends
        CuratedPairing(
            "davidoff_late_hour", "islay_peated", 97, "Cask-Aged Mirror",
            "Aged six months in Scotch whisky casks, The Late Hour shares Islay malt's smoky oak, black pepper, and dark caramel DNA.",
            "Pour neat with two drops of water to open sweet malt beneath the peat.",
        ),
        CuratedPairing(
            "davidoff_late_hour", "smoked_old_fashioned", 96, "Lounge Gold Standard",
            "Wood-smoked maple and orange bitters echo the Late Hour's whisky-cask oak and dark chocolate core.",
            "Serve over a single slow-melting crystal sphere.",
        ),
        CuratedPairing(
            "davidoff_late_hour", "zero_proof_old_fashioned", 96, "Zero-Proof Oak Ritual",
            "Non-alcoholic charred oak and orange bitters deliver the exact evening lounge ceremony beside Scotch-aged tobacco.",
            "Express a fresh orange twist over a large rock.",
        ),
        CuratedPairing(
            "my_father_antillas", "venezuelan_rum_12", 95, "Sweet Tames Spice",
            "Silky molasses and vanilla oak round off the Antillas' bold white pepper opening into warm baking spice and cocoa.",
            "Enjoy neat; the rum's sweetness shines especially in the peppery first third.",
        ),
        CuratedPairing(
            "my_father_antillas", "masala_chai", 94, "Zero-Proof Spice",
            "Steamed milk softens the Nicaraguan pepper while cardamom and ginger harmonize with nutmeg and cedar.",
            "Serve piping hot; re-warm if needed for the 75-minute Toro session.",
        ),
        CuratedPairing(
            "oliva_melanio", "tawny_port_20", 97, "Velvet Fig & Cacao",
            "Roasted fig, walnut, and butterscotch in 20-Year Tawny Port unlock hidden salted-caramel and dark fruit layers in the Melanio.",
            "Serve slightly chilled at 56°F in a stemmed Port glass.",
        ),
        CuratedPairing(
            "oliva_melanio", "smoked_maple_cold_brew", 96, "Zero-Proof Mocha",
            "Low-acid cold brew and smoky maple foam mirror the Melanio's espresso-and-caramel elegance.",
            "Drink cold alongside the creamy second third.",
        ),
        CuratedPairing(
            "fuente_opusx", "anejo_tequila", 95, "Agave & Rosado Fire",
            "Oak-aged agave caramel and cinnamon tame OpusX's commanding red peppercorn while citrus zest illuminates its dried fig notes.",
            "Serve neat at 65°F in a copita; match slow sips with slow draws.",
        ),
        CuratedPairing(
            "fuente_opusx", "belgian_quadrupel", 96, "Abbey Fig & Spice",
            "Dark candi sugar, stewed fig, and Trappist clove spice embrace Rosado's dried stone fruit and red pepper fire.",
            "Pour at 52°F into a stemmed chalice.",
        ),
        CuratedPairing(
            "fuente_opusx", "fiery_ginger_beer", 93, "Zero-Proof Spark",
            "Bright key lime and ginger bite provide crisp contrast against OpusX's dense spice and resinous cedar.",
            "Serve chilled over clear ice; refreshes the palate between thirds.",
        ),
        CuratedPairing(
            "fuente_anejo", "xo_cognac", 98, "Barrel Siblings",
            "Wrapped in Broadleaf aged in Cognac barrels, the Añejo locks effortlessly with Grande Champagne oak, raisin, and dark cocoa.",
            "Pour neat in a tulip brandy glass.",
        ),
        CuratedPairing(
            "fuente_anejo", "cherry_sarsaparilla", 95, "Zero-Proof Cherry Wood",
            "Dark cherry fruit and root spice echo the Añejo's Cognac-soaked Broadleaf raisin sweetness.",
            "Serve cold over cracked ice.",
        ),
        CuratedPairing(
            "alec_prensado", "imperial_coffee_stout", 95, "Roast & Leather",
            "Roasted barley, espresso, and dark cocoa in the stout match the Honduran Corojo's coffee-and-saddle-leather backbone.",
            "Pour into a snifter at 52°F—too cold masks the chocolate aromatics.",
        ),
        CuratedPairing(
            "padron_1964", "bourbon_single_barrel", 98, "Iconic Oak & Cocoa",
            "Bittersweet cocoa and molasses in the 1964 Maduro interlock seamlessly with single-barrel burnt caramel and charred white oak.",
            "Serve neat or over one clear sphere; an unmistakable evening standard.",
        ),
        CuratedPairing(
            "padron_1964", "cuban_cafecito", 97, "Zero-Proof Perfection",
            "Demerara espuma softens Padrón's black pepper opening while intense espresso locks arms with bittersweet Maduro cocoa.",
            "Serve hot in a demitasse right as you toast the foot.",
        ),
        CuratedPairing(
            "padron_1926_no9", "px_sherry", 98, "Black Raisin & Fudge",
            "Sun-dried Pedro Ximénez raisin nectar coats the palate and elevates 1926's intense dark mocha and black pepper.",
            "Sip in 2 oz chilled pours from a Sherry copita.",
        ),
        CuratedPairing(
            "padron_1926_no9", "mexican_hot_chocolate", 96, "Zero-Proof Fudge & Spice",
            "Stone-ground cacao, Ceylon cinnamon, and roasted almond tame the 1926's bold pepper punch into dessert-like luxury.",
            "Serve frothy and warm in a ceramic mug.",
        ),
        CuratedPairing(
            "liga_privada_9", "islay_peated", 95, "Smoke Meets Ember",
            "Broadleaf Oscuro's dense earth and blackstrap molasses hold their ground effortlessly beside campfire peat and sea brine.",
            "Add 2 drops of water to the Islay malt to unlock sweet caramel beneath the peat.",
        ),
        CuratedPairing(
            "liga_privada_9", "imperial_coffee_stout", 97, "Midnight Velvet",
            "Creamy oats and bitter-sweet espresso in the barrel-aged stout amplify Liga No. 9's thick smoke and roasted coffee punch.",
            "Serve at 52°F in a snifter; let the stout warm slightly as the cigar deepens.",
        ),
        CuratedPairing(
            "liga_privada_9", "drinking_cacao", 96, "Zero-Proof Decadence",
            "75% dark cacao and subtle chili warmth turn the Oscuro's heavy mineral earth into silky chocolate truffle.",
            "Serve warm in a 4 oz cup; ideal for tempering the bold final third.",
        ),
        CuratedPairing(
            "foundation_tabernacle", "espresso_martini", 96, "Velvet Crema Match",
            "Frothy chilled espresso and dark liqueur lift Tabernacle's chocolate fudge and dense Broadleaf earth.",
            "Shake hard with fresh espresso for thick crema.",
        ),
        CuratedPairing(
            "foundation_tabernacle", "aged_puerh_tea", 95, "Zero-Proof Forest Earth",
            "10-year aged Pu-erh's velvet forest-floor sweetness mirrors Broadleaf mineral earth with zero astringency.",
            "Brew gongfu-style at 205°F in a clay teapot.",
        ),
        CuratedPairing(
            "bolivar_belicosos", "vsop_armagnac", 96, "Rustic Fire & Fruit",
            "Gascon oak and stewed prune in Bas-Armagnac meet Bolívar's heavy mineral earth and espresso with rugged poise.",
            "Serve neat at 66°F.",
        ),
        CuratedPairing(
            "undercrown_maduro", "cafe_de_olla", 96, "Zero-Proof San Andrés",
            "Mexican piloncillo cane and cinnamon coffee embrace San Andrés Maduro's natural dark chocolate sweetness.",
            "Serve hot in a stoneware mug.",
        ),
        CuratedPairing(
            "cao_flathead_v660", "bourbon_single_barrel", 97, "Broadleaf & Barrel Oak",
            "Single-barrel burnt caramel and charred oak wrap effortlessly around Flathead V660's thick molasses and dark chocolate.",
            "Pour 2 oz over a large ice sphere to keep pace with the 95-minute Gordo.",
        ),
        CuratedPairing(
            "cao_flathead_v660", "cuban_cafecito", 96, "Zero-Proof Espresso Bolt",
            "Syrupy Cuban espresso and demerara espuma double down on Flathead's bittersweet cocoa core.",
            "Sip hot in a demitasse right as the second third opens up.",
        ),
        CuratedPairing(
            "cohiba_red_dot", "xo_cognac", 95, "Cameroon & Rancio",
            "West African Cameroon's sweet cedar and toasted almond mingle gracefully with Grande Champagne hazelnut and orange peel.",
            "Serve neat in a hand-warmed tulip glass.",
        ),
        CuratedPairing(
            "cohiba_black_supremo", "imperial_coffee_stout", 96, "Maduro Velvet",
            "Roasted barley and dark chocolate oats amplify Cohiba Black's three-year aged Broadleaf cocoa richness.",
            "Pour at 52°F into a snifter.",
        ),
        CuratedPairing(
            "macanudo_inspirado_orange", "anejo_tequila", 94, "Citrus & Spice Lift",
            "Oak-aged agave caramel and white pepper match Inspirado Orange's zesty Honduran cedar and orange peel notes.",
            "Sip neat in a copita alongside the Robusto.",
        ),
        CuratedPairing(
            "alec_magic_toast", "tawny_port_20", 97, "Caramel & Walnut",
            "20-Year Tawny Port's butterscotch and roasted walnut turn Magic Toast's Honduran Maduro into liquid dessert.",
            "Serve slightly chilled at 56°F.",
        ),
        CuratedPairing(
            "diesel_whiskey_row_sherry", "speyside_sherry", 98, "Sherry Cask Twins",
            "Aged in Rabbit Hole PX Sherry casks, this Diesel shares figs, dried fruit, and sherry-oak DNA directly with Speyside malt.",
            "Pour neat at 64°F in a Glencairn.",
        ),
        CuratedPairing(
            "diesel_unholy_cocktail", "rye_whiskey_100", 95, "Full-Throttle Spice",
            "100-proof cracked rye pepper and charred oak stand shoulder-to-shoulder with Pennsylvania Broadleaf intensity.",
            "Serve over one ice cube to open the spice.",
        ),
        CuratedPairing(
            "man_o_war_virtue", "roasted_oolong", 95, "Zero-Proof Cream & Cedar",
            "Charcoal-baked stone fruit and mineral oolong preserve Virtue's delicate vanilla cream and toasted almond.",
            "Steep warm at 195°F for a calm afternoon smoke.",
        ),
        CuratedPairing(
            "man_o_war_armada", "smoked_old_fashioned", 96, "Oak Chest Synergy",
            "Wood-smoked maple and orange bitters highlight Armada's aged-in-cedar dark mocha and black cherry nuances.",
            "Serve over a single crystal ice sphere.",
        ),
        CuratedPairing(
            "ave_maria_argentum", "drinking_cacao", 97, "Zero-Proof Dark Fudge",
            "75% stone-ground cacao mirrors Argentum's Broadleaf dark fudge, molasses, and black cherry finish.",
            "Serve warm in a 4 oz sipping cup.",
        ),
        CuratedPairing(
            "nica_libre_1990", "cafe_de_olla", 96, "Zero-Proof San Andrés",
            "Cinnamon-spiced Mexican cane coffee amplifies the 1990 Anniversary's San Andrés cocoa and molasses.",
            "Serve hot in an earthenware mug.",
        ),
        CuratedPairing(
            "padron_family_reserve_45", "cask_strength_bourbon", 98, "Uncut Barrel & Maduro",
            "High-proof charred stave caramel and dark cherry wrap effortlessly around 10-year aged Estelí dark fudge and pepper.",
            "Pour 1.5 oz neat with two drops of spring water.",
        ),
        CuratedPairing(
            "perdomo_double_aged_maduro", "port_finished_bourbon", 96, "Oak & Berry Velvet",
            "Ruby Port blackberry jam mirrors the 12-year bourbon-barrel Maduro's dark cocoa and sweet charred white oak.",
            "Sip neat at 65°F in a Glencairn.",
        ),
        CuratedPairing(
            "fuente_don_carlos_no2", "highland_scotch_18", 97, "Cameroon & Oloroso",
            "Seville orange marmalade and walnut oak elevate Don Carlos's roasted almond and sweet cedar finish.",
            "Pour neat in a copita alongside the tapered torpedo.",
        ),
        CuratedPairing(
            "foundation_metapa_maduro", "extra_anejo_tequila", 97, "Agave & Sumatra Cacao",
            "Oak-rested blue agave caramel and cinnamon amplify Metapa's Sumatra dark cacao and black cherry nuances.",
            "Sip slowly neat at room temperature.",
        ),
        CuratedPairing(
            "casa_turrent_1880_oscuro", "demerara_navy_rum", 96, "Molasses & Tuxtla Earth",
            "Wooden-still blackstrap treacle and clove smooth Tuxtla mineral earth into rich Mexican dark chocolate.",
            "Serve neat in a warm snifter.",
        ),
        CuratedPairing(
            "selected_atabey_divinos", "vsop_cognac", 98, "Cedar Incense & Apricot",
            "Five-year cedar-aged Costa Rican cream melts seamlessly into Fine Champagne apricot blossom and Limousin oak.",
            "Pour 1.5 oz in a tulip glass.",
        ),
        CuratedPairing(
            "my_father_el_centurion_h2k", "vieux_carre", 96, "Spice & Herbal Honey",
            "Rye pepper and cognac raisin framed by herbal Bénédictine balance El Centurión's Habano 2000 baking spice.",
            "Stir over a large crystal ice sphere.",
        ),
        CuratedPairing(
            "crowned_heads_four_kicks", "sazerac_cocktail", 95, "Rye & Anise Snap",
            "Chilled rye bitters and star anise cut cleanly across Ecuadorian Habano cedar and baking spice.",
            "Serve neat in a chilled rocks glass.",
        ),
        CuratedPairing(
            "por_larranaga_petit_corona", "madeira_malmsey_15", 97, "Caramel & Madeira Elegance",
            "Sun-warmed Malmsey Madeira's burnt sugar, candied walnut, and crisp acidity harmonize brilliantly with Por Larrañaga's signature caramel cedar.",
            "Serve lightly chilled at 56°F in a tulip wine glass alongside a slow draw.",
        ),
        CuratedPairing(
            "ramon_allones_specially_selected", "indian_single_malt_sherry", 97, "Sherry Oak & Cuban Fig",
            "Vibrant tropical-aged Oloroso single malt unlocks deep dried fig, cocoa nibs, and spiced floral cedar in the classic RASS Robusto.",
            "Pour neat at 64°F in a Glencairn; swirl gently to open raisin and spice aromatics.",
        ),
        CuratedPairing(
            "casdagli_traditional_cotton_tail", "lowland_triple_malt", 96, "Botanical Blossom Harmony",
            "Triple-distilled Scottish orchard fruit and honeysuckle cradle Casdagli's delicate Ecuadorian Cotui cedar and almond silkiness.",
            "Serve neat at room temperature to preserve delicate citrus and floral layers.",
        ),
        CuratedPairing(
            "patina_maduro_toro", "american_single_malt_oak", 96, "Malted Char & Broadleaf",
            "Roasted chocolate barley malt and new charred oak amplify Patina Maduro's dense dark cocoa, black cherry, and espresso core.",
            "Pour over one crystal sphere to unlock sweet toasted-grain undertones.",
        ),
        CuratedPairing(
            "espinosa_laranja_corona", "finished_rum_cask_rye", 96, "Orange Blossom & Rum Rye",
            "Molasses-cask rye spice locks directly into Laranja's vibrant blood-orange zest, toasted cedar, and creamy café crema.",
            "Serve neat in a rocks glass; notice how citrus oils brighten every puff.",
        ),
        CuratedPairing(
            "curivari_buenaventura_p600", "barbados_xo_rum", 95, "Cubanesque Cane & Cedar",
            "Dry charred bourbon-cask Barbados rum balances coconut, roasted vanilla, and toasted nut seamlessly with Buenaventura's Cuban-seed earth.",
            "Sip neat in a tulip snifter at 66°F.",
        ),
        CuratedPairing(
            "juan_lopez_seleccion_no2", "hors_dage_cognac", 97, "Rancio & Seville Citrus",
            "Decades-aged Grande Champagne rancio oak and candied tangerine lift Juan López's bright citrus blossom and sweet milk-chocolate earth.",
            "Hand-warm a small brandy tulip glass slowly throughout the Robusto session.",
        ),
        CuratedPairing(
            "dapper_cubo_maduro", "black_russian_espresso", 95, "Velvet Roast Contrast",
            "Chilled coffee liqueur over crystal ice cushions Cubo Maduro's chocolate fudge, cedar, and lively Nicaraguan pepper.",
            "Stir over a large ice cube and take short sips between draws.",
        ),
    )

    fun cigarById(id: String): Cigar? = cigarMap[id]

    fun beverageById(id: String): Beverage? = beverageMap[id]

    fun evaluatePairing(
        cigar: Cigar,
        beverage: Beverage,
        ratingSummary: PairingRatingSummary? = null,
        strategy: PairingStrategy = PairingStrategy.BALANCED,
    ): PairingSuggestion {
        val curated = curatedPairings.find { it.cigarId == cigar.id && it.beverageId == beverage.id }
        return PairingEngine.evaluate(
            cigar = cigar,
            beverage = beverage,
            curated = curated,
            ratingSummary = ratingSummary,
            strategy = strategy,
        )
    }

    /**
     * Always returns top Alcoholic AND top Zero-Proof pairings for every cigar in the catalog.
     */
    fun pairingsForCigar(
        cigarId: String,
        ratingsByPairingId: Map<String, PairingRatingSummary>,
        strategy: PairingStrategy = PairingStrategy.BALANCED,
    ): List<PairingSuggestion> {
        val cigar = cigarById(cigarId) ?: return emptyList()
        val curatedMap = curatedPairings
            .filter { it.cigarId == cigarId }
            .associateBy { it.beverageId }

        val allEvaluated = beverages.map { bev ->
            evaluatePairing(
                cigar = cigar,
                beverage = bev,
                ratingSummary = ratingsByPairingId[buildPairingId(cigar.id, bev.id)],
                strategy = strategy,
            )
        }

        val comparator = if (strategy == PairingStrategy.BALANCED) {
            compareByDescending<PairingSuggestion> { curatedMap.containsKey(it.beverage.id) }
                .thenByDescending { it.matchScore }
        } else {
            compareByDescending<PairingSuggestion> { it.matchScore }
                .thenByDescending { curatedMap.containsKey(it.beverage.id) }
        }

        val topAlcoholic = allEvaluated
            .filter { it.beverage.isAlcoholic }
            .sortedWith(comparator)
            .take(5)

        val topZeroProof = allEvaluated
            .filter { !it.beverage.isAlcoholic }
            .sortedWith(comparator)
            .take(5)

        return (topAlcoholic + topZeroProof).sortedByDescending { it.matchScore }
    }

    /**
     * Always returns top Cigar pairings for every beverage in the catalog.
     */
    fun pairingsForBeverage(
        beverageId: String,
        ratingsByPairingId: Map<String, PairingRatingSummary>,
        strategy: PairingStrategy = PairingStrategy.BALANCED,
    ): List<PairingSuggestion> {
        val bev = beverageById(beverageId) ?: return emptyList()
        val curatedSet = curatedPairings
            .filter { it.beverageId == beverageId }
            .map { it.cigarId }
            .toSet()

        val comparator = if (strategy == PairingStrategy.BALANCED) {
            compareByDescending<PairingSuggestion> { it.cigar.id in curatedSet }
                .thenByDescending { it.matchScore }
        } else {
            compareByDescending<PairingSuggestion> { it.matchScore }
                .thenByDescending { it.cigar.id in curatedSet }
        }

        return cigars
            .map { cigar ->
                evaluatePairing(
                    cigar = cigar,
                    beverage = bev,
                    ratingSummary = ratingsByPairingId[buildPairingId(cigar.id, bev.id)],
                    strategy = strategy,
                )
            }
            .sortedWith(comparator)
            .take(6)
    }

    fun allCuratedSuggestions(
        ratingsByPairingId: Map<String, PairingRatingSummary>,
        strategy: PairingStrategy = PairingStrategy.BALANCED,
    ): List<PairingSuggestion> {
        return curatedPairings.mapNotNull { cp ->
            val cigar = cigarById(cp.cigarId) ?: return@mapNotNull null
            val bev = beverageById(cp.beverageId) ?: return@mapNotNull null
            evaluatePairing(cigar, bev, ratingsByPairingId[cp.pairingId], strategy)
        }
    }
}
