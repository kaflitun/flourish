package com.example.flourish

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.flourish.databinding.FragmentGetSearchResultBinding
import com.example.flourish.helper.GptApiRequestHandler
import com.example.flourish.helper.Image
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

// Fragment for displaying the search result (name of the plant or disease) after the user takes a photo
// The app sends the image to OpenAI to get the result (name of the plant or disease)
class SearchByPhotoResultFragment : Fragment() {

    private var _binding: FragmentGetSearchResultBinding? = null
    private val binding: FragmentGetSearchResultBinding get() = _binding!!
    private lateinit var searchModel: SearchModel
    private lateinit var imgPath: String
    private lateinit var imgView: ImageView
    private lateinit var req: String
    private lateinit var searchType: String
    private var imgHeight: Int = 450
    private var imgWidth: Int = 450
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the SearchModel instance
        searchModel = ViewModelProvider(requireActivity())[SearchModel::class.java]
        // Get the search type (plant or disease)
        searchType = searchModel.searchType
        // Get the image path from the arguments
        arguments?.let {
            imgPath = it.getString("imgPath", null)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Bind the layout for this fragment
        _binding = FragmentGetSearchResultBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the image view with the image from the image path
        imgView = binding.plantImage
        val img = BitmapFactory.decodeFile(imgPath)
        imgView.setImageBitmap(img)

        // Resize the image to 1024x1024 if the search type is disease
        if (searchType == "disease") {
            imgHeight = 1024
            imgWidth = 1024
        }

        // Resize the image to 450x450 or 1024x1024
        val rescaledImg = Bitmap.createScaledBitmap(img, imgWidth, imgHeight, false)
        // Encode the rescaled image to base64
        val res = Image.encodeBitmapToBase64(rescaledImg)

        // Create the request to send to OpenAI based on the search type (plant or disease)
        req = if (searchType == "plant")
            GptApiRequestHandler.createRequestPlantImage(res)
        else
            GptApiRequestHandler.createRequestPlantDiseaseImage(res)

        // Set the on click listener for the select button
        binding.btnSelect.setOnClickListener {
            val name = binding.name.text.toString()
            // If the name is empty or unknown , show a toast message
            if (name.isEmpty() || name == "This is not a plant." || name == "Unknown plant."
                || name == "Unknown plant disease." || name == "No plant disease found."
            ) {
                Toast.makeText(requireContext(), "Please try again", Toast.LENGTH_SHORT).show()
            }
            // If the name is not empty, navigate to the search description result fragment
            else {
                val searchDescFrag = SearchDescResultFragment()
                // Pass the name and image path to the search description result fragment using a bundle
                // Bundle is used to pass data between fragments
                val bundle = Bundle()
                bundle.putString("name", name)
                bundle.putString("imgPath", imgPath)
                searchDescFrag.arguments = bundle
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container_view, searchDescFrag)
                    commit()
                }
            }
        }
        // If the user wants to try again, delete the image from the storage and navigate to the search by photo fragment
        binding.btnTryAgain.setOnClickListener {
            Image.deleteImageFromStorage(imgPath)
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container_view, SearchByPhotoFragment())
                commit()
            }
        }

        // Use coroutines to send the image to OpenAI and get the result
        CoroutineScope(Dispatchers.IO).launch {
            // Call the function to send the image to OpenAI
            GptApiRequestHandler.sendRequestToOpenAIWithCoroutines(req) { result ->
                // Process the result on the main thread
                requireActivity().runOnUiThread {
                    // Get the response from the result
                    val response = JSONObject(result)
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    // Set the name text view with the response
                    binding.name.text = response
                    Log.i("SearchByPhotoResultFragment", "Response: $response")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Unbind the view to avoid memory leaks
        _binding = null
    }
}