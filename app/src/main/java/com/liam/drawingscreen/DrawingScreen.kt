package com.liam.drawingscreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
    val paths = remember { mutableStateListOf<Path>() } // 다 그려진 획 리스트 State

    val removedPaths = remember { mutableStateListOf<Path>() }

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
                        paths.add(path)
                        points.clear()

                        path = Path()
                    }
                )
            },
    ) {
        paths.forEach { path ->
            drawPath(
                path = path,
                color = Color.Black,
                style = Stroke()
            )
        }

        drawPath(
            path = path,
            color = Color.Black,
            style = Stroke()
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

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

@Preview
@Composable
fun Preview_DrawingScreen() {
    DrawingScreen()
}