package dev.com.sigea.dominio.cripto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Criterios de aceite da Fase 1 do Projeto 2.
 *
 * Os testes aleatorios usam uma semente impressa na saida e repassavel por
 * -Dcifra.semente=N: se o teste de propriedade falhar uma vez em mil, sem
 * a semente o caso que quebrou se perde e o bug vira "intermitente".
 */
class CifraSigeaTest {

    private static final long SEMENTE = Long.getLong("cifra.semente", new SecureRandom().nextLong());

    static {
        System.out.println("[CifraSigeaTest] semente = " + SEMENTE + " (reproduza com -Dcifra.semente=" + SEMENTE + ")");
    }

    private static byte[] aleatorio(Random rnd, int tamanho) {
        byte[] b = new byte[tamanho];
        rnd.nextBytes(b);
        return b;
    }

    private static byte[] chave(Random rnd) {
        return aleatorio(rnd, CifraSigea.TAMANHO_CHAVE);
    }

    // ------------------------------------------------------------------
    // Criterio 1 - teste de propriedade
    // ------------------------------------------------------------------

    @Test
    @DisplayName("1.000 entradas aleatorias de 1 a 10.000 bytes voltam identicas byte a byte")
    void propriedadeIdaEVolta() {
        Random rnd = new Random(SEMENTE);
        for (int n = 0; n < 1000; n++) {
            int tamanho = 1 + rnd.nextInt(10_000);
            byte[] texto = aleatorio(rnd, tamanho);
            // Chave nova a cada entrada: exercita 1.000 S-boxes e permutacoes
            // diferentes, nao so 1.000 textos sob as mesmas tabelas.
            CifraSigea cifra = new CifraSigea(chave(rnd));

            ResultadoCifragem resultado = cifra.cifrar(texto);
            byte[] decifrado = cifra.decifrar(resultado);

            String contexto = "entrada #" + n + ", " + tamanho + " bytes, semente " + SEMENTE;
            assertEquals((tamanho / 16 + 1) * 16, resultado.textoCifrado().length, "tamanho do ciphertext, " + contexto);
            assertArrayEquals(texto, decifrado, contexto);
        }
    }

    @Test
    @DisplayName("Ida e volta funciona com qualquer numero de rodadas de 1 a 16")
    void idaEVoltaEmTodasAsRodadas() {
        // A Fase 2 mede avalanche de 1 a 10 rodadas; se a inversa so estiver
        // certa para 10, aquele grafico seria construido sobre uma cifra quebrada.
        Random rnd = new Random(SEMENTE ^ 0x5EEDL);
        for (int rodadas = 1; rodadas <= 16; rodadas++) {
            CifraSigea cifra = new CifraSigea(chave(rnd), rodadas);
            for (int n = 0; n < 50; n++) {
                byte[] texto = aleatorio(rnd, rnd.nextInt(300));
                assertArrayEquals(texto, cifra.decifrar(cifra.cifrar(texto)),
                        rodadas + " rodadas, entrada #" + n + ", semente " + SEMENTE);
            }
        }
    }

    // ------------------------------------------------------------------
    // Criterio 2 - fronteiras de padding
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Texto vazio, 1, 16 e 17 bytes (e vizinhos) atravessam as fronteiras de padding")
    void fronteirasDePadding() {
        Random rnd = new Random(SEMENTE ^ 0xFADL);
        CifraSigea cifra = new CifraSigea(chave(rnd));
        int[][] casos = {
                // {tamanho do texto, tamanho esperado do ciphertext}
                {0, 16}, {1, 16}, {15, 16}, {16, 32}, {17, 32}, {31, 32}, {32, 48}, {33, 48}
        };
        for (int[] caso : casos) {
            byte[] texto = aleatorio(rnd, caso[0]);
            ResultadoCifragem resultado = cifra.cifrar(texto);
            assertEquals(caso[1], resultado.textoCifrado().length, "ciphertext para " + caso[0] + " bytes");
            assertArrayEquals(texto, cifra.decifrar(resultado), "ida e volta para " + caso[0] + " bytes");
        }
    }

    @Test
    @DisplayName("Texto de 16 bytes terminado em 0x01 nao e confundido com padding")
    void textoQueTerminaComoPadding() {
        CifraSigea cifra = new CifraSigea(chave(new Random(SEMENTE)));
        byte[] texto = new byte[16];
        Arrays.fill(texto, (byte) 0x01);
        assertArrayEquals(texto, cifra.decifrar(cifra.cifrar(texto)));
    }

