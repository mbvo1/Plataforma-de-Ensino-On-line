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
    // Atualizar datas próximas
    atualizarDatasProximas(dados.eventosProximos || []);
    
    // Atualizar avisos
    const avisosCard = document.getElementById('avisos-card');
    if (avisosCard) {
        if (dados.avisosNaoLidos > 0) {
            avisosCard.innerHTML = `<span style="color: #e74c3c; font-weight: bold;">Você tem ${dados.avisosNaoLidos} aviso${dados.avisosNaoLidos > 1 ? 's' : ''} não lido${dados.avisosNaoLidos > 1 ? 's' : ''}</span>`;
        } else {
            avisosCard.innerHTML = `<span style="color: #27ae60;">Nenhum aviso novo</span>`;
        }
    }
    
    // Atualizar desempenho
    atualizarDesempenho(dados.notasResumo || []);
    
    // Atualizar frequência
    const frequenciaContent = document.getElementById('frequencia-content');
    if (frequenciaContent) {
        const frequenciaFormatada = dados.frequenciaPercentual.toFixed(1);
        frequenciaContent.innerHTML = `
            <p class="freq-title">Total</p>
            <p><strong>Faltas:</strong> ${dados.totalFaltas}</p>
            <p><strong>Frequência:</strong> ${frequenciaFormatada}%</p>
        `;
    }
}

function atualizarDatasProximas(eventos) {
    const datasCard = document.querySelector('.info-card:first-child');
    if (!datasCard) return;
    
    const cardSubtitle = datasCard.querySelector('.card-subtitle');
    const cardEmpty = datasCard.querySelector('.card-empty');
    
    if (!eventos || eventos.length === 0) {
        if (cardSubtitle) cardSubtitle.textContent = 'Esta semana';
        if (cardEmpty) {
            cardEmpty.textContent = 'Sem atividades esta semana';
            cardEmpty.style.display = 'block';
        }
        return;
    }
    
    if (cardSubtitle) cardSubtitle.textContent = 'Próximos 7 dias';
    if (cardEmpty) cardEmpty.style.display = 'none';
    
    // Criar lista de eventos
    const eventosList = document.createElement('div');
    eventosList.style.marginTop = '0.5rem';
    eventosList.style.fontSize = '0.875rem';
    
    eventos.slice(0, 5).forEach(evento => { // Mostra no máximo 5 eventos
        const eventoItem = document.createElement('div');
        eventoItem.style.marginBottom = '0.5rem';
        eventoItem.style.padding = '0.5rem';
        eventoItem.style.background = '#f5f5f5';
        eventoItem.style.borderRadius = '4px';
        
        let tipoLabel = '';
        if (evento.tipo === 'ATIVIDADE_TURMA') {
            tipoLabel = '📋 Atividade';
        } else if (evento.tipo === 'EVENTO_PROFESSOR') {
            tipoLabel = '📅 Evento';
        } else {
            tipoLabel = '📅 Evento';
        }
        
        eventoItem.innerHTML = `
            <div style="font-weight: 600; color: #333;">${tipoLabel}: ${evento.titulo}</div>
            <div style="color: #666; font-size: 0.8rem; margin-top: 0.25rem;">
                ${evento.disciplinaNome ? evento.disciplinaNome + ' - ' : ''}${evento.data}
            </div>
        `;
        
        eventosList.appendChild(eventoItem);
    });
    
    if (eventos.length > 5) {
        const maisEventos = document.createElement('div');
        maisEventos.style.marginTop = '0.5rem';
        maisEventos.style.color = '#666';
        maisEventos.style.fontSize = '0.8rem';
        maisEventos.textContent = `+${eventos.length - 5} mais evento${eventos.length - 5 > 1 ? 's' : ''}`;
        eventosList.appendChild(maisEventos);
    }
    
    // Remove elementos antigos e adiciona a nova lista
    const existingList = datasCard.querySelector('.eventos-list');
    if (existingList) existingList.remove();
    eventosList.className = 'eventos-list';
    datasCard.appendChild(eventosList);
}

function atualizarDesempenho(notasResumo) {
    const desempenhoCard = document.querySelectorAll('.info-card')[2]; // Terceiro card
    if (!desempenhoCard) return;
    
    const cardEmpty = desempenhoCard.querySelector('.card-empty');
    
    if (!notasResumo || notasResumo.length === 0) {
        if (cardEmpty) {
            cardEmpty.textContent = 'Sem notas atribuídas';
            cardEmpty.style.display = 'block';
        }
        return;
    }
    
    if (cardEmpty) cardEmpty.style.display = 'none';
    
    // Criar lista de notas
    const notasList = document.createElement('div');
    notasList.style.marginTop = '0.5rem';
    notasList.style.fontSize = '0.875rem';
    
    notasResumo.slice(0, 3).forEach(nota => { // Mostra no máximo 3 disciplinas
        const notaItem = document.createElement('div');
        notaItem.style.marginBottom = '0.5rem';
        notaItem.style.padding = '0.5rem';
        notaItem.style.background = '#f5f5f5';
        notaItem.style.borderRadius = '4px';
        
        let notasTexto = '';
        if (nota.av1 != null && nota.av2 != null) {
            notasTexto = `AV1: ${nota.av1.toFixed(1)} | AV2: ${nota.av2.toFixed(1)} | Média: ${nota.mediaParcial.toFixed(1)}`;
        } else if (nota.av1 != null) {
            notasTexto = `AV1: ${nota.av1.toFixed(1)}`;
        } else if (nota.av2 != null) {
            notasTexto = `AV2: ${nota.av2.toFixed(1)}`;
        }
        
        notaItem.innerHTML = `
            <div style="font-weight: 600; color: #333;">${nota.disciplinaNome}</div>
            <div style="color: #666; font-size: 0.8rem; margin-top: 0.25rem;">${notasTexto}</div>
        `;
        
        notasList.appendChild(notaItem);
    });
    
    if (notasResumo.length > 3) {
        const maisNotas = document.createElement('div');
        maisNotas.style.marginTop = '0.5rem';
        maisNotas.style.color = '#666';
        maisNotas.style.fontSize = '0.8rem';
        maisNotas.textContent = `+${notasResumo.length - 3} mais disciplina${notasResumo.length - 3 > 1 ? 's' : ''}`;
        notasList.appendChild(maisNotas);
    }
    
    // Remove elementos antigos e adiciona a nova lista
    const existingList = desempenhoCard.querySelector('.notas-list');
    if (existingList) existingList.remove();
    notasList.className = 'notas-list';
    desempenhoCard.appendChild(notasList);
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
