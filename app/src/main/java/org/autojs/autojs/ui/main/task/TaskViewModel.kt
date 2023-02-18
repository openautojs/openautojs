package org.autojs.autojs.ui.main.task

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.stardust.autojs.execution.ScriptExecution
import com.stardust.autojs.execution.ScriptExecutionListener
import com.stardust.autojs.execution.SimpleScriptExecutionListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.autojs.autojs.autojs.AutoJs
import org.autojs.autojs.storage.database.ModelChange
import org.autojs.autojs.timing.TimedTaskManager

data class TaskGroup1(
    val title:String
)
class TaskViewModel(application: Application) : AndroidViewModel(application) {


    private var runningTaskGroup: TaskGroup.RunningTaskGroup = TaskGroup.RunningTaskGroup(getApplication())
    private var pendingTaskGroup: TaskGroup.PendingTaskGroup = TaskGroup.PendingTaskGroup(getApplication())
    val taskGroups: MutableList<TaskGroup> = mutableListOf(runningTaskGroup,pendingTaskGroup)

    private var timedTaskChangeDisposable: Disposable? = null
    private var intentTaskChangeDisposable: Disposable? = null

    private val scriptExecutionListener: ScriptExecutionListener =
        object : SimpleScriptExecutionListener() {
            override fun onStart(execution: ScriptExecution?) {
                execution?.let { runningTaskGroup.addTask(it) }
            }

            override fun onSuccess(execution: ScriptExecution?, result: Any?) {
                onFinish(execution)
            }

            override fun onException(execution: ScriptExecution?, e: Throwable?) {
                onFinish(execution)
            }

            private fun onFinish(execution: ScriptExecution?) {
                execution?.let { runningTaskGroup.removeTask(it) }
//            post {
//                val i = mRunningTaskGroup!!.removeTask(execution)
//                if (i >= 0) {
//                    mAdapter!!.notifyChildRemoved(0, i)
//                } else {
//                    refresh()
//                }
//            }
            }
        }

    init {
        AutoJs.getInstance().scriptEngineService.registerGlobalScriptExecutionListener(
            scriptExecutionListener
        )
        timedTaskChangeDisposable = TimedTaskManager.timeTaskChanges
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { onTaskChange(it) }
        intentTaskChangeDisposable = TimedTaskManager.intentTaskChanges
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { onTaskChange(it) }
    }

    fun onTaskChange(taskChange: ModelChange<*>) {
        if (taskChange.action == ModelChange.INSERT) {
            pendingTaskGroup.addTask(taskChange.data)
//        mAdapter!!.notifyChildInserted(1, mPendingTaskGroup!!.addTask(taskChange.data))
        } else if (taskChange.action == ModelChange.DELETE) {
            val i = pendingTaskGroup.removeTask(taskChange.data)
            if (i >= 0) {
//            mAdapter!!.notifyChildRemoved(1, i)
            } else {
//            Log.w(TaskListRecyclerView.LOG_TAG, "data inconsistent on change: $taskChange")
//            refresh()
            }
        } else if (taskChange.action == ModelChange.UPDATE) {
            val i = pendingTaskGroup.updateTask(taskChange.data)
//        if (i >= 0) {
//            mAdapter!!.notifyChildChanged(1, i)
//        } else {
//            refresh()
//        }
        }
    }

    override fun onCleared() {
        super.onCleared()
        AutoJs.getInstance().scriptEngineService.unregisterGlobalScriptExecutionListener(
            scriptExecutionListener
        )
        timedTaskChangeDisposable!!.dispose()
        intentTaskChangeDisposable!!.dispose()
    }
}