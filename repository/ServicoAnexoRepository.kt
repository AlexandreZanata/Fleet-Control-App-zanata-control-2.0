package com.alexandre.controledefrota.repository

import androidx.lifecycle.LiveData
import com.alexandre.controledefrota.dao.ServicoAnexoDao
import com.alexandre.controledefrota.model.ServicoAnexo

class ServicoAnexoRepository(private val servicoAnexoDao: ServicoAnexoDao) {

    fun obterAnexosPorServico(historicoId: Long): LiveData<List<ServicoAnexo>> {
        return servicoAnexoDao.obterAnexosPorServico(historicoId)
    }

    suspend fun inserirAnexo(anexo: ServicoAnexo) {
        servicoAnexoDao.inserir(anexo)
    }

    suspend fun deletarAnexo(anexo: ServicoAnexo) {
        servicoAnexoDao.deletar(anexo)
    }

    fun inserir(anexo: ServicoAnexo) {

    }

    fun deletar(anexo: ServicoAnexo) {

    }
}