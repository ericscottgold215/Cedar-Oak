package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Cigar
import com.example.model.CigarCompanyHistory
import com.example.model.CigarReleaseHistory

@Composable
fun CigarHistoryLessonCard(
    cigar: Cigar,
    releaseHistory: CigarReleaseHistory?,
    companyHistory: CigarCompanyHistory?,
    modifier: Modifier = Modifier,
) {
    if (releaseHistory == null && companyHistory == null) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cigar_history_lesson_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoStories,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "History & Heritage Lesson",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (releaseHistory != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "THE CIGAR · ${cigar.name.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        HistoryYearBadge(text = "Launched ${releaseHistory.launchYear}")
                    }

                    HistoryFactRow(
                        icon = Icons.Filled.LocationOn,
                        label = "Origin & Factory",
                        value = releaseHistory.productionLocation,
                    )
                    HistoryFactRow(
                        icon = Icons.Filled.Person,
                        label = "Blender",
                        value = releaseHistory.blenderAndFactory,
                    )
                    Text(
                        text = releaseHistory.storyAndLegacy,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (releaseHistory != null && companyHistory != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            if (companyHistory != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "THE COMPANY · ${companyHistory.brand.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        HistoryYearBadge(text = "Est. ${companyHistory.foundedYear}")
                    }

                    HistoryFactRow(
                        icon = Icons.Filled.LocationOn,
                        label = "Location",
                        value = companyHistory.headquartersAndFactory,
                    )
                    HistoryFactRow(
                        icon = Icons.Filled.Person,
                        label = "Founder(s)",
                        value = companyHistory.founder,
                    )
                    HistoryFactRow(
                        icon = Icons.Filled.Business,
                        label = "Ownership",
                        value = companyHistory.currentOwner,
                    )
                    Text(
                        text = companyHistory.heritageStory,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun BrandHeritageBannerCard(
    companyHistory: CigarCompanyHistory,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable(companyHistory.brand) { mutableStateOf(true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { expanded = !expanded }
            .testTag("brand_heritage_banner"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "${companyHistory.brand} Heritage",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    HistoryYearBadge(text = "Est. ${companyHistory.foundedYear}")
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "Collapse brand history" else "Expand brand history",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HistoryFactRow(
                icon = Icons.Filled.LocationOn,
                label = "Location",
                value = companyHistory.headquartersAndFactory,
            )
            HistoryFactRow(
                icon = Icons.Filled.Business,
                label = "Owner",
                value = companyHistory.currentOwner,
            )

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    HistoryFactRow(
                        icon = Icons.Filled.Person,
                        label = "Founded by",
                        value = companyHistory.founder,
                    )
                    Text(
                        text = companyHistory.heritageStory,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun PairingCigarHistoryPreviewCard(
    cigar: Cigar,
    releaseHistory: CigarReleaseHistory?,
    companyHistory: CigarCompanyHistory?,
    onOpenCigarHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (releaseHistory == null && companyHistory == null) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoStories,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Cigar & House History",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                if (releaseHistory != null) {
                    HistoryYearBadge(text = releaseHistory.launchYear)
                }
            }

            if (companyHistory != null) {
                HistoryFactRow(
                    icon = Icons.Filled.CalendarToday,
                    label = "${companyHistory.brand} Founded",
                    value = "${companyHistory.foundedYear} by ${companyHistory.founder}",
                )
                HistoryFactRow(
                    icon = Icons.Filled.LocationOn,
                    label = "Location",
                    value = companyHistory.headquartersAndFactory,
                )
                HistoryFactRow(
                    icon = Icons.Filled.Business,
                    label = "Owner",
                    value = companyHistory.currentOwner,
                )
            }

            if (releaseHistory != null) {
                Text(
                    text = releaseHistory.storyAndLegacy,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            TextButton(
                onClick = onOpenCigarHistory,
                modifier = Modifier.minimumInteractiveComponentSize(),
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoStories,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Full ${cigar.brand} & Blend History")
            }
        }
    }
}

@Composable
private fun HistoryYearBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun HistoryFactRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(15.dp),
        )
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
