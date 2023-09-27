package dev.vcreations.scanx.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import java.io.IOException

class CommonUtils {
    companion object{
        /**
         * takes URI of the image and returns bitmap
         */
        fun uriToBitmap(selectedFileUri: Uri?,contentResolver : ContentResolver): Bitmap? {
            try {
                val parcelFileDescriptor = contentResolver.openFileDescriptor(selectedFileUri!!, "r")
                val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
                val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                parcelFileDescriptor.close()
                return image
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        /**
         * rotate image if image captured on samsung devices
         * Most phone cameras are landscape, meaning if you take the photo in portrait, the resulting photos will be rotated 90 degrees.
         */
        @SuppressLint("Range")
        fun rotateBitmap(input: Bitmap?, imageUri : Uri?,contentResolver : ContentResolver): Bitmap {
            val orientationColumn =
                arrayOf(MediaStore.Images.Media.ORIENTATION)
            val cur = contentResolver.query(imageUri!!, orientationColumn, null, null, null)
            var orientation = -1f
            if (cur != null && cur.moveToFirst()) {
                orientation = cur.getFloat(cur.getColumnIndex(orientationColumn[0]))
            }
            Log.d("tryOrientation", orientation.toString() + "")
            val rotationMatrix = Matrix()
            rotationMatrix.setRotate(orientation)
            return Bitmap.createBitmap(
                input!!,
                0,
                0,
                input.width,
                input.height,
                rotationMatrix,
                true
            )
        }

        fun findWordStart(text: String, offset: Int): Int {
            var start = offset
            while (start > 0 && !Character.isWhitespace(text[start - 1])) {
                start--
            }
            return start
        }

        fun findWordEnd(text: String, offset: Int): Int {
            var end = offset
            while (end < text.length && !Character.isWhitespace(text[end])) {
                end++
            }
            return end
        }
    }
}