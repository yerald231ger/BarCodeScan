package com.example.barcodescanner

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.barcodescanner.ui.theme.BarCodeScannerTheme
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC
            )
            .build()
        val scanner = GmsBarcodeScanning.getClient(this)

        setContent {
            BarCodeScannerTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center

                    ) {
                        Text(text = "Ready to scan...?")
                        Button(
                            onClick = {
                                scanner.startScan()
                                    .addOnSuccessListener {
                                        val rawValue: String? = it.rawValue
                                        Log.d("MainActivity", "onCreate: $rawValue")
                                    }
                                    .addOnFailureListener {
                                        Log.d("MainActivity", "onCreate: ${it.message}")
                                    }
                                    .addOnCanceledListener {
                                        Log.d("MainActivity", "onCreate: onCancel")
                                    }.addOnCompleteListener {
                                        Log.d("MainActivity", "onCreate: onComplete")
                                    }
                            }) {
                            Text(text = "Scan")
                        }
                    }
                }
            }
        }
    }

}
