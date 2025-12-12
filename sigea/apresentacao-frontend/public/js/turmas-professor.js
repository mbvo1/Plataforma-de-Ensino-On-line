// Verifica autenticação ao carregar a página
window.addEventListener('DOMContentLoaded', () => {
    const usuarioId = localStorage.getItem('usuarioId');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    
    if (!usuarioId || usuarioPerfil !== 'PROFESSOR') {
        window.location.href = '/login-professor.html';
        return;
    }
    
    loadUserInfo();
    carregarSalas();
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

function carregarSalas() {
    const professorId = localStorage.getItem('usuarioId');
    
    fetch(`http://localhost:8080/api/professor/turmas?professorId=${professorId}`)
        .then(response => response.json())
        .then(turmas => {
            const container = document.getElementById('salas-container');
            
            if (turmas.length === 0) {
                container.innerHTML = `
                    <div class="empty-state-salas">
                        <i class="fas fa-chalkboard-teacher empty-icon"></i>
                        <p>Sem salas cadastradas</p>
                    </div>
                `;
            } else {
                container.innerHTML = `
                    <div class="salas-grid">
                        ${turmas.map(turma => `
                            <div class="sala-card">
                                <div class="sala-header">
                                    <h3>${turma.titulo}</h3>
                                </div>
                                <div class="sala-info">
                                    <p><i class="fas fa-user-tie"></i> Criado por: <strong>${turma.nomeProfessor}</strong></p>
                                </div>
                                <div class="sala-actions">
                                    <button class="btn-acessar" onclick="acessarSala(${turma.turmaId})">
                                        <i class="fas fa-door-open"></i> Acessar
                                    </button>
                                </div>
                            </div>
                        `).join('')}
                    </div>
                `;
            }
        })
        .catch(error => {
            console.error('Erro ao carregar turmas:', error);
            const container = document.getElementById('salas-container');
            container.innerHTML = `
                <div class="empty-state-salas">
                    <i class="fas fa-exclamation-triangle empty-icon" style="color: #f44336;"></i>
                    <p>Erro ao carregar turmas</p>
                </div>
            `;
        });
}

function criarSala() {
    // Limpa o formulário
    document.getElementById('form-criar-sala').reset();
    
    // Abre o modal
    const modal = document.getElementById('modal-criar-sala');
    modal.classList.add('show');
}

function fecharModalCriarSala() {
    const modal = document.getElementById('modal-criar-sala');
    modal.classList.remove('show');
}

async function salvarSala(event) {
    event.preventDefault();
    
    const nomeSala = document.getElementById('input-nome-sala').value.trim();
    const professorId = localStorage.getItem('usuarioId');
    
    // Gera código de acesso aleatório de 6 caracteres (letras e números)
    const codigoAcesso = gerarCodigoAcesso();
    
    const turmaData = {
        nomeTurma: nomeSala,
        codigoAcesso: codigoAcesso
    };
    
    try {
        const response = await fetch(`http://localhost:8080/api/professor/turmas?professorId=${professorId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(turmaData)
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Erro ao criar turma');
        }
        
        const turmaCriada = await response.json();
        
        alert(`Turma criada com sucesso!\n\nNome: ${turmaCriada.titulo}\nCriado por: ${turmaCriada.nomeProfessor}`);
        
        fecharModalCriarSala();
        carregarSalas();
    } catch (error) {
        console.error('Erro ao criar turma:', error);
        alert('Erro ao criar turma: ' + error.message);
    }
}

function gerarCodigoAcesso() {
    const caracteres = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    let codigo = '';
    
    for (let i = 0; i < 6; i++) {
        const indiceAleatorio = Math.floor(Math.random() * caracteres.length);
        codigo += caracteres[indiceAleatorio];
    }
    
    return codigo;
}

function acessarSala(salaId) {
    window.location.href = `turma-detalhes.html?id=${salaId}`;
}

function entrarEmSala() {
    // TODO: Implementar modal para entrar em sala com código
    console.log('Entrar em sala clicado');
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
