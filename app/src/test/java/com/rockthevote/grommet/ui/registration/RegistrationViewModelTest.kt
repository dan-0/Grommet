package com.rockthevote.grommet.ui.registration

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.rockthevote.grommet.data.db.dao.RegistrationDao
import com.rockthevote.grommet.data.db.model.GeoLocation
import com.rockthevote.grommet.data.db.model.Registration
import com.rockthevote.grommet.testdata.Fake
import com.rockthevote.grommet.ui.registration.review.ReviewAndConfirmState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RegistrationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val fakeSessionData = SessionData(
        1,
        "temp",
        "temp",
        "temp",
        GeoLocation(1.0, 1.0),
        "temp"
    )

    private lateinit var registrationDao: RegistrationDao

    private lateinit var ut: RegistrationViewModel

    @Before
    fun setUp() {
        registrationDao = FakeRegistrationDao()
        ut = RegistrationViewModel(fakeSessionData, registrationDao)

        // reviewAndConfirmState is backed by a MediatorLiveData, which doesn't produce a value if not observed
        ut.reviewAndConfirmState.observeForever {  }
    }

    @Test
    fun `registrationData init state is empty`() {
        assertNull(currentRegistrationData.newRegistrantData)
        assertNull(currentRegistrationData.addressData)
        assertNull(currentRegistrationData.additionalInfoData)
        assertNull(currentRegistrationData.assistanceData)
        assertNull(currentRegistrationData.reviewData)
    }

    @Test
    fun `registrationState initializes in init state`() {
        assertEquals(RegistrationState.Init, currentRegistrationState)
    }

    @Test
    fun `reviewAndConfirmState initializes with empty data`() {
        val expected = Fake.EMPTY_REVIEW_AND_CONFIRM_STATE_DATA

        val state = currentReviewAndConfirmState as ReviewAndConfirmState.Content

        assertEquals(expected, state.data)
    }

    @Test
    fun `storeNewRegistrantData adds to main data state`() {
        val fakeNewRegistrantData = Fake.NEW_REGISTRANT_DATA

        ut.storeNewRegistrantData(fakeNewRegistrantData)

        assertEquals(fakeNewRegistrantData, currentRegistrationData.newRegistrantData)
    }

    @Test
    fun `storeAddressData adds to main data state`() {
        val fakeAddressData = Fake.PERSONAL_INFO_DATA

        ut.storeAddressData(fakeAddressData)

        assertEquals(fakeAddressData, currentRegistrationData.addressData)
    }

    @Test
    fun `storeAdditionalInfoData adds to main data state`() {
        val fakeAdditionalInfo = Fake.ADDITIONAL_INFO_DATA

        ut.storeAdditionalInfoData(fakeAdditionalInfo)

        assertEquals(fakeAdditionalInfo, currentRegistrationData.additionalInfoData)
    }

    @Test
    fun `storeAssistanceData adds to main data state`() {
        val fakeAssistanceData = Fake.ASSISTANCE_DATA

        ut.storeAssistanceData(fakeAssistanceData)

        assertEquals(fakeAssistanceData, currentRegistrationData.assistanceData)
    }

    // TODO completeRegistration paths

    private val currentRegistrationData
        get() = ut.registrationData.value!!

    private val currentRegistrationState
        get() = ut.registrationState.value!!

    private val currentReviewAndConfirmState
        get() = ut.reviewAndConfirmState.value!!
}

class FakeRegistrationDao : RegistrationDao {

    override fun getAll(): List<Registration> {
        TODO("Not yet implemented")
    }

    override fun insert(vararg registration: Registration) {
        TODO("Not yet implemented")
    }

    override fun delete(registration: Registration) {
        TODO("Not yet implemented")
    }
}