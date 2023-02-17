package org.autojs.autojs.ui.edit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.stardust.app.OnActivityResultDelegate
import com.stardust.app.OnActivityResultDelegate.DelegateHost
import com.stardust.autojs.core.permission.OnRequestPermissionsResultCallback
import com.stardust.autojs.core.permission.PermissionRequestProxyActivity
import com.stardust.autojs.core.permission.RequestPermissionCallbacks
import com.stardust.autojs.execution.ScriptExecution
import com.stardust.pio.PFiles
import com.stardust.pio.PFiles.write
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.androidannotations.annotations.AfterViews
import org.androidannotations.annotations.EActivity
import org.androidannotations.annotations.ViewById
import org.autojs.autojs.storage.file.TmpScriptFiles
import org.autojs.autojs.theme.dialog.ThemeColorMaterialDialogBuilder
import org.autojs.autojs.tool.Observers
import org.autojs.autojs.ui.BaseActivity
import org.autojs.autojs.ui.main.MainActivity
import org.openautojs.autojs.R
import java.io.File
import java.io.IOException

/**
 * Created by Stardust on 2017/1/29.
 */
@EActivity(R.layout.activity_edit)
open class EditActivity : BaseActivity(), DelegateHost, PermissionRequestProxyActivity {
    private val mMediator = OnActivityResultDelegate.Mediator()

    @JvmField
    @ViewById(R.id.editor_view)
    var mEditorView: EditorView? = null

