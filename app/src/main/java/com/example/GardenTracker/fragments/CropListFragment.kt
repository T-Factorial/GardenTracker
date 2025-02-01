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
import com.example.GardenTracker.CropDialog
import com.example.GardenTracker.model.Crop
import com.example.GardenTracker.adapters.MyCropAdapter
import com.example.GardenTracker.R
import com.example.GardenTracker.model.CropListViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * This fragment displays all the crops the user currently has saved.
 * These crops are meant to be currently growing in the user's garden.
 */
class CropListFragment : Fragment() {

    private val TAG = "CROP_LIST_FRAGMENT"

    private var columnCount = 1
    private lateinit var mDrawables: List<Drawable?>

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: MyCropAdapter
    private lateinit var addCropBtn: FloatingActionButton

    private var listener: OnCropFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            Log.d(TAG, "Unpacking savedInstanceState arguments.")
            columnCount = it.getInt(ARG_COLUMN_COUNT)
            val drawableResIds = it.getIntegerArrayList(ARG_DRAWABLES)

            if (drawableResIds != null) {
                mDrawables = ArrayList(drawableResIds.map { resId ->
                    requireContext().getDrawable(resId)!!
                })
            }
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
                mDrawables,
                listener
            )
            mAdapter = adapter as MyCropAdapter
        }
        mAdapter.notifyDataSetChanged()

        // Observe crop list
        CropListViewModel.cropList.observe(viewLifecycleOwner) { updatedList ->
            Log.d(TAG, "Crop list updated: ${updatedList.size} items")
            mAdapter.submitList(updatedList)
            mAdapter.notifyDataSetChanged()
        }

        // Add click listener to FAB
        addCropBtn.setOnClickListener() {
            // Pop-up a dialog
            fragmentManager?.let { it ->
                CropDialog().show(it,"Add crop")
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
    }

    companion object {

        const val ARG_COLUMN_COUNT = "column-count"
        const val ARG_CROP_LIST = "crop-list"
        const val ARG_DRAWABLES = "drawable-resources"

        @JvmStatic
        fun newInstance(columnCount: Int, cropList: ArrayList<Crop>, drawables: ArrayList<*>) =
            CropListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                    putSerializable(ARG_CROP_LIST, cropList)
                    putSerializable(ARG_DRAWABLES, drawables)
                }
            }
    }
}
