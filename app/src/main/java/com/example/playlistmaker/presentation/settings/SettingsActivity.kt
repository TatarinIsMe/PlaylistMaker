package com.example.playlistmaker.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.playlistmaker.R
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModel()

    private lateinit var themeSwitcher: SwitchMaterial
    private var isSwitchUpdating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        themeSwitcher = findViewById(R.id.themeSwitcher)

        val back = findViewById<ImageView>(R.id.back_arrow)
        val shareLayout = findViewById<LinearLayout>(R.id.share_layout)
        val supportLayout = findViewById<LinearLayout>(R.id.support_layout)
        val licenceLayout = findViewById<LinearLayout>(R.id.licence_layout)

        bindObservers()

        back.setOnClickListener { viewModel.onBackClicked() }

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
        viewModel.state.observe(this) { state ->
            isSwitchUpdating = true
            themeSwitcher.isChecked = state.isDarkThemeEnabled
            isSwitchUpdating = false
        }

        viewModel.effect.observe(this) { effect ->
            when (effect) {
                SettingsEffect.Close -> finish()

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
