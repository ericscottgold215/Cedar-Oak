package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.catalog.PairingCatalog
import com.example.model.Cigar
import com.example.model.StrengthLevel
import com.example.model.WrapperShade
import com.example.ui.components.FlavorNoteTag
import com.example.ui.components.StrengthDotsMeter
import com.example.ui.components.WrapperSwatchPill

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CigarsScreen(
    searchQuery: String,
    strengthFilter: StrengthLevel?,
    onSearchQueryChange: (String) -> Unit,
    onStrengthFilterChange: (StrengthLevel?) -> Unit,
    onOpenCigar: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var wrapperFilter by remember { mutableStateOf<WrapperShade?>(null) }
    var brandFilter by remember { mutableStateOf<String?>(null) }
    val allBrands = remember {
        PairingCatalog.cigars.map { it.brand }.distinct().sorted()
    }

    val filteredCigars = remember(searchQuery, strengthFilter, wrapperFilter, brandFilter) {
        PairingCatalog.cigars.filter { cigar ->
            val matchesStrength = strengthFilter == null || cigar.strength == strengthFilter
            val matchesWrapper = wrapperFilter == null || cigar.wrapperShade == wrapperFilter
            val matchesBrand = brandFilter == null || cigar.brand == brandFilter
            val q = searchQuery.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                cigar.name.lowercase().contains(q) ||
                cigar.brand.lowercase().contains(q) ||
                cigar.wrapper.lowercase().contains(q) ||
                cigar.origin.lowercase().contains(q) ||
                cigar.tastingNotes.any { it.lowercase().contains(q) }
            matchesStrength && matchesWrapper && matchesBrand && matchesQuery
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(MaterialTheme.shapes.large),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_cigar_wrappers),
                    contentDescription = stringResource(R.string.cd_cigar_wrappers),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xFF120D0B).copy(alpha = 0.86f))
                            )
                        ),
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                ) {
                    Text(
                        text = "The Humidor (${PairingCatalog.cigars.size} Cigars)",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color(0xFFF5ECE4),
                    )
                    Text(
                        text = "Cuban, Nicaraguan, Dominican & Honduran blends across ${allBrands.size} brands.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD8C9BC),
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search cigar, brand, wrapper, origin, or note...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cigar_search_input"),
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = brandFilter == null,
                        onClick = { brandFilter = null },
                        label = { Text("All Brands") },
                    )
                }
                items(allBrands) { brand ->
                    FilterChip(
                        selected = brandFilter == brand,
                        onClick = {
                            brandFilter = if (brandFilter == brand) null else brand
                        },
                        label = { Text(brand) },
                    )
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = strengthFilter == null,
                        onClick = { onStrengthFilterChange(null) },
                        label = { Text("All Strengths") },
                    )
                }
                items(StrengthLevel.entries) { strength ->
                    FilterChip(
                        selected = strengthFilter == strength,
                        onClick = {
                            onStrengthFilterChange(if (strengthFilter == strength) null else strength)
                        },
                        label = { Text(strength.label) },
                    )
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = wrapperFilter == null,
                        onClick = { wrapperFilter = null },
                        label = { Text("All Wrappers") },
                    )
                }
                items(WrapperShade.entries) { shade ->
                    FilterChip(
                        selected = wrapperFilter == shade,
                        onClick = {
                            wrapperFilter = if (wrapperFilter == shade) null else shade
                        },
                        label = { Text(shade.label) },
                    )
                }
            }
        }

        item {
            Text(
                text = "Showing ${filteredCigars.size} of ${PairingCatalog.cigars.size} cigars",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (filteredCigars.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "No matching cigars",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Try clearing the search query or selecting a broader strength level.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            items(filteredCigars, key = { it.id }) { cigar ->
                CigarListCard(
                    cigar = cigar,
                    onClick = { onOpenCigar(cigar.id) },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CigarListCard(
    cigar: Cigar,
    onClick: () -> Unit,
) {
    val pairingCount = remember(cigar.id) {
        PairingCatalog.curatedPairings.count { it.cigarId == cigar.id }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .testTag("cigar_card_${cigar.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = cigar.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${cigar.vitola} (${cigar.dimensions}) · ~${cigar.smokeTimeMinutes} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            WrapperSwatchPill(
                wrapperShade = cigar.wrapperShade,
                wrapperName = cigar.wrapper,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                cigar.tastingNotes.take(3).forEach { note ->
                    FlavorNoteTag(text = note)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$pairingCount curated pairings (Spirits & Zero-Proof)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "View cigar profile and pairings",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
