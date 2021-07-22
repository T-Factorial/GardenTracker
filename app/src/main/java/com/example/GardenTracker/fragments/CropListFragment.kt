package com.example.GardenTracker.fragments

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.GardenTracker.AddCropDialog
import com.example.GardenTracker.model.Crop
import com.example.GardenTracker.adapters.MyCropAdapter
import com.example.GardenTracker.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

// I pretty much just load up the RecyclerView with the data I'm given!
class CropListFragment : Fragment() {

    private val TAG = "CROP_LIST_FRAGMENT"

    private var columnCount = 1
    private lateinit var mCropList: ArrayList<Crop>
    private lateinit var mDrawableResources: ArrayList<*>

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: MyCropAdapter
    private lateinit var addCropBtn: FloatingActionButton

    private var listener: OnCropFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            Log.d(TAG, "Unpacking savedInstanceState arguments.")
            columnCount = it.getInt(ARG_COLUMN_COUNT)
            mCropList = it.getSerializable(ARG_CROP_LIST) as ArrayList<Crop>
            mDrawableResources = it.getSerializable(ARG_DRAWABLES) as ArrayList<*>
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_crop_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRecyclerView = view.findViewById(R.id.crop_list)
        addCropBtn = view.findViewById(R.id.add_crop_btn)

        // Set the adapter
        with(mRecyclerView) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = MyCropAdapter(
                mDrawableResources,
                mCropList,
                listener
            )
            mAdapter = adapter as MyCropAdapter

        }
        mAdapter.notifyDataSetChanged()

        // Add click listener to FAB
        addCropBtn.setOnClickListener() {
            // Pop-up a dialog
            fragmentManager?.let { it ->
                AddCropDialog().show(it,"Add crop")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity)?.supportActionBar?.title = "Your Garden"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCropFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnCropFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnCropFragmentInteractionListener {
        fun onCropListInteraction(item: Crop)
        fun onAddCropBtnPressed()
    }

    companion object {

        const val ARG_COLUMN_COUNT = "column-count"
        const val ARG_CROP_LIST = "crop-list"
        const val ARG_DRAWABLES = "drawable-resources"

        @JvmStatic
        fun newInstance(columnCount: Int, cropList: ArrayList<Crop>, drawables: ArrayList<Drawable?>) =
            CropListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                    putSerializable(ARG_CROP_LIST, cropList)
                    putSerializable(ARG_DRAWABLES, drawables)
                }
            }
    }
}
