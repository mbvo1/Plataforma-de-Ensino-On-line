// Verifica autenticação ao carregar a página
window.addEventListener('DOMContentLoaded', () => {
    const usuarioId = localStorage.getItem('usuarioId');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    
    if (!usuarioId || usuarioPerfil !== 'PROFESSOR') {
        window.location.href = '/login-professor.html';
        return;
    }
    
    loadUserInfo();
    loadDashboardData();
    initializeMenuToggle();
});

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    
    // Atualiza o nome do usuário no header
    const userNameElement = document.getElementById('user-name');
    if (userNameElement) {
        userNameElement.textContent = `Professor - ${nome || 'Usuário'}`;
    }
}

function loadDashboardData() {
    // Carrega a data de hoje
    const today = new Date();
    const dateString = today.toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
    
    const dateTodayElement = document.getElementById('date-today');
    if (dateTodayElement) {
        dateTodayElement.textContent = dateString;
    }
    
    // TODO: Carregar dados reais do backend
    // - Minhas turmas
    // - Aulas do dia (disciplinas que o professor é responsável)
    // - Lançar Notas/Faltas (todas as salas das disciplinas)
    // - Fórum (mensagens novas)
}

function initializeMenuToggle() {
    const menuToggle = document.getElementById('menuToggle');
    const sidebar = document.getElementById('sidebar');
    
    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', () => {
            sidebar.classList.toggle('collapsed');
        });
    }
}

function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        localStorage.removeItem('usuarioId');
        localStorage.removeItem('usuarioNome');
        localStorage.removeItem('usuarioEmail');
        localStorage.removeItem('usuarioPerfil');
        
        window.location.href = '/login-professor.html';
    }
}
