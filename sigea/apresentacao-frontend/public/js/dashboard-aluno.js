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
    carregarDadosDashboard();
});

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    
    // Debug - verificar o que está no localStorage
    console.log('DEBUG loadUserInfo - usuarioNome:', nome);
    console.log('DEBUG loadUserInfo - localStorage completo:', {
        usuarioId: localStorage.getItem('usuarioId'),
        usuarioNome: localStorage.getItem('usuarioNome'),
        usuarioEmail: localStorage.getItem('usuarioEmail'),
        usuarioPerfil: localStorage.getItem('usuarioPerfil')
    });
    
    // Verifica se o nome é válido (não null, não undefined, não "null", não "undefined", não vazio)
    const nomeValido = nome && 
                       nome !== 'null' && 
                       nome !== 'undefined' && 
                       nome.trim() !== '';
    
    const nomeExibir = nomeValido ? nome : 'Usuário';
    
    // Atualiza o nome do usuário no header
    const userNameElement = document.getElementById('user-name');
    if (userNameElement) {
        userNameElement.textContent = `Aluno - ${nomeExibir}`;
    }
    
    // Atualiza o nome no título de boas-vindas
    const welcomeNameElement = document.getElementById('welcome-name');
    if (welcomeNameElement) {
        welcomeNameElement.textContent = nomeExibir;
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

async function carregarDadosDashboard() {
    const usuarioId = localStorage.getItem('usuarioId');
    
    try {
        const response = await fetch(`http://localhost:8080/api/dashboard/aluno/${usuarioId}`);
        
        if (response.ok) {
            const dados = await response.json();
            atualizarDashboard(dados);
        } else {
            console.error('Erro ao carregar dados do dashboard');
        }
    } catch (error) {
        console.error('Erro ao conectar com o servidor:', error);
    }
}

function atualizarDashboard(dados) {
    // Atualizar avisos
    const avisosCard = document.getElementById('avisos-card');
    if (avisosCard) {
        if (dados.avisosNaoLidos > 0) {
            avisosCard.innerHTML = `<span style="color: #e74c3c; font-weight: bold;">Você tem ${dados.avisosNaoLidos} avisos não lidos</span>`;
        } else {
            avisosCard.innerHTML = `<span style="color: #27ae60;">Nenhum aviso novo</span>`;
        }
    }
    
    // Atualizar frequência
    const frequenciaContent = document.getElementById('frequencia-content');
    if (frequenciaContent) {
        frequenciaContent.innerHTML = `
            <p class="freq-title">Total</p>
            <p><strong>Faltas:</strong> ${dados.totalFaltas}</p>
            <p><strong>Frequência:</strong> ${dados.frequenciaPercentual.toFixed(0)}%</p>
        `;
    }
}

function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        localStorage.removeItem('usuarioId');
        localStorage.removeItem('usuarioNome');
        localStorage.removeItem('usuarioEmail');
        localStorage.removeItem('usuarioPerfil');
        window.location.href = '/index.html';
    }
}
