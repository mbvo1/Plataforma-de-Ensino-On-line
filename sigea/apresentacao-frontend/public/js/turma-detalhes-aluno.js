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

// Pega o ID da turma da URL
const urlParams = new URLSearchParams(window.location.search);
const turmaId = urlParams.get('id');

// Verifica autenticação ao carregar a página
window.addEventListener('DOMContentLoaded', () => {
    if (!isLoginValido()) {
        limparDadosUsuario();
        window.location.href = '/index.html';
        return;
    }
    
    if (!turmaId) {
        alert('Turma não encontrada!');
        window.location.href = 'turmas-aluno.html';
        return;
    }
    
    loadUserInfo();
    initializeMenuToggle();
    carregarTurma();
});

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    
    const nomeValido = nome && 
                       nome !== 'null' && 
                       nome !== 'undefined' && 
                       nome.trim() !== '';
    
    const nomeExibir = nomeValido ? nome : 'Usuário';
    
    const userNameElement = document.getElementById('userName');
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

// Carrega dados da turma
async function carregarTurma() {
    const alunoId = localStorage.getItem('usuarioId');
    
    try {
        // Busca a turma específica do aluno
        const response = await fetch(`http://localhost:8080/api/aluno/${alunoId}/turmas/${turmaId}`);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar turma');
        }
        
        const turma = await response.json();
        
        // Atualiza o nome da turma
        document.getElementById('turmaNome').textContent = turma.titulo;
        
        // Carrega os avisos e atividades da turma
        carregarAvisos();
        carregarAtividades();
        
    } catch (error) {
        console.error('Erro ao carregar turma:', error);
        document.getElementById('turmaNome').textContent = 'Turma';
        // Mesmo se falhar, tenta carregar avisos e atividades
        carregarAvisos();
        carregarAtividades();
    }
}

// Carrega avisos da turma
async function carregarAvisos() {
    try {
        const response = await fetch(`http://localhost:8080/api/aluno/turmas/${turmaId}/avisos`);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar avisos');
        }
        
        const avisos = await response.json();
        const avisosList = document.getElementById('avisosList');
        avisosList.innerHTML = '';
        
        if (avisos.length === 0) {
            avisosList.innerHTML = `
                <div class="empty-state-no-border">
                    <i class="fas fa-bullhorn" style="font-size: 36px; color: #999; margin-bottom: 12px;"></i>
                    <p style="color: #666; font-size: 14px;">Nenhum aviso publicado nesta turma.</p>
                </div>
            `;
        } else {
            avisos.forEach(aviso => {
                adicionarAvisoNaLista(aviso);
            });
        }
        
    } catch (error) {
        console.error('Erro ao carregar avisos:', error);
        document.getElementById('avisosList').innerHTML = `
            <div class="empty-state-no-border">
                <i class="fas fa-bullhorn" style="font-size: 36px; color: #999; margin-bottom: 12px;"></i>
                <p style="color: #666; font-size: 14px;">Nenhum aviso publicado nesta turma.</p>
            </div>
        `;
    }
}

function adicionarAvisoNaLista(aviso) {
    const avisosList = document.getElementById('avisosList');
    
    const avisoCard = document.createElement('div');
    avisoCard.className = 'aviso-card';
    
    // Verifica se há arquivo válido
    const arquivoUrl = aviso.arquivoUrl || null;
    const temArquivoValido = arquivoUrl && (arquivoUrl.startsWith('data:') || arquivoUrl.startsWith('http') || arquivoUrl.startsWith('/'));
    
    // Constrói o HTML do arquivo
    let arquivoHtml = '';
    if (aviso.arquivoPath) {
        if (temArquivoValido) {
            arquivoHtml = `
                <a href="${arquivoUrl}" class="aviso-arquivo arquivo-link-aluno" target="_blank" download="${aviso.arquivoPath}">
                    <i class="fas fa-paperclip"></i>
                    <span>${aviso.arquivoPath}</span>
                </a>
            `;
        } else {
            arquivoHtml = `
                <div class="aviso-arquivo arquivo-sem-link">
                    <i class="fas fa-paperclip"></i>
                    <span>${aviso.arquivoPath}</span>
                </div>
            `;
        }
    }
    
    avisoCard.innerHTML = `
        <div class="aviso-header">
            <div class="aviso-autor">
                <i class="fas fa-user-circle"></i>
                <div class="aviso-autor-info">
                    <strong>${aviso.professorNome || 'Professor'}</strong>
                </div>
            </div>
        </div>
        <div class="aviso-body">
            <p>${aviso.mensagem || ''}</p>
            ${arquivoHtml}
        </div>
        <div class="aviso-footer">
            <span class="aviso-data">${aviso.dataPostagem || ''}</span>
        </div>
    `;
    
    avisosList.appendChild(avisoCard);
}

