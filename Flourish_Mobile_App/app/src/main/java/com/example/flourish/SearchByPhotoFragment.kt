package com.example.flourish

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.flourish.databinding.FragmentProcessPhotoBinding
import com.example.flourish.helper.Image
import java.io.File

// This fragment is used to take a photo and search for a plant or disease by photo
class SearchByPhotoFragment : Fragment() {
    private var _binding: FragmentProcessPhotoBinding? = null
    private val binding: FragmentProcessPhotoBinding get() = _binding!!
    private lateinit var img: ImageView
    private lateinit var imageUri: Uri
    private lateinit var imgPath: String

    // ActivityResultContracts.TakePicture() is used to take a photo and save it to the app's internal storage
    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        img.setImageBitmap(null)
        imgPath = Image.saveUri(requireContext(), imageUri)!!
        val imgBitmap = BitmapFactory.decodeFile(imgPath)
        img.setImageBitmap(imgBitmap)
        Log.i("SearchByPhotoFragment", "Image saved to $imgPath")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Bind the layout for this fragment
        _binding = FragmentProcessPhotoBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create an image URI and set the image view to display the image
        imageUri = createImgUri()
        img = binding.imgPlant

        // Launch the camera to take a photo
        contract.launch(imageUri)

        // Proceed to the search result fragment when the proceed button is clicked
        binding.btnProceed.setOnClickListener {
            val getSearchResultFrag = SearchByPhotoResultFragment()
            // Pass the image path to the search result fragment
            getSearchResultFrag.arguments = Bundle().apply {
                putString("imgPath", imgPath)
            }
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container_view, getSearchResultFrag)
                commit()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Create an image URI to save the photo taken by the camera in the app's internal storage
    private fun createImgUri(): Uri {
        val img = File(requireContext().filesDir, "camera_photos.png")
        return FileProvider.getUriForFile(
            requireContext(),
            "com.example.flourish.fileprovider",
            img
        )
    }

}