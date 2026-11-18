package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ai.configureAppCheck
import com.example.data.catalog.PairingCatalog
import com.example.model.PairingReview
import com.example.ui.components.RatingComposerSheet
import com.example.ui.screens.BeverageDetailScreen
import com.example.ui.screens.CigarDetailScreen
import com.example.ui.screens.CigarsScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.DrinksScreen
import com.example.ui.screens.PairingDetailScreen
import com.example.ui.screens.SynergyLabScreen
import com.example.ui.screens.TranscribeScreen
import com.example.ui.theme.CedarAndOakTheme
import com.example.ui.viewmodel.PairingViewModel
import kotlinx.coroutines.launch

private object AppRoutes {
    const val PAIRINGS = "pairings"
    const val CIGARS = "cigars"
    const val DRINKS = "drinks"
    const val SYNERGY = "synergy"
    const val TRANSCRIBE = "transcribe"
    const val CIGAR_DETAIL = "cigar/{cigarId}"
    const val BEVERAGE_DETAIL = "beverage/{beverageId}"
    const val PAIRING_DETAIL = "pairing/{cigarId}/{beverageId}"

    fun cigarDetail(cigarId: String) = "cigar/$cigarId"
    fun beverageDetail(beverageId: String) = "beverage/$beverageId"
    fun pairingDetail(cigarId: String, beverageId: String) = "pairing/$cigarId/$beverageId"
}

private data class TopLevelTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val TopLevelTabs = listOf(
    TopLevelTab(
        route = AppRoutes.PAIRINGS,
        label = "Pairings",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
    ),
    TopLevelTab(
        route = AppRoutes.CIGARS,
        label = "Cigars",
        selectedIcon = Icons.Filled.Whatshot,
        unselectedIcon = Icons.Outlined.Whatshot,
    ),
    TopLevelTab(
        route = AppRoutes.DRINKS,
        label = "Drinks",
        selectedIcon = Icons.Filled.LocalBar,
        unselectedIcon = Icons.Outlined.LocalBar,
    ),
    TopLevelTab(
        route = AppRoutes.SYNERGY,
        label = "Synergy",
        selectedIcon = Icons.Filled.CompareArrows,
        unselectedIcon = Icons.Outlined.CompareArrows,
    ),
    TopLevelTab(
        route = AppRoutes.TRANSCRIBE,
        label = "Transcribe",
        selectedIcon = Icons.Filled.GraphicEq,
        unselectedIcon = Icons.Outlined.GraphicEq,
    ),
)

private data class ActiveRateTarget(
    val cigarId: String,
    val beverageId: String,
    val existingReview: PairingReview? = null,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            val viewModel: PairingViewModel = viewModel()
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

            CedarAndOakTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    CedarAndOakApp(viewModel = viewModel, isDarkTheme = isDarkTheme)
                }
            }
        }
    }
}

