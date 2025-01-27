package com.example.GardenTracker.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.GardenTracker.R
import com.example.GardenTracker.adapters.MyMemoryAdapter
import com.example.GardenTracker.database.DatabaseGateway
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * This fragment contains a list of all memories for a selected crop.
 * Users can view existing memories and add new ones.
 */
class MemoriesFragment : Fragment() {

    private val TAG = "MEMORY_LIST_FRAGMENT"

    private var columnCount = 3
    private var cropId: Int? = null
    private var cropMemories: ArrayList<Bitmap>? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyMemoryAdapter
    private lateinit var addMemoryBtn: FloatingActionButton

    private lateinit var databaseGateway: DatabaseGateway

    private var listener: OnMemoryListInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            Log.d(TAG, "Unpacking savedInstanceState arguments.")
            cropId = it.getInt(CROP_ID)
            cropMemories = it.getParcelableArrayList(CROP_MEMORIES)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_memory_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseGateway = context?.let { DatabaseGateway(it) }!!

        recyclerView = view.findViewById(R.id.memory_list)
        addMemoryBtn = view.findViewById(R.id.add_memory_btn)

        with(recyclerView) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }

            // Load crop memories if provided, otherwise fetch from the database
            val memories = cropMemories ?: cropId?.let {
                ArrayList(databaseGateway.getMemoriesForCrop(it).map { memory ->
                    // Load Bitmap from file path
                    BitmapFactory.decodeFile(memory.filePath)
                })
            } ?: ArrayList()

            adapter = MyMemoryAdapter(memories, listener!!)
            this.adapter = adapter
        }
        adapter.notifyDataSetChanged()

        // Add click listener to FAB
        addMemoryBtn.setOnClickListener {
            listener?.onAddMemory(cropId)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity)?.supportActionBar?.title = "Crop Memories"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnMemoryListInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnMemoryListInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnMemoryListInteractionListener {
        fun onMemorySelect(memory: Bitmap)
        fun onAddMemory(cropId: Int?)
    }

    companion object {
        private const val CROP_ID = "crop-id"
        private const val CROP_MEMORIES = "crop-memories"

        @JvmStatic
        fun newInstance(cropId: Int, cropMemories: ArrayList<Bitmap>) =
            MemoriesFragment().apply {
                arguments = Bundle().apply {
                    putInt(CROP_ID, cropId)
                    putParcelableArrayList(CROP_MEMORIES, cropMemories)
                }
            }
    }
}
