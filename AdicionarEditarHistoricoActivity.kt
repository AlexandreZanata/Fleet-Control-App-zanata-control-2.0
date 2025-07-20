package com.alexandre.controledefrota

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alexandre.controledefrota.databinding.ActivityAdicionarEditarHistoricoBinding
import com.alexandre.controledefrota.model.Historico
import com.alexandre.controledefrota.viewmodel.HistoricoViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdicionarEditarHistoricoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdicionarEditarHistoricoBinding
    private lateinit var historicoViewModel: HistoricoViewModel
    private var historicoId: Long = 0
    private var placaVeiculo: String? = null
    private var editando = false
    private var dataAtual = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    companion object {
        const val EXTRA_HISTORICO_ID = "extra_historico_id"
        const val EXTRA_PLACA = "extra_placa"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdicionarEditarHistoricoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        historicoId = intent.getLongExtra(EXTRA_HISTORICO_ID, 0)
        placaVeiculo = intent.getStringExtra(EXTRA_PLACA)

        if (placaVeiculo == null) {
            finish()
            return
        }
        editando = historicoId > 0
        if (editando) {
            title = "Editar Manutenção"
        } else {
            title = "Adicionar Manutenção"
        }
        setupViewModel()
        setupSpinner()
        setupDatePicker()
        setupListeners()
        if (editando) {
            carregarDadosHistorico()
        } else {
            binding.edtData.setText(dateFormat.format(Date()))
        }
    }

    private fun setupViewModel() {
        historicoViewModel = ViewModelProvider(this)[HistoricoViewModel::class.java]
    }

    private fun setupSpinner() {
        val tiposServico = arrayOf(
            "Troca de Óleo",
            "Revisão Completa",
            "Troca de Filtros",
            "Troca de Pneus",
            "Balanceamento",
            "Alinhamento",
            "Manutenção de Freios",
            "Manutenção Elétrica",
            "Lanternagem/Pintura",
            "Outros"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tiposServico)
        binding.spinnerTipoServico.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        binding.edtData.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    dataAtual.set(Calendar.YEAR, year)
                    dataAtual.set(Calendar.MONTH, month)
                    dataAtual.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    binding.edtData.setText(dateFormat.format(dataAtual.time))
                },
                dataAtual.get(Calendar.YEAR),
                dataAtual.get(Calendar.MONTH),
                dataAtual.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    private fun setupListeners() {
        binding.btnSalvar.setOnClickListener {
            salvarHistorico()
        }
    }

    private fun carregarDadosHistorico() {
        historicoViewModel.obterPorId(historicoId).observe(this) { historico ->
            historico?.let { preencherCampos(it) }
        }
    }

    private fun preencherCampos(historico: Historico) {
        binding.apply {
            spinnerTipoServico.setText(historico.tipoServico)
            edtQuilometragem.setText(historico.quilometragem.toString())
            edtCusto.setText(historico.custoServico.toString())
            edtComentario.setText(historico.comentario)
            dataAtual.time = historico.data
            edtData.setText(dateFormat.format(historico.data))
        }
    }

    private fun salvarHistorico() {
        val tipoServico = binding.spinnerTipoServico.text.toString()
        val quilometragemStr = binding.edtQuilometragem.text.toString()
        val custoStr = binding.edtCusto.text.toString()
        val comentario = binding.edtComentario.text.toString()

        if (tipoServico.isEmpty() || quilometragemStr.isEmpty() || custoStr.isEmpty()) {
            Toast.makeText(this, "Preencha os campos obrigatórios", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val quilometragem = quilometragemStr.toInt()
            val custo = custoStr.toDouble()
            val historico = Historico(
                id = if (editando) historicoId else 0,
                placaVeiculo = placaVeiculo!!,
                tipoServico = tipoServico,
                quilometragem = quilometragem,
                data = dataAtual.time,
                custoServico = custo,
                comentario = comentario
            )

            if (editando) {
                historicoViewModel.atualizarHistorico(historico)
                Toast.makeText(this, "Registro atualizado com sucesso", Toast.LENGTH_SHORT).show()
            } else {
                historicoViewModel.inserirHistorico(historico) {
                    Toast.makeText(this, "Histórico salvo com sucesso", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Valores de quilometragem ou custo inválidos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
