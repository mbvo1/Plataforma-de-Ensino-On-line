package dev.com.sigea.apresentacao.mapeamento;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

/**
 * Mapeador responsável por converter entre DTOs da camada de apresentação
 * e entidades de domínio.
 */
@Component
public class BackendMapeador {
    
    private final ModelMapper modelMapper;
    
    public BackendMapeador() {
        this.modelMapper = new ModelMapper();
        this.modelMapper.getConfiguration()
            .setMatchingStrategy(MatchingStrategies.STRICT)
            .setSkipNullEnabled(true);
        
        configurarConversores();
    }
    
    private void configurarConversores() {
        // Adicionar conversores customizados aqui se necessário
        // Exemplo: modelMapper.addConverter(...);
    }
    
    /**
     * Converte um DTO para uma entidade de domínio.
     */
    public <D, T> T paraDominio(D dto, Class<T> dominioClass) {
        if (dto == null) {
            return null;
        }
        return modelMapper.map(dto, dominioClass);
    }
    
    /**
     * Converte uma entidade de domínio para um DTO.
     */
    public <T, D> D paraDto(T dominio, Class<D> dtoClass) {
        if (dominio == null) {
            return null;
        }
        return modelMapper.map(dominio, dtoClass);
    }
    
    /**
     * Atualiza uma entidade de domínio com dados de um DTO.
     */
    public <D, T> void atualizarDominio(D dto, T dominio) {
        if (dto == null || dominio == null) {
            return;
        }
        modelMapper.map(dto, dominio);
    }
}

