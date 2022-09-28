package com.aold.stockmarketapp.domain.repository

import com.aold.stockmarketapp.domain.model.CompanyInfo
import com.aold.stockmarketapp.domain.model.CompanyListing
import com.aold.stockmarketapp.domain.model.IntradayInfo
import com.aold.stockmarketapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface StockRepository {

    suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>>

    suspend fun getIntradayInfo(
        symbol: String
    ): Resource<List<IntradayInfo>>

    suspend fun getCompanyInfo(
        symbol: String
    ): Resource<CompanyInfo>
}