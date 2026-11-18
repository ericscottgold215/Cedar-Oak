package com.example.data.history

import com.example.data.catalog.pdf.PdfCatalogData
import com.example.model.CigarCompanyHistory
import com.example.model.CigarReleaseHistory

object CigarHistoryCatalog {
    val companies: List<CigarCompanyHistory> =
        CigarCompanyHistoryData.companies +
            CigarMoreHistoryExpansions.companies +
            CigarBoutiqueVaultHistory.companies +
            PdfCompanyHistoryData.companies
    val releases: List<CigarReleaseHistory> =
        CigarReleaseHistoryData.releases +
            CigarReleaseHistoryExpansions.releases +
            CigarMoreHistoryExpansions.releases +
            CigarBoutiqueVaultHistory.releases +
            PdfCatalogData.releases

    private val companyByBrand: Map<String, CigarCompanyHistory> =
        companies.associateBy { it.brand }

    private val releaseByCigarId: Map<String, CigarReleaseHistory> =
        releases.associateBy { it.cigarId }

    fun companyForBrand(brand: String): CigarCompanyHistory? = companyByBrand[brand]

    fun releaseForCigar(cigarId: String): CigarReleaseHistory? = releaseByCigarId[cigarId]
}
