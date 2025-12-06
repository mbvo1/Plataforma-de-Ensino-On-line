// Verifica autenticação ao carregar a página
window.addEventListener('DOMContentLoaded', () => {
    const usuarioId = localStorage.getItem('usuarioId');
    
    if (!usuarioId) {
        window.location.href = '/';
        return;
    }
    
    loadUserInfo();
});

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    const email = localStorage.getItem('usuarioEmail');
    const perfil = localStorage.getItem('usuarioPerfil');
    
    document.getElementById('user-name').textContent = nome || 'Usuário';
    document.getElementById('info-nome').textContent = nome || '-';
    document.getElementById('info-email').textContent = email || '-';
    document.getElementById('info-perfil').textContent = perfil || '-';
}

function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        localStorage.removeItem('usuarioId');
        localStorage.removeItem('usuarioNome');
        localStorage.removeItem('usuarioEmail');
        localStorage.removeItem('usuarioPerfil');
        
        window.location.href = '/';
    }
}