// Carrega atividades da turma
async function carregarAtividades() {
    try {
        const response = await fetch(`http://localhost:8080/api/aluno/turmas/${turmaId}/atividades`);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar atividades');
        }
        
        const atividades = await response.json();
        const atividadesList = document.getElementById('atividadesList');
        atividadesList.innerHTML = '';
        
        if (atividades.length === 0) {
            atividadesList.innerHTML = `
                <div class="empty-state-no-border">
                    <i class="fas fa-tasks" style="font-size: 36px; color: #999; margin-bottom: 12px;"></i>
                    <p style="color: #666; font-size: 14px;">Nenhuma atividade publicada nesta turma.</p>
                </div>
            `;
        } else {
            atividades.forEach(atv => {
                adicionarAtividadeNaLista(atv);
            });
        }
        
    } catch (error) {
        console.error('Erro ao carregar atividades:', error);
        document.getElementById('atividadesList').innerHTML = `
            <div class="empty-state-no-border">
                <i class="fas fa-tasks" style="font-size: 36px; color: #999; margin-bottom: 12px;"></i>
                <p style="color: #666; font-size: 14px;">Nenhuma atividade publicada nesta turma.</p>
            </div>
        `;
    }
}

function adicionarAtividadeNaLista(atv) {
    const container = document.getElementById('atividadesList');
    if (!container) return;

    const card = document.createElement('div');
    card.className = 'aviso-card atividade-card atividade-card-aluno';
    card.style.cursor = 'pointer';
    
    // Verifica se há arquivo válido
    const arquivoUrl = atv.arquivoUrl || null;
    const temArquivoValido = arquivoUrl && (arquivoUrl.startsWith('data:') || arquivoUrl.startsWith('http') || arquivoUrl.startsWith('/'));
    
    // Constrói o HTML do arquivo
    let arquivoHtml = '';
    if (atv.arquivoPath) {
        if (temArquivoValido) {
            arquivoHtml = `
                <a href="${arquivoUrl}" class="aviso-arquivo arquivo-link-aluno" target="_blank" download="${atv.arquivoPath}">
                    <i class="fas fa-paperclip"></i>
                    <span>${atv.arquivoPath}</span>
                </a>
            `;
        } else {
            arquivoHtml = `
                <div class="aviso-arquivo arquivo-sem-link">
                    <i class="fas fa-paperclip"></i>
                    <span>${atv.arquivoPath}</span>
                </div>
            `;
        }
    }

    card.innerHTML = `
        <div class="aviso-header">
            <div class="aviso-autor">
                <i class="fas fa-folder" style="color: var(--primary-color);"></i>
                <div class="aviso-autor-info">
                    <div class="aviso-titulo"><strong>${atv.titulo}</strong></div>
                    <small style="color: var(--text-secondary);">${atv.professorNome || 'Professor'}</small>
                </div>
            </div>
        </div>
        <div class="aviso-body">
            <p>${atv.descricao || ''}</p>
            ${arquivoHtml}
        </div>
        <div class="aviso-footer">
            <div class="aviso-destinatarios">
                ${atv.prazo ? `<span class="atividade-prazo"><i class="fas fa-clock"></i> Prazo: ${atv.prazo}</span>` : ''}
            </div>
            <div class="aviso-data">${atv.dataCriacao || ''}</div>
        </div>
    `;

    container.appendChild(card);
    
    // Click no card navega para a página de detalhes
    card.addEventListener('click', function(e) {
        // Não navega se clicar no link do arquivo
        if (e.target.closest('.arquivo-link-aluno') || e.target.closest('.aviso-arquivo')) {
            return;
        }
        navegarParaDetalhes(atv);
    });
}

// Navega para a página de detalhes da atividade
function navegarParaDetalhes(atv) {
    // Salva os dados da atividade no sessionStorage
    sessionStorage.setItem('atividadeDetalhesAluno', JSON.stringify(atv));
    window.location.href = 'atividade-detalhes-aluno.html';
}

// Modal de detalhes da atividade
function abrirModalDetalhes(atv) {
    document.getElementById('detalhesTitulo').textContent = atv.titulo;
    document.getElementById('detalhesTituloCentro').textContent = atv.titulo;
    document.getElementById('detalhesProfessorTurma').textContent = `${atv.professorNome || 'Professor'}`;
    document.getElementById('detalhesDataPostagem').textContent = atv.dataCriacao || '';
    document.getElementById('detalhesDescricao').textContent = atv.descricao || 'Sem descrição';
    document.getElementById('detalhesPrazo').textContent = atv.prazo || '--';
    
    // Arquivo
    const linkArquivo = document.getElementById('detalhesArquivoLink');
    const nomeArquivo = document.getElementById('detalhesArquivoNome');
    
    if (atv.arquivoPath && atv.arquivoUrl) {
        linkArquivo.href = atv.arquivoUrl;
        nomeArquivo.textContent = atv.arquivoPath;
        linkArquivo.style.display = 'inline-flex';
    } else if (atv.arquivoPath) {
        linkArquivo.href = '#';
        nomeArquivo.textContent = atv.arquivoPath;
        linkArquivo.style.display = 'inline-flex';
    } else {
        linkArquivo.style.display = 'none';
    }
    
    const modal = document.getElementById('modalDetalhesAtividade');
    modal.style.display = 'flex';
    modal.style.justifyContent = 'center';
    modal.style.alignItems = 'center';
}

function fecharModalDetalhes() {
    document.getElementById('modalDetalhesAtividade').style.display = 'none';
}

// Eventos do modal
document.getElementById('btnFecharModalDetalhes').addEventListener('click', fecharModalDetalhes);
document.getElementById('btnFecharModalDetalhesFooter').addEventListener('click', fecharModalDetalhes);
document.getElementById('modalDetalhesAtividade').addEventListener('click', function(e) {
    if (e.target === this) {
        fecharModalDetalhes();
    }
});

function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        limparDadosUsuario();
        window.location.href = '/index.html';
    }
}
