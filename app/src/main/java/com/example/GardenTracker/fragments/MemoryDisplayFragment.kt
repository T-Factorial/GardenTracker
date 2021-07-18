package com.example.GardenTracker.fragments

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.GardenTracker.R

private const val DISPLAY_MEMORY = "display-memory"

class MemoryDisplayFragment : Fragment() {

    private lateinit var memoryDisplay : ImageView
    private lateinit var memory : Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            memory = it.getParcelable<Bitmap>(DISPLAY_MEMORY) as Bitmap
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_memory_display, container, false)

        memoryDisplay = view.findViewById(R.id.memory_display)
        memoryDisplay.setImageBitmap(memory)

        return view
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity)?.supportActionBar?.title = "Your Memory"
    }

}
