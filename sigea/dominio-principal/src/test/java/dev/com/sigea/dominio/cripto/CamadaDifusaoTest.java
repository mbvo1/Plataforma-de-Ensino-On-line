package dev.com.sigea.dominio.cripto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HexFormat;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CamadaDifusaoTest {

    private static final HexFormat HEX = HexFormat.of();

    @Test
    @DisplayName("MixColumns reproduz os vetores de teste conhecidos do Rijndael (FIPS-197)")
    void mixColumnsContraVetoresConhecidos() {
        // Sem vetor externo, "MixColumns" seria so o nome: uma matriz ou um
        // polinomio errado ainda inverte corretamente e passa na ida e volta,
        // mas perde a propriedade MDS que motivou a troca.
        String[][] colunas = {
                {"db135345", "8e4da1bc"},
                {"f20a225c", "9fdc589d"},
                {"01010101", "01010101"},
                {"c6c6c6c6", "c6c6c6c6"},
                {"d4d4d4d5", "d5d5d7d6"},
                {"2d26314c", "4d7ebdf8"},
        };
        for (String[] caso : colunas) {
            // A mesma coluna repetida nas quatro posicoes do bloco.
            byte[] bloco = HEX.parseHex(caso[0].repeat(4));
            CamadaDifusao.MIXCOLUMNS_MDS.aplicar(bloco);
            assertEquals(caso[1].repeat(4), HEX.formatHex(bloco), "coluna " + caso[0]);
            CamadaDifusao.MIXCOLUMNS_MDS.inverter(bloco);
            assertEquals(caso[0].repeat(4), HEX.formatHex(bloco), "inversa da coluna " + caso[0]);
        }
    }

    @Test
    @DisplayName("Multiplicacao em GF(2^8) confere com o exemplo do FIPS-197 (57 x 13 = fe)")
    void multiplicacaoGf256() {
        assertEquals(0xfe, CamadaDifusao.mul(0x57, 0x13));
        assertEquals(0xc1, CamadaDifusao.mul(0x57, 0x83));
    }

    @Test
    @DisplayName("As duas camadas se invertem em blocos aleatorios")
    void inversaDasDuasCamadas() {
        Random rnd = new Random(300);
        for (CamadaDifusao camada : CamadaDifusao.values()) {
            for (int n = 0; n < 1000; n++) {
                byte[] bloco = new byte[16];
                rnd.nextBytes(bloco);
                byte[] copia = bloco.clone();
                camada.aplicar(copia);
                camada.inverter(copia);
                assertArrayEquals(bloco, copia, camada + " bloco #" + n);
            }
        }
    }

    @Test
    @DisplayName("CADEIA_DUPLA reproduz byte a byte a cifra da Fase 1, antes da refatoracao")
    void cadeiaDuplaIgualAFase1() {
        // Vetores capturados do commit da Fase 1 (379685e), com o MixChain
        // ainda inline. Garantem que a comparacao do relatorio compara a
        // camada que de fato foi medida, e nao uma reimplementacao parecida.
        // A Fase 1 sorteava a permutacao no modo LIVRE; o padrao mudou depois
        // (ver ModoPermutacao), entao a reproducao fixa LIVRE explicitamente.
        byte[] chave = new byte[32];
        for (int i = 0; i < 32; i++) chave[i] = (byte) i;
        byte[] nonce = new byte[12];
        for (int i = 0; i < 12; i++) nonce[i] = (byte) (0xA0 + i);

        Object[][] vetores = {
                {1, 0, "7d947d60fbf03c714a49e06d5a000c66"},
                {1, 17, "f2b3388f180a69e6c018898351349d63ffccf27a6a367913070c4b662d56ad33"},
                {3, 16, "b9e453aa2b02026dfd0911502a2d2828be9d14de42d798c8de2c45da538be377"},
                {10, 0, "f28bebc344eb485103d842d3223db256"},
                {10, 1, "ea1a6376f6ead8bc27015edf1c84618d"},
                {10, 17, "f992d4c230bc9bc885525dd21c02aed84322206cdf9ccf2e249a9181816b9d9a"},
                {10, 100, "f992d4c230bc9bc885525dd21c02aed8c06fd0a533f0d7ed7b70828c226fd76391c837ca7ee4d25e"
                        + "31c9be2982596c71df9d0acbc69e0fd64aa3f6c0dca89c6bc5f04d1ea1b3d033f534d45a8c4deb56"
                        + "3b78ce8983383f224307d30d52ed1559e29e86e412a0d2873bb04c5e939156e1"},
        };
        for (Object[] v : vetores) {
            int rodadas = (int) v[0];
            int tamanho = (int) v[1];
            byte[] texto = new byte[tamanho];
            for (int i = 0; i < tamanho; i++) texto[i] = (byte) (i * 7);
            CifraSigea cifra = new CifraSigea(chave, rodadas, CamadaDifusao.CADEIA_DUPLA, ModoPermutacao.LIVRE);
            assertEquals(v[2], HEX.formatHex(cifra.cifrarComNonce(texto, nonce).textoCifrado()),
                    rodadas + " rodadas, " + tamanho + " bytes");
        }
    }

    @Test
    @DisplayName("A camada padrao e a MixColumns e muda o ciphertext em relacao a cadeia")
    void padraoEMixColumns() {
        byte[] chave = new byte[32];
        byte[] nonce = new byte[12];
        CifraSigea padrao = new CifraSigea(chave);
        assertEquals(CamadaDifusao.MIXCOLUMNS_MDS, padrao.difusao());
        assertEquals(ModoPermutacao.ESPALHA_COLUNAS, padrao.permutacao());
        assertNotEquals(
                HEX.formatHex(new CifraSigea(chave, 10, CamadaDifusao.CADEIA_DUPLA).cifrarComNonce(new byte[16], nonce).textoCifrado()),
                HEX.formatHex(padrao.cifrarComNonce(new byte[16], nonce).textoCifrado()));
    }
}
