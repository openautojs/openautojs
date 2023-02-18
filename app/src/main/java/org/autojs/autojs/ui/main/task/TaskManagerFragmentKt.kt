@file:OptIn(ExperimentalMaterial3Api::class)

package org.autojs.autojs.ui.main.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment

class TaskManagerFragmentKt : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Scaffold(
                    floatingActionButton = {
                    },
                ) {
                    Box(
                        modifier = Modifier
                            .padding(it)
                            .fillMaxSize()
                    ) {
                        TaskPage()
                    }
                }
            }
        }
    }

}