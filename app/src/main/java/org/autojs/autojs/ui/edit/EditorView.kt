package org.autojs.autojs.ui.edit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.SparseBooleanArray
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.Snackbar
import com.stardust.autojs.engine.JavaScriptEngine
import com.stardust.autojs.engine.ScriptEngine
import com.stardust.autojs.execution.ScriptExecution
import com.stardust.pio.PFiles.getNameWithoutExtension
import com.stardust.pio.PFiles.move
import com.stardust.pio.PFiles.read
import com.stardust.pio.PFiles.write
import com.stardust.util.BackPressedHandler.HostActivity
import com.stardust.util.Callback
import com.stardust.util.ViewUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import org.androidannotations.annotations.AfterViews
import org.androidannotations.annotations.EViewGroup
import org.androidannotations.annotations.ViewById
import org.autojs.autojs.Pref
import org.autojs.autojs.autojs.AutoJs
import org.autojs.autojs.model.autocomplete.AutoCompletion
import org.autojs.autojs.model.autocomplete.CodeCompletions
import org.autojs.autojs.model.autocomplete.Symbols
import org.autojs.autojs.model.indices.Module
import org.autojs.autojs.model.indices.Property
import org.autojs.autojs.model.script.Scripts.ACTION_ON_EXECUTION_FINISHED
import org.autojs.autojs.model.script.Scripts.EXTRA_EXCEPTION_COLUMN_NUMBER
import org.autojs.autojs.model.script.Scripts.EXTRA_EXCEPTION_LINE_NUMBER
import org.autojs.autojs.model.script.Scripts.EXTRA_EXCEPTION_MESSAGE
import org.autojs.autojs.model.script.Scripts.openByOtherApps
import org.autojs.autojs.model.script.Scripts.runWithBroadcastSender
import org.autojs.autojs.tool.Observers
import org.autojs.autojs.ui.doc.ManualDialog
import org.autojs.autojs.ui.edit.completion.CodeCompletionBar
import org.autojs.autojs.ui.edit.completion.CodeCompletionBar.OnHintClickListener
import org.autojs.autojs.ui.edit.debug.DebugBar
import org.autojs.autojs.ui.edit.editor.CodeEditor
import org.autojs.autojs.ui.edit.keyboard.FunctionsKeyboardHelper
import org.autojs.autojs.ui.edit.keyboard.FunctionsKeyboardView
import org.autojs.autojs.ui.edit.keyboard.FunctionsKeyboardView.ClickCallback
import org.autojs.autojs.ui.edit.theme.Theme
import org.autojs.autojs.ui.edit.theme.Themes.getAllThemes
import org.autojs.autojs.ui.edit.theme.Themes.getCurrent
import org.autojs.autojs.ui.edit.theme.Themes.setCurrent
import org.autojs.autojs.ui.edit.toolbar.*
import org.autojs.autojs.ui.log.LogActivityKt.Companion.start
import org.autojs.autojs.ui.widget.EWebView
import org.autojs.autojs.ui.widget.SimpleTextWatcher
import org.autojs.autojs.ui.widget.SwipeRefreshWebView
import org.openautojs.autojs.R
import java.io.File

/**
 * Created by Stardust on 2017/9/28.
 */
