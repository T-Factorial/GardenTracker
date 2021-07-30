package com.example.GardenTracker

import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
import com.example.GardenTracker.model.Crop
import com.example.GardenTracker.model.Note
import com.google.android.material.navigation.NavigationView
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

private const val ARG_COLUMN_COUNT = "column-count"
private const val NEW_CROP = "new-crop"
private const val ARG_CROP_LIST = "crop-list"
private const val ARG_DRAWABLES = "drawable-resources"
private const val CROP_NAME = "crop-name"
private const val CROP_TYPE = "crop-type"
private const val CROP_NOTES = "crop-notes"
private const val NOTE = "note"
private const val NOTE_CONTENT = "note-content"
private const val CROP_MEMORIES = "crop-memories"
private const val GROWTH_TIME = "growth-time"
private const val WATER_HOURS = "water-freq"
private const val STATUS_CROP = "status_crop"
private const val DISPLAY_MEMORY = "display-memory"
private const val CAMERA_REQUEST = 0
private const val FROM_ALL_NOTES = "from-all-notes"

private var currYear = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
    .get(Calendar.YEAR)
private var currMonth = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
    .get(Calendar.MONTH)
private var currDay = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
    .get(Calendar.DAY_OF_MONTH)
private var currHour = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
    .get(Calendar.HOUR_OF_DAY)

