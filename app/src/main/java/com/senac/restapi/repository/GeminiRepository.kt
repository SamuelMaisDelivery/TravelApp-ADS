package com.senac.restapi.repository

import android.util.Log
import com.senac.restapi.BuildConfig
import com.senac.restapi.api.GeminiClient
import com.senac.restapi.api.GeminiContent
import com.senac.restapi.api.GeminiPart
import com.senac.restapi.api.GeminiRequest
import com.senac.restapi.database.TripEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GeminiRepository {

    private val api = GeminiClient.geminiApi
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))

    suspend fun generateItinerary(trip: TripEntity): Result<String> {
        Log.d(TAG, "generateItinerary() called — trip: ${trip.productTitle} | city: ${trip.cidade}")
        Log.d(TAG, "API key blank? ${BuildConfig.GEMINI_API_KEY.isBlank()} | key prefix: ${BuildConfig.GEMINI_API_KEY.take(8)}...")

        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            return Result.failure(
                Exception("Chave de API do Gemini não configurada. Adicione GEMINI_API_KEY no arquivo local.properties.")
            )
        }
        return try {
            val prompt = buildPrompt(trip)
            Log.d(TAG, "Prompt built — ${prompt.length} chars. Sending to Gemini...")

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                )
            )
            val response = api.generateContent(
                apiKey = BuildConfig.GEMINI_API_KEY,
                request = request
            )
            Log.d(TAG, "Response received — candidates: ${response.candidates?.size}")

            val text = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.filter { it.thought != true }
                ?.mapNotNull { it.text }
                ?.joinToString("")
                ?.takeIf { it.isNotBlank() }
                ?: throw Exception("Resposta inválida ou vazia do Gemini.")

            Log.d(TAG, "Text extracted — ${text.length} chars. SUCCESS.")
            Result.success(text)
        } catch (e: Exception) {
            Log.e(TAG, "generateItinerary() FAILED: ${e.javaClass.simpleName} — ${e.message}", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "GeminiRepository"
    }

    private fun buildPrompt(trip: TripEntity): String {
        val startFormatted = dateFormat.format(Date(trip.startDate))
        val endFormatted = dateFormat.format(Date(trip.endDate))
        val destino = buildString {
            append(trip.productTitle)
            if (trip.cidade.isNotBlank()) append(" — ${trip.cidade}")
        }

        return """
Você é um especialista em turismo e planejamento de viagens internacionais.
Sua tarefa é criar um roteiro completo e inteligente utilizando as seguintes informações:
* Destino: $destino
* Data de início: $startFormatted
* Data de término: $endFormatted
* Motivo da viagem: ${trip.tripType} (Lazer ou Negócios)

Regras Gerais
1. Calcule automaticamente a quantidade de dias da viagem utilizando a data de início e a data de término.
2. Gere um roteiro organizado por dia, considerando apenas os dias da viagem.
3. Distribua as atividades de forma equilibrada, evitando excesso de deslocamentos no mesmo dia.
4. Sempre priorize horários realistas e a proximidade geográfica entre os locais.
5. Evite repetir atrações.
6. Caso algum local normalmente esteja fechado em determinado dia da semana, escolha outra alternativa.
7. Considere fatores sazonais da época da viagem (clima, eventos importantes, feriados e alta/baixa temporada), caso sejam relevantes.
8. Caso o destino possua atrações que exijam reserva antecipada, informe isso.
9. Inclua uma estimativa aproximada de tempo em cada atividade.

Se a viagem for Lazer
O roteiro deve priorizar:
* Pontos turísticos mais famosos
* Monumentos históricos
* Museus relevantes
* Mirantes
* Parques
* Praias (quando aplicável)
* Experiências culturais
* Gastronomia típica
* Mercados locais
* Ruas famosas
* Bairros tradicionais
* Passeios panorâmicos
* Vida noturna (quando fizer sentido, evitando locais de alta periculosidade)
* Experiências autênticas da cidade
Misture atrações conhecidas com sugestões menos turísticas que sejam bem avaliadas.

Se a viagem for Negócios
O roteiro deve priorizar:
* Restaurantes sofisticados e adequados para reuniões de negócios
* Cafés tranquilos para reuniões informais
* Hotéis com espaços para networking
* Centros financeiros
* Centros de convenções
* Coworkings renomados
* Lounges executivos
* Bares elegantes apropriados para networking
* Locais frequentemente utilizados por executivos e empresários
Caso exista tempo livre fora do horário comercial, inclua sugestões de:
* Pontos turísticos próximos
* Restaurantes renomados
* Passeios rápidos
* Locais culturais
* Experiências gastronômicas
O foco principal deve permanecer nas atividades profissionais e oportunidades de networking.

Para cada dia apresente:
Dia X — (Data)
Manhã
* Atividade
* Horário sugerido
* Tempo estimado
Almoço
* Restaurante recomendado
* Tipo de culinária
* Faixa de preço
Tarde
* Atividade
* Tempo estimado
Jantar
* Restaurante recomendado
* Motivo da recomendação
Noite
* Sugestão opcional (caso aplicável)

Para cada local informado, inclua:
* Nome
* Breve descrição (2 a 4 linhas)
* Tempo médio de visita
* Faixa de preço (Gratuito, ${'$'}, ${'$'}${'$'}, ${'$'}${'$'}${'$'} ou ${'$'}${'$'}${'$'}${'$'})
* Endereço aproximado (quando aplicável)

Ao final do roteiro, forneça:
Resumo da viagem
* Quantidade total de dias
* Principais atrações visitadas
* Restaurantes recomendados
* Dicas de transporte
* Melhor aplicativo de transporte utilizado na cidade
* Cartão de transporte público (se existir e for relevante)
* Moeda local
* Idioma predominante
* Tomadas elétricas
* Clima esperado durante a viagem
* Orçamento diário estimado (econômico, intermediário e confortável)

Regras importantes
* O roteiro deve ser realista.
* Não invente atrações inexistentes.
* Utilize locais amplamente conhecidos e bem avaliados.
* Caso existam eventos importantes durante o período da viagem, inclua-os no roteiro quando fizer sentido.
* Caso o número de dias seja pequeno, priorize as atrações mais relevantes.
* Caso a viagem seja longa, distribua o roteiro de forma confortável, evitando dias excessivamente cansativos.
* Sempre adapte as recomendações ao destino específico, independentemente do país ou cidade.
        """.trimIndent()
    }
}
