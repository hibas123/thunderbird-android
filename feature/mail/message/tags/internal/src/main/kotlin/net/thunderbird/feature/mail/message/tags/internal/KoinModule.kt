package net.thunderbird.feature.mail.message.tags.internal

import net.thunderbird.feature.mail.message.tags.api.MessageTagsNavigation
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureMessageTagsModule = module {
    single<DefaultTagNameProvider> { ResourcesDefaultTagNameProvider(context = androidContext()) }

    viewModel {
        MessageTagsViewModel(
            settingsManager = get(),
            defaultTagNameProvider = get(),
        )
    }

    single<MessageTagsNavigation> { DefaultMessageTagsNavigation() }
}
