package com.gwproductsusa.gwtasks.core.di

import com.gwproductsusa.gwtasks.data.repository.OdooRepositoryImpl
import com.gwproductsusa.gwtasks.domain.repository.OdooRepository
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
    abstract fun bindOdooRepository(impl: OdooRepositoryImpl): OdooRepository
}
