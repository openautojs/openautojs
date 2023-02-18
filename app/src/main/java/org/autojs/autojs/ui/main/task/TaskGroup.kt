package org.autojs.autojs.ui.main.task

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.bignerdranch.expandablerecyclerview.model.Parent
import com.stardust.autojs.execution.ScriptExecution
import org.autojs.autojs.autojs.AutoJs
import org.autojs.autojs.timing.IntentTask
import org.autojs.autojs.timing.TimedTask
import org.autojs.autojs.timing.TimedTaskManager.allIntentTasksAsList
import org.autojs.autojs.timing.TimedTaskManager.allTasksAsList
import org.autojs.autojs.ui.main.task.Task.PendingTask
import org.autojs.autojs.ui.main.task.Task.RunningTask
import org.openautojs.autojs.R

/**
 * Created by Stardust on 2017/11/28.
 */
abstract class TaskGroup protected constructor(val title: String) : Parent<Task> {
    protected var mTasks = mutableStateListOf<Task>()

    override fun getChildList(): SnapshotStateList<Task> {
        return mTasks
    }

    override fun isInitiallyExpanded(): Boolean {
        return true
    }

    abstract fun refresh()
    class PendingTaskGroup(context: Context) :
        TaskGroup(context.getString(R.string.text_timed_task)) {
        init {
            refresh()
        }

        override fun refresh() {
            mTasks.clear()
            for (timedTask in allTasksAsList) {
                mTasks.add(PendingTask(timedTask))
            }
            for (intentTask in allIntentTasksAsList) {
                mTasks.add(PendingTask(intentTask))
            }
        }

        fun addTask(task: Any): Int {
            val pos = mTasks.size
            when (task) {
                is TimedTask -> {
                    mTasks.add(PendingTask(task))
                }
                is IntentTask -> {
                    mTasks.add(PendingTask(task))
                }
                else -> {
                    throw IllegalArgumentException("task = $task")
                }
            }
            return pos
        }

        fun removeTask(data: Any): Int {
            val i = indexOf(data)
            if (i >= 0) mTasks.removeAt(i)
            return i
        }

        private fun indexOf(data: Any): Int {
            for (i in mTasks.indices) {
                val task = mTasks[i] as PendingTask
                if (task.taskEquals(data)) {
                    return i
                }
            }
            return -1
        }

        fun updateTask(task: Any): Int {
            val i = indexOf(task)
            if (i >= 0) {
                when (task) {
                    is TimedTask -> {
                        (mTasks[i] as PendingTask).timedTask = task
                    }
                    is IntentTask -> {
                        (mTasks[i] as PendingTask).setIntentTask(task)
                    }
                    else -> {
                        throw IllegalArgumentException("task = $task")
                    }
                }
            }
            return i
        }
    }

    class RunningTaskGroup(context: Context) :
        TaskGroup(context.getString(R.string.text_running_task)) {
        init {
            refresh()
        }

        override fun refresh() {
            val executions = AutoJs.getInstance().scriptEngineService.scriptExecutions
            mTasks.clear()
            for (execution in executions) {
                mTasks.add(RunningTask(execution))
            }
        }

        fun addTask(engine: ScriptExecution): Int {
            val pos = mTasks.size
            mTasks.add(RunningTask(engine))
            return pos
        }

        fun removeTask(engine: ScriptExecution): Int {
            val i = indexOf(engine)
            if (i >= 0) {
                mTasks.removeAt(i)
            }
            return i
        }

        fun indexOf(engine: ScriptExecution): Int {
            for (i in mTasks.indices) {
                if ((mTasks[i] as RunningTask).scriptExecution == engine) {
                    return i
                }
            }
            return -1
        }
    }
}