    // ------------------------------------------------------------------
    // Criterio 3 - nonce
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Mesmo texto e mesma chave produzem ciphertexts diferentes (o nonce funciona)")
    void mesmoTextoMesmaChaveCiphertextsDiferentes() {
        CifraSigea cifra = new CifraSigea(chave(new Random(SEMENTE)));
        byte[] texto = "Aviso: a prova de Calculo foi adiada para sexta.".getBytes(StandardCharsets.UTF_8);

        ResultadoCifragem a = cifra.cifrar(texto);
        ResultadoCifragem b = cifra.cifrar(texto);

        assertFalse(Arrays.equals(a.nonce(), b.nonce()), "nonces deveriam diferir");
        assertFalse(Arrays.equals(a.textoCifrado(), b.textoCifrado()), "ciphertexts deveriam diferir");
        assertNotEquals(a.paraTextoVersionado(), b.paraTextoVersionado());
        assertArrayEquals(texto, cifra.decifrar(a));
        assertArrayEquals(texto, cifra.decifrar(b));
    }

    @Test
    @DisplayName("1.000 cifragens do mesmo texto nao repetem nenhum ciphertext")
    void milCifragensSemRepeticao() {
        CifraSigea cifra = new CifraSigea(chave(new Random(SEMENTE)));
        byte[] texto = new byte[32];
        Set<String> vistos = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            assertTrue(vistos.add(cifra.cifrar(texto).paraTextoVersionado()), "ciphertext repetido na cifragem #" + i);
        }
    }

    @Test
    @DisplayName("A diferenca vem do nonce: com o nonce fixado, a cifragem e deterministica")
    void diferencaVemDoNonce() {
        // Contraprova do teste anterior: sem isto, ciphertexts diferentes
        // poderiam vir de qualquer fonte de nao-determinismo, inclusive um bug.
        Random rnd = new Random(SEMENTE);
        CifraSigea cifra = new CifraSigea(chave(rnd));
        byte[] texto = aleatorio(rnd, 100);
        byte[] nonce = aleatorio(rnd, CifraSigea.TAMANHO_NONCE);

        assertArrayEquals(cifra.cifrarComNonce(texto, nonce).textoCifrado(),
                cifra.cifrarComNonce(texto, nonce).textoCifrado());
    }

    @Test
    @DisplayName("Subchaves de rodada mudam com o nonce mesmo com a chave mestra fixa")
    void subchavesMudamComONonce() {
        Random rnd = new Random(SEMENTE);
        byte[] chave = chave(rnd);
        EscalonadorChaves e1 = new EscalonadorChaves(chave, aleatorio(rnd, 12), 10);
        EscalonadorChaves e2 = new EscalonadorChaves(chave, aleatorio(rnd, 12), 10);
        for (int r = 1; r <= 10; r++) {
            assertFalse(Arrays.equals(e1.copiaSubchave(r), e2.copiaSubchave(r)), "subchave da rodada " + r);
        }
    }

    // ------------------------------------------------------------------
    // Criterio 4 - chave errada
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Decifrar com chave errada falha ou devolve lixo, nunca o texto original")
    void chaveErradaNuncaDevolveOOriginal() {
        Random rnd = new Random(SEMENTE ^ 0xBADL);
        int recusadas = 0;
        int lixo = 0;
        for (int n = 0; n < 500; n++) {
            byte[] chaveCerta = chave(rnd);
            byte[] chaveErrada = chaveCerta.clone();
            if (n % 2 == 0) {
                // Metade dos casos com a chave errada por um unico bit: o
                // caso mais dificil, porque so a avalanche de chave separa as duas.
                int bit = rnd.nextInt(256);
                chaveErrada[bit / 8] ^= (byte) (1 << (bit % 8));
            } else {
                chaveErrada = chave(rnd);
            }
            byte[] texto = aleatorio(rnd, rnd.nextInt(200));
            ResultadoCifragem resultado = new CifraSigea(chaveCerta).cifrar(texto);

            try {
                byte[] decifrado = new CifraSigea(chaveErrada).decifrar(resultado);
                assertFalse(Arrays.equals(texto, decifrado), "chave errada devolveu o original no caso #" + n);
                lixo++;
            } catch (CifraSigea.FalhaDecifragem esperado) {
                recusadas++;
            }
        }
        System.out.println("[CifraSigeaTest] chave errada: " + recusadas + " recusadas pelo padding, " + lixo + " devolveram lixo");
        assertEquals(500, recusadas + lixo);
    }

    @Test
    @DisplayName("Chave errada no formato v1: tambem nunca devolve o texto original")
    void chaveErradaNoFormatoVersionado() {
        Random rnd = new Random(SEMENTE);
        String original = "Topico: duvidas sobre a lista de exercicios de Algebra Linear";
        String cifrado = new CifraSigea(chave(rnd)).cifrarTexto(original);
        CifraSigea outra = new CifraSigea(chave(rnd));
        try {
            assertNotEquals(original, outra.decifrarTexto(cifrado));
        } catch (CifraSigea.FalhaDecifragem esperado) {
            // recusar tambem atende ao criterio
        }
    }

    // ------------------------------------------------------------------
    // Formato e validacao de entrada
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Formato v1: + Base64(nonce || ciphertext) com texto em portugues")
    void formatoVersionadoComAcentos() {
        CifraSigea cifra = new CifraSigea(chave(new Random(SEMENTE)));
        String original = "Atenção: a avaliação de Programação será às 19h — tragam calculadora.";
        String cifrado = cifra.cifrarTexto(original);

        assertTrue(cifrado.startsWith("v1:"));
        assertFalse(cifrado.contains("avalia"));
        byte[] payload = java.util.Base64.getDecoder().decode(cifrado.substring(3));
        assertEquals(0, (payload.length - CifraSigea.TAMANHO_NONCE) % CifraSigea.TAMANHO_BLOCO);
        assertEquals(original, cifra.decifrarTexto(cifrado));
        assertNull(cifra.cifrarTexto(null));
        assertNull(cifra.decifrarTexto(null));
    }

    @Test
    @DisplayName("Entradas invalidas sao recusadas com erro explicito")
    void validacaoDeEntrada() {
        Random rnd = new Random(SEMENTE);
        assertThrows(IllegalArgumentException.class, () -> new CifraSigea(new byte[16]));
        assertThrows(IllegalArgumentException.class, () -> new CifraSigea(null));
        assertThrows(IllegalArgumentException.class, () -> new CifraSigea(chave(rnd), 0));

        CifraSigea cifra = new CifraSigea(chave(rnd));
        byte[] nonce = new byte[CifraSigea.TAMANHO_NONCE];
        assertThrows(IllegalArgumentException.class, () -> cifra.cifrarComNonce(new byte[1], new byte[8]));
        assertThrows(CifraSigea.FalhaDecifragem.class, () -> cifra.decifrar(nonce, new byte[0]));
        assertThrows(CifraSigea.FalhaDecifragem.class, () -> cifra.decifrar(nonce, new byte[17]));
        assertThrows(CifraSigea.FalhaDecifragem.class, () -> cifra.decifrarTexto("semprefixo"));
        assertThrows(CifraSigea.FalhaDecifragem.class, () -> cifra.decifrarTexto("v1:@@@"));
        assertThrows(CifraSigea.FalhaDecifragem.class, () -> cifra.decifrarTexto("v1:AAAA"));
    }

    @Test
    @DisplayName("Alterar o array da chave depois de construir nao muda a cifra")
    void copiaDefensivaDaChave() {
        Random rnd = new Random(SEMENTE);
        byte[] chave = chave(rnd);
        CifraSigea cifra = new CifraSigea(chave);
        byte[] texto = aleatorio(rnd, 40);
        ResultadoCifragem resultado = cifra.cifrar(texto);
        Arrays.fill(chave, (byte) 0);
        assertArrayEquals(texto, cifra.decifrar(resultado));
    }

    // ------------------------------------------------------------------
    // Tabelas da chave
    // ------------------------------------------------------------------

    @Test
    @DisplayName("S-box e uma permutacao de 0..255 e a inversa a desfaz")
    void sboxEPermutacaoComInversa() {
        CaixaSubstituicao caixa = new CaixaSubstituicao(chave(new Random(SEMENTE)), 16);
        Set<Integer> imagens = new HashSet<>();
        for (int x = 0; x < 256; x++) {
            int y = caixa.substituir(x);
            imagens.add(y);
            assertEquals(x, caixa.substituirInverso(y), "inversa em " + x);
        }
        assertEquals(256, imagens.size(), "S-box precisa ser bijetora");

        int[] p = caixa.permutacaoPosicoes();
        assertEquals(16, p.length);
        assertEquals(16, Arrays.stream(p).distinct().filter(v -> v >= 0 && v < 16).count());
    }

    @Test
    @DisplayName("Chaves diferentes geram S-boxes diferentes; a mesma chave gera a mesma")
    void sboxDependeDaChave() {
        Random rnd = new Random(SEMENTE);
        byte[] chave = chave(rnd);
        assertArrayEquals(new CaixaSubstituicao(chave, 16).sbox(), new CaixaSubstituicao(chave.clone(), 16).sbox());
        assertFalse(Arrays.equals(new CaixaSubstituicao(chave, 16).sbox(), new CaixaSubstituicao(chave(rnd), 16).sbox()));
    }
}
