package com.example.madcamp1

import android.app.AlertDialog
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapter
    private lateinit var playerList: List<Player>

    private val REQUEST_PICK_IMAGE = 2001
    private val REQUEST_CAMERA_IMAGE = 2002

    private var imageProfilePreview: ImageView? = null
    private var dialogImageUri: Uri? = null

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // RecyclerView 초기화
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // playerList 초기화 후 ViewModel에 설정
        if (sharedViewModel.players.value.isNullOrEmpty()) {
            sharedViewModel.setPlayers(getPlayers())
        }

        // FloatingActionButton 설정
        val fab = view.findViewById<FloatingActionButton>(R.id.addProfileButton)

        fab.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_player, null)

            val imageProfile = dialogView.findViewById<ImageView>(R.id.imageProfile)
            imageProfilePreview = imageProfile // 미리보기 업데이트용 참조 저장
            dialogImageUri = null // 다이얼로그 열 때 초기화

            val btnSelectPhoto = dialogView.findViewById<Button>(R.id.btnSelectPhoto)
            val btnTakePhoto = dialogView.findViewById<Button>(R.id.btnTakePhoto)
            val editName = dialogView.findViewById<EditText>(R.id.editPlayerName)
            val editPosition = dialogView.findViewById<EditText>(R.id.editPlayerPosition)
            val editNumber = dialogView.findViewById<EditText>(R.id.editPlayerNumber)
            val editAvailable = dialogView.findViewById<EditText>(R.id.editAvailableTime)

            // 갤러리에서 사진 선택
            btnSelectPhoto.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, REQUEST_PICK_IMAGE)
            }

            // 카메라로 사진 촬영
            btnTakePhoto.setOnClickListener {
                val photoFile = File(requireContext().cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                dialogImageUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.madcamp1.provider",
                    photoFile
                )
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, dialogImageUri)
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                startActivityForResult(intent, REQUEST_CAMERA_IMAGE)
            }

            // 입력 다이얼로그 생성
            AlertDialog.Builder(requireContext())
                .setTitle("선수 추가")
                .setView(dialogView)
                .setPositiveButton("추가") { _, _ ->
                    val name = editName.text.toString().trim()
                    val position = editPosition.text.toString().trim()
                    val numberText = editNumber.text.toString().trim()
                    val availableText = editAvailable.text.toString().trim()

                    if (name.isNotEmpty() && position.isNotEmpty() && numberText.isNotEmpty()) {
                        val number = numberText.toIntOrNull() ?: 0
                        val availableSlots = availableText.split(',').map { it.trim() }.filter { it.isNotEmpty() }
                        val currentList = sharedViewModel.players.value ?: emptyList()

                        val newPlayer = Player(
                            name = name,
                            position = position,
                            number = number,
                            availableSlots = availableSlots,
                            photoResId = if (dialogImageUri == null) R.drawable.playerdefault else 0,
                            uri = dialogImageUri
                        )
                        sharedViewModel.setPlayers(currentList + newPlayer)
                        dialogImageUri = null // 초기화
                    } else {
                        Toast.makeText(requireContext(), "모든 항목을 입력하세요", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("취소") { _, _ ->
                    dialogImageUri = null // 취소 시도 초기화
                }
                .show()
        }

        // ViewModel의 데이터로 RecyclerView 갱신
        sharedViewModel.players.observe(viewLifecycleOwner) { players ->
            adapter = MyAdapter(players)
            recyclerView.adapter = adapter
        }

        return view
    }

    // onActivityResult에서 선택한 이미지 미리보기 반영
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQUEST_PICK_IMAGE -> {
                data?.data?.let { uri ->
                    dialogImageUri = uri
                    imageProfilePreview?.setImageURI(uri)
                }
            }
            REQUEST_CAMERA_IMAGE -> {
                dialogImageUri?.let { uri ->
                    imageProfilePreview?.setImageURI(uri)
                }
            }
        }
    }

    // 샘플 플레이어 목록 반환
    private fun getPlayers(): List<Player> {
        return listOf(
            Player(
                name = "John Smith",
                position = "Forward",
                number = 9,
                availableSlots = listOf("2025-07-05 08:00", "2025-07-07 09:00"),
                photoResId = R.drawable.playerdefault
            ),
            Player(
                name = "Alex Johnson",
                position = "Midfielder",
                number = 8,
                availableSlots = listOf("2025-07-06 08:00", "2025-07-07 08:00"),
                photoResId = R.drawable.playerdefault
            ),
            Player(
                name = "Emily Davis",
                position = "Defender",
                number = 4,
                availableSlots = listOf("2025-07-05 08:00", "2025-07-08 09:00"),
                photoResId = R.drawable.playerdefault
            ),
            Player(
                name = "Michael Lee",
                position = "Goalkeeper",
                number = 1,
                availableSlots = listOf("2025-07-06 09:00", "2025-07-08 09:00"),
                photoResId = R.drawable.playerdefault
            )
        )
    }
}
