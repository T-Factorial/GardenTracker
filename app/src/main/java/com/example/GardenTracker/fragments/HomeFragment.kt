package com.example.GardenTracker.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.GardenTracker.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "DRAWABLE"

/**
 * This fragment is meant to be the Home fragment of the app.
 * It will contain a summary of crop statuses (perhaps # of crops that need
 * watered, and whether there is a crop that has reached 100% growth progress)
 * It could also contain a carousel of crop memories, and shortcuts to notes and all memories
 *
 * Ideally, we change the app from it's current state to get rid of the Drawer layout, which
 * is currently the user's main navigational point around the app, to large buttons on the home
 * screen that take you to each of the app's destinations.
 */

class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var mDrawables: ArrayList<Drawable?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mDrawables = it.getSerializable(ARG_PARAM1) as ArrayList<Drawable?>
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity)?.supportActionBar?.title = "Garden Tracker Home"
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: ArrayList<Drawable?>) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PARAM1, param1)
                }
            }
    }
}
