package com.mishaki.cartracktest.manager.carTrack

import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptor

/**
 * 根据缩放等级计算车辆每次移动的距离
 */
class NormalOfZoomManager(baiduMap: BaiduMap, carIcon: BitmapDescriptor) : NormalCarTrackManager(baiduMap, carIcon) {
    private var lastMoveDistance = moveDistance.toFloat()
    private var currentMoveDistance = moveDistance.toFloat()

    var reduceTimes = 4f

    init {
        removeUiSetting()
        animateZoom = 16f
        baiduMap.onZoomLevelChange {
            currentMoveDistance = it / reduceTimes
            if (lastMoveDistance != currentMoveDistance) {
                moveLatLngList = splitLatLng4MoveDistance(actualLatLngList, currentMoveDistance.toInt())
            }
            lastMoveDistance = currentMoveDistance
        }
    }

}