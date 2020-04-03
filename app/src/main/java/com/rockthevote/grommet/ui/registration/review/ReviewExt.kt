package com.rockthevote.grommet.ui.registration.review

import android.graphics.Bitmap
import com.rockthevote.grommet.databinding.FragmentReviewAndConfirmBinding
import com.rockthevote.grommet.util.Images
import java.io.ByteArrayOutputStream

fun FragmentReviewAndConfirmBinding.toReviewData(): ReviewData {
    val hasReviewAndConfirmInfo = checkboxAgreement.isChecked

    val signature = ByteArrayOutputStream().use { baos ->
        val image = Images.transformAspectRatio(signaturePad.signatureBitmap, 3, 1).let {
            Images.aspectSafeScale(it, 180, 60)
        }

        image.compress(Bitmap.CompressFormat.PNG, 100, baos)

        baos.toByteArray()
    }

    return ReviewData(
        hasReviewAndConfirmInfo,
        signature
    )
}