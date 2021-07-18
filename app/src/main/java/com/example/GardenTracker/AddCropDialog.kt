package com.example.GardenTracker

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.example.GardenTracker.model.Crop

private const val ARG_NEW_CROP = "new-crop"
private const val CROP_NAME = "crop-name"
private const val CROP_TYPE = "crop-type"
private const val GROWTH_TIME = "growth-time"
private const val WATER_FREQ = "water-freq"

class AddCropDialog : DialogFragment() {

    private var listener: OnCropDialogInteraction? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            // Get Builder
            val builder = AlertDialog.Builder(it)
            // Inflate custom layout
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.fragment_add_crop_dialog, null)
            val nameInput: EditText = view.findViewById(R.id.crop_name_input)
            val typeInput: Spinner = view.findViewById(R.id.crop_type_input)
            val harvestInput: EditText = view.findViewById(R.id.crop_growth_input)
            val waterInput: EditText = view.findViewById(R.id.water_freq_input)

            builder.setMessage(R.string.NewCropDialogTitle)
                .setView(view)
                .setPositiveButton(R.string.PosButt,
                    DialogInterface.OnClickListener { dialog, id ->
                        var err = false
                        val name = nameInput.text.toString()
                        val type = typeInput.selectedItem.toString()
                        val growth = harvestInput.text
                        val water = waterInput.text
                        if (name.isEmpty()) {
                            err = true
                            Toast.makeText(
                                context,
                                "You did not enter a crop name.", Toast.LENGTH_SHORT
                            ).show()
                        }
                        if (type.equals("")) {
                            err = true
                            Toast.makeText(
                                context,
                                "You did not select a crop type.", Toast.LENGTH_SHORT
                            ).show()
                        }
                        if (growth.isEmpty()) {
                            err = true
                            Toast.makeText(
                                context,
                                "You did not enter a growth time.", Toast.LENGTH_SHORT
                            ).show()
                        }
                        if (water.isEmpty()) {
                            err = true
                            Toast.makeText(
                                context,
                                "You did not enter a watering frequency.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        if (!err) {
                            var newCrop = Crop(
                                name, type,
                                growth.toString().toInt(), water.toString().toInt()
                            )
                            var newCropData = bundleOf(
                                Pair(CROP_NAME, name),
                                Pair(CROP_TYPE, type),
                                Pair(GROWTH_TIME, growth.toString().toInt()),
                                Pair(WATER_FREQ, growth.toString().toInt())
                            )
                            listener?.onDialogAccept(newCropData)
                        }
                    })
                .setNegativeButton(R.string.NegButt,
                    DialogInterface.OnClickListener { dialog, id ->
                        // User cancelled the dialog
                        dialog.cancel()
                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCropDialogInteraction) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnCropDialogInteraction")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnCropDialogInteraction {
        fun onDialogAccept(newCropDate: Bundle)
    }
}
