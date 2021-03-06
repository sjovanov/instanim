package mk.com.ukim.finki.mpip.instanim.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import mk.com.ukim.finki.mpip.instanim.data.entity.Post
import mk.com.ukim.finki.mpip.instanim.data.entity.User
import mk.com.ukim.finki.mpip.instanim.data.model.Resource
import mk.com.ukim.finki.mpip.instanim.repository.AuthRepository
import mk.com.ukim.finki.mpip.instanim.repository.PostRepository
import mk.com.ukim.finki.mpip.instanim.repository.UserRepository

class ProfileViewModel(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _profilePosts = MutableLiveData<Resource<List<Post>>>()
    val profilePosts: LiveData<Resource<List<Post>>>
        get() = _profilePosts

    private val _profile = MutableLiveData<Resource<User>>()
    val profile: LiveData<Resource<User>>
        get() = _profile

    private val _updateProfile = MutableLiveData<Resource<Unit>>()
    val updateProfile: LiveData<Resource<Unit>>
        get() = _updateProfile

    private val _updateProfileFollowed = MutableLiveData<Resource<Unit>>()
    val updateProfileFollowed: LiveData<Resource<Unit>>
        get() = _updateProfileFollowed

    fun fetchProfileData(uid: String?) {

        uid?.let {
            this.fetchAppUserData(it)
        } ?: run {
            this.fetchCurrentUserData()
        }
    }

    private fun fetchCurrentUserData() {
        val mUser = authRepository.currentUser()
        val uid = mUser.data!!.uid
        fetchAppUserData(uid)
    }

    private fun fetchAppUserData(uid: String) {
        _profile.value = Resource.loading(null)
        _profilePosts.value = Resource.loading(null)
//        Log.d("SOME", "fetchAppUserData: $uid")
        viewModelScope.launch(IO) {
            _profile.postValue(
                userRepository.getUser(uid)
            )
            _profilePosts.postValue(
                postRepository.getPosts(uid)
            )
        }
    }

    fun follow(followerUser: String, followedUser: String) {
        _updateProfile.value = Resource.loading(null)
        _updateProfileFollowed.value = Resource.loading(null)
        viewModelScope.launch(IO) {
            val follower = userRepository.getUser(followerUser)
            val followed = userRepository.getUser(followedUser)
            if (follower.data?.follows?.contains(followedUser) == true){
                follower.data.follows.remove(followedUser)
            } else {
                follower.data?.follows?.add(followedUser)
            }
            if (followed.data?.followedBy?.contains(followerUser) == true){
                followed.data.followedBy.remove(followerUser)
            } else {
                followed.data?.followedBy?.add(followerUser)
            }
            _updateProfile.postValue(
                followed.data?.let { userRepository.updateUser(it) }
            )
            _updateProfileFollowed.postValue(
                follower.data?.let { userRepository.updateUser(it) }
            )
        }
    }
}
