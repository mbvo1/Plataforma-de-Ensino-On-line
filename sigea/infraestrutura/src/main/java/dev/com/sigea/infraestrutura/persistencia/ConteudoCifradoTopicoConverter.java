package dev.com.sigea.infraestrutura.persistencia;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

/**
 * Projeto 2: topicos.conteudo em repouso com a CifraSigea. Um converter por
 * coluna so para o log saber de onde veio cada operacao (recurso
 * "topicos.conteudo"); a cifra e a mesma, em CifraConteudo.
 *
 * Nonce aleatorio por gravacao, ao contrario do CPF: aqui ninguem busca pelo
 * conteudo nem ha UNIQUE na coluna, entao o mesmo texto pode (e deve) virar
 * textos cifrados diferentes.
 */
@Component
@Converter
public class ConteudoCifradoTopicoConverter implements AttributeConverter<String, String> {

    private static final String RECURSO = "topicos.conteudo";

    private final CifraConteudo cifra;

    public ConteudoCifradoTopicoConverter(CifraConteudo cifra) {
        this.cifra = cifra;
    }

    @Override
    public String convertToDatabaseColumn(String textoClaro) {
        return cifra.cifrar(RECURSO, textoClaro);
    }

    @Override
    public String convertToEntityAttribute(String valorColuna) {
        return cifra.decifrarOuIndisponivel(RECURSO, valorColuna);
    }
}
