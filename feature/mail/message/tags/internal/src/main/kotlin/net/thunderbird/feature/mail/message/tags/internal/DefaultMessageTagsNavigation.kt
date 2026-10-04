package net.thunderbird.feature.mail.message.tags.internal

import androidx.navigation.NavGraphBuilder
import net.thunderbird.core.ui.contract.mvi.observe
import net.thunderbird.core.ui.navigation.deepLinkComposable
import net.thunderbird.feature.mail.message.tags.api.MessageTagsNavigation
import net.thunderbird.feature.mail.message.tags.api.MessageTagsRoute
import org.koin.compose.viewmodel.koinViewModel

internal class DefaultMessageTagsNavigation : MessageTagsNavigation {

    override fun registerRoutes(
        navGraphBuilder: NavGraphBuilder,
        onBack: () -> Unit,
        onFinish: (MessageTagsRoute) -> Unit,
    ) {
        with(navGraphBuilder) {
            deepLinkComposable<MessageTagsRoute>(basePath = MessageTagsRoute.basePath) {
                val viewModel: MessageTagsViewModel = koinViewModel()
                val (state, dispatch) = viewModel.observe { effect ->
                    when (effect) {
                        MessageTagsContract.Effect.NavigateBack -> onBack()
                    }
                }

                MessageTagsScreen(
                    state = state.value,
                    onEvent = dispatch,
                )
            }
        }
    }
}
