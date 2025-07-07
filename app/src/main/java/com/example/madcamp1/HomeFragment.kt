package com.example.madcamp1

import android.graphics.Color
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 🔍 검색창 기능 추가
        val searchInput = view.findViewById<EditText>(R.id.searchInput)
        searchInput.addTextChangedListener {
            val query = it.toString().lowercase()
            val players = sharedViewModel.players.value ?: emptyList()
            val filtered = players.filter { player -> player.name.lowercase().contains(query) }
            recyclerView.adapter = MyAdapter(filtered.toMutableList())
        }

        if (sharedViewModel.players.value.isNullOrEmpty()) {
            sharedViewModel.setPlayers(getPlayers())
        }

        val fab = view.findViewById<FloatingActionButton>(R.id.addProfileButton)

        fab.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_player, null)

            // 👇 여기서 루트 레이아웃에 터치 리스너 추가
            dialogView.setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    val focusedView = dialogView.findFocus()
                    if (focusedView is EditText) {
                        focusedView.clearFocus()
                        imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
                    }
                    // ✅ 접근성 처리를 위해 performClick 호출
                    view.performClick()
                }
                false
            }

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
            val editTags = dialogView.findViewById<EditText>(R.id.editPlayerTags)

            val selectedSlots = mutableListOf<String>()

            // 포지션 드롭다운 설정
            val positions = listOf("FW", "MF", "DF", "GK")
            spinnerPosition.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, positions)

            // 시간대 추가 버튼
            btnPickTime.setOnClickListener {
                val gridView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_pick_time, null)
                val gridContainer = gridView.findViewById<LinearLayout>(R.id.gridContainer)

                // 7일치 날짜
                val today = LocalDate.now()
                val dates = (0..6).map { today.plusDays(it.toLong()) }

                // 시간대 30분 단위
                val times = mutableListOf<String>()
                for (hour in 8..21) {
                    times.add(String.format("%02d:00", hour))
                    times.add(String.format("%02d:30", hour))
                }

                val selectedSlotsTemp = mutableSetOf<String>()
                selectedSlotsTemp.addAll(selectedSlots) // 이전 선택 유지

                // 날짜 헤더
                val headerRow = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                }
                headerRow.addView(TextView(requireContext()).apply {
                    text = ""
                    width = 100
                })
                dates.forEach { date ->
                    headerRow.addView(TextView(requireContext()).apply {
                        text = date.format(DateTimeFormatter.ofPattern("MM/dd"))
                        width = 100
                        gravity = Gravity.CENTER
                        setBackgroundColor(Color.LTGRAY)
                    })
                }
                gridContainer.addView(headerRow)

                // 각 시간행
                times.forEach { time ->
                    val row = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.HORIZONTAL
                    }

                    // 왼쪽 라벨
                    row.addView(TextView(requireContext()).apply {
                        text = time
                        width = 100
                        gravity = Gravity.CENTER
                        setBackgroundColor(Color.LTGRAY)
                    })

                    // 날짜별 셀
                    dates.forEach { date ->
                        val slotKey = "${date} $time"
                        val cell = TextView(requireContext()).apply {
                            width = 100
                            height = 100
                            gravity = Gravity.CENTER
                            text = ""
                            setBackgroundColor(
                                if (selectedSlotsTemp.contains(slotKey)) Color.GREEN else Color.WHITE
                            )
                            setOnClickListener {
                                if (selectedSlotsTemp.contains(slotKey)) {
                                    selectedSlotsTemp.remove(slotKey)
                                    setBackgroundColor(Color.WHITE)
                                } else {
                                    selectedSlotsTemp.add(slotKey)
                                    setBackgroundColor(Color.GREEN)
                                }
                            }
                        }
                        row.addView(cell)
                    }

                    gridContainer.addView(row)
                }

                AlertDialog.Builder(requireContext())
                    .setTitle("선호 시간대 선택")
                    .setView(gridView)
                    .setPositiveButton("확인") { _, _ ->
                        selectedSlots.clear()
                        selectedSlots.addAll(selectedSlotsTemp)
                        textSelectedTimes.text = selectedSlots.joinToString(", ")
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }

            // 갤러리 사진 선택 버튼
            btnSelectPhoto.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, REQUEST_PICK_IMAGE)
            }
            // 카메라 사진 촬영 버튼
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

            // 선수 추가 Dialog 생성
            AlertDialog.Builder(requireContext())
                .setTitle("선수 추가")
                .setView(dialogView)
                .setPositiveButton("추가") { _, _ ->
                    val name = editName.text.toString().trim()
                    val position = spinnerPosition.selectedItem.toString()
                    val numberText = editNumber.text.toString().trim()
                    val tagInput = editTags.text.toString().trim()

                    val tags = if (tagInput.isNotEmpty()) {
                        tagInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    } else {
                        emptyList()
                    }

                    if (name.isNotEmpty() && numberText.isNotEmpty()) {
                        val number = numberText.toIntOrNull() ?: 0
                        val currentList = sharedViewModel.players.value ?: emptyList()

                        val newPlayer = Player(
                            name = name,
                            position = position,
                            number = number,
                            availableSlots = selectedSlots,
                            photoResId = if (dialogImageUri == null) R.drawable.playerdefault else 0,
                            uri = dialogImageUri,
                            tag = tags
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
                position = "FW",
                number = 9,
                availableSlots = listOf("2025-07-05 08:00", "2025-07-07 09:00"),
                photoResId = R.drawable.playerdefault,
                tag = listOf("성실", "젊은피", "빠른발")
            ),
            Player(
                name = "Alex Johnson",
                position = "MF",
                number = 8,
                availableSlots = listOf("2025-07-06 08:00", "2025-07-07 08:00"),
                photoResId = R.drawable.playerdefault,
                tag = listOf("성실", "젊은피", "빠른발")
            ),
            Player(
                name = "Emily Davis",
                position = "DF",
                number = 4,
                availableSlots = listOf("2025-07-05 08:00", "2025-07-08 09:00"),
                photoResId = R.drawable.playerdefault,
                tag = listOf("성실", "철벽", "빠른발")
            ),
            Player(
                name = "Michael Lee",
                position = "GK",
                number = 1,
                availableSlots = listOf("2025-07-06 09:00", "2025-07-08 09:00"),
                photoResId = R.drawable.playerdefault,
                tag = listOf("노련", "거미손")
            )
        )
    }
}
