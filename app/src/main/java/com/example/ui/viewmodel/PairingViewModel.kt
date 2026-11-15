package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.catalog.PairingCatalog
import com.example.data.local.AppDatabase
import com.example.data.repository.PairingReviewRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.model.BeverageCategory
import com.example.model.BeverageFilter
import com.example.model.PairingRatingSummary
import com.example.model.PairingReview
import com.example.model.PairingStrategy
import com.example.model.PairingSuggestion
import com.example.model.StrengthLevel
import com.example.model.buildPairingId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MatcherDirection(val label: String) {
    CIGAR_TO_DRINK("I Have a Cigar"),
    DRINK_TO_CIGAR("I Have a Drink"),
}

class PairingViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val prefsRepo = UserPreferencesRepository(application)
    private val reviewRepo = PairingReviewRepository(database.pairingReviewDao(), prefsRepo)

    val ratingsByPairingId: StateFlow<Map<String, PairingRatingSummary>> =
        reviewRepo.observeRatingSummaries()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val allReviews: StateFlow<List<PairingReview>> =
        reviewRepo.observeAllReviews()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val userReviews: StateFlow<List<PairingReview>> =
        reviewRepo.observeUserReviews()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val beverageFilter: StateFlow<BeverageFilter> =
        prefsRepo.preferredBeverageFilter
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BeverageFilter.ALL)

    val isDarkTheme: StateFlow<Boolean> =
        prefsRepo.isDarkTheme
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val lastReviewerName: StateFlow<String> =
        prefsRepo.lastReviewerName
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "Aficionado")

    private val _matcherDirection = MutableStateFlow(MatcherDirection.CIGAR_TO_DRINK)
    val matcherDirection: StateFlow<MatcherDirection> = _matcherDirection.asStateFlow()

    private val _selectedMatcherCigarId = MutableStateFlow(PairingCatalog.cigars.first().id)
    val selectedMatcherCigarId: StateFlow<String> = _selectedMatcherCigarId.asStateFlow()

    private val _selectedMatcherBeverageId = MutableStateFlow(PairingCatalog.beverages.first().id)
    val selectedMatcherBeverageId: StateFlow<String> = _selectedMatcherBeverageId.asStateFlow()

    private val _cigarSearchQuery = MutableStateFlow("")
    val cigarSearchQuery: StateFlow<String> = _cigarSearchQuery.asStateFlow()

    private val _cigarStrengthFilter = MutableStateFlow<StrengthLevel?>(null)
    val cigarStrengthFilter: StateFlow<StrengthLevel?> = _cigarStrengthFilter.asStateFlow()

    private val _drinkSearchQuery = MutableStateFlow("")
    val drinkSearchQuery: StateFlow<String> = _drinkSearchQuery.asStateFlow()

    private val _drinkCategoryFilter = MutableStateFlow<BeverageCategory?>(null)
    val drinkCategoryFilter: StateFlow<BeverageCategory?> = _drinkCategoryFilter.asStateFlow()

    private val _pairingStrategy = MutableStateFlow(PairingStrategy.BALANCED)
    val pairingStrategy: StateFlow<PairingStrategy> = _pairingStrategy.asStateFlow()

    private val _labCigarId = MutableStateFlow("padron_1964")
    val labCigarId: StateFlow<String> = _labCigarId.asStateFlow()

    private val _labBeverageId = MutableStateFlow("cuban_cafecito")
    val labBeverageId: StateFlow<String> = _labBeverageId.asStateFlow()

    val featuredPairings: StateFlow<List<PairingSuggestion>> =
        combine(ratingsByPairingId, beverageFilter) { ratings, filter ->
            PairingCatalog.allCuratedSuggestions(ratings)
                .filter { suggestion ->
                    when (filter) {
                        BeverageFilter.ALL -> true
                        BeverageFilter.ALCOHOLIC -> suggestion.beverage.isAlcoholic
                        BeverageFilter.NON_ALCOHOLIC -> !suggestion.beverage.isAlcoholic
                    }
                }
                .sortedWith(
                    compareByDescending<PairingSuggestion> { it.averageRating ?: 0.0 }
                        .thenByDescending { it.reviewCount }
                        .thenByDescending { it.matchScore }
                )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val matcherInputs = combine(
        matcherDirection,
        selectedMatcherCigarId,
        selectedMatcherBeverageId,
        beverageFilter,
        pairingStrategy,
    ) { direction, cigarId, bevId, filter, strategy ->
        MatcherInputs(direction, cigarId, bevId, filter, strategy)
    }

    val quickMatcherResults: StateFlow<List<PairingSuggestion>> =
        combine(matcherInputs, ratingsByPairingId) { inputs, ratings ->
            when (inputs.direction) {
                MatcherDirection.CIGAR_TO_DRINK -> {
                    PairingCatalog.pairingsForCigar(
                        cigarId = inputs.cigarId,
                        ratingsByPairingId = ratings,
                        strategy = inputs.strategy,
                    ).filter { suggestion ->
                        when (inputs.filter) {
                            BeverageFilter.ALL -> true
                            BeverageFilter.ALCOHOLIC -> suggestion.beverage.isAlcoholic
                            BeverageFilter.NON_ALCOHOLIC -> !suggestion.beverage.isAlcoholic
                        }
                    }
                }
                MatcherDirection.DRINK_TO_CIGAR -> {
                    PairingCatalog.pairingsForBeverage(
                        beverageId = inputs.bevId,
                        ratingsByPairingId = ratings,
                        strategy = inputs.strategy,
                    )
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val labSuggestion: StateFlow<PairingSuggestion?> =
        combine(labCigarId, labBeverageId, pairingStrategy, ratingsByPairingId) { cId, bId, strategy, ratings ->
            val cigar = PairingCatalog.cigarById(cId) ?: return@combine null
            val bev = PairingCatalog.beverageById(bId) ?: return@combine null
            PairingCatalog.evaluatePairing(
                cigar = cigar,
                beverage = bev,
                ratingSummary = ratings[buildPairingId(cId, bId)],
                strategy = strategy,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            reviewRepo.ensureSeeded()
        }
    }

    fun setMatcherDirection(direction: MatcherDirection) {
        _matcherDirection.value = direction
    }

    fun selectMatcherCigar(cigarId: String) {
        _selectedMatcherCigarId.value = cigarId
    }

    fun selectMatcherBeverage(beverageId: String) {
        _selectedMatcherBeverageId.value = beverageId
    }

    fun setBeverageFilter(filter: BeverageFilter) {
        viewModelScope.launch {
            prefsRepo.setPreferredBeverageFilter(filter)
            val currentCategory = _drinkCategoryFilter.value
            if (currentCategory != null) {
                val matchesFilter = when (filter) {
                    BeverageFilter.ALL -> true
                    BeverageFilter.ALCOHOLIC -> currentCategory.isAlcoholic
                    BeverageFilter.NON_ALCOHOLIC -> !currentCategory.isAlcoholic
                }
                if (!matchesFilter) {
                    _drinkCategoryFilter.value = null
                }
            }
        }
    }

    fun setCigarSearchQuery(query: String) {
        _cigarSearchQuery.value = query
    }

    fun setCigarStrengthFilter(strength: StrengthLevel?) {
        _cigarStrengthFilter.value = strength
    }

    fun setDrinkSearchQuery(query: String) {
        _drinkSearchQuery.value = query
    }

    fun setDrinkCategoryFilter(category: BeverageCategory?) {
        _drinkCategoryFilter.value = category
    }

    fun selectLabCigar(cigarId: String) {
        _labCigarId.value = cigarId
    }

    fun selectLabBeverage(beverageId: String) {
        _labBeverageId.value = beverageId
    }

    fun setPairingStrategy(strategy: PairingStrategy) {
        _pairingStrategy.value = strategy
    }

    fun toggleDarkTheme(currentDark: Boolean) {
        viewModelScope.launch {
            prefsRepo.setDarkTheme(!currentDark)
        }
    }

    fun observeReviewsForPairing(pairingId: String) =
        reviewRepo.observeReviewsForPairing(pairingId)

    fun submitReview(
        cigarId: String,
        beverageId: String,
        rating: Int,
        reviewText: String,
        reviewerName: String,
        existingReviewId: Long = 0L,
    ) {
        viewModelScope.launch {
            reviewRepo.submitReview(
                cigarId = cigarId,
                beverageId = beverageId,
                rating = rating,
                reviewText = reviewText,
                reviewerName = reviewerName,
                existingReviewId = existingReviewId,
            )
        }
    }

    fun deleteReview(reviewId: Long) {
        viewModelScope.launch {
            reviewRepo.deleteReview(reviewId)
        }
    }

    private data class MatcherInputs(
        val direction: MatcherDirection,
        val cigarId: String,
        val bevId: String,
        val filter: BeverageFilter,
        val strategy: PairingStrategy,
    )
}
