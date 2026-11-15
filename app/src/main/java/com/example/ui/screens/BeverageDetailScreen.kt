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
import androidx.compose.material.icons.filled.Whatshot
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
import com.example.model.PairingRatingSummary
import com.example.ui.components.BeverageProofBadge
import com.example.ui.components.FlavorNoteTag
import com.example.ui.components.FlavorRadarChart
import com.example.ui.components.PairingSuggestionCard
import com.example.ui.components.StrengthDotsMeter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BeverageDetailScreen(
    beverageId: String,
    ratingsByPairingId: Map<String, PairingRatingSummary>,
    onBack: () -> Unit,
    onOpenPairing: (cigarId: String, beverageId: String) -> Unit,
    onQuickRate: (cigarId: String, beverageId: String) -> Unit,
) {
    val beverage = remember(beverageId) { PairingCatalog.beverageById(beverageId) }
    val cigarPairings = remember(beverageId, ratingsByPairingId) {
        PairingCatalog.pairingsForBeverage(beverageId, ratingsByPairingId)
    }

    if (beverage == null) {
        onBack()
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(beverage.name, style = MaterialTheme.typography.titleLarge) },
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
                            BeverageProofBadge(beverage = beverage)
                            StrengthDotsMeter(
                                filledDots = beverage.body.dots,
                                label = "${beverage.body.label} Body",
                            )
                        }

                        Text(
                            text = beverage.name,
                            style = MaterialTheme.typography.headlineLarge,
                        )
                        Text(
                            text = "${beverage.subtitle} · ${beverage.glassware} · ${beverage.servingTemp}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Text(
                            text = beverage.summary,
                            style = MaterialTheme.typography.bodyLarge,
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            beverage.tastingNotes.forEach { note ->
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
                            primaryAxes = beverage.flavorAxes,
                            primaryLabel = beverage.name,
                            primaryColor = if (beverage.isAlcoholic) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.tertiary
                            },
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
                            text = "Nose, Palate & Finish",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = beverage.palateBreakdown.first,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = beverage.palateBreakdown.second,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = beverage.palateBreakdown.third,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Whatshot,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Best Cigar Pairings",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            }

            items(cigarPairings, key = { "cigar_match_${it.pairingId}" }) { suggestion ->
                PairingSuggestionCard(
                    suggestion = suggestion,
                    highlightBeverageFirst = false,
                    onOpenPairing = { onOpenPairing(suggestion.cigar.id, suggestion.beverage.id) },
                    onQuickRate = { onQuickRate(suggestion.cigar.id, suggestion.beverage.id) },
                )
            }
        }
    }
}
