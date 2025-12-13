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

async function loadDashboardData() {
    const usuarioId = localStorage.getItem('usuarioId');
    
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
    
    // Carrega dados do backend
    await carregarTurmas(usuarioId);
    await carregarDisciplinas(usuarioId);
    await carregarAulasHoje(usuarioId, today);
    await carregarMensagensNovas(usuarioId);
}

// Carrega turmas criadas pelo professor
async function carregarTurmas(professorId) {
    const container = document.getElementById('turmas-container');
    if (!container) return;
    
    try {
        const response = await fetch(`http://localhost:8080/api/professor/turmas?professorId=${professorId}`);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar turmas');
        }
        
        const turmas = await response.json();
        
        if (!turmas || turmas.length === 0) {
            container.innerHTML = '<p class="empty-state">Nenhuma turma criada</p>';
            return;
        }
        
        container.innerHTML = '';
        
        turmas.forEach(turma => {
            const li = document.createElement('li');
            li.className = 'disciplina-item';
            li.textContent = turma.titulo;
            li.onclick = () => {
                window.location.href = `turma-detalhes.html?id=${turma.turmaId}`;
            };
            container.appendChild(li);
        });
        
    } catch (error) {
        console.error('Erro ao carregar turmas:', error);
        container.innerHTML = '<p class="empty-state">Nenhuma turma criada</p>';
    }
}

// Carrega disciplinas (salas) do professor para Lançar Notas/Faltas
async function carregarDisciplinas(professorId) {
    const container = document.getElementById('disciplinas-list');
    if (!container) return;
    
    try {
        const response = await fetch(`http://localhost:8080/api/professor/disciplinas?professorId=${professorId}`);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar disciplinas');
        }
        
        const disciplinas = await response.json();
        
        if (!disciplinas || disciplinas.length === 0) {
            container.innerHTML = '<li class="empty-state">Nenhuma disciplina encontrada</li>';
            return;
        }
        
        container.innerHTML = '';
        
        disciplinas.forEach(d => {
            const li = document.createElement('li');
            li.className = 'disciplina-item';
            li.textContent = `${d.nome} - ${d.identificador}`;
            li.onclick = () => {
                const params = new URLSearchParams({
                    salaId: d.salaId,
                    disciplina: d.nome,
                    sala: d.identificador
                });
                window.location.href = `disciplina-detalhes-professor.html?${params.toString()}`;
            };
            container.appendChild(li);
        });
        
    } catch (error) {
        console.error('Erro ao carregar disciplinas:', error);
        container.innerHTML = '<li class="empty-state">Erro ao carregar disciplinas</li>';
    }
}

// Carrega aulas do dia baseado no horário das salas
async function carregarAulasHoje(professorId, hoje) {
    const container = document.getElementById('aulas-list');
    if (!container) return;
    
    try {
        // Obtém o dia da semana atual
        const diasSemana = ['domingo', 'segunda', 'terca', 'quarta', 'quinta', 'sexta', 'sabado'];
        const diaAtual = diasSemana[hoje.getDay()];
        
        const response = await fetch(`http://localhost:8080/api/professor/aulas-hoje?professorId=${professorId}&diaSemana=${diaAtual}`);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar aulas');
        }
        
        const aulas = await response.json();
        
        if (!aulas || aulas.length === 0) {
            container.innerHTML = '<li class="empty-state">Não há aulas hoje</li>';
            return;
        }
        
        container.innerHTML = '';
        
        aulas.forEach(aula => {
            const li = document.createElement('li');
            li.className = 'aula-item';
            li.textContent = `${aula.disciplinaNome} - ${aula.salaIdentificador}: ${aula.horario}`;
            container.appendChild(li);
        });
        
    } catch (error) {
        console.error('Erro ao carregar aulas:', error);
        container.innerHTML = '<li class="empty-state">Não há aulas hoje</li>';
    }
}

// Carrega mensagens novas do fórum
async function carregarMensagensNovas(professorId) {
    const container = document.getElementById('forum-list');
    if (!container) return;
    
    try {
        const response = await fetch(`http://localhost:8080/api/professor/forum/mensagens-novas?professorId=${professorId}`);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar mensagens');
        }
        
        const mensagens = await response.json();
        
        if (!mensagens || mensagens.length === 0) {
            container.innerHTML = '<li class="empty-state">Nenhuma mensagem nova</li>';
            return;
        }
        
        container.innerHTML = '';
        
        mensagens.forEach(msg => {
            const li = document.createElement('li');
            li.className = 'forum-item';
            li.textContent = `${msg.autorNome} - ${msg.disciplinaNome}: ${msg.titulo}`;
            li.onclick = () => {
                window.location.href = `topico-detalhes-professor.html?topicoId=${msg.topicoId}&disciplinaId=${msg.disciplinaId}`;
            };
            container.appendChild(li);
        });
        
    } catch (error) {
        console.error('Erro ao carregar mensagens:', error);
        container.innerHTML = '<li class="empty-state">Nenhuma mensagem nova</li>';
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

function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        localStorage.removeItem('usuarioId');
        localStorage.removeItem('usuarioNome');
        localStorage.removeItem('usuarioEmail');
        localStorage.removeItem('usuarioPerfil');
        
        window.location.href = '/login-professor.html';
    }
}
