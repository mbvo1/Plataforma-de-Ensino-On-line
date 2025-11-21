package dev.com.sigea.infraestrutura.persistencia.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioJpa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Long id;
    
    @Column(nullable = false)
    private String nome;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "senha_hash")
    private String senhaHash;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerfilJpa perfil;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UsuarioStatusJpa status;
    
    public enum PerfilJpa {
        ALUNO, PROFESSOR, ADMINISTRADOR
    }
    
    public enum UsuarioStatusJpa {
        ATIVO, INATIVO
    }
}

