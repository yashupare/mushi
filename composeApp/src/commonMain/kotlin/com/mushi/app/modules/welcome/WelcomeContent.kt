package com.mushi.app.modules.welcome

import org.jetbrains.compose.resources.StringResource
import com.mushi.app.generated.resources.Res
import com.mushi.app.generated.resources.welcome_get_started
import com.mushi.app.generated.resources.welcome_lets_go
import com.mushi.app.generated.resources.welcome_next
import com.mushi.app.generated.resources.welcome_step_guide
import com.mushi.app.generated.resources.welcome_step_plugins
import com.mushi.app.generated.resources.welcome_step_support
import com.mushi.app.generated.resources.welcome_step_welcome

/** Order matches the 4 onboarding pages. */
internal val welcomePrimaryLabels: List<StringResource> = listOf(
    Res.string.welcome_lets_go,
    Res.string.welcome_next,
    Res.string.welcome_next,
    Res.string.welcome_get_started)

/** Short labels for the step switcher. Order matches the 4 onboarding pages. */
internal val welcomeStepLabels: List<StringResource> = listOf(
    Res.string.welcome_step_welcome,
    Res.string.welcome_step_support,
    Res.string.welcome_step_plugins,
    Res.string.welcome_step_guide)
