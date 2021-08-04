package com.example.GardenTracker.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
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
import com.example.GardenTracker.CropDialog
import com.example.GardenTracker.model.Crop
import com.example.GardenTracker.adapters.MyMemoryAdapter
import com.example.GardenTracker.R

private const val STATUS_CROP = "status_crop"
private const val EDIT_CROP = "edit-crop"
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
            mStatusCrop = it.getSerializable(STATUS_CROP) as Crop
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
        val titleBox: TextView = view.findViewById(R.id.crop_title)
        val cropType: ImageView = view.findViewById(R.id.crop_type)
        val cropLabel: TextView = view.findViewById(R.id.crop_label)
        val harvestProgress: ProgressBar = view.findViewById(R.id.time_to_harvest)
        val waterStatus: TextView = view.findViewById(R.id.water_status_crop)
        val waterTimes: TextView = view.findViewById(R.id.watering_times)

        if (mStatusCrop.needsWater) {
            waterStatus.text = "Thirsty"
            waterStatus.setTextColor(Color.RED)
        } else {
            waterStatus.text = "Quenched"
            waterStatus.setTextColor(Color.BLUE)
        }

        val waterHours = mStatusCrop.waterHoursFromString()
        waterHours.forEach {
            if (waterHours.indexOf(it) != waterHours.lastIndex)
            waterTimes.text = "${waterTimes.text} ${hourToString(it)},"
            else waterTimes.text = "${waterTimes.text} ${hourToString(it)}"
        }

        // Get view buttons
        val waterButton: Button = view.findViewById(R.id.water_crop_button)
        val editButton: Button = view.findViewById(R.id.edit_crop_button)
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
        waterButton.setOnClickListener {
            listener?.waterCrop(mStatusCrop)
        }

        editButton.setOnClickListener {
           // listener?.editCrop(mStatusCrop)
            fragmentManager?.let { it ->
                val dialog = CropDialog()
                val bundle = Bundle()
                bundle.putSerializable(EDIT_CROP, mStatusCrop)
                dialog.arguments = bundle
                dialog.show(it,"Edit crop")
            }
        }

        removeButton.setOnClickListener {
            listener?.removeCrop(mStatusCrop)
        }

        notesButton.setOnClickListener {
            listener?.goToNotes(mStatusCrop)
        }

        captureButton.setOnClickListener {
            listener?.startCameraActivity()
        }

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
                        Log.d(TAG, "Successfully retrieved data.")
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
        (activity as AppCompatActivity).supportActionBar?.title = mStatusCrop.name + " Status"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCropStatusListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnCropStatusListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun hourToString(hour: Int): String {
        when (hour) {
            0 -> return "12:00AM"
            1 -> return "1:00AM"
            2 -> return "2:00AM"
            3 -> return "3:00AM"
            4 -> return "4:00AM"
            5 -> return "5:00AM"
            6 -> return "6:00AM"
            7 -> return "7:00AM"
            8 -> return "8:00AM"
            9 -> return "9:00AM"
            10 -> return "10:00AM"
            11 -> return "11:00AM"
            12 -> return "12:00PM"
            13 -> return "1:00PM"
            14 -> return "2:00PM"
            15 -> return "3:00PM"
            16 -> return "4:00PM"
            17 -> return "5:00PM"
            18 -> return "6:00PM"
            19 -> return "7:00PM"
            20 -> return "8:00PM"
            21 -> return "9:00PM"
            22 -> return "10:00PM"
            23 -> return "11:00PM"
            else -> return ""
        }
    }

    interface OnCropStatusListener {
        fun waterCrop(crop: Crop)
        fun removeCrop(crop: Crop)
        fun editCrop(crop: Crop)
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
