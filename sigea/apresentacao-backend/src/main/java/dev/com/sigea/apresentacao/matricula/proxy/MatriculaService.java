package dev.com.sigea.apresentacao.matricula.proxy;

/**
 * Proxy Pattern - Interface para serviço de matrícula
 */
public interface MatriculaService {
    void matricular(String salaId, String alunoId);
    void cancelar(String matriculaId);
}
