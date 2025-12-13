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
    carregarTurmas();
});

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    
    const nomeValido = nome && 
                       nome !== 'null' && 
                       nome !== 'undefined' && 
                       nome.trim() !== '';
    
    const nomeExibir = nomeValido ? nome : 'Usuário';
    
    const userNameElement = document.getElementById('user-name');
    if (userNameElement) {
        userNameElement.textContent = `Aluno - ${nomeExibir}`;
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

async function carregarTurmas() {
    const alunoId = localStorage.getItem('usuarioId');
    const container = document.getElementById('turmas-container');
    
    try {
        const response = await fetch(`http://localhost:8080/api/aluno/${alunoId}/turmas`);
        
        if (response.ok) {
            const turmas = await response.json();
            
            if (turmas.length === 0) {
                container.innerHTML = `
                    <div class="empty-state-no-border">
                        <i class="fas fa-chalkboard-teacher empty-icon" style="font-size: 48px; color: #999; margin-bottom: 16px;"></i>
                        <p style="color: #666; font-size: 16px;">O aluno não está cadastrado em nenhuma sala.</p>
                    </div>
                `;
            } else {
                container.innerHTML = `
                    <div class="salas-grid">
                        ${turmas.map(turma => `
                            <div class="sala-card">
                                <div class="sala-header">
                                    <h3>${turma.titulo || turma.nomeTurma}</h3>
                                </div>
                                <div class="sala-info">
                                    <p><i class="fas fa-user-tie"></i> Professor: <strong>${turma.nomeProfessor}</strong></p>
                                </div>
                                <div class="sala-actions">
                                    <button class="btn-acessar" onclick="acessarTurma(${turma.turmaId || turma.id})">
                                        <i class="fas fa-door-open"></i> Acessar
                                    </button>
                                </div>
                            </div>
                        `).join('')}
                    </div>
                `;
            }
        } else {
            container.innerHTML = `
                <div class="empty-state-no-border">
                    <i class="fas fa-chalkboard-teacher empty-icon" style="font-size: 48px; color: #999; margin-bottom: 16px;"></i>
                    <p style="color: #666; font-size: 16px;">O aluno não está cadastrado em nenhuma sala.</p>
                </div>
            `;
        }
    } catch (error) {
        console.error('Erro ao carregar turmas:', error);
        container.innerHTML = `
            <div class="empty-state-no-border">
                <i class="fas fa-exclamation-triangle empty-icon" style="font-size: 48px; color: #f44336; margin-bottom: 16px;"></i>
                <p style="color: #666; font-size: 16px;">Erro ao carregar turmas</p>
            </div>
        `;
    }
}

function abrirModalEntrarSala() {
    document.getElementById('form-entrar-sala').reset();
    const modal = document.getElementById('modal-entrar-sala');
    modal.classList.add('show');
}

function fecharModalEntrarSala() {
    const modal = document.getElementById('modal-entrar-sala');
    modal.classList.remove('show');
}

async function entrarNaSala(event) {
    event.preventDefault();
    
    const codigo = document.getElementById('input-codigo-sala').value.trim().toUpperCase();
    const alunoId = localStorage.getItem('usuarioId');
    
    if (!codigo) {
        alert('Por favor, digite o código da sala.');
        return;
    }
    
    try {
        const response = await fetch(`http://localhost:8080/api/aluno/${alunoId}/turmas/entrar`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ codigoAcesso: codigo })
        });
        
        if (response.ok) {
            const turma = await response.json();
            alert(`Você entrou na turma "${turma.titulo || turma.nomeTurma}" com sucesso!`);
            fecharModalEntrarSala();
            carregarTurmas();
        } else {
            const error = await response.json();
            alert(error.erro || error.message || 'Código de sala inválido ou sala não encontrada.');
        }
    } catch (error) {
        console.error('Erro ao entrar na sala:', error);
        alert('Erro ao conectar com o servidor. Tente novamente.');
    }
}

function acessarTurma(turmaId) {
    window.location.href = `turma-detalhes-aluno.html?id=${turmaId}`;
}

function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        limparDadosUsuario();
        window.location.href = '/index.html';
    }
}
