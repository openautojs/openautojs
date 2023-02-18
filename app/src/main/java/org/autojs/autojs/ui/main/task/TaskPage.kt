@file:OptIn(ExperimentalMaterial3Api::class)

package org.autojs.autojs.ui.main.task

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.autojs.autojs.ui.compose.widget.MyIcon

data class GroupItem(
    val isExpanded: Boolean,
    val taskGroup: TaskGroup,
)

@Composable
fun TaskPage() {
    val context = LocalContext.current
    val vm = viewModel<TaskViewModel>()
    val itemList = remember {
        mutableStateListOf<GroupItem>().apply {
            addAll(vm.taskGroups.map { GroupItem(true, it) })
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {


        itemList.forEachIndexed { index, item ->
            ElevatedCard(modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
                onClick = { itemList[index] = item.copy(isExpanded = !item.isExpanded) }) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row {
                        Text(
                            text = item.taskGroup.title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium
                        )
                        MyIcon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    AnimatedVisibility(visible = item.isExpanded) {
                        LazyColumn() {
                            itemsIndexed(item.taskGroup.childList) { index, task ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Box(modifier = Modifier.size(48.dp), Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = task.name,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(text = task.desc ?: "", style = MaterialTheme.typography.bodySmall)
                                    }
                                    IconButton(onClick = { task.cancel() }) {
                                        MyIcon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "clear"
                                        )
                                    }
                                }
                            }
                        }
                    }

                }


            }

        }
    }
}

