package codec

class BBox {
    var xMin = 0
    var xMax = 0
    var yMin = 0
    var yMax = 0
    var width = 0
    var height = 0

    fun set(src: BBox) {
        xMin = src.xMin
        xMax = src.xMax
        yMin = src.yMin
        yMax = src.yMax
        width = src.width
        height = src.height
    }

    fun max(src: BBox) {
        if (src.xMin < xMin) xMin = src.xMin
        if (src.yMin < yMin) yMin = src.yMin
        if (src.xMax > xMax) xMax = src.xMax
        if (src.yMax > yMax) yMax = src.yMax
        width = xMax - xMin
        height = yMax - yMin
    }
}
