package com.rockthevote.grommet.ui.registration.assistance

import com.rockthevote.grommet.databinding.FragmentAssistantInfoBinding
import com.rockthevote.grommet.ui.registration.address.bindToAddressView
import com.rockthevote.grommet.ui.registration.address.toAddress
import com.rockthevote.grommet.ui.registration.name.bindToNameView
import com.rockthevote.grommet.ui.registration.name.toEditable
import com.rockthevote.grommet.ui.registration.name.toPersonName

fun FragmentAssistantInfoBinding.toAssistanceData(): AssistanceData {
    val hasSomeoneAssisted = checkboxHasAssistant.isChecked

    val helperName = assistantName.toPersonName()
    val helperAddress = assistantAddress.toAddress()
    val helperPhone = assistantPhone.text?.toString()
    val hasConfirmedTerms = checkboxAssistantAffirmation.isChecked

    return AssistanceData(
        hasSomeoneAssisted,
        helperName,
        helperAddress,
        helperPhone,
        hasConfirmedTerms
    )
}

fun AssistanceData.toFragmentAssistantInfoBinding(binding: FragmentAssistantInfoBinding) {
    binding.checkboxHasAssistant.isChecked = hasSomeoneAssisted

    helperName?.bindToNameView(binding.assistantName)
    helperAddress?.bindToAddressView(binding.assistantAddress)
    binding.assistantPhone.text = helperPhone?.toEditable()
    binding.checkboxAssistantAffirmation.isChecked = hasConfirmedTerms
}