package mk.com.ukim.finki.mpip.instanim.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mk.com.ukim.finki.mpip.instanim.repository.AuthRepository
import mk.com.ukim.finki.mpip.instanim.repository.StorageRepository
import mk.com.ukim.finki.mpip.instanim.repository.UserRepository
import mk.com.ukim.finki.mpip.instanim.ui.auth.AuthViewModel

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val storageRepository: StorageRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AuthViewModel(authRepository, userRepository, storageRepository) as T
    }
}
