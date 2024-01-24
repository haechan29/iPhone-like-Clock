# 🌟 iPhone-like Clock
### Say Good-bye to TimePicker.

<!-- <img src="https://github.com/haechan29/iPhone-like-Clock/assets/63138511/72fb0a52-5cf6-4016-9ce5-3320a0217f10"/> -->

</br>

## ⚒️ How Does It Work
1️⃣ Calculate the __rotated degree__ of the item using the ``height`` of the clock and the ``offset`` of the item
<details>
  <summary>View code</summary>
  
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
</details>

</br>

2️⃣ Rotate the item into ``y-direction`` and decrease the ``height`` of the item by the __rotated degree__.
<details>
  <summary>View code</summary>

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
</details>

</br>

3️⃣ Snap to a specific position whenever __fling event__ haapens using ``scrollTo()`` method
<details>
  <summary>View code</summary>
  
  ```
  flingBehavior = flingBehaviorWithOnFinished {
      scope.launch {
          listState.animateScrollToItem(listState.firstItemIndex)
      }
  }
  ```
</details>

</br>

💡 Can detect the end of __fling event__ using ``animation`` in  ``ScrollScope.performFling()`` method of FlingBehavior
<details>
  <summary>View code</summary>
  
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
</detail>

</br>

## 🥄 Challenges
1. 아이템의 높이가 제대로 설정되지 않는 문제
[상황] 시계의 ``height``와 아이템의 ``offset``을 이용해서 아이템이 회전한 각도하여 아이템의 ``height``을 설정했으나, 제대로 동작하지 않음
[분석] 1. 처음에는 LazyColumn 내부 아이템의 ``height``이 변해서 스크롤된다고 생각했음
          하지만 로그를 찍어 보니 LazyColumn 내부 아이템의 ``height``이 변해도 스크롤은 변하지 않았음
          2. LazyColumn의 firstVisibleItemIndex가 반환하는 값이 변한다고 생각했음
          마찬가지로 로그를 찍어 확인해보니 LazyColumn의 첫 아이템이 10dp까지 줄어도 firstVisibleItemIndex는 변하지 않았음.
[원인] Kotlin의 삼각함수에 도(°)가 아니라 라디안(rad) 단위의 숫자를 대입해야 함
[해결] 삼각함수에 대입하는 값을 라디안 단위로 변경하여 대입함

2. Snapper가 제대로 동작하지 않는 문제

[상황] LazyColumn 내부 아이템의 ``height``이 계속 변하다보니 Snapper가 제대로 동작하지 않음
[분석] Android 커뮤니티에 문의한 결과, FlingBehavior을 통해 Fling 관련 설정을 할 수 있다는 사실을 알게 됨
[원인] FlingBehavior의 구현체를 통해 Fling 이벤트의 종료 시점에 scrollTo() 메서드를 호출함.
[해결] 시계가 고정된 위치에 멈추게 됨.
