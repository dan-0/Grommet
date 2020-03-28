package com.rockthevote.grommet.ui.registration.address

import com.rockthevote.grommet.databinding.FragmentPersonalInfoBinding
import com.rockthevote.grommet.databinding.ViewAddressBinding
import com.rockthevote.grommet.ui.registration.name.toEditable
import com.rockthevote.grommet.ui.views.AddressView

fun FragmentPersonalInfoBinding.toAddressData(): PersonalInfoData {
    val homeAddress = homeAddress.toAddress()!!
    val isMailingAddressDifferent = mailingAddressIsDifferent.isChecked
    val hasPreviousAddressChanged = addressChanged.isChecked

    val mailingAddress = mailingAddress.toAddress()
    val previousAddress = previousAddress.toAddress()

    return PersonalInfoData(
        homeAddress,
        isMailingAddressDifferent,
        hasPreviousAddressChanged,
        mailingAddress,
        previousAddress
    )
}

fun PersonalInfoData.toFragmentPersonalInfoBinding(binding: FragmentPersonalInfoBinding) {
    homeAddress.toAddressView(binding.homeAddress)
    binding.mailingAddressIsDifferent.isChecked = isMailingAddressDifferent
    binding.addressChanged.isChecked = hasPreviousAddress

    mailingAddress?.toAddressView(binding.mailingAddress)
    previousAddress?.toAddressView(binding.previousAddress)
}

fun AddressView.toAddress(): Address? {
    val addressBinding = ViewAddressBinding.bind(this)

    with (addressBinding) {
        val streetAddress = street.text?.toString() ?: return null
        val city = city.text?.toString() ?: return null
        val state = spinnerState.editText.text?.toString() ?: return null
        val zipCode = zip.text?.toString() ?: return null
        val county = spinnerCounty.editText.text?.toString() ?: return null

        val streetAddressTwo = street2.text?.toString()
        val unitType = spinnerUnitType.editText.text?.toString()
        val unitNumber = unit.text?.toString()

        return Address(
            streetAddress,
            city,
            state,
            zipCode,
            county,
            streetAddressTwo,
            unitType,
            unitNumber
        )
    }
}

fun Address.toAddressView(view: AddressView) {
    val binding = ViewAddressBinding.bind(view)
    with (binding) {
        street.text = streetAddress.toEditable()
        city.text = this@toAddressView.city.toEditable()
        spinnerState.setEditText(state)
        zip.text = zipCode.toEditable()
        spinnerCounty.setEditText(county)

        street2.text = streetAddressTwo?.toEditable()
        unitType?.let { spinnerUnitType.setEditText(it) }
        unit.text = unitNumber?.toEditable()
    }
}