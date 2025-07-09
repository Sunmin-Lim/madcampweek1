package com.example.madcamp1

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import kotlinx.coroutines.launch

class NaverMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap

    private val sharedViewModel: SharedViewModel by activityViewModels()

    // 마커 관리
    private val playerMarkerList = mutableListOf<Marker>()
    private val responseMarkerList = mutableListOf<Marker>()
    private var centerMarker: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_naver_map, container, false)
        mapView = view.findViewById(R.id.naver_map_view)
        mapView.getMapAsync(this)

        view.findViewById<Button>(R.id.btnSetLocation).setOnClickListener {
            Toast.makeText(requireContext(), "지도를 클릭해서 위치를 추가하세요", Toast.LENGTH_SHORT).show()
            enableMapClickToAddLocation()
        }

        view.findViewById<Button>(R.id.btnFindCenter).setOnClickListener {
            searchFromCenter()
        }

        return view
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        showPlayerMarkers()
        showCenterMarker()
    }

    private fun showPlayerMarkers() {
        val players = sharedViewModel.players.value ?: return
        players.filter { it.latitude != null && it.longitude != null }.forEach { player ->
            Marker().apply {
                position = LatLng(player.latitude!!, player.longitude!!)
                captionText = player.name
                map = naverMap
                playerMarkerList.add(this)
            }
        }
    }

    private fun showCenterMarker() {
        if (playerMarkerList.isEmpty()) {
            centerMarker?.map = null
            centerMarker = null
            Toast.makeText(requireContext(), "중간 지점을 구하기 위해 마커를 추가해주세요.", Toast.LENGTH_SHORT).show()
        }
        else {
            val avgLat = playerMarkerList.map { it.position.latitude }.average()
            val avgLng = playerMarkerList.map { it.position.longitude }.average()

            centerMarker?.map = null
            centerMarker = Marker().apply {
                position = LatLng(avgLat, avgLng)
                captionText = "중간 지점"
                iconTintColor = Color.RED
                map = naverMap
            }
            naverMap.moveCamera(CameraUpdate.scrollTo(LatLng(avgLat, avgLng)))
        }
    }

    private fun enableMapClickToAddLocation() {
        naverMap.setOnMapClickListener { _, latLng ->
            val marker = Marker().apply {
                position = latLng
                captionText = "Player ${playerMarkerList.size + 1}"
                iconTintColor = Color.GREEN
                map = naverMap
            }
            playerMarkerList.add(marker)
            showCenterMarker()
        }
    }

    private fun searchFromCenter() {
        Toast.makeText(requireContext(), "추천 축구장 검색", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            Log.d("MapDebug", "API 호출 시작, 검색 기준 - 위도: ${centerMarker?.position?.latitude}, 경도: ${centerMarker?.position?.longitude}")
            val response = RetrofitClient.api.searchPlaces(
                clientId = "pqUSUqL5V_eTSJjG6KWu",
                clientSecret = "etmR3VxoS2",
                query = "축구장"
            )

            if (response.isSuccessful) {
                // 기존 마커 삭제
                for (marker in responseMarkerList) {
                    marker.map = null
                }
                // 리스트도 비워줌
                responseMarkerList.clear()

                val items = response.body()?.items ?: emptyList()
                val start = response.body()?.start
                Log.d("MapDebug", "검색된 장소 개수: ${items.size}")
                Log.d("MapDebug", "검색 시작 위치: $start")
                items.forEachIndexed { index, item ->
                    Log.d("MapDebug", "[$index] ${item.title} → 위도: ${item.mapy / 1e7}, 경도: ${item.mapx / 1e7}")
                    Marker().apply {
                        position = LatLng(item.mapy / 1e7, item.mapx / 1e7)
                        captionText = HtmlCompat.fromHtml(item.title, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                        iconTintColor = Color.BLUE
                        map = naverMap
                        responseMarkerList.add(this)
                    }
                }
            } else {
                Log.e("MapDebug", "API 오류 코드: ${response.code()}")
            }
        }
    }

    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { mapView.onPause(); super.onPause() }
    override fun onStop() { mapView.onStop(); super.onStop() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
    override fun onDestroyView() { mapView.onDestroy(); super.onDestroyView() }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
