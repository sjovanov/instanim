package mk.com.ukim.finki.mpip.instanim.ui.profiles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import mk.com.ukim.finki.mpip.instanim.adapter.ProfileListAdapter
import mk.com.ukim.finki.mpip.instanim.data.entity.User
import mk.com.ukim.finki.mpip.instanim.data.model.Status
import mk.com.ukim.finki.mpip.instanim.databinding.FragmentProfileListBinding
import mk.com.ukim.finki.mpip.instanim.util.FactoryInjector

class ProfileListFragment : Fragment() {

    private var _binding: FragmentProfileListBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var profileListAdapter: ProfileListAdapter
    private val profileListViewModel: ProfileListViewModel by viewModels {
        FactoryInjector.getProfileListViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        profileListViewModel.fetchProfiles(null)
        profileListViewModel.profiles.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.ERROR -> {
                    // do nothing
                }
                Status.LOADING -> {
                    // do nothing
                }
                Status.SUCCESS -> {
                    it.data?.let { users ->
                        updateAdapterData(users)
                    }
                }
            }
        })
    }

    private fun initRecycler() {
        profileListAdapter = ProfileListAdapter {
            navigateToProfileDetails(it)
        }

        val llm = LinearLayoutManager(context)

        binding.recycler.apply {
            adapter = profileListAdapter
            layoutManager = llm
        }
    }

    private fun updateAdapterData(users: List<User>) {
        profileListAdapter.setUsers(users)
    }

    private fun navigateToProfileDetails(uid: String) {
        val action = ProfileListFragmentDirections.actionProfileListFragmentToProfileFragment(uid)
        findNavController().navigate(action)
    }
}
