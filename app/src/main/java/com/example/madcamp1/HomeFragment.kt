package com.example.madcamp1

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        if (sharedViewModel.players.value.isNullOrEmpty()) {
            sharedViewModel.setPlayers(getPlayers())
        }

        val fab = view.findViewById<FloatingActionButton>(R.id.addProfileButton)

        fab.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_player, null)

            val imageProfile = dialogView.findViewById<ImageView>(R.id.imageProfile)
            imageProfilePreview = imageProfile
            dialogImageUri = null

            val btnSelectPhoto = dialogView.findViewById<Button>(R.id.btnSelectPhoto)
            val btnTakePhoto = dialogView.findViewById<Button>(R.id.btnTakePhoto)
            val editName = dialogView.findViewById<EditText>(R.id.editPlayerName)
            val spinnerPosition = dialogView.findViewById<Spinner>(R.id.spinnerPlayerPosition)
            val editNumber = dialogView.findViewById<EditText>(R.id.editPlayerNumber)
            val btnPickTime = dialogView.findViewById<Button>(R.id.btnPickTime)
            val textSelectedTimes = dialogView.findViewById<TextView>(R.id.textSelectedTimes)

            val selectedSlots = mutableListOf<String>()

            // 포지션 드롭다운 설정
            val positions = listOf("FW", "MF", "DF", "GK")
            spinnerPosition.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, positions)

            // 시간대 추가 버튼
            btnPickTime.setOnClickListener {
                val pickView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_pick_time, null)
                val datePicker = pickView.findViewById<DatePicker>(R.id.datePicker)
                val timePicker = pickView.findViewById<TimePicker>(R.id.timePicker)
                timePicker.setIs24HourView(true)

                // 분을 30분 단위로 제한
                try {
                    val minuteField = timePicker.findViewById<NumberPicker>(
                        android.content.res.Resources.getSystem().getIdentifier("minute", "id", "android")
                    )
                    minuteField.minValue = 0
                    minuteField.maxValue = 1
                    minuteField.displayedValues = arrayOf("00", "30")
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                AlertDialog.Builder(requireContext())
                    .setTitle("시간 선택")
                    .setView(pickView)
                    .setPositiveButton("추가") { _, _ ->
                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.YEAR, datePicker.year)
                            set(Calendar.MONTH, datePicker.month)
                            set(Calendar.DAY_OF_MONTH, datePicker.dayOfMonth)
                            set(Calendar.HOUR_OF_DAY, timePicker.hour)
                            set(Calendar.MINUTE, timePicker.minute)
                        }
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        val slot = sdf.format(calendar.time)
                        selectedSlots.add(slot)
                        textSelectedTimes.text = selectedSlots.joinToString(", ")
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }

            btnSelectPhoto.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, REQUEST_PICK_IMAGE)
            }

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

            AlertDialog.Builder(requireContext())
                .setTitle("선수 추가")
                .setView(dialogView)
                .setPositiveButton("추가") { _, _ ->
                    val name = editName.text.toString().trim()
                    val position = spinnerPosition.selectedItem.toString()
                    val numberText = editNumber.text.toString().trim()

                    if (name.isNotEmpty() && numberText.isNotEmpty()) {
                        val number = numberText.toIntOrNull() ?: 0
                        val currentList = sharedViewModel.players.value ?: emptyList()

                        val newPlayer = Player(
                            name = name,
                            position = position,
                            number = number,
                            availableSlots = selectedSlots,
                            photoResId = if (dialogImageUri == null) R.drawable.playerdefault else 0,
                            uri = dialogImageUri
                        )
                        sharedViewModel.setPlayers(currentList + newPlayer)
                        dialogImageUri = null
                    } else {
                        Toast.makeText(requireContext(), "모든 항목을 입력하세요", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("취소") { _, _ ->
                    dialogImageUri = null
                }
                .show()
        }

        sharedViewModel.players.observe(viewLifecycleOwner) { players ->
            adapter = MyAdapter(players)
            recyclerView.adapter = adapter
        }

        return view
    }

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
