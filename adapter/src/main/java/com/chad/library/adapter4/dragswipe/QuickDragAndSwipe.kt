package com.chad.library.adapter4.dragswipe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.dragswipe.listener.DragAndSwipeDataCallback
import com.chad.library.adapter4.dragswipe.listener.OnItemDragListener
import com.chad.library.adapter4.dragswipe.listener.OnItemSwipeListener
import com.chad.library.adapter4.viewholder.StateLayoutVH
import java.util.Arrays

/**
 * @author yangfeng
 * @date 2022/7/27
 * 默认实现的适配带有头布局的拖拽类，可继承此类自定义
 */
open class QuickDragAndSwipe : ItemTouchHelper.Callback() {

    protected var recyclerView: RecyclerView? = null

    private val _itemTouchHelper: ItemTouchHelper = ItemTouchHelper(this)

    private var _isLongPressDragEnabled: Boolean = true
    private var _isItemViewSwipeEnabled: Boolean = true
    private var _isItemViewLeftSwipeEnabled: Boolean = true
    private var _isItemViewRightSwipeEnabled: Boolean = true

    /**
     * 设置拖拽的flag
     */
    private var _dragMoveFlags: Int = ItemTouchHelper.ACTION_STATE_IDLE

    /**
     * 设置侧滑的flag
     */
    private var _swipeMoveFlags: Int = ItemTouchHelper.ACTION_STATE_IDLE

    private var mOnItemDragListener: OnItemDragListener? = null
    private var mOnItemSwipeListener: OnItemSwipeListener? = null
    private var _dataCallback: DragAndSwipeDataCallback? = null
    private var isDrag = false
    private var isSwipe = false

    private var mSwipedView: SwipedView? = null


    val dataCallback: DragAndSwipeDataCallback
        get() {
            checkNotNull(_dataCallback) {
                "Please set _adapterImpl"
            }
            return _dataCallback!!
        }

    val itemTouchHelper: ItemTouchHelper get() = _itemTouchHelper

