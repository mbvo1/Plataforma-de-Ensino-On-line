// Função para limpar dados do usuário
function limparDadosUsuario() {
    localStorage.removeItem('usuarioId');
    localStorage.removeItem('usuarioNome');
    localStorage.removeItem('usuarioEmail');
    localStorage.removeItem('usuarioPerfil');
}

// Verifica se o login é válido
function isLoginValido() {
    const usuarioId = localStorage.getItem('usuarioId');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    
    return usuarioId && 
           usuarioPerfil && 
           usuarioId !== 'null' && 
           usuarioId !== 'undefined' && 
           usuarioId.trim() !== '' &&
           usuarioPerfil !== 'null' && 
           usuarioPerfil !== 'undefined' &&
           usuarioPerfil.trim() !== '' &&
           usuarioPerfil === 'ALUNO';
}

// Verifica autenticação ao carregar a página
window.addEventListener('DOMContentLoaded', () => {
    if (!isLoginValido()) {
        limparDadosUsuario();
        window.location.href = '/index.html';
        return;
    }
    
    loadUserInfo();
    initializeMenuToggle();
    carregarDesempenho();
});

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    const userNameElement = document.getElementById('user-name');
    if (userNameElement) {
        userNameElement.textContent = `Aluno - ${nome || 'Usuário'}`;
    }
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

async function carregarDesempenho() {
    const container = document.getElementById('desempenho-content');
    const usuarioId = localStorage.getItem('usuarioId');
    
    try {
        const response = await fetch(`http://localhost:8080/api/aluno/${usuarioId}/desempenho`);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar desempenho');
        }
        
        const desempenho = await response.json();
        
        if (!desempenho || desempenho.length === 0) {
            container.innerHTML = '<p class="empty-state">Nenhuma nota registrada ainda.</p>';
            return;
        }
        
        let html = '<table class="desempenho-table"><thead><tr><th>Disciplina</th><th>AV1</th><th>AV2</th><th>Média</th><th>Situação</th></tr></thead><tbody>';
        
        desempenho.forEach(d => {
            const media = ((d.av1 || 0) + (d.av2 || 0)) / 2;
            const situacao = media >= 7 ? 'Aprovado' : media >= 4 ? 'Recuperação' : 'Reprovado';
            html += `<tr>
                <td>${d.disciplina}</td>
                <td>${d.av1 !== null ? d.av1.toFixed(1) : '-'}</td>
                <td>${d.av2 !== null ? d.av2.toFixed(1) : '-'}</td>
                <td>${media.toFixed(1)}</td>
                <td class="situacao-${situacao.toLowerCase()}">${situacao}</td>
            </tr>`;
        });
        
        html += '</tbody></table>';
        container.innerHTML = html;
        
    } catch (error) {
        console.error('Erro ao carregar desempenho:', error);
        container.innerHTML = '<p class="empty-state">Nenhuma nota registrada ainda.</p>';
    }
}

function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        localStorage.clear();
        window.location.href = '/';
    }
}
