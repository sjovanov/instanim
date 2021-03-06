package mk.com.ukim.finki.mpip.instanim.ui.posts

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import mk.com.ukim.finki.mpip.instanim.R
import mk.com.ukim.finki.mpip.instanim.adapter.CommentAdapter
import mk.com.ukim.finki.mpip.instanim.data.entity.Comment
import mk.com.ukim.finki.mpip.instanim.data.entity.Post
import mk.com.ukim.finki.mpip.instanim.data.model.Status
import mk.com.ukim.finki.mpip.instanim.databinding.FragmentPostDetailsBinding
import mk.com.ukim.finki.mpip.instanim.glide.GlideApp
import mk.com.ukim.finki.mpip.instanim.ui.auth.AuthViewModel
import mk.com.ukim.finki.mpip.instanim.util.FactoryInjector


class PostDetailsFragment : Fragment() {
    private var _binding: FragmentPostDetailsBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var commentAdapter: CommentAdapter
    private val args: PostDetailsFragmentArgs by navArgs()

    private val authViewModel: AuthViewModel by activityViewModels {
        FactoryInjector.getAuthViewModel()
    }

    private val viewModel: PostViewModel by viewModels {
        FactoryInjector.getPostViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val postId = args.postId
        postId?.let {
            viewModel.fetchPost(postId)
        }
        initRecycler()

        viewModel.post.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.let { post -> attachDataToFragment(post) }
                }
                Status.ERROR -> {

                }
                Status.LOADING -> {

                }
            }
        })

        viewModel.updatePostStatus.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.SUCCESS -> {
                    postId?.let {
                        viewModel.fetchPost(postId)
                        binding.commentText.editText?.text?.clear()
                        binding.commentText.clearFocus()
                        val inputManager =
                            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputManager.hideSoftInputFromWindow(view.windowToken, 0)
                    }
                }
                Status.ERROR -> {

                }
                Status.LOADING -> {

                }
            }
        })

        viewModel.deletePost.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.SUCCESS -> {
                    findNavController().navigateUp()
                }
                Status.ERROR -> {

                }
                Status.LOADING -> {

                }
            }
        })
    }

    private fun attachDataToFragment(post: Post) {
        GlideApp.with(binding.root)
            .load(post.imageUri)
            .into(binding.postImageDetails)
        binding.descriptionPostDetails.text = post.description
        binding.likesPostDetails.text = resources.getString(R.string.liked_by, post.likedBy.size)
        updateAdapterData(post.comments.filterNotNull())
        binding.usernameTextView.text = post.username
        binding.likeButton.setOnClickListener {
            viewModel.likePost(post)
        }
        binding.commentButton.setOnClickListener {
            if (binding.commentText.editText?.text?.isNotEmpty() == true) {
                viewModel.postComment(post, binding.commentText.editText?.text.toString())
            } else {
                Toast.makeText(context, "Please write your comment first", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        binding.mapsButton.setOnClickListener {
            navigateToMapPostDetails(post)
        }
        authViewModel.fetchCurrentUser()
        val currentUserUid = authViewModel.currentUser.value?.data?.uid
        if (post.likedBy.contains(currentUserUid)) {
//                binder.likeButton.text = "Unlike"
            binding.likeButton.icon = ResourcesCompat.getDrawable(
                binding.root.resources,
                R.drawable.ic_baseline_like_24,
                null
            )
        } else {
//                binder.likeButton.text = "Like"
            binding.likeButton.icon = ResourcesCompat.getDrawable(
                binding.root.resources,
                R.drawable.ic_baseline_like_border_24,
                null
            )
        }
        if (post.userId == currentUserUid){
            binding.deleteButton.visibility = View.VISIBLE
            binding.deleteButton.setOnClickListener {
                deletePost(post.postId)
            }
        }
    }

    private fun initRecycler() {
        commentAdapter = CommentAdapter()

        val llm = LinearLayoutManager(context)
        binding.commentRecyclerView.addItemDecoration(
            DividerItemDecoration(
                binding.commentRecyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.commentRecyclerView.apply {
            adapter = commentAdapter
            layoutManager = llm
        }
    }

    private fun updateAdapterData(comments: List<Comment>) {
        commentAdapter.setComments(comments)
    }

    private fun navigateToMapPostDetails(post: Post) {
        val action =
            PostDetailsFragmentDirections.actionPostDetailsFragmentToPostDetailsMapsFragment(
                post.lat.toString(), post.lng.toString(),
                post.description.toString()
            )
        findNavController().navigate(action)
    }

    private fun deletePost(postId: String){
        viewModel.deletePost(postId)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
