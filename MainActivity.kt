package com.alexandre.controledefrota

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import com.alexandre.controledefrota.adapter.PesquisaVeiculoAdapter
import com.alexandre.controledefrota.adapter.VeiculoAdapter
import com.alexandre.controledefrota.databinding.ActivityMainBinding
import com.alexandre.controledefrota.model.Historico
import com.alexandre.controledefrota.util.LoadingDialog
import com.alexandre.controledefrota.viewmodel.HistoricoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.alexandre.controledefrota.viewmodel.VeiculoViewModel
// Adicione este import no início do arquivo:
import com.alexandre.controledefrota.model.Veiculo


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var veiculoViewModel: VeiculoViewModel
    private lateinit var historicoViewModel: HistoricoViewModel
    private lateinit var adapter: VeiculoAdapter
    private lateinit var pesquisaAdapter: PesquisaVeiculoAdapter
    private lateinit var loadingDialog: LoadingDialog

    companion object {
        private const val IMPORT_VEICULOS_CODE = 1001
        private const val EXPORT_VEICULOS_CODE = 1002
        private const val IMPORT_HISTORICO_CODE = 1003
        private const val EXPORT_HISTORICO_CODE = 1004
        private const val PERMISSION_REQUEST_CODE = 2000
        private const val PERMISSION_REQUEST_READ_MEDIA = 2001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        loadingDialog = LoadingDialog(this)
        setupRecyclerView()
        setupViewModel()
        setupListeners()
        setupPesquisa()
    }

    private fun setupRecyclerView() {
        adapter = VeiculoAdapter { veiculo ->
            val intent = Intent(this, DetalhesVeiculoActivity::class.java)
            intent.putExtra(DetalhesVeiculoActivity.EXTRA_PLACA, veiculo.placa)
            startActivity(intent)
        }
        binding.recyclerViewVeiculos.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewVeiculos.adapter = adapter
        pesquisaAdapter = PesquisaVeiculoAdapter { veiculo ->
            binding.recyclerViewResultadoPesquisa.visibility = View.GONE
            binding.edtPesquisa.setText("")
            binding.btnLimparPesquisa.visibility = View.GONE
            val intent = Intent(this, DetalhesVeiculoActivity::class.java)
            intent.putExtra(DetalhesVeiculoActivity.EXTRA_PLACA, veiculo.placa)
            startActivity(intent)
        }
        binding.recyclerViewResultadoPesquisa.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewResultadoPesquisa.adapter = pesquisaAdapter
    }

    private fun setupViewModel() {
        veiculoViewModel = ViewModelProvider(this)[VeiculoViewModel::class.java]
        historicoViewModel = ViewModelProvider(this)[HistoricoViewModel::class.java]
        veiculoViewModel.todosVeiculos.observe(this) { veiculos ->
            adapter.submitList(veiculos)
            binding.txtListaVazia.visibility = if (veiculos.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerViewVeiculos.visibility = if (veiculos.isEmpty()) View.GONE else View.VISIBLE
        }
        veiculoViewModel.resultadosPesquisa.observe(this) { resultados ->
            pesquisaAdapter.atualizarLista(resultados)
            binding.recyclerViewResultadoPesquisa.visibility =
                if (resultados.isNotEmpty() && binding.edtPesquisa.text.isNotEmpty()) View.VISIBLE else View.GONE
        }
        historicoViewModel.todosHistoricos.observe(this) { historicos ->
            Log.d("MainActivity", "Históricos carregados: ${historicos.size}")
        }
    }

    private fun setupListeners() {
        binding.fabAdicionarVeiculo.setOnClickListener {
            val intent = Intent(this, AdicionarEditarVeiculoActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupPesquisa() {
        binding.edtPesquisa.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                veiculoViewModel.pesquisar(query)
                binding.btnLimparPesquisa.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.btnLimparPesquisa.setOnClickListener {
            binding.edtPesquisa.setText("")
            binding.recyclerViewResultadoPesquisa.visibility = View.GONE
            it.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_importar_veiculos -> {
                iniciarImportacaoVeiculos()
                true
            }
            R.id.action_exportar_veiculos -> {
                iniciarExportacaoVeiculos()
                true
            }
            R.id.action_importar_historico -> {
                iniciarImportacaoHistorico()
                true
            }
            R.id.action_exportar_historico -> {
                iniciarExportacaoHistorico()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun iniciarImportacaoVeiculos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                abrirSeletorArquivoImportacao()
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                    PERMISSION_REQUEST_READ_MEDIA
                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            abrirSeletorArquivoImportacao()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                abrirSeletorArquivoImportacao()
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        } else {
            abrirSeletorArquivoImportacao()
        }
    }

    private fun abrirSeletorArquivoImportacao() {
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                    "text/csv",
                    "text/comma-separated-values",
                    "text/plain"
                ))
            }
            startActivityForResult(intent, IMPORT_VEICULOS_CODE)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Erro ao abrir seletor de arquivo: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE, PERMISSION_REQUEST_READ_MEDIA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    abrirSeletorArquivoImportacao()
                } else {
                    Toast.makeText(
                        this,
                        "Permissão negada. Não é possível importar arquivos.",
                        Toast.LENGTH_LONG
                    ).show()
                    if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) ||
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_MEDIA_IMAGES))) {
                        mostrarDialogoExplicacaoPermissao()
                    } else {
                        mostrarDialogoConfiguracoesApp()
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun mostrarDialogoExplicacaoPermissao() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permissão necessária")
            .setMessage("Para importar arquivos CSV, o aplicativo precisa de acesso aos arquivos do seu dispositivo.")
            .setPositiveButton("Tentar novamente") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                        PERMISSION_REQUEST_READ_MEDIA
                    )
                } else {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        PERMISSION_REQUEST_CODE
                    )
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoConfiguracoesApp() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permissão necessária")
            .setMessage("Para importar arquivos CSV, o aplicativo precisa de acesso aos arquivos do seu dispositivo. Por favor, acesse as configurações do aplicativo para conceder esta permissão.")
            .setPositiveButton("Configurações") { _, _ ->
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = android.net.Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun iniciarExportacaoVeiculos() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "veiculos_export.csv")
        }
        startActivityForResult(intent, EXPORT_VEICULOS_CODE)
    }

    private fun iniciarImportacaoHistorico() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "text/csv",
                "text/comma-separated-values",
                "text/plain"
            ))
        }
        startActivityForResult(intent, IMPORT_HISTORICO_CODE)
    }

    private fun iniciarExportacaoHistorico() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "historico_export.csv")
        }
        startActivityForResult(intent, EXPORT_HISTORICO_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                when (requestCode) {
                    IMPORT_VEICULOS_CODE -> importarVeiculos(uri)
                    EXPORT_VEICULOS_CODE -> exportarVeiculos(uri)
                    IMPORT_HISTORICO_CODE -> importarHistorico(uri)
                    EXPORT_HISTORICO_CODE -> exportarHistorico(uri)
                }
            }
        }
    }

    private fun importarVeiculos(uri: Uri) {
        loadingDialog.mostrar("Importando veículos...")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    withContext(Dispatchers.Main) {
                        loadingDialog.esconder()
                        Toast.makeText(this@MainActivity,
                            "Erro: Não foi possível abrir o arquivo",
                            Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }
                val encodings = listOf("ISO-8859-1", "UTF-8", "CP1252", "Windows-1252")
                var veiculos = mutableListOf<Veiculo>()
                var sucessoLeitura = false
                var codificacaoUsada = ""
                for (encoding in encodings) {
                    try {
                        inputStream.close()
                        val newInputStream = contentResolver.openInputStream(uri)
                        if (newInputStream == null) continue
                        Log.d("ImportVeiculos", "Tentando codificação: $encoding")
                        val reader = CSVReader(InputStreamReader(newInputStream, encoding))
                        val header = reader.readNext()
                        if (header == null) {
                            reader.close()
                            newInputStream.close()
                            continue
                        }
                        veiculos = mutableListOf()
                        var line: Array<String>?
                        while (reader.readNext().also { line = it } != null) {
                            line?.let {
                                if (it.size >= 11) {
                                    val veiculo = Veiculo(
                                        placa = it[0].trim(),
                                        tipoVeiculo = it[1].trim(),
                                        marca = it[2].trim(),
                                        modelo = it[3].trim(),
                                        ano = it[4].trim(),
                                        onus = it[5].trim(),
                                        valor = it[6].trim(),
                                        chassi = it[7].trim(),
                                        renavan = it[8].trim(),
                                        motorista = it[9].trim(),
                                        cep = it[10].trim()
                                    )
                                    veiculos.add(veiculo)
                                }
                            }
                        }
                        reader.close()
                        newInputStream.close()
                        if (veiculos.isNotEmpty()) {
                            sucessoLeitura = true
                            codificacaoUsada = encoding
                            break
                        }
                    } catch (e: Exception) {
                        Log.e("ImportVeiculos", "Falha ao tentar codificação $encoding: ${e.message}")
                    }
                }
                try {
                    inputStream.close()
                } catch (e: Exception) {
                    // Ignorar erros ao fechar
                }
                if (sucessoLeitura && veiculos.isNotEmpty()) {
                    veiculoViewModel.importarVeiculos(veiculos)
                    withContext(Dispatchers.Main) {
                        loadingDialog.esconder()
                        Toast.makeText(this@MainActivity,
                            "${veiculos.size} veículos importados com sucesso (usando $codificacaoUsada)",
                            Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        loadingDialog.esconder()
                        Toast.makeText(this@MainActivity,
                            "Não foi possível ler o arquivo corretamente. Verifique se é um CSV válido.",
                            Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    loadingDialog.esconder()
                    Toast.makeText(this@MainActivity,
                        "Erro ao importar veículos: ${e.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun exportarVeiculos(uri: Uri) {
        loadingDialog.mostrar("Exportando veículos...")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val outputStream = contentResolver.openOutputStream(uri)
                val writer = OutputStreamWriter(outputStream, "ISO-8859-1")
                val csvWriter = CSVWriter(
                    writer,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.DEFAULT_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END
                )
                val header = arrayOf(
                    "PLACA", "VEICULO", "MARCA", "MODELO", "ANO",
                    "ONUS", "VALOR", "CHASSI", "RENAVAN", "MOTORISTA", "CEP"
                )
                csvWriter.writeNext(header)
                val veiculos = veiculoViewModel.todosVeiculos.value ?: listOf()
                veiculos.forEach { veiculo ->
                    val linha = arrayOf(
                        veiculo.placa, veiculo.tipoVeiculo, veiculo.marca, veiculo.modelo,
                        veiculo.ano, veiculo.onus, veiculo.valor, veiculo.chassi,
                        veiculo.renavan, veiculo.motorista, veiculo.cep
                    )
                    csvWriter.writeNext(linha)
                }
                csvWriter.close()
                writer.close()
                outputStream?.close()
                withContext(Dispatchers.Main) {
                    loadingDialog.esconder()
                    Toast.makeText(this@MainActivity,
                        "${veiculos.size} veículos exportados com sucesso",
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    loadingDialog.esconder()
                    Toast.makeText(this@MainActivity,
                        "Erro ao exportar veículos: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Dentro da classe MainActivity

    private fun importarHistoricos(uri: Uri) {
        loadingDialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val csvReader = CSVReader(reader)

                val historicos = mutableListOf<Historico>()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                var lineCount = 0
                var line: Array<String>? = null

                // Pular cabeçalho
                csvReader.readNext()

                while (csvReader.readNext().also { line = it } != null) {
                    lineCount++
                    try {
                        line?.let {
                            if (it.size >= 6) {
                                val placaVeiculo = it[0].trim()
                                val tipoServico = it[1].trim()
                                val quilometragemStr = it[2].trim()
                                val dataStr = it[3].trim()
                                val custoStr = it[4].trim().replace(",", ".")
                                val comentario = it[5].trim()
                                val tecnico = if (it.size > 6) it[6].trim() else ""

                                try {
                                    val date = dateFormat.parse(dataStr) ?: Date()
                                    val quilometragem = quilometragemStr.toInt()
                                    val custo = custoStr.toDouble()
                                    val historico = Historico(
                                        id = 0,
                                        placaVeiculo = placaVeiculo,
                                        tipoServico = tipoServico,
                                        quilometragem = quilometragem,
                                        data = date,
                                        custoServico = custo,
                                        comentario = comentario,
                                        tecnicoResponsavel = tecnico
                                    )
                                    historicos.add(historico)
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Erro na linha $lineCount: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ImportHistorico", "Erro na linha $lineCount", e)
                    }
                }

                if (historicos.isNotEmpty()) {
                    historicoViewModel.inserirTodos(historicos)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "${historicos.size} históricos importados com sucesso!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Nenhum histórico encontrado para importar.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                inputStream?.close()

            } catch (e: Exception) {
                Log.e("ImportHistorico", "Erro ao importar: ", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Erro ao importar históricos: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                }
            }
        }
    }

    private fun importarHistorico(uri: Uri) {
        loadingDialog.mostrar("Exportando histórico...")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val outputStream = contentResolver.openOutputStream(uri)
                if (outputStream == null) {
                    withContext(Dispatchers.Main) {
                        loadingDialog.esconder()
                        Toast.makeText(this@MainActivity,
                            "Erro: Não foi possível criar o arquivo",
                            Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }
                val writer = OutputStreamWriter(outputStream, "ISO-8859-1")
                val csvWriter = CSVWriter(
                    writer,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.DEFAULT_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END
                )
                val header = arrayOf(
                    "PLACA", "TIPO_SERVICO", "QUILOMETRAGEM", "DATA",
                    "CUSTO", "COMENTARIO"
                )
                csvWriter.writeNext(header)
                val historicos = withContext(Dispatchers.Main) {
                    historicoViewModel.todosHistoricos.value ?: listOf()
                }
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                for (historico in historicos) {
                    val linha = arrayOf(
                        historico.placaVeiculo,
                        historico.tipoServico,
                        historico.quilometragem.toString(),
                        dateFormat.format(historico.data),
                        historico.custoServico.toString(),
                        historico.comentario
                    )
                    csvWriter.writeNext(linha)
                }
                csvWriter.close()
                writer.close()
                outputStream.close()
                withContext(Dispatchers.Main) {
                    loadingDialog.esconder()
                    Toast.makeText(this@MainActivity,
                        "${historicos.size} registros de histórico exportados com sucesso",
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ExportHistorico", "Exceção: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    loadingDialog.esconder()
                    Toast.makeText(this@MainActivity,
                        "Erro ao exportar histórico: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun exportarHistorico(uri: Uri) {
        loadingDialog.mostrar("Exportando histórico...")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val outputStream = contentResolver.openOutputStream(uri)
                if (outputStream == null) {
                    withContext(Dispatchers.Main) {
                        loadingDialog.esconder()
                        Toast.makeText(this@MainActivity,
                            "Erro: Não foi possível criar o arquivo",
                            Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }
                val writer = OutputStreamWriter(outputStream, "ISO-8859-1")
                val csvWriter = CSVWriter(
                    writer,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.DEFAULT_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END
                )
                val header = arrayOf(
                    "PLACA", "TIPO_SERVICO", "QUILOMETRAGEM", "DATA",
                    "CUSTO", "COMENTARIO", "TECNICO"
                )
                csvWriter.writeNext(header)
                val historicos = withContext(Dispatchers.Main) {
                    historicoViewModel.todosHistoricos.value ?: listOf()
                }
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                historicos.forEach { historico ->
                    val linha = arrayOf(
                        historico.placaVeiculo,
                        historico.tipoServico,
                        historico.quilometragem.toString(),
                        dateFormat.format(historico.data),
                        historico.custoServico.toString().replace(".", ","),
                        historico.comentario,
                        historico.tecnicoResponsavel
                    )
                    csvWriter.writeNext(linha)
                }
                csvWriter.close()
                writer.close()
                outputStream.close()
                withContext(Dispatchers.Main) {
                    loadingDialog.esconder()
                    Toast.makeText(this@MainActivity,
                        "${historicos.size} registros de histórico exportados com sucesso",
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ExportHistorico", "Exceção: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    loadingDialog.esconder()
                    Toast.makeText(this@MainActivity,
                        "Erro ao exportar histórico: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
