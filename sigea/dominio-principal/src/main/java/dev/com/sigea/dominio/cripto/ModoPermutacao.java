package dev.com.sigea.dominio.cripto;

/**
 * Como a permutacao de posicoes P (passo 4 da rodada, o Permute) e sorteada
 * a partir da chave. Selecionavel pelo mesmo motivo da CamadaDifusao: o
 * relatorio compara o antes e o depois sobre exatamente os mesmos pares, e
 * os vetores da Fase 1 continuam reproduziveis.
 *
 * Por que a escolha importa: a MixColumns so mistura bytes DENTRO de cada
 * coluna de 4 bytes (bytes 0-3, 4-7, 8-11, 12-15). Quem leva um byte de uma
 * coluna para outra e so o Permute. E P e o mesmo em todas as rodadas.
 *
 * LIVRE (Fase 1 e Adendo 01): qualquer uma das 16! permutacoes, sorteada por
 * Fisher-Yates. Para cerca de 1 em 1.820 chaves por coluna, P devolve os 4
 * bytes de uma coluna para a mesma coluna; uma diferenca nascida ali fica
 * presa em 32 bits em todas as rodadas, e 12 dos 16 bytes da saida nao
 * dependem daqueles 4 bytes de entrada. E uma classe de chaves fracas, medida
 * em DiagnosticoCaudaTest (cauda-diagnostico.csv): era a causa da cauda
 * residual que o Adendo 01 atribuia a S-box.
 *
 * ESPALHA_COLUNAS (padrao): so as permutacoes em que os 4 bytes de cada
 * coluna de origem caem em 4 colunas de destino diferentes - a mesma
 * propriedade do ShiftRows do AES, mas ainda sorteada pela chave.
 * Consequencia, por construcao e nao por medicao: 1 byte diferente vira 4 na
 * MixColumns (MDS, numero de ramificacao 5), o Permute leva cada um para uma
 * coluna diferente, e a MixColumns da rodada seguinte ativa os 16 bytes. Em
 * 2 rodadas toda diferenca alcanca o bloco inteiro, para toda chave.
 *
 * Preco registrado: o conjunto de P possiveis cai de 16! (~2^44,3) para
 * 24^8 (~2^36,7). Isso nao reduz a chave de 256 bits - P e derivado da chave
 * por SHA-256, nao e chave a parte -, mas torna P mais previsivel para quem
 * tenta adivinha-lo isoladamente.
 */
public enum ModoPermutacao {
    LIVRE,
    ESPALHA_COLUNAS
}
