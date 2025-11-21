package dev.com.sigea.infraestrutura.mapeamento;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Component
public class JpaMapeador {
    
    private final ModelMapper modelMapper;
    
    public JpaMapeador() {
        this.modelMapper = new ModelMapper();
        this.modelMapper.getConfiguration()
            .setMatchingStrategy(MatchingStrategies.STRICT)
            .setSkipNullEnabled(true);
        
        // Configurar conversores customizados aqui se necessário
        configurarConversores();
    }
    
    private void configurarConversores() {
        // Adicionar conversores customizados aqui
        // Exemplo: modelMapper.addConverter(...);
    }
    
    public <D, J> J paraJpa(D dominio, Class<J> jpaClass) {
        if (dominio == null) {
            return null;
        }
        return modelMapper.map(dominio, jpaClass);
    }
    
    public <J, D> D paraDominio(J jpa, Class<D> dominioClass) {
        if (jpa == null) {
            return null;
        }
        return modelMapper.map(jpa, dominioClass);
    }
    
    public <D, J> void atualizarJpa(D dominio, J jpa) {
        if (dominio == null || jpa == null) {
            return;
        }
        modelMapper.map(dominio, jpa);
    }
}

