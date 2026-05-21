package com.vialreport.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vialreport.app.data.local.TokenStore
import com.vialreport.app.presentation.navigation.AppNavGraph
import com.vialreport.app.ui.theme.VialReportTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var tokenStore: TokenStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VialReportTheme {
                AppNavGraph(tokenStore)
            }
        }
    }
}
