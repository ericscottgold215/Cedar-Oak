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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.model.Beverage
import com.example.model.BeverageCategory
import com.example.model.BeverageFilter
import com.example.ui.components.BeverageProofBadge
import com.example.ui.components.FlavorNoteTag
import com.example.ui.components.StrengthDotsMeter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DrinksScreen(
    beverageFilter: BeverageFilter,
    categoryFilter: BeverageCategory?,
    searchQuery: String,
    onBeverageFilterChange: (BeverageFilter) -> Unit,
    onCategoryFilterChange: (BeverageCategory?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onOpenBeverage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleCategories = remember(beverageFilter) {
        BeverageCategory.entries.filter { cat ->
            when (beverageFilter) {
                BeverageFilter.ALL -> true
                BeverageFilter.ALCOHOLIC -> cat.isAlcoholic
                BeverageFilter.NON_ALCOHOLIC -> !cat.isAlcoholic
            }
        }
    }

    val filteredDrinks = remember(beverageFilter, categoryFilter, searchQuery) {
        PairingCatalog.beverages.filter { bev ->
            val matchesProof = when (beverageFilter) {
                BeverageFilter.ALL -> true
                BeverageFilter.ALCOHOLIC -> bev.isAlcoholic
                BeverageFilter.NON_ALCOHOLIC -> !bev.isAlcoholic
            }
            val matchesCategory = categoryFilter == null || bev.category == categoryFilter
            val q = searchQuery.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                bev.name.lowercase().contains(q) ||
                bev.subtitle.lowercase().contains(q) ||
                bev.category.label.lowercase().contains(q) ||
                bev.tastingNotes.any { it.lowercase().contains(q) }
            matchesProof && matchesCategory && matchesQuery
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(MaterialTheme.shapes.large),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_pairing_drinks),
                    contentDescription = stringResource(R.string.cd_pairing_drinks),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xFF120D0B).copy(alpha = 0.88f))
                            )
                        ),
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                ) {
                    Text(
                        text = "The Cellar & Café (${PairingCatalog.beverages.size} Drinks)",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color(0xFFF5ECE4),
                    )
                    Text(
                        text = "Whiskey, rum, wine, beer, cocktails, coffee, tea, sodas & zero-proof.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD8C9BC),
                    )
                }
            }
        }

        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                BeverageFilter.entries.forEachIndexed { index, filter ->
                    SegmentedButton(
                        selected = beverageFilter == filter,
                        onClick = { onBeverageFilterChange(filter) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = BeverageFilter.entries.size,
                        ),
                        modifier = Modifier.testTag("drinks_proof_filter_${filter.name}"),
                    ) {
                        Text(filter.label)
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search whiskey, cocktail, wine, beer, coffee, tea, soda...") },
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
                    .testTag("drink_search_input"),
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = categoryFilter == null,
                        onClick = { onCategoryFilterChange(null) },
                        label = { Text("All Categories") },
                    )
                }
                items(visibleCategories) { cat ->
                    FilterChip(
                        selected = categoryFilter == cat,
                        onClick = {
                            onCategoryFilterChange(if (categoryFilter == cat) null else cat)
                        },
                        label = { Text(cat.label) },
                    )
                }
            }
        }

        item {
            Text(
                text = "Showing ${filteredDrinks.size} of ${PairingCatalog.beverages.size} beverages",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        items(filteredDrinks, key = { it.id }) { bev ->
            BeverageListCard(
                beverage = bev,
                onClick = { onOpenBeverage(bev.id) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BeverageListCard(
    beverage: Beverage,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .testTag("beverage_card_${beverage.id}"),
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
                BeverageProofBadge(beverage = beverage)
                StrengthDotsMeter(
                    filledDots = beverage.body.dots,
                    label = "${beverage.body.label} Body",
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = beverage.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${beverage.subtitle} · ${beverage.servingTemp}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                beverage.tastingNotes.take(3).forEach { note ->
                    FlavorNoteTag(text = note)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "View flavor profile & best cigar matches",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "View beverage profile and cigar pairings",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
