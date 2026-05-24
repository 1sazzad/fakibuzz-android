package com.qarena.android.presentation.common

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.qarena.android.data.remote.dto.QuestionResponse
import com.qarena.android.data.remote.dto.SearchResultResponse
import com.qarena.android.model.SubQuestion
import com.qarena.android.model.Suggestion

data class DiagramInfo(
    val diagramRequired: Boolean? = null,
    val diagramType: String? = null,
    val diagramSvg: String? = null,
    val diagramUrl: String? = null,
    val diagramDescription: String? = null,
    val diagramReference: String? = null
)

@Composable
fun DiagramRenderer(
    diagramInfo: DiagramInfo?,
    modifier: Modifier = Modifier
) {
    val required = diagramInfo?.diagramRequired == true
    val diagramType = diagramInfo?.diagramType?.trim().orEmpty()
    val diagramSvg = diagramInfo?.diagramSvg?.trim().orEmpty()
    val diagramUrl = diagramInfo?.diagramUrl?.trim().orEmpty()
    val diagramDescription = diagramInfo?.diagramDescription?.trim().orEmpty()

    val shouldRenderSvg = diagramSvg.startsWith("<svg", ignoreCase = true) ||
        (diagramType.equals("svg", ignoreCase = true) && diagramSvg.isNotBlank())
    val shouldRenderImage = diagramUrl.isNotBlank()
    val shouldRenderDescription = diagramDescription.isNotBlank()
    val canRenderGraphic = shouldRenderSvg || shouldRenderImage

    if (!canRenderGraphic && !shouldRenderDescription && !required) {
        return
    }

    if (shouldRenderSvg) {
        val html = remember(diagramSvg) { buildSvgHtml(diagramSvg) }
        var webView by remember { mutableStateOf<WebView?>(null) }

        AndroidView(
            modifier = modifier.fillMaxWidth().wrapContentHeight(),
            factory = { context ->
                WebView(context).also { view ->
                    webView = view
                    configureWebView(view)
                }
            },
            update = { view ->
                view.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
            }
        )

        DisposableEffect(Unit) {
            onDispose {
                webView?.destroy()
                webView = null
            }
        }

        return
    }

    if (shouldRenderImage) {
        val html = remember(diagramUrl) { buildImageHtml(diagramUrl) }
        var webView by remember { mutableStateOf<WebView?>(null) }

        AndroidView(
            modifier = modifier.fillMaxWidth().wrapContentHeight(),
            factory = { context ->
                WebView(context).also { view ->
                    webView = view
                    configureWebView(view)
                }
            },
            update = { view ->
                view.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
            }
        )

        DisposableEffect(Unit) {
            onDispose {
                webView?.destroy()
                webView = null
            }
        }

        return
    }

    if (shouldRenderDescription) {
        Column(modifier = modifier.fillMaxWidth()) {
            Text(
                text = "Diagram: $diagramDescription",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (required && !canRenderGraphic) {
            Text(
                text = "Diagram available but cannot be displayed.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        return
    }

    if (required) {
        Text(
            text = "Diagram available but cannot be displayed.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

fun QuestionResponse.toDiagramInfo(): DiagramInfo {
    return DiagramInfo(
        diagramRequired = diagramRequired,
        diagramType = diagramType,
        diagramSvg = diagramSvg,
        diagramUrl = diagramUrl,
        diagramDescription = diagramDescription,
        diagramReference = diagramReference
    )
}

fun Suggestion.toDiagramInfo(): DiagramInfo {
    return DiagramInfo(
        diagramRequired = diagramRequired,
        diagramType = diagramType,
        diagramSvg = diagramSvg,
        diagramUrl = diagramUrl,
        diagramDescription = diagramDescription,
        diagramReference = diagramReference
    )
}

fun SubQuestion.toDiagramInfo(): DiagramInfo {
    return DiagramInfo(
        diagramRequired = diagramRequired,
        diagramType = diagramType,
        diagramSvg = diagramSvg,
        diagramUrl = diagramUrl,
        diagramDescription = diagramDescription,
        diagramReference = diagramReference
    )
}

fun SearchResultResponse.toDiagramInfo(): DiagramInfo {
    return DiagramInfo(
        diagramRequired = diagramRequired,
        diagramType = diagramType,
        diagramSvg = diagramSvg,
        diagramUrl = diagramUrl,
        diagramDescription = diagramDescription,
        diagramReference = diagramReference
    )
}

private fun configureWebView(webView: WebView) {
    with(webView.settings) {
        javaScriptEnabled = false
        allowFileAccess = false
        allowContentAccess = false
        domStorageEnabled = false
        useWideViewPort = true
        loadWithOverviewMode = true
        setSupportZoom(false)
        builtInZoomControls = false
        displayZoomControls = false
    }

    webView.webViewClient = WebViewClient()
    webView.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}

private fun buildSvgHtml(svg: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <style>
                html, body {
                    margin: 0;
                    padding: 0;
                    background: transparent;
                    overflow: hidden;
                }
                body {
                    display: flex;
                    justify-content: center;
                    align-items: center;
                }
                .diagram {
                    width: 100%;
                }
                svg {
                    max-width: 100%;
                    height: auto;
                    display: block;
                }
            </style>
        </head>
        <body>
            <div class="diagram">
                $svg
            </div>
        </body>
        </html>
    """.trimIndent()
}

private fun buildImageHtml(imageUrl: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <style>
                html, body {
                    margin: 0;
                    padding: 0;
                    background: transparent;
                    overflow: hidden;
                }
                body {
                    display: flex;
                    justify-content: center;
                    align-items: center;
                }
                img {
                    max-width: 100%;
                    height: auto;
                    display: block;
                }
            </style>
        </head>
        <body>
            <img src="$imageUrl" alt="Diagram" />
        </body>
        </html>
    """.trimIndent()
}