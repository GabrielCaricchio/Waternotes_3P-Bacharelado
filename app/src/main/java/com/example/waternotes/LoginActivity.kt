package com.example.waternotes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Tela de Login (LoginActivity)
 * Permite ao usuário validar suas credenciais armazenadas localmente no SQLite.
 */
class LoginActivity : AppCompatActivity() {

    // Declaração tardia (lateinit) das views que usaremos no código
    private lateinit var tilUsername: TextInputLayout
    private lateinit var etUsername: TextInputEditText
    private lateinit var tilSenha: TextInputLayout
    private lateinit var etSenha: TextInputEditText
    private lateinit var btnEntrar: MaterialButton
    private lateinit var tvNaoTemConta: TextView

    // Instância da nossa classe gerenciadora de banco de dados
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializa o banco de dados
        dbHelper = DatabaseHelper(this)

        // Verifica se o usuário já está logado anteriormente
        verificarUsuarioLogado()

        // Vincula as variáveis com as views correspondentes do layout XML
        inicializarViews()

        // Configura as ações de clique dos botões e textos
        configurarCliques()
    }

    /**
     * Vincula as views do XML com as variáveis da classe.
     */
    private fun inicializarViews() {
        tilUsername = findViewById(R.id.tilUsername)
        etUsername = findViewById(R.id.etUsername)
        tilSenha = findViewById(R.id.tilSenha)
        etSenha = findViewById(R.id.etSenha)
        btnEntrar = findViewById(R.id.btnEntrar)
        tvNaoTemConta = findViewById(R.id.tvNaoTemConta)
    }

    /**
     * Define a lógica ao clicar nos elementos interativos da tela.
     */
    private fun configurarCliques() {
        // Ação ao clicar no botão "Entrar"
        btnEntrar.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val senha = etSenha.text.toString().trim()

            // Valida se os campos não estão vazios antes de consultar o banco
            if (validarCampos(username, senha)) {
                
                // Consulta o SQLite para verificar se as credenciais estão corretas
                val userId = dbHelper.fazerLogin(username, senha)

                if (userId != null) {
                    // Login bem-sucedido! Salva o ID nas SharedPreferences para manter a sessão ativa
                    salvarSessaoUsuario(userId)

                    // Direciona para a Tela Principal (Home)
                    irParaHome(userId)
                } else {
                    // Credenciais inválidas
                    Toast.makeText(this, "Usuário ou senha incorretos!", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Ação ao clicar em "Não tem conta? Cadastre-se"
        tvNaoTemConta.setOnClickListener {
            // Abre a tela de cadastro
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Valida os campos de entrada, aplicando erros visuais caso estejam em branco.
     */
    private fun validarCampos(username: String, email: String): Boolean {
        var valido = true

        // Validação do campo de usuário
        if (username.isEmpty()) {
            tilUsername.error = "Por favor, insira o usuário"
            valido = false
        } else {
            tilUsername.error = null
        }

        // Validação do campo de senha
        if (email.isEmpty()) {
            tilSenha.error = "Por favor, insira a senha"
            valido = false
        } else {
            tilSenha.error = null
        }

        return valido
    }

    /**
     * Salva o ID do usuário nas SharedPreferences para que ele não precise fazer login novamente
     * sempre que abrir o aplicativo.
     */
    private fun salvarSessaoUsuario(userId: Int) {
        val sharedPrefs = getSharedPreferences("WaternotesPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putInt("USUARIO_ID", userId).apply()
    }

    /**
     * Verifica se existe um ID de usuário salvo nas SharedPreferences.
     * Caso positivo, abre a tela Home diretamente de forma transparente para o usuário.
     */
    private fun verificarUsuarioLogado() {
        val sharedPrefs = getSharedPreferences("WaternotesPrefs", Context.MODE_PRIVATE)
        val userId = sharedPrefs.getInt("USUARIO_ID", -1)

        if (userId != -1) {
            irParaHome(userId)
        }
    }

    /**
     * Direciona para a MainActivity (Home), enviando o usuario_id logado, e fecha a tela de Login.
     */
    private fun irParaHome(userId: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USUARIO_ID", userId)
        }
        startActivity(intent)
        finish() // Finaliza a LoginActivity para que o usuário não volte para ela ao pressionar "Voltar"
    }
}
