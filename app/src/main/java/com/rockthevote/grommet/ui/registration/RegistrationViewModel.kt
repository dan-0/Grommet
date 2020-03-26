package com.rockthevote.grommet.ui.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rockthevote.grommet.ui.registration.name.NewRegistrantData

class RegistrationViewModel : ViewModel() {
    private val _registrationData = MutableLiveData(RegistrationData())
    val registrationData: LiveData<RegistrationData> = _registrationData

    fun storeNewRegistrantData(data: NewRegistrantData) {
        val newData = _registrationData.value?.copy(
            newRegistrantData = data
        )

        _registrationData.postValue(newData)
    }
}

