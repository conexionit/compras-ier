package com.ierresurreccion.compras

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    companion object {
        // URL final publicada vía Cloudflare (Custom Domain)
        private const val APP_URL = "https://compras.ierresurreccion.com"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Modo "incógnito": borra toda sesión/cookie/almacenamiento ANTES de cargar la app.
        // Esto evita el conflicto cuando el dispositivo tiene varias cuentas Google activas
        // en Chrome, ya que el WebView arranca sin ninguna sesión residual.
        limpiarSesionCompleta()

        webView = WebView(this)
        setContentView(webView)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true          // requerido: la app usa sessionStorage
            cacheMode = WebSettings.LOAD_NO_CACHE
            allowFileAccess = false
            allowContentAccess = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        }

        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                Toast.makeText(
                    this@MainActivity,
                    "Sin conexión. Verifica tu internet e intenta de nuevo.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        webView.loadUrl(APP_URL)
    }

    private fun limpiarSesionCompleta() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        WebStorage.getInstance().deleteAllData()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        // Al cerrar la app, vuelve a limpiar — así la próxima apertura arranca
        // completamente "en blanco", sin importar quién la usó antes.
        limpiarSesionCompleta()
        super.onDestroy()
    }
}
