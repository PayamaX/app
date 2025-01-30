package no1.payamax

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageInfo
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.ContactsContract
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.database.getStringOrNull
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import no1.payamax.composables.MessagesComposable
import no1.payamax.contracts.CellNumber
import no1.payamax.contracts.Contact
import no1.payamax.contracts.Origin
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.Usability
import no1.payamax.contracts.UsabilityClass
import no1.payamax.contracts.UsabilityRate
import no1.payamax.model.ProcessResult
import no1.payamax.model.ProcessedPayamakModel
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.services.PayamakColumns
import no1.payamax.services.UsabilityProcessorObject
import no1.payamax.ui.theme.PayamaXTheme
import no1.payamax.vm.MessagesViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        setContent {
            PayamaXTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            CheckPayamakPermission {
                                val inboxUri = "content://sms/inbox"
                                val uri = Uri.parse(inboxUri)
                                (contentResolver.query(uri, null, "", null, "date desc")
                                    ?: throw RuntimeException("")).use { cursor ->
                                    Payamaks(cursor, contentResolver)
                                }
                            }
                        }
                        Text(text = packageInfo(LocalContext.current))
                    }
                }
            }
        }
    }

    private fun packageInfo(context: Context): String {
        val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION") pInfo.versionCode
        }
        return "${pInfo.packageName}:${versionCode}:${pInfo.versionName}"
    }
}

@Composable
fun Payamaks(cursor: Cursor, cr: ContentResolver) {

    val messages = mutableListOf<ReviewableProcessedPayamak>()
    var index = 0

    if (cursor.moveToFirst()) {
        val payamakColumns = PayamakColumns(cursor)
        LazyColumn(modifier = Modifier.padding(5.dp)) {
            do {
                val addressValue = cursor.getString(payamakColumns.addressIndex)
                val address = addressValue.toLongOrNull()?.let { CellNumber.parse(it) }
                val addressTitle = if (address == null) addressValue else null
                val origin =
                    Origin(address, addressTitle, address?.let { contact(addressValue, cr) })
                val payamak = Payamak(0L, origin, cursor.getString(payamakColumns.bodyIndex))

                messages.add(
                    ReviewableProcessedPayamak(
                        ProcessedPayamakModel(
                            cursor.getLong(payamakColumns.idIndex),
                            payamak,
                            UsabilityProcessorObject.detect(payamak)
                        ), false
                    )
                )
            } while (cursor.moveToNext() && index++ < 99)
        }
    }
    if (BuildConfig.DEBUG && messages.isEmpty()) {
        messages.add(
            ReviewableProcessedPayamak(
                ProcessedPayamakModel(
                    0,
                    Payamak(
                        0, Origin(
                            CellNumber(
                                98, 9123456789
                            ),
                            null,
                            null,
                        ), "Sample"
                    ),
                    Usability(
                        UsabilityClass.Usable,
                        UsabilityRate(0.9),
                        listOf(ProcessResult("Sample", false, null, ""))
                    ),
                    UsabilityClass.Usable,
                ), false
            )
        )
    }
    if (messages.isEmpty()) {
        Text(text = "Empty")
    } else {
        MessagesComposable(
            viewModel = MessagesViewModel(
                messages.filter { it.pp.usability.clazz != UsabilityClass.Spam && it.pp.usability.clazz != UsabilityClass.Unknown   }
            )
        )
    }
}

fun contact(addressNumber: String, cr: ContentResolver): Contact? {
    val contactsUri = Uri.withAppendedPath(
        ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(addressNumber)
    )
    cr.query(
        contactsUri,
        arrayOf(BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME),
        null,
        null,
        null
    )?.use {
        //return Contact(it.count.toLong(), it.count.toString())
        if (it.moveToFirst()) {
            val dump = dump(it)
            Log.i("dump", dump)
            return Contact(it.getLong(0), it.getString(1))
        }
    }
    return null
}

fun dump(it: Cursor): String {
    val output = StringBuffer()
    for (col in it.columnNames.withIndex()) {
        val index = col.index
        val name = col.value
        val type = it.getType(col.index)
        val typeName = typeName(type)
        val value = it.getStringOrNull(col.index)
        output.append("$index\t$name\t$typeName\t$value\n")
    }
    return output.toString()
}

fun typeName(type: Int): String {
    return when (type) {
        Cursor.FIELD_TYPE_BLOB -> "BLOB"
        Cursor.FIELD_TYPE_FLOAT -> "FLOAT"
        Cursor.FIELD_TYPE_INTEGER -> "INT"
        Cursor.FIELD_TYPE_NULL -> "NULL"
        Cursor.FIELD_TYPE_STRING -> "TEXT"
        else -> "ELSE"
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CheckPayamakPermission(content: @Composable () -> Unit) {
    val permissionState = rememberMultiplePermissionsState(
        listOf(android.Manifest.permission.READ_SMS, android.Manifest.permission.READ_CONTACTS)
    )

    for (ps in permissionState.permissions) {
        if (!ps.status.isGranted) {
            when (ps.permission) {
                android.Manifest.permission.READ_SMS -> ReadPayamakPermissionMessage(ps)
                android.Manifest.permission.READ_CONTACTS -> ReadContactsPermissionMessage(ps)
            }
            return
        }
    }
    content()
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ReadContactsPermissionMessage(ps: PermissionState) {
    Column {
        val textToShow = if (ps.status.shouldShowRationale) {
            // If the user has denied the permission but the rationale can be shown,
            // then gently explain why the app requires this permission
            "Reading contacts permission is required"
        } else {
            // If it's the first time the user lands on this feature, or the user
            // doesn't want to be asked again for this permission, explain that the
            // permission is required
            "Reading contacts permission is required to detect if sender already saved in your contacts or not"
        }
        Text(textToShow)
        Button(onClick = { ps.launchPermissionRequest() }) {
            Text("Request for contacts permission")
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ReadPayamakPermissionMessage(ps: PermissionState) {
    Column {
        val textToShow = if (ps.status.shouldShowRationale) {
            // If the user has denied the permission but the rationale can be shown,
            // then gently explain why the app requires this permission
            "مجوز دریافت پیامک‌ها لازم است"
        } else {
            // If it's the first time the user lands on this feature, or the user
            // doesn't want to be asked again for this permission, explain that the
            // permission is required
            "برای بررسی و تشخیص هرزپیامک‌ها، دسترسی به پیامک‌ها لازم است. "
        }
        Text(textToShow)
        Button(onClick = { ps.launchPermissionRequest() }) {
            Text("درخواست محوز دریافت پیامک‌ها")
        }
    }
}
