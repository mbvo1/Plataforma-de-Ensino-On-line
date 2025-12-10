// Verifica autenticação ao carregar a página
window.addEventListener('DOMContentLoaded', () => {
    const usuarioId = localStorage.getItem('usuarioId');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    
    if (!usuarioId || usuarioPerfil !== 'ADMINISTRADOR') {
        window.location.href = '/login-admin.html';
        return;
    }
    
    loadUserInfo();
    carregarDadosDashboard();
});

async function carregarDadosDashboard() {
    try {
        // Carregar estatísticas
        const statsResponse = await fetch('http://localhost:8080/api/dashboard/stats');
        if (statsResponse.ok) {
            const stats = await statsResponse.json();
            atualizarEstatisticas(stats);
        }
        
        // Carregar últimos usuários
        const usuariosResponse = await fetch('http://localhost:8080/api/dashboard/ultimos-usuarios');
        if (usuariosResponse.ok) {
            const usuarios = await usuariosResponse.json();
            exibirUltimosUsuarios(usuarios);
        }
    } catch (error) {
        console.error('Erro ao carregar dados do dashboard:', error);
    }
}

function atualizarEstatisticas(stats) {
    document.getElementById('total-alunos').textContent = stats.totalAlunos;
    document.getElementById('total-professores').textContent = stats.totalProfessores;
    document.getElementById('total-disciplinas').textContent = stats.totalDisciplinas;
    document.getElementById('total-turmas').textContent = stats.totalTurmas;
}

function exibirUltimosUsuarios(usuarios) {
    const container = document.getElementById('ultimos-usuarios-lista');
    
    if (!usuarios || usuarios.length === 0) {
        container.innerHTML = '<p class="empty-message">Nenhum usuário cadastrado ainda</p>';
        return;
    }
    
    const html = usuarios.map(usuario => `
        <div class="user-item">
            <i class="fas ${usuario.perfil === 'PROFESSOR' ? 'fa-chalkboard-teacher' : 'fa-user-graduate'}"></i>
            <div class="user-info">
                <span class="user-name">${usuario.nome}</span>
                <span class="user-type">${usuario.perfil === 'PROFESSOR' ? 'Professor' : 'Aluno'}</span>
            </div>
        </div>
    `).join('');
    
    container.innerHTML = html;
}

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    
    // Atualiza o nome do usuário no header
    document.getElementById('user-name').textContent = `Admin - ${nome || 'Usuário'}`;
}

function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        localStorage.removeItem('usuarioId');
        localStorage.removeItem('usuarioNome');
        localStorage.removeItem('usuarioEmail');
        localStorage.removeItem('usuarioPerfil');
        
        window.location.href = '/login-admin.html';
    }
}
