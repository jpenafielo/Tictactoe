package com.dadm.tictactoe

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun TicTacToeBoard(
    modifier: Modifier = Modifier,
    xBitmap: Bitmap,
    oBitmap: Bitmap,
    onPlaySound: () -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TicTacToeCustomView(context, xBitmap, oBitmap, onPlaySound)
        }
    )
}

class TicTacToeCustomView(
    context: android.content.Context,
    private val xBitmap: Bitmap,
    private val oBitmap: Bitmap,
    private val onPlaySound: () -> Unit
) : View(context) {

    private val board = Array(3) { Array(3) { "" } }
    private val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 8f
        isAntiAlias = true
    }

    private var currentPlayer = "X"

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cellSize = width / 3
        drawBoard(canvas, cellSize)
        drawMarkers(canvas, cellSize)
    }

    private fun drawBoard(canvas: Canvas, cellSize: Int) {
        for (i in 1..2) {
            // Líneas horizontales
            canvas.drawLine(
                0f, (cellSize * i).toFloat(), width.toFloat(), (cellSize * i).toFloat(), paint
            )
            // Líneas verticales
            canvas.drawLine(
                (cellSize * i).toFloat(), 0f, (cellSize * i).toFloat(), height.toFloat(), paint
            )
        }
    }

    private fun drawMarkers(canvas: Canvas, cellSize: Int) {
        for (i in 0..2) {
            for (j in 0..2) {
                val marker = board[i][j]
                val left = j * cellSize
                val top = i * cellSize

                if (marker == "X") {
                    canvas.drawBitmap(xBitmap, left.toFloat(), top.toFloat(), null)
                } else if (marker == "O") {
                    canvas.drawBitmap(oBitmap, left.toFloat(), top.toFloat(), null)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val cellSize = width / 3
            val row = (event.y / cellSize).toInt()
            val col = (event.x / cellSize).toInt()

            if (row in 0..2 && col in 0..2 && board[row][col].isEmpty()) {
                board[row][col] = currentPlayer
                onPlaySound()
                currentPlayer = if (currentPlayer == "X") "O" else "X"
                invalidate() // Redibujar la vista
                return true
            }
        }
        return false
    }
}
