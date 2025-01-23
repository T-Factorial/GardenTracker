package com.example.GardenTracker.adapters

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.GardenTracker.R
import com.example.GardenTracker.fragments.CropListFragment.OnCropFragmentInteractionListener
import com.example.GardenTracker.model.Crop
import kotlinx.android.synthetic.main.fragment_crop.view.*

class MyCropAdapter(
    private val drawables: List<Drawable?>,
    private val mListener: OnCropFragmentInteractionListener?
) : ListAdapter<Crop, MyCropAdapter.ViewHolder>(CropDiffCallback()) {

    private val TAG = "MY_CROP_ADAPTER"

    private val mOnClickListener: View.OnClickListener = View.OnClickListener { v ->
        val item = v.tag as Crop
        mListener?.onCropListInteraction(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_crop, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "Binding crop: ${getItem(position).name}")
        val item = getItem(position)

        holder.cropType.setImageDrawable(
            when (item.type) {
                "Flower" -> drawables[0]
                "Herb" -> drawables[1]
                "Fruit" -> drawables[2]
                "Vegetable" -> drawables[3]
                else -> drawables[0]
            }
        )
        holder.cropName.text = item.name
        holder.timeToHarvest.progress = item.harvestProgress()

        if (item.needsWater) {
            holder.waterStatus.text = "Thirsty"
            holder.waterStatus.setTextColor(Color.RED)
        } else {
            holder.waterStatus.text = "Quenched"
            holder.waterStatus.setTextColor(Color.BLUE)
        }

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val cropType: ImageView = mView.crop_type
        val cropName: TextView = mView.crop_label
        val timeToHarvest: ProgressBar = mView.time_to_harvest
        val waterStatus: TextView = mView.water_status
    }
}

class CropDiffCallback : DiffUtil.ItemCallback<Crop>() {
    override fun areItemsTheSame(oldItem: Crop, newItem: Crop): Boolean {
        return oldItem.id == newItem.id // Use unique IDs to compare items
    }

    override fun areContentsTheSame(oldItem: Crop, newItem: Crop): Boolean {
        return oldItem == newItem // Use data class equals for deep comparison
    }
}
