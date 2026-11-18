package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.catalog.PairingCatalog
import com.example.data.history.CigarHistoryCatalog
import com.example.model.PairingRatingSummary
import com.example.ui.components.CigarHistoryLessonCard
import com.example.ui.components.FlavorNoteTag
import com.example.ui.components.FlavorRadarChart
import com.example.ui.components.PairingSuggestionCard
import com.example.ui.components.StrengthDotsMeter
import com.example.ui.components.WrapperSwatchPill

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CigarDetailScreen(
    cigarId: String,
    ratingsByPairingId: Map<String, PairingRatingSummary>,
    onBack: () -> Unit,
    onOpenPairing: (cigarId: String, beverageId: String) -> Unit,
    onQuickRate: (cigarId: String, beverageId: String) -> Unit,
) {
    val cigar = remember(cigarId) { PairingCatalog.cigarById(cigarId) }
    val releaseHistory = remember(cigarId) { CigarHistoryCatalog.releaseForCigar(cigarId) }
    val companyHistory = remember(cigar?.brand) {
        cigar?.let { CigarHistoryCatalog.companyForBrand(it.brand) }
    }
    val allPairings = remember(cigarId, ratingsByPairingId) {
        PairingCatalog.pairingsForCigar(cigarId, ratingsByPairingId)
    }
    val alcoholicPairings = remember(allPairings) { allPairings.filter { it.beverage.isAlcoholic } }
    val zeroProofPairings = remember(allPairings) { allPairings.filter { !it.beverage.isAlcoholic } }

    if (cigar == null) {
        onBack()
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cigar.name, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${cigar.brand.uppercase()} · ${cigar.origin.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            StrengthDotsMeter(
                                filledDots = cigar.strength.dots,
                                label = cigar.strength.label,
                            )
                        }

                        Text(
                            text = cigar.name,
                            style = MaterialTheme.typography.headlineLarge,
                        )
                        Text(
                            text = "${cigar.vitola} (${cigar.dimensions}) · ~${cigar.smokeTimeMinutes} min smoke",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        WrapperSwatchPill(
                            wrapperShade = cigar.wrapperShade,
                            wrapperName = "${cigar.wrapper} (${cigar.wrapperShade.label})",
                        )

                        Text(
                            text = cigar.summary,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            cigar.tastingNotes.forEach { note ->
                                FlavorNoteTag(text = note)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "8-Axis Flavor Profile",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        FlavorRadarChart(
                            primaryAxes = cigar.flavorAxes,
                            primaryLabel = cigar.name,
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Tasting Progression by Third",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = cigar.thirdProgression.first,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = cigar.thirdProgression.second,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = cigar.thirdProgression.third,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            if (releaseHistory != null || companyHistory != null) {
                item {
                    CigarHistoryLessonCard(
                        cigar = cigar,
                        releaseHistory = releaseHistory,
                        companyHistory = companyHistory,
                    )
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalBar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Best Alcoholic Pairings",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            }

            items(alcoholicPairings, key = { "alc_${it.pairingId}" }) { suggestion ->
                PairingSuggestionCard(
                    suggestion = suggestion,
                    highlightBeverageFirst = true,
                    onOpenPairing = { onOpenPairing(suggestion.cigar.id, suggestion.beverage.id) },
                    onQuickRate = { onQuickRate(suggestion.cigar.id, suggestion.beverage.id) },
                )
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalCafe,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                    Text(
                        text = "Best Zero-Proof Pairings",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }

            items(zeroProofPairings, key = { "na_${it.pairingId}" }) { suggestion ->
                PairingSuggestionCard(
                    suggestion = suggestion,
                    highlightBeverageFirst = true,
                    onOpenPairing = { onOpenPairing(suggestion.cigar.id, suggestion.beverage.id) },
                    onQuickRate = { onQuickRate(suggestion.cigar.id, suggestion.beverage.id) },
                )
            }
        }
    }
}
