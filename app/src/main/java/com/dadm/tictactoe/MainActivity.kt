package com.dadm.tictactoe

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var playerWins = 0
    private var computerWins = 0
    private var ties = 0
    private var isPlayerFirst = true // Alterna quién empieza el juego
    private var difficulty = "Harder" // Nivel de dificultad inicial
    private lateinit var newGameButton: Button
    private lateinit var difficutyButton: Button
    private lateinit var quitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gameStatus = findViewById<TextView>(R.id.gameStatus)
        val gameBoard = findViewById<GridLayout>(R.id.gameBoard)
        newGameButton = findViewById(R.id.new_game_menu)
        difficutyButton = findViewById(R.id.difficulty_menu)
        quitButton = findViewById(R.id.quit_menu)
        startNewGame(gameStatus, gameBoard)
        listenerButtons()
    }

    @SuppressLint("SetTextI18n")
    fun listenerButtons(){
        newGameButton.setOnClickListener{
            val gameStatus = findViewById<TextView>(R.id.gameStatus)
            val gameBoard = findViewById<GridLayout>(R.id.gameBoard)
            startNewGame(gameStatus, gameBoard)
        }

        difficutyButton.setOnClickListener{
            showDifficultyDialog()
        }

        quitButton.setOnClickListener{
            finish()
        }

    }

    private fun showDifficultyDialog() {
        val difficulties = arrayOf("Easy", "Harder", "Expert")
        AlertDialog.Builder(this)
            .setTitle("Select Difficulty")
            .setSingleChoiceItems(difficulties, difficulties.indexOf(difficulty)) { _, which ->
                difficulty = difficulties[which] // Actualiza la dificultad
            }
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    @SuppressLint("SetTextI18n")
    private fun startNewGame(
        gameStatus: TextView,
        gameBoard: GridLayout
    ) {
        val board = Array(3) { Array(3) { "" } }
        var currentPlayer = if (isPlayerFirst) "X" else "O"
        var winner: String? = null
        var moves = 0

        gameStatus.text = if (currentPlayer == "X") "Turno del jugador" else "Turno del computador"

        // Clear and initialize the board
        gameBoard.removeAllViews()
        for (i in 0..2) {
            for (j in 0..2) {
                val button = Button(this).apply {
                    textSize = 32f
                    gravity = Gravity.CENTER
                    setBackgroundColor(0xFF888888.toInt())
                    setOnClickListener {
                        if (board[i][j].isEmpty() && currentPlayer == "X" && winner == null) {
                            board[i][j] = "X"
                            text = "X"
                            moves++
                            winner = checkWinner(board)
                            if (winner == null) {
                                currentPlayer = "O"
                                gameStatus.text = "Turno del computador"
                                lifecycleScope.launch {
                                    delay(500) // Delay for computer's move
                                    makeComputerMove(board, gameBoard)
                                    moves++
                                    winner = checkWinner(board)
                                    if (winner == null) {
                                        currentPlayer = "X"
                                        gameStatus.text = "Turno del jugador"
                                    } else {
                                        updateStatus(gameStatus, winner, currentPlayer)
                                    }
                                }
                            } else {
                                updateStatus(gameStatus, winner, currentPlayer)
                            }
                        }
                    }
                }
                gameBoard.addView(button, GridLayout.LayoutParams().apply {
                    width = 200
                    height = 200
                    rowSpec = GridLayout.spec(i)
                    columnSpec = GridLayout.spec(j)
                    setMargins(4, 4, 4, 4)
                })
            }
        }

        // Computer goes first if needed
        if (!isPlayerFirst && winner == null) {
            lifecycleScope.launch {
                delay(1000) // Delay for computer's first move
                makeComputerMove(board, gameBoard)
                moves++
                winner = checkWinner(board)
                if (winner == null) {
                    currentPlayer = "X"
                    gameStatus.text = "Turno del jugador"
                } else {
                    updateStatus(gameStatus, winner, currentPlayer)
                }
            }
        }
    }

    private fun makeComputerMove(board: Array<Array<String>>, gameBoard: GridLayout) {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j].isEmpty()) emptyCells.add(i to j)
            }
        }
        if (emptyCells.isNotEmpty()) {
            val (row, col) = when (difficulty) {
                "Easy" -> emptyCells.random()
                "Harder" -> strategicMove(emptyCells, board) // Implementa lógica más avanzada
                "Expert" -> expertMove(emptyCells, board) // Implementa lógica óptima
                else -> emptyCells.random()
            }
            board[row][col] = "O"
            val button = gameBoard.getChildAt(row * 3 + col) as Button
            button.text = "O"
        }
    }

    private fun checkWinner(board: Array<Array<String>>): String? {
        // Check rows and columns
        for (i in 0..2) {
            if (board[i][0] == board[i][1] && board[i][1] == board[i][2] && board[i][0].isNotEmpty()) {
                return board[i][0] // Winner in a row
            }
            if (board[0][i] == board[1][i] && board[1][i] == board[2][i] && board[0][i].isNotEmpty()) {
                return board[0][i] // Winner in a column
            }
        }

        // Check diagonals
        if (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[0][0].isNotEmpty()) {
            return board[0][0] // Winner in the main diagonal
        }
        if (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[0][2].isNotEmpty()) {
            return board[0][2] // Winner in the anti-diagonal
        }

        // Check for draw
        if (board.all { row -> row.all { it.isNotEmpty() } }) {
            return "Empate"
        }

        // No winner yet
        return null
    }

    @SuppressLint("SetTextI18n")
    private fun updateStatus(
        gameStatus: TextView,
        winner: String?,
        currentPlayer: String
    ) {
        when (winner) {
            "X" -> {
                playerWins++
                gameStatus.text = "Jugador X gana!"
            }

            "O" -> {
                computerWins++
                gameStatus.text = "Computador gana!"
            }

            "Empate" -> {
                ties++
                gameStatus.text = "Es un empate!"
            }

            else -> {
                gameStatus.text =
                    if (currentPlayer == "X") "Turno del jugador" else "Turno del computador"
            }
        }

        if (winner != null) {
            val playerWinsView = findViewById<TextView>(R.id.playerWins)
            val computerWinsView = findViewById<TextView>(R.id.computerWins)
            val tiesView = findViewById<TextView>(R.id.ties)

            playerWinsView.text = "Jugador: $playerWins"
            computerWinsView.text = "Computador: $computerWins"
            tiesView.text = "Empates: $ties"

            isPlayerFirst = !isPlayerFirst // Alternar quién inicia
        }
    }

    private fun strategicMove(
        emptyCells: List<Pair<Int, Int>>,
        board: Array<Array<String>>
    ): Pair<Int, Int> {
        // 1. Intentar ganar
        for (cell in emptyCells) {
            val (row, col) = cell
            board[row][col] = "O"
            if (checkWinner(board) == "O") {
                board[row][col] = "" // Reset the move
                return cell
            }
            board[row][col] = "" // Reset the move
        }

        // 2. Bloquear al jugador
        for (cell in emptyCells) {
            val (row, col) = cell
            board[row][col] = "X"
            if (checkWinner(board) == "X") {
                board[row][col] = "" // Reset the move
                return cell
            }
            board[row][col] = "" // Reset the move
        }

        // 3. Elegir al azar si no hay necesidad de ganar o bloquear
        return emptyCells.random()
    }

    private fun expertMove(emptyCells: List<Pair<Int, Int>>, board: Array<Array<String>>): Pair<Int, Int> {
        var bestScore = Int.MIN_VALUE
        var bestMove: Pair<Int, Int> = emptyCells.first()

        for (cell in emptyCells) {
            val (row, col) = cell
            board[row][col] = "O"
            val score = minimax(board, depth = 0, isMaximizing = false)
            board[row][col] = "" // Reset the move

            if (score > bestScore) {
                bestScore = score
                bestMove = cell
            }
        }
        return bestMove
    }

    // Implementación del algoritmo MiniMax
    private fun minimax(board: Array<Array<String>>, depth: Int, isMaximizing: Boolean): Int {
        val winner = checkWinner(board)
        if (winner == "O") return 10 - depth // Maximizar la computadora
        if (winner == "X") return depth - 10 // Minimizar el jugador
        if (board.all { row -> row.all { it.isNotEmpty() } }) return 0 // Empate

        if (isMaximizing) {
            var bestScore = Int.MIN_VALUE
            for (i in 0..2) {
                for (j in 0..2) {
                    if (board[i][j].isEmpty()) {
                        board[i][j] = "O"
                        val score = minimax(board, depth + 1, isMaximizing = false)
                        board[i][j] = "" // Reset the move
                        bestScore = maxOf(bestScore, score)
                    }
                }
            }
            return bestScore
        } else {
            var bestScore = Int.MAX_VALUE
            for (i in 0..2) {
                for (j in 0..2) {
                    if (board[i][j].isEmpty()) {
                        board[i][j] = "X"
                        val score = minimax(board, depth + 1, isMaximizing = true)
                        board[i][j] = "" // Reset the move
                        bestScore = minOf(bestScore, score)
                    }
                }
            }
            return bestScore
        }
    }


}
