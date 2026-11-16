package dev.com.sigea.dominio.cripto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HexFormat;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** Garante que a curva do "AES" no relatorio e de fato do AES. */
class AesReferenciaTest {

    private static final HexFormat HEX = HexFormat.of();

    @Test
    @DisplayName("S-box calculada confere com valores do FIPS-197")
    void sboxConfere() {
        assertEquals(0x63, AesReferencia.sbox(0x00));
        assertEquals(0x7c, AesReferencia.sbox(0x01));
        assertEquals(0xed, AesReferencia.sbox(0x53));
        assertEquals(0x16, AesReferencia.sbox(0xff));
    }

    @Test
    @DisplayName("AES-256 com 14 rodadas reproduz o vetor C.3 do FIPS-197")
    void vetorFips197() {
        byte[] chave = HEX.parseHex("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f");
        byte[] texto = HEX.parseHex("00112233445566778899aabbccddeeff");
        assertEquals("8ea2b7ca516745bfeafc49904b496089", HEX.formatHex(AesReferencia.cifrarBloco(chave, texto, 14)));
    }

    @Test
    @DisplayName("AES-256 com 14 rodadas coincide com o AES do JCE em 500 blocos aleatorios")
    void coincideComJce() {
        Random rnd = new Random(14);
        for (int n = 0; n < 500; n++) {
            byte[] chave = new byte[32];
            byte[] bloco = new byte[16];
            rnd.nextBytes(chave);
            rnd.nextBytes(bloco);
            assertArrayEquals(AesReferencia.ecbCifrar(chave, bloco), AesReferencia.cifrarBloco(chave, bloco, 14), "bloco #" + n);
        }
    }
}
