package it.ibashkimi.lockscheduler.profiles

import android.app.Activity
import it.ibashkimi.lockscheduler.addeditprofile.AddEditProfileActivity
import it.ibashkimi.lockscheduler.model.Profile
import it.ibashkimi.lockscheduler.model.ProfileRepository
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

/**
 * Listens to user actions from the UI ([ProfilesFragment]), retrieves the data and updates the
 * UI as required.
 */
internal class ProfilesPresenter(private val repository: ProfileRepository, private val profilesView: ProfilesContract.View) : ProfilesContract.Presenter {

    override fun start() {
        loadProfiles()
    }

    override fun result(requestCode: Int, resultCode: Int, extras: String?) {
        // If a profile was successfully added, show snackbar
        if (resultCode == Activity.RESULT_CANCELED)
            return
        if (AddEditProfileActivity.REQUEST_ADD_PROFILE == requestCode) {
            profilesView.showSuccessfullySavedMessage()
        } else if (AddEditProfileActivity.REQUEST_EDIT_PROFILE == requestCode) {
            if (extras != null) {
                when (extras) {
                    "deleted" -> profilesView.showSuccessfullyRemovedMessage()
                    "updated" -> profilesView.showSuccessfullyUpdatedMessage()
                    else -> throw RuntimeException("Unhandled case.")
                }
            } else {
                throw RuntimeException("This should not be possible.")
            }
        }
    }

    override fun loadProfiles() {
        if (!profilesView.isActive) {
            return
        }
        launch(CommonPool) {
            val profiles = repository.getAll()
            launch(UI) {
                if (profiles.isEmpty())
                    profilesView.showNoProfiles()
                else
                    profilesView.showProfiles(profiles)

            }
        }
    }

    override fun addNewProfile() {
        profilesView.showAddProfile()
    }

    override fun openProfileDetails(requestedProfile: Profile) {
        profilesView.showProfileDetailsUi(requestedProfile.id)
    }

    override fun deleteProfile(profileId: Long) {

    }

    override fun swapProfiles(profile1: Profile, profile2: Profile) {
        repository.swap(profile1, profile2)
    }

}
