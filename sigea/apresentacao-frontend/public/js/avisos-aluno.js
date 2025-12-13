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
    carregarAvisos();
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

async function carregarAvisos() {
    const container = document.getElementById('avisos-list');
    const usuarioId = localStorage.getItem('usuarioId');
    
    try {
        const response = await fetch(`http://localhost:8080/api/aluno/${usuarioId}/avisos`);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar avisos');
        }
        
        const avisos = await response.json();
        
        if (!avisos || avisos.length === 0) {
            container.innerHTML = '<p class="empty-state">Nenhum aviso disponível.</p>';
            return;
        }
        
        container.innerHTML = '';
        avisos.forEach(aviso => {
            const card = document.createElement('div');
            card.className = 'aviso-card';
            card.innerHTML = `
                <div class="aviso-header">
                    <h3>${aviso.titulo}</h3>
                    <span class="aviso-data">${formatarData(aviso.dataCriacao)}</span>
                </div>
                <p class="aviso-conteudo">${aviso.conteudo}</p>
                <span class="aviso-autor">Por: ${aviso.autor || 'Sistema'}</span>
            `;
            container.appendChild(card);
        });
        
    } catch (error) {
        console.error('Erro ao carregar avisos:', error);
        container.innerHTML = '<p class="empty-state">Nenhum aviso disponível.</p>';
    }
}

function formatarData(dataStr) {
    const data = new Date(dataStr);
    return data.toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
}

function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        localStorage.clear();
        window.location.href = '/';
    }
}