    private var mEditorMenu: EditorMenu? = null
    private val mRequestPermissionCallbacks = RequestPermissionCallbacks()
    private var mNewTask = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mNewTask = intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0
    }

    @SuppressLint("CheckResult")
    @AfterViews
    fun setUpViews() {
        mEditorView!!.handleIntent(intent)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                Observers.emptyConsumer()
            ) { ex: Throwable -> onLoadFileError(ex.message) }
        mEditorMenu = EditorMenu(mEditorView)
        setUpToolbar()
    }

    override fun onWindowStartingActionMode(callback: ActionMode.Callback): ActionMode? {
        return super.onWindowStartingActionMode(callback)
    }

    override fun onWindowStartingActionMode(callback: ActionMode.Callback, type: Int): ActionMode? {
        return super.onWindowStartingActionMode(callback, type)
    }

    private fun onLoadFileError(message: String?) {
        ThemeColorMaterialDialogBuilder(this)
            .title(getString(R.string.text_cannot_read_file))
            .content(message!!)
            .positiveText(R.string.text_exit)
            .cancelable(false)
            .onPositive { dialog: MaterialDialog?, which: DialogAction? -> finish() }
            .show()
    }

    private fun setUpToolbar() {
//        setToolbarAsBack(this, R.id.toolbar, mEditorView!!.name)
        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = title
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            toolbar.setNavigationIcon(R.drawable.outline_menu_24)
            toolbar.setNavigationOnClickListener {
                mEditorView?.mDrawerLayout?.let {
                    if (it.isOpen) it.close() else it.open()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_editor, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return mEditorMenu!!.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        Log.d(LOG_TAG, "onPrepareOptionsMenu: $menu")
        val isScriptRunning = mEditorView!!.scriptExecutionId != ScriptExecution.NO_ID
        val forceStopItem = menu.findItem(R.id.action_force_stop)
        forceStopItem.isEnabled = isScriptRunning
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onActionModeStarted(mode: ActionMode) {
        Log.d(LOG_TAG, "onActionModeStarted: $mode")
        val menu = mode.menu
        val item = menu.getItem(menu.size() - 1)
        // 以下两项在64位版存在失效bug，暂时注释掉，相关功能可通过编辑菜单使用
//        menu.add(item.getGroupId(), R.id.action_delete_line, 10000, R.string.text_delete_line);
//        menu.add(item.getGroupId(), R.id.action_copy_line, 20000, R.string.text_copy_line);
        super.onActionModeStarted(mode)
    }

    override fun onSupportActionModeStarted(mode: androidx.appcompat.view.ActionMode) {
        Log.d(LOG_TAG, "onSupportActionModeStarted: mode = $mode")
        super.onSupportActionModeStarted(mode)
    }

    override fun onWindowStartingSupportActionMode(callback: androidx.appcompat.view.ActionMode.Callback): androidx.appcompat.view.ActionMode? {
        Log.d(LOG_TAG, "onWindowStartingSupportActionMode: callback = $callback")
        return super.onWindowStartingSupportActionMode(callback)
    }

    override fun startActionMode(callback: ActionMode.Callback, type: Int): ActionMode? {
        Log.d(LOG_TAG, "startActionMode: callback = $callback, type = $type")
        return super.startActionMode(callback, type)
    }

    override fun startActionMode(callback: ActionMode.Callback): ActionMode? {
        Log.d(LOG_TAG, "startActionMode: callback = $callback")
        return super.startActionMode(callback)
    }

    override fun onBackPressed() {
        if (!mEditorView!!.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun finish() {
        if (mEditorView!!.isTextChanged) {
            showExitConfirmDialog()
            return
        }
        finishAndRemoveFromRecents()
    }

    private fun finishAndRemoveFromRecents() {
        finishAndRemoveTask()
        if (mNewTask) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun showExitConfirmDialog() {
        ThemeColorMaterialDialogBuilder(this)
            .title(R.string.text_alert)
            .content(R.string.edit_exit_without_save_warn)
            .positiveText(R.string.text_cancel)
            .negativeText(R.string.text_save_and_exit)
            .neutralText(R.string.text_exit_directly)
            .onNegative { dialog: MaterialDialog?, which: DialogAction? ->
                mEditorView!!.saveFile()
                finishAndRemoveFromRecents()
            }
            .onNeutral { dialog: MaterialDialog?, which: DialogAction? -> finishAndRemoveFromRecents() }
            .show()
    }

    override fun onDestroy() {
        mEditorView!!.destroy()
        super.onDestroy()
    }

    override fun getOnActivityResultDelegateMediator(): OnActivityResultDelegate.Mediator {
        return mMediator
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mMediator.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (!mEditorView!!.isTextChanged) {
            return
        }
        val text = mEditorView!!.editor!!.text
        if (text.length < 256 * 1024) {
            outState.putString("text", text)
        } else {
            val tmp = saveToTmpFile(text)
            if (tmp != null) {
                outState.putString("path", tmp.path)
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun saveToTmpFile(text: String): File? {
        return try {
            val tmp = TmpScriptFiles.create(this)
            Observable.just(text)
                .observeOn(Schedulers.io())
                .subscribe { t: String? -> write(tmp, t!!) }
            tmp
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    @SuppressLint("CheckResult")
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val text = savedInstanceState.getString("text")
        if (text != null) {
            mEditorView!!.setRestoredText(text)
            return
        }
        val path = savedInstanceState.getString("path")
        if (path != null) {
            Observable.just(path)
                .observeOn(Schedulers.io())
                .map { PFiles.read(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ t: String? ->
                    mEditorView!!.editor!!.text = t
                }) { obj: Throwable -> obj.printStackTrace() }
        }
    }

    override fun addRequestPermissionsCallback(callback: OnRequestPermissionsResultCallback) {
        mRequestPermissionCallbacks.addCallback(callback)
    }

    override fun removeRequestPermissionsCallback(callback: OnRequestPermissionsResultCallback): Boolean {
        return mRequestPermissionCallbacks.removeCallback(callback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mRequestPermissionCallbacks.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )
    }

    companion object {
        private const val LOG_TAG = "EditActivity"

        @JvmStatic
        fun editFile(context: Context, path: String?, newTask: Boolean) {
            editFile(context, null, path, newTask)
        }

        @JvmStatic
        fun editFile(context: Context, uri: Uri?, newTask: Boolean) {
            context.startActivity(
                newIntent(context, newTask)
                    .setData(uri)
            )
        }

        @JvmStatic
        fun editFile(context: Context, name: String?, path: String?, newTask: Boolean) {
            context.startActivity(
                newIntent(context, newTask)
                    .putExtra(EditorView.EXTRA_PATH, path)
                    .putExtra(EditorView.EXTRA_NAME, name)
            )
        }

        @JvmStatic
        fun viewContent(context: Context, name: String?, content: String?, newTask: Boolean) {
            context.startActivity(
                newIntent(context, newTask)
                    .putExtra(EditorView.EXTRA_CONTENT, content)
                    .putExtra(EditorView.EXTRA_NAME, name)
                    .putExtra(EditorView.EXTRA_READ_ONLY, true)
            )
        }

        @JvmStatic
        private fun newIntent(context: Context, newTask: Boolean): Intent {
            val intent = Intent(context, EditActivity_::class.java)
            if (newTask || context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return intent
        }
    }
}