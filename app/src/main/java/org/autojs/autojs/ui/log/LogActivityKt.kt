@file:OptIn(ExperimentalMaterial3Api::class)

package org.autojs.autojs.ui.log

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import com.stardust.autojs.core.console.ConsoleView
import com.stardust.autojs.core.console.GlobalConsole
import org.autojs.autojs.autojs.AutoJs
import org.autojs.autojs.ui.compose.theme.AutoXJsTheme
import org.autojs.autojs.ui.compose.theme.isLight
import org.autojs.autojs.ui.compose.util.SetSystemUI
import org.autojs.autojs.ui.widget.fillMaxSize
import org.openautojs.autojs.R

class LogActivityKt : ComponentActivity() {

    companion object {
        @JvmStatic
        fun start(context: Context) {
            context.startActivity(Intent(context, LogActivityKt::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()
        setContent {
            AutoXJsTheme {
                Surface(color = MaterialTheme.colorScheme.surface) {
//                    SetSystemUI()
                    Content()
                }
            }
        }
    }

    @Composable
    fun Content() {
        val consoleImpl = remember {
            AutoJs.getInstance().globalConsole
        }
        Scaffold(
            topBar = {
                TopAppBar()
            },
            floatingActionButton = {
                FloatingActionButton(consoleImpl)
            }
        ) {
            Console(it, consoleImpl)
        }
    }

    @Composable
    private fun Console(
        it: PaddingValues,
        consoleImpl: GlobalConsole
    ) {
        val isLight = MaterialTheme.colorScheme.isLight
        AndroidView(
            modifier = Modifier.padding(it),
            factory = { context ->
                ConsoleView(context).apply {
                    fillMaxSize()


                    setConsole(consoleImpl)
                    findViewById<View>(R.id.input_container).visibility = View.GONE
                }
            },
            update = {
                if (isLight) {
                    it.colors.apply {
                        put(Log.VERBOSE, -0x203f3f40)
                        put(Log.DEBUG, -0x34000000)
                    }
                } else {
                    it.colors.apply {
                        put(Log.VERBOSE, -0x203f3f40)
                        put(Log.DEBUG, -0x20000001)
                    }
                }
            }
        )
    }

    @Composable
    private fun FloatingActionButton(consoleImpl: GlobalConsole) {
        FloatingActionButton(
            onClick = { consoleImpl.clear() },
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = stringResource(id = R.string.text_clear)
            )
        }
    }

    @Composable
    private fun TopAppBar() {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.text_log)) },
            navigationIcon = {
                IconButton(onClick = { finish() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(id = R.string.desc_back)
                    )
                }
            }
        )
    }

}