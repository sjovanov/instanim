package mk.com.ukim.finki.mpip.instanim.ui.posts.create

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import mk.com.ukim.finki.mpip.instanim.data.model.Status
import mk.com.ukim.finki.mpip.instanim.databinding.FragmentCreatePostBinding
import mk.com.ukim.finki.mpip.instanim.util.FactoryInjector

class CreatePostFragment : Fragment() {

    private lateinit var binding: FragmentCreatePostBinding
    private val viewModel: PostCreateViewModel by activityViewModels {
        FactoryInjector.getPostCreateViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val uri = Uri.parse(viewModel.postBuilder.imageUri)
        binding.postImageThumbnail.setImageURI(uri)

        binding.createPostButton.setOnClickListener {
            handleCreate()
        }

        viewModel.createResult.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.SUCCESS -> {
                    redirectToList()
                }
                else -> {
                    // do nothing
                    // TODO: disable user input with dialog while waiting
                }
            }
        })

    }

    private fun redirectToList() {
        val action = CreatePostFragmentDirections.actionCreatePostFragmentToPostListFragment()
        findNavController().navigate(action)
    }

    private fun handleCreate() {
        val description = binding.postDescription.text.toString()
        val location = binding.postLocation.text.toString()

        viewModel.finalizePost(description, location)
    }
}