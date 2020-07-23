package com.mishaki.cartracktest.manager.carTrack

import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.Marker
import com.baidu.mapapi.model.LatLng
import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread

/**
 * 仅行驶
 */
open class MoveCarTrackManager(baiduMap: BaiduMap, carIcon: BitmapDescriptor) : CarTrackManager(baiduMap, carIcon) {
    protected var firstIndex = 0
    protected var secondIndex = 0
    private var lastMarker: Marker? = null
    protected var moveLatLngList = ArrayList<ArrayList<LatLng>>()

    init {
        removeUiSetting()
    }

    override fun onSetTrackLatLngListFinish() {
        moveLatLngList = splitLatLng4MoveDistance(actualLatLngList, moveDistance)
    }

    override fun start() {
        if (isRunning) {
            return
        }
        isRunning = true
        asyncTask = threadController.async {
            if (firstIndex == 0) {
                lastMarker?.remove()
                val ood = generateCarMarker(actualLatLngList[0], actualLatLngList[1])
                lastMarker = baiduMap.addOverlay(ood) as Marker
                val u = MapStatusUpdateFactory.newLatLngZoom(actualLatLngList[0], animateZoom)
                baiduMap.animateMapStatus(u)
            }
            isStop = false
            for (i in firstIndex + 1 until actualLatLngList.size - 1) {
                try {
                    if (isStop) {
                        return@async
                    }
                    if (isPause) {
                        isPause = false
                    } else {
                        Thread.sleep(sleepTime)
                    }
                    moveCar(moveLatLngList[i])
                    lastMarker!!.rotate = carRotation(actualLatLngList[i], actualLatLngList[i + 1])
                    firstIndex = i
                    secondIndex = 0
                } catch (e: MoveCarStopException) {
                    return@async
                }
            }
            try {
                moveCar(moveLatLngList[actualLatLngList.lastIndex])
                moveCarFinish()
            } catch (e: MoveCarStopException) {
                return@async
            }
        }
    }

    @Throws(MoveCarStopException::class)
    private fun moveCar(list: ArrayList<LatLng>) {
        lastMarker?.position = list[0]
        for (i in secondIndex + 1 until list.size) {
            if (isStop) {
                throw MoveCarStopException()
            }
            Thread.sleep(sleepTime)
            lastMarker!!.position = list[i]
            secondIndex = i
        }
    }

    private fun moveCarFinish() {
        firstIndex = 0
        secondIndex = 0
        isRunning = false
        threadController.uiThread {
            onMoveFinishListener?.onMoveFinish()
        }
    }

    override fun resetIndex() {
        firstIndex = 0
        secondIndex = 0
    }
}