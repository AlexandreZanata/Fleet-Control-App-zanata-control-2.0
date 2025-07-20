package com.alexandre.controledefrota.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.alexandre.controledefrota.model.Historico
import com.alexandre.controledefrota.model.ServicoAnexo
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
// Adicione este import no início do arquivo:
import com.alexandre.controledefrota.model.Veiculo

class PDFGenerator {

    companion object {
        fun gerarRelatorioPDF(
            context: Context,
            veiculo: Veiculo,
            historico: Historico,
            anexos: List<ServicoAnexo>
        ): File {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val pdfFile = File(
                context.getExternalFilesDir(null),
                "ZanataControl_${veiculo.placa}_${timeStamp}.pdf"
            )

            val document = Document()
            PdfWriter.getInstance(document, FileOutputStream(pdfFile))
            document.open()

            // Adicionar cabeçalho
            val fontTitle = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD)
            val title = Paragraph("Relatório de Serviço", fontTitle)
            title.alignment = Element.ALIGN_CENTER
            title.spacingAfter = 20f
            document.add(title)

            // Informações do veículo
            val fontSubtitle = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD)
            val subTitle = Paragraph("Dados do Veículo", fontSubtitle)
            subTitle.spacingAfter = 10f
            document.add(subTitle)

            val tableVeiculo = PdfPTable(2)
            tableVeiculo.widthPercentage = 100f
            adicionarLinhaNaTabela(tableVeiculo, "Placa:", veiculo.placa)
            adicionarLinhaNaTabela(tableVeiculo, "Marca/Modelo:", "${veiculo.marca} ${veiculo.modelo}")
            adicionarLinhaNaTabela(tableVeiculo, "Ano:", veiculo.ano)
            adicionarLinhaNaTabela(tableVeiculo, "Motorista:", veiculo.motorista)
            document.add(tableVeiculo)
            document.add(Paragraph(" "))

            // Informações do serviço
            val serviceTitle = Paragraph("Dados do Serviço", fontSubtitle)
            serviceTitle.spacingAfter = 10f
            document.add(serviceTitle)

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val tableServico = PdfPTable(2)
            tableServico.widthPercentage = 100f
            adicionarLinhaNaTabela(tableServico, "Tipo de Serviço:", historico.tipoServico)
            adicionarLinhaNaTabela(tableServico, "Quilometragem:", historico.quilometragem.toString())
            adicionarLinhaNaTabela(tableServico, "Data:", dateFormat.format(historico.data))
            adicionarLinhaNaTabela(tableServico, "Custo:", "R$ ${String.format("%.2f", historico.custoServico)}")
            adicionarLinhaNaTabela(tableServico, "Técnico:", historico.tecnicoResponsavel)
            document.add(tableServico)
            document.add(Paragraph(" "))

            // Comentários
            val commentTitle = Paragraph("Descrição do Serviço", fontSubtitle)
            commentTitle.spacingAfter = 10f
            document.add(commentTitle)

            val comentario = Paragraph(historico.comentario)
            document.add(comentario)
            document.add(Paragraph(" "))
            document.add(Paragraph(" "))

            // Anexos
            if (anexos.isNotEmpty()) {
                val attachTitle = Paragraph("Anexos", fontSubtitle)
                attachTitle.spacingAfter = 10f
                document.add(attachTitle)

                for (anexo in anexos) {
                    try {
                        if (anexo.tipoArquivo == "IMAGEM") {
                            val imageFile = File(anexo.caminhoArquivo)
                            if (imageFile.exists()) {
                                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                                val stream = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                                val imageBytes = stream.toByteArray()
                                val image = Image.getInstance(imageBytes)

                                // Redimensionar imagem para caber na página
                                val pageWidth = document.pageSize.width - document.leftMargin() - document.rightMargin()
                                val pageHeight = document.pageSize.height / 2
                                image.scaleToFit(pageWidth, pageHeight)

                                document.add(image)
                                document.add(Paragraph(anexo.nomeArquivo))
                                document.add(Paragraph(" "))
                            }
                        } else {
                            document.add(Paragraph("Documento anexado: ${anexo.nomeArquivo}"))
                            document.add(Paragraph("Tipo: ${anexo.tipoArquivo}"))
                            document.add(Paragraph(" "))
                        }
                    } catch (e: Exception) {
                        document.add(Paragraph("Erro ao carregar anexo: ${e.message}"))
                    }
                }
            }

            // Rodapé
            document.add(Paragraph(" "))
            document.add(Paragraph(" "))
            val footer = Paragraph("Gerado por: ZanataControl Software", Font(Font.FontFamily.HELVETICA, 10f, Font.ITALIC))
            footer.alignment = Element.ALIGN_CENTER
            document.add(footer)

            val dateFooter = Paragraph("Data de emissão: ${dateFormat.format(Date())}", Font(Font.FontFamily.HELVETICA, 8f, Font.NORMAL))
            dateFooter.alignment = Element.ALIGN_CENTER
            document.add(dateFooter)

            document.close()
            return pdfFile
        }

        private fun adicionarLinhaNaTabela(table: PdfPTable, label: String, value: String) {
            val fontBold = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD)
            val fontNormal = Font(Font.FontFamily.HELVETICA, 12f)

            val cellLabel = PdfPCell(Phrase(label, fontBold))
            cellLabel.border = Rectangle.NO_BORDER
            cellLabel.horizontalAlignment = Element.ALIGN_RIGHT
            cellLabel.paddingRight = 5f
            table.addCell(cellLabel)

            val cellValue = PdfPCell(Phrase(value, fontNormal))
            cellValue.border = Rectangle.NO_BORDER
            cellValue.horizontalAlignment = Element.ALIGN_LEFT
            table.addCell(cellValue)
        }
    }
}