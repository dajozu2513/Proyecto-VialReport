package com.vialreport.app.core.di

import com.vialreport.app.data.repository.AuthRepositoryImpl
import com.vialreport.app.data.repository.ReportRepositoryImpl
import com.vialreport.app.domain.repository.IAuthRepository
import com.vialreport.app.domain.repository.IReportRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): IReportRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): IAuthRepository
}
