package com.example.barcodescanner

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.common.moduleinstall.InstallStatusListener
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallClient
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate.InstallState.STATE_CANCELED
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate.InstallState.STATE_COMPLETED
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate.InstallState.STATE_FAILED
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning


class ScannerActivityGoogleServices : ComponentActivity() {


    private var moduleInstallClient: ModuleInstallClient? = null
    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val barcodeOptions = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_CODE_128
            )
            .enableAutoZoom()
            .allowManualInput()
            .build()
        val scanner = GmsBarcodeScanning.getClient(this, barcodeOptions)
        moduleInstallClient = ModuleInstall.getClient(this)
        progressBar = ProgressBar(this)

        setContent {
            val context = LocalContext.current
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center

            ) {
                Button(onClick = {
                    moduleInstallClient!!
                        .areModulesAvailable(scanner)
                        .addOnSuccessListener {
                            if (it.areModulesAvailable()) {
                                scanner.startScan()
                                    .addOnSuccessListener { barcode ->
                                        val rawValue = barcode.rawValue
                                        Toast.makeText(context, rawValue, Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Failed to scan",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }.addOnCompleteListener {
                                    }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Modules not available. Downloading...",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                                val moduleInstallRequest = ModuleInstallRequest.newBuilder()
                                    .addApi(scanner)
                                    .setListener(listener)
                                    .build()
                                moduleInstallClient!!.installModules(moduleInstallRequest)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Module downloaded",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Can't download the module. Try again...",
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                    }
                                progressBar!!.visibility = ProgressBar.VISIBLE
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Can't verify the module. Try again...",
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }

                }) {
                    Text("Scan Barcodes")
                }
            }

        }
    }

    inner class ModuleInstallProgressListener : InstallStatusListener {
        override fun onInstallStatusUpdated(update: ModuleInstallStatusUpdate) {
            // Progress info is only set when modules are in the progress of downloading.
            update.progressInfo?.let {
                val progress = (it.bytesDownloaded * 100 / it.totalBytesToDownload).toInt()
                // Set the progress for the progress bar.
                progressBar!!.progress = progress
            }

            if (isTerminateState(update.installState)) {
                moduleInstallClient!!.unregisterListener(this)
            }
        }

        private fun isTerminateState(@ModuleInstallStatusUpdate.InstallState state: Int): Boolean {
            progressBar!!.visibility = ProgressBar.GONE
            return state == STATE_CANCELED || state == STATE_COMPLETED || state == STATE_FAILED
        }
    }

    private val listener = ModuleInstallProgressListener()


}


