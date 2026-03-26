package com.example.playlistmaker.presentation.media.create

import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentCreatePlaylistBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreatePlaylistFragment : Fragment() {

    private val viewModel: CreatePlaylistViewModel by viewModel()
    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding: FragmentCreatePlaylistBinding
        get() = _binding ?: error("Binding is only valid between onCreateView and onDestroyView")

    private var renderedCoverUri: String? = null

    private val coverPickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.toString()?.let(viewModel::onCoverSelected)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            handleCloseRequest()
        }
    }

    override fun onDestroyView() {
        Glide.with(this).clear(binding.ivPlaceholder)
        _binding = null
        super.onDestroyView()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            handleCloseRequest()
        }

        binding.imageContainer.setOnClickListener {
            coverPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.etName.doAfterTextChanged {
            viewModel.onNameChanged(it?.toString().orEmpty())
        }

        binding.etDescription.doAfterTextChanged {
            viewModel.onDescriptionChanged(it?.toString().orEmpty())
        }

        binding.btnCreate.setOnClickListener {
            viewModel.onCreateClicked()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner, ::renderState)
        viewModel.playlistCreatedEvent.observe(viewLifecycleOwner, ::onPlaylistCreated)
    }

    private fun renderState(state: CreatePlaylistUiState) {
        if (binding.etName.text?.toString() != state.name) {
            binding.etName.setText(state.name)
        }

        if (binding.etDescription.text?.toString() != state.description) {
            binding.etDescription.setText(state.description)
        }

        binding.btnCreate.isEnabled = state.isCreateEnabled

        if (renderedCoverUri != state.coverUri) {
            renderedCoverUri = state.coverUri
            renderCover(state.coverUri)
        }
    }

    private fun renderCover(coverUri: String?) {
        if (coverUri.isNullOrBlank()) {
            Glide.with(this).clear(binding.ivPlaceholder)
            binding.ivPlaceholder.scaleType = ImageView.ScaleType.CENTER
            binding.ivPlaceholder.setImageResource(R.drawable.ic_playlist_add_80)
            return
        }

        binding.ivPlaceholder.scaleType = ImageView.ScaleType.CENTER_CROP
        Glide.with(this)
            .load(Uri.parse(coverUri))
            .centerCrop()
            .into(binding.ivPlaceholder)
    }

    private fun onPlaylistCreated(playlistName: String) {
        showPlaylistCreatedToast(getString(R.string.playlist_created, playlistName))
        findNavController().navigateUp()
    }

    @Suppress("DEPRECATION")
    private fun showPlaylistCreatedToast(message: String) {
        val toastView = layoutInflater.inflate(R.layout.view_create_playlist_toast, null)
        toastView.findViewById<TextView>(R.id.tvToastMessage).text = message

        Toast(requireContext().applicationContext).apply {
            duration = Toast.LENGTH_SHORT
            setView(toastView)
            setGravity(
                Gravity.BOTTOM or Gravity.FILL_HORIZONTAL,
                0,
                resources.getDimensionPixelSize(R.dimen.create_playlist_toast_bottom_offset)
            )
            show()
        }
    }

    private fun handleCloseRequest() {
        if (!viewModel.hasUnsavedData()) {
            findNavController().navigateUp()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.finish_create_playlist_dialog_title)
            .setMessage(R.string.finish_create_playlist_dialog_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.finish) { _, _ ->
                findNavController().navigateUp()
            }
            .show()
    }
}
