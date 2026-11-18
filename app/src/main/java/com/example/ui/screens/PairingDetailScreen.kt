package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.catalog.PairingCatalog
import com.example.data.history.CigarHistoryCatalog
import com.example.model.PairingRatingSummary
import com.example.model.PairingReview
import com.example.model.buildPairingId
import com.example.ui.components.AverageRatingBadge
import com.example.ui.components.BeverageProofBadge
import com.example.ui.components.FlavorNoteTag
import com.example.ui.components.FlavorRadarChart
import com.example.ui.components.MatchScorePill
import com.example.ui.components.PairingCigarHistoryPreviewCard
import com.example.ui.components.PairingLogicBreakdownCard
import com.example.ui.components.RatingBreakdownCard
import com.example.ui.components.ReviewItemCard
import com.example.ui.components.WrapperSwatchPill

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PairingDetailScreen(
    cigarId: String,
    beverageId: String,
    ratingsByPairingId: Map<String, PairingRatingSummary>,
    reviews: List<PairingReview>,
    onBack: () -> Unit,
    onOpenCigar: (String) -> Unit,
    onOpenBeverage: (String) -> Unit,
    onOpenRateComposer: (existingReview: PairingReview?) -> Unit,
    onDeleteReview: (Long) -> Unit,
) {
    val cigar = remember(cigarId) { PairingCatalog.cigarById(cigarId) }
    val beverage = remember(beverageId) { PairingCatalog.beverageById(beverageId) }
    val pairingId = remember(cigarId, beverageId) { buildPairingId(cigarId, beverageId) }

    if (cigar == null || beverage == null) {
        onBack()
        return
    }

    val releaseHistory = remember(cigarId) { CigarHistoryCatalog.releaseForCigar(cigarId) }
    val companyHistory = remember(cigar.brand) { CigarHistoryCatalog.companyForBrand(cigar.brand) }
    val summary = ratingsByPairingId[pairingId]
    val suggestion = remember(cigar, beverage, summary) {
        PairingCatalog.evaluatePairing(cigar, beverage, summary)
    }
    val userOwnReview = remember(reviews) { reviews.firstOrNull { it.isUserOwn } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pairing Profile", style = MaterialTheme.typography.titleLarge) },
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
                actions = {
                    TextButton(
                        onClick = { onOpenRateComposer(userOwnReview) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("top_bar_rate_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (userOwnReview != null) "Edit Rating" else "Rate")
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
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                MatchScorePill(score = suggestion.matchScore)
                                BeverageProofBadge(beverage = beverage)
                            }
                            AverageRatingBadge(
                                averageRating = suggestion.averageRating,
                                reviewCount = suggestion.reviewCount,
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .clickable { onOpenCigar(cigar.id) }
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "CIGAR · ${cigar.brand.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = cigar.name,
                                style = MaterialTheme.typography.headlineMedium,
                            )
                            WrapperSwatchPill(
                                wrapperShade = cigar.wrapperShade,
                                wrapperName = "${cigar.wrapper} · ${cigar.strength.label}",
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .clickable { onOpenBeverage(beverage.id) }
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = "POUR · ${beverage.category.label.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (beverage.isAlcoholic) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.tertiary
                                },
                            )
                            Text(
                                text = beverage.name,
                                style = MaterialTheme.typography.headlineMedium,
                            )
                            Text(
                                text = "${beverage.subtitle} · ${beverage.glassware}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
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
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Flavor Overlay Comparison",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        FlavorRadarChart(
                            primaryAxes = cigar.flavorAxes,
                            primaryLabel = cigar.name,
                            primaryColor = MaterialTheme.colorScheme.primary,
                            secondaryAxes = beverage.flavorAxes,
                            secondaryLabel = beverage.name,
                            secondaryColor = if (beverage.isAlcoholic) {
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
                            text = "Sommelier Notes · ${suggestion.pairingStyle}",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = suggestion.whyItWorks,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        if (suggestion.bridgeNotes.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                suggestion.bridgeNotes.forEach { bridge ->
                                    FlavorNoteTag(text = "Bridge: $bridge")
                                }
                            }
                        }
                        Text(
                            text = "Serving Ritual: ${suggestion.servingTip}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            suggestion.breakdown?.let { breakdown ->
                item {
                    PairingLogicBreakdownCard(breakdown = breakdown)
                }
            }

            if (releaseHistory != null || companyHistory != null) {
                item {
                    PairingCigarHistoryPreviewCard(
                        cigar = cigar,
                        releaseHistory = releaseHistory,
                        companyHistory = companyHistory,
                        onOpenCigarHistory = { onOpenCigar(cigar.id) },
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.label_rating_title),
                        style = MaterialTheme.typography.headlineMedium,
                    )

                    RatingBreakdownCard(
                        reviews = reviews,
                        averageRating = suggestion.averageRating,
                    )

                    Button(
                        onClick = { onOpenRateComposer(userOwnReview) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("rate_pairing_cta"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.RateReview,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (userOwnReview != null) {
                                stringResource(R.string.label_update_your_review)
                            } else {
                                stringResource(R.string.label_rate_this_pairing)
                            }
                        )
                    }
                }
            }

            if (reviews.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "Be the first to rate this pairing",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = "Tap \"Rate This Pairing\" above to leave 1–5 stars and tasting notes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                items(reviews, key = { it.id }) { review ->
                    ReviewItemCard(
                        review = review,
                        onEdit = if (review.isUserOwn) {
                            { onOpenRateComposer(review) }
                        } else null,
                        onDelete = if (review.isUserOwn) {
                            { onDeleteReview(review.id) }
                        } else null,
                    )
                }
            }
        }
    }
}
