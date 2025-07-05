package com.example.madcamp1

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class PhotosFragment : Fragment(R.layout.fragment_photos) {

    private lateinit var photoAdapter: PhotoAdapter
    private val allPhotos = mutableListOf<Photo>()

    private var photoUri: Uri? = null
    private val REQUEST_CAMERA = 1001
    private val REQUEST_GALLERY = 1002

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView setup
        val recyclerView = view.findViewById<RecyclerView>(R.id.photoRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        photoAdapter = PhotoAdapter(allPhotos)
        recyclerView.adapter = photoAdapter

        // Load initial sample photos
        loadSamplePhotos()

        // FAB setup
        val fab = view.findViewById<FloatingActionButton>(R.id.addPhotoButton)
        fab.setOnClickListener {
            showPhotoSourceDialog()
        }
    }

    private fun loadSamplePhotos() {
        allPhotos.addAll(
            listOf(
                Photo(R.drawable.soccertest, description = "first try"),
                Photo(R.drawable.soccertest, description = "second try"),
                Photo(R.drawable.soccertest, description = "third try")
            )
        )
        photoAdapter.notifyDataSetChanged()
    }

    private fun showPhotoSourceDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Add Photo")
            .setItems(arrayOf("Camera", "Gallery")) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile = File(requireContext().cacheDir, "photo_${System.currentTimeMillis()}.jpg")
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "com.example.madcamp1.provider", // 권한 이름 정확히 일치해야 함
            photoFile
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION) // 권한 추가
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQUEST_CAMERA -> {
                photoUri?.let {
                    addPhotoFromUri(it)
                }
            }
            REQUEST_GALLERY -> {
                data?.data?.let {
                    addPhotoFromUri(it)
                }
            }
        }
    }

    private fun addPhotoFromUri(uri: Uri) {
        allPhotos.add(Photo(uri = uri))
        photoAdapter.notifyDataSetChanged()
        Toast.makeText(requireContext(), "Photo added!", Toast.LENGTH_SHORT).show()
    }
}
