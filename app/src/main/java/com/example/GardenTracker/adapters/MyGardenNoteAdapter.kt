package com.example.GardenTracker.adapters

import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.GardenTracker.R


import com.example.GardenTracker.fragments.NotesFragment.OnNoteListInteractionListener
import com.example.GardenTracker.model.Note

import kotlinx.android.synthetic.main.fragment_note_list_item.view.*

class MyGardenNoteAdapter(
    private val drawable: Drawable?,
    private val drawables: ArrayList<Drawable>?,
    private val mValues: List<Note>,
    private val mListener: OnNoteListInteractionListener?
) : RecyclerView.Adapter<MyGardenNoteAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener
    private var mDrawable: Drawable? = null
    private var mDrawables: ArrayList<Drawable>? = null

    init {
        if (drawable != null) {
            mDrawable = drawable
        }
        if (drawables != null) {
            mDrawables = drawables
        }
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Note
            mListener?.onNoteSelect(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_note_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
         holder.mNoteCreationDate.text = item.cropName + " " + item.creationDay.toString() +
                 "/" + item.creationMonth + "/" + item.creationYear

         holder.mNoteCropType.setImageDrawable(mDrawable)

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mNoteCreationDate: TextView = mView.note_creation_date
        val mNoteCropType: ImageView = mView.crop_type

        override fun toString(): String {
            return super.toString() + " ' '" + mNoteCreationDate.text +
                    "' '" + mNoteCropType.toString() + "'"
        }

    }
}
