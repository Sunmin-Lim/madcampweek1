package com.example.madcamp1

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
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

            recyclerView.adapter = MyAdapter(players.toMutableList()) { player ->
                showPlayerDetailDialog(player)
            }
        }

        val fab = view.findViewById<FloatingActionButton>(R.id.addProfileButton)
        fab.setOnClickListener { showAddPlayerDialog() }

        sharedViewModel.players.observe(viewLifecycleOwner) { players ->
            adapter = MyAdapter(players.toMutableList()) { player ->
                showPlayerDetailDialog(player)
            }
            recyclerView.adapter = adapter
        }

        return view
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
                setColor(if (selectedSlots.contains(slotKey)) Color.GREEN else Color.WHITE)
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
                    width = 100
                    gravity = Gravity.CENTER
                    textSize = 12f
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
                    gravity = Gravity.CENTER
                }
                row.addView(timeText)

                if (index == times.lastIndex) {
                    dates.forEach { _ ->
                        row.addView(TextView(requireContext()).apply {
                            width = 100
                            height = 50
                            setBackgroundColor(Color.WHITE)
                        })
                    }
                } else {
                    dates.forEach { date ->
                        val slotKey = "${date} $timeLabel"
                        val cell = TextView(requireContext()).apply {
                            width = 100
                            height = 50
                            gravity = Gravity.CENTER
                            background = GradientDrawable().apply {
                                setColor(if (selectedSlots.contains(slotKey)) Color.GREEN else Color.WHITE)
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

    private fun showPlayerDetailDialog(player: Player) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_player_detail, null)

        val photoView = dialogView.findViewById<ImageView>(R.id.detailPlayerPhoto)
        val nameView = dialogView.findViewById<TextView>(R.id.detailPlayerName)
        val positionNumberView = dialogView.findViewById<TextView>(R.id.detailPlayerPositionNumber)
        val tagsView = dialogView.findViewById<TextView>(R.id.detailPlayerTags)
        val availabilityView = dialogView.findViewById<TextView>(R.id.detailPlayerAvailability)

        // 데이터 바인딩
        player.uri?.let {
            photoView.setImageURI(it)
        } ?: photoView.setImageResource(player.photoResId)

        nameView.text = player.name
        positionNumberView.text = "${player.position} · ${player.number}"
        tagsView.text = "태그: ${player.tag.joinToString(", ")}"
        availabilityView.text = "가능 시간: ${player.availableSlots.joinToString(", ")}"

        AlertDialog.Builder(requireContext())
            .setTitle("선수 정보")
            .setView(dialogView)
            .setPositiveButton("수정") { _, _ ->
                // 나중에 수정 기능 추가할 위치
                //showEditPlayerDialog(player)
            }
            .setNegativeButton("닫기", null)
            .show()
    }

    private fun getPlayers(): List<Player> = listOf(
        Player("John Smith", "FW", 9, listOf("2025-07-05 08:00", "2025-07-07 09:00"), null, listOf("성실", "젊은피", "빠른발"), R.drawable.playerdefault),
        Player("Alex Johnson", "MF", 8, listOf("2025-07-06 08:00", "2025-07-07 08:00"), null, listOf("성실", "젊은피", "빠른발"), R.drawable.playerdefault),
        Player("Emily Davis", "DF", 4, listOf("2025-07-05 08:00", "2025-07-08 09:00"), null, listOf("성실", "철벽", "빠른발"), R.drawable.playerdefault),
        Player("Michael Lee", "GK", 1, listOf("2025-07-06 09:00", "2025-07-08 09:00"), null, listOf("노련", "거미손"), R.drawable.playerdefault)
    )

}
