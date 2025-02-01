package com.example.GardenTracker

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.GardenTracker.database.*
import com.example.GardenTracker.fragments.CropFragment
import com.example.GardenTracker.fragments.CropListFragment
import com.example.GardenTracker.fragments.NoteFragment
import com.example.GardenTracker.fragments.NotesFragment
import com.example.GardenTracker.model.*
import com.google.android.material.navigation.NavigationView
import kotlinx.android.parcel.Parcelize
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val EDIT_CROP = "edit-crop"
private const val ARG_CROP_LIST = "crop-list"
private const val ARG_DRAWABLES = "drawable-resources"
private const val CROP_NAME = "crop-name"
private const val CROP_TYPE = "crop-type"
private const val CROP_NOTES = "crop-notes"
private const val NOTE = "note"
private const val CROP_MEMORIES = "crop-memories"
private const val STATUS_CROP = "status_crop"
private const val DISPLAY_MEMORY = "display-memory"
private const val CAMERA_REQUEST = 0
private const val FROM_ALL_NOTES = "from-all-notes"
private const val WATER_CHANNEL_ID = "water-channel"
private const val WATER_NOTIFY_ID = 0

class MainActivity :
    AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    CropListFragment.OnCropFragmentInteractionListener,
    CropDialog.OnAddCropDialogInteraction,
    CropFragment.OnCropStatusListener,
    NotesFragment.OnNoteListInteractionListener,
    NoteFragment.OnNoteInteractionListener {

    private val TAG = "MAIN_ACTIVITY"

    private lateinit var iCropListFrag: Intent
    private lateinit var iPendingCropList: PendingIntent

    private var mAllNotes: Boolean = true

    private var receiver: DateTimeReceiver? = null

    private var dateTimeHolder: DateTimeHolder = DateTimeHolder()

    private lateinit var dbg: DatabaseGateway
    private lateinit var mSavedCrops: ArrayList<Crop>

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mNavView: NavigationView
    private lateinit var mNavController: NavController
    private lateinit var mDrawableResources: ArrayList<Drawable>

    private val mDrawableResIds = arrayListOf(
        R.drawable.ic_launcher_foreground
    )

    @Parcelize
    class DateTimeHolder() : Parcelable {
        var currYear: Int = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
            .get(Calendar.YEAR)
        var currMonth = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
            .get(Calendar.MONTH)
        var currDay = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
            .get(Calendar.DAY_OF_MONTH)
        var currHour = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
            .get(Calendar.HOUR_OF_DAY)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        iCropListFrag = Intent(this, CropListFragment::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        iPendingCropList = PendingIntent.getActivity(this, 0, iCropListFrag, 0)

        requestCameraPermission() // Get camera permissions

        // Create Notification Channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Setup channels
            val waterName = getString(R.string.WaterChannelName)
            val waterDescription = getString(R.string.WaterChannelDescription)
            val waterImportance = NotificationManager.IMPORTANCE_DEFAULT
            val waterChannel = NotificationChannel(WATER_CHANNEL_ID, waterName, waterImportance).apply {
                description = waterDescription
            }

            // Register Channels
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(waterChannel)
        }

        // Get database gateway
        dbg = DatabaseGateway(this)

        // Load saved data
        if (savedInstanceState != null) {
            if (!savedInstanceState.isEmpty) {
                Log.d(TAG, "Getting crops from savedInstanceState")
                if (savedInstanceState.get(ARG_CROP_LIST) is ArrayList<*>) {
                    if ((savedInstanceState.get(ARG_CROP_LIST) as ArrayList<*>)[0] is Crop) {
                        mSavedCrops = savedInstanceState.get(ARG_CROP_LIST) as ArrayList<Crop>
                    }
                }
            }
        } else {
            mSavedCrops = ArrayList<Crop>()
        }

        mSavedCrops = if (dbg.cropDataToLoad()) {
            Log.d(TAG, "Loading crops from database...")
            dbg.getAllCrops()
        } else {
            Log.d(TAG, "No crops to load.")
            ArrayList()
        }

        // Initialize CropListViewModel with saved crops
        CropListViewModel.cropList.value = mSavedCrops

        // Load the 4 crop icons
        loadDrawableResources()

        // Setup the top toolbar/actionbar
        val mToolbar: Toolbar = findViewById(R.id.drawer_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        // Setup the DrawerLayout and Navigation Component
        mDrawerLayout = findViewById(R.id.drawers)
        mNavView = findViewById(R.id.nav_view)
        mNavController = findNavController(R.id.nav_host_fragment)

        // Hookup Nav Component to actionbar (?)
        NavigationUI.setupActionBarWithNavController(this, mNavController, mDrawerLayout)
        NavigationUI.setupWithNavController(mNavView, mNavController)

        // Set nav listener
        mNavView.setNavigationItemSelectedListener(this)

        // Update crop data
        timeUpdateCrops()

    }

    override fun onStop() {
        super.onStop()

        Log.d(TAG,"onStop closing databases...")
        // Close databases
        dbg.closeDb()
    }

    override fun onSaveInstanceState(outState: Bundle) {

        Log.d(TAG, "Saving instance state...")

        // Save Crop states
        outState.putSerializable(ARG_CROP_LIST, mSavedCrops)

        super.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted.")
                Toast.makeText(this, "Camera permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Log.e(TAG, "Camera permission denied.")
                Toast.makeText(this, "Camera permission denied. Please enable it in settings.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun requestCameraPermission() {
        Log.d(TAG, "Requesting camera permissions.")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission already granted.")
                return
            }

            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Toast.makeText(this, "Camera permission is needed to take photos.", Toast.LENGTH_LONG).show()
            }

            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST)
        }
    }

    private fun timeUpdateCrops() {
        updateCropsTime()
        checkToNotify()
    }

    // Function to schedule watering reminder
    fun scheduleWateringReminder(wateringTimeInMillis: Long) {

        Log.d(TAG, "Scheduling watering reminder for " + wateringTimeInMillis)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, DateTimeReceiver::class.java)
        intent.putExtra("EXTRA_WATERING_TIME", wateringTimeInMillis)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            wateringTimeInMillis,
            pendingIntent
        )
    }


    private fun updateCropsTime() {
        Log.d(TAG, "Updating the water status of all crops")
        mSavedCrops.forEach { crop ->
            crop.updateNeedsWater(dateTimeHolder.currHour)
            if (crop.needsWater) {
                CropStatusViewModel.waterStatus.value = "Thirsty"
            } else {
                CropStatusViewModel.waterStatus.value = "Quenched"
            }
        }
        Log.d(TAG, "Updating crops in database")
        dbg.updateCrops(mSavedCrops)
        CropListViewModel.cropList.value = mSavedCrops
    }

    private fun checkToNotify() {
        if (mSavedCrops.any { crop -> crop.needsWater }) {
            // Setup notification
            var builder = NotificationCompat.Builder(this, WATER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_opacity_24)
                .setContentTitle(getString(R.string.WaterNotificationTitle))
                .setContentText(getString(R.string.WaterNotificationContent))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(iPendingCropList)
                .setAutoCancel(true)

            // Display notification
            with(NotificationManagerCompat.from(this)) {
                notify(WATER_NOTIFY_ID, builder.build())
            }
        }
    }

    private fun loadDrawableResources() {
        val resources = ArrayList<Drawable>()
        for (x in 0..3) {
            val resource = getDrawable(R.drawable.ic_launcher_foreground)
            if (resource != null) {
                resources.add(resource)
            }
        }
        mDrawableResources = resources
    }

    private fun getAllNotes() : ArrayList<Note> {
        val allNotes: List<Note>? = dbg.getAllNotes()
        if (allNotes != null) {
            Log.d(TAG, "Retrieved all notes from database.")
            return allNotes.toCollection(ArrayList())
        }
        Log.e(TAG, "No notes retrieved from database.")
        return ArrayList()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveBitmap(bm: Bitmap, filename: String) {

        val resolver = applicationContext.contentResolver

        // Get the primary shared external image storage
        val imageCollection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

        // Set image data
        val imageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        // Get image content uri
        val imageContentUri = resolver.insert(imageCollection, imageDetails)

        // Write data to pending image file
        if (imageContentUri != null) {
            resolver.openFileDescriptor(imageContentUri, "w", null).use { pfd ->
                if (pfd != null) {

                    Log.d(TAG, "File descriptor acquired. Writing image data...")

                    // Compress bitmap
                    val bytes = ByteArrayOutputStream()
                    bm.compress(Bitmap.CompressFormat.JPEG, 85, bytes)

                    // Write data
                    val outputStream = FileOutputStream(pfd.fileDescriptor)
                    outputStream.write(bytes.toByteArray())

                    // Close output stream
                    outputStream.close()
                } else {
                    Log.e(TAG, "Failed to acquire file descriptor...")
                }
            }

            // Release 'pending' status so other apps can access image
            imageDetails.clear()
            imageDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(imageContentUri, imageDetails, null, null)

            Log.d(TAG, "Image saved to shared storage successfully!")
        } else {
            Log.e(TAG, "Unable to retrieve ImageContentURI. Image not saved.")
        }

    }

    /*****************************************************************************************
     * BEGIN NAVIGATION OVERRIDES
     ****************************************************************************************/
    override fun onSupportNavigateUp() : Boolean {
        Log.d(TAG, "onSupportNavigateUp() called.")
        return NavigationUI.navigateUp(mNavController, mDrawerLayout)
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed() called")
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    // This listener attached to each item in the NavDrawer
    // When an item is selected, we go to it using the Nav Component's NavController
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        item.isChecked = true
        mDrawerLayout.closeDrawers()
        Log.d(TAG, "Navigating to id: ${item.itemId}")
        when (item.itemId) {
            R.id.your_garden_item -> {
                mAllNotes = false
                onSupportNavigateUp()
                mNavController.navigate(
                    R.id.action_homeFragment_to_cropFragment,
                    bundleOf(
                        Pair(ARG_DRAWABLES, mDrawableResIds)
                    )
                )
                mDrawerLayout.closeDrawer(GravityCompat.START)
            }
            R.id.harvests_item -> {
                onSupportNavigateUp()
                mNavController
                    .navigate(R.id.action_homeFragment_to_harvestFragment)
                mDrawerLayout.closeDrawer(GravityCompat.START)
            }
            R.id.notes_item -> {
                mAllNotes = true
                onSupportNavigateUp()
                mNavController.navigate(
                    R.id.action_homeFragment_to_cropNotesFragment,
                    bundleOf(
                        Pair(CROP_NOTES, getAllNotes())
                    )
                )
                mDrawerLayout.closeDrawer(GravityCompat.START)
            }
            R.id.settings_item -> {
                onSupportNavigateUp()
                mNavController
                    .navigate(R.id.action_homeFragment_to_settingsFragment)
                mDrawerLayout.closeDrawer(GravityCompat.START)
            }
            else -> false
        }
        return true
    }
    /*****************************************************************************************
     * END NAVIGATION OVERRIDES
     ****************************************************************************************/

    /*****************************************************************************************
     * BEGIN CROP LIST OVERRIDES
     ****************************************************************************************/
    override fun onAddDialogAccept(newCrop: Crop) {
        Log.d(TAG, "New Crop received.")

        // Add to saved crops
        mSavedCrops.add(newCrop)

        // Save the new crops data
        dbg.insertCrop(newCrop)

        // Update Crop list vew model
        CropListViewModel.cropList.value = ArrayList(mSavedCrops)
    }

    override fun onCropListInteraction(item: Crop) {
        Log.d(TAG, "Navigating to Crop Status fragment.")
        mNavController.navigate(
            R.id.action_cropListFragment_to_cropFragment,
            bundleOf(
                Pair(STATUS_CROP, item),
                Pair(ARG_DRAWABLES, mDrawableResIds)
            )
        )
    }

    /*****************************************************************************************
     * END CROP LIST OVERRIDES
     ****************************************************************************************/

    /*****************************************************************************************
     * BEGIN CROP STATUS OVERRIDES
     ****************************************************************************************/

    override fun onEditDialogAccept(editCrop: Crop) {
        // Update crop in database
        dbg.updateCrop(editCrop)

        // Update CropListViewModel
        mSavedCrops.find { it.id == editCrop.id }?.apply {
            val index = mSavedCrops.indexOf(this)
            mSavedCrops[index] = editCrop
        }
        CropListViewModel.cropList.value = ArrayList(mSavedCrops)

        // Update CropStatusViewModel
        CropStatusViewModel.waterHours.value = editCrop.waterHoursFromString()
    }

    override fun waterCrop(crop: Crop) {
        if (crop.needsWater) {
            crop.water()
            dbg.updateCrop(crop)
            Log.d(TAG, "Crop watered.")
            Toast.makeText(
                this,
                "${crop.name} watered.", Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                "${crop.name} does not currently need watering.", Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun editCrop(crop: Crop) {
        Log.d(TAG, "Navigating to EditCropDialog...")
        mNavController.navigate(
            R.id.action_cropFragment_to_addCropDialog2,
            bundleOf(
                Pair(EDIT_CROP, crop)
            )
        )
    }

    override fun removeCrop(crop: Crop) {

        mSavedCrops.remove(crop)

        // Remove crop notes from database
        val notes = dbg.getNotesForCrop(crop.name)
        notes?.forEach {
            dbg.deleteNote(it)
            Log.d(TAG, "Deleted note: $it")
        }

        // Remove crop memories from database
        val memories = dbg.getMemoriesForCrop(crop.id)
        memories?.forEach {
            dbg.deleteMemory(it)
            Log.d(TAG, "Deleted memories: $it")
        }

        // Remove crop from database
        dbg.deleteCrop(crop)

        Log.d(TAG, "Deleted crop: $crop. Navigating to Crop List fragment.")
        mNavController.navigate(
            R.id.action_cropFragment_to_cropListFragment,
            bundleOf(
                Pair(ARG_CROP_LIST, mSavedCrops),
                Pair(ARG_DRAWABLES, mDrawableResIds)
            )
        )
    }

    override fun goToNotes(crop: Crop) {
        Log.d(TAG, "Navigating to crop notes for: $crop")
        mNavController.navigate(
            R.id.action_cropFragment_to_cropNotesFragment,
            bundleOf(
                Pair(CROP_NAME, crop.name),
                Pair(CROP_TYPE, crop.type),
                Pair(CROP_NOTES, dbg.getNotesForCrop(crop.name))
            )
        )
    }

    override fun onMemorySelect(bm: Bitmap) {
        Log.d(TAG, "Navigating to Memory Display fragment")
        mNavController.navigate(
            R.id.action_cropFragment_to_memoryDisplayFragment,
            bundleOf(
                Pair(DISPLAY_MEMORY, bm)
            )
        )
    }

    override fun startCameraActivity() {
        Log.d(TAG, "Starting camera activity.")

        val iCapture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (iCapture.resolveActivity(packageManager) != null) {
            startActivityForResult(iCapture, CAMERA_REQUEST)
        } else {
            Log.e(TAG, "No camera app found.")
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun saveNewMemory(crop: Crop, memory: Bitmap) {

        // Update crops memories
        val memoriesSize = crop.memoriesFromString().size + 1
        val filename = crop.name + "_mem_" + memoriesSize + ".jpg"

        // Save bitmap to file
        saveBitmap(memory, filename)

        crop.addMemory(filename)

        // Renavigate to Crop Status page with new memory
        onSupportNavigateUp()
        mNavController.navigate(
            R.id.action_cropListFragment_to_cropFragment,
            bundleOf(
                Pair(STATUS_CROP, crop),
                Pair(ARG_DRAWABLES, mDrawableResIds)
            )
        )

        // Save updated crop list
        dbg.updateCrop(crop)
        dbg.insertMemory(Memory(cropId = crop.id, filePath = filename))
    }
    /**************************************************************************************
     * END CROP STATUS OVERRIDES
     **************************************************************************************/

    /**************************************************************************************
     * NOTE LIST OVERRIDES
     *************************************************************************************/
    override fun onNoteSelect(note: Note) {
        Log.d(TAG, "Navigating to Note fragment.")
        mNavController.navigate(
            R.id.action_cropNotesFragment_to_noteFragment,
            bundleOf(
                Pair(CROP_NAME, note.cropName),
                Pair(CROP_TYPE, note.cropType),
                Pair(NOTE, note),
                Pair(FROM_ALL_NOTES, mAllNotes)
            )
        )
    }

    override fun loadCropNotes(type: String) {
        TODO("not implemented")
    }

    override fun onAddNoteBtnPressed(name: String, type: String) {
        Log.d(TAG, "Navigating to note fragment.")
        mNavController.navigate(
            R.id.action_cropNotesFragment_to_noteFragment,
            bundleOf(
                Pair(CROP_NAME, name),
                Pair(CROP_TYPE, type),
                Pair(FROM_ALL_NOTES, mAllNotes)
            )
        )
    }

    /***************************************************************************************
     * END NOTE LIST OVERRIDES
     ***************************************************************************************/

    /***************************************************************************************
     * Begin NOTE OVERRIDES
     ***************************************************************************************/

    override fun saveNote(note: Note) {

        // Check if note already exists
        val maybeNote = dbg.getNoteByUid(note.id)

        val crop = mSavedCrops.find {
            Log.d(TAG, "saveNote(note: Note): Crop found.")
            it.name == note.cropName
        }
        if (crop != null) {
            Log.d(TAG, "Successfully found crop.")
            if (maybeNote == null) {
                // Add note to database
                 dbg.insertOne(note)
            } else {
                Log.d(TAG, "Updating note content.")
                // Update note content
                maybeNote.noteContent = note.noteContent
                // Update note in database
                 dbg.updateNote(note)
            }
        }

        // Re-navigate
        if (!mAllNotes) {
            Log.d(TAG, "Navigating to Crop Notes frag w/ only this crop notes.")
            mNavController.navigate(
                R.id.action_noteFragment_to_cropNotesFragment,
                bundleOf(
                    Pair(CROP_NAME, crop?.name),
                    Pair(CROP_TYPE, crop?.type),
                    Pair(CROP_NOTES, crop?.name?.let { dbg.getNotesForCrop(it) })
                )

            )
        } else {
            Log.d(TAG, "Navigating to Crop notes frag w/ all notes.")
            mNavController.navigate(
                R.id.action_noteFragment_to_cropNotesFragment,
                bundleOf(
                    Pair(CROP_NOTES, getAllNotes())
                )
            )
        }
    }

    override fun deleteNote(note: Note) {

        // Delete note from database
         dbg.deleteNote(note)

        val savedCrop = mSavedCrops.find {
            Log.d(TAG, "deleteNote(note: Note): Crop found")
            it.name == note.cropName
        }

        Log.d(TAG, "Navigating to Crop Notes fragment.")
        // Re-navigate
        mNavController.navigate(
            R.id.action_noteFragment_to_cropNotesFragment,
            bundleOf(
                Pair(CROP_NAME, savedCrop!!.name),
                Pair(CROP_TYPE, savedCrop.type),
                when(mAllNotes) {
                    false -> Pair(CROP_NOTES, dbg.getNotesForCrop(savedCrop.name))
                    true -> Pair(CROP_NOTES, getAllNotes())
                }
            )
        )
    }

    /***************************************************************************************
     * END NOTE OVERRIDES
     ***************************************************************************************/

}
