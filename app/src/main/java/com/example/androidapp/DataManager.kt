package com.example.androidapp

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateMapOf
import java.text.SimpleDateFormat
import java.util.Locale

class DataManager(
    private val context: Context
) {

    // Device address to directory uri
    // Device uri to list of filenames
    // filename to file uri

    // Device uri to list of filenames
    val deviceFilesList = mutableStateMapOf<Uri, MutableSet<String>>()
//    val timestamp = System.currentTimeMillis()
//    val csvFile = File(deviceFolder, "data_$timestamp.csv")

    fun loadDeviceFiles(deviceAddress: String) {
        val deviceUri = getDirectoryUri(deviceAddress)
        val sharedPreferences = context.getSharedPreferences("DataManager", Context.MODE_PRIVATE)
        deviceUri?.let {
            val files = sharedPreferences.getStringSet(deviceUri.toString(), emptySet())?.toMutableSet() ?: mutableSetOf()
            deviceFilesList[deviceUri] = files
        }
    }

    fun getDirectoryUri(deviceAddress: String): Uri? {
        val sharedPreferences = context.getSharedPreferences("DataManager", Context.MODE_PRIVATE)
        val uriString = sharedPreferences.getString(deviceAddress, null)
        return if (uriString != null) Uri.parse(uriString) else null
    }

    fun setDirectoryUri(deviceAddress: String, uri: Uri) {
        val sharedPreferences = context.getSharedPreferences("DataManager", Context.MODE_PRIVATE)
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
        val sharedPreferences = context.getSharedPreferences("DataManager", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        // Remove device address to directory uri
        editor.remove(deviceAddress)
        //  Remove filenames to file uri
        val fileList = sharedPreferences.getStringSet(uri.toString(), emptySet())?.toList() ?: emptyList()
        fileList.forEach{fileName ->
            editor.remove(fileName)
        }
        // Remove device uri to filenames list
        editor.remove(uri.toString())
        editor.apply()
        try {
            if (uri != null) {
                DocumentsContract.deleteDocument(context.applicationContext.contentResolver, uri)
            }
            Log.d("DataManager", "Deleted folder: $uri")
        } catch (e: Exception) {
            Log.e("DataManager", "Failed to delete folder: $uri", e)
        }
    }

    fun createCsvFile(deviceAddress: String): Uri? {
        // File Name
        val timestamp = System.currentTimeMillis()
        val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.ENGLISH)
        val fileName = simpleDateFormat.format(timestamp)
        // Creation
        val deviceFolderUri = getDirectoryUri(deviceAddress)
        return try {
            if (deviceFolderUri != null) {
                val fileUri = DocumentsContract.createDocument(
                    context.contentResolver,
                    deviceFolderUri,
                    "text/csv",
                    fileName
                )
                val sharedPreferences = context.getSharedPreferences("DataManager", Context.MODE_PRIVATE)
                // Filename to uri
                sharedPreferences.edit().putString(fileName, fileUri.toString()).apply()
                // Device uri to filename
                val files = deviceFilesList[deviceFolderUri] ?: mutableSetOf()
                files.add(fileName)
                deviceFilesList.remove(deviceFolderUri)
                deviceFilesList[deviceFolderUri] = files
                val fileNames = (sharedPreferences.getStringSet(deviceFolderUri.toString(), emptySet()) ?: emptySet()).toMutableSet()
                fileNames.add(fileName)
                sharedPreferences.edit().putStringSet(deviceFolderUri.toString(), fileNames).apply()

                Log.d("DataManager", "Created a CSV file: $fileUri")
                Toast.makeText(context, "Created a CSV file: $timestamp at $deviceFolderUri", Toast.LENGTH_LONG).show()
                return fileUri
            } else {
                return null
            }
        } catch (e: Exception) {
            Log.e("DataManager", "Failed to create CSV file: $timestamp", e)
            Toast.makeText(context, "\"Failed to create CSV file: $timestamp at $deviceFolderUri", Toast.LENGTH_LONG).show()
            null
        }
    }

    fun getCsvFilesList(deviceUri: Uri): List<String> {
        val sharedPreferences = context.getSharedPreferences("DataManager", Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet(deviceUri.toString(), emptySet())?.toList() ?: emptyList()
    }

    fun getFileUri(fileName: String): Uri {
        val sharedPreferences = context.getSharedPreferences("DataManager", Context.MODE_PRIVATE)
        return Uri.parse(sharedPreferences.getString(fileName, ""))
    }

    fun deleteCsvFile(deviceAddress: String, fileName: String) {
        val fileUri = getFileUri(fileName)
        val deviceUri = getDirectoryUri(deviceAddress)
        val sharedPreferences = context.getSharedPreferences("DataManager", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        //  Remove filenames to file uri
        editor.remove(fileName)
        // Remove filename from device uri set
        if (deviceUri != null) {
            val files = deviceFilesList[deviceUri] ?: mutableSetOf()
            files.remove(fileName)
            deviceFilesList.remove(deviceUri)
            deviceFilesList[deviceUri] = files
            Log.d("DataManager", deviceFilesList[deviceUri].toString())
            sharedPreferences.edit().putStringSet(deviceUri.toString(), files).apply()
        }
        editor.apply()

        try {
            DocumentsContract.deleteDocument(context.applicationContext.contentResolver, fileUri)
            Log.d("DataManager", "Deleted file: $fileUri")
            Toast.makeText(context, "Deleted file: $fileUri", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("DataManager", "Failed to delete file: $fileUri", e)
            Toast.makeText(context, "Failed to delete file: $fileUri", Toast.LENGTH_LONG).show()

        }
    }
}