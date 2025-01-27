package com.example.GardenTracker.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.GardenTracker.R
import com.example.GardenTracker.fragments.MemoriesFragment
import kotlinx.android.synthetic.main.memory_image_view.view.*

class MyMemoryAdapter(private val mValues: ArrayList<Bitmap>, private val mListener: MemoriesFragment.OnMemoryListInteractionListener)
    : RecyclerView.Adapter<MyMemoryAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener = View.OnClickListener { v ->
        val item = v.tag as Bitmap
        mListener.onMemorySelect(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.memory_image_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int { return mValues.size }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mImage = mValues.get(position)

        holder.mImage.setImageBitmap(mImage)

        with(holder.mView) {
            tag = mImage
            setOnClickListener(mOnClickListener)
        }
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mImage: ImageView = mView.memory_view
    }
}