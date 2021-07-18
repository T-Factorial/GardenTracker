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
