package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.catalog.PairingCatalog
import com.example.model.BeverageFilter
import com.example.model.PairingStrategy
import com.example.model.PairingSuggestion
import com.example.ui.components.BeveragePickerSheet
import com.example.ui.components.CigarPickerSheet
import com.example.ui.components.PairingStrategySelectorRow
import com.example.ui.components.PairingSuggestionCard
import com.example.ui.theme.StarGold
import com.example.ui.viewmodel.MatcherDirection

@Composable
fun DiscoverScreen(
    matcherDirection: MatcherDirection,
    selectedCigarId: String,
    selectedBeverageId: String,
    beverageFilter: BeverageFilter,
    pairingStrategy: PairingStrategy,
    quickMatches: List<PairingSuggestion>,
    featuredPairings: List<PairingSuggestion>,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onDirectionChange: (MatcherDirection) -> Unit,
    onSelectCigar: (String) -> Unit,
    onSelectBeverage: (String) -> Unit,
    onBeverageFilterChange: (BeverageFilter) -> Unit,
    onPairingStrategyChange: (PairingStrategy) -> Unit,
    onOpenPairing: (cigarId: String, beverageId: String) -> Unit,
    onQuickRate: (cigarId: String, beverageId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCigarPicker by remember { mutableStateOf(false) }
    var showBeveragePicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            LoungeHeroHeader(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
            )
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "Instant Sommelier Matcher",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    MatcherDirection.entries.forEachIndexed { index, dir ->
                        SegmentedButton(
                            selected = matcherDirection == dir,
                            onClick = { onDirectionChange(dir) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = MatcherDirection.entries.size,
                            ),
                            modifier = Modifier.testTag("direction_${dir.name}"),
                        ) {
                            Text(
                                text = dir.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                if (matcherDirection == MatcherDirection.CIGAR_TO_DRINK) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val activeCigar = PairingCatalog.cigarById(selectedCigarId)
                        Text(
                            text = activeCigar?.let { "${it.brand} ${it.name}" } ?: "Select Cigar",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            onClick = { showCigarPicker = true },
                            modifier = Modifier.testTag("browse_all_cigars_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("All ${PairingCatalog.cigars.size} Cigars")
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(PairingCatalog.cigars, key = { it.id }) { cigar ->
                            val selected = cigar.id == selectedCigarId
                            Card(
                                modifier = Modifier
                                    .width(172.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable { onSelectCigar(cigar.id) }
                                    .border(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant
                                        },
                                        shape = MaterialTheme.shapes.medium,
                                    )
                                    .testTag("matcher_cigar_${cigar.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selected) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerLow
                                    },
                                ),
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(cigar.wrapperShade.swatchColor),
                                        )
                                        Text(
                                            text = cigar.strength.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                    Text(
                                        text = cigar.brand,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = cigar.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BeverageFilter.entries.forEach { filter ->
                            FilterChip(
                                selected = beverageFilter == filter,
                                onClick = { onBeverageFilterChange(filter) },
                                label = { Text(filter.label) },
                                leadingIcon = when (filter) {
                                    BeverageFilter.ALCOHOLIC -> {
                                        {
                                            Icon(
                                                imageVector = Icons.Filled.LocalBar,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                    BeverageFilter.NON_ALCOHOLIC -> {
                                        {
                                            Icon(
                                                imageVector = Icons.Filled.LocalCafe,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                    BeverageFilter.ALL -> null
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (filter == BeverageFilter.NON_ALCOHOLIC) {
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.primaryContainer
                                    },
                                ),
                                modifier = Modifier.testTag("discover_filter_${filter.name}"),
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val activeBev = PairingCatalog.beverageById(selectedBeverageId)
                        Text(
                            text = activeBev?.name ?: "Select Beverage",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            onClick = { showBeveragePicker = true },
                            modifier = Modifier.testTag("browse_all_beverages_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("All ${PairingCatalog.beverages.size} Drinks")
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(PairingCatalog.beverages, key = { it.id }) { bev ->
                            val selected = bev.id == selectedBeverageId
                            Card(
                                modifier = Modifier
                                    .width(184.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable { onSelectBeverage(bev.id) }
                                    .border(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant
                                        },
                                        shape = MaterialTheme.shapes.medium,
                                    )
                                    .testTag("matcher_beverage_${bev.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selected) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerLow
                                    },
                                ),
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = if (bev.isAlcoholic) bev.abvLabel else "Zero-Proof",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (bev.isAlcoholic) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.tertiary
                                        },
                                    )
                                    Text(
                                        text = bev.category.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = bev.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }

                PairingStrategySelectorRow(
                    selectedStrategy = pairingStrategy,
                    onStrategySelected = onPairingStrategyChange,
                )
            }
        }

        items(quickMatches, key = { "quick_${it.pairingId}" }) { suggestion ->
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                PairingSuggestionCard(
                    suggestion = suggestion,
                    highlightBeverageFirst = matcherDirection == MatcherDirection.CIGAR_TO_DRINK,
                    onOpenPairing = { onOpenPairing(suggestion.cigar.id, suggestion.beverage.id) },
                    onQuickRate = { onQuickRate(suggestion.cigar.id, suggestion.beverage.id) },
                )
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = StarGold,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "Top-Rated Community Pairings",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Text(
                    text = "Ranked by live 1–5 star ratings across alcoholic & zero-proof picks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        items(featuredPairings.take(8), key = { "featured_${it.pairingId}" }) { suggestion ->
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                PairingSuggestionCard(
                    suggestion = suggestion,
                    onOpenPairing = { onOpenPairing(suggestion.cigar.id, suggestion.beverage.id) },
                    onQuickRate = { onQuickRate(suggestion.cigar.id, suggestion.beverage.id) },
                )
            }
        }
    }

    if (showCigarPicker) {
        CigarPickerSheet(
            selectedCigarId = selectedCigarId,
            onSelectCigar = onSelectCigar,
            onDismiss = { showCigarPicker = false },
        )
    }

    if (showBeveragePicker) {
        BeveragePickerSheet(
            selectedBeverageId = selectedBeverageId,
            onSelectBeverage = onSelectBeverage,
            onDismiss = { showBeveragePicker = false },
        )
    }
}

@Composable
private fun LoungeHeroHeader(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp)
            .clip(MaterialTheme.shapes.large),
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_lounge_hero),
            contentDescription = stringResource(R.string.cd_lounge_hero),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color(0xFF120D0B).copy(alpha = 0.88f),
                        ),
                    )
                ),
        )
        IconButton(
            onClick = onToggleTheme,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .minimumInteractiveComponentSize()
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f))
                .testTag("theme_toggle_button"),
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = if (isDarkTheme) "Switch to parchment light mode" else "Switch to dark lounge mode",
                tint = Color(0xFFFFE0B2),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFE2A856).copy(alpha = 0.22f),
            ) {
                Text(
                    text = "${PairingCatalog.cigars.size} CIGARS · ${PairingCatalog.beverages.size} BEVERAGES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFE0B2),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayMedium,
                color = Color(0xFFF5ECE4),
            )
            Text(
                text = "Spirits, cocktails, wines, beers, zero-proof pours & ratings.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFD8C9BC),
            )
        }
    }
}
