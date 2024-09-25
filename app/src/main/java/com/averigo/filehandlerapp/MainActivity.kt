package com.averigo.filehandlerapp

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    lateinit var btnDownload: Button
    lateinit var btnUpload: Button
    lateinit var file: File
    val instance by lazy {RetrofitInstance.getRetroInstance().create(RetroService::class.java)}
    private var fileName = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        btnDownload = findViewById(R.id.downloadButton)
        btnUpload = findViewById(R.id.uploadButton)

        btnUpload.setOnClickListener {
            val fileContent = "This is the content of my text file"
            fileName = generateFileText()
            val file = File(applicationContext.cacheDir, "$fileName.txt")
            file.writeText(fileContent)
            val requestFile = RequestBody.create("text/plain".toMediaTypeOrNull(), file)
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val call = instance.uploadFile(
                "8a9R7mNcA3eLxuQlVLUtei5SDm7iXGKLG7FSKOEgS94UEexChOaWBDU3Kl3O",
                filePart
            )

            call.enqueue(object : retrofit2.Callback<FileUploadResponse> {
                override fun onResponse(
                    call: Call<FileUploadResponse>,
                    response: Response<FileUploadResponse>
                ) {
                    if (response.isSuccessful) {
                        val res = response.body()
                        Toast.makeText(applicationContext, res!!.message, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(applicationContext, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }

            })
        }

        btnDownload.setOnClickListener {
            val dwCall = instance.downloadFile(
                "8a9R7mNcA3eLxuQlVLUtei5SDm7iXGKLG7FSKOEgS94UEexChOaWBDU3Kl3O",
                "$fileName.txt"
            )
            dwCall.enqueue(object : retrofit2.Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val res = response.body()
                        val documentsPath: String? = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath
                        val dirPath: String = applicationContext.getExternalFilesDir(null)!!.absolutePath
                        val filePath: String = dirPath
                        saveFile(res, "$filePath","$fileName.txt")
                        Log.d("fileNameA", "$filePath/$fileName.txt")

                    } else {
                        Toast.makeText(applicationContext, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }

            })

        }
    }


    fun saveFile(body: ResponseBody?, pathWhereYouWantToSaveFile: String, fileName: String): String {
        // Return an empty string if the body is null
        if (body == null) {
            Log.e("saveFile", "ResponseBody is null")
            return ""
        }

        var input: InputStream? = null
        return try {
            input = body.byteStream()

            // Make sure the file path ends with a separator (e.g. '/')
            val fullFilePath = if (pathWhereYouWantToSaveFile.endsWith("/")) {
                "$pathWhereYouWantToSaveFile$fileName"
            } else {
                "$pathWhereYouWantToSaveFile/$fileName"
            }

            val file = File(fullFilePath)
            // Ensure the parent directories exist
            file.parentFile?.mkdirs()

            FileOutputStream(file).use { output ->
                val buffer = ByteArray(4 * 1024) // Buffer size
                var read: Int
                // Read from the input stream and write to the output stream
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush() // Not strictly necessary with 'use', but good practice
            }
            fullFilePath // Return the full path where the file is saved
        } catch (e: Exception) {
            Log.e("saveFile", "Error saving file: ${e.message}")
            "" // Return an empty string on error
        } finally {
            input?.close() // Close input stream in the finally block
        }
    }


    private fun generateRandomString(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    // Function to format the current date and time in the desired format
    private fun formatDateTime(): String {
        val date = Date()
        val formatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        return formatter.format(date)
    }

    private fun generateFileText(): String {
        val dateTime = formatDateTime()
        val randomString = generateRandomString(4)
        return "TXT_${dateTime}${randomString}"
    }
}