package com.alexandre.controledefrota

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alexandre.controledefrota.adapter.HistoricoAdapter
import com.alexandre.controledefrota.databinding.ActivityDetalhesVeiculoBinding
import com.alexandre.controledefrota.viewmodel.HistoricoViewModel
import com.alexandre.controledefrota.viewmodel.VeiculoViewModel
import com.alexandre.controledefrota.model.Veiculo
import com.alexandre.controledefrota.viewmodel.ServicoAnexoViewModel

class DetalhesVeiculoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PLACA = "extra_placa"
        private const val REQUEST_ADD_SERVICO = 1001
        private const val REQUEST_EDIT_VEICULO = 1002
    }

    private lateinit var binding: ActivityDetalhesVeiculoBinding
    private lateinit var veiculoViewModel: VeiculoViewModel
    private lateinit var historicoViewModel: HistoricoViewModel
    private lateinit var historicoAdapter: HistoricoAdapter
    private var veiculoAtual: Veiculo? = null
    private var placaVeiculo = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesVeiculoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        setupViewModels()

        placaVeiculo = intent.getStringExtra(EXTRA_PLACA) ?: ""
        Log.d("DetalhesVeiculo", "Placa recebida: $placaVeiculo")

        if (placaVeiculo.isEmpty()) {
            Toast.makeText(this, "Placa de veículo inválida", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        carregarDados()
        setupListeners()
    }

    private fun setupRecyclerView() {
        // Obter instância do ServicoAnexoViewModel
        val servicoAnexoViewModel = ViewModelProvider(this)[ServicoAnexoViewModel::class.java]

        historicoAdapter = HistoricoAdapter(
            onItemClick = { historico ->
                val intent = Intent(this, AdicionarEditarServicoActivity::class.java).apply {
                    putExtra(AdicionarEditarServicoActivity.EXTRA_HISTORICO_ID, historico.id)
                    putExtra(AdicionarEditarServicoActivity.EXTRA_PLACA_VEICULO, placaVeiculo)
                }
                startActivityForResult(intent, REQUEST_ADD_SERVICO)
            },
            anexoViewModel = servicoAnexoViewModel
        )

        binding.recyclerViewHistorico.apply {
            layoutManager = LinearLayoutManager(this@DetalhesVeiculoActivity)
            adapter = historicoAdapter
        }
    }

    private fun setupViewModels() {
        veiculoViewModel = ViewModelProvider(this)[VeiculoViewModel::class.java]
        historicoViewModel = ViewModelProvider(this)[HistoricoViewModel::class.java]
    }

    private fun carregarDados() {
        Log.d("DetalhesVeiculo", "Carregando dados para placa: $placaVeiculo")

        veiculoViewModel.obterPorPlaca(placaVeiculo).observe(this) { veiculo: Veiculo? ->
            Log.d("DetalhesVeiculo", "Veículo obtido: ${veiculo?.placa ?: "nulo"}")

            if (veiculo != null) {
                veiculoAtual = veiculo
                mostrarDadosVeiculo(veiculo)
                carregarHistorico()
            } else {
                Log.e("DetalhesVeiculo", "Veículo não encontrado")
                Toast.makeText(this, "Veículo não encontrado", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun carregarHistorico() {
        historicoViewModel.obterHistoricoVeiculo(placaVeiculo).observe(this) { historicos ->
            Log.d("DetalhesVeiculo", "Históricos carregados: ${historicos.size}")

            historicoAdapter.submitList(historicos)
            binding.txtSemHistorico.visibility = if (historicos.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerViewHistorico.visibility = if (historicos.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun mostrarDadosVeiculo(veiculo: Veiculo) {
        Log.d("DetalhesVeiculo", "Mostrando dados do veículo: ${veiculo.placa}")

        binding.txtPlaca.text = veiculo.placa
        binding.txtMarcaModelo.text = "${veiculo.marca} ${veiculo.modelo}"
        binding.txtAno.text = veiculo.ano
        binding.txtTipoVeiculo.text = veiculo.tipoVeiculo
        binding.txtMotorista.text = veiculo.motorista
        binding.txtChassi.text = veiculo.chassi
        binding.txtRenavan.text = veiculo.renavan
        binding.txtOnus.text = veiculo.onus
        binding.txtValor.text = veiculo.valor
        binding.txtCep.text = veiculo.cep

        supportActionBar?.title = "Veículo ${veiculo.placa}"
    }

    private fun setupListeners() {
        binding.fabAdicionarServico.setOnClickListener {
            Log.d("DetalhesVeiculo", "Clicou no botão adicionar serviço")

            val intent = Intent(this, AdicionarEditarServicoActivity::class.java).apply {
                putExtra(AdicionarEditarServicoActivity.EXTRA_PLACA_VEICULO, placaVeiculo)
            }
            startActivityForResult(intent, REQUEST_ADD_SERVICO)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detalhes_veiculo, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_editar -> {
                editarVeiculo()
                true
            }
            R.id.action_excluir -> {
                confirmarExclusao()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun editarVeiculo() {
        Log.d("DetalhesVeiculo", "Iniciando edição do veículo")

        val intent = Intent(this, AdicionarEditarVeiculoActivity::class.java).apply {
            putExtra(EXTRA_PLACA, placaVeiculo)
        }
        startActivityForResult(intent, REQUEST_EDIT_VEICULO)
    }

    private fun confirmarExclusao() {
        if (veiculoAtual == null) return

        AlertDialog.Builder(this)
            .setTitle("Confirmar exclusão")
            .setMessage("Tem certeza que deseja excluir este veículo e todo seu histórico de serviços? Esta ação não pode ser desfeita.")
            .setPositiveButton("Excluir") { _, _ ->
                veiculoAtual?.let { veiculo ->
                    veiculoViewModel.deletar(veiculo)
                    Toast.makeText(this, "Veículo excluído com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("DetalhesVeiculo", "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")

        if ((requestCode == REQUEST_ADD_SERVICO || requestCode == REQUEST_EDIT_VEICULO) &&
            resultCode == Activity.RESULT_OK) {
            // Recarregar dados
            carregarDados()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("DetalhesVeiculo", "onResume chamado")
        // Recarregar dados sempre que a atividade voltar ao primeiro plano
        carregarDados()
    }
}