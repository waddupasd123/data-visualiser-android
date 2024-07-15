package com.example.androidapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.mutableStateListOf
import java.io.File

class DataManager(
    private val context: Context,
    private val createFileLauncher: ActivityResultLauncher<Intent>
) {


    val csvFilesList = mutableStateListOf<File>()
//    val timestamp = System.currentTimeMillis()
//    val csvFile = File(deviceFolder, "data_$timestamp.csv")

    fun getDirectoryUri(deviceAddress: String): Uri? {
        val sharedPreferences = context.getSharedPreferences("DirectoryUris", Context.MODE_PRIVATE)
        val uriString = sharedPreferences.getString(deviceAddress, null)
        return if (uriString != null) Uri.parse(uriString) else null
    }

    fun setDirectoryUri(deviceAddress: String, uri: Uri) {
        val sharedPreferences = context.getSharedPreferences("DirectoryUris", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(deviceAddress, uri.toString()).apply()
    }

    private fun createFolder(folderName: String, parentUri: Uri): Uri? {
        val contentResolver = context.contentResolver
        val docUri = DocumentsContract.buildDocumentUriUsingTree(
            parentUri,
            DocumentsContract.getTreeDocumentId(parentUri)
        )
        return try {
            DocumentsContract.createDocument(contentResolver, docUri, DocumentsContract.Document.MIME_TYPE_DIR, folderName)
        } catch (e: Exception) {
            Log.e("DataManager", "Failed to create folder: $folderName", e)
            null
        }
    }


    fun createDeviceFolder(deviceAddress: String, parentUri: Uri): Uri? {
        val uri = createFolder(deviceAddress, parentUri)
        if (uri != null) {
            setDirectoryUri(deviceAddress, uri)
        }
        return uri
    }

    fun deleteFolder(deviceAddress: String) {
        val uri = getDirectoryUri(deviceAddress)
        val sharedPreferences = context.getSharedPreferences("DirectoryUris", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(deviceAddress)
        editor.apply();
        try {
            if (uri != null) {
                DocumentsContract.deleteDocument(context.applicationContext.contentResolver, uri)
            }
            Log.d("DataManager", "Deleted folder: $uri")
        } catch (e: Exception) {
            Log.e("DataManager", "Failed to delete folder: $uri", e)
        }
    }

    fun createCsvFile(deviceAddress: String) {
        val timestamp = System.currentTimeMillis()
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val parentFolder = File(documentsDir, "BLESensorData")
            val deviceFolder = File(parentFolder, deviceAddress)
            val pickerInitialUri =deviceFolder.toURI()
            putExtra(Intent.EXTRA_TITLE, "($deviceAddress)_$timestamp.csv")
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
            Log.d("DataManager", "Initial url: $pickerInitialUri")
        }
        createFileLauncher.launch(intent)
    }
}