class MainActivity :
    AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    CropListFragment.OnCropFragmentInteractionListener,
    AddCropDialog.OnCropDialogInteraction,
    CropFragment.OnCropStatusListener,
    NotesFragment.OnNoteListInteractionListener,
    NoteFragment.OnNoteInteractionListener {

    private val TAG = "MAIN_ACTIVITY"

    private var iCapture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

    private var mAllNotes: Boolean = true

    private var receiver: DateTimeReceiver? = null

    private lateinit var dbg: DatabaseGateway
    private lateinit var mSavedCrops: ArrayList<Crop>

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mNavView: NavigationView
    private lateinit var mNavController: NavController
    private lateinit var mDrawableResources: ArrayList<Drawable>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Register DateTime Receiver
        Log.d(TAG, "Registering DateTimeReceiver...")
        receiver = DateTimeReceiver()
        registerReceiver(receiver, IntentFilter("android.intent.action.TIME_TICK"))

        // Get database gateway
        dbg = DatabaseGateway(this)

        // Load saved data
        if (savedInstanceState != null) {
            if (!savedInstanceState.isEmpty) {
                Log.d(TAG, "Getting crops from savedInstanceState")
                mSavedCrops = savedInstanceState.get(ARG_CROP_LIST) as ArrayList<Crop>
            }
        } else {
            mSavedCrops = ArrayList<Crop>()
        }

        if (dbg.cropDataToLoad()) {
            Log.d(TAG, "Loading crops from database...")
            mSavedCrops = dbg.getAllCrops() as ArrayList<Crop>
        } else {
            Log.d(TAG, "No crops to load.")
            mSavedCrops = ArrayList()
        }

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

    }

    override fun onStop() {
        super.onStop()

        // Unregister DateTimeReceiver
        Log.d(TAG, "onStop unregistering broadcast receiver...")
        if (receiver != null) {
            unregisterReceiver(receiver)
        }

        Log.d(TAG,"onStop closing databases...")
        // Close databases
        dbg.closeCropDb()
        dbg.closeNoteDb()
    }

    override fun onSaveInstanceState(outState: Bundle) {

        Log.d(TAG, "Saving instance state...")

        // Save Crop states
        outState.putSerializable(ARG_CROP_LIST, mSavedCrops)

        super.onSaveInstanceState(outState)
    }

    private fun updateCropsTime() {
        Log.d(TAG, "Updating the water status of all crops")
        mSavedCrops.forEach {
            it.updateNeedsWater(currHour)
        }
    }

    // This Broadcast Receiver will be registered so that
    // we're (hopefully) constantly listening to the time
    // and it will then update our crops time variables and check if they're ready
    class DateTimeReceiver() : BroadcastReceiver() {

        private val TAG = "DATE_TIME_RECEIVER"

        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Received an intent: ${intent.toString()}")
            if (intent != null) {
                if (intent.action == "android.intent.action.TIME_TICK") { // Check intent matches
                    if (currHour != GregorianCalendar.  // Only perform updates at each hour
                                    getInstance(Locale("en_US@calendar=english"))
                                    .get(Calendar.HOUR_OF_DAY)) {

                        Log.d(TAG, "Updating app's date information")

                        // Update the app's current date data
                        currYear = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
                            .get(Calendar.YEAR)
                        currMonth = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
                            .get(Calendar.MONTH)
                        currDay = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
                            .get(Calendar.DAY_OF_MONTH)
                        currHour = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
                            .get(Calendar.HOUR_OF_DAY)

                        // Check and update crops for watering
                        MainActivity().updateCropsTime()
                    }

                    // TODO integrate the following commented-out code elsewhere in the program.
                    /*
                    // Update current crop date
                    thisCrop.updateCropDate()

                    // Check if crop ready to harvest
                    if (thisCrop.checkReadyToHarvest()) {
                        thisCrop.setReadyToHarvest()
                        // TODO notify user crop is ready to harvest
                    }

                    // Check if crop needs water
                    thisCrop.updateNeedsWater()

                    if (thisCrop.needsWater) {
                        // TODO notify user crop needs to be watered
                    }
                    */
                }
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

    fun loadBitmaps(crop: Crop) : java.util.ArrayList<Bitmap> {

        val files : List<String> = crop.memoriesFromString()
        val bmps = java.util.ArrayList<Bitmap>()

        // Get resolver
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

        // Load each bitmap from filename and add to bitmap list
        files.forEach {
            var bmp : Bitmap? = null

            val bmpUri : Uri = Uri.withAppendedPath(
                imageCollection,
                it // filename
            )

            resolver.openInputStream(bmpUri).use { stream ->
                try {
                    bmp = BitmapFactory.decodeStream(stream)
                    stream!!.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Error opening input stream...")
                }
            }

            if (bmp != null) {
                Log.d(TAG, "Successfully loaded image: $it")
                bmps.add(bmp!!)
            } else {
                Log.e(TAG, "Failed to load image $it...")
            }
        }

        return bmps
    }

    /*****************************************************************************************
     * BEGIN NAVIGATION OVERRIDES
     ****************************************************************************************/
    override fun onSupportNavigateUp() : Boolean {
        Log.d(TAG, "onSupportNavigateUp() called.")
        return NavigationUI.navigateUp(mNavController, mDrawerLayout)
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPresed() called")
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    // This listener attached to each item in the NavDrawer
    // When an item is selected, we go to it using the Nav Component's NavController
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        item.setChecked(true)
        mDrawerLayout.closeDrawers()
        Log.d(TAG, "Navigating to id: ${item.itemId}")
        when (item.itemId) {
            R.id.your_garden_item -> {
                mAllNotes = false
                onSupportNavigateUp()
                mNavController.navigate(
                    R.id.action_homeFragment_to_cropFragment,
                    bundleOf(
                        Pair(ARG_CROP_LIST, mSavedCrops),
                        Pair(ARG_DRAWABLES, mDrawableResources)
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
            R.id.help_item ->  {
                onSupportNavigateUp()
                mNavController
                .navigate(R.id.action_homeFragment_to_helpFragment)
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
    override fun onDialogAccept(newCrop: Bundle) {


        // Unpack bundle
        val nCrop = newCrop.get(NEW_CROP) as Crop
        mSavedCrops.add(nCrop)

        Log.d(TAG, "New Crop received.")

        // Update the list with new crop (re-navigate to the frag again)
        onSupportNavigateUp()
        mNavController.navigate(
            R.id.action_homeFragment_to_cropFragment,
            bundleOf(
                Pair(ARG_CROP_LIST, mSavedCrops),
                Pair(ARG_DRAWABLES, mDrawableResources)
            )
        )

        // Save the new crops data
        dbg.insertCrop(nCrop)

    }

    override fun onCropListInteraction(item: Crop) {
        val contain = ArrayList<Crop>()
        contain.add(item)
        val memories = loadBitmaps(item)
        Log.d(TAG, "Navigating to Crop Status fragment.")
        mNavController.navigate(
            R.id.action_cropListFragment_to_cropFragment,
            bundleOf(
                Pair(STATUS_CROP, contain),
                Pair(ARG_DRAWABLES, mDrawableResources),
                Pair(CROP_MEMORIES, memories)
            )
        )
    }

    override fun onAddCropBtnPressed() {
        mNavController.navigate(R.id.action_cropFragment_to_addCropDialog)
    }
    /*****************************************************************************************
     * END CROP LIST OVERRIDES
     ****************************************************************************************/

    /*****************************************************************************************
     * BEGIN CROP STATUS OVERRIDES
     ****************************************************************************************/
    override fun waterCrop(crop: Crop) {
        mSavedCrops[mSavedCrops.indexOf(crop)].water()
        dbg.updateCrop(crop)
        Log.d(TAG, "Crop watered.")
    }

    override fun removeCrop(crop: Crop) {

        mSavedCrops.remove(crop)

        // Remove crop notes from database
        val notes = dbg.getNotesByCrop(crop.name)
        notes?.forEach {
            dbg.deleteNote(it)
            Log.d(TAG, "Deleted note: $it")
        }

        // Remove crop from database
        dbg.deleteCrop(crop)

        Log.d(TAG, "Deleted crop: $crop. Navigating to Crop List fragment.")
        mNavController.navigate(
            R.id.action_cropFragment_to_cropListFragment,
            bundleOf(
                Pair(ARG_CROP_LIST, mSavedCrops),
                Pair(ARG_DRAWABLES, mDrawableResources)
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
                Pair(CROP_NOTES, dbg.getNotesByCrop(crop.name))
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
        startActivityForResult(iCapture, CAMERA_REQUEST)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun saveNewMemory(crop: Crop, memory: Bitmap) {

        val crop = mSavedCrops.get(mSavedCrops.indexOf(crop))

        // Update crops memories
        val memoriesSize = crop.memoriesFromString().size + 1
        val filename = crop.name + "_mem_" + memoriesSize + ".jpg"

        // Save bitmap to file
        saveBitmap(memory, filename)

        crop.addMemory(filename)

        // Renavigate to Crop Status page with new memory
        onSupportNavigateUp()
        val contain = ArrayList<Crop>()
        contain.add(crop)
        mNavController.navigate(
            R.id.action_cropListFragment_to_cropFragment,
            bundleOf(
                Pair(STATUS_CROP, contain),
                Pair(ARG_DRAWABLES, mDrawableResources)
            )
        )

        // Save updated crop list
        dbg.updateCrops(mSavedCrops)
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
        val maybeNote = getAllNotes().find {
            Log.d(TAG, "saveNote(note: Note): Note found.")
            it.id == note.id
        }

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
                    Pair(CROP_NOTES, crop?.name?.let { dbg.getNotesByCrop(it) })
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
                    false -> Pair(CROP_NOTES, dbg.getNotesByCrop(savedCrop.name))
                    true -> Pair(CROP_NOTES, getAllNotes())
                }
            )
        )
    }

    /***************************************************************************************
     * END NOTE OVERRIDES
     ***************************************************************************************/

}
