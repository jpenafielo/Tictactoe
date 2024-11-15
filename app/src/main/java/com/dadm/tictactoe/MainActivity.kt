package com.dadm.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadm.tictactoe.ui.theme.TicTacToeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicTacToeTheme {
                TicTacToeGame()
            }
        }
    }
}

@Composable
fun TicTacToeGame() {
    var board by remember { mutableStateOf(Array(3) { Array(3) { "" } }) }
    var currentPlayer by remember { mutableStateOf("X") }
    var winner by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tic Tac Toe",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        for (row in 0..2) {
            Row {
                for (col in 0..2) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.Gray)
                            .padding(2.dp)
                            .clickable(enabled = board[row][col].isEmpty() && winner == null) {
                                board = board.mapIndexed { i, currentRow ->
                                    currentRow.mapIndexed { j, cell ->
                                        if (i == row && j == col) currentPlayer else cell
                                    }.toTypedArray()
                                }.toTypedArray() // Necesario porque `board` es un array
                                currentPlayer = if (currentPlayer == "X") "O" else "X"
                                winner = checkWinner(board)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = board[row][col],
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (winner != null) {
            Text(
                text = if (winner == "Draw") "¡Es un empate!" else "¡El ganador es $winner!",
                fontSize = 24.sp,
                color = Color.Yellow,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Reiniciar",
                fontSize = 20.sp,
                color = Color.Cyan,
                modifier = Modifier.clickable {
                    board = Array(3) { Array(3) { "" } }
                    currentPlayer = "X"
                    winner = null
                }
            )
        }
    }
}

fun checkWinner(board: Array<Array<String>>): String? {
    // Check rows and columns
    for (i in 0..2) {
        if (board[i][0] == board[i][1] && board[i][1] == board[i][2] && board[i][0].isNotEmpty())
            return board[i][0]
        if (board[0][i] == board[1][i] && board[1][i] == board[2][i] && board[0][i].isNotEmpty())
            return board[0][i]
    }
    // Check diagonals
    if (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[0][0].isNotEmpty())
        return board[0][0]
    if (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[0][2].isNotEmpty())
        return board[0][2]
    // Check draw
    if (board.all { row -> row.all { it.isNotEmpty() } }) return "Draw"
    return null
}
