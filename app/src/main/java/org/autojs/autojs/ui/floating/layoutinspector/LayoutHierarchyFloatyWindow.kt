@file:OptIn(ExperimentalMaterial3Api::class)

package org.autojs.autojs.ui.floating.layoutinspector

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.view.ContextThemeWrapper
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.compositionContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.*
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.stardust.app.DialogUtils
import com.stardust.enhancedfloaty.FloatyService
import com.stardust.view.accessibility.NodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.openautojs.autojs.R
import org.autojs.autojs.ui.codegeneration.CodeGenerateDialog
import org.autojs.autojs.ui.compose.theme.AutoXJsTheme
import org.autojs.autojs.ui.floating.FloatyWindowManger
import org.autojs.autojs.ui.floating.FullScreenFloatyWindow
import org.autojs.autojs.ui.floating.MyLifecycleOwner
import org.autojs.autojs.ui.widget.BubblePopupMenu
import java.util.*

/**
 * Created by Stardust on 2017/3/12.
 */
open class LayoutHierarchyFloatyWindow(private val mRootNode: NodeInfo) : FullScreenFloatyWindow() {
    private var mLayoutHierarchyView: LayoutHierarchyView? = null
    private var mNodeInfoDialog: MaterialDialog? = null
    private var mBubblePopMenu: BubblePopupMenu? = null
    private var mNodeInfoView: NodeInfoView? = null
    private var mContext: Context? = null
    private var mSelectedNode: NodeInfo? = null

    override fun onCreateView(floatyService: FloatyService): View {
        mContext = ContextThemeWrapper(floatyService, R.style.AppTheme)
        mLayoutHierarchyView = LayoutHierarchyView(mContext)

        val view = ComposeView(mContext!!).apply {
            isFocusableInTouchMode = true
            setOnKeyListener { view, i, event ->
                if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    close()
                    return@setOnKeyListener true
                }
                return@setOnKeyListener false
            }
        }

        view.setContent {
            AutoXJsTheme {
                Content()
            }
        }
        val lifecycleOwner = MyLifecycleOwner()
        lifecycleOwner.performRestore(null)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        ViewTreeLifecycleOwner.set(view, lifecycleOwner)
        view.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        val viewModelStore = ViewModelStore()
        ViewTreeViewModelStoreOwner.set(view) { viewModelStore }
        val coroutineContext = AndroidUiDispatcher.CurrentThread
        val runRecomposeScope = CoroutineScope(coroutineContext)
        val recomposer = Recomposer(coroutineContext)
        view.compositionContext = recomposer
        runRecomposeScope.launch {
            recomposer.runRecomposeAndApplyChanges()
        }
        return view
    }

    @Composable
    private fun Content() {
        val context = LocalContext.current

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (mLayoutHierarchyView!!.mShowClickedNodeBounds) {
                        mLayoutHierarchyView!!.mClickedNodeInfo?.let {
                            val statusBarHeight = mLayoutHierarchyView!!.mStatusBarHeight
                            val rect = Rect(it.boundsInScreen)
                            rect.offset(0, -statusBarHeight)
                            drawRect(
                                color = Color(
                                    mLayoutHierarchyView!!.boundsPaint?.color ?: 0x2cd0d1
                                ),
                                topLeft = Offset(rect.left.toFloat(), rect.top.toFloat()),
                                size = Size(rect.width().toFloat(), rect.height().toFloat()),
                                style = Stroke(
                                    width = mLayoutHierarchyView!!.boundsPaint?.strokeWidth
                                        ?: 3f
                                )
                            )
                        }
                    }
                }
            }
            Column(modifier = Modifier.fillMaxSize()) {
                var isShowLayoutHierarchyView by remember {
                    mutableStateOf(true)
                }
                AndroidView(
                    factory = {
                        mLayoutHierarchyView!!
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    update = {
                        it.alpha = if (isShowLayoutHierarchyView) 1f else 0f
                    }
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { close() },
                    ) {
                        Text(text = stringResource(R.string.text_exit_floating_window))
                    }
                    Button(
                        onClick = { isShowLayoutHierarchyView = !isShowLayoutHierarchyView },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(text = stringResource(if (isShowLayoutHierarchyView)R.string.text_hide else R.string.text_show))
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(v: View) {
        mLayoutHierarchyView!!.setBackgroundColor(COLOR_SHADOW)
        mLayoutHierarchyView!!.setShowClickedNodeBounds(true)
        mLayoutHierarchyView!!.boundsPaint?.strokeWidth = 3f
        mLayoutHierarchyView!!.boundsPaint?.color = -0x2cd0d1
        mLayoutHierarchyView!!.setOnItemLongClickListener { view: View, nodeInfo: NodeInfo ->
            mSelectedNode = nodeInfo
            ensureOperationPopMenu()
            if (mBubblePopMenu!!.contentView.measuredWidth <= 0) mBubblePopMenu!!.preMeasure()
            mBubblePopMenu!!.showAsDropDown(
                view,
                view.width / 2 - mBubblePopMenu!!.contentView.measuredWidth / 2,
                0
            )
        }
        mLayoutHierarchyView!!.setRootNode(mRootNode)
        mSelectedNode?.let { mLayoutHierarchyView!!.setSelectedNode(it) }
    }

    private fun ensureOperationPopMenu() {
        if (mBubblePopMenu != null) return
        mBubblePopMenu = BubblePopupMenu(
            mContext, Arrays.asList(
                mContext!!.getString(R.string.text_show_widget_infomation),
                mContext!!.getString(R.string.text_show_layout_bounds),
                mContext!!.getString(R.string.text_generate_code)
            )
        )
        mBubblePopMenu!!.setOnItemClickListener { view: View?, position: Int ->
            mBubblePopMenu!!.dismiss()
            when (position) {
                0 -> {
                    showNodeInfo()
                }
                1 -> {
                    showLayoutBounds()
                }
                else -> {
                    generateCode()
                }
            }
        }
        mBubblePopMenu!!.width = ViewGroup.LayoutParams.WRAP_CONTENT
        mBubblePopMenu!!.height = ViewGroup.LayoutParams.WRAP_CONTENT
    }

    private fun generateCode() {
        DialogUtils.showDialog(
            CodeGenerateDialog(mContext!!, mRootNode, mSelectedNode)
                .build()
        )
    }

    private fun showLayoutBounds() {
        close()
        val window = LayoutBoundsFloatyWindow(mRootNode)
        window.setSelectedNode(mSelectedNode)
        FloatyService.addWindow(window)
    }

    fun showNodeInfo() {
        ensureNodeInfoDialog()
        mNodeInfoView!!.setNodeInfo(mSelectedNode!!)
        mNodeInfoDialog!!.show()
    }

    private fun ensureNodeInfoDialog() {
        if (mNodeInfoDialog == null) {
            mNodeInfoView = NodeInfoView(mContext!!)
            mNodeInfoDialog = MaterialDialog.Builder(mContext!!)
                .customView(mNodeInfoView!!, false)
                .theme(Theme.LIGHT)
                .build()
            mNodeInfoDialog!!.window?.setType(FloatyWindowManger.getWindowType())
        }
    }

    fun setSelectedNode(selectedNode: NodeInfo?) {
        mSelectedNode = selectedNode
    }

    companion object {
        private const val TAG = "FloatingHierarchyView"
        private const val COLOR_SHADOW = -0x22000001
    }
}