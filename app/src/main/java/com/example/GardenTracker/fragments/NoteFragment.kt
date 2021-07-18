package com.example.GardenTracker.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.GardenTracker.R
import com.example.GardenTracker.model.Note

private const val CROP_NAME = "crop-name"
private const val CROP_TYPE = "crop-type"
private const val NOTE = "note"
private const val FROM_ALL_NOTES = "from-all-notes"

// TODO: Make '`' and '|' unallowable characters

class NoteFragment : Fragment() {

    private val TAG = "NoteFragment"

    private var mCropName: String? = null
    private var mCropType: String? = null
    private var mNote: Note? = null
    private var mAllNotes: Boolean = false

    private lateinit var mToolBar : Toolbar
    private lateinit var mSaveBtn : Button
    private lateinit var mDeleteBtn : Button
    private lateinit var mTextArea : EditText

    private var listener: NoteFragment.OnNoteInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mCropName = it.getString(CROP_NAME)
            mCropType = it.getString(CROP_TYPE)
            mNote = it.getSerializable(NOTE) as Note?
            mAllNotes = it.getBoolean(FROM_ALL_NOTES)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get views
        mToolBar = view.findViewById(R.id.note_toolbar)
        mTextArea = view.findViewById(R.id.note_text_area)
        mSaveBtn = view.findViewById(R.id.note_save_btn)
        mDeleteBtn = view.findViewById(R.id.delete_note_btn)

        // Set EditText focus listener
        mTextArea.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val imm: InputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }

        // Load saved note content
        if (mNote != null) {
            Log.d(TAG, "Loading note content...")
            mTextArea.setText(mNote!!.noteContent)
        }

        // Set on-click listeners
        mSaveBtn.setOnClickListener(View.OnClickListener {
            if (mNote != null) {
                mNote!!.noteContent = mTextArea.text.toString()
            } else {
                mNote = Note(mCropName!!, mCropType!!, mTextArea.text.toString())
            }
            listener?.saveNote(mNote!!)
        })

        mDeleteBtn.setOnClickListener(View.OnClickListener {
            if (mNote != null) {
                listener?.deleteNote(mNote!!)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (mNote != null) {
            (activity as AppCompatActivity).supportActionBar?.title = mNote!!.cropName + " " +
                mNote!!.creationDay.toString() + "/" +
                mNote!!.creationMonth.toString() + "/" +
                mNote!!.creationYear.toString()
        } else {
            (activity as AppCompatActivity).supportActionBar?.title = "New $mCropName Note"
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnNoteInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnNoteInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnNoteInteractionListener {
        fun saveNote(note: Note)
        fun deleteNote(note: Note)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param note Note being loaded
         * @return A new instance of fragment NoteFragment.
         */
        @JvmStatic
        fun newInstance(note: Note) =
            NoteFragment().apply {
                arguments = Bundle().apply {
                    putString(CROP_NAME, note.cropName)
                    putString(CROP_TYPE, note.cropType)
                    putSerializable(NOTE, note)
                }
            }

        @JvmStatic
        fun newInstance(type: String) =
            NoteFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(CROP_TYPE, type)
                }
            }
    }
}