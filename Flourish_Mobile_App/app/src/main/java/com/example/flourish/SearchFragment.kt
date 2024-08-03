package com.example.flourish

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flourish.adapter.AdapterOptions
import com.example.flourish.databinding.FragmentSearchPlantOptionsBinding
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


// Fragment for the search options for the plant disease and plant identification
// The user can search by name or by image
@Suppress("DEPRECATION")
class SearchFragment : Fragment(), AdapterOptions.RecyclerViewClickListener,
    EasyPermissions.PermissionCallbacks {
    companion object {
        // Camera request code for the camera permission
        // Can be any number as long as it is unique to the request
        const val CAMERA_REQUEST_CODE = 123
    }

    private var _binding: FragmentSearchPlantOptionsBinding? = null
    private val binding: FragmentSearchPlantOptionsBinding get() = _binding!!
    private lateinit var searchOptionsList: ArrayList<String>
    private lateinit var adapterOptions: AdapterOptions
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Binding the layout for the fragment search plant options
        _binding = FragmentSearchPlantOptionsBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Getting the search options from the resources
        searchOptionsList =
            resources.getStringArray(R.array.plant_disease_search_options).toCollection(ArrayList())
        // Setting the adapter for the recycler view to display the search options
        adapterOptions = AdapterOptions(searchOptionsList, this)

        // Setting the layout manager for the recycler view
        recyclerView = binding.recyclerView
        recyclerView.adapter = adapterOptions
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

    }

    // Function to handle the click event on the search options and navigate to the respective fragment
    override fun onItemClick(position: Int) {
        when (searchOptionsList[position]) {
            "Find by Name" -> {
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container_view, SearchByNameFragment())
                    commit()
                }
            }

            "Find by Image" -> {
                if (hasPermissions()) {
                    navigateToPhotoSearchFragment()
                } else {
                    requestPermissions()
                }
            }
        }
    }

    // Function to navigate to the search by photo fragment
    // It requires the camera permission to be granted to navigate to the fragment
    private fun navigateToPhotoSearchFragment() {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container_view, SearchByPhotoFragment())
            commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Function to check if the camera permission is granted
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        // If the camera permission is granted, navigate to the search by photo fragment
        navigateToPhotoSearchFragment()
        Log.i("SearchFragment", "Camera Permission granted")
    }

    // Function to handle the case when the camera permission is denied
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.i("SearchFragment", "Camera Permission denied")
        // If the camera permission is denied permanently, show the app settings dialog to navigate to the app settings
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
        // If the camera permission is denied, request the permission again
        else {
            requestPermissions()
        }
    }

    // Check camera permission status in the Manifest file
    private fun hasPermissions(): Boolean {
        return EasyPermissions.hasPermissions(
            requireContext(),
            android.Manifest.permission.CAMERA
        )
    }

    // Request camera permission if not granted
    private fun requestPermissions() {
        // Request the camera permission using EasyPermissions library
        EasyPermissions.requestPermissions(
            this,
            "This app needs access to your camera to take photos",
            CAMERA_REQUEST_CODE,
            android.Manifest.permission.CAMERA
        )
    }

    // Function to handle the camera permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Handle the camera permission request result
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}
