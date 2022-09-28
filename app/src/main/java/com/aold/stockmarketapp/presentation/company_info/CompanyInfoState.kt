package com.aold.stockmarketapp.presentation.company_info

import com.aold.stockmarketapp.domain.model.CompanyInfo
import com.aold.stockmarketapp.domain.model.IntradayInfo

data class CompanyInfoState(
    val stockInfo: List<IntradayInfo> = emptyList(),
    val company: CompanyInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
