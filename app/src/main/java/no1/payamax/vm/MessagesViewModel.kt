package no1.payamax.vm

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import no1.payamax.BuildConfig
import no1.payamax.model.ReviewableProcessedPayamak
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.Charset

class MessagesViewModel(val msgs: List<ReviewableProcessedPayamak>) : ViewModel() {
    fun share(ctx: Context) {
        val exportedItems = msgs.filter { it.selected }.map { it.pp }.toTypedArray()
        if (exportedItems.isEmpty()) {
            return
        }
        val json = Gson().toJson(exportedItems)
        val targetFile = File("${ctx.cacheDir}/messages.json")
        if (targetFile.exists())
            targetFile.delete()
        FileUtils.forceMkdir(targetFile.parentFile)
        FileUtils.write(targetFile, json, Charset.defaultCharset())

        val targetUri = FileProvider.getUriForFile(
            ctx, BuildConfig.APPLICATION_ID + ".files", targetFile
        )

        val sendIntent = Intent().apply {
            action = ACTION_SEND
            type = "application/json"
            putExtra(Intent.EXTRA_SUBJECT, "Sample Payamaks")
            putExtra(Intent.EXTRA_TEXT, "Attached you can find a json file")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("px@no1.ir"))
            putExtra(Intent.EXTRA_TITLE, "Payamaks to review")
            putExtra(Intent.EXTRA_STREAM, targetUri)
        }
        val shareIntent = Intent.createChooser(sendIntent, "email to px@no1.ir or t.me/DrHBN ")
        ctx.startActivity(shareIntent)
    }
}