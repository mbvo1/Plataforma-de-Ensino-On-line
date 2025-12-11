// Verifica autenticação ao carregar a página
window.addEventListener('DOMContentLoaded', () => {
    const usuarioId = localStorage.getItem('usuarioId');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    
    if (!usuarioId || usuarioPerfil !== 'ADMINISTRADOR') {
        window.location.href = '/login-admin.html';
        return;
    }
    
    // Fecha modal ao carregar
    const modal = document.getElementById('modal-novo-aviso');
    if (modal) modal.style.display = 'none';
    
    loadUserInfo();
    carregarAvisos();
});

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    
    const userNameElement = document.getElementById('user-name');
    if (userNameElement) {
        userNameElement.textContent = `Admin - ${nome || 'Usuário'}`;
    }
}

// ========== ESTADO DA VISUALIZAÇÃO ==========

let mostrandoHistorico = false;

// ========== CARREGAR AVISOS NÃO LIDOS ==========

async function carregarAvisos() {
    const container = document.getElementById('avisos-container');
    let usuarioId = localStorage.getItem('usuarioId');
    
    // Se não houver usuarioId, usa 1 como padrão (admin)
    if (!usuarioId || usuarioId === 'undefined' || usuarioId === 'null') {
        usuarioId = '1';
        localStorage.setItem('usuarioId', '1');
    }
    
    try {
        const endpoint = mostrandoHistorico 
            ? `http://localhost:8080/api/avisos/historico?usuarioId=${usuarioId}`
            : `http://localhost:8080/api/avisos/nao-lidos?usuarioId=${usuarioId}`;
            
        const response = await fetch(endpoint);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar avisos');
        }
        
        const avisos = await response.json();
        
        if (avisos.length === 0) {
            const mensagem = mostrandoHistorico 
                ? 'Nenhum aviso no histórico.' 
                : 'Nenhum aviso não lido.';
            container.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-bell-slash"></i>
                    <p>${mensagem}</p>
                </div>
            `;
        } else {
            exibirAvisos(avisos);
        }
    } catch (error) {
        console.error('Erro ao carregar avisos:', error);
        container.innerHTML = `
            <div class="empty-state error-state">
                <i class="fas fa-exclamation-circle"></i>
                <p>Erro ao carregar avisos. Tente novamente.</p>
            </div>
        `;
    }
}

// ========== ALTERNAR ENTRE AVISOS NÃO LIDOS E HISTÓRICO ==========

function toggleHistorico(event) {
    event.preventDefault();
    mostrandoHistorico = !mostrandoHistorico;
    
    const link = event.target;
    link.textContent = mostrandoHistorico 
        ? 'Ver avisos não lidos' 
        : 'Ver histórico de avisos';
    
    carregarAvisos();
}

function exibirAvisos(avisos) {
    const container = document.getElementById('avisos-container');
    
    const html = avisos.map(aviso => {
        const badgeLido = aviso.lido ? '<span class="badge-lido">Lido</span>' : '';
        
        return `
        <div class="aviso-card ${aviso.lido ? 'aviso-lido' : ''}">
            <button class="btn-close-aviso" onclick="marcarComoLido('${aviso.id}')" title="Marcar como lido">
                <i class="fas fa-times"></i>
            </button>
            <div class="aviso-header">
                <h3>${aviso.titulo} ${badgeLido}</h3>
            </div>
            <div class="aviso-body">
                <p>${aviso.conteudo || ''}</p>
            </div>
            <div class="aviso-footer">
                <span class="aviso-destinatarios">
                    <i class="fas fa-users"></i> ${aviso.escopo || 'Geral'}
                </span>
                <button class="btn-cancelar" onclick="excluirAviso('${aviso.id}')">
                    Cancelar
                </button>
            </div>
        </div>
    `}).join('');
    
    container.innerHTML = html;
}

function formatarData(dataISO) {
    const data = new Date(dataISO);
    const dia = String(data.getDate()).padStart(2, '0');
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const ano = data.getFullYear();
    const hora = String(data.getHours()).padStart(2, '0');
    const minuto = String(data.getMinutes()).padStart(2, '0');
    
    return `${dia}/${mes}/${ano} às ${hora}:${minuto}`;
}

// ========== MODAL NOVO AVISO ==========

function abrirModalNovoAviso() {
    console.log('abrirModalNovoAviso() chamada');
    const modal = document.getElementById('modal-novo-aviso');
    modal.classList.add('show');
    
    // Limpa o formulário
    document.getElementById('form-novo-aviso').reset();
    console.log('Modal aviso aberto');
}

function fecharModalNovoAviso() {
    const modal = document.getElementById('modal-novo-aviso');
    modal.classList.remove('show');
}

// Fecha modal ao clicar fora dele
window.onclick = function(event) {
    const modal = document.getElementById('modal-novo-aviso');
    if (event.target === modal) {
        fecharModalNovoAviso();
    }
}

// ========== CRIAR AVISO ==========

async function criarAviso(event) {
    event.preventDefault();
    
    const titulo = document.getElementById('titulo-aviso').value.trim();
    const mensagem = document.getElementById('mensagem-aviso').value.trim();
    const destinatarios = document.getElementById('destinatarios-aviso').value;
    
    if (!titulo || !mensagem || !destinatarios) {
        alert('Preencha todos os campos obrigatórios!');
        return;
    }
    
    // Mapeia destinatários para escopo do backend
    let escopo;
    switch(destinatarios) {
        case 'TODOS':
            escopo = 'GERAL';
            break;
        case 'ALUNOS':
            escopo = 'ALUNOS';
            break;
        case 'PROFESSORES':
            escopo = 'PROFESSORES';
            break;
        default:
            escopo = 'GERAL';
    }
    
    const usuarioId = localStorage.getItem('usuarioId');
    
    const novoAviso = {
        titulo,
        conteudo: mensagem,
        autorId: usuarioId,
        disciplinaId: null,
        prioridade: 'MEDIA',
        escopo: escopo
    };
    
    try {
        const response = await fetch('http://localhost:8080/api/avisos', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(novoAviso)
        });
        
        if (response.ok) {
            alert('Aviso criado com sucesso!');
            fecharModalNovoAviso();
            carregarAvisos();
        } else {
            const erro = await response.text();
            alert('Erro ao criar aviso: ' + erro);
        }
        
    } catch (error) {
        console.error('Erro ao criar aviso:', error);
        alert('Erro ao criar aviso. Verifique se o servidor está rodando.');
    }
}

// ========== EDITAR AVISO ==========

async function editarAviso(id) {
    // TODO: Implementar edição
    console.log('Editar aviso:', id);
    alert('Funcionalidade de edição será implementada em breve.');
}

// ========== MARCAR COMO LIDO ==========

async function marcarComoLido(avisoId) {
    let usuarioId = localStorage.getItem('usuarioId');
    
    if (!usuarioId || usuarioId === 'undefined' || usuarioId === 'null') {
        usuarioId = '1';
    }
    
    try {
        const response = await fetch('http://localhost:8080/api/avisos/marcar-lido', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ avisoId, usuarioId })
        });
        
        if (response.ok) {
            // Remove o aviso da tela sem excluí-lo do banco
            carregarAvisos();
        } else {
            alert('Erro ao marcar aviso como lido.');
        }
        
    } catch (error) {
        console.error('Erro ao marcar como lido:', error);
        alert('Erro ao marcar aviso como lido. Verifique se o servidor está rodando.');
    }
}

// ========== EXCLUIR AVISO ==========

async function excluirAviso(id) {
    if (!confirm('Tem certeza que deseja cancelar/excluir este aviso?')) {
        return;
    }
    
    try {
        const response = await fetch(`http://localhost:8080/api/avisos/${id}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            alert('Aviso excluído com sucesso!');
            carregarAvisos();
        } else {
            alert('Erro ao excluir aviso.');
        }
        
    } catch (error) {
        console.error('Erro ao excluir aviso:', error);
        alert('Erro ao excluir aviso. Verifique se o servidor está rodando.');
    }
}

function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        localStorage.removeItem('usuarioId');
        localStorage.removeItem('usuarioNome');
        localStorage.removeItem('usuarioEmail');
        localStorage.removeItem('usuarioPerfil');
        
        window.location.href = '/login-admin.html';
    }
}
