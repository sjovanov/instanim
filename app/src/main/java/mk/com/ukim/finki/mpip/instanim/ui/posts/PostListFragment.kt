package mk.com.ukim.finki.mpip.instanim.ui.posts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import mk.com.ukim.finki.mpip.instanim.adapter.PostAdapter
import mk.com.ukim.finki.mpip.instanim.data.entity.Post
import mk.com.ukim.finki.mpip.instanim.data.model.Status
import mk.com.ukim.finki.mpip.instanim.databinding.FragmentPostListBinding
import mk.com.ukim.finki.mpip.instanim.ui.auth.AuthViewModel
import mk.com.ukim.finki.mpip.instanim.util.FactoryInjector

class PostListFragment : Fragment() {
    private var _binding: FragmentPostListBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var postAdapter: PostAdapter

    private val authViewModel: AuthViewModel by activityViewModels {
        FactoryInjector.getAuthViewModel()
    }

    private val postListViewModel: PostViewModel by viewModels {
        FactoryInjector.getPostViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()

        authViewModel.currentUser.value?.data?.let { postListViewModel.fetchPosts(it.uid) }

        postListViewModel.posts.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.LOADING -> {
                    binding.loadingPanel.visibility = View.VISIBLE
                    // do nothing
                }
                Status.ERROR -> {
                    binding.loadingPanel.visibility = View.GONE
                    // do nothing
                }
                Status.SUCCESS -> {
                    binding.loadingPanel.visibility = View.GONE
                    it.data?.let { posts ->
                        updateAdapterData(posts.sortedByDescending { post -> post.createdAt })
                    }
                }
            }
        })
    }

    private fun initRecycler() {
        authViewModel.fetchCurrentUser()
        postAdapter = PostAdapter(
            authViewModel.currentUser.value?.data?.uid.toString(),
            mutableListOf(), // empty list
            likePost = { post -> likePost(post) },
            onDetails = { id -> navigateToDetails(id) })

        val llm = LinearLayoutManager(context)
//        binding.postList.setItemViewCacheSize(30)

        binding.postList.apply {
            adapter = postAdapter
            layoutManager = llm
        }
    }

    private fun updateAdapterData(posts: List<Post>) {
        postAdapter.setPosts(posts)
    }

    private fun navigateToDetails(postId: String) {
        val action = PostListFragmentDirections.actionPostListFragmentToPostDetailsFragment(postId)
        findNavController().navigate(action)
    }

    private fun likePost(post: Post) {
        postListViewModel.likePost(post)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
