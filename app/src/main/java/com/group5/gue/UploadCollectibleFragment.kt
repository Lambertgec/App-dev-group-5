package com.group5.gue

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.group5.gue.data.Result
import com.group5.gue.data.collectible.CollectibleRepository
import com.group5.gue.data.model.Collectible
import com.group5.gue.data.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Collectible upload screen.
 */
class UploadCollectibleFragment : Fragment(R.layout.fragment_upload_collectible) {

    companion object {
        const val RESULT_KEY = "collectible_uploaded"
    }

    private val repository = CollectibleRepository.getInstance()
    private val userRepository = UserRepository.getInstance()

    private var selectedImageBytes: ByteArray? = null
    private var selectedImageExtension: String? = null

    private lateinit var previewImageView: ImageView
    private lateinit var imageStatusView: TextView
    private lateinit var nameInput: TextInputEditText
    private lateinit var scoreInput: TextInputEditText
    private lateinit var descriptionInput: TextInputEditText
    private lateinit var progressBar: ProgressBar
    private lateinit var publishButton: Button
    private lateinit var chooseImageButton: Button

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) {
            return@registerForActivityResult
        }

        previewImageView.setImageURI(uri)
        previewImageView.isVisible = true
        imageStatusView.setText(R.string.collectibles_loading)

        viewLifecycleOwner.lifecycleScope.launch {
            val imageBytes = withContext(Dispatchers.IO) {
                requireContext().contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }

            if (imageBytes == null || imageBytes.isEmpty()) {
                selectedImageBytes = null
                selectedImageExtension = null
                imageStatusView.setText(R.string.upload_read_failed)
                Toast.makeText(requireContext(), R.string.upload_read_failed, Toast.LENGTH_SHORT).show()
                return@launch
            }

            selectedImageBytes = imageBytes
            selectedImageExtension = resolveFileExtension(uri)
            imageStatusView.setText(R.string.upload_image_selected)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (userRepository.getCachedUser()?.isAdmin != true) {
            Toast.makeText(requireContext(), "Only admins can upload collectibles", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        previewImageView = view.findViewById(R.id.uploadPreviewImage)
        imageStatusView = view.findViewById(R.id.uploadImageStatus)
        nameInput = view.findViewById(R.id.collectibleNameInput)
        scoreInput = view.findViewById(R.id.collectibleScoreInput)
        descriptionInput = view.findViewById(R.id.collectibleDescriptionInput)
        progressBar = view.findViewById(R.id.uploadProgressBar)
        publishButton = view.findViewById(R.id.publishCollectibleButton)
        chooseImageButton = view.findViewById(R.id.chooseImageButton)

        previewImageView.isVisible = false
        progressBar.isVisible = false

        view.findViewById<Button>(R.id.backToGalleryButton).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        chooseImageButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        publishButton.setOnClickListener {
            submitCollectible()
        }
    }

    private fun submitCollectible() {
        if (userRepository.getCachedUser()?.isAdmin != true) {
            Toast.makeText(requireContext(), "Only admins can upload collectibles", Toast.LENGTH_SHORT).show()
            return
        }

        val name = nameInput.text?.toString()?.trim().orEmpty()
        val scoreRaw = scoreInput.text?.toString()?.trim().orEmpty()
        val description = descriptionInput.text?.toString()?.trim()?.takeIf { it.isNotBlank() }
        val imageBytes = selectedImageBytes
        val fileExtension = selectedImageExtension

        if (name.isBlank()) {
            nameInput.error = getString(R.string.upload_name_required)
            return
        }

        if (scoreRaw.isBlank()) {
            scoreInput.error = getString(R.string.upload_score_required)
            return
        }

        val score = scoreRaw.toIntOrNull()
        if (score == null || score < 0) {
            scoreInput.error = getString(R.string.upload_score_invalid)
            return
        }

        if (imageBytes == null || imageBytes.isEmpty() || fileExtension.isNullOrBlank()) {
            Toast.makeText(requireContext(), R.string.upload_image_required, Toast.LENGTH_SHORT).show()
            return
        }

        val uploadBytes = imageBytes
        val uploadExtension = fileExtension

        setUploading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                val collectible = Collectible(
                    creatorId = null,
                    name = name,
                    score = score,
                    description = description
                )
                repository.insertCollectible(
                    collectible = collectible,
                    imageBytes = uploadBytes,
                    fileExtension = uploadExtension,
                )
            }

            setUploading(false)
            when (result) {
                is Result.Success<Collectible> -> {
                    parentFragmentManager.setFragmentResult(RESULT_KEY, Bundle())
                    parentFragmentManager.popBackStack()
                }

                is Result.Error<Collectible> -> {
                    Toast.makeText(
                        requireContext(),
                        result.getError().message,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }


    private fun resolveFileExtension(uri: Uri): String {
        val mimeType = requireContext().contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            ?: uri.lastPathSegment
                ?.substringAfterLast('.', "")
                ?.lowercase()
                ?.takeIf { it.isNotBlank() }
            ?: "jpg"
    }

    private fun setUploading(isUploading: Boolean) {
        progressBar.isVisible = isUploading
        publishButton.isEnabled = !isUploading
        chooseImageButton.isEnabled = !isUploading
        nameInput.isEnabled = !isUploading
        scoreInput.isEnabled = !isUploading
        descriptionInput.isEnabled = !isUploading
        if (isUploading) {
            Toast.makeText(requireContext(), R.string.upload_in_progress, Toast.LENGTH_SHORT).show()
        }
    }
}