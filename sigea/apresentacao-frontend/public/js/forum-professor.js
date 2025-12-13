// Verificação de autenticação
window.addEventListener('DOMContentLoaded', () => {
    const usuarioId = localStorage.getItem('usuarioId');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    
    console.log('Perfil atual:', usuarioPerfil);
    console.log('Usuario ID:', usuarioId);
    
    if (!usuarioId || usuarioPerfil !== 'PROFESSOR') {
        alert('Você precisa fazer login como PROFESSOR para acessar esta página.\n\nPerfil atual: ' + (usuarioPerfil || 'Não logado'));
        window.location.href = '/login-professor.html';
        return;
    }

    loadUserInfo();
    carregarDisciplinas();
});

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    const userNameElement = document.getElementById('user-name');
    if (userNameElement) {
        userNameElement.textContent = `Professor - ${nome || 'Usuário'}`;
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

// Toggle da sidebar
document.addEventListener('DOMContentLoaded', function() {
    const menuToggle = document.getElementById('menuToggle');
    const sidebar = document.getElementById('sidebar');
    
    if (menuToggle) {
        menuToggle.addEventListener('click', function() {
            sidebar.classList.toggle('collapsed');
        });
    }
});

async function carregarDisciplinas() {
    const loadingElement = document.getElementById('loading');
    const gridElement = document.getElementById('forum-grid');
    const noForumsElement = document.getElementById('no-forums');
    const usuarioId = localStorage.getItem('usuarioId');

    console.log('Carregando disciplinas para professor ID:', usuarioId);

    try {
        loadingElement.style.display = 'block';
        gridElement.style.display = 'none';
        noForumsElement.style.display = 'none';

        // Busca as salas (disciplinas) do professor
        const url = `http://localhost:8080/api/professor/salas?professorId=${usuarioId}`;
        console.log('Fazendo requisição para:', url);
        
        const response = await fetch(url);
        
        console.log('Status da resposta:', response.status);
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error('Erro na resposta:', errorText);
            throw new Error('Erro ao carregar disciplinas: ' + response.status);
        }

        const salas = await response.json();
        console.log('Salas recebidas:', salas);
        
        loadingElement.style.display = 'none';

        if (salas.length === 0) {
            noForumsElement.style.display = 'block';
            return;
        }

        // Agrupa salas por disciplina (remove duplicatas)
        const disciplinasUnicas = new Map();
        salas.forEach(sala => {
            if (!disciplinasUnicas.has(sala.disciplinaId)) {
                disciplinasUnicas.set(sala.disciplinaId, sala);
            }
        });

        // Renderiza os cards das disciplinas únicas
        gridElement.style.display = 'grid';
        gridElement.innerHTML = '';

        disciplinasUnicas.forEach(sala => {
            const card = criarCardDisciplina(sala);
            gridElement.appendChild(card);
        });

    } catch (error) {
        console.error('Erro ao carregar disciplinas:', error);
        loadingElement.style.display = 'none';
        noForumsElement.style.display = 'block';
        noForumsElement.innerHTML = `
            <i class="fas fa-exclamation-triangle"></i>
            <p>Erro ao carregar fóruns.</p>
            <p style="font-size: 0.9rem; margin-top: 0.5rem;">Tente novamente mais tarde.</p>
        `;
    }
}

function criarCardDisciplina(sala) {
    const card = document.createElement('div');
    card.className = 'forum-card';
    card.onclick = () => abrirForum(sala.disciplinaId, sala.disciplinaNome);

    card.innerHTML = `
        <div class="forum-card-title">${sala.disciplinaNome || 'Disciplina ' + sala.disciplinaId}</div>
        <div class="forum-card-professor">
            <i class="fas fa-user"></i>
            <span>${localStorage.getItem('usuarioNome') || 'Professor'}</span>
        </div>
    `;

    return card;
}

function abrirForum(disciplinaId, disciplinaNome) {
    // Salva informações da disciplina para usar na próxima página
    localStorage.setItem('forumDisciplinaId', disciplinaId);
    localStorage.setItem('forumDisciplinaNome', disciplinaNome);
    
    // Redireciona para página de detalhes do fórum
    window.location.href = `forum-detalhes-professor.html?disciplinaId=${disciplinaId}`;
}
