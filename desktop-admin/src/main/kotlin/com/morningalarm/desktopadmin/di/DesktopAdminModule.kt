package com.morningalarm.desktopadmin.di

import com.morningalarm.desktopadmin.config.AppPreferences
import com.morningalarm.desktopadmin.data.AdminApiClient
import com.morningalarm.desktopadmin.data.AdminRingtoneRepository
import com.morningalarm.desktopadmin.data.AdminRingtoneRepositoryImpl
import com.morningalarm.desktopadmin.media.DesktopMediaPlaybackController
import com.morningalarm.desktopadmin.media.MediaPlaybackController
import com.morningalarm.desktopadmin.ui.AdminSession
import com.morningalarm.desktopadmin.ui.login.LoginViewModel
import com.morningalarm.desktopadmin.ui.workspace.WorkspaceViewModel
import org.koin.dsl.module

internal val desktopAdminModule = module {
    single { AppPreferences() }

    factory { (session: AdminSession) ->
        AdminApiClient(session.baseUrl)
    }

    factory<AdminRingtoneRepository> { (session: AdminSession) ->
        AdminRingtoneRepositoryImpl(
            apiClient = get { org.koin.core.parameter.parametersOf(session) },
            bearerToken = session.authSession.bearerToken,
            adminSecret = session.adminSecret,
        )
    }

    factory<MediaPlaybackController> { DesktopMediaPlaybackController() }

    factory {
        LoginViewModel(
            preferences = get(),
            apiClientFactory = { baseUrl -> AdminApiClient(baseUrl) },
        )
    }

    factory { (session: AdminSession) ->
        WorkspaceViewModel(
            repository = get { org.koin.core.parameter.parametersOf(session) },
            baseUrl = session.baseUrl,
        )
    }
}
