// Array para armazenar todos os alunos
let todosAlunos = [];

function escapeHtml(texto) {
    if (texto === null || texto === undefined) return '';
    const div = document.createElement('div');
    div.textContent = String(texto);
    return div.innerHTML;
}

// Verifica autenticaÃ§Ã£o ao carregar a pÃ¡gina
window.addEventListener('DOMContentLoaded', () => {
    const usuarioId = localStorage.getItem('usuarioId');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    
    if (!usuarioId || usuarioPerfil !== 'ADMINISTRADOR') {
        window.location.href = '/login-admin.html';
        return;
    }
    
    loadUserInfo();
    carregarAlunos();
});

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    
    const userNameElement = document.getElementById('user-name');
    if (userNameElement) {
        userNameElement.textContent = `Admin - ${nome || 'UsuÃ¡rio'}`;
    }
}

async function carregarAlunos() {
    try {
        // Busca dados reais do banco de dados
        const response = await fetch('/api/admin/alunos', {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });

        if (!response.ok) {
            throw new Error('Erro ao buscar alunos');
        }
        
        todosAlunos = await response.json();
        exibirAlunos(todosAlunos);
    } catch (error) {
        console.error('Erro ao carregar alunos:', error);
        const tbody = document.getElementById('tabela-alunos');
        tbody.innerHTML = '<tr><td colspan="4" class="error-message">Erro ao carregar alunos</td></tr>';
    }
}

function exibirAlunos(alunos) {
    const tbody = document.getElementById('tabela-alunos');
    
    if (!alunos || alunos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="empty-message">Nenhum aluno encontrado</td></tr>';
        return;
    }
    
    const html = alunos.map(aluno => {
        // Formata o status para exibiÃ§Ã£o (ATIVO â†’ Ativo, INATIVO â†’ Inativo)
        const statusFormatado = aluno.status.charAt(0) + aluno.status.slice(1).toLowerCase();
        const statusClass = aluno.status.toLowerCase();
        
        return `
        <tr>
            <td>${escapeHtml(aluno.nome)}</td>
            <td>${aluno.email}</td>
            <td>
                <span class="status-badge ${statusClass}">${statusFormatado}</span>
            </td>
            <td>
                <button class="btn-visualizar" onclick="visualizarDetalhes(${aluno.id})">
                    Visualizar Detalhes
                </button>
            </td>
        </tr>
        `;
    }).join('');
    
    tbody.innerHTML = html;
}

function filtrarAlunos() {
    const searchTerm = document.getElementById('search-input').value.toLowerCase().trim();
    
    if (searchTerm === '') {
        exibirAlunos(todosAlunos);
        return;
    }
    
    const alunosFiltrados = todosAlunos.filter(aluno => {
        const nomeMatch = aluno.nome.toLowerCase().includes(searchTerm);
        const emailMatch = aluno.email.toLowerCase().includes(searchTerm);
        return nomeMatch || emailMatch;
    });
    
    exibirAlunos(alunosFiltrados);
}

function visualizarDetalhes(alunoId) {
    // Navega para a pÃ¡gina de detalhes do aluno
    window.location.href = `/detalhes-aluno.html?id=${alunoId}`;
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
