package com.rockthevote.grommet.ui.registration.name

import com.rockthevote.grommet.data.db.model.Name

data class PersonName(
    // Required Fields
    val firstName: String,
    val title: Name.Prefix,
    val lastName: String,
    // Optional
    val middleName: String?,
    val suffix: Name.Suffix?
)