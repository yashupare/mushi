package com.mushi.app

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.animation.PathInterpolator
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mushi.app.core.deeplink.ExternalUriHandler
import com.mushi.app.core.newpipe.NewPipeDownloader
import com.mushi.app.core.paths.Paths
import com.mushi.app.media.PlaybackService
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        // Cold start only: let the logo animation (about 800 ms) finish instead of cutting it off
        // when the first frame is ready. Recreated activities (rotation, etc.) skip the wait.
        if (savedInstanceState == null) {
            val shownAt = SystemClock.uptimeMillis()
            splash.setKeepOnScreenCondition { SystemClock.uptimeMillis() - shownAt < SPLASH_HOLD_MS }
        }
        splash.setOnExitAnimationListener(::playSplashExit)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        FileKit.init(this)
        NewPipeDownloader.init(Paths())
        
        // Plain start from the foreground; Media3 promotes PlaybackService itself once playback starts.
        // If the app is opened while the phone is asleep or locked, Android treats it as being in
        // the background and refuses the start. That must never crash the app: the player starts
        // the service again when music actually plays.
        try {
            startService(Intent(this, PlaybackService::class.java))
        } catch (e: Exception) {
            android.util.Log.w("MainActivity", "Playback service not started yet", e)
        }

        intent?.dataString?.let(ExternalUriHandler::onNewUri)
        setContent {
            App()
        }
    }

    /**
     * Hand-off from the splash to the app: the logo grows and fades while the splash background
     * fades out, revealing the first screen. Skipped when the system has animations turned off.
     */
    private fun playSplashExit(provider: SplashScreenViewProvider) {
        if (!ValueAnimator.areAnimatorsEnabled()) {
            provider.remove()
            return
        }
        runCatching {
            provider.iconView.animate()
                .scaleX(1.6f)
                .scaleY(1.6f)
                .alpha(0f)
                .setDuration(450)
                .setInterpolator(PathInterpolator(0.4f, 0f, 0.2f, 1f))
                .start()
        }
        provider.view.animate()
            .alpha(0f)
            .setStartDelay(100)
            .setDuration(350)
            .withEndAction { provider.remove() }
            .start()
    }

    private companion object {
        /** How long a cold start keeps the splash, so the logo animation can play through. */
        const val SPLASH_HOLD_MS = 700L
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.dataString?.let(ExternalUriHandler::onNewUri)
    }
}