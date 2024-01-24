# 🌟 iPhone-like Clock
<img src="https://github.com/haechan29/iPhone-like-Clock/assets/63138511/7b52020b-0957-4111-9753-907909c44afe" width="400"/>

</br>

### ✅ Supports ``rotation animation``
### ✅ ``Snaps`` after fling event
### ✅ Is designed by ``Jetpack Compose``

</br>
</br>

# ⚒️ How Does It Work?
## 1️⃣ Calculate the __rotated degree__ of the item
using the ``height`` of the clock and the ``offset`` of the item
```
fun getRotationDegree(layoutInfo: LazyListLayoutInfo, indexInVisibleItems: Int): Double {
  val viewportHeight = with(layoutInfo) { viewportEndOffset - viewportStartOffset }
  val itemInfo = layoutInfo.visibleItemsInfo[indexInVisibleItems]
  val itemCenterOffset = calcItemCenterOffset(itemInfo)
  val h = viewportHeight / 2 - itemCenterOffset
  val r = CLOCK_RADIUS.toPx()
  return asin(h.toDouble() / r.toDouble())
}
```

</br>

## 2️⃣ Rotate the item by the rotated degree
by increasing ``rotationX`` and decreasing the ``height`` of the item.
```
.graphicsLayer {
    if (!isItemVisible(layoutInfo, indexInVisibleItems)) return@graphicsLayer
    rotationX = getRotationDegree(layoutInfo, indexInVisibleItems).toDegree().toFloat()
}
```
```
.height(
    if (!isItemVisible(layoutInfo, indexInVisibleItems))
        calcItemHeight(ITEM_SIZE.dp, DEGREE_OF_ENDPOINT)
    else {
        val degree = getRotationDegree(layoutInfo, indexInVisibleItems)
        calcItemHeight(ITEM_SIZE.dp, degree)
    }
)
```

</br>

## 3️⃣ Snap to a specific position
whenever __fling event__ ends using ``scrollTo()`` method
```
flingBehavior = flingBehaviorWithOnFinished {
    scope.launch {
        listState.animateScrollToItem(listState.firstItemIndex)
    }
}
```

</br>

### 💡 Can detect the end of __fling event__
using ``animation`` in  ``ScrollScope.performFling()`` method of FlingBehavior
```
override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        var isAnimationRunning = true
        var velocityLeft = initialVelocity
        var lastValue = 0f
        val animationState = AnimationState(
            initialValue = 0f,
            initialVelocity = initialVelocity,
        )
        animationState.animateDecay(decayAnimSpec) {
            val delta = value - lastValue
            val consumed = scrollBy(delta)
            lastValue = value
            velocityLeft = this.velocity
            if (isAnimationRunning != isRunning) {
                if (!isRunning) {
                    onFinished()
                }
                isAnimationRunning = isRunning
            }
            // avoid rounding errors and stop if anything is unconsumed
            if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
        }
        return velocityLeft
    }
```

</br>

# 🥄 Challenges
## 1️⃣ 아이템의 높이가 제대로 설정되지 않는 문제
[상황] 시계의 ``height``와 아이템의 ``offset``을 이용해서 아이템이 회전한 각도를 계산하여 __아이템의 ``height``을 설정했으나, 제대로 동작하지 않음__ </br>
[분석] 1. 처음에는 LazyColumn 내부 아이템의 ``height``이 변해서 스크롤된다고 생각했음</br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;하지만 ``로그``를 찍어 보니 LazyColumn 내부 아이템의 ``height``이 변해도 __스크롤은 변하지 않았음__ </br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;2. LazyColumn의 ``firstVisibleItemIndex``가 반환하는 값이 변한다고 생각했음</br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;마찬가지로 ``로그``를 찍어 확인해보니 LazyColumn의 첫 아이템이 __10dp__ 까지 줄어도 __``firstVisibleItemIndex``는 변하지 않았음__.</br>
[원인] Kotlin의 삼각함수에 도(°)가 아니라 __라디안(rad)__ 단위의 숫자를 대입해야 함</br>
[해결] __삼각함수에 대입하는 값을 라디안 단위로 변경__ 하여 대입함</br>

</br>

## 2️⃣ Snapper가 제대로 동작하지 않는 문제
[상황] LazyColumn 내부 아이템의 ``height``이 계속 변하다보니 __``Snapper`` 가 제대로 동작하지 않음__ </br>
[분석] __Android 커뮤니티에 문의__ 한 결과, __``FlingBehavior``을 통해 ``Fling`` 관련 설정을 할 수 있다__ 는 사실을 알게 됨</br>
[원인] ``FlingBehavior``의 구현체를 통해 __Fling 이벤트의 종료 시점에 scrollTo() 메서드를 호출함__.</br>
[해결] __시계가 고정된 위치에 멈추게 됨__.</br>
