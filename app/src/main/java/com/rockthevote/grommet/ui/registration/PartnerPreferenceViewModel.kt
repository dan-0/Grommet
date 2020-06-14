package com.rockthevote.grommet.ui.registration

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao
import com.rockthevote.grommet.ui.registration.name.NewRegistrantPartnerData
import com.rockthevote.grommet.ui.registration.personal.AdditionalInfoPartnerData
import com.rockthevote.grommet.util.coroutines.DispatcherProvider
import com.rockthevote.grommet.util.coroutines.DispatcherProviderImpl
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import java.util.*

/**
 * Created by Mechanical Man on 6/14/20.
 */
class PartnerPreferenceViewModel(
        private val dispatchers: DispatcherProvider = DispatcherProviderImpl(),
        private val partnerInfoDao: PartnerInfoDao) : ViewModel() {

    val additionalInfoPartnerData =
            Transformations.map(partnerInfoDao.getCurrentPartnerInfoLive()) {
                AdditionalInfoPartnerData(
                        it.partnerName,
                        it.volunteerText
                )
            }

    val newRegistrantPartnerData = Transformations.map(partnerInfoDao.getCurrentPartnerInfoLive()) {
        NewRegistrantPartnerData(
                it.registrationDeadlineDate,
                it.registrationNotificationText
        )
    }

    /*
        check if registrant will be 18 by the election date.
        Calendar uses 0 as the first month but LocalDate does not, so make sure and add 1 to it
     */
    fun validateBirthDay(birthDate: Date, successCallback: () -> Unit, failCallback: () -> Unit) {
        viewModelScope.launch(dispatchers.io) {
            val registrationDeadline = partnerInfoDao.getCurrentPartnerInfo().registrationDeadlineDate

            val birthCal = Calendar.getInstance()
            birthCal.time = birthDate

            val regCal = Calendar.getInstance()
            regCal.time = registrationDeadline

            val regDate = LocalDate.of(
                    regCal[Calendar.YEAR],
                    regCal[Calendar.MONTH] + 1,
                    regCal[Calendar.DAY_OF_MONTH])

            val birthday = LocalDate.of(
                    birthCal[Calendar.YEAR],
                    birthCal[Calendar.MONTH] + 1,
                    birthCal[Calendar.DAY_OF_MONTH])


            if (ChronoUnit.YEARS.between(birthday, regDate) >= 18) {
                successCallback()
            } else {
                String.format(getString(R.string.birthday_error),
                Dates.formatAsISO8601_ShortDate(registrationDeadline.get())));
                failCallback()
            }
        }
    }
}

class PartnerPreferenceViewModelFactory(
        private val partnerInfoDao: PartnerInfoDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val dispatchers = DispatcherProviderImpl()

        @Suppress("UNCHECKED_CAST")
        return PartnerPreferenceViewModel(dispatchers, partnerInfoDao) as T
    }
}