    /**
     * 绑定RecyclerView
     */
    open fun attachToRecyclerView(recyclerView: RecyclerView) = apply {
        if (this.recyclerView == recyclerView) return this
        this.recyclerView = recyclerView
        _itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    /**
     * 设置拖拽的flag
     */
    fun setDragMoveFlags(dragMoveFlags: Int) = apply {
        this._dragMoveFlags = dragMoveFlags
    }

    fun getDragMoveFlags(): Int = this._dragMoveFlags

    /**
     * 设置侧滑的flag
     */
    fun setSwipeMoveFlags(swipeMoveFlags: Int) = apply {
        this._swipeMoveFlags = swipeMoveFlags
    }

    fun getSwipeMoveFlags(): Int = this._swipeMoveFlags

    /**
     * 是否开启拖拽
     */
    fun setLongPressDragEnabled(isLongPressDragEnabled: Boolean) = apply {
        _isLongPressDragEnabled = isLongPressDragEnabled
    }

    /**
     * 是否开启侧滑
     *
     * @param isItemViewSwipeEnabled
     */
    fun setItemViewSwipeEnabled(isItemViewSwipeEnabled: Boolean) = apply {
        _isItemViewSwipeEnabled = isItemViewSwipeEnabled
    }

    fun setItemViewLeftSwipeEnabled(isItemViewSwipeEnabled: Boolean) = apply {
        _isItemViewLeftSwipeEnabled = isItemViewSwipeEnabled
    }

    fun setItemViewRightSwipeEnabled(isItemViewRightSwipeEnabled: Boolean) = apply {
        _isItemViewRightSwipeEnabled = isItemViewRightSwipeEnabled
    }

    /**
     * 拖拽
     * 长按默认可拖动，可不进行设置此方法
     * 此方法可以做特殊使用进行调用
     * 如：长按此条position对应的item，触发 position+1 对应的item
     */
    open fun startDrag(holder: RecyclerView.ViewHolder) = apply {
        _itemTouchHelper.startDrag(holder)
    }

    /**
     * 拖拽
     * 长按默认可拖动，可不进行设置此方法
     * 此方法可以做特殊使用进行调用
     * 如：长按此条position对应的item，触发 position+1 对应的item
     */
    open fun startDrag(position: Int) = apply {
        val holder = recyclerView?.findViewHolderForAdapterPosition(position) ?: return this
        _itemTouchHelper.startDrag(holder)
    }

    /**
     * 启动侧滑
     */
    open fun startSwipe(holder: RecyclerView.ViewHolder) = apply {
        _itemTouchHelper.startSwipe(holder)
    }

    /**
     * 启动侧滑
     */
    open fun startSwipe(position: Int) = apply {
        val holder = recyclerView?.findViewHolderForAdapterPosition(position) ?: return this
        _itemTouchHelper.startSwipe(holder)
    }

    /********************************************************/
    /*              ItemTouchHelper.Callback()              */
    /********************************************************/

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_DRAG -> {
                isDrag = true
                mOnItemDragListener?.onItemDragStart(
                    viewHolder,
                    getViewHolderPosition(viewHolder)
                )
            }

            ItemTouchHelper.ACTION_STATE_SWIPE -> {
                isSwipe = true
                mOnItemSwipeListener?.onItemSwipeStart(
                    viewHolder,
                    getViewHolderPosition(viewHolder)
                )
            }
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    /**
     * 是否可拖动或左右滑动
     * 可根据viewHolder获取对应的条目对某条，不进行拖动或滑动操作。返回值设置为 makeMovementFlags(0, 0) 即可
     */
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        //此处判断，是否可以长按拖动
        if (isEmptyView(viewHolder)) {
            return makeMovementFlags(0, 0)
        }

        val dragFlags = _dragMoveFlags
        var swipeFlags = 0

        if (_isItemViewSwipeEnabled) {
            if (_isItemViewLeftSwipeEnabled) {
                swipeFlags = ItemTouchHelper.LEFT
            }

            if (_isItemViewRightSwipeEnabled) {
                swipeFlags = ItemTouchHelper.RIGHT
            }

            if (_isItemViewRightSwipeEnabled && _isItemViewLeftSwipeEnabled) {
                swipeFlags = _swipeMoveFlags
            }
        }

        return makeMovementFlags(dragFlags, swipeFlags)
//        return makeMovementFlags(_dragMoveFlags, _swipeMoveFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return viewHolder.itemViewType == target.itemViewType
    }

    override fun onMoved(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        fromPos: Int,
        target: RecyclerView.ViewHolder,
        toPos: Int,
        x: Int,
        y: Int
    ) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
        val fromPosition = viewHolder.bindingAdapterPosition
        val toPosition = target.bindingAdapterPosition

        if (fromPosition == RecyclerView.NO_POSITION || toPosition == RecyclerView.NO_POSITION) return

