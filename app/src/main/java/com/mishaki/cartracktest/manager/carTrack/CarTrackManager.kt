package com.mishaki.cartracktest.manager.carTrack

import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.PolylineOptions
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.utils.DistanceUtil
import org.jetbrains.anko.AnkoAsyncContext
import org.jetbrains.anko.async
import java.lang.ref.WeakReference
import java.util.concurrent.Future

/**
 * 车辆移动的基类
 */
abstract class CarTrackManager(protected val baiduMap: BaiduMap, private val canIcon: BitmapDescriptor) {
    //随便写的,但Marker的ZIndex必须比Polyline的ZIndex大
    protected var carMarkerZIndex = -50
    protected var polylineLineZIndex = -100
    var polylineWidth = 10
    var polylineColor = 0xff000000.toInt()
    var moveDistance = 5
    var sleepTime = 20L

    protected val threadController = AnkoAsyncContext(WeakReference(this))
    protected var asyncTask: Future<Unit>? = null
    protected var isStop = false
    protected var isPause = false
    protected var isRunning = false

    protected var actualLatLngList = ArrayList<LatLng>()

    var animateZoom = 14f

    var onMoveFinishListener: OnMoveFinishListener? = null
    /**
     * 建议最后控制一下数据量,过大的话就导致计算不过来
     */
    fun setTrackLatLngList(list: ArrayList<LatLng>, distance: Int = 0) {
        if (list.size <= 1) {
            actualLatLngList = list
            onSetTrackLatLngListFinish()
            return
        }
        if (distance <= 0) {
            actualLatLngList = list
            onSetTrackLatLngListFinish()
            return
        }
        actualLatLngList.clear()
        actualLatLngList.add(list[0])
        (1 until list.size).filter { DistanceUtil.getDistance(list[it - 1], list[it]) > distance }.mapTo(actualLatLngList) { list[it] }
        onSetTrackLatLngListFinish()
    }

    protected open fun onSetTrackLatLngListFinish() {}

    var maxLevel = 19F
        set(maxLevel) {
            field = maxLevel
            baiduMap.setMaxAndMinZoomLevel(maxLevel, minLevel)
        }
    var minLevel = 4F
        set(minLevel) {
            field = minLevel
            baiduMap.setMaxAndMinZoomLevel(maxLevel, minLevel)
        }

    init {
        baiduMap.setMaxAndMinZoomLevel(maxLevel, minLevel)
    }

    companion object {
        /**
         * 边行驶边画线
         */
        @JvmStatic
        fun newNormalInstance(baiduMap: BaiduMap, carIcon: BitmapDescriptor): CarTrackManager {
            return NormalCarTrackManeger(baiduMap, carIcon)
        }

        /**
         * 仅行驶
         */
        @JvmStatic
        fun newMoveInstance(baiduMap: BaiduMap, canIcon: BitmapDescriptor): CarTrackManager {
            return MoveCarTrackManager(baiduMap, canIcon)
        }

        /**
         * 先画全部的路线再行驶
         */
        @JvmStatic
        fun newMoveHasLineInstance(baiduMap: BaiduMap, canIcon: BitmapDescriptor): CarTrackManager {
            return MoveHasLineTrackMananger(baiduMap, canIcon)
        }

        /**
         * 根据缩放等级计算车辆每次移动的距离
         */
        @JvmStatic
        fun newNormalOfZoomInstance(baiduMap: BaiduMap, carIcon: BitmapDescriptor, reduceTimes: Float = 4f): CarTrackManager {
            val manager = NormalOfZoomManager(baiduMap, carIcon)
            manager.reduceTimes = reduceTimes
            return manager
        }

        /**
         * 先画线,再移动.移动的距离由缩放等级来决定
         */
        @JvmStatic
        fun newMoveHasLineOfZoomInstance(baiduMap: BaiduMap, canIcon: BitmapDescriptor, reduceTimes: Float = 4f): CarTrackManager {
            val manager = MoveHasLineOfZoomCarTrackManager(baiduMap, canIcon)
            manager.reduceTimes = reduceTimes
            return manager
        }
    }

    protected fun removeUiSetting() {
        baiduMap.uiSettings.isOverlookingGesturesEnabled = false
        baiduMap.uiSettings.isRotateGesturesEnabled = false
        baiduMap.uiSettings.isCompassEnabled = false
    }

    protected fun generateCarMarker(firstLatLng: LatLng, sencondLatLng: LatLng): MarkerOptions {
        return MarkerOptions().anchor(0.5f, 0.5f).icon(canIcon).position(firstLatLng).rotate(carRotation(firstLatLng, sencondLatLng)).zIndex(carMarkerZIndex)
    }

    protected fun generatePolylineOptions(pointList: List<LatLng>): PolylineOptions {
        return PolylineOptions().width(polylineWidth).color(polylineColor).points(pointList).zIndex(polylineLineZIndex)
    }


    abstract fun start()

    open fun pause() {
        isStop = true
        isPause = true
        isRunning = false
    }

    open fun stop() {
        isStop = true
        isRunning = false
        threadController.async {
            Thread.sleep(sleepTime * 2)
            resetIndex()
        }
    }

    protected abstract fun resetIndex()

    fun release() {
        stop()
        asyncTask?.cancel(true)
        canIcon.recycle()
    }

    interface OnMoveFinishListener {
        fun onMoveFinish()
    }

    protected class MoveCarStopException : Exception()
}
