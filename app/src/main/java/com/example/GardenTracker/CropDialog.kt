package com.example.GardenTracker

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.example.GardenTracker.model.Crop
import java.util.*
import kotlin.collections.ArrayList

private const val EDIT_CROP = "edit-crop"

/**
 * This class is a Dialog for crop creation and editing.
 */

class CropDialog : DialogFragment() {

    private var listener: OnAddCropDialogInteraction? = null

    private val TAG = "ADD_CROP_DIALOG"

    private var mEditCrop: Crop? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { it ->

            arguments?.let {
                mEditCrop = it.getSerializable(EDIT_CROP) as Crop
            }

            // Get Builder
            val builder = AlertDialog.Builder(it)
            // Inflate custom layout
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.fragment_crop_dialog, null)
            val nameInput: EditText = view.findViewById(R.id.crop_name_input)
            val typeInput: Spinner = view.findViewById(R.id.crop_type_input)
            val harvestInput: EditText = view.findViewById(R.id.crop_growth_input)
            val waterLabel: TextView = view.findViewById(R.id.waterHoursLabel)
            val waterInput: Spinner = view.findViewById(R.id.water_hours_input)
            val undoButton: Button = view.findViewById(R.id.undo_hour_button)

            var isTouched = false

            var waterHours: MutableList<Int> = ArrayList()

            var dialogTitle = ""
            var posBtnMsg = ""

            if (mEditCrop != null) {
                // Fill in dialog w/ current data
                nameInput.setText(mEditCrop!!.name)
                when (mEditCrop!!.type) {
                    "Flower" -> typeInput.setSelection(1)
                    "Herb" -> typeInput.setSelection(2)
                    "Vegetable" -> typeInput.setSelection(3)
                    "Fruit" -> typeInput.setSelection(4)
                }
                harvestInput.setText("${mEditCrop!!.growthTime}")
                waterHours = mEditCrop!!.waterHoursFromString() as MutableList<Int>
                waterHours.forEach {
                    waterLabel.text = "${waterLabel.text}\n${intToTime(it)}"
                }

                if (waterLabel.text != "") {
                    undoButton.visibility = View.VISIBLE
                }

                dialogTitle = getString(R.string.EditCropDialogTitle, mEditCrop!!.name)
                posBtnMsg = getString(R.string.EdButt)
            } else {
                dialogTitle = getString(R.string.NewCropDialogTitle)
                posBtnMsg = getString(R.string.PosButt)
            }

            // Set click listener for undo add hour button
            undoButton.setOnClickListener {
                if (waterHours.isNotEmpty()) {
                    waterHours.dropLast(1)
                    waterLabel.text = waterLabel.text.dropLastWhile { c -> c != '\n' }
                    waterLabel.text = waterLabel.text.dropLast(1)
                }
                if (0 == waterHours.size) {
                    undoButton.visibility = View.INVISIBLE
                }
            }

            // Set touch listener for waterInput spinner
            waterInput.setOnTouchListener { v, _ ->
                v.performClick()
                isTouched = true
                false
            }

            // Set item select listener for waterInput spinner
            waterInput.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (isTouched) {
                        if (parent != null) {
                            if (position != 0) {
                                Log.d(TAG,
                                    "Spinner item ${parent.getItemAtPosition(position)} selected.")
                                if (!waterHours.contains(position)) {
                                    val selectedTime = parent.getItemAtPosition(position)
                                    waterLabel.text = "${waterLabel.text}\n$selectedTime"
                                    waterHours.add(position - 1)

                                    if (undoButton.visibility != View.VISIBLE) {
                                        undoButton.visibility = View.VISIBLE
                                    }
                                }
                            }
                            waterInput.setSelection(0)
                        }
                    }
                    isTouched = false
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }


            // Build dialog
            builder.setMessage(dialogTitle)
                .setView(view)
                .setPositiveButton(posBtnMsg,
                    DialogInterface.OnClickListener { dialog, id ->
                        var err = false
                        val name = nameInput.text.toString()
                        val type = typeInput.selectedItem.toString()
                        val growth = harvestInput.text

                        if (name.isEmpty()) {
                            err = true
                            Toast.makeText(
                                context,
                                "You did not enter a crop name.", Toast.LENGTH_SHORT
                            ).show()
                        }
                        if (typeInput.selectedItemPosition == 0) {
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
                        if (waterHours.isEmpty()) {
                            err = true
                            Toast.makeText(
                                context,
                                "You did not enter a watering frequency.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        if (!err) {
                            if (mEditCrop != null) {
                                mEditCrop = mEditCrop!!.updateName(name)
                                mEditCrop = mEditCrop!!.updateType(type)
                                mEditCrop = mEditCrop!!.updateGrowthTime(growth.toString().toInt())
                                mEditCrop = mEditCrop!!.updateWaterHours(waterHours)
                                listener?.onEditDialogAccept(mEditCrop!!)
                            } else {
                                var newCrop = Crop(name = name, type = type,
                                    growthTime = growth.toString().toInt())
                                newCrop = newCrop.updateWaterHours(waterHours)
                                listener?.onAddDialogAccept(newCrop)
                            }
                            for (time: Int in waterHours) {
                                val wateringTimeInMillis = calculateWateringTime(time)
                                (activity as MainActivity).scheduleWateringReminder(wateringTimeInMillis)
                            }
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
        if (context is OnAddCropDialogInteraction) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnAddCropDialogInteraction")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun intToTime(time: Int): String {
        when (time) {
            0 ->  return "12:00AM"
            1 ->  return "1:00AM"
            2 ->  return "2:00AM"
            3 ->  return "3:00AM"
            4 ->  return "4:00AM"
            5 ->  return "5:00AM"
            6 ->  return "6:00AM"
            7 ->  return "7:00AM"
            8 ->  return "8:00AM"
            9 ->  return "9:00AM"
            10 ->  return "10:00AM"
            11 ->  return "11:00AM"
            12 ->  return "12:00PM"
            13 ->  return "1:00PM"
            14 ->  return "2:00PM"
            15 ->  return "3:00PM"
            16 ->  return "4:00PM"
            17 ->  return "5:00PM"
            18 ->  return "6:00PM"
            19 ->  return "7:00PM"
            20 ->  return "8:00PM"
            21 ->  return "9:00PM"
            22 ->  return "10:00PM"
            23 ->  return "11:00PM"
            else -> return ""
        }
    }

    private fun calculateWateringTime(time: Int): Long {
        val calendar = Calendar.getInstance()

        // Set the watering hour (0 - 23)
        calendar.set(Calendar.HOUR_OF_DAY, time)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // If the calculated time is in the past for today, set it for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }

    interface OnAddCropDialogInteraction {
        fun onAddDialogAccept(newCrop: Crop)
        fun onEditDialogAccept(editCrop: Crop)
    }
}
