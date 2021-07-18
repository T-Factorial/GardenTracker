package com.example.GardenTracker.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.GardenTracker.R
import com.example.GardenTracker.adapters.MyGardenNoteAdapter
import com.example.GardenTracker.adapters.NoteTouchHelper
import com.example.GardenTracker.database.DatabaseGateway
import com.example.GardenTracker.model.Note
import com.google.android.material.floatingactionbutton.FloatingActionButton

// TODO: Change method by which we determine this is crop specific notes or all notes

class NotesFragment : Fragment() {

    private var columnCount = 1
    private var mCropName: String? = null
    private var mCropType: String? = null
    private var mCropNotes: ArrayList<Note>? = null
    private var mDrawable: Drawable? = null
    private var mDrawables: ArrayList<Drawable>? = null

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: MyGardenNoteAdapter
    private lateinit var addNoteBtn: FloatingActionButton

    private lateinit var dbg: DatabaseGateway

    private var listener: OnNoteListInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mCropName = it.getString(CROP_NAME).toString()
            mCropType = it.getString(CROP_TYPE).toString()
            mCropNotes = it.getSerializable(CROP_NOTES) as ArrayList<Note>
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_note_list, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbg = context?.let { DatabaseGateway(it) }!!

        mRecyclerView = view.findViewById(R.id.note_list)
        addNoteBtn = view.findViewById(R.id.add_note_btn)

        // Set drawable based on crop type
        if (mDrawable != null) {
            mDrawable = when (mCropType) {
                "Flower" -> context?.getDrawable(R.drawable.ic_launcher_foreground)!!
                "Herb" -> context?.getDrawable(R.drawable.ic_launcher_foreground)!!
                "Fruit" -> context?.getDrawable(R.drawable.ic_launcher_foreground)!!
                "Vegetable" -> context?.getDrawable(R.drawable.ic_launcher_foreground)!!
                else -> context?.getDrawable(R.drawable.ic_launcher_foreground)!!
            }
        } else {
            loadDrawableResources()
        }

        // Set the adapter
        with(mRecyclerView) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            if (mCropNotes != null) {
                adapter =
                    MyGardenNoteAdapter(
                        mDrawable,
                        mDrawables,
                        mCropNotes!!,
                        listener
                    )
            } else {
                val notes = dbg.getAllNotes()
                if (notes != null) {
                    adapter =
                        MyGardenNoteAdapter(
                            mDrawable,
                            mDrawables,
                            notes,
                            listener
                        )
                } else {
                    adapter =
                        MyGardenNoteAdapter(
                            mDrawable,
                            mDrawables,
                            ArrayList(),
                            listener
                        )
                }
            }
            mAdapter = adapter as MyGardenNoteAdapter
        }
        mAdapter.notifyDataSetChanged()

        // Add click listener to FAB
        if (mCropName != "null") {
            addNoteBtn.setOnClickListener() {
                // Bring up note editor
                listener?.onAddNoteBtnPressed(mCropName!!, mCropType!!)
            }
        } else {
            addNoteBtn.hide()
        }

        // Add swipe functionality to note list
        ItemTouchHelper(NoteTouchHelper()).attachToRecyclerView(mRecyclerView)

    }

    override fun onResume() {
        super.onResume()
        if (mCropName != null && mCropName != "null") {
            (activity as AppCompatActivity)?.supportActionBar?.title = mCropName + " Notes"
        } else {
            (activity as AppCompatActivity)?.supportActionBar?.title = "All Notes"
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnNoteListInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnNoteListInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnNoteListInteractionListener {
        fun onNoteSelect(note: Note)
        fun loadCropNotes(filename: String)
        fun onAddNoteBtnPressed(name: String, type: String)
    }

    private fun loadDrawableResources() {
        val resources = ArrayList<Drawable>()
        for (x in 0..3) {
            val resource = context?.getDrawable(R.drawable.ic_launcher_foreground)
            if (resource != null) {
                resources.add(resource)
            }
        }
        mDrawables = resources
    }

    companion object {

        const val CROP_NAME = "crop-name"
        const val CROP_TYPE = "crop-type"
        const val CROP_NOTES = "crop-notes"

        @JvmStatic
        fun newInstance(cropName: String, cropType: String, cropNotes: ArrayList<Note>) =
            NotesFragment().apply {
                arguments = Bundle().apply {
                    putString(CROP_NAME, cropName)
                    putString(CROP_TYPE, cropType)
                    putSerializable(CROP_NOTES, cropNotes)
                }
            }
    }
}
