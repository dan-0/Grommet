package com.rockthevote.grommet.ui.registration.name

import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import com.rockthevote.grommet.R
import com.rockthevote.grommet.data.db.model.Prefix
import com.rockthevote.grommet.data.db.model.Suffix
import com.rockthevote.grommet.databinding.ViewNameBinding
import com.rockthevote.grommet.ui.views.NameView

data class PersonName(
    // Required Fields
    val firstName: String,
    val title: Prefix,
    val lastName: String,
    // Optional
    val middleName: String?,
    val suffix: Suffix?
)

@Throws(IllegalArgumentException::class)
fun NameView.toPersonName(): PersonName? {
    val nameBinding = ViewNameBinding.bind(this)

    return with(nameBinding) {
        val title = Prefix.fromString(spinnerTitle.editText.text!!.toString())
        if (title == Prefix.NONE) throw IllegalArgumentException("Title should not be NONE")

        val suffix = Suffix.fromString(spinnerSuffix.editText.text.toString()).let {
            if (it == Suffix.EMPTY) null else it
        }

        PersonName(
            firstName = firstName.text?.toString() ?: return null,
            title = title,
            lastName = lastName.text?.toString() ?: return null,
            middleName = middleName.text?.toString(),
            suffix = suffix
        )
    }
}

fun PersonName.bindToNameView(view: NameView) {
    ViewNameBinding.bind(view)?.let {
        it.firstName.text = firstName.toEditable()
        it.spinnerTitle.setEditText(title.toString())
        it.lastName.text = lastName.toEditable()
        it.middleName.text = middleName?.toEditable()
        it.spinnerSuffix.setEditText(suffix.toString())
    }
}

fun String.toEditable() = Editable.Factory.getInstance().newEditable(this)