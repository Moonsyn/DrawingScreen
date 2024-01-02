package com.liam.drawingscreen

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class DrawingViewModel: ViewModel() {
	// MutableLiveData는 수정이 가능함
	private val _paths = NonNullLiveData<MutableList<Pair<Path, PathStyle>>>(
		 mutableListOf()
	)
	private val _pathStyle = NonNullLiveData(
		PathStyle()
	)

	private val removedPaths = mutableListOf<Pair<Path, PathStyle>>()

	// LiveData는 외부에서 수정이 불가능하게 설정
	// getter를 사용하여 데이터를 읽는 과정만 수행 가능
	val paths: LiveData<MutableList<Pair<Path, PathStyle>>>
		get() = _paths
	val pathStyle: LiveData<PathStyle>
		get() = _pathStyle

	fun updateWidth(width: Float) {
		val style = _pathStyle.value
		style.width = width

		_pathStyle.value = style
	}

	fun updateColor(color: Color) {
		val style = _pathStyle.value
		style.color = color

		_pathStyle.value = style
	}

	fun updateAlpha(alpha: Float) {
		val style = _pathStyle.value
		style.alpha = alpha

		_pathStyle.value = style
	}

	fun addPath(pair: Pair<Path, PathStyle>) {
		val list = _paths.value
		list.add(pair)
		_paths.value = list
	}

	fun undoPath() {
		val pathList = _paths.value
		if (pathList.isEmpty())
			return
		val last = pathList.last()
		val size = pathList.size

		removedPaths.add(last)
		_paths.value = pathList.subList(0, size-1)
	}

	fun redoPath() {
		if (removedPaths.isEmpty())
			return
		_paths.value = (_paths.value + removedPaths.removeLast()) as MutableList<Pair<Path, PathStyle>>
	}
}