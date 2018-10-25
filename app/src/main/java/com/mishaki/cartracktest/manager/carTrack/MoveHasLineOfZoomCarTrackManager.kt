package com.mishaki.cartracktest.manager.carTrack

import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptor

/**
 *  先将行驶路线画出来,再移动.移动的距离根据缩放等级计算
 */
class MoveHasLineOfZoomCarTrackManager(baiduMap: BaiduMap, carIcon: BitmapDescriptor) : MoveHasLineTrackMananger(baiduMap, carIcon) {
    private var currentMoveDistance = moveDistance.toFloat()
    private var lastMoveDistance = moveDistance.toFloat()

    var reduceTimes = 4f

    init {
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