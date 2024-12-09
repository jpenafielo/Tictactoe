package com.dadm.tictactoe

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var playerWins = 0
    private var computerWins = 0
    private var ties = 0
    private var isPlayerFirst = true
    private var difficulty = "Harder"
    private lateinit var moveSound: MediaPlayer
    private val board = Array(3) { Array(3) { "" } }
    private var currentPlayer = "X"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        moveSound = MediaPlayer.create(this, R.raw.move_sound)

        val bottomMenu = findViewById<ComposeView>(R.id.bottom_menu_compose)
        bottomMenu.setContent {
            BottomMenu()
        }

        val gameStatus = findViewById<TextView>(R.id.gameStatus)
        val gameBoard = findViewById<GridLayout>(R.id.gameBoard)

        loadStatistics() // Cargar estadísticas al inicio

        if (savedInstanceState != null) {
            restoreGameState(savedInstanceState, gameStatus, gameBoard)
        } else {
            startNewGame(gameStatus, gameBoard)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        moveSound.release()
        saveGameState()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val gson = Gson()
        val boardJson = gson.toJson(board)
        outState.putString("boardJson", boardJson)
        outState.putString("currentPlayer", currentPlayer)
        outState.putInt("playerWins", playerWins)
        outState.putInt("computerWins", computerWins)
        outState.putInt("ties", ties)
        outState.putString("gameStatusText", findViewById<TextView>(R.id.gameStatus).text.toString())
        outState.putBoolean("isComputerTurn", currentPlayer == "O") // Guardar si es turno del computador
        outState.putString("difficulty", difficulty) // Guardar dificultad
    }


    @SuppressLint("SetTextI18n")
    private fun restoreGameState(
        savedInstanceState: Bundle,
        gameStatus: TextView,
        gameBoard: GridLayout
    ) {

        val gson = Gson()

        // Restaura el tablero desde JSON
        val boardJson = savedInstanceState.getString("boardJson", "")
        val restoredBoard = gson.fromJson(boardJson, Array<Array<String>>::class.java)
        for (i in 0..2) {
            for (j in 0..2) {
                board[i][j] = restoredBoard[i][j]
            }
        }
        currentPlayer = savedInstanceState.getString("currentPlayer", "X")
        playerWins = savedInstanceState.getInt("playerWins", 0)
        computerWins = savedInstanceState.getInt("computerWins", 0)
        ties = savedInstanceState.getInt("ties", 0)
        difficulty = savedInstanceState.getString("difficulty", "Harder") // Restaurar dificultad

        val restoredStatusText = savedInstanceState.getString("gameStatusText", "")
        gameStatus.text = restoredStatusText

        gameBoard.removeAllViews()
        for (i in 0..2) {
            for (j in 0..2) {
                val imageView = ImageView(this).apply {
                    setBackgroundColor(0xFF888888.toInt())
                    when (board[i][j]) {
                        "X" -> setImageResource(R.drawable.x_image)
                        "O" -> setImageResource(R.drawable.o_image)
                    }
                    setOnClickListener {
                        if (board[i][j].isEmpty() && currentPlayer == "X") {
                            board[i][j] = "X"
                            setImageResource(R.drawable.x_image)
                            moveSound.start()
                            handlePlayerMove(gameStatus, gameBoard)
                        }
                    }
                }
                gameBoard.addView(imageView, GridLayout.LayoutParams().apply {
                    width = 200
                    height = 200
                    rowSpec = GridLayout.spec(i)
                    columnSpec = GridLayout.spec(j)
                    setMargins(4, 4, 4, 4)
                })
            }
        }

        val isComputerTurn = savedInstanceState.getBoolean("isComputerTurn", false)

        if (isComputerTurn) {
            lifecycleScope.launch {
                delay(1000)
                makeComputerMove(board, gameBoard)
                val winner = checkWinner(board)
                if (winner == null) {
                    currentPlayer = "X"
                    gameStatus.text = "Turno del jugador"
                } else {
                    updateStatus(gameStatus, winner, currentPlayer)
                }
            }
        }
    }




    private fun saveGameState() {
        val sharedPreferences = getSharedPreferences("TicTacToePrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("playerWins", playerWins)
            putInt("computerWins", computerWins)
            putInt("ties", ties)
            apply()
        }
    }

    @Composable
    fun BottomMenu() {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(), // Asegura que ocupe toda la altura asignada
                horizontalArrangement = Arrangement.SpaceEvenly, // Distribución uniforme
                verticalAlignment = Alignment.CenterVertically // Centrado vertical
            ) {
                MenuButton("Nuevo Juego", icon = Icons.Default.Refresh) {
                    val gameStatus = findViewById<TextView>(R.id.gameStatus)
                    val gameBoard = findViewById<GridLayout>(R.id.gameBoard)
                    startNewGame(gameStatus, gameBoard)
                }
                VerticalDivider() // Línea divisoria
                MenuButton("Dificultad", icon = Icons.Default.Settings) {
                    showDifficultyDialog()
                }
                VerticalDivider() // Línea divisoria
                MenuButton("Salir",icon = Icons.AutoMirrored.Filled.ExitToApp) {
                    showExitConfirmationDialog()
                }
            }
        }
    }

    @Composable
    fun MenuButton(text: String, icon: ImageVector, onClick: () -> Unit) {
        TextButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = null, // Descripción del ícono
                modifier = Modifier.size(24.dp), // Tamaño del ícono
                tint = Color.White // Color del ícono
            )
            Spacer(modifier = Modifier.width(8.dp)) // Espaciado entre ícono y texto
            Text(
                text = text,
                color = Color.White, // Color del texto
                textAlign = TextAlign.Center
            )
        }
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirmación")
            .setMessage("¿Estás seguro de que deseas salir?")
            .setPositiveButton("Sí") { dialog, _ ->
                dialog.dismiss() // Cierra el diálogo
                finish() // Cierra la actividad actual
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss() // Simplemente cierra el diálogo
            }
            .create()
            .show()
    }


    private fun showDifficultyDialog() {
        val difficulties = arrayOf("Easy", "Harder", "Expert")
        AlertDialog.Builder(this)
            .setTitle("Select Difficulty")
            .setSingleChoiceItems(difficulties, difficulties.indexOf(difficulty)) { _, which ->
                difficulty = difficulties[which] // Actualiza la dificultad seleccionada
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
        gameBoard: GridLayout,
        restore: Boolean = false
    ) {
        if (!restore) {
            for (i in 0..2) {
                for (j in 0..2) {
                    board[i][j] = ""
                }
            }
            currentPlayer = if (isPlayerFirst) "X" else "O"
            gameStatus.text = if (currentPlayer == "X") "Turno del jugador" else "Turno del computador"
        }

        // Actualizar estadísticas en la interfaz
        val playerWinsView = findViewById<TextView>(R.id.playerWins)
        val computerWinsView = findViewById<TextView>(R.id.computerWins)
        val tiesView = findViewById<TextView>(R.id.ties)

        playerWinsView.text = "Jugador: $playerWins"
        computerWinsView.text = "Computador: $computerWins"
        tiesView.text = "Empates: $ties"

        gameBoard.removeAllViews()

        // Crear el tablero visual
        for (i in 0..2) {
            for (j in 0..2) {
                val imageView = ImageView(this).apply {
                    setBackgroundColor(0xFF888888.toInt())
                    when (board[i][j]) {
                        "X" -> setImageResource(R.drawable.x_image)
                        "O" -> setImageResource(R.drawable.o_image)
                    }
                    setOnClickListener {
                        if (board[i][j].isEmpty() && currentPlayer == "X") {
                            board[i][j] = "X"
                            setImageResource(R.drawable.x_image)
                            moveSound.start()
                            handlePlayerMove(gameStatus, gameBoard)
                        }
                    }
                }
                gameBoard.addView(imageView, GridLayout.LayoutParams().apply {
                    width = 200
                    height = 200
                    rowSpec = GridLayout.spec(i)
                    columnSpec = GridLayout.spec(j)
                    setMargins(4, 4, 4, 4)
                })
            }
        }

        if (!isPlayerFirst && !restore) {
            handleComputerMove(gameStatus, gameBoard)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun handlePlayerMove(gameStatus: TextView, gameBoard: GridLayout) {
        val winner = checkWinner(board)
        if (winner == null) {
            currentPlayer = "O"
            gameStatus.text = "Turno del computador"
            lifecycleScope.launch {
                delay(1000)
                handleComputerMove(gameStatus, gameBoard)
            }
        } else {
            updateStatus(gameStatus, winner)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleComputerMove(gameStatus: TextView, gameBoard: GridLayout) {
        makeComputerMove(board, gameBoard)
        val winner = checkWinner(board)
        if (winner == null) {
            currentPlayer = "X"
            gameStatus.text = "Turno del jugador"
        } else {
            updateStatus(gameStatus, winner)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateStatus(gameStatus: TextView, winner: String?) {
        when (winner) {
            "X" -> {
                playerWins++
                saveStatistics() // Guardar estadísticas al ganar
                gameStatus.text = "Jugador X gana!"
            }
            "O" -> {
                computerWins++
                saveStatistics() // Guardar estadísticas al ganar
                gameStatus.text = "Computador gana!"
            }
            "Empate" -> {
                ties++
                saveStatistics() // Guardar estadísticas en empate
                gameStatus.text = "Es un empate!"
            }
        }

        // Actualizar la interfaz con las estadísticas
        val playerWinsView = findViewById<TextView>(R.id.playerWins)
        val computerWinsView = findViewById<TextView>(R.id.computerWins)
        val tiesView = findViewById<TextView>(R.id.ties)

        playerWinsView.text = "Jugador: $playerWins"
        computerWinsView.text = "Computador: $computerWins"
        tiesView.text = "Empates: $ties"

        // Alternar quién inicia la siguiente partida
        isPlayerFirst = !isPlayerFirst
    }

    private fun saveStatistics() {
        val sharedPreferences = getSharedPreferences("TicTacToePrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("playerWins", playerWins)
            putInt("computerWins", computerWins)
            putInt("ties", ties)
            apply()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadStatistics() {
        val sharedPreferences = getSharedPreferences("TicTacToePrefs", MODE_PRIVATE)
        playerWins = sharedPreferences.getInt("playerWins", 0)
        computerWins = sharedPreferences.getInt("computerWins", 0)
        ties = sharedPreferences.getInt("ties", 0)

        // Actualizar las estadísticas en la interfaz
        val playerWinsView = findViewById<TextView>(R.id.playerWins)
        val computerWinsView = findViewById<TextView>(R.id.computerWins)
        val tiesView = findViewById<TextView>(R.id.ties)

        playerWinsView.text = "Jugador: $playerWins"
        computerWinsView.text = "Computador: $computerWins"
        tiesView.text = "Empates: $ties"
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
                "Harder" -> strategicMove(emptyCells, board)
                "Expert" -> expertMove(emptyCells, board)
                else -> emptyCells.random()
            }
            board[row][col] = "O"
            val imageView = gameBoard.getChildAt(row * 3 + col) as ImageView
            imageView.setImageResource(R.drawable.o_image) // Imagen para la "O"
            moveSound.start() // Reproducir sonido al clic del computador
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
