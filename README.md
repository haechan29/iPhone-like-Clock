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

삼각함수를 잘못 이용한 문제
각이 아니라 호도법으로 넣어줘야 했음
처음에는 뷰의 스크롤 관련 문제라고 생각했음
그러나 보이는 뷰의 높이가 변한다고 스크롤이 변하지는 않음
firstVisibleItemIndex의 문제인지도 고려했으나, 실험 결과 첫 아이템이 10dp만 되어도 해당 문제는 발생하지 않음
그래서 텍스트로 높이를 찍어본 결과 값이 이상함을 발견

Snapper가 잘 동작하지 않는 문제
계속해서 뷰들의 높이가 변하다보니 일반적인 Snapper가 잘 작동하지 않았음
안드로이드 커뮤니티에 문의한 끝에 FlingBehavior에서 Fling의 동작 여부를 파악할 수 있다는 걸 알게 되어
Fling이 끝났을 때 animateScrollToItem()을 호출하는 방식으로 Snapper 구현

1. 아이템의 높이가 제대로 설정되지 않는 문제
[상황] 시계의 ``height``와 아이템의 ``offset``을 이용해서 아이템이 회전한 각도를 구했으나, 제대로 산출되지 않음
[분석] 1. 처음에는 LazyColumn 내부 아이템의 ``height``이 변해서 스크롤된다고 생각했음
          하지만 로그를 찍어 보니 LazyColumn 내부 아이템의 ``height``이 변해도 스크롤은 변하지 않았음
          2. LazyColumn의 firstVisibleItemIndex가 반환하는 값이 변한다고 생각했음
          마찬가지로 로그를 찍어 확인해보니 LazyColumn의 첫 아이템이 10dp까지 줄어도 firstVisibleItemIndex는 변하지 않았음.
[원인] Kotlin의 삼각함수에 도(°)가 아니라 라디안(rad) 단위의 숫자를 대입해야 함 
[해결] 삼각함수에 대입하는 값을 라디안 단위로 변경하여 대입함


3. 앞 화면의 Scroll이 뒷 화면에도 적용되는 문제

[상황] 앞 화면을 클릭하면 뒷 화면으로 이동하는데, 뒷 화면은 화면 높이보다 작음에도 불구하고 scroll이 가능한 상태가 됨.
          또, Scroll 값 때문에 계산된 Offset 값이 어긋나 레이아웃이 깨짐.



[분석] 1. Scroll 값은 Root 뷰의 최상단으로부터 보이는 화면 최상단까지의 거리임.
          2. LazyColumn에는 userScrollEnabled 속성이 있지만, Column에는 Scroll을 막는 별다른 방법이 없음.
          3. 뒷 화면에 전달된 Scroll 값은 앞 화면의 Scroll 값과 정확히 일치함.
[원인] 앞 화면에만 ScrollState을 적용해야 하는데, 앞 화면과 뒷 화면의 공통 부모에 ScrollState을 적용하고 있었음.
[해결] 부모에 적용되던 ScrollState을 앞 화면에만 적용함.


3. 앞 화면을 클릭했을 때 앞 화면의 이미지를 투명하게 만들어야 하는 문제

[상황] 앞 화면을 클릭했을 때 앞 화면의 이미지가 확대되며 뒷 화면에 삽입되는데,
          보다 자연스럽게 보이기 위해서는 앞 화면의 이미지가 투명해져야 했음.
[해결] 애니메이션 진행도를 나타내는 변수 progress, 뒷 화면이 보이는 지를 나타내는 변수 isShowingDetail를 이용하여 해결함. 
          해결은 되었지만 제대로 한 것인지 잘 모르겠음. 추후 더 좋은 방법을 고민해봐야 할듯함.
