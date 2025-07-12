package com.app.core

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object ShareUtils {

    fun shareData(context: Context, fileName: String, mimeType: String, content: String) {
        try {
            val cachePath = File(context.cacheDir, "exports/")
            cachePath.mkdirs() // Create the directory if it doesn't exist

            val file = File(cachePath, fileName)
            file.writeText(content)

            val authority = "${context.packageName}.fileprovider"
            val fileUri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Export Data")
            context.startActivity(chooserIntent)

        } catch (e: Exception) {
            // Handle exceptions, e.g., show a toast message
            e.printStackTrace()
        }
    }
}