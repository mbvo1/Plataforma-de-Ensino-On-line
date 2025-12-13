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
    carregarMatricula();
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

async function carregarMatricula() {
    const container = document.getElementById('matricula-content');
    const usuarioId = localStorage.getItem('usuarioId');
    
    try {
        const response = await fetch(`http://localhost:8080/api/aluno/${usuarioId}/matriculas`);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar matrículas');
        }
        
        const matriculas = await response.json();
        
        if (!matriculas || matriculas.length === 0) {
            container.innerHTML = `
                <p class="empty-state">Você não possui matrículas ativas.</p>
                <button class="btn-primary" onclick="abrirModalMatricula()">
                    <i class="fas fa-plus"></i> Realizar Matrícula
                </button>
            `;
            return;
        }
        
        let html = `
            <div class="matricula-header">
                <h2>Minhas Matrículas</h2>
                <button class="btn-primary" onclick="abrirModalMatricula()">
                    <i class="fas fa-plus"></i> Nova Matrícula
                </button>
            </div>
            <table class="matricula-table">
                <thead>
                    <tr>
                        <th>Disciplina</th>
                        <th>Sala</th>
                        <th>Status</th>
                        <th>Data Matrícula</th>
                    </tr>
                </thead>
                <tbody>
        `;
        
        matriculas.forEach(m => {
            html += `<tr>
                <td>${m.disciplinaNome}</td>
                <td>${m.salaIdentificador}</td>
                <td class="status-${m.status.toLowerCase()}">${m.status}</td>
                <td>${formatarData(m.dataMatricula)}</td>
            </tr>`;
        });
        
        html += '</tbody></table>';
        container.innerHTML = html;
        
    } catch (error) {
        console.error('Erro ao carregar matrículas:', error);
        container.innerHTML = `
            <p class="empty-state">Você não possui matrículas ativas.</p>
            <button class="btn-primary" onclick="abrirModalMatricula()">
                <i class="fas fa-plus"></i> Realizar Matrícula
            </button>
        `;
    }
}

function formatarData(dataStr) {
    if (!dataStr) return '-';
    const data = new Date(dataStr);
    return data.toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
}

function abrirModalMatricula() {
    alert('Funcionalidade de matrícula em desenvolvimento.');
}

function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        localStorage.clear();
        window.location.href = '/';
    }
}
