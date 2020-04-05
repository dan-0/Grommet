package com.rockthevote.grommet.ui.registration.address

import com.rockthevote.grommet.databinding.FragmentPersonalInfoBinding
import com.rockthevote.grommet.databinding.ViewAddressBinding
import com.rockthevote.grommet.ui.registration.name.toEditable
import com.rockthevote.grommet.ui.views.AddressView

fun FragmentPersonalInfoBinding.toAddressData(): PersonalInfoData {
    val homeAddress = homeAddress.toAddress()!!
    val isMailingAddressDifferent = mailingAddressIsDifferent.isChecked
    val hasPreviousAddressChanged = addressChanged.isChecked

    val mailingAddress = if (isMailingAddressDifferent) mailingAddress.toAddress() else null

    val previousAddress = if (hasPreviousAddressChanged) previousAddress.toAddress() else null

    return PersonalInfoData(
        homeAddress,
        isMailingAddressDifferent,
        hasPreviousAddressChanged,
        mailingAddress,
        previousAddress
    )
}

fun PersonalInfoData.toFragmentPersonalInfoBinding(binding: FragmentPersonalInfoBinding) {
    homeAddress.bindToAddressView(binding.homeAddress)
    binding.mailingAddressIsDifferent.isChecked = isMailingAddressDifferent
    binding.addressChanged.isChecked = hasPreviousAddress

    mailingAddress?.bindToAddressView(binding.mailingAddress)
    previousAddress?.bindToAddressView(binding.previousAddress)
}

fun AddressView.toAddress(): AddressData? {
    val addressBinding = ViewAddressBinding.bind(this)

    with (addressBinding) {
        val streetAddress = street.text?.toString() ?: return null
        val city = city.text?.toString() ?: return null
        val state = spinnerState.spinnerText ?: return null
        val zipCode = zip.text?.toString() ?: return null
        val county = spinnerCounty.spinnerText ?: return null

        val streetAddressTwo = street2.text?.toString()
        val unitType = spinnerUnitType.spinnerText
        val unitNumber = unit.text?.toString()

        return AddressData(
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

fun AddressData.bindToAddressView(view: AddressView) {
    val binding = ViewAddressBinding.bind(view)
    with (binding) {
        street.text = streetAddress.toEditable()
        city.text = this@bindToAddressView.city.toEditable()
        spinnerState.setEditText(state)
        zip.text = zipCode.toEditable()

        spinnerCounty.setEditText(county ?: "")
        street2.text = streetAddressTwo?.toEditable()
        unitType?.let { spinnerUnitType.setEditText(it) }
        unit.text = unitNumber?.toEditable()
    }
}