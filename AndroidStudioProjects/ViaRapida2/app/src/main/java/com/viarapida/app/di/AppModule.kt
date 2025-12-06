package com.viarapida.app.di

import com.viarapida.app.data.repository.AuthRepository
import com.viarapida.app.data.repository.AuthRepositoryImpl
import com.viarapida.app.data.repository.RouteRepository
import com.viarapida.app.data.repository.RouteRepositoryImpl
import com.viarapida.app.data.repository.TicketRepository
import com.viarapida.app.data.repository.TicketRepositoryImpl

object AppModule {

    private var authRepository: AuthRepository? = null
    private var routeRepository: RouteRepository? = null
    private var ticketRepository: TicketRepository? = null

    fun provideAuthRepository(): AuthRepository {
        if (authRepository == null) {
            authRepository = AuthRepositoryImpl()
        }
        return authRepository!!
    }

    fun provideRouteRepository(): RouteRepository {
        if (routeRepository == null) {
            routeRepository = RouteRepositoryImpl()
        }
        return routeRepository!!
    }

    fun provideTicketRepository(): TicketRepository {
        if (ticketRepository == null) {
            ticketRepository = TicketRepositoryImpl()
        }
        return ticketRepository!!
    }
}