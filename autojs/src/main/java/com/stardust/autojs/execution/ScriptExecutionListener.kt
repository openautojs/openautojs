package com.stardust.autojs.execution

import java.io.Serializable

/**
 * Created by Stardust on 2017/4/2.
 */
interface ScriptExecutionListener : Serializable {
    fun onStart(execution: ScriptExecution?)
    fun onSuccess(execution: ScriptExecution?, result: Any?)
    fun onException(execution: ScriptExecution?, e: Throwable?)
}