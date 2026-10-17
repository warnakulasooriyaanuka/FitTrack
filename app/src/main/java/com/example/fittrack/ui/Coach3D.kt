package com.example.fittrack.ui

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/** True while the on-screen keyboard is showing (used to shrink the coach so inputs stay visible). */
@Composable
fun rememberImeVisible(): Boolean = WindowInsets.ime.getBottom(LocalDensity.current) > 0

/**
 * The 3D chibi coach. It is a small three.js scene (built from 3D shapes, see assets/coach/coach.js)
 * rendered in a transparent WebView. Drag to turn it, tap to make it wave.
 *
 * @param talking  moves the mouth and nods the head while true
 * @param cheerKey increase this number to make the coach jump and cheer
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun Coach3D(modifier: Modifier = Modifier, talking: Boolean = false, cheerKey: Int = 0) {
    val holder = remember { arrayOfNulls<WebView>(1) }
    var ready by remember { mutableStateOf(false) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER
                settings.javaScriptEnabled = true
                settings.allowFileAccess = true          // only used for file:///android_asset
                settings.allowContentAccess = false
                settings.setSupportZoom(false)
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) { ready = true }
                    // the coach page never needs to navigate anywhere
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean = true
                }
                loadUrl("file:///android_asset/coach/index.html")
                holder[0] = this
            }
        },
        onRelease = { it.stopLoading(); it.destroy(); holder[0] = null }
    )

    LaunchedEffect(ready, talking) {
        if (ready) holder[0]?.evaluateJavascript("window.coach && window.coach.setTalking($talking)", null)
    }
    LaunchedEffect(ready, cheerKey) {
        if (ready && cheerKey > 0) holder[0]?.evaluateJavascript("window.coach && window.coach.cheer()", null)
    }
}
