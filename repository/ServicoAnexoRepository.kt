package com.alexandre.controledefrota.repository

import androidx.lifecycle.LiveData
import com.alexandre.controledefrota.dao.ServicoAnexoDao
import com.alexandre.controledefrota.model.ServicoAnexo

class ServicoAnexoRepository(private val servicoAnexoDao: ServicoAnexoDao) {

    suspend fun inserir(anexo: ServicoAnexo): Long {
        return servicoAnexoDao.inserir(anexo)
    }

    suspend fun deletar(anexo: ServicoAnexo) {
        servicoAnexoDao.deletar(anexo)
    }

    fun obterAnexosPorServico(historicoId: Long): LiveData<List<ServicoAnexo>> {
        return servicoAnexoDao.obterAnexosPorServico(historicoId)
    }

    suspend fun obterAnexoPorId(id: Long): ServicoAnexo? {
        return servicoAnexoDao.obterAnexoPorId(id)
    }
}