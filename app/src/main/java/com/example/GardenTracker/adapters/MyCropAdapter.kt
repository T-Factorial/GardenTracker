package com.example.GardenTracker.adapters

import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.example.GardenTracker.R


import com.example.GardenTracker.fragments.CropListFragment.OnCropFragmentInteractionListener
import com.example.GardenTracker.model.Crop
import kotlinx.android.synthetic.main.fragment_crop.view.*

class MyCropAdapter(
    val drawables: ArrayList<*>,
    var mValues: ArrayList<Crop>,
    mListener: OnCropFragmentInteractionListener?
) : RecyclerView.Adapter<MyCropAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener
    private var mDrawables : ArrayList<Drawable?>

    init {
        mDrawables = drawables as ArrayList<Drawable?>
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Crop
            mListener?.onCropListInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_crop, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]

        holder.cropType.setImageDrawable(
            when(item.type) {
                "Flower" -> mDrawables[0]
                "Herb" -> mDrawables[1]
                "Fruit" -> mDrawables[2]
                "Vegetable" -> mDrawables[3]
                else -> mDrawables[0]
            }
        )
        holder.cropName.text = item.name
        holder.timeToHarvest.progress = item.harvestProgress()

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val cropType: ImageView = mView.crop_type
        val cropName: TextView = mView.crop_label
        val timeToHarvest: ProgressBar = mView.time_to_harvest

        /*
        override fun toString(): String {
            return super.toString() + " '" + timeToHarvest.text + "'"
        }
         */
    }
}
