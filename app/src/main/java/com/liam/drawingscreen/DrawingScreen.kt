package com.liam.drawingscreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DrawingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DrawingCanvas()
    }
}

@Composable
fun DrawingCanvas() {
    var point by remember { mutableStateOf(Offset.Zero) } // point 위치 추적을 위한 State
    val points = remember { mutableListOf<Offset>() } // 새로 그려지는 path 표시하기 위한 points State

    var path by remember { mutableStateOf(Path()) } // 새로 그려지고 있는 중인 획 State
    val paths = remember { mutableStateListOf<Pair<Path, PathStyle>>() } // 다 그려진 획 리스트 State

    val removedPaths = remember { mutableStateListOf<Pair<Path, PathStyle>>() }

    val pathStyle = PathStyle()

    Canvas(
        modifier = Modifier
            .size(360.dp)
            .background(Color.White)
            .aspectRatio(1.0f)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        point = offset
                        points.add(point)
                    },
                    onDrag = { _, dragAmount ->
                        point += dragAmount
                        points.add(point)
                        // onDrag가 호출될 때마다 현재 그리는 획을 새로 보여줌
                        path = Path()
                        points.forEachIndexed { index, point ->
                            if (index == 0) {
                                path.moveTo(point.x, point.y)
                            } else {
                                path.lineTo(point.x, point.y)
                            }
                        }
                    },
                    onDragEnd = {
                        paths.add(Pair(path, pathStyle.copy()))
                        points.clear()

                        path = Path()
                    }
                )
            },
    ) {
        paths.forEach { pair ->
            drawPath(
                path = pair.first,
                style = pair.second
            )
        }

        drawPath(
            path = path,
            style = pathStyle
        )
    }

    Spacer(modifier = Modifier.height(12.dp))
    // Undo, Redo 버튼
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        DrawingUndoButton {
            if (paths.isEmpty()) return@DrawingUndoButton
            // Ctrl + Z
            val lastPath = paths.removeLast()
            removedPaths.add(lastPath)
        }

        Spacer(modifier = Modifier.width(24.dp))

        DrawingRedoButton {
            if (removedPaths.isEmpty()) return@DrawingRedoButton
            // Ctrl + Shift + Z
            val lastRemovedPath = removedPaths.removeLast()
            paths.add(lastRemovedPath)
        }
    }
    // 획 스타일 조절하는 영역
    DrawingStyleArea(
        onSizeChanged = { pathStyle.width = it },
        onColorChanged = { pathStyle.color = it },
        onAlphaChanged = { pathStyle.alpha = it }
    )
}

@Composable
fun DrawingUndoButton(
    onClick: () -> Unit
) {
    Button(onClick = { onClick() }) {
        Text(text = "Undo")
    }
}

@Composable
fun DrawingRedoButton(
    onClick: () -> Unit
) {
    Button(onClick = { onClick() }) {
        Text(text = "Redo")
    }
}

@Composable
fun DrawingStyleArea(
    onSizeChanged: (Float) -> Unit,
    onColorChanged: (Color) -> Unit,
    onAlphaChanged: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier
                    .width(72.dp)
                    .padding(horizontal = 8.dp),
                text = "두께",
                textAlign = TextAlign.Center
            )

            var size by remember { mutableStateOf(10.0f) }

            Slider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                value = size,
                valueRange = 1.0f..30.0f,
                onValueChange = {
                    size = it
                    onSizeChanged(it)
                }
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier
                    .width(72.dp)
                    .padding(horizontal = 8.dp),
                text = "투명도",
                textAlign = TextAlign.Center
            )

            var alpha by remember { mutableStateOf(1.0f) }

            Slider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                value = alpha,
                valueRange = 0.0f..1.0f,
                onValueChange = {
                    alpha = it
                    onAlphaChanged(it)
                }
            )
        }

        DrawingColorPalette(
            onColorChanged = onColorChanged
        )
    }
}

@Composable
fun DrawingColorPalette(
    onColorChanged: (Color) -> Unit
) {
    var selectedIndex by remember { mutableStateOf(0) }
    val colors = listOf(Color.Black, Color.Red, Color.Green, Color.Blue, Color.Magenta, Color.Yellow)

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        colors.forEachIndexed { index, color ->
            Box(
                modifier = Modifier.size(36.dp)
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .clickable {
                            selectedIndex = index
                            onColorChanged(color)
                        },
                    painter = ColorPainter(color),
                    contentDescription = "색상 선택"
                )

                if (selectedIndex == index) {
                    Image(
                        modifier = Modifier.align(Alignment.Center),
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = "선택된 색상 체크 표시"
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun Preview_DrawingScreen() {
    DrawingScreen()
}

internal fun DrawScope.drawPath(
    path: Path,
    style: PathStyle
) {
    drawPath(
        path = path,
        color = style.color,
        alpha = style.alpha,
        style = Stroke(width = style.width)
    )
}