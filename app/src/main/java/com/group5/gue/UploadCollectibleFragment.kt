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
 * Fragment responsible for uploading new collectibles to the system.
 * This functionality is restricted to administrator users only.
 * It handles image selection, local preview, and multi-part data submission (metadata + image).
 */
class UploadCollectibleFragment : Fragment(R.layout.fragment_upload_collectible) {

    companion object {
        // Result key used to notify parent fragments that a collectible was successfully uploaded.
        const val RESULT_KEY = "collectible_uploaded"
    }

    // Repository for collectible data operations.
    private val repository = CollectibleRepository.getInstance()
    // Repository for user data and authentication status.
    private val userRepository = UserRepository.getInstance()

    // Temporarily holds the raw bytes of the selected image.
    private var selectedImageBytes: ByteArray? = null
    // Stores the file extension (e.g., "png", "jpg") of the selected image.
    private var selectedImageExtension: String? = null

    // UI component for displaying the selected image.
    private lateinit var previewImageView: ImageView
    // TextView to provide feedback on the image selection process.
    private lateinit var imageStatusView: TextView
    // Input field for the collectible's display name.
    private lateinit var nameInput: TextInputEditText
    // Input field for the point value (score) of the collectible.
    private lateinit var scoreInput: TextInputEditText
    // Input field for a detailed description of the collectible.
    private lateinit var descriptionInput: TextInputEditText
    // Loading indicator shown during the network-intensive upload process.
    private lateinit var progressBar: ProgressBar
    // Button to trigger the final validation and submission.
    private lateinit var publishButton: Button
    // Button to open the system file picker for selecting an image.
    private lateinit var chooseImageButton: Button

    /**
     * Activity Result Launcher for picking an image from the device's storage.
     * Updates the UI and internal byte buffers upon selection.
     */
    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) {
            return@registerForActivityResult
        }

        // Display selected image in the preview container
        previewImageView.setImageURI(uri)
        previewImageView.isVisible = true
        imageStatusView.setText(R.string.collectibles_loading)

        // Read the image bytes asynchronously to avoid blocking the UI thread
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

    /**
     * Initializes the UI components and sets up click listeners.
     * Enforces admin-only access check.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Security check: bounce non-admin users back to the previous screen
        if (userRepository.getCachedUser()?.isAdmin != true) {
            Toast.makeText(requireContext(), "Only admins can upload collectibles", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        // Bind layout views to local variables
        previewImageView = view.findViewById(R.id.uploadPreviewImage)
        imageStatusView = view.findViewById(R.id.uploadImageStatus)
        nameInput = view.findViewById(R.id.collectibleNameInput)
        scoreInput = view.findViewById(R.id.collectibleScoreInput)
        descriptionInput = view.findViewById(R.id.collectibleDescriptionInput)
        progressBar = view.findViewById(R.id.uploadProgressBar)
        publishButton = view.findViewById(R.id.publishCollectibleButton)
        chooseImageButton = view.findViewById(R.id.chooseImageButton)

        // Initial UI state: hide preview and loader
        previewImageView.isVisible = false
        progressBar.isVisible = false

        // Navigate back when requested
        view.findViewById<Button>(R.id.backToGalleryButton).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Launch image picker on click
        chooseImageButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // Validate and submit data on click
        publishButton.setOnClickListener {
            submitCollectible()
        }
    }

    /**
     * Validates all input fields (name, score, image) and submits the collectible data to the repository.
     * If successful, it notifies the parent and closes the fragment.
     */
    private fun submitCollectible() {
        // Double-check admin status before proceeding with server request
        if (userRepository.getCachedUser()?.isAdmin != true) {
            Toast.makeText(requireContext(), "Only admins can upload collectibles", Toast.LENGTH_SHORT).show()
            return
        }

        // Gather and sanitize user input
        val name = nameInput.text?.toString()?.trim().orEmpty()
        val scoreRaw = scoreInput.text?.toString()?.trim().orEmpty()
        val description = descriptionInput.text?.toString()?.trim()?.takeIf { it.isNotBlank() }
        val imageBytes = selectedImageBytes
        val fileExtension = selectedImageExtension

        // Validation logic for name
        if (name.isBlank()) {
            nameInput.error = getString(R.string.upload_name_required)
            return
        }

        // Validation logic for score (must be a positive integer)
        if (scoreRaw.isBlank()) {
            scoreInput.error = getString(R.string.upload_score_required)
            return
        }

        val score = scoreRaw.toIntOrNull()
        if (score == null || score < 0) {
            scoreInput.error = getString(R.string.upload_score_invalid)
            return
        }

        // Image is mandatory for new collectibles
        if (imageBytes == null || imageBytes.isEmpty() || fileExtension.isNullOrBlank()) {
            Toast.makeText(requireContext(), R.string.upload_image_required, Toast.LENGTH_SHORT).show()
            return
        }

        val uploadBytes = imageBytes
        val uploadExtension = fileExtension

        // Disable UI and show loading indicator
        setUploading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                // Construct data model without the image URL (URL is generated by the repository)
                val collectible = Collectible(
                    creatorId = null,
                    name = name,
                    score = score,
                    description = description
                )
                // Execute insertion into DB and upload to storage
                repository.insertCollectible(
                    collectible = collectible,
                    imageBytes = uploadBytes,
                    fileExtension = uploadExtension,
                )
            }

            // Restore UI state
            setUploading(false)
            when (result) {
                is Result.Success<Collectible> -> {
                    // Notify gallery fragment to refresh
                    parentFragmentManager.setFragmentResult(RESULT_KEY, Bundle())
                    parentFragmentManager.popBackStack()
                }

                is Result.Error<Collectible> -> {
                    // Show error details on failure
                    Toast.makeText(
                        requireContext(),
                        result.getError().message,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }


    /**
     * Resolves the file extension from the given URI using MimeTypeMap or filename parsing.
     * Defaults to "jpg" if no extension can be determined.
     */
    private fun resolveFileExtension(uri: Uri): String {
        val mimeType = requireContext().contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            ?: uri.lastPathSegment
                ?.substringAfterLast('.', "")
                ?.lowercase()
                ?.takeIf { it.isNotBlank() }
            ?: "jpg"
    }

    /**
     * Toggles the UI state between 'uploading' and 'idle'.
     * Disables input elements to prevent race conditions or duplicate submissions.
     * @param isUploading Whether an upload is currently in progress.
     */
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