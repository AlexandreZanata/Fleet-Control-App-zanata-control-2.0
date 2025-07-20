package com.alexandre.controledefrota

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Selection
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alexandre.controledefrota.adapter.AnexosAdapter
import com.alexandre.controledefrota.databinding.ActivityAdicionarEditarServicoBinding
import com.alexandre.controledefrota.model.Historico
import com.alexandre.controledefrota.model.ServicoAnexo
import com.alexandre.controledefrota.model.Veiculo
import com.alexandre.controledefrota.util.FileUtils
import com.alexandre.controledefrota.util.PDFGenerator
import com.alexandre.controledefrota.viewmodel.HistoricoViewModel
import com.alexandre.controledefrota.viewmodel.ServicoAnexoViewModel
import com.alexandre.controledefrota.viewmodel.VeiculoViewModel
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class AdicionarEditarServicoActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_PLACA_VEICULO = "extra_placa_veiculo"
        const val EXTRA_HISTORICO_ID = "extra_historico_id"
    }

    private lateinit var binding: ActivityAdicionarEditarServicoBinding
    private lateinit var historicoViewModel: HistoricoViewModel
    private lateinit var veiculoViewModel: VeiculoViewModel
    private lateinit var anexoViewModel: ServicoAnexoViewModel
    private lateinit var anexosAdapter: AnexosAdapter
    private var historicoAtual: Historico? = null
    private var placaVeiculo: String = ""
    private var fotoTempUri: Uri? = null
    private val anexosTemporarios = mutableListOf<ServicoAnexo>()
    private val calendar = Calendar.getInstance()

    private val registroFotografia = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            fotoTempUri?.let { uri ->
                val anexo = ServicoAnexo(
                    historicoId = historicoAtual?.id ?: 0,
                    caminhoArquivo = uri.path ?: "",
                    tipoArquivo = "IMAGEM",
                    nomeArquivo = "Foto ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}"
                )
                if (historicoAtual != null) {
                    anexoViewModel.inserirAnexo(anexo)
                } else {
                    anexosTemporarios.add(anexo)
                    atualizarListaAnexosTemporarios()
                }
            }
        }
    }

    private val escolhaDocumento = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val nomeDocumento = FileUtils.obterNomeArquivo(this, uri)
            val extensao = FileUtils.obterExtensao(nomeDocumento)
            val arquivo = FileUtils.copiarArquivoParaStorage(this, uri, extensao)
            val anexo = ServicoAnexo(
                historicoId = historicoAtual?.id ?: 0,
                caminhoArquivo = arquivo.absolutePath,
                tipoArquivo = extensao.uppercase(),
                nomeArquivo = nomeDocumento
            )
            if (historicoAtual != null) {
                anexoViewModel.inserirAnexo(anexo)
            } else {
                anexosTemporarios.add(anexo)
                atualizarListaAnexosTemporarios()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdicionarEditarServicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("AdicionarServico", "onCreate chamado")

        setupToolbar()
        setupViewModels()
        setupAnexosRecyclerView()

        placaVeiculo = intent.getStringExtra(EXTRA_PLACA_VEICULO) ?: ""
        val historicoId = intent.getLongExtra(EXTRA_HISTORICO_ID, -1L)

        Log.d("AdicionarServico", "Placa recebida: $placaVeiculo, HistoricoId: $historicoId")

        if (historicoId > 0) {
            carregarHistorico(historicoId)
        } else {
            binding.toolbar.title = "Adicionar Serviço"
            // Preencher a data atual para novos serviços
            preencherDataAtual()
            verificarPlacaVeiculo()
        }

        setupListeners()
        setupDataField()
    }

    private fun preencherDataAtual() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.edtData.setText(dateFormat.format(Date()))
    }

    private fun setupDataField() {
        // Configurar o campo de data para abrir o selecionador de data quando clicado
        binding.edtData.setOnClickListener {
            showDatePicker()
        }

        // Adicionar máscara de formatação para o campo de data
        binding.edtData.addTextChangedListener(object : TextWatcher {
            private var current = ""
            private val ddmmyyyy = "ddmmyyyy"
            private val cal = Calendar.getInstance()

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s == null || s.toString() == current) return

                var clean = s.toString().replace("[^\\d.]".toRegex(), "")
                if (clean.length > 8) clean = clean.substring(0, 8)

                val cleanC = StringBuilder(clean)
                val sel = s.length < current.length

                var i = 2
                while (i < clean.length && i < 6) {
                    cleanC.insert(i, '/')
                    i += 3
                }

                clean = cleanC.toString()

                val mm = if (clean.length >= 3) clean.substring(3, if (clean.length > 4) 5 else clean.length) else ""
                val dd = if (clean.isNotEmpty()) clean.substring(0, if (clean.length > 2) 2 else clean.length) else ""
                val yy = if (clean.length >= 6) clean.substring(6) else ""

                if (clean.length < 8) {
                    current = clean
                } else {
                    try {
                        val day = dd.toInt()
                        val month = mm.toInt()
                        val year = yy.toInt()
                        if (isValidDate(year, month, day)) {
                            current = String.format("%02d/%02d/%04d", day, month, year)
                        } else {
                            current = s.toString()
                            // Não atualizar o campo se a data for inválida
                            return
                        }
                    } catch (e: Exception) {
                        current = s.toString()
                        // Não atualizar o campo se ocorrer um erro
                        return
                    }
                }

                val newSel = current.length
                s.replace(0, s.length, current)
                if (newSel <= s.length) {
                    if (!sel) s.setSpan(Selection.SELECTION_END, newSel, newSel, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                }
            }
        })
    }

    private fun isValidDate(year: Int, month: Int, day: Int): Boolean {
        if (year < 1900 || year > 2100) return false
        if (month < 1 || month > 12) return false

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        return day >= 1 && day <= maxDay
    }

    private fun showDatePicker() {
        // Obter a data atual ou a data do campo se já estiver preenchido
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDateString = binding.edtData.text.toString()
        val calendar = Calendar.getInstance()

        if (currentDateString.isNotEmpty()) {
            try {
                val date = dateFormat.parse(currentDateString)
                if (date != null) {
                    calendar.time = date
                }
            } catch (e: ParseException) {
                Log.e("AdicionarServico", "Erro ao converter data", e)
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay)

                binding.edtData.setText(dateFormat.format(calendar.time))
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupViewModels() {
        historicoViewModel = ViewModelProvider(this)[HistoricoViewModel::class.java]
        veiculoViewModel = ViewModelProvider(this)[VeiculoViewModel::class.java]
        anexoViewModel = ViewModelProvider(this)[ServicoAnexoViewModel::class.java]
    }

    private fun setupAnexosRecyclerView() {
        anexosAdapter = AnexosAdapter { anexo ->
            if (historicoAtual != null) {
                anexoViewModel.deletarAnexo(anexo)
            } else {
                anexosTemporarios.remove(anexo)
                atualizarListaAnexosTemporarios()
            }
        }
        binding.recyclerViewAnexos.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewAnexos.adapter = anexosAdapter
    }

    private fun carregarHistorico(id: Long) {
        Log.d("AdicionarServico", "Carregando histórico ID: $id")

        historicoViewModel.obterPorId(id).observe(this) { historico: Historico? ->
            if (historico != null) {
                Log.d("AdicionarServico", "Histórico carregado: ${historico.id}")

                historicoAtual = historico
                binding.toolbar.title = "Editar Serviço"
                preencherCamposComHistorico(historico)

                anexoViewModel.obterAnexosPorServico(historico.id).observe(this) { anexos ->
                    Log.d("AdicionarServico", "Anexos carregados: ${anexos.size}")

                    anexosAdapter.submitList(anexos)
                    binding.txtVazioAnexos.visibility = if (anexos.isEmpty()) View.VISIBLE else View.GONE
                }
            } else {
                Log.e("AdicionarServico", "Histórico não encontrado")
                Toast.makeText(this, "Serviço não encontrado", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun verificarPlacaVeiculo() {
        if (placaVeiculo.isNotEmpty()) {
            Log.d("AdicionarServico", "Verificando placa: $placaVeiculo")

            veiculoViewModel.obterVeiculoPorPlaca(placaVeiculo).observe(this) { veiculo: Veiculo? ->
                if (veiculo == null) {
                    Log.e("AdicionarServico", "Veículo não encontrado para a placa: $placaVeiculo")
                    Toast.makeText(this, "Veículo não encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.d("AdicionarServico", "Veículo encontrado: ${veiculo.placa}")
                }
            }
        } else {
            Log.e("AdicionarServico", "Placa de veículo não informada")
            Toast.makeText(this, "Placa do veículo não informada", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun preencherCamposComHistorico(historico: Historico) {
        binding.edtTipoServico.setText(historico.tipoServico)
        binding.edtQuilometragem.setText(historico.quilometragem.toString())
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.edtData.setText(dateFormat.format(historico.data))
        binding.edtCusto.setText(String.format("%.2f", historico.custoServico))
        binding.edtComentario.setText(historico.comentario)
        binding.edtTecnico.setText(historico.tecnicoResponsavel)
    }

    private fun atualizarListaAnexosTemporarios() {
        anexosAdapter.submitList(anexosTemporarios.toList())
        binding.txtVazioAnexos.visibility = if (anexosTemporarios.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupListeners() {
        binding.btnAdicionarFoto.setOnClickListener {
            tirarFoto()
        }
        binding.btnAdicionarDocumento.setOnClickListener {
            escolherDocumento()
        }
        binding.btnGerarPDF.setOnClickListener {
            if (historicoAtual != null) {
                gerarPDF()
            } else {
                Toast.makeText(this, "Salve o serviço antes de gerar o PDF", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnSalvar.setOnClickListener {
            salvarServico()
        }
    }

    private fun tirarFoto() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val arquivoImagem = File(getExternalFilesDir(null), "IMG_$timeStamp.jpg")
        fotoTempUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            arquivoImagem
        )
        registroFotografia.launch(fotoTempUri)
    }

    private fun escolherDocumento() {
        escolhaDocumento.launch("*/*")
    }

    private fun gerarPDF() {
        historicoAtual?.let { historico ->
            veiculoViewModel.obterVeiculoPorPlaca(historico.placaVeiculo).observe(this) { veiculo: Veiculo? ->
                if (veiculo != null) {
                    anexoViewModel.obterAnexosPorServico(historico.id).observe(this) { anexos ->
                        val pdfFile = PDFGenerator.gerarRelatorioPDF(this, veiculo, historico, anexos)
                        val uri = FileProvider.getUriForFile(
                            this,
                            "${packageName}.provider",
                            pdfFile
                        )
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_VIEW
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        startActivity(Intent.createChooser(shareIntent, "Visualizar PDF"))
                    }
                }
            }
        } ?: run {
            Toast.makeText(this, "Nenhum serviço selecionado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun salvarServico() {
        if (!validarCampos()) return
        val tipoServico = binding.edtTipoServico.text.toString()
        val quilometragemStr = binding.edtQuilometragem.text.toString()
        val dataStr = binding.edtData.text.toString()
        val custoStr = binding.edtCusto.text.toString()
        val comentario = binding.edtComentario.text.toString()
        val tecnico = binding.edtTecnico.text.toString()
        try {
            val quilometragem = quilometragemStr.toInt()
            val custo = custoStr.replace(",", ".").toDouble()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val data = dateFormat.parse(dataStr) ?: Date()
            if (historicoAtual == null) {
                val novoHistorico = Historico(
                    id = 0,
                    placaVeiculo = placaVeiculo,
                    tipoServico = tipoServico,
                    quilometragem = quilometragem,
                    data = data,
                    comentario = comentario,
                    custoServico = custo,
                    tecnicoResponsavel = tecnico
                )
                historicoViewModel.inserirHistorico(novoHistorico) { novoId ->
                    if (novoId > 0 && anexosTemporarios.isNotEmpty()) {
                        for (anexo in anexosTemporarios) {
                            val anexoAtualizado = anexo.copy(historicoId = novoId)
                            anexoViewModel.inserirAnexo(anexoAtualizado)
                        }
                    }
                    Toast.makeText(this, "Serviço salvo com sucesso", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            } else {
                val historicoAtualizado = historicoAtual!!.copy(
                    tipoServico = tipoServico,
                    quilometragem = quilometragem,
                    data = data,
                    comentario = comentario,
                    custoServico = custo,
                    tecnicoResponsavel = tecnico
                )
                historicoViewModel.atualizarHistorico(historicoAtualizado)
                Toast.makeText(this, "Serviço atualizado com sucesso", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Valores numéricos inválidos", Toast.LENGTH_SHORT).show()
        } catch (e: ParseException) {
            Toast.makeText(this, "Formato de data inválido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validarCampos(): Boolean {
        if (binding.edtTipoServico.text.toString().isEmpty()) {
            Toast.makeText(this, "Informe o tipo de serviço", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.edtQuilometragem.text.toString().isEmpty()) {
            Toast.makeText(this, "Informe a quilometragem", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.edtData.text.toString().isEmpty()) {
            Toast.makeText(this, "Informe a data", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.edtCusto.text.toString().isEmpty()) {
            Toast.makeText(this, "Informe o custo", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validar se a data está no formato correto
        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateFormat.isLenient = false
            dateFormat.parse(binding.edtData.text.toString())
        } catch (e: ParseException) {
            Toast.makeText(this, "Data inválida. Use o formato dd/mm/aaaa", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_adicionar_editar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_save -> {
                salvarServico()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}