@Composable
private fun CedarAndOakApp(
    viewModel: PairingViewModel,
    isDarkTheme: Boolean,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showTopLevelNav = currentRoute in TopLevelTabs.map { it.route }

    val ratingsByPairingId by viewModel.ratingsByPairingId.collectAsStateWithLifecycle()
    val allReviews by viewModel.allReviews.collectAsStateWithLifecycle()
    val userReviews by viewModel.userReviews.collectAsStateWithLifecycle()
    val beverageFilter by viewModel.beverageFilter.collectAsStateWithLifecycle()
    val pairingStrategy by viewModel.pairingStrategy.collectAsStateWithLifecycle()
    val lastReviewerName by viewModel.lastReviewerName.collectAsStateWithLifecycle()

    val matcherDirection by viewModel.matcherDirection.collectAsStateWithLifecycle()
    val selectedMatcherCigarId by viewModel.selectedMatcherCigarId.collectAsStateWithLifecycle()
    val selectedMatcherBeverageId by viewModel.selectedMatcherBeverageId.collectAsStateWithLifecycle()
    val quickMatches by viewModel.quickMatcherResults.collectAsStateWithLifecycle()
    val featuredPairings by viewModel.featuredPairings.collectAsStateWithLifecycle()

    val cigarSearchQuery by viewModel.cigarSearchQuery.collectAsStateWithLifecycle()
    val cigarStrengthFilter by viewModel.cigarStrengthFilter.collectAsStateWithLifecycle()
    val drinkSearchQuery by viewModel.drinkSearchQuery.collectAsStateWithLifecycle()
    val drinkCategoryFilter by viewModel.drinkCategoryFilter.collectAsStateWithLifecycle()

    val labCigarId by viewModel.labCigarId.collectAsStateWithLifecycle()
    val labBeverageId by viewModel.labBeverageId.collectAsStateWithLifecycle()
    val labSuggestion by viewModel.labSuggestion.collectAsStateWithLifecycle()

    var rateTarget by remember { mutableStateOf<ActiveRateTarget?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun navigateTopTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (showTopLevelNav && !useRail) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        containerColor = MaterialTheme.colorScheme.surface,
                    ) {
                        TopLevelTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label,
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("nav_${tab.route}"),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (showTopLevelNav && useRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ) {
                        TopLevelTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label,
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("rail_${tab.route}"),
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = AppRoutes.PAIRINGS,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 880.dp),
                    ) {
                        composable(AppRoutes.PAIRINGS) {
                            DiscoverScreen(
                                matcherDirection = matcherDirection,
                                selectedCigarId = selectedMatcherCigarId,
                                selectedBeverageId = selectedMatcherBeverageId,
                                beverageFilter = beverageFilter,
                                pairingStrategy = pairingStrategy,
                                quickMatches = quickMatches,
                                featuredPairings = featuredPairings,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { viewModel.toggleDarkTheme(isDarkTheme) },
                                onDirectionChange = viewModel::setMatcherDirection,
                                onSelectCigar = viewModel::selectMatcherCigar,
                                onSelectBeverage = viewModel::selectMatcherBeverage,
                                onBeverageFilterChange = viewModel::setBeverageFilter,
                                onPairingStrategyChange = viewModel::setPairingStrategy,
                                onOpenPairing = { cId, bId ->
                                    navController.navigate(AppRoutes.pairingDetail(cId, bId))
                                },
                                onQuickRate = { cId, bId ->
                                    rateTarget = ActiveRateTarget(cId, bId)
                                },
                            )
                        }

                        composable(AppRoutes.CIGARS) {
                            CigarsScreen(
                                searchQuery = cigarSearchQuery,
                                strengthFilter = cigarStrengthFilter,
                                onSearchQueryChange = viewModel::setCigarSearchQuery,
                                onStrengthFilterChange = viewModel::setCigarStrengthFilter,
                                onOpenCigar = { cigarId ->
                                    navController.navigate(AppRoutes.cigarDetail(cigarId))
                                },
                            )
                        }

                        composable(AppRoutes.DRINKS) {
                            DrinksScreen(
                                beverageFilter = beverageFilter,
                                categoryFilter = drinkCategoryFilter,
                                searchQuery = drinkSearchQuery,
                                onBeverageFilterChange = viewModel::setBeverageFilter,
                                onCategoryFilterChange = viewModel::setDrinkCategoryFilter,
                                onSearchQueryChange = viewModel::setDrinkSearchQuery,
                                onOpenBeverage = { bevId ->
                                    navController.navigate(AppRoutes.beverageDetail(bevId))
                                },
                            )
                        }

                        composable(AppRoutes.SYNERGY) {
                            SynergyLabScreen(
                                selectedCigarId = labCigarId,
                                selectedBeverageId = labBeverageId,
                                pairingStrategy = pairingStrategy,
                                labSuggestion = labSuggestion,
                                userReviews = userReviews,
                                allReviews = allReviews,
                                onSelectCigar = viewModel::selectLabCigar,
                                onSelectBeverage = viewModel::selectLabBeverage,
                                onPairingStrategyChange = viewModel::setPairingStrategy,
                                onOpenPairing = { cId, bId ->
                                    navController.navigate(AppRoutes.pairingDetail(cId, bId))
                                },
                                onOpenRateComposer = { cId, bId, existing ->
                                    rateTarget = ActiveRateTarget(cId, bId, existing)
                                },
                                onDeleteReview = { reviewId ->
                                    viewModel.deleteReview(reviewId)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Review deleted")
                                    }
                                },
                            )
                        }

                        composable(AppRoutes.TRANSCRIBE) {
                            TranscribeScreen(
                                onShowMessage = { msg ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                },
                            )
                        }

                        composable(
                            route = AppRoutes.CIGAR_DETAIL,
                            arguments = listOf(navArgument("cigarId") { type = NavType.StringType }),
                        ) { entry ->
                            val cigarId = entry.arguments?.getString("cigarId") ?: ""
                            CigarDetailScreen(
                                cigarId = cigarId,
                                ratingsByPairingId = ratingsByPairingId,
                                onBack = { navController.popBackStack() },
                                onOpenPairing = { cId, bId ->
                                    navController.navigate(AppRoutes.pairingDetail(cId, bId))
                                },
                                onQuickRate = { cId, bId ->
                                    rateTarget = ActiveRateTarget(cId, bId)
                                },
                            )
                        }

                        composable(
                            route = AppRoutes.BEVERAGE_DETAIL,
                            arguments = listOf(navArgument("beverageId") { type = NavType.StringType }),
                        ) { entry ->
                            val beverageId = entry.arguments?.getString("beverageId") ?: ""
                            BeverageDetailScreen(
                                beverageId = beverageId,
                                ratingsByPairingId = ratingsByPairingId,
                                onBack = { navController.popBackStack() },
                                onOpenPairing = { cId, bId ->
                                    navController.navigate(AppRoutes.pairingDetail(cId, bId))
                                },
                                onQuickRate = { cId, bId ->
                                    rateTarget = ActiveRateTarget(cId, bId)
                                },
                            )
                        }

                        composable(
                            route = AppRoutes.PAIRING_DETAIL,
                            arguments = listOf(
                                navArgument("cigarId") { type = NavType.StringType },
                                navArgument("beverageId") { type = NavType.StringType },
                            ),
                        ) { entry ->
                            val cId = entry.arguments?.getString("cigarId") ?: ""
                            val bId = entry.arguments?.getString("beverageId") ?: ""
                            val pairingId = com.example.model.buildPairingId(cId, bId)
                            val reviews by remember(pairingId) {
                                viewModel.observeReviewsForPairing(pairingId)
                            }.collectAsStateWithLifecycle(initialValue = emptyList())

                            PairingDetailScreen(
                                cigarId = cId,
                                beverageId = bId,
                                ratingsByPairingId = ratingsByPairingId,
                                reviews = reviews,
                                onBack = { navController.popBackStack() },
                                onOpenCigar = { navController.navigate(AppRoutes.cigarDetail(it)) },
                                onOpenBeverage = { navController.navigate(AppRoutes.beverageDetail(it)) },
                                onOpenRateComposer = { existing ->
                                    rateTarget = ActiveRateTarget(cId, bId, existing)
                                },
                                onDeleteReview = { reviewId ->
                                    viewModel.deleteReview(reviewId)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Review deleted")
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    rateTarget?.let { target ->
        val cigar = PairingCatalog.cigarById(target.cigarId)
        val bev = PairingCatalog.beverageById(target.beverageId)
        if (cigar != null && bev != null) {
            RatingComposerSheet(
                cigarName = "${cigar.brand} ${cigar.name}",
                beverageName = bev.name,
                defaultReviewerName = lastReviewerName,
                existingReview = target.existingReview,
                onDismiss = { rateTarget = null },
                onSubmit = { rating, reviewText, reviewerName ->
                    viewModel.submitReview(
                        cigarId = target.cigarId,
                        beverageId = target.beverageId,
                        rating = rating,
                        reviewText = reviewText,
                        reviewerName = reviewerName,
                        existingReviewId = target.existingReview?.id ?: 0L,
                    )
                    rateTarget = null
                    scope.launch {
                        snackbarHostState.showSnackbar("Saved $rating★ rating for ${cigar.name} × ${bev.name}")
                    }
                },
            )
        }
    }
}
