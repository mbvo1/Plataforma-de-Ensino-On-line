// Verificação de autenticação
window.addEventListener('DOMContentLoaded', () => {
    const usuarioId = localStorage.getItem('usuarioId');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    
    if (!usuarioId || usuarioPerfil !== 'ALUNO') {
        alert('Você precisa fazer login como ALUNO para acessar esta página.');
        window.location.href = '/index.html';
        return;
    }

    loadUserInfo();
    carregarDisciplinas();
});

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    const userNameElement = document.getElementById('user-name');
    if (userNameElement) {
        userNameElement.textContent = `Aluno - ${nome || 'Usuário'}`;
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

    console.log('Carregando disciplinas para aluno ID:', usuarioId);

    try {
        loadingElement.style.display = 'block';
        gridElement.style.display = 'none';
        noForumsElement.style.display = 'none';

        // Busca as disciplinas do aluno através do endpoint de matrículas
        const url = `http://localhost:8080/api/aluno/${usuarioId}/matriculas`;
        console.log('Fazendo requisição para:', url);
        
        const response = await fetch(url);
        
        console.log('Status da resposta:', response.status);
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error('Erro na resposta:', errorText);
            throw new Error('Erro ao carregar disciplinas: ' + response.status);
        }

        const matriculas = await response.json();
        console.log('Matrículas recebidas:', matriculas);
        
        loadingElement.style.display = 'none';

        if (!matriculas || matriculas.length === 0) {
            noForumsElement.style.display = 'block';
            return;
        }

        // Agrupa disciplinas por ID (remove duplicatas) - o endpoint retorna matrículas com disciplinaId
        const disciplinasUnicas = new Map();
        matriculas.forEach(matricula => {
            const disciplinaId = matricula.disciplinaId;
            if (disciplinaId && !disciplinasUnicas.has(disciplinaId)) {
                // Cria objeto disciplina a partir da matrícula
                disciplinasUnicas.set(disciplinaId, {
                    id: disciplinaId,
                    nome: matricula.disciplinaNome || `Disciplina ${disciplinaId}`,
                    professorNome: matricula.professorNome || 'Professor'
                });
            }
        });

        // Renderiza os cards das disciplinas únicas
        gridElement.style.display = 'grid';
        gridElement.innerHTML = '';

        disciplinasUnicas.forEach(disc => {
            const card = criarCardDisciplina(disc);
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

function criarCardDisciplina(disc) {
    const card = document.createElement('div');
    card.className = 'forum-card';
    card.onclick = () => abrirForum(disc.id, disc.nome);

    // Busca nome do professor da disciplina (se disponível)
    const professorNome = disc.professorNome || 'Professor';

    card.innerHTML = `
        <div class="forum-card-title">${disc.nome || 'Disciplina ' + disc.id}</div>
        <div class="forum-card-professor">
            <i class="fas fa-user"></i>
            <span>${professorNome}</span>
        </div>
    `;

    return card;
}

function abrirForum(disciplinaId, disciplinaNome) {
    // Salva informações da disciplina para usar na próxima página
    localStorage.setItem('forumDisciplinaId', disciplinaId);
    localStorage.setItem('forumDisciplinaNome', disciplinaNome);
    
    // Redireciona para página de detalhes do fórum
    window.location.href = `forum-detalhes-aluno.html?disciplinaId=${disciplinaId}`;
}
