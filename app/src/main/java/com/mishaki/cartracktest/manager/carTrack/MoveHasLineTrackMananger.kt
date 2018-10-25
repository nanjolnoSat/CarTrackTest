package com.mishaki.cartracktest.manager.carTrack

import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.Polyline
import com.mishaki.cartracktest.manager.carTrack.MoveCarTrackManager

/**
 * 先画全部的路线再行驶
 */
open class MoveHasLineTrackMananger(baiduMap: BaiduMap, carIcon: BitmapDescriptor) : MoveCarTrackManager(baiduMap, carIcon) {
    private var trackLine: Polyline? = null
    override fun start() {
        if (isRunning) {
            return
        }
        if (firstIndex == 0 && secondIndex == 0) {
            trackLine?.remove()
            val line = generatePolylineOptions(actualLatLngList)
            trackLine = baiduMap.addOverlay(line) as Polyline
        }
        super.start()
    }
}