package org.autojs.autojs.ui.main.task

import com.stardust.app.GlobalAppContext.getString
import com.stardust.autojs.execution.ScriptExecution
import com.stardust.autojs.script.AutoFileSource
import com.stardust.autojs.script.JavaScriptSource
import com.stardust.pio.PFiles.getSimplifiedPath
import org.autojs.autojs.timing.IntentTask
import org.autojs.autojs.timing.TimedTask
import org.autojs.autojs.timing.TimedTaskManager.removeTask
import org.autojs.autojs.ui.timing.TimedTaskSettingActivity
import org.joda.time.format.DateTimeFormat
import org.openautojs.autojs.R

/**
 * Created by Stardust on 2017/11/28.
 */
abstract class Task {
    abstract val name: String
    abstract val desc: String?
    abstract fun cancel()
    abstract val engineName: String

    data class PendingTask(val task: Any) : Task() {
        var timedTask: TimedTask?
        var mIntentTask: IntentTask?

        init {
            when (task) {
                is TimedTask -> {
                    this.timedTask = task
                    mIntentTask = null
                }
                is IntentTask -> {
                    mIntentTask = task
                    timedTask = null
                }
                else -> {
                    throw Exception("Unsupported types: ${task.javaClass}")
                }
            }
        }

        fun taskEquals(task: Any): Boolean {
            return if (timedTask != null) {
                timedTask == task
            } else mIntentTask == task
        }

        override val name: String
            get() = getSimplifiedPath(scriptPath!!)

        override val desc: String?
            get() {
                return if (timedTask != null) {
                    val nextTime = timedTask!!.nextTime
                    getString(R.string.text_next_run_time) + ": " +
                            DateTimeFormat.forPattern("yyyy/MM/dd HH:mm").print(nextTime)
                } else {
                    assert(mIntentTask != null)
                    val desc = TimedTaskSettingActivity.ACTION_DESC_MAP[mIntentTask!!.action]
                    if (desc != null) {
                        getString(desc)
                    } else mIntentTask!!.action
                }
            }

        override fun cancel() {
            if (timedTask != null) {
                removeTask(timedTask!!)
            } else {
                removeTask(mIntentTask!!)
            }
        }

        override val engineName: String
            get() {
                return if (scriptPath!!.endsWith(".js")) {
                    JavaScriptSource.ENGINE
                } else {
                    AutoFileSource.ENGINE
                }
            }
        private val scriptPath: String?
            get() = if (timedTask != null) {
                timedTask!!.scriptPath
            } else {
                assert(mIntentTask != null)
                mIntentTask!!.scriptPath
            }


        fun setIntentTask(intentTask: IntentTask?) {
            mIntentTask = intentTask
        }

        val id: Long
            get() = if (timedTask != null) timedTask!!.id else mIntentTask!!.id
    }

    data class RunningTask(val scriptExecution: ScriptExecution) : Task() {


        override val name: String
            get() = scriptExecution.source.name

        override val desc: String
            get() = scriptExecution.source.toString()

        override fun cancel() {
            val engine = scriptExecution.engine
            engine?.forceStop()
        }

        override val engineName: String
            get() = scriptExecution.source.engineName

    }
}