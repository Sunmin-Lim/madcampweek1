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
import androidx.recyclerview.widget.StaggeredGridLayoutManager
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
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
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
                Photo(R.drawable.pic1, description = "상대를 완벽하게 제치며 마치 춤을 추듯 공을 다루는 화려한 드리블 장면으로, 관중의 탄성을 자아내는 순간", tags = "드리블, 개인기, 예술적인 플레이, 공격 전개, 축구 경기"),
                Photo(R.drawable.pic2, description = "강력한 발리 슈팅이 골망을 흔들며 상대 골키퍼가 손쓸 틈도 없이 득점으로 이어지는 박진감 넘치는 순간", tags = "슈팅, 골 장면, 스트라이커, 강력한 공격, 득점"),
                Photo(R.drawable.pic3, description = "힘든 경기를 뒤집고 골을 넣은 후, 팀 동료들과 함께 뜨겁게 포옹하며 기쁨을 만끽하는 감동적인 세레머니", tags = "세레머니, 팀워크, 감정 표현, 승리의 순간, 동료애"),
                Photo(R.drawable.pic4, description = "몸을 날려 극적으로 상대의 결정적인 슈팅을 막아내는 골키퍼의 순발력과 집중력이 빛나는 장면", tags = "골키퍼, 슈퍼세이브, 수비, 반사신경, 경기 집중"),
                Photo(R.drawable.pic5, description = "경기장의 중심에서 패스를 주고받으며 팀의 공격을 조율하고 템포를 만들어가는 지능적인 미드필더의 플레이", tags = "미드필더, 경기 운영, 패스 마스터, 중원 싸움, 전략"),
                Photo(R.drawable.pic6, description = "경기 전 전술판을 보며 서로의 역할을 확인하고 굳게 손을 맞잡아 결의를 다지는 선수들의 진지한 표정", tags = "전술회의, 작전, 팀워크, 준비 과정, 결속력"),
                Photo(R.drawable.pic7, description = "빠른 스피드로 사이드라인을 돌파한 뒤 정확한 크로스를 올려 동료에게 득점 기회를 만드는 장면", tags = "윙어 플레이, 크로스, 어시스트, 측면 돌파, 공격 루트"),
                Photo(R.drawable.pic8, description = "상대의 빠른 돌파를 저지하기 위해 과감하게 다가가 정확한 태클로 공을 빼앗는 수비수의 헌신", tags = "태클, 수비 기술, 몸싸움, 집중 방어, 저지"),
                Photo(R.drawable.pic9, description = "수비를 허무는 빠른 역습 전개로 상대 진영을 순식간에 무너뜨리고 결정적인 찬스를 만들어내는 모습", tags = "역습, 스피드, 전환 플레이, 공격 찬스, 조직력"),
                Photo(R.drawable.pic10, description = "골대를 향한 긴장감이 감도는 순간, 코너킥 키커가 집중하며 날카로운 킥을 준비하는 모습", tags = "코너킥, 세트피스, 기회 창출, 집중력, 전술"),
                Photo(R.drawable.pic11, description = "두 선수가 하늘로 뛰어올라 공중볼을 차지하기 위해 몸을 날리는 치열한 헤더 경합의 장면", tags = "헤더, 공중볼 경합, 몸싸움, 피지컬, 경쟁"),
                Photo(R.drawable.pic12, description = "사이드라인에서 열정적으로 선수들에게 지시를 내리며 전술 변화를 주는 감독의 카리스마", tags = "감독, 전략 지시, 전술 변화, 팀 관리, 리더십"),
                Photo(R.drawable.pic13, description = "경기 전 차분하게 몸을 풀고 스트레칭을 하며 오늘의 경기에 집중하는 선수들의 준비된 모습", tags = "워밍업, 스트레칭, 준비 운동, 경기 집중, 컨디션 관리"),
                Photo(R.drawable.pic14, description = "상대의 돌파를 저지하기 위해 과감하게 뻗은 발끝이 공을 향해 미끄러져 들어가는 박진감 넘치는 슬라이딩 태클", tags = "슬라이딩 태클, 수비 기술, 저지, 강한 수비, 경기력"),
                Photo(R.drawable.pic15, description = "모두의 시선이 집중되는 순간, 예리하게 궤적을 그리는 프리킥을 준비하는 키커의 집중력", tags = "프리킥, 세트피스, 슈팅, 골 기회, 전략"),
                Photo(R.drawable.pic16, description = "경기장을 가득 메운 관중들이 함성과 응원가로 선수들을 북돋우며 뜨거운 열기를 뿜어내는 모습", tags = "관중, 응원, 팬 문화, 경기장 분위기, 열정"),
                Photo(R.drawable.pic17, description = "모두가 숨죽인 가운데 준비되는 페널티킥 순간의 긴장감과 골키퍼의 날카로운 시선", tags = "페널티킥, 긴장감, 골키퍼, 심리전, 압박"),
                Photo(R.drawable.pic18, description = "삼각형 패스를 주고받으며 상대 수비를 끌어내고 공간을 만들어내는 정교하고 조직적인 팀플레이", tags = "패스워크, 삼각형 움직임, 전술적 플레이, 팀워크, 창의성"),
                Photo(R.drawable.pic19, description = "거친 파울 장면 이후 경고 카드를 꺼내 보이며 경기의 질서를 유지하려는 심판의 단호함", tags = "심판, 경고, 규칙, 페어플레이, 경기 통제"),
                Photo(R.drawable.pic20, description = "경기가 끝난 뒤 함께 모여 기쁨을 나누며 카메라를 향해 웃는 팀 전체의 끈끈한 단체 사진", tags = "팀사진, 단체사진, 우정, 단합, 팀 정신")
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
