# CarTrackTest
<a href="https://blog.csdn.net/android_upl/article/details/78647147">博客</a><br/>
效果图:<br/><image src="https://github.com/nanjolnoSat/CarTrackTest/blob/master/carTrack.gif" /><br/>
示例代码我就不提供了,毕竟涉及到需要提供经纬度,这些东西是公司的数据,我不敢外露.<br/>
可以手动创建实例,也可以直接通过CarTrackManager的静态方法创建实例,2种方式都一样,没有区别.<br/>
使用setTrackLatLngList(list:ArrayList&lt;LatLng&gt;,distance:Int=0)设置经纬度,distance的作用是当2个点的距离小于distance时,则不添加到车辆行驶的list<br/>
<b>NormalCarTrackManager:</b>边走边画线.<br/>
<b>NormalOfZoomCarTrackManger:</b>边走边画线,不过每次行走的距离是会根据缩放等级变化的.根据当前缩放等级后1cm对应的具体距离/4,这个4也可以设置成别的.<br/>
<b>MoveCarTrackManager:</b>只是车辆行驶,没有画线.<br/>
<b>MoveHasLineCarTrackManager:</b>先将整个路线画出来,再行驶.<br/>
<b>MoveHasLineOfZoomCarTrackManager:</b>先将整个路线画出来,再行驶.行驶的距离由缩放等级决定.<br/>
汽车图片的角度注意必须是<b>→</b>.如果不是就将计算出来的角度加上实际图片的角度减去<b>→</b>的角度吧

**已于2019年5月从该公司离职，现在手头上没有可以测试的数据，也懒得去想办法造一些测试数据出来。所以如果哪位愿意将async改成协程，还请您改成协程并提一个PR让我review，非常感谢。**
