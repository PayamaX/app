package no1.payamax.utils

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File

data class OriginExportData(
    val title: String,
    val number: String,
    val category: String
)

object ExcelExporter {

    fun exportOriginsToCsv(
        context: Context,
        categoryName: String,
        origins: List<OriginExportData>
    ): Result<Uri> {
        return try {
            if (origins.isEmpty()) {
                return Result.failure(Exception("EMPTY"))
            }

            val fileName = "PayamaX_${categoryName}_${System.currentTimeMillis()}.csv"
            val headers = arrayOf("ردیف", "عنوان فرستنده", "شماره فرستنده", "دسته‌بندی")

            val csvBuilder = StringBuilder()
            csvBuilder.append("\uFEFF") // UTF-8 BOM for Microsoft Excel compatibility
            csvBuilder.append(headers.joinToString(",") { "\"$it\"" }).append("\n")

            origins.forEachIndexed { index, item ->
                val row = arrayOf(
                    (index + 1).toString(),
                    item.title.replace("\"", "\"\""),
                    item.number.replace("\"", "\"\""),
                    item.category
                )
                csvBuilder.append(row.joinToString(",") { "\"$it\"" }).append("\n")
            }

            val savedUri = saveToDownloads(context, fileName, csvBuilder.toString())
            if (savedUri != null) {
                Result.success(savedUri)
            } else {
                Result.failure(Exception("SAVE_FAILED"))
            }
        } catch (e: Exception) {
            Log.e("ExcelExporter", "Export error", e)
            Result.failure(e)
        }
    }

    private fun saveToDownloads(context: Context, fileName: String, content: String): Uri? {
        val bytes = content.toByteArray(Charsets.UTF_8)
        var resultUri: Uri? = null

        // 1. Direct write to Public External Downloads directory
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val file = File(downloadsDir, fileName)
            file.writeBytes(bytes)
            resultUri = Uri.fromFile(file)

            // Trigger MediaScanner so File Managers scan and list the file instantly
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf("text/csv")
            ) { _, _ -> }
        } catch (e: Exception) {
            Log.e("ExcelExporter", "Direct file write failed", e)
        }

        // 2. MediaStore insert with IS_PENDING handling for Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val mediaStoreUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (mediaStoreUri != null) {
                    resolver.openOutputStream(mediaStoreUri)?.use { os ->
                        os.write(bytes)
                        os.flush()
                    }
                    values.clear()
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(mediaStoreUri, values, null, null)
                    if (resultUri == null) {
                        resultUri = mediaStoreUri
                    }
                }
            } catch (e: Exception) {
                Log.e("ExcelExporter", "MediaStore save failed", e)
            }
        }

        return resultUri
    }
}
