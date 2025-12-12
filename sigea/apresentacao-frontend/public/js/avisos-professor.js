// View-only avisos loader for professors (based on admin avisos view)
window.addEventListener('DOMContentLoaded', () => {
    const usuarioId = localStorage.getItem('usuarioId');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    if (!usuarioId || usuarioPerfil !== 'PROFESSOR') {
        window.location.href = '/login-professor.html';
        return;
    }

    const modal = document.getElementById('modal-novo-aviso');
    if (modal) modal.style.display = 'none';

    loadUserInfo();
    carregarAvisos();
    initializeMenuToggle();
});

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    const userNameElement = document.getElementById('user-name');
    if (userNameElement) userNameElement.textContent = `Professor - ${nome || 'Usuário'}`;
}

async function carregarAvisos() {
    const container = document.getElementById('avisos-container');
    let usuarioId = localStorage.getItem('usuarioId');
    if (!usuarioId || usuarioId === 'undefined' || usuarioId === 'null') {
        usuarioId = '1';
        localStorage.setItem('usuarioId', '1');
    }

    try {
        const endpoint = `http://localhost:8080/api/avisos/nao-lidos?usuarioId=${usuarioId}`;
        const response = await fetch(endpoint);
        if (!response.ok) throw new Error('Erro ao carregar avisos');
        const avisos = await response.json();

        if (!avisos || avisos.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-bell-slash"></i>
                    <p>Nenhum aviso não lido.</p>
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

function exibirAvisos(avisos) {
    const container = document.getElementById('avisos-container');
    const html = avisos.map(aviso => {
        const badgeLido = aviso.lido ? '<span class="badge-lido">Lido</span>' : '';
        return `
        <div class="aviso-card ${aviso.lido ? 'aviso-lido' : ''}">
            <button class="btn-close-aviso" title="Marcar como lido" onclick="marcarComoLido(${aviso.id})">
                <i class="fas fa-times"></i>
            </button>
            <div class="aviso-header">
                <h3>${aviso.titulo || ''} ${badgeLido}</h3>
                <div class="aviso-data">${aviso.dataExpiracao || ''}</div>
            </div>
            <div class="aviso-body">
                <p>${aviso.conteudo || ''}</p>
            </div>
            <div class="aviso-footer">
                <span class="aviso-destinatarios"><i class="fas fa-users"></i> ${aviso.escopo || 'Geral'}</span>
            </div>
        </div>
    `;
    }).join('');
    container.innerHTML = html;
}

async function marcarComoLido(avisoId) {
    let usuarioId = localStorage.getItem('usuarioId');
    if (!usuarioId || usuarioId === 'undefined' || usuarioId === 'null') usuarioId = '1';

    try {
        const response = await fetch('http://localhost:8080/api/avisos/marcar-lido', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ avisoId, usuarioId })
        });
        if (response.ok) carregarAvisos();
    } catch (e) {
        console.error('Erro ao marcar como lido', e);
    }
}

function initializeMenuToggle() {
    const menuToggle = document.getElementById('menuToggle');
    const sidebar = document.getElementById('sidebar');
    if (menuToggle && sidebar) menuToggle.addEventListener('click', () => sidebar.classList.toggle('collapsed'));
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
