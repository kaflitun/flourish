package com.example.flourish

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.flourish.databinding.FragmentSearchByNameBinding

// Fragment for searching for a plant or disease by name
class SearchByNameFragment : Fragment() {
    private var _binding: FragmentSearchByNameBinding? = null
    private val binding: FragmentSearchByNameBinding get() = _binding!!
    private lateinit var searchModel: SearchModel
    private lateinit var name: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the SearchModel instance
        searchModel = ViewModelProvider(requireActivity())[SearchModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Bind the layout for this fragment
        _binding = FragmentSearchByNameBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Proceed to the search result fragment when the continue button is clicked
        binding.continueBtn.setOnClickListener {

            // Set the name to the text entered by the user
            binding.name.text.toString().let { name = it }

            // Proceed to the search result fragment if the name is not empty
            if (name.isNotEmpty()) {
                Log.i("SearchByNameFragment", "Plant name: $name")
                val searchDescFrag = SearchDescResultFragment()
                // Pass the name to the search result fragment and set the image path to an empty string
                val bundle = Bundle()
                bundle.putString("name", name)
                bundle.putString("imgPath", "")
                // Set the arguments for the search result fragment
                searchDescFrag.arguments = bundle

                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container_view, searchDescFrag)
                    commit()
                }
            }
            // Display a toast message if the name is empty
            else {
                Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Unbind the view to avoid memory leaks
        _binding = null
    }

}