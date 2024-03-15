package com.example.barcodescanner.testImageAnalyzers

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer

public class QrCodeAnalyzer(
    private val onQrCodeDetected: (String) -> Unit
): ImageAnalysis.Analyzer {

    private val supportedImageFormat = listOf(
        ImageFormat.YUV_420_888,
        ImageFormat.YUV_422_888,
        ImageFormat.YUV_444_888
    )

    override fun analyze(image: ImageProxy) {
        if(image.format in supportedImageFormat){
            val buffer = image.planes[0].buffer
            val data = ByteArray(buffer.capacity()).also { buffer.get(it) }

            val source = PlanarYUVLuminanceSource(
                data,
                image.width,
                image.height,
                0,
                0,
                image.width,
                image.height,
                false
            )

            var binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            try {
                val result = MultiFormatReader().apply {
                    setHints(
                        mapOf(
                           DecodeHintType.POSSIBLE_FORMATS to listOf(
                               com.google.zxing.BarcodeFormat.CODE_128
                           )
                        )
                    )
                }.decode(binaryBitmap)
                onQrCodeDetected(result.text)
            } catch (e: Exception){
                e.printStackTrace()
            } finally {
                image.close()
            }
        }
    }

}