        // 进行位置的切换
        _dataCallback?.dataMove(fromPosition, toPosition)
        mOnItemDragListener?.onItemDragMoving(viewHolder, fromPosition, target, toPosition)
    }


    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition

        if (position == RecyclerView.NO_POSITION) return

        if (direction == ItemTouchHelper.LEFT) {
            if (_isItemViewLeftSwipeEnabled) {
                // Handle left swipe
                _dataCallback?.dataRemoveAt(position)
                mOnItemSwipeListener?.onItemSwiped(viewHolder, direction, position)
            }
        } else if (direction == ItemTouchHelper.RIGHT) {
            if (_isItemViewRightSwipeEnabled) {
                // Handle right swipe
                _dataCallback?.dataRemoveAt(position)
                mOnItemSwipeListener?.onItemSwiped(viewHolder, direction, position)
            }
        }
    }

    override fun isLongPressDragEnabled(): Boolean {
        return _isLongPressDragEnabled
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return _isItemViewSwipeEnabled
    }


    fun setSwipeView(swipedView: SwipedView) {
        this.mSwipedView = swipedView
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_SWIPE -> {
                mOnItemSwipeListener?.onItemSwipeMoving(c, viewHolder, dX, dY, isCurrentlyActive)
            }
        }

        val v = viewHolder.itemView
        val context = v.context

        var toDraw: ChildToDraw? = null
        if (dX > 0) {
            toDraw = ChildToDraw(
                dX.toInt(),
                v,
                context,
                0
            ).invoke()
        } else if (dX < 0) {
            toDraw = ChildToDraw(
                dX.toInt(),
                v,
                context,
                1
            ).invoke()
        }
        if (toDraw != null) draw(c, context, toDraw)
    }

    private fun draw(c: Canvas, context: Context, toDraw: ChildToDraw) {
        val bg: ColorDrawable? = toDraw.getBg()
        val icon: Drawable? = toDraw.getIcon()
        val paint: Paint? = toDraw.getPaintText()
        bg?.draw(c)
        if (icon != null) {
            icon.draw(c)
            toDraw?.getText()?.let {
                c.drawText(
                    it, icon.bounds.centerX().toFloat(),
                    icon.bounds.centerY() + icon.bounds.height() +
                            convertDpToPixel(2f, context), paint!!
                )
            }
        } else {
            toDraw.getText()?.let {
                c.drawText(
                    it, bg?.bounds?.centerX()?.toFloat()!!,
                    bg.bounds.centerY() + mSwipedView?.textSize!!.toFloat() / 2, paint!!
                )
            }
        }
    }


    fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() /
                DisplayMetrics.DENSITY_DEFAULT)
    }


    inner class ChildToDraw constructor(
        private val dX: Int,
        private val v: View,
        private val context: Context,
        private val mSide: Int
    ) {
        private var bg: ColorDrawable? = null
        private var icon: Drawable? = null
        private var paintText: Paint? = null
        private var text: String? = null


        fun getBg(): ColorDrawable? {
            return bg
        }

        fun getIcon(): Drawable? {
            return icon
        }

        fun getPaintText(): Paint? {
            return paintText
        }

        fun getText(): String? {
            return text
        }

        operator fun invoke(): ChildToDraw {
            var iconMargin = 0
            var iconTop = 0
            var iconBottom = 0
            try {
                icon = ContextCompat.getDrawable(
                    context,
                    if (mSide == 0) mSwipedView?.leftIcon!! else mSwipedView?.rightIcon!!
                )
                val tintColor = ContextCompat.getColor(context, android.R.color.white)
                icon?.let {
                    val iconCopy = it.mutate()
                    DrawableCompat.wrap(iconCopy)
                    DrawableCompat.setTint(iconCopy, tintColor)
                }
                iconMargin = (v.height - icon!!.intrinsicHeight) / 2
                iconTop = v.top + (v.height - icon!!.intrinsicHeight) / 2
                iconBottom = iconTop + icon!!.intrinsicHeight
            } catch (e: Exception) {
                icon = null
                e.printStackTrace()
            }
            val iconLeft: Int
            val iconRight: Int
            when (mSide) {
                0 -> {
                    text = mSwipedView?.leftText
                    if (icon != null) {
                        iconLeft = v.left + iconMargin
                        iconRight = v.left + iconMargin + icon!!.intrinsicWidth
                        icon!!.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    }
                    bg = ColorDrawable(
                        context.resources
                            .getColor(mSwipedView?.leftBg!!)
                    )
                    bg!!.setBounds(
                        v.left, v.top, v.left +
                                dX, v.bottom
                    )
                }

                1 -> {
                    text = mSwipedView?.rightText
                    if (icon != null) {
                        iconLeft = v.right - iconMargin - icon!!.intrinsicWidth
                        iconRight = v.right - iconMargin
                        icon!!.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    }
                    bg = ColorDrawable(
                        context.resources
                            .getColor(mSwipedView?.rightBg!!)
                    )
                    bg!!.setBounds(
                        v.right + dX, v.top,
                        v.right, v.bottom
                    )
                }
            }
            paintText = Paint()
            paintText!!.color = mSwipedView?.textColor!!
            paintText?.textSize = if (mSwipedView?.textSize == 15) convertDpToPixel(
                mSwipedView?.textSize!!.toFloat(),
                context
            ) else mSwipedView?.textSize!!.toFloat()
            paintText!!.textAlign = Paint.Align.CENTER
            return this
        }

    }


    class SwipedView {
        var mIcons: IntArray? = null
        var mBackgrounds: IntArray? = null
        var mTexts: Array<String?>? = null
        var textColor = 0
        var textSize = 0

        constructor()

        /**
         * Represents the child to draw in [SwipeLeftRightCallback]
         *
         * @param icons       - must contain 2 icons - [0] - left, [1] - right
         * @param texts       - must contain 2 strings - [0] - left, [1] - right
         * @param backgrounds - must contain 2 backgrounds int - [0] - left, [1] - right
         * - assign null/-1 for unwanted side
         */
        internal constructor(icons: IntArray?, backgrounds: IntArray?, texts: Array<String?>?) {
            mIcons = icons
            mBackgrounds = backgrounds
            mTexts = texts
        }

        val leftIcon: Int
            get() = mIcons!![0]
        val rightIcon: Int
            get() = mIcons!![1]
        val leftBg: Int
            get() = mBackgrounds!![0]
        val rightBg: Int
            get() = mBackgrounds!![1]
        val leftText: String
            get() = if (mTexts!![0] == null) "" else mTexts!![0]!!
        val rightText: String
            get() = if (mTexts!![1] == null) "" else mTexts!![1]!!

        fun setIcons(icons: IntArray?) {
            mIcons = icons
        }

        fun setBackrounds(backrounds: IntArray?) {
            mBackgrounds = backrounds
        }

        fun setTexts(texts: Array<String?>?) {
            mTexts = texts
        }

        fun setTextColorView(color: Int) {
            textColor = color
        }

        fun setTextSizeView(size: Int) {
            textSize = size
        }

        val isFull: Boolean
            get() = mBackgrounds != null && mBackgrounds!!.size == 2 && mIcons != null && mIcons!!.size == 2 && mTexts != null && mTexts!!.size == 2

        override fun toString(): String {
            return "SwipedView{" +
                    "mIcons=" + Arrays.toString(mIcons) +
                    ", mBackrounds=" + Arrays.toString(mBackgrounds) +
                    ", mTexts=" + Arrays.toString(mTexts) +
                    '}'
        }
    }


    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        val position = viewHolder.bindingAdapterPosition
        if (isSwipe) {
            mOnItemSwipeListener?.onItemSwipeEnd(viewHolder, position)
            isSwipe = false
        }
        if (isDrag) {
            mOnItemDragListener?.onItemDragEnd(viewHolder, position)
            isDrag = false
        }
    }

    /********************************************************/
    /*                 private method                       */
    /********************************************************/

    /**
     * 是否是空布局
     */
    private fun isEmptyView(viewHolder: RecyclerView.ViewHolder): Boolean {
        return viewHolder is StateLayoutVH
    }


    private fun getViewHolderPosition(viewHolder: RecyclerView.ViewHolder?): Int {
        return viewHolder?.bindingAdapterPosition ?: RecyclerView.NO_POSITION
    }

    /********************************************************/
    /*                       Listener                       */
    /********************************************************/

    /**
     * 设置拖拽的监听
     */
    fun setItemDragListener(onItemDragListener: OnItemDragListener?) = apply {
        this.mOnItemDragListener = onItemDragListener
    }

    fun setItemSwipeListener(onItemSwipeListener: OnItemSwipeListener?) = apply {
        this.mOnItemSwipeListener = onItemSwipeListener
    }

    fun setDataCallback(callback: DragAndSwipeDataCallback) = apply {
        this._dataCallback = callback
    }

}