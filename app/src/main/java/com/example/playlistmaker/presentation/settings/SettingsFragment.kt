package com.example.playlistmaker.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.google.android.material.switchmaterial.SwitchMaterial
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment(R.layout.activity_settings) {

    private val viewModel: SettingsViewModel by viewModel()

    private lateinit var themeSwitcher: SwitchMaterial
    private var isSwitchUpdating = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        themeSwitcher = view.findViewById(R.id.themeSwitcher)

        val shareLayout = view.findViewById<LinearLayout>(R.id.share_layout)
        val supportLayout = view.findViewById<LinearLayout>(R.id.support_layout)
        val licenceLayout = view.findViewById<LinearLayout>(R.id.licence_layout)

        bindObservers()

        shareLayout.setOnClickListener {
            viewModel.onShareClicked(getString(R.string.share_message))
        }

        supportLayout.setOnClickListener {
            viewModel.onSupportClicked(
                email = getString(R.string.support_email),
                subject = getString(R.string.support_subject),
                body = getString(R.string.support_body)
            )
        }

        licenceLayout.setOnClickListener {
            viewModel.onLicenceClicked(getString(R.string.licence_url))
        }

        themeSwitcher.setOnCheckedChangeListener { _, checked ->
            if (isSwitchUpdating) return@setOnCheckedChangeListener
            viewModel.onDarkThemeToggled(checked)
        }
    }

    private fun bindObservers() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            isSwitchUpdating = true
            themeSwitcher.isChecked = state.isDarkThemeEnabled
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
