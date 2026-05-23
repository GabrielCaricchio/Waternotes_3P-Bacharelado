package com.example.waternotes

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Data Class que representa o Modelo de Usuário no sistema.
 * Facilita a passagem de dados de perfil do banco para a interface gráfica.
 */
data class Usuario(
    val id: Int,
    val nome: String,
    val idade: Int,
    val genero: String,
    val peso: Double,
    val altura: Double,
    val username: String
)

/**
 * Classe responsável por gerenciar a criação do banco de dados SQLite e o CRUD (Create, Read, Update, Delete).
 * Herda de SQLiteOpenHelper para lidar com o ciclo de vida e versionamento do banco.
 */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Nome e versão do arquivo do banco de dados
        private const val DATABASE_NAME = "waternotes.db"
        private const val DATABASE_VERSION = 1

        // Nome da tabela de usuários e suas colunas
        const val TABELA_USUARIOS = "usuarios"
        const val COL_USER_ID = "id"
        const val COL_USER_NOME = "nome"
        const val COL_USER_IDADE = "idade"
        const val COL_USER_GENERO = "genero"
        const val COL_USER_PESO = "peso"
        const val COL_USER_ALTURA = "altura"
        const val COL_USER_USERNAME = "username"
        const val COL_USER_SENHA = "senha"

        // Nome da tabela de histórico e suas colunas
        const val TABELA_HISTORICO = "historico_agua"
        const val COL_HIST_ID = "id"
        const val COL_HIST_USER_ID = "usuario_id"
        const val COL_HIST_QTD = "quantidade_ml"
        const val COL_HIST_DATA = "data" // String no formato YYYY-MM-DD
    }

    /**
     * Chamado quando o banco de dados é criado pela primeira vez.
     * Aqui definimos a estrutura das tabelas usando comandos SQL.
     */
    override fun onCreate(db: SQLiteDatabase) {
        // SQL para criar a tabela de usuários
        val criarTabelaUsuarios = """
            CREATE TABLE $TABELA_USUARIOS (
                $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_NOME TEXT NOT NULL,
                $COL_USER_IDADE INTEGER,
                $COL_USER_GENERO TEXT,
                $COL_USER_PESO REAL,
                $COL_USER_ALTURA REAL,
                $COL_USER_USERNAME TEXT UNIQUE NOT NULL,
                $COL_USER_SENHA TEXT NOT NULL
            )
        """.trimIndent()

        // SQL para criar a tabela de histórico de água com Foreign Key vinculando ao usuário
        val criarTabelaHistorico = """
            CREATE TABLE $TABELA_HISTORICO (
                $COL_HIST_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_HIST_USER_ID INTEGER NOT NULL,
                $COL_HIST_QTD INTEGER NOT NULL,
                $COL_HIST_DATA TEXT NOT NULL,
                FOREIGN KEY($COL_HIST_USER_ID) REFERENCES $TABELA_USUARIOS($COL_USER_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        // Executa a criação das tabelas
        db.execSQL(criarTabelaUsuarios)
        db.execSQL(criarTabelaHistorico)
    }

    /**
     * Chamado quando há atualizações na versão do banco de dados (DATABASE_VERSION).
     * Útil para migrações de estrutura sem perda de dados (em produção) ou recriação didática (aqui).
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABELA_HISTORICO")
        db.execSQL("DROP TABLE IF EXISTS $TABELA_USUARIOS")
        onCreate(db)
    }

    /**
     * Ativa restrições de chaves estrangeiras no SQLite.
     * Por padrão, o SQLite não valida as Foreign Keys caso isso não seja habilitado manualmente.
     */
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    // ==========================================
    //            CRUD - OPERAÇÕES DO BANCO
    // ==========================================

    /**
     * Insere um novo usuário no banco de dados.
     * @return O ID do registro inserido (AutoIncrement) ou -1 em caso de erro (ex: username duplicado).
     */
    fun registrarUsuario(
        nome: String,
        idade: Int,
        genero: String,
        peso: Double,
        altura: Double,
        username: String,
        senha: String
    ): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_NOME, nome)
            put(COL_USER_IDADE, idade)
            put(COL_USER_GENERO, genero)
            put(COL_USER_PESO, peso)
            put(COL_USER_ALTURA, altura)
            put(COL_USER_USERNAME, username)
            put(COL_USER_SENHA, senha)
        }
        
        return try {
            db.insert(TABELA_USUARIOS, null, values)
        } catch (e: Exception) {
            -1L // Retorna falha caso aconteça algum erro (ex: UNIQUE constraint no username)
        } finally {
            db.close() // Sempre fecha a conexão com o banco
        }
    }

    /**
     * Valida se existe um usuário com o username e senha informados.
     * @return O ID do usuário logado (Int) se for válido, ou null se falhar.
     */
    fun fazerLogin(username: String, senha: String): Int? {
        val db = this.readableDatabase
        val query = "SELECT $COL_USER_ID FROM $TABELA_USUARIOS WHERE $COL_USER_USERNAME = ? AND $COL_USER_SENHA = ?"
        val cursor = db.rawQuery(query, arrayOf(username, senha))

        var userId: Int? = null
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID))
        }
        cursor.close()
        db.close()
        return userId
    }

    /**
     * Busca todos os dados de perfil de um usuário através de seu ID único.
     * @return Objeto Usuario contendo os dados recuperados ou null caso não encontre.
     */
    fun buscarUsuarioPorId(id: Int): Usuario? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABELA_USUARIOS WHERE $COL_USER_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(id.toString()))

        var usuario: Usuario? = null
        if (cursor.moveToFirst()) {
            val nome = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NOME))
            val idade = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_IDADE))
            val genero = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_GENERO))
            val peso = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_USER_PESO))
            val altura = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_USER_ALTURA))
            val username = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_USERNAME))

            usuario = Usuario(id, nome, idade, genero, peso, altura, username)
        }
        cursor.close()
        db.close()
        return usuario
    }

    /**
     * Adiciona um novo registro de consumo de água.
     * @return true se a inserção ocorreu com sucesso, false caso contrário.
     */
    fun inserirAgua(usuarioId: Int, quantidadeMl: Int, data: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_HIST_USER_ID, usuarioId)
            put(COL_HIST_QTD, quantidadeMl)
            put(COL_HIST_DATA, data)
        }

        val resultado = db.insert(TABELA_HISTORICO, null, values)
        db.close()
        return resultado != -1L
    }

    /**
     * Obtém a quantidade total de água consumida por um usuário em um dia específico (YYYY-MM-DD).
     */
    fun buscarAguaDoDia(usuarioId: Int, data: String): Int {
        val db = this.readableDatabase
        val query = "SELECT SUM($COL_HIST_QTD) FROM $TABELA_HISTORICO WHERE $COL_HIST_USER_ID = ? AND $COL_HIST_DATA = ?"
        val cursor = db.rawQuery(query, arrayOf(usuarioId.toString(), data))

        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0) // Retorna o valor somado
        }
        cursor.close()
        db.close()
        return total
    }

    /**
     * Obtém a soma acumulada de água consumida nos últimos 7 dias (incluindo o dia de hoje).
     * O cálculo gera os últimos 7 dias retroativos dinamicamente no formato de data YYYY-MM-DD.
     */
    fun buscarAguaDaSemana(usuarioId: Int): Int {
        val db = this.readableDatabase
        val datas = ArrayList<String>()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Adiciona a data de hoje e dos 6 dias anteriores
        for (i in 0 until 7) {
            datas.add(format.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        // Monta os placeholders (?) do SQL dinamicamente
        val placeholders = datas.joinToString(",") { "?" }
        val query = """
            SELECT SUM($COL_HIST_QTD) 
            FROM $TABELA_HISTORICO 
            WHERE $COL_HIST_USER_ID = ? AND $COL_HIST_DATA IN ($placeholders)
        """.trimIndent()

        // Junta o ID do usuário e a lista das datas para passar como parâmetro do SQL
        val args = ArrayList<String>()
        args.add(usuarioId.toString())
        args.addAll(datas)

        val cursor = db.rawQuery(query, args.toArray(arrayOf<String>()))
        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return total
    }
}
