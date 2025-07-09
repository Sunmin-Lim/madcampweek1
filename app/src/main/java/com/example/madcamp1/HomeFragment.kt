package com.example.madcamp1

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.flexbox.FlexboxLayout
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapter

    private val REQUEST_PICK_IMAGE = 2001
    private val REQUEST_CAMERA_IMAGE = 2002
    private var imageProfilePreview: ImageView? = null
    private var dialogImageUri: Uri? = null

    private val sharedViewModel: SharedViewModel by activityViewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    private var currentWeekStart: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY)

    @RequiresApi(Build.VERSION_CODES.O)
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

        val searchInput = view.findViewById<EditText>(R.id.searchInput)
        searchInput.addTextChangedListener {
            val rawQuery = it.toString().trim().lowercase()
            val queries = rawQuery.split("[,\\s]+".toRegex()).filter { it.isNotBlank() }
            val players = sharedViewModel.players.value ?: emptyList()

            val filtered = players.filter { player ->
                queries.all { query ->
                    when {
                        query.startsWith("#") -> {
                            val tagQuery = query.removePrefix("#")
                            player.tag.any { tag -> tag.lowercase().contains(tagQuery) }
                        }

                        else -> player.name.lowercase().contains(query)
                    }
                }
            }

            recyclerView.adapter = MyAdapter(filtered.toMutableList()) { player, index ->
                showPlayerDetailDialog(player, index)
            }
        }

        val fab = view.findViewById<FloatingActionButton>(R.id.addProfileButton)
        fab.setOnClickListener { showAddPlayerDialog() }

        sharedViewModel.players.observe(viewLifecycleOwner) { players ->
            adapter = MyAdapter(players.toMutableList()) { player, index ->
                showPlayerDetailDialog(player, index)
            }
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
                    imageProfilePreview?.setImageURI(uri)  // ✅ 여기!
                }
            }
            REQUEST_CAMERA_IMAGE -> {
                dialogImageUri?.let { uri ->
                    imageProfilePreview?.setImageURI(uri)  // ✅ 여기!
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAddPlayerDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_player, null)

        dialogView.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                val focusedView = dialogView.findFocus()
                if (focusedView is EditText) {
                    focusedView.clearFocus()
                    imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
                }
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
        val positions = listOf("FW", "MF", "DF", "GK")
        spinnerPosition.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, positions)

        btnPickTime.setOnClickListener {
            showPickTimeDialog(selectedSlots, textSelectedTimes)
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
                val tagInput = editTags.text.toString().trim()

                val tags = if (tagInput.isNotEmpty()) {
                    tagInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                } else emptyList()

                if (name.isNotEmpty() && numberText.isNotEmpty()) {
                    val number = numberText.toIntOrNull() ?: 0
                    val currentList = sharedViewModel.players.value ?: emptyList()

                    val newPlayer = Player(
                        name = name,
                        position = position,
                        number = number,
                        availableSlots = selectedSlots,
                        photoResId = if (dialogImageUri == null) R.drawable.player6 else 0,
                        uri = dialogImageUri,
                        tag = tags
                    )
                    sharedViewModel.setPlayers(currentList + newPlayer)
                    dialogImageUri = null
                } else {
                    Toast.makeText(requireContext(), "모든 항목을 입력하세요", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showPickTimeDialog(selectedSlots: MutableList<String>, textSelectedTimes: TextView) {
        val gridView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_pick_time, null)
        val gridContainer = gridView.findViewById<LinearLayout>(R.id.gridContainer)
        val buttonPrevWeek = gridView.findViewById<ImageButton>(R.id.buttonPrevWeek)
        val buttonNextWeek = gridView.findViewById<ImageButton>(R.id.buttonNextWeek)
        val textWeekRange = gridView.findViewById<TextView>(R.id.textWeekRange)

        var weekStart = currentWeekStart
        var isDragging = false
        var dragAddMode = true

        fun toggleCell(slotKey: String, cell: TextView, mode: Boolean) {
            if (mode) {
                if (!selectedSlots.contains(slotKey)) selectedSlots.add(slotKey)
            } else {
                selectedSlots.remove(slotKey)
            }
            cell.background = GradientDrawable().apply {
                setColor(if (selectedSlots.contains(slotKey)) Color.GREEN else Color.LTGRAY)
                setStroke(2, Color.BLACK)
            }
        }

        fun getWeekDates(): List<LocalDate> =
            (0..6).map { weekStart.plusDays(it.toLong()) }

        fun updateWeekLabel(weekDates: List<LocalDate>) {
            val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
            textWeekRange.text = "${formatter.format(weekDates.first())} ~ ${formatter.format(weekDates.last())}"
        }

        fun renderGrid(dates: List<LocalDate>) {
            gridContainer.removeAllViews()

            val headerRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            headerRow.addView(TextView(requireContext()).apply {
                text = ""
                width = 90
            })

            val dateHeaders = dates.map { date ->
                val datePart = date.format(DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH))
                val dayPart = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                "$datePart\n$dayPart"
            }

            dateHeaders.forEach { headerText ->
                headerRow.addView(TextView(requireContext()).apply {
                    text = headerText
                    width = 110
                    gravity = Gravity.CENTER
                    textSize = 10f
                    setPadding(4, 4, 4, 4)
                })
            }

            gridContainer.addView(headerRow)

            val blankRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            blankRow.addView(TextView(requireContext()).apply {
                text = ""
                width = 90
                height = 50
                setBackgroundColor(Color.WHITE)
            })
            dates.forEach { _ ->
                blankRow.addView(TextView(requireContext()).apply {
                    width = 100
                    height = 50
                    setBackgroundColor(Color.WHITE)
                })
            }
            gridContainer.addView(blankRow)

            val times = mutableListOf<String>().apply {
                for (hour in 8..22) {
                    add(String.format("%02d:00", hour))
                    if (hour < 22) add(String.format("%02d:30", hour))
                }
            }

            for ((index, timeLabel) in times.withIndex()) {
                val row = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                }

                val timeText = TextView(requireContext()).apply {
                    text = timeLabel
                    width = 90
                    textSize = 10f
                    gravity = Gravity.TOP
                }
                row.addView(timeText)

                if (index == times.lastIndex) {
                    dates.forEach { _ ->
                        row.addView(TextView(requireContext()).apply {
                            width = 105
                            height = 40
                            setBackgroundColor(Color.WHITE)
                        })
                    }
                } else {
                    dates.forEach { date ->
                        val slotKey = "${date} $timeLabel"
                        val cell = TextView(requireContext()).apply {
                            width = 105
                            height = 55
                            gravity = Gravity.CENTER
                            background = GradientDrawable().apply {
                                setColor(if (selectedSlots.contains(slotKey)) Color.GREEN else Color.LTGRAY)
                                setStroke(2, Color.BLACK)
                            }
                        }
                        cell.setOnTouchListener { _, event ->
                            when (event.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    isDragging = true
                                    dragAddMode = !selectedSlots.contains(slotKey)
                                    toggleCell(slotKey, cell, dragAddMode)
                                    true
                                }
                                MotionEvent.ACTION_MOVE -> {
                                    if (isDragging) toggleCell(slotKey, cell, dragAddMode)
                                    true
                                }
                                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                    isDragging = false
                                    true
                                }
                                else -> false
                            }
                        }
                        row.addView(cell)
                    }
                }
                gridContainer.addView(row)
            }
        }

        fun refreshWeek() {
            val dates = getWeekDates()
            updateWeekLabel(dates)
            renderGrid(dates)
        }

        buttonPrevWeek.setOnClickListener {
            weekStart = weekStart.minusWeeks(1)
            refreshWeek()
        }
        buttonNextWeek.setOnClickListener {
            weekStart = weekStart.plusWeeks(1)
            refreshWeek()
        }

        refreshWeek()

        AlertDialog.Builder(requireContext())
            .setTitle("선호 시간대 선택")
            .setView(gridView)
            .setPositiveButton("확인") { _, _ ->
                textSelectedTimes.text = selectedSlots.joinToString(", ")
            }
            .setNegativeButton("취소", null)
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showPlayerDetailDialog(player: Player, index: Int) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_player_detail, null)

        val photoView = dialogView.findViewById<ImageView>(R.id.detailPlayerPhoto)
        val nameView = dialogView.findViewById<TextView>(R.id.detailPlayerName)
        val positionNumberView = dialogView.findViewById<TextView>(R.id.detailPlayerPositionNumber)
        val tagContainer = dialogView.findViewById<FlexboxLayout>(R.id.detailPlayerTagContainer)

        // 데이터 바인딩
        player.uri?.let {
            photoView.setImageURI(it)
        } ?: photoView.setImageResource(player.photoResId)

        nameView.text = player.name
        positionNumberView.text = "${player.position} · ${player.number}"

        tagContainer.removeAllViews()
        for (tagText in player.tag) {
            val tagView = TextView(requireContext()).apply {
                text = tagText
                setPadding(24, 12, 24, 12)
                background = ContextCompat.getDrawable(context, R.drawable.player_tag)
                setTextColor(Color.BLACK)
                textSize = 14f
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 8, 8, 8)
                }
            }
            tagContainer.addView(tagView)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("선수 정보")
            .setView(dialogView)
            .setPositiveButton("수정") { _, _ ->
                showEditPlayerDialog(player, index)
            }
            .setNegativeButton("닫기", null)
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showEditPlayerDialog(player: Player, index: Int) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_player, null)
        dialogView.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                val focusedView = dialogView.findFocus()
                if (focusedView is EditText) {
                    focusedView.clearFocus()
                    imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
                }
                view.performClick()
            }
            false
        }

        val imageProfile = dialogView.findViewById<ImageView>(R.id.imageProfile)
        val btnSelectPhoto = dialogView.findViewById<Button>(R.id.btnSelectPhoto)
        val btnTakePhoto = dialogView.findViewById<Button>(R.id.btnTakePhoto)
        val editName = dialogView.findViewById<EditText>(R.id.editPlayerName)
        val spinnerPosition = dialogView.findViewById<Spinner>(R.id.spinnerPlayerPosition)
        val editNumber = dialogView.findViewById<EditText>(R.id.editPlayerNumber)
        val btnPickTime = dialogView.findViewById<Button>(R.id.btnPickTime)
        btnPickTime.text = "선호 시간대 수정"
        val textSelectedTimes = dialogView.findViewById<TextView>(R.id.textSelectedTimes)
        val editTags = dialogView.findViewById<EditText>(R.id.editPlayerTags)

        val selectedSlots = player.availableSlots.toMutableList()
        val localUri = player.uri
        val positions = listOf("FW", "MF", "DF", "GK")
        spinnerPosition.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, positions)

        // 초기값 채워 넣기
        if (player.uri != null) {
            imageProfile.setImageURI(player.uri)
        } else {
            imageProfile.setImageResource(player.photoResId)
        }
        editName.setText(player.name)
        spinnerPosition.setSelection(listOf("FW", "MF", "DF", "GK").indexOf(player.position))
        editNumber.setText(player.number.toString())
        editTags.setText(player.tag.joinToString(", "))

        textSelectedTimes.text = selectedSlots.joinToString(", ")

        imageProfilePreview = imageProfile
        dialogImageUri = localUri

        btnPickTime.setOnClickListener {
            showPickTimeDialog(selectedSlots, textSelectedTimes)
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

        // 다이얼로그 생성
        AlertDialog.Builder(requireContext())
            .setTitle("선수 정보 수정")
            .setView(dialogView)
            .setPositiveButton("저장") { _, _ ->
                val name = editName.text.toString().trim()
                val position = spinnerPosition.selectedItem.toString()
                val number = editNumber.text.toString().toIntOrNull() ?: return@setPositiveButton
                val tags = editTags.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }

                val updatedPlayer = Player(
                    name = name,
                    position = position,
                    number = number,
                    availableSlots = selectedSlots,
                    photoResId = if (dialogImageUri == null) R.drawable.player6 else 0,
                    uri = dialogImageUri,
                    tag = tags
                )

                // ViewModel 리스트 갱신
                val updatedList = sharedViewModel.players.value?.toMutableList() ?: return@setPositiveButton
                updatedList[index] = updatedPlayer
                sharedViewModel.setPlayers(updatedList)

                dialogImageUri = null
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun getPlayers(): List<Player> = listOf(
        Player("김민수", "FW", 9, listOf("2025-07-10 09:00", "2025-07-12 15:00", "2025-07-13 15:00", "2025-07-11 08:00"), null, listOf("결정", "민첩"), R.drawable.playerdefault, 37.4979, 127.0276),
        Player("이준호", "MF", 8, listOf("2025-07-10 09:00", "2025-07-11 09:00", "2025-07-13 15:00", "2025-07-12 14:00"), null, listOf("패스", "지능"), R.drawable.playerdefault, 37.5566, 126.9236),
        Player("박지훈", "DF", 4, listOf("2025-07-10 09:00", "2025-07-11 09:00", "2025-07-12 15:00", "2025-07-12 08:00"), null, listOf("강인", "집중"), R.drawable.playerdefault, 37.5145, 127.1059),
        Player("최유진", "GK", 1, listOf("2025-07-11 09:00", "2025-07-12 15:00", "2025-07-13 15:00", "2025-07-10 08:30"), null, listOf("반사", "침착"), R.drawable.playerdefault, 37.6544, 127.0565),
        Player("정하늘", "MF", 7, listOf("2025-07-10 09:00", "2025-07-11 09:00", "2025-07-13 15:00", "2025-07-11 10:00"), null, listOf("시야", "연결"), R.drawable.playerdefault, 37.5509, 126.8495),
        Player("한도윤", "FW", 11, listOf("2025-07-10 09:00", "2025-07-11 09:00", "2025-07-12 15:00", "2025-07-13 16:00"), null, listOf("위치", "속도"), R.drawable.playerdefault, 37.5039, 126.9483),
        Player("오서준", "DF", 5, listOf("2025-07-10 09:00", "2025-07-13 15:00", "2025-07-12 15:00", "2025-07-11 14:00"), null, listOf("체력", "헤딩"), R.drawable.playerdefault, 37.4781, 126.9516),
        Player("서예빈", "MF", 6, listOf("2025-07-11 09:00", "2025-07-13 15:00", "2025-07-12 15:00", "2025-07-13 14:00"), null, listOf("드리블", "유연"), R.drawable.playerdefault, 37.5894, 127.0167),
        Player("문가영", "FW", 10, listOf("2025-07-10 09:00", "2025-07-11 09:00", "2025-07-13 15:00", "2025-07-13 16:00"), null, listOf("감각", "슛팅"), R.drawable.playerdefault, 37.6176, 126.9227),
        Player("이하늘", "DF", 2, listOf("2025-07-10 09:00", "2025-07-12 15:00", "2025-07-11 09:00", "2025-07-10 10:00"), null, listOf("냉정", "수비"), R.drawable.playerdefault, 37.4836, 127.0326),
        Player("장수진", "GK", 13, listOf("2025-07-11 09:00", "2025-07-13 15:00", "2025-07-12 15:00", "2025-07-13 14:30"), null, listOf("순발", "캐치"), R.drawable.playerdefault, 37.5645, 126.9979),
        Player("윤지호", "MF", 15, listOf("2025-07-10 09:00", "2025-07-11 09:00", "2025-07-12 15:00", "2025-07-11 08:30"), null, listOf("지능", "연결"), R.drawable.playerdefault, 37.6688, 127.0477),
        Player("홍유라", "FW", 18, listOf("2025-07-10 09:00", "2025-07-12 15:00", "2025-07-13 15:00", "2025-07-11 07:30"), null, listOf("침투", "속도"), R.drawable.playerdefault, 37.5169, 126.8665),
        Player("배서연", "DF", 3, listOf("2025-07-11 09:00", "2025-07-12 15:00", "2025-07-13 15:00", "2025-07-10 07:30"), null, listOf("태클", "기동"), R.drawable.playerdefault, 37.5720, 126.9794),
        Player("신지섭", "MF", 14, listOf("2025-07-10 09:00", "2025-07-11 09:00", "2025-07-12 15:00", "2025-07-13 15:00"), null, listOf("전개", "지구"), R.drawable.playerdefault, 37.5985, 127.0937),
        Player("조현우", "GK", 12, listOf("2025-07-11 09:00", "2025-07-12 15:00", "2025-07-13 15:00", "2025-07-10 16:00"), null, listOf("방어", "점프"), R.drawable.playerdefault, 37.4954, 126.8874),
        Player("임은지", "FW", 19, listOf("2025-07-10 09:00", "2025-07-13 15:00", "2025-07-12 15:00", "2025-07-11 09:30"), null, listOf("골결", "냉정"), R.drawable.playerdefault, 37.6396, 127.0256),
        Player("강태호", "DF", 6, listOf("2025-07-10 09:00", "2025-07-11 09:00", "2025-07-13 15:00", "2025-07-11 08:00"), null, listOf("성실", "꾸준"), R.drawable.playerdefault, 37.5264, 126.8962),
        Player("전소현", "MF", 16, listOf("2025-07-10 09:00", "2025-07-12 15:00", "2025-07-11 09:00", "2025-07-13 15:00"), null, listOf("정확", "창의"), R.drawable.playerdefault, 37.5794, 126.9368),
        Player("권지민", "FW", 17, listOf("2025-07-10 09:00", "2025-07-11 09:00", "2025-07-13 15:00", "2025-07-13 16:00"), null, listOf("속공", "기민"), R.drawable.playerdefault, 37.5324, 126.9908)
    )
}