package com.example.GardenTracker.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.GardenTracker.CropDialog
import com.example.GardenTracker.model.Crop
import com.example.GardenTracker.adapters.MyMemoryAdapter
import com.example.GardenTracker.R
import com.example.GardenTracker.adapters.MyCropAdapter
import com.example.GardenTracker.model.CropStatusViewModel
import kotlinx.coroutines.*
import java.io.IOException

private const val STATUS_CROP = "status_crop"
private const val EDIT_CROP = "edit-crop"
private const val ARG_DRAWABLES = "drawable-resources"
private const val CROP_MEMORIES = "crop-memories"
private const val CAMERA_REQUEST = 0

/**
 * This Fragment contains general information about a specific crop
 * You can see the crop's watering status and growth status, and times it needs to be watered
 * An edit button allows you to make changes to vital information about the crop.
 * The water button will notify the app that the user has watered the crop.
 * You can also go to the crop's note page and add notes or view old ones.
 */

class CropFragment : Fragment() {

    private val TAG = "CROP_FRAGMENT"

    private val fragmentScope = CoroutineScope(Dispatchers.Main)

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_crop_status, container, false)
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

        // Setup LiveData and Observers
        val waterStatusObserver = Observer<String> { nStatus ->
            waterStatus.text = nStatus
            if (mStatusCrop.needsWater) {
                waterStatus.setTextColor(Color.RED)
            } else {
                waterStatus.setTextColor(Color.BLUE)
            }
        }
        CropStatusViewModel.waterStatus.observe(this, waterStatusObserver)

        val waterHoursObserver = Observer<List<Int>> { nHours ->
            waterTimes.text = ""
            nHours.forEach {
                if (nHours.indexOf(it) != nHours.lastIndex)
                waterTimes.text = "${waterTimes.text} ${hourToString(it)},"
                else waterTimes.text = "${waterTimes.text} ${hourToString(it)}"
            }
        }
        CropStatusViewModel.waterHours.observe(this, waterHoursObserver)


        // This block of code should immediately update the water status and water hours on the
        // crop status page
        if (mStatusCrop.needsWater) {
            CropStatusViewModel.waterStatus.value = "Thirsty"
        } else {
            CropStatusViewModel.waterStatus.value = "Quenched"
        }
        CropStatusViewModel.waterHours.value = mStatusCrop.waterHoursFromString()

        // Get view buttons
        val waterButton: Button = view.findViewById(R.id.water_crop_button)
        val editButton: Button = view.findViewById(R.id.edit_crop_button)
        val removeButton: Button = view.findViewById(R.id.remove_crop_button)
        val notesButton: Button = view.findViewById(R.id.crop_to_notes_button)
        val captureButton: Button = view.findViewById(R.id.goto_camera_button)

        // Set up with crop data
        titleBox.append(mStatusCrop.name)
        Log.d(TAG, "Updating crop logo.")
        // Remove conditional when real drawables are available
        if (mDrawables.size > 1) {
            when (mStatusCrop.type) {
                "Flower" -> cropType.setImageDrawable(mDrawables[0])
                "Herb" -> cropType.setImageDrawable(mDrawables[1])
                "Fruit" -> cropType.setImageDrawable(mDrawables[2])
                "Vegetable" -> cropType.setImageDrawable(mDrawables[3])
                else -> cropType.setImageDrawable(mDrawables[0])
            }
        } else {
            cropType.setImageDrawable(mDrawables[0])
        }
        cropLabel.text = mStatusCrop.name // Might not work
        harvestProgress.progress = mStatusCrop.harvestProgress()

        // Set button listeners
        waterButton.setOnClickListener {
            listener?.waterCrop(mStatusCrop)
            CropStatusViewModel.waterStatus.value = if (mStatusCrop.needsWater) "Thirsty" else "Quenched" // Update LiveData
            Log.d(TAG, "Crop watered. Status updated to: ${CropStatusViewModel.waterStatus.value}")
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
            listener?.startCameraActivity(mStatusCrop)
        }

        // Setup Memory RecyclerView
        mRecyclerView = view.findViewById(R.id.crop_memories)
        mRecyclerView.layoutManager = GridLayoutManager(context, 3)

        // Asynchronously load bitmaps and update the adapter
        fragmentScope.launch {
            Log.d(TAG, "Loading crop ${mStatusCrop.name}'s memories...")

            val memories = loadMemories(mStatusCrop) // Wait for memories to load
            mCropMemories = memories // Initialize mCropMemories properly

            // Now attach the adapter *after* memories are loaded
            mAdapter = MyMemoryAdapter(mCropMemories, listener!!)
            mRecyclerView.adapter = mAdapter
            mAdapter.notifyDataSetChanged()

            Log.d(TAG, "Adapter set with ${mCropMemories.size} memories.")
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = mStatusCrop.name + " Status"
    }

    override fun onStop() {
        super.onStop()
        CropStatusViewModel.growthProgress.value = 0
        CropStatusViewModel.waterStatus.value = ""
        CropStatusViewModel.waterHours.value = ArrayList()
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

    override fun onDestroy() {
        super.onDestroy()
        fragmentScope.cancel() // Avoid memory leaks
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

    private suspend fun loadMemories(crop: Crop): ArrayList<Bitmap> = withContext(Dispatchers.IO) {
        val files = crop.memoriesFromString()
        val bitmaps = ArrayList<Bitmap>()

        val resolver = requireContext().contentResolver
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        files.forEach { fileName ->
            try {
                val bmpUri = Uri.withAppendedPath(imageCollection, fileName)
                resolver.openInputStream(bmpUri).use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        bitmaps.add(bitmap)
                    } else {
                        Log.e(TAG, "Failed to decode image: $fileName")
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error loading image $fileName: ${e.localizedMessage}")
            }
        }

        return@withContext bitmaps
    }


    interface OnCropStatusListener {
        fun waterCrop(crop: Crop)
        fun removeCrop(crop: Crop)
        fun editCrop(crop: Crop)
        fun goToNotes(crop: Crop)
        fun onMemorySelect(bm: Bitmap)
        fun startCameraActivity(crop: Crop)
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: ArrayList<Crop>, param2: ArrayList<Int>) =
            CropListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(STATUS_CROP, param1)
                    putSerializable(ARG_DRAWABLES, param2)
                }
            }
    }
}