@SuppressLint("NonConstantResourceId")
@EViewGroup(R.layout.editor_view)
open class EditorView : FrameLayout, OnHintClickListener, ClickCallback,
    ToolbarFragment.OnMenuItemClickListener {
    @ViewById(R.id.editor)
    @JvmField
    var editor: CodeEditor? = null

    @JvmField
    @ViewById(R.id.code_completion_bar)
    var mCodeCompletionBar: CodeCompletionBar? = null

    @JvmField
    @ViewById(R.id.input_method_enhance_bar)
    var mInputMethodEnhanceBar: View? = null

    @JvmField
    @ViewById(R.id.symbol_bar)
    var mSymbolBar: CodeCompletionBar? = null

    @JvmField
    @ViewById(R.id.functions)
    var mShowFunctionsButton: ImageView? = null

    @JvmField
    @ViewById(R.id.functions_keyboard)
    var mFunctionsKeyboard: FunctionsKeyboardView? = null

    @ViewById(R.id.debug_bar)
    @JvmField
    var debugBar: DebugBar? = null

    @JvmField
    @ViewById(R.id.docs)
    var mDocsWebView: SwipeRefreshWebView? = null

    @JvmField
    @ViewById(R.id.drawer_layout)
    var mDrawerLayout: DrawerLayout? = null

    var name: String? = null
        private set
    var uri: Uri? = null
        private set
    private var mReadOnly = false
    var scriptExecutionId = 0
        private set
    private var mAutoCompletion: AutoCompletion? = null
    private var mEditorTheme: Theme? = null
    private var mFunctionsKeyboardHelper: FunctionsKeyboardHelper? = null
    private val mOnRunFinishedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_ON_EXECUTION_FINISHED == intent.action) {
                scriptExecutionId = ScriptExecution.NO_ID
                if (mDebugging) {
                    exitDebugging()
                }
                setMenuItemStatus(R.id.run, true)
                val msg = intent.getStringExtra(EXTRA_EXCEPTION_MESSAGE)
                val line = intent.getIntExtra(EXTRA_EXCEPTION_LINE_NUMBER, -1)
                val col = intent.getIntExtra(EXTRA_EXCEPTION_COLUMN_NUMBER, 0)
                if (line >= 1) {
                    editor!!.jumpTo(line - 1, col)
                }
                msg?.let { showErrorMessage(it) }
            }
        }
    }
    private val mMenuItemStatus = SparseBooleanArray()
    private var mRestoredText: String? = null
    private val mNormalToolbar: NormalToolbarFragment = NormalToolbarFragment_()
    private var mDebugging = false

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        context.registerReceiver(mOnRunFinishedReceiver, IntentFilter(ACTION_ON_EXECUTION_FINISHED))
        if (context is HostActivity) {
            (context as HostActivity).backPressedObserver.registerHandler(mFunctionsKeyboardHelper)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        context.unregisterReceiver(mOnRunFinishedReceiver)
        if (context is HostActivity) {
            (context as HostActivity).backPressedObserver.unregisterHandler(mFunctionsKeyboardHelper)
        }
    }

    fun handleIntent(intent: Intent): Observable<String> {
        name = intent.getStringExtra(EXTRA_NAME)
        return handleText(intent)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { str: String? ->
                mReadOnly = intent.getBooleanExtra(EXTRA_READ_ONLY, false)
                val saveEnabled = intent.getBooleanExtra(EXTRA_SAVE_ENABLED, true)
                if (mReadOnly || !saveEnabled) {
                    findViewById<View>(R.id.save).visibility = GONE
                }
                if (!intent.getBooleanExtra(EXTRA_RUN_ENABLED, true)) {
                    findViewById<View>(R.id.run).visibility = GONE
                }
                if (mReadOnly) {
                    editor!!.setReadOnly(true)
                }
            }
    }

    fun setRestoredText(text: String?) {
        mRestoredText = text
        editor!!.text = text
    }

    private fun handleText(intent: Intent): Observable<String> {
        val path = intent.getStringExtra(EXTRA_PATH)
        val content = intent.getStringExtra(EXTRA_CONTENT)
        return if (content != null) {
            setInitialText(content)
            Observable.just(content)
        } else {
            if (path == null) {
                if (intent.data == null) {
                    return Observable.error(
                        IllegalArgumentException(
                            "path and content is empty"
                        )
                    )
                } else {
                    uri = intent.data
                }
            } else {
                uri = Uri.fromFile(File(path))
            }
            if (name == null) {
                name = getNameWithoutExtension(uri!!.path!!)
            }
            loadUri(uri)
        }
    }

    @SuppressLint("CheckResult")
    private fun loadUri(uri: Uri?): Observable<String> {
        editor!!.setProgress(true)
        return Observable.fromCallable {
            read(
                context.contentResolver.openInputStream(uri!!)!!
            )
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { s: String ->
                setInitialText(s)
                editor!!.setProgress(false)
            }
    }

    private fun setInitialText(text: String) {
        if (mRestoredText != null) {
            editor!!.text = mRestoredText
            mRestoredText = null
            return
        }
        editor!!.setInitialText(text)
    }

    private fun setMenuItemStatus(id: Int, enabled: Boolean) {
        mMenuItemStatus.put(id, enabled)
        val fragment = activity.supportFragmentManager
            .findFragmentById(R.id.toolbar_menu) as ToolbarFragment?
        if (fragment == null) {
            mNormalToolbar.setMenuItemStatus(id, enabled)
        } else {
            fragment.setMenuItemStatus(id, enabled)
        }
    }

    fun getMenuItemStatus(id: Int, defValue: Boolean): Boolean {
        return mMenuItemStatus[id, defValue]
    }

    @SuppressLint("CheckResult")
    @AfterViews
    fun init() {
        //setTheme(Theme.getDefault(getContext()));
        setUpEditor()
        setUpInputMethodEnhancedBar()
        setUpFunctionsKeyboard()
        setMenuItemStatus(R.id.save, false)

        mDocsWebView!!.webView.settings.displayZoomControls = true
        mDocsWebView!!.webView.loadUrl(Pref.getDocumentationUrl())

        getCurrent(context)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer { theme: Theme? -> setTheme(theme) })
        initNormalToolbar()
    }

    private fun initNormalToolbar() {
        mNormalToolbar.setOnMenuItemClickListener(this)
        mNormalToolbar.setOnMenuItemLongClickListener { id: Int ->
            if (id == R.id.run) {
                debug()
                return@setOnMenuItemLongClickListener true
            }
            false
        }
        val fragment = activity.supportFragmentManager.findFragmentById(R.id.toolbar_menu)
        if (fragment == null) {
            showNormalToolbar()
        }
    }

    private fun setUpFunctionsKeyboard() {
        mFunctionsKeyboardHelper = FunctionsKeyboardHelper.with(context as Activity)
            .setContent(editor)
            .setFunctionsTrigger(mShowFunctionsButton)
            .setFunctionsView(mFunctionsKeyboard)
            .setEditView(editor!!.codeEditText)
            .build()
        mFunctionsKeyboard!!.setClickCallback(this)
    }

    private fun setUpInputMethodEnhancedBar() {
        mSymbolBar!!.codeCompletions = Symbols.getSymbols()
        mCodeCompletionBar!!.setOnHintClickListener(this)
        mSymbolBar!!.setOnHintClickListener(this)
        mAutoCompletion = AutoCompletion(context, editor!!.codeEditText)
        mAutoCompletion!!.setAutoCompleteCallback { codeCompletions: CodeCompletions? ->
            mCodeCompletionBar!!.codeCompletions = codeCompletions
        }
    }

    private fun setUpEditor() {
        editor!!.codeEditText.addTextChangedListener(SimpleTextWatcher { s: Editable? ->
            setMenuItemStatus(R.id.save, editor!!.isTextChanged)
            setMenuItemStatus(R.id.undo, editor!!.canUndo())
            setMenuItemStatus(R.id.redo, editor!!.canRedo())
        })
        editor!!.addCursorChangeCallback { line: String, cursor: Int -> autoComplete(line, cursor) }
        editor!!.codeEditText.textSize = Pref.getEditorTextSize(
            ViewUtils.pxToSp(
                context, editor!!.codeEditText.textSize
            ).toInt()
        ).toFloat()
    }

    private fun autoComplete(line: String, cursor: Int) {
        mAutoCompletion!!.onCursorChange(line, cursor)
    }

    fun setTheme(theme: Theme?) {
        mEditorTheme = theme
        editor!!.setTheme(theme)
        mInputMethodEnhanceBar!!.setBackgroundColor(theme!!.imeBarBackgroundColor)
        val textColor = theme.imeBarForegroundColor
        mCodeCompletionBar!!.setTextColor(textColor)
        mSymbolBar!!.setTextColor(textColor)
        mShowFunctionsButton!!.setColorFilter(textColor)
        invalidate()
    }

    fun onBackPressed(): Boolean {
        if (mDrawerLayout!!.isDrawerOpen(GravityCompat.START)) {

            if (mDocsWebView!!.webView.canGoBack()) {
                mDocsWebView!!.webView.goBack()
            } else {
                mDrawerLayout!!.closeDrawer(GravityCompat.START)
            }

            return true
        }
        return false
    }

    override fun onToolbarMenuItemClick(id: Int) {
        when (id) {
            R.id.run -> runAndSaveFileIfNeeded()
            R.id.save -> saveFile()
            R.id.undo -> undo()
            R.id.redo -> redo()
            R.id.replace -> replace()
            R.id.find_next -> findNext()
            R.id.find_prev -> findPrev()
            R.id.cancel_search -> cancelSearch()
            R.id.textSizePlus -> setTextSizePlus()
            R.id.textSizeMinus -> setTextSizeMinus()
        }
    }

    @SuppressLint("CheckResult")
    fun runAndSaveFileIfNeeded() {
        save().observeOn(AndroidSchedulers.mainThread())
            .subscribe({ s: String? -> run(true) }, Observers.toastMessage())
    }

    fun run(showMessage: Boolean): ScriptExecution? {
        if (showMessage) {
            Snackbar.make(this, R.string.text_start_running, Snackbar.LENGTH_SHORT).show()
        }
        // TODO: 2018/10/24
        val execution = runWithBroadcastSender(File(uri!!.path))
            ?: return null
        scriptExecutionId = execution.id
        setMenuItemStatus(R.id.run, false)
        return execution
    }

    fun undo() {
        editor!!.undo()
    }

    fun redo() {
        editor!!.redo()
    }

    fun save(): Observable<String> {
        val path = uri!!.path
        val backPath = "$path.b_a_k"
        move(path!!, backPath)
        return Observable.just(editor!!.text)
            .observeOn(Schedulers.io())
            .doOnNext { s: String? ->
                write(
                    context.contentResolver.openOutputStream(
                        uri!!
                    )!!, s!!
                )
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { s: String? ->
                editor!!.markTextAsSaved()
                setMenuItemStatus(R.id.save, false)
            }
            .doOnNext { s: String? -> File(backPath).delete() }
    }

    fun forceStop() {
        doWithCurrentEngine { obj: ScriptEngine<*> -> obj.forceStop() }
    }

    private fun doWithCurrentEngine(callback: Callback<ScriptEngine<*>>) {
        val execution = AutoJs.getInstance().scriptEngineService.getScriptExecution(
            scriptExecutionId
        )
        if (execution != null) {
            val engine = execution.engine
            if (engine != null) {
                callback.call(engine)
            }
        }
    }

    @SuppressLint("CheckResult")
    fun saveFile() {
        save()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Observers.emptyConsumer()) { e: Throwable ->
                e.printStackTrace()
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    fun findNext() {
        editor!!.findNext()
    }

    fun findPrev() {
        editor!!.findPrev()
    }

    fun cancelSearch() {
        showNormalToolbar()
    }

    private fun showNormalToolbar() {
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.toolbar_menu, mNormalToolbar)
            .commitAllowingStateLoss()
    }

    val activity: FragmentActivity
        get() {
            var context = context
            while (context !is Activity && context is ContextWrapper) {
                context = context.baseContext
            }
            return context as FragmentActivity
        }

    fun replace() {
        editor!!.replaceSelection()
    }

    val isTextChanged: Boolean
        get() = editor!!.isTextChanged

    fun showConsole() {
        doWithCurrentEngine { engine: ScriptEngine<*> -> (engine as JavaScriptEngine).runtime.console.show() }
    }

    fun openByOtherApps() {
        if (uri != null) {
            openByOtherApps(uri!!)
        }
    }

    fun beautifyCode() {
        editor!!.beautifyCode()
    }

    @SuppressLint("CheckResult")
    fun selectEditorTheme() {
        editor!!.setProgress(true)
        getAllThemes(context)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                editor!!.setProgress(false)
                it?.let { it1 -> selectEditorTheme(it1) }
            }
    }

    fun selectTextSize() {
        TextSizeSettingDialogBuilder(context)
            .initialValue(ViewUtils.pxToSp(context, editor!!.codeEditText.textSize).toInt())
            .callback { value: Int -> setTextSize(value) }
            .show()
    }

    fun setTextSize(value: Int) {
        Pref.setEditorTextSize(value)
        editor!!.codeEditText.textSize = value.toFloat()
    }

    fun setTextSizePlus() {
        val value = ViewUtils.pxToSp(context, editor!!.codeEditText.textSize).toInt()
        Pref.setEditorTextSize(Math.min(value + 2, 60))
        editor!!.codeEditText.textSize = Math.min(value + 2, 60).toFloat()
    }

    fun setTextSizeMinus() {
        val value = ViewUtils.pxToSp(context, editor!!.codeEditText.textSize).toInt()
        Pref.setEditorTextSize(Math.max(value - 2, 2))
        editor!!.codeEditText.textSize = Math.max(value - 2, 2).toFloat()
    }

    private fun selectEditorTheme(themes: List<Theme?>) {
        var i = themes.indexOf(mEditorTheme)
        if (i < 0) {
            i = 0
        }
        MaterialDialog.Builder(context)
            .title(R.string.text_editor_theme)
            .items(themes)
            .itemsCallbackSingleChoice(i) { dialog: MaterialDialog?, itemView: View?, which: Int, text: CharSequence? ->
                setTheme(
                    themes[which]
                )
                setCurrent(themes[which]!!.name)
                true
            }
            .show()
    }

    @Throws(CodeEditor.CheckedPatternSyntaxException::class)
    fun find(keywords: String?, usingRegex: Boolean) {
        editor!!.find(keywords, usingRegex)
        showSearchToolbar(false)
    }

    private fun showSearchToolbar(showReplaceItem: Boolean) {
        val searchToolbarFragment = SearchToolbarFragment_.builder()
            .arg(SearchToolbarFragment.ARGUMENT_SHOW_REPLACE_ITEM, showReplaceItem)
            .build()
        searchToolbarFragment.setOnMenuItemClickListener(this)
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.toolbar_menu, searchToolbarFragment)
            .commit()
    }

    @Throws(CodeEditor.CheckedPatternSyntaxException::class)
    fun replace(keywords: String?, replacement: String?, usingRegex: Boolean) {
        editor!!.replace(keywords, replacement, usingRegex)
        showSearchToolbar(true)
    }

    @Throws(CodeEditor.CheckedPatternSyntaxException::class)
    fun replaceAll(keywords: String?, replacement: String?, usingRegex: Boolean) {
        editor!!.replaceAll(keywords, replacement, usingRegex)
    }

    fun debug() {
        val debugToolbarFragment = DebugToolbarFragment_.builder()
            .build()
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.toolbar_menu, debugToolbarFragment)
            .commit()
        debugBar!!.visibility = VISIBLE
        mInputMethodEnhanceBar!!.visibility = GONE
        mDebugging = true
    }

    fun exitDebugging() {
        val fragmentManager = activity.supportFragmentManager
        val fragment = fragmentManager.findFragmentById(R.id.toolbar_menu)
        if (fragment is DebugToolbarFragment) {
            fragment.detachDebugger()
        }
        showNormalToolbar()
        editor!!.setDebuggingLine(-1)
        debugBar!!.visibility = GONE
        mInputMethodEnhanceBar!!.visibility = VISIBLE
        mDebugging = false
    }

    private fun showErrorMessage(msg: String) {
        Snackbar.make(
            this@EditorView,
            resources.getString(R.string.text_error) + ": " + msg,
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.text_detail) { v: View? ->
                start(
                    context
                )
            }
            .show()
    }

    override fun onHintClick(completions: CodeCompletions, pos: Int) {
        val completion = completions[pos]
        editor!!.insert(completion.insertText)
    }

    override fun onHintLongClick(completions: CodeCompletions, pos: Int) {
        val completion = completions[pos]
        if (completion.url == null) return
        showManual(completion.url, completion.hint)
    }

    private fun showManual(url: String, title: String) {
        val absUrl = Pref.getDocumentationUrl() + url
        ManualDialog(context)
            .title(title)
            .url(absUrl)
            .pinToLeft { v: View? ->

                mDocsWebView!!.webView.loadUrl(absUrl)
                mDrawerLayout!!.openDrawer(GravityCompat.START)

            }
            .show()
    }

    override fun onModuleLongClick(module: Module) {
        showManual(module.url, module.name)
    }

    override fun onPropertyClick(m: Module, property: Property) {
        var p = property.key
        if (!property.isVariable) {
            p = "$p()"
        }
        if (property.isGlobal) {
            editor!!.insert(p)
        } else {
            editor!!.insert(m.name + "." + p)
        }
        if (!property.isVariable) {
            editor!!.moveCursor(-1)
        }
        mFunctionsKeyboardHelper!!.hideFunctionsLayout(true)
    }

    override fun onPropertyLongClick(m: Module, property: Property) {
        if (TextUtils.isEmpty(property.url)) {
            showManual(m.url, property.key)
        } else {
            showManual(property.url, property.key)
        }
    }

    val scriptExecution: ScriptExecution?
        get() = AutoJs.getInstance().scriptEngineService.getScriptExecution(
            scriptExecutionId
        )

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        val superData = super.onSaveInstanceState()
        bundle.putParcelable("super_data", superData)
        bundle.putInt("script_execution_id", scriptExecutionId)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val bundle = state as Bundle
        val superData = bundle.getParcelable<Parcelable>("super_data")
        scriptExecutionId = bundle.getInt("script_execution_id", ScriptExecution.NO_ID)
        super.onRestoreInstanceState(superData)
        setMenuItemStatus(R.id.run, scriptExecutionId == ScriptExecution.NO_ID)
    }

    fun destroy() {
        editor!!.destroy()
        mAutoCompletion!!.shutdown()
    }

    companion object {
        const val EXTRA_PATH = "path"
        const val EXTRA_NAME = "name"
        const val EXTRA_CONTENT = "content"
        const val EXTRA_READ_ONLY = "readOnly"
        const val EXTRA_SAVE_ENABLED = "saveEnabled"
        const val EXTRA_RUN_ENABLED = "runEnabled"
    }
}