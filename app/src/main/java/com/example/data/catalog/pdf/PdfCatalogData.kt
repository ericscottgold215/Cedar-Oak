package com.example.data.catalog.pdf

import com.example.model.Cigar
import com.example.model.CigarReleaseHistory

object PdfCatalogData {
    val entries: List<PdfCigarEntry> =
        PdfCatalogVol1.entries +
            PdfCatalogVol2.entries +
            PdfCatalogVol3.entries +
            PdfCatalogVol4.entries +
            PdfCatalogVol5.entries +
            PdfCatalogVol6.entries

    val cigars: List<Cigar> = entries.map { it.toCigar() }

    val releases: List<CigarReleaseHistory> = entries.map { it.toReleaseHistory() }
}
