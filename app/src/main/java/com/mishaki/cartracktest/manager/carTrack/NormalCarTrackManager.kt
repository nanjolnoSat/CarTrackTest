package com.mishaki.cartracktest.manager.carTrack

import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread

/**
 * 边行驶边画线
 */
open class NormalCarTrackManager(baiduMap: BaiduMap, carIcon: BitmapDescriptor) : CarTrackManager(baiduMap, carIcon) {
    private var firstIndex = 0
    private var secondIndex = 0
    protected var moveLatLngList = ArrayList<ArrayList<LatLng>>()
    private var lastMarker: Marker? = null
    private var lastSmallLine: Polyline? = null
    private var lastLine: Polyline? = null
    private lateinit var lastLatLng: LatLng

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
        asyncTask = threadController.async {
            isRunning = true
            isStop = false
            if (firstIndex == 0 && secondIndex == 0) {
                lastMarker?.remove()
                lastSmallLine?.remove()
                lastLine?.remove()
                val ood = generateCarMarker(actualLatLngList[0], actualLatLngList[1])
                lastMarker = baiduMap.addOverlay(ood) as Marker
                lastLatLng = actualLatLngList[0]
                val u = MapStatusUpdateFactory.newLatLngZoom(actualLatLngList[0], animateZoom)
                baiduMap.animateMapStatus(u)
            }
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
                    moveCar(moveLatLngList[i], i)
                    //到了这里表示已经完成了2个点之间的移动
                    lastMarker!!.rotate = carRotation(actualLatLngList[i], actualLatLngList[i + 1])
                    firstIndex = i
                    secondIndex = 0
                } catch (e: MoveCarStopException) {
                    return@async
                }
            }
            //如果最后一个点不在for外面判断的话,那每次循环都要在for里面判断是否需要旋转
            //车辆移动本来就是一个很耗性能的操作,所以尽量减少里面不必要的判断
            try {
                if (isStop) {
                    return@async
                }
                if (isPause) {
                    isPause = false
                } else {
                    Thread.sleep(sleepTime)
                }
                moveCar(moveLatLngList.last(), actualLatLngList.lastIndex)
                carMoveFinish()
            } catch (e: MoveCarStopException) {//当抛异常表示在这个时间内 暂停/停止 过
                return@async
            }
        }
    }

    private fun carMoveFinish() {
        firstIndex = 0
        secondIndex = 0
        isRunning = false
        threadController.uiThread {
            onMoveFinishListener?.onMoveFinish()
        }
    }

    //使用抛异常的方式可以减少start里面判断isStop的次数,从而提升for的效率
    @Throws(MoveCarStopException::class)
    private fun moveCar(list: ArrayList<LatLng>, index: Int) {
        var firstLine: Polyline? = null
        if (secondIndex == 0) {
            lastMarker!!.position = list[0]
            val line = generatePolylineOptions(arrayListOf(lastLatLng, list[0]))
            lastLatLng = list[0]
            firstLine = baiduMap.addOverlay(line) as Polyline
        }
        if (list.size == 1) {
            val latLngList = (0..index).map { actualLatLngList[it] }
            val line = generatePolylineOptions(latLngList)
            val tempLine = baiduMap.addOverlay(line) as Polyline
            firstLine?.remove()
            lastLine?.remove()
            lastLine = tempLine
        } else {
            val lineList = ArrayList<Polyline>()
            if (firstLine != null) {
                lineList.add(firstLine)
            }
            try {
                for (i in secondIndex + 1 until list.size) {
                    if (isStop) {
                        return
                    }
                    Thread.sleep(sleepTime)
                    val line = generatePolylineOptions(arrayListOf(lastLatLng, list[i]))
                    lastMarker!!.position = list[i]
                    lastLatLng = list[i]
                    secondIndex = i
                    lineList.add(baiduMap.addOverlay(line) as Polyline)
                }
            } finally {
                lastSmallLine?.remove()
                if (!isStop) {
                    val latLngList = (0..index).map { actualLatLngList[it] }
                    val line = generatePolylineOptions(latLngList)
                    val tempLine = baiduMap.addOverlay(line) as Polyline
                    lineList.forEach { it.remove() }
                    lastLine?.remove()
                    lastLine = tempLine
                    lastSmallLine = null
                } else {
                    lineList.forEach { it.remove() }
                    val size = lineList.size
                    val line: PolylineOptions
                    if (lastSmallLine == null) {
                        line = generatePolylineOptions(arrayListOf(lineList[0].points[0], lineList[size - 1].points[1]))
                    } else {
                        line = generatePolylineOptions(arrayListOf(lastSmallLine!!.points[0], lineList[size - 1].points[1]))
                    }
                    lastSmallLine = baiduMap.addOverlay(line) as Polyline
                    throw MoveCarStopException()
                }
            }
        }
    }

    override fun resetIndex() {
        firstIndex = 0
        secondIndex = 0
    }
}