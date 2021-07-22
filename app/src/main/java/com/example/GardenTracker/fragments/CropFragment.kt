package com.example.GardenTracker.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.GardenTracker.model.Crop
import com.example.GardenTracker.adapters.MyMemoryAdapter
import com.example.GardenTracker.R

private const val STATUS_CROP = "status_crop"
private const val ARG_DRAWABLES = "drawable-resources"
private const val CROP_MEMORIES = "crop-memories"
private const val CAMERA_REQUEST = 0

class CropFragment : Fragment() {

    private val TAG = "CROP_FRAGMENT"

    private lateinit var mStatusCrop : Crop
    private lateinit var mDrawables : ArrayList<Drawable>

    private var listener: OnCropStatusListener? = null
    
    private lateinit var mCropMemories: ArrayList<Bitmap>

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: MyMemoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            Log.d(TAG, "Unpacking savedInstanceState arguments.")
            mStatusCrop = (it.getSerializable(STATUS_CROP) as ArrayList<Crop>).get(0)
            mDrawables = it.getSerializable(ARG_DRAWABLES) as ArrayList<Drawable>
            mCropMemories = it.getSerializable(CROP_MEMORIES) as ArrayList<Bitmap>
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_crop_status, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get crop status views
        val titleBox : TextView = view.findViewById(R.id.crop_title)
        val cropType : ImageView = view.findViewById(R.id.crop_type)
        val cropLabel : TextView = view.findViewById(R.id.crop_label)
        var harvestProgress : ProgressBar = view.findViewById(R.id.time_to_harvest)


        // Get view buttons
        val waterButton: Button = view.findViewById(R.id.water_crop_button)
        val removeButton: Button = view.findViewById(R.id.remove_crop_button)
        val notesButton: Button = view.findViewById(R.id.crop_to_notes_button)
        val captureButton: Button = view.findViewById(R.id.goto_camera_button)

        // Set up with crop data
        titleBox.append(mStatusCrop.name)
        Log.d(TAG, "Updating crop logo.")
        when(mStatusCrop.type) {
            "Flower" -> cropType.setImageDrawable(mDrawables[0])
            "Herb" -> cropType.setImageDrawable(mDrawables[1])
            "Fruit" -> cropType.setImageDrawable(mDrawables[2])
            "Vegetable" -> cropType.setImageDrawable(mDrawables[3])
            else -> cropType.setImageDrawable(mDrawables[0])
        }
        cropLabel.text = mStatusCrop.name // Might not work
        harvestProgress.progress = mStatusCrop.harvestProgress()

        // Set button listeners
        waterButton.setOnClickListener(View.OnClickListener {
            listener?.waterCrop(mStatusCrop)
        })

        removeButton.setOnClickListener(View.OnClickListener {
            listener?.removeCrop(mStatusCrop)
        })

        notesButton.setOnClickListener(View.OnClickListener {
            listener?.goToNotes(mStatusCrop)
        })

        captureButton.setOnClickListener(View.OnClickListener {
            listener?.startCameraActivity()
        })

        // Setup Memory RecyclerView
        mRecyclerView = view.findViewById(R.id.crop_memories)
        with(mRecyclerView) {
            layoutManager = GridLayoutManager(context, 3)
            adapter = listener?.let {
                MyMemoryAdapter(
                    mCropMemories,
                    it
                )
            }
            mAdapter = adapter as MyMemoryAdapter

        }
        mAdapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST) {
            if (data != null) {
                Log.d(TAG, "Data exists to unpack")
                if (data.extras != null) {
                    val extras = data.extras
                    if (extras?.get("data") != null) {
                        Log.d(TAG, "Successfully retreived data.")
                        val image : Bitmap = extras.get("data") as Bitmap
                        //mCropMemories.add(image)
                        listener?.saveNewMemory(mStatusCrop, image)
                        mAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity)?.supportActionBar?.title = mStatusCrop.name + " Status"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCropStatusListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnCropStatusListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnCropStatusListener {
        fun waterCrop(crop: Crop)
        fun removeCrop(crop: Crop)
        fun goToNotes(crop: Crop)
        fun onMemorySelect(bm: Bitmap)
        fun startCameraActivity()
        fun saveNewMemory(crop: Crop, memory: Bitmap)
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: ArrayList<Crop>, param2: ArrayList<Drawable>) =
            CropListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(STATUS_CROP, param1)
                    putSerializable(ARG_DRAWABLES, param2)
                }
            }
    }
}
