package com.example.GardenTracker.adapters

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.GardenTracker.model.Note

// https://social.msdn.microsoft.com/Forums/en-US/e9288570-cbb8-48c0-9174-f016c87517ec/cannot-create-an-instance-of-the-abstract-class-or-interface-itemtouchhelpersimplecallback?forum=xamarinandroid

class NoteTouchHelper : ItemTouchHelper.Callback() {
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags : Int = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags : Int = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, targer: RecyclerView.ViewHolder) : Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // TODO: Implement swipe-to-delete feature for notes
    }
}