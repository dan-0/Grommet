package com.rockthevote.grommet.util

object ValidationRegex {
    const val PHONE = "/[ [:punct:]]*\\d{3}[ [:punct:]]*\\d{3}[ [:punct:]]*\\d{4}\\D*/"
    const val EMAIL = " /\\A[A-Z0-9_.&%+\\-‘]+@(?:[A-Z0-9\\-]+\\.)+(?:[A-Z]{2,25})\\z/ix"
    const val ZIP = "/\\A\\d{5}(-\\d{4})?\\z/"
    const val CITY = "/\\A[a-zA-Z0-9#\\-\\s’\\.]*\\z/"
    const val ADDRESS = "/\\A[a-zA-Z0-9#\\-\\s,\\/\\.]*\\z/"
    const val NAME = "/\\A[^\\u{1F600}-\\u{1F6FF}]*\\z/"
}

object PennValidations {
    const val UNIT_MAX_CHARS = 15
    const val DRIVERS_LICENSE_CHARS = 8
    const val SSN_LAST_4_CHARS = 4
}