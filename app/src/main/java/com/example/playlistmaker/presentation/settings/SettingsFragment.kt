package com.example.playlistmaker.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivitySettingsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModel()
    private var _binding: ActivitySettingsBinding? = null
    private val binding: ActivitySettingsBinding
        get() = _binding ?: error("Binding is only valid between onCreateView and onDestroyView")
    private var isSwitchUpdating = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivitySettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindObservers()

        binding.shareLayout.setOnClickListener {
            viewModel.onShareClicked(getString(R.string.share_message))
        }

        binding.supportLayout.setOnClickListener {
            viewModel.onSupportClicked(
                email = getString(R.string.support_email),
                subject = getString(R.string.support_subject),
                body = getString(R.string.support_body)
            )
        }

        binding.licenceLayout.setOnClickListener {
            viewModel.onLicenceClicked(getString(R.string.licence_url))
        }

        binding.themeSwitcher.setOnCheckedChangeListener { _, checked ->
            if (isSwitchUpdating) return@setOnCheckedChangeListener
            viewModel.onDarkThemeToggled(checked)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindObservers() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            isSwitchUpdating = true
            binding.themeSwitcher.isChecked = state.isDarkThemeEnabled
            isSwitchUpdating = false
        }

        viewModel.effect.observe(viewLifecycleOwner) { effect ->
            when (effect) {
                SettingsEffect.Close -> findNavController().navigateUp()

                is SettingsEffect.Share -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, effect.text)
                    }
                    startActivity(Intent.createChooser(intent, getString(R.string.share_app)))
                }

                is SettingsEffect.SupportEmail -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(effect.email))
                        putExtra(Intent.EXTRA_SUBJECT, effect.subject)
                        putExtra(Intent.EXTRA_TEXT, effect.body)
                    }
                    startActivity(intent)
                }

                is SettingsEffect.OpenUrl -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(effect.url))
                    startActivity(intent)
                }
            }
        }
    }
}
