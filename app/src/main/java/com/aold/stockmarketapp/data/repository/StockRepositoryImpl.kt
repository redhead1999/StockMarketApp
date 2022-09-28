package com.aold.stockmarketapp.data.repository

import com.aold.stockmarketapp.data.csv.CSVParser
import com.aold.stockmarketapp.data.local.StockDatabase
import com.aold.stockmarketapp.data.mapper.toCompanyInfo
import com.aold.stockmarketapp.data.mapper.toCompanyListing
import com.aold.stockmarketapp.data.mapper.toCompanyListingEntity
import com.aold.stockmarketapp.data.remote.StockApi
import com.aold.stockmarketapp.domain.model.CompanyInfo
import com.aold.stockmarketapp.domain.model.CompanyListing
import com.aold.stockmarketapp.domain.model.IntradayInfo
import com.aold.stockmarketapp.domain.repository.StockRepository
import com.aold.stockmarketapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val api: StockApi,
    private val db: StockDatabase,
    private val companyListingsParser: CSVParser<CompanyListing>,
    private val intradayInfoParser: CSVParser<IntradayInfo>,
): StockRepository {

    private val dao = db.dao

    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return flow<Resource<List<CompanyListing>>> {
            emit(Resource.Loading(true))
            val localListings = dao.searchCompanyListing(query)
            emit(Resource.Success(
                data = localListings.map { it.toCompanyListing() }
            ))

            val isDbEmpty = localListings.isEmpty() && query.isBlank()
            val shouldJustLoadFromCache = !isDbEmpty && !fetchFromRemote
            if(shouldJustLoadFromCache) {
                emit(Resource.Loading(false))
                return@flow
            }
            val remoteListings = try {
                val response = api.getListings()
                companyListingsParser.parse(response.byteStream())
            } catch(e: IOException) {
                e.printStackTrace()
                emit(Resource.Error("Не удалось загрузить данные"))
                null
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("Не удалось загрузить данные"))
                null
            }

            remoteListings?.let { listings ->
                dao.clearCompanyListings()
                dao.insertCompanyListings(
                    listings.map { it.toCompanyListingEntity() }
                )
                emit(Resource.Success(
                    data = dao
                        .searchCompanyListing("")
                        .map { it.toCompanyListing() }
                ))
                emit(Resource.Loading(false))
            }
        }
    }

    override suspend fun getIntradayInfo(symbol: String): Resource<List<IntradayInfo>> {
        return try {
            val response = api.getIntradayInfo(symbol)
            val results = intradayInfoParser.parse(response.byteStream())
            Resource.Success(results)
        } catch(e: IOException) {
            e.printStackTrace()
            Resource.Error(
                message = "Не удалось загрузить внутридневную информацию"
            )
        } catch(e: HttpException) {
            e.printStackTrace()
            Resource.Error(
                message = "Не удалось загрузить внутридневную информацию"
            )
        }
    }

    override suspend fun getCompanyInfo(symbol: String): Resource<CompanyInfo> {
        return try {
            val result = api.getCompanyInfo(symbol)
            Resource.Success(result.toCompanyInfo())
        } catch(e: IOException) {
            e.printStackTrace()
            Resource.Error(
                message = "Не удалось загрузить информацию о компании"
            )
        } catch(e: HttpException) {
            e.printStackTrace()
            Resource.Error(
                message = "Не удалось загрузить информацию о компании"
            )
        }
    }
}