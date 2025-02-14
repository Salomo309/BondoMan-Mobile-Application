package com.example.bondoman.ui.setting

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bondoman.service.TokenExpirationService
import com.example.bondoman.storage.TokenManager
import com.example.bondoman.room.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SettingViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context: Context = application.applicationContext
    private var isRandomizationEnabled = false

    fun changeRandomizationStatus() {
        isRandomizationEnabled = !isRandomizationEnabled
    }

    fun getIsRandomizationEnabled() : Boolean {
        return isRandomizationEnabled
    }

    fun logout(){
        // Remove all shared prefs
        TokenManager.removeSharedPrefs(context)

        // Stop all services
        context.stopService(Intent(context, TokenExpirationService::class.java))
    }

    fun saveTransactionsToFile(activity: Activity, transactionData: List<TransactionEntity>?, extension: String) {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            val permissionRequestCode = 1001
            ActivityCompat.requestPermissions(activity, arrayOf(permission), permissionRequestCode)
        } else {
            // Permission has already been granted, proceed with file saving
            val workbook = generateExcelFile(transactionData)
            saveExcelFileToDownloadDirectory(workbook, extension)
        }
    }

    fun sendEmailWithAttachment(activity: Activity, transactionData: List<TransactionEntity>?, extension: String) {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            val permissionRequestCode = 1001
            ActivityCompat.requestPermissions(activity, arrayOf(permission), permissionRequestCode)
        } else {
            // Permission has already been granted, proceed with file saving
            val workbook = generateExcelFile(transactionData)
            sendEmail(activity, workbook, extension)
        }
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun generateExcelFile(transactionData: List<TransactionEntity>?): XSSFWorkbook {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Transactions")
        val headerRow = sheet.createRow(0)

        // Create a font style for bold
        val boldFont = workbook.createFont()
        boldFont.bold = true
        val boldStyle = workbook.createCellStyle()
        boldStyle.setFont(boldFont)

        // Set bold style for header cells
        val headerCellStyle = sheet.workbook.createCellStyle()
        headerCellStyle.setFont(boldFont)

        headerRow.createCell(0).apply {
            setCellValue("ID")
            cellStyle = boldStyle
        }
        headerRow.createCell(1).apply {
            setCellValue("Title")
            cellStyle = boldStyle
        }
        headerRow.createCell(2).apply {
            setCellValue("Category")
            cellStyle = boldStyle
        }
        headerRow.createCell(3).apply {
            setCellValue("Amount")
            cellStyle = boldStyle
        }
        headerRow.createCell(4).apply {
            setCellValue("Location")
            cellStyle = boldStyle
        }
        headerRow.createCell(5).apply {
            setCellValue("Longitude")
            cellStyle = boldStyle
        }
        headerRow.createCell(6).apply {
            setCellValue("Latitude")
            cellStyle = boldStyle
        }
        headerRow.createCell(7).apply {
            setCellValue("Date")
            cellStyle = boldStyle
        }

        transactionData?.forEachIndexed { index, transaction ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(transaction.id.toDouble())
            row.createCell(1).setCellValue(transaction.title)
            row.createCell(2).setCellValue(transaction.category)
            row.createCell(3).setCellValue(transaction.amount)
            row.createCell(4).setCellValue(transaction.location)
            row.createCell(5).setCellValue(transaction.longitude)
            row.createCell(6).setCellValue(transaction.latitude)
            row.createCell(7).setCellValue(
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(transaction.date)
            )
        }

        return workbook
    }

    private fun saveExcelFileToDownloadDirectory(workbook: XSSFWorkbook, extension: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileName = createFileName(extension)
            var fos: FileOutputStream? = null
            fos.use {
                try {
                    val path =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            .toString()
                    val file = File(path, fileName)
                    fos = FileOutputStream(file)
                    workbook.write(fos)
                    showToast("Excel Sheet Generated: ${file.path}")
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun createFileName(extension: String): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val currentTimeStamp = dateFormat.format(Date())
        return "Transactions_$currentTimeStamp.$extension"
    }

    private fun sendEmail(activity: Activity, workbook: XSSFWorkbook, extension: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Save the workbook to a temporary file
            val fileName = createFileName(extension)
            val tempFile = File(activity.cacheDir, fileName)
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(tempFile)
                workbook.write(fos)
            } catch (e: IOException) {
                e.printStackTrace()
                showToast("Failed to create temporary file")
                return@launch
            } finally {
                fos?.close()
            }

            // Grant URI permission
            val authority = "${activity.packageName}.provider"
            val uri = FileProvider.getUriForFile(activity, authority, tempFile)
            val nim = TokenManager.getNIM(activity)
            activity.grantUriPermission(activity.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

            // Create and send email intent with the attachment
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.type = "message/rfc822"

            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("${nim}@std.stei.itb.ac.id"))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Your List of Transactions")
            emailIntent.putExtra(Intent.EXTRA_TEXT, "This is all of your transactions. Thanks")
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri)

            val intentChooser = Intent.createChooser(emailIntent, "Send mail...")
            try {
                activity.startActivity(intentChooser)
            } catch (ex: Exception) {
                ex.printStackTrace()
                showToast("There is no email client installed.")
            }
        }
    }
}
