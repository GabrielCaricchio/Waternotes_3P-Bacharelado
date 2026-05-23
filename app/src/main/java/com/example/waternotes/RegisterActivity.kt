package com.example.waternotes

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Tela de Cadastro (RegisterActivity)
 * Permite ao usuário criar uma nova conta. Realiza a lógica de cálculo de meta diária de água.
 */
class RegisterActivity : AppCompatActivity() {

    // TextInputLayouts (para controle de erros visuais)
    private lateinit var tilNome: TextInputLayout
    private lateinit var tilIdade: TextInputLayout
    private lateinit var tilPeso: TextInputLayout
    private lateinit var tilAltura: TextInputLayout
    private lateinit var tilRegUsername: TextInputLayout
    private lateinit var tilRegSenha: TextInputLayout

    // EditTexts (para recuperar os valores digitados)
    private lateinit var etNome: TextInputEditText
    private lateinit var etIdade: TextInputEditText
    private lateinit var etPeso: TextInputEditText
    private lateinit var etAltura: TextInputEditText
    private lateinit var etRegUsername: TextInputEditText
    private lateinit var etRegSenha: TextInputEditText

    // Spinner para escolha do Gênero
    private lateinit var spinnerGenero: Spinner

    // Botões e navegação
    private lateinit var btnCadastrar: MaterialButton
    private lateinit var tvVoltarLogin: TextView

    // Banco de Dados SQLite
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializa o banco de dados
        dbHelper = DatabaseHelper(this)

        // Vincula as views
        inicializarViews()

        // Configura o Spinner de Gêneros
        configurarSpinnerGenero()

        // Configura ações de clique
        configurarCliques()
    }

    /**
     * Faz o link entre os objetos do XML e os atributos do Kotlin.
     */
    private fun inicializarViews() {
        tilNome = findViewById(R.id.tilNome)
        tilIdade = findViewById(R.id.tilIdade)
        tilPeso = findViewById(R.id.tilPeso)
        tilAltura = findViewById(R.id.tilAltura)
        tilRegUsername = findViewById(R.id.tilRegUsername)
        tilRegSenha = findViewById(R.id.tilRegSenha)

        etNome = findViewById(R.id.etNome)
        etIdade = findViewById(R.id.etIdade)
        etPeso = findViewById(R.id.etPeso)
        etAltura = findViewById(R.id.etAltura)
        etRegUsername = findViewById(R.id.etRegUsername)
        etRegSenha = findViewById(R.id.etRegSenha)

        spinnerGenero = findViewById(R.id.spinnerGenero)
        btnCadastrar = findViewById(R.id.btnCadastrar)
        tvVoltarLogin = findViewById(R.id.tvVoltarLogin)
    }

    /**
     * Preenche o Spinner com as opções de gênero requeridas.
     */
    private fun configurarSpinnerGenero() {
        val opcoesGenero = arrayOf("Feminino", "Masculino", "Prefiro não informar")
        
        // Cria um adaptador simples para o Spinner usando o layout nativo do Android
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoesGenero)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        spinnerGenero.adapter = adapter
    }

    /**
     * Configura as ações ao clicar nos botões da tela.
     */
    private fun configurarCliques() {
        // Voltar para a tela de Login
        tvVoltarLogin.setOnClickListener {
            finish() // Fecha esta Activity e retorna à tela anterior (LoginActivity)
        }

        // Executar o cadastro ao clicar em "Cadastrar"
        btnCadastrar.setOnClickListener {
            if (validarFormulario()) {
                val nome = etNome.text.toString().trim()
                val idade = etIdade.text.toString().toInt()
                val genero = spinnerGenero.selectedItem.toString()
                val peso = etPeso.text.toString().toDouble()
                val altura = etAltura.text.toString().toDouble()
                val username = etRegUsername.text.toString().trim()
                val senha = etRegSenha.text.toString().trim()

                // Regra de Negócio: Cálculo didático da meta diária com base no peso (Peso * 35 ml)
                val metaDiaria = (peso * 35).toInt()

                // Insere os dados no banco de dados SQLite
                val resultado = dbHelper.registrarUsuario(
                    nome = nome,
                    idade = idade,
                    genero = genero,
                    peso = peso,
                    altura = altura,
                    username = username,
                    senha = senha
                )

                if (resultado != -1L) {
                    // Cadastro bem sucedido! Exibe a meta calculada didaticamente ao usuário
                    val mensagem = "Cadastro Realizado! Com base no seu peso (${peso}kg), sua meta diária é de ${metaDiaria}ml de água."
                    Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
                    
                    finish() // Fecha e volta para a tela de Login
                } else {
                    // Caso o nome de usuário já esteja em uso
                    tilRegUsername.error = "Este nome de usuário já está em uso."
                    Toast.makeText(this, "Erro ao cadastrar usuário! Tente outro nome de usuário.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Valida os campos de formulário antes de processar o cadastro.
     * Retorna true se tudo estiver correto, ou aplica avisos nos campos vazios e retorna false.
     */
    private fun validarFormulario(): Boolean {
        var valido = true

        // Nome
        if (etNome.text.toString().trim().isEmpty()) {
            tilNome.error = "Insira seu nome completo"
            valido = false
        } else {
            tilNome.error = null
        }

        // Idade
        val idadeStr = etIdade.text.toString().trim()
        if (idadeStr.isEmpty() || idadeStr.toIntOrNull() == null) {
            tilIdade.error = "Idade inválida"
            valido = false
        } else {
            tilIdade.error = null
        }

        // Peso
        val pesoStr = etPeso.text.toString().trim()
        if (pesoStr.isEmpty() || pesoStr.toDoubleOrNull() == null) {
            tilPeso.error = "Peso inválido"
            valido = false
        } else {
            tilPeso.error = null
        }

        // Altura
        val alturaStr = etAltura.text.toString().trim()
        if (alturaStr.isEmpty() || alturaStr.toDoubleOrNull() == null) {
            tilAltura.error = "Altura inválida"
            valido = false
        } else {
            tilAltura.error = null
        }

        // Username
        if (etRegUsername.text.toString().trim().isEmpty()) {
            tilRegUsername.error = "Insira um nome de usuário"
            valido = false
        } else {
            tilRegUsername.error = null
        }

        // Senha
        if (etRegSenha.text.toString().trim().isEmpty()) {
            tilRegSenha.error = "Insira uma senha"
            valido = false
        } else {
            tilRegSenha.error = null
        }

        return valido
    }
}
