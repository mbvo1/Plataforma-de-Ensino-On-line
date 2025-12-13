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
    carregarForum();
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

async function carregarForum() {
    const container = document.getElementById('forum-content');
    const usuarioId = localStorage.getItem('usuarioId');
    
    try {
        // Primeiro busca as disciplinas do aluno
        const discResponse = await fetch(`http://localhost:8080/api/aluno/${usuarioId}/disciplinas`);
        
        if (!discResponse.ok) {
            throw new Error('Erro ao carregar disciplinas');
        }
        
        const disciplinas = await discResponse.json();
        
        if (!disciplinas || disciplinas.length === 0) {
            container.innerHTML = '<p class="empty-state">Você precisa estar matriculado em disciplinas para acessar o fórum.</p>';
            return;
        }
        
        container.innerHTML = '<h2>Selecione uma disciplina:</h2>';
        
        const listaDisciplinas = document.createElement('ul');
        listaDisciplinas.className = 'disciplinas-forum-list';
        
        disciplinas.forEach(disc => {
            const li = document.createElement('li');
            li.className = 'disciplina-forum-item';
            li.innerHTML = `<i class="fas fa-book"></i> ${disc.nome}`;
            li.onclick = () => {
                window.location.href = `forum-disciplina-aluno.html?disciplinaId=${disc.id}&disciplinaNome=${encodeURIComponent(disc.nome)}`;
            };
            listaDisciplinas.appendChild(li);
        });
        
        container.appendChild(listaDisciplinas);
        
    } catch (error) {
        console.error('Erro ao carregar fórum:', error);
        container.innerHTML = '<p class="empty-state">Você precisa estar matriculado em disciplinas para acessar o fórum.</p>';
    }
}

function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        localStorage.clear();
        window.location.href = '/';
    }
}
