package com.alexandre.controledefrota

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.alexandre.controledefrota.databinding.ActivityAdicionarEditarVeiculoBinding
import com.alexandre.controledefrota.viewmodel.VeiculoViewModel
import com.alexandre.controledefrota.model.Veiculo

class AdicionarEditarVeiculoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PLACA = "extra_placa" // Usando a mesma constante da DetalhesVeiculoActivity
    }

    private lateinit var binding: ActivityAdicionarEditarVeiculoBinding
    private lateinit var veiculoViewModel: VeiculoViewModel
    private var veiculoAtual: Veiculo? = null
    private var placaOriginal: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdicionarEditarVeiculoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        veiculoViewModel = ViewModelProvider(this)[VeiculoViewModel::class.java]

        val placaEditar = intent.getStringExtra(EXTRA_PLACA) // Usando a constante correta
        Log.d("AdicionarEditarVeiculo", "Placa para editar: $placaEditar")

        if (placaEditar != null) {
            carregarVeiculo(placaEditar)
            placaOriginal = placaEditar
            supportActionBar?.title = "Editar Veículo"
        } else {
            supportActionBar?.title = "Adicionar Veículo"
        }

        setupListeners()
    }

    private fun carregarVeiculo(placa: String) {
        Log.d("AdicionarEditarVeiculo", "Carregando veículo com placa: $placa")

        veiculoViewModel.obterPorPlaca(placa).observe(this) { veiculo: Veiculo? ->
            Log.d("AdicionarEditarVeiculo", "Resultado da consulta: ${veiculo?.placa ?: "nulo"}")

            if (veiculo != null) {
                veiculoAtual = veiculo
                preencherCamposComVeiculo(veiculo)
                Log.d("AdicionarEditarVeiculo", "Veículo carregado com sucesso")
            } else {
                Log.e("AdicionarEditarVeiculo", "Veículo não encontrado")
                Toast.makeText(this, "Veículo não encontrado", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun preencherCamposComVeiculo(veiculo: Veiculo) {
        Log.d("AdicionarEditarVeiculo", "Preenchendo campos com dados: ${veiculo.placa}, ${veiculo.modelo}")

        binding.edtPlaca.setText(veiculo.placa)
        binding.edtTipoVeiculo.setText(veiculo.tipoVeiculo)
        binding.edtMarca.setText(veiculo.marca)
        binding.edtModelo.setText(veiculo.modelo)
        binding.edtAno.setText(veiculo.ano)
        binding.edtOnus.setText(veiculo.onus)
        binding.edtValor.setText(veiculo.valor)
        binding.edtChassi.setText(veiculo.chassi)
        binding.edtRenavan.setText(veiculo.renavan)
        binding.edtMotorista.setText(veiculo.motorista)
        binding.edtCep.setText(veiculo.cep)
    }

    private fun setupListeners() {
        binding.btnSalvar.setOnClickListener {
            salvarVeiculo()
        }

        binding.btnExcluir.setOnClickListener {
            confirmarExclusao()
        }
    }

    private fun salvarVeiculo() {
        if (!validarCampos()) return

        val placa = binding.edtPlaca.text.toString().trim().uppercase()
        val tipoVeiculo = binding.edtTipoVeiculo.text.toString().trim()
        val marca = binding.edtMarca.text.toString().trim()
        val modelo = binding.edtModelo.text.toString().trim()
        val ano = binding.edtAno.text.toString().trim()
        val onus = binding.edtOnus.text.toString().trim()
        val valor = binding.edtValor.text.toString().trim()
        val chassi = binding.edtChassi.text.toString().trim()
        val renavan = binding.edtRenavan.text.toString().trim()
        val motorista = binding.edtMotorista.text.toString().trim()
        val cep = binding.edtCep.text.toString().trim()

        val novoVeiculo = Veiculo(
            placa = placa,
            tipoVeiculo = tipoVeiculo,
            marca = marca,
            modelo = modelo,
            ano = ano,
            onus = onus,
            valor = valor,
            chassi = chassi,
            renavan = renavan,
            motorista = motorista,
            cep = cep
        )

        if (veiculoAtual != null) {
            if (placa != placaOriginal) {
                // Se mudou a placa, precisa deletar o antigo e inserir um novo
                veiculoViewModel.deletar(veiculoAtual!!)
                veiculoViewModel.inserir(novoVeiculo)
            } else {
                // Se não mudou a placa, só atualiza
                veiculoViewModel.atualizar(novoVeiculo)
            }
            Toast.makeText(this, "Veículo atualizado com sucesso!", Toast.LENGTH_SHORT).show()
        } else {
            // Novo veículo
            veiculoViewModel.inserir(novoVeiculo)
            Toast.makeText(this, "Veículo adicionado com sucesso!", Toast.LENGTH_SHORT).show()
        }

        setResult(RESULT_OK) // Adicionado para informar à atividade chamadora que houve alteração
        finish()
    }

    private fun validarCampos(): Boolean {
        if (binding.edtPlaca.text.toString().isEmpty()) {
            binding.edtPlaca.error = "Placa obrigatória"
            return false
        }

        if (binding.edtTipoVeiculo.text.toString().isEmpty()) {
            binding.edtTipoVeiculo.error = "Tipo de veículo obrigatório"
            return false
        }

        if (binding.edtMarca.text.toString().isEmpty()) {
            binding.edtMarca.error = "Marca obrigatória"
            return false
        }

        if (binding.edtModelo.text.toString().isEmpty()) {
            binding.edtModelo.error = "Modelo obrigatório"
            return false
        }

        return true
    }

    private fun confirmarExclusao() {
        if (veiculoAtual == null) return

        AlertDialog.Builder(this)
            .setTitle("Confirmar exclusão")
            .setMessage("Tem certeza que deseja excluir este veículo? Esta ação não pode ser desfeita.")
            .setPositiveButton("Excluir") { _, _ ->
                veiculoViewModel.deletar(veiculoAtual!!)
                Toast.makeText(this, "Veículo excluído com sucesso!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK) // Adicionado para informar à atividade chamadora que houve alteração
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}