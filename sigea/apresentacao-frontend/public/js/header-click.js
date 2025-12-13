// header-click.js - Script comum para tornar headers clicáveis

document.addEventListener('DOMContentLoaded', () => {
    const headerRight = document.querySelector('.header-right');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    
    if (headerRight && usuarioPerfil) {
        // Remove qualquer onclick existente para evitar conflitos
        headerRight.removeAttribute('onclick');
        
        // Adiciona evento de clique
        headerRight.addEventListener('click', () => {
            if (usuarioPerfil === 'ALUNO') {
                window.location.href = 'perfil-aluno.html';
            } else if (usuarioPerfil === 'PROFESSOR') {
                window.location.href = 'perfil-professor.html';
            }
        });
        
        // Adiciona estilo de cursor pointer se não tiver
        if (!headerRight.style.cursor) {
            headerRight.style.cursor = 'pointer';
        }
    }
});

