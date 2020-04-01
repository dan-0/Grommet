package com.rockthevote.grommet.ui.registration.name

import android.text.Editable
import com.rockthevote.grommet.data.db.model.Prefix
import com.rockthevote.grommet.data.db.model.Suffix
import com.rockthevote.grommet.databinding.ViewNameBinding
import com.rockthevote.grommet.ui.views.NameView

data class PersonNameData(
    // Required Fields
    val firstName: String,
    val title: Prefix,
    val lastName: String,
    // Optional
    val middleName: String?,
    val suffix: Suffix?
)

@Throws(IllegalArgumentException::class)
fun NameView.toPersonName(): PersonNameData? {
    val nameBinding = ViewNameBinding.bind(this)

    return with(nameBinding) {
        val title = Prefix.fromString(spinnerTitle.spinnerText!!)
        if (title == Prefix.NONE) return null

        val suffix = spinnerSuffix.editText.text?.toString()?.let {
            Suffix.fromString(it)
        }

        PersonNameData(
            firstName = firstName.text?.toString() ?: return null,
            title = title,
            lastName = lastName.text?.toString() ?: return null,
            middleName = middleName.text?.toString(),
            suffix = suffix
        )
    }
}

fun PersonNameData.bindToNameView(view: NameView) {
    ViewNameBinding.bind(view)?.let {
        it.firstName.text = firstName.toEditable()
        it.spinnerTitle.setEditText(title.toString())
        it.lastName.text = lastName.toEditable()

        it.middleName.text = middleName?.toEditable()
        suffix?.toString()?.let { suffixString -> it.spinnerSuffix.setEditText(suffixString) }
    }
}

fun String.toEditable() = Editable.Factory.getInstance().newEditable(this)