package com.example.waternotes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tela Home (MainActivity)
 * Mostra o progresso diário de água do usuário, ações rápidas de inserção e resumo semanal.
 */
class MainActivity : AppCompatActivity() {

    // Views do Layout
    private lateinit var tvGreeting: TextView
    private lateinit var tvPorcentagem: TextView
    private lateinit var progressBarAgua: ProgressBar
    private lateinit var tvProgressoTexto: TextView
    private lateinit var btnAgua250: LinearLayout
    private lateinit var btnAgua500: LinearLayout
    private lateinit var tvResumoSemanal: TextView
    private lateinit var btnSair: ImageButton

    // Banco de dados e Sessão
    private lateinit var dbHelper: DatabaseHelper
    private var usuarioId: Int = -1
    private var metaDiariaMl: Int = 2000 // Valor padrão de fallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa o banco de dados
        dbHelper = DatabaseHelper(this)

        // Recupera o ID do usuário da Intent ou das SharedPreferences
        recuperarSessaoUsuario()

        // Se o usuário não for válido, retorna para a tela de login por segurança
        if (usuarioId == -1) {
            irParaLogin()
            return
        }

        // Vincula os componentes XML
        inicializarViews()

        // Carrega as informações iniciais e saúda o usuário
        carregarDadosUsuario()

        // Atualiza a tela com o consumo atual de hoje e da semana
        atualizarMetricasProgresso()

        // Configura os ouvintes de clique nos botões
        configurarCliques()
    }

    /**
     * Tenta obter o ID do usuário logado via Intent ou SharedPreferences de backup.
     */
    private fun recuperarSessaoUsuario() {
        // Tenta pegar da Intent de transição direta
        usuarioId = intent.getIntExtra("USUARIO_ID", -1)

        // Caso não encontre (ex: reinício do app), recupera das SharedPreferences persistentes
        if (usuarioId == -1) {
            val sharedPrefs = getSharedPreferences("WaternotesPrefs", Context.MODE_PRIVATE)
            usuarioId = sharedPrefs.getInt("USUARIO_ID", -1)
        }
    }

    /**
     * Localiza as views declaradas no XML e as associa com os atributos.
     */
    private fun inicializarViews() {
        tvGreeting = findViewById(R.id.tvGreeting)
        tvPorcentagem = findViewById(R.id.tvPorcentagem)
        progressBarAgua = findViewById(R.id.progressBarAgua)
        tvProgressoTexto = findViewById(R.id.tvProgressoTexto)
        btnAgua250 = findViewById(R.id.btnAgua250)
        btnAgua500 = findViewById(R.id.btnAgua500)
        tvResumoSemanal = findViewById(R.id.tvResumoSemanal)
        btnSair = findViewById(R.id.btnSair)
    }

    /**
     * Busca os dados cadastrais do usuário para calcular a meta diária e exibir a saudação.
     */
    private fun carregarDadosUsuario() {
        val usuario = dbHelper.buscarUsuarioPorId(usuarioId)
        if (usuario != null) {
            // Define o nome de saudação na tela
            tvGreeting.text = "Olá, ${usuario.nome}!"

            // Calcula a meta com base na regra de peso: Peso * 35 ml
            metaDiariaMl = (usuario.peso * 35).toInt()
        } else {
            // Em caso de falha rara ao buscar o usuário
            tvGreeting.text = "Olá!"
        }
    }

    /**
     * Consulta o SQLite para ler a soma de hoje e da semana e atualiza as views na tela de forma reativa.
     */
    private fun atualizarMetricasProgresso() {
        // Obtém a data de hoje no formato YYYY-MM-DD
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hoje = format.format(Date())

        // Consulta a soma de água consumida hoje
        val totalHojeMl = dbHelper.buscarAguaDoDia(usuarioId, hoje)

        // Consulta a soma de água consumida na semana (últimos 7 dias)
        val totalSemanaMl = dbHelper.buscarAguaDaSemana(usuarioId)

        // --- ATUALIZAÇÕES DIÁRIAS ---
        
        // Texto descritivo do progresso
        tvProgressoTexto.text = "Você bebeu ${totalHojeMl}ml de ${metaDiariaMl}ml hoje"

        // Progresso percentual da barra
        val porcentagem = if (metaDiariaMl > 0) {
            ((totalHojeMl.toDouble() / metaDiariaMl) * 100).toInt()
        } else {
            0
        }

        // Define a porcentagem no TextView e atualiza a barra de progresso
        tvPorcentagem.text = "$porcentagem%"
        progressBarAgua.max = metaDiariaMl
        progressBarAgua.progress = totalHojeMl

        // --- ATUALIZAÇÕES SEMANAIS ---
        
        // Define o texto na seção do resumo semanal
        tvResumoSemanal.text = "Total bebido nos últimos 7 dias: ${totalSemanaMl} ml"
    }

    /**
     * Configura as ações de clique para inserir consumo ou efetuar logout.
     */
    private fun configurarCliques() {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hoje = format.format(Date())

        // Botão rápido: Adicionar 250ml
        btnAgua250.setOnClickListener {
            registrarConsumoAgua(250, hoje)
        }

        // Botão rápido: Adicionar 500ml
        btnAgua500.setOnClickListener {
            registrarConsumoAgua(500, hoje)
        }

        // Botão de Logout (Sair)
        btnSair.setOnClickListener {
            logoutUsuario()
        }
    }

    /**
     * Executa a inserção da água no banco e atualiza a interface instantaneamente.
     */
    private fun registrarConsumoAgua(quantidade: Int, data: String) {
        val inseriu = dbHelper.inserirAgua(usuarioId, quantidade, data)
        if (inseriu) {
            Toast.makeText(this, "Sucesso! +${quantidade}ml registrados.", Toast.LENGTH_SHORT).show()
            // Recarrega os dados imediatamente na tela para feedback em tempo real
            atualizarMetricasProgresso()
        } else {
            Toast.makeText(this, "Erro ao registrar o consumo.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Limpa o ID das SharedPreferences para fechar a sessão e redireciona ao Login.
     */
    private fun logoutUsuario() {
        val sharedPrefs = getSharedPreferences("WaternotesPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().remove("USUARIO_ID").apply()
        
        Toast.makeText(this, "Até logo!", Toast.LENGTH_SHORT).show()
        irParaLogin()
    }

    /**
     * Redireciona o usuário para a LoginActivity e finaliza a MainActivity.
     */
    private fun irParaLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}