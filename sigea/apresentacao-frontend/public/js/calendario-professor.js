// Copy of calendario-admin.js adjusted for Professor view
let mesAtual = new Date().getMonth();
let anoAtual = new Date().getFullYear();
let eventos = [];
let periodos = [];

const feriadosFixos = {
    '01-01': 'Confraternização Universal',
    '04-21': 'Tiradentes',
    '05-01': 'Dia do Trabalhador',
    '09-07': 'Independência do Brasil',
    '10-12': 'Nossa Senhora Aparecida',
    '11-02': 'Finados',
    '11-15': 'Proclamação da República',
    '11-20': 'Dia da Consciência Negra',
    '12-25': 'Natal'
};

function calcularPascoa(ano) {
    const a = ano % 19;
    const b = Math.floor(ano / 100);
    const c = ano % 100;
    const d = Math.floor(b / 4);
    const e = b % 4;
    const f = Math.floor((b + 8) / 25);
    const g = Math.floor((b - f + 1) / 3);
    const h = (19 * a + b - d - g + 15) % 30;
    const i = Math.floor(c / 4);
    const k = c % 4;
    const l = (32 + 2 * e + 2 * i - h - k) % 7;
    const m = Math.floor((a + 11 * h + 22 * l) / 451);
    const mes = Math.floor((h + l - 7 * m + 114) / 31);
    const dia = ((h + l - 7 * m + 114) % 31) + 1;
    
    return new Date(ano, mes - 1, dia);
}

function getFeriadosMoveis(ano) {
    const pascoa = calcularPascoa(ano);
    const feriados = {};
    
    const carnaval = new Date(pascoa);
    carnaval.setDate(carnaval.getDate() - 47);
    feriados[formatarData(carnaval)] = 'Carnaval';
    
    const sextaSanta = new Date(pascoa);
    sextaSanta.setDate(sextaSanta.getDate() - 2);
    feriados[formatarData(sextaSanta)] = 'Sexta-feira Santa';
    
    const corpusChristi = new Date(pascoa);
    corpusChristi.setDate(corpusChristi.getDate() + 60);
    feriados[formatarData(corpusChristi)] = 'Corpus Christi';
    
    return feriados;
}

function formatarData(data) {
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const dia = String(data.getDate()).padStart(2, '0');
    return `${mes}-${dia}`;
}

function isFeriado(data) {
    const dataFormatada = formatarData(data);
    if (feriadosFixos[dataFormatada]) return feriadosFixos[dataFormatada];
    const feriadosMoveis = getFeriadosMoveis(data.getFullYear());
    if (feriadosMoveis[dataFormatada]) return feriadosMoveis[dataFormatada];
    return null;
}

window.addEventListener('DOMContentLoaded', () => {
    const usuarioId = localStorage.getItem('usuarioId');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    if (!usuarioId || usuarioPerfil !== 'PROFESSOR') {
        window.location.href = '/login-professor.html';
        return;
    }

    loadUserInfo();
    carregarPeriodos();
    carregarEventos();
    renderizarCalendario();
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

function mesAnterior() {
    mesAtual--;
    if (mesAtual < 0) { mesAtual = 11; anoAtual--; adicionarFeriadosDoAno(anoAtual); }
    renderizarCalendario();
}

function proximoMes() {
    mesAtual++;
    if (mesAtual > 11) { mesAtual = 0; anoAtual++; adicionarFeriadosDoAno(anoAtual); }
    renderizarCalendario();
}

function adicionarFeriadosDoAno(ano) {
    eventos = eventos.filter(e => !e.isFeriado || !e.id.includes('-' + ano));
    Object.keys(feriadosFixos).forEach(dataKey => {
        const [mes, dia] = dataKey.split('-');
        const dataFeriado = `${ano}-${mes}-${dia}`;
        eventos.push({ id: 'feriado-' + dataKey + '-' + ano, titulo: feriadosFixos[dataKey], data: dataFeriado, tipo: 'TODOS', isAutomatico: true, isFeriado: true });
    });
    const feriadosMoveis = getFeriadosMoveis(ano);
    Object.keys(feriadosMoveis).forEach(dataKey => {
        const [mes, dia] = dataKey.split('-');
        const dataFeriado = `${ano}-${mes}-${dia}`;
        eventos.push({ id: 'feriado-movel-' + dataKey + '-' + ano, titulo: feriadosMoveis[dataKey], data: dataFeriado, tipo: 'TODOS', isAutomatico: true, isFeriado: true });
    });
}

function renderizarCalendario() {
    const meses = ['Janeiro','Fevereiro','Março','Abril','Maio','Junho','Julho','Agosto','Setembro','Outubro','Novembro','Dezembro'];
    document.getElementById('mesAnoAtual').textContent = `${meses[mesAtual]} ${anoAtual}`;
    const primeiroDia = new Date(anoAtual, mesAtual, 1);
    const diaSemana = primeiroDia.getDay();
    const diasOffset = diaSemana === 0 ? 6 : diaSemana - 1;
    const ultimoDia = new Date(anoAtual, mesAtual + 1, 0).getDate();
    const calendarGrid = document.getElementById('calendarGrid');
    calendarGrid.innerHTML = '';
    for (let i = 0; i < diasOffset; i++) { const cell = document.createElement('div'); cell.className = 'calendar-cell empty'; calendarGrid.appendChild(cell); }
    for (let dia = 1; dia <= ultimoDia; dia++) {
        const data = new Date(anoAtual, mesAtual, dia);
        const cell = document.createElement('div');
        cell.className = 'calendar-cell';
        const hoje = new Date(); if (data.toDateString() === hoje.toDateString()) cell.classList.add('today');
        const dayNumber = document.createElement('div'); dayNumber.className = 'day-number'; dayNumber.textContent = dia; cell.appendChild(dayNumber);
        const eventosContainer = document.createElement('div'); eventosContainer.className = 'eventos-container';
        const eventosNoDia = getEventosNaData(data);
        eventosNoDia.forEach(evento => {
            const eventoEl = document.createElement('div');
            eventoEl.className = `evento-tag evento-${(evento.tipo||'todos').toLowerCase()}`;
            const eventoTexto = document.createElement('span'); eventoTexto.textContent = evento.titulo; eventoEl.appendChild(eventoTexto);
            if (!evento.isAutomatico) {
                const btnExcluir = document.createElement('button'); btnExcluir.className = 'evento-delete'; btnExcluir.innerHTML = '×'; btnExcluir.title = 'Excluir evento'; btnExcluir.onclick = (e) => { e.stopPropagation(); excluirEvento(evento.id); };
                eventoEl.appendChild(btnExcluir);
            }
            let tipoLabel = evento.tipo; if (evento.tipo === 'TODOS') tipoLabel = 'Todos os Usuários'; else if (evento.tipo === 'ALUNOS') tipoLabel = 'Apenas Alunos'; else if (evento.tipo === 'PROFESSORES') tipoLabel = 'Apenas Professores';
            eventoEl.title = `${evento.titulo} - ${tipoLabel}`;
            eventosContainer.appendChild(eventoEl);
        });
        cell.appendChild(eventosContainer); calendarGrid.appendChild(cell);
    }
}

function getPeriodoNaData(data) { return periodos.find(periodo => { const inicio = new Date(periodo.dataInicio); const fim = new Date(periodo.dataFim); return data >= inicio && data <= fim; }); }
function parseData(dataString) {
    if (!dataString) return null;
    if (dataString instanceof Date) return dataString;
    if (/^\d{4}-\d{2}-\d{2}$/.test(dataString)) { const [ano, mes, dia] = dataString.split('-').map(Number); return new Date(ano, mes - 1, dia); }
    const data = new Date(dataString); return new Date(data.getFullYear(), data.getMonth(), data.getDate());
}
function getEventosNaData(data) { return eventos.filter(evento => { const dataEvento = parseData(evento.data || evento.dataEvento); if (!dataEvento) return false; return dataEvento.toDateString() === data.toDateString(); }); }

async function carregarPeriodos() {
    try { const response = await fetch('http://localhost:8080/api/periodos'); if (!response.ok) throw new Error('Erro ao carregar períodos'); periodos = await response.json(); renderizarCalendario(); } catch (error) { console.error('Erro ao carregar períodos:', error); }
}

// Carrega eventos e filtra para o professor atual
async function carregarEventos() {
    try {
        // Load institutional/global events
        const responseEventos = await fetch('http://localhost:8080/api/eventos');
        if (!responseEventos.ok) throw new Error('Erro ao carregar eventos');
        const all = await responseEventos.json();

        // Load professor-specific events separately
        const usuarioId = parseInt(localStorage.getItem('usuarioId'));
        let profEvents = [];
        try {
            const r2 = await fetch(`http://localhost:8080/api/professor/eventos?professorId=${usuarioId}`);
            if (r2.ok) profEvents = await r2.json();
        } catch (err) { /* ignore */ }

        // Filter institutional events for visibility: TODOS or PROFESSORES or created by this user
        const filtered = all.filter(e => {
            const tipo = (e.tipo || '').toUpperCase();
            if (tipo === 'TODOS' || tipo === 'PROFESSORES') return true;
            if (e.responsavelId && parseInt(e.responsavelId) === usuarioId) return true;
            return false;
        }).map(e => ({ ...e }));

        // Merge institutional + professor-specific events
        eventos = filtered.concat(profEvents.map(e => ({ id: e.id, titulo: e.titulo, dataEvento: e.dataEvento, tipo: 'PROFESSOR', responsavelId: e.professorId })));

        // Adiciona feriados do ano atual e próximo
        const anoInicial = new Date().getFullYear();
        adicionarFeriadosDoAno(anoInicial);
        adicionarFeriadosDoAno(anoInicial + 1);

        // Tenta adicionar período letivo automático (mesma lógica do admin)
        try {
            const responsePeriodos = await fetch('http://localhost:8080/api/admin/periodos/atual');
            if (responsePeriodos.ok) {
                const periodo = await responsePeriodos.json();
                if (periodo && periodo.dataInicio) {
                    eventos.push({ id: 'periodo-inicio-' + periodo.id, titulo: `Início do Período ${periodo.nome}`, data: periodo.dataInicio, tipo: 'TODOS', isAutomatico: true });
                    if (periodo.dataFim) eventos.push({ id: 'periodo-fim-' + periodo.id, titulo: `Fim do Período ${periodo.nome}`, data: periodo.dataFim, tipo: 'TODOS', isAutomatico: true });
                }
            }
        } catch (err) { /* ignore */ }

        renderizarCalendario();
    } catch (error) {
        console.error('Erro ao carregar eventos:', error);
    }
}

async function excluirEvento(eventoId) {
    if (!confirm('Deseja realmente excluir este evento?')) return;
    try {
        const response = await fetch(`http://localhost:8080/api/eventos/${eventoId}`, { method: 'DELETE' });
        if (!response.ok) throw new Error('Erro ao excluir evento');
        await carregarEventos();
        alert('Evento excluído com sucesso!');
    } catch (error) { console.error('Erro ao excluir evento:', error); alert('Erro ao excluir evento. Tente novamente.'); }
}

function abrirModalEvento() {
    const modal = document.getElementById('modalEvento');
    if (modal) {
        modal.classList.add('show');
        const hoje = new Date().toISOString().split('T')[0];
        document.getElementById('dataEvento').value = hoje;
        document.getElementById('checkTodos').checked = true;
        handleCheckTodos();
    }
}
function fecharModalEvento() { const modal = document.getElementById('modalEvento'); if (modal) { modal.classList.remove('show'); document.getElementById('formEvento').reset(); } }
function handleCheckTodos() { const checkTodos = document.getElementById('checkTodos'); const checkAlunos = document.getElementById('checkAlunos'); const checkProfessores = document.getElementById('checkProfessores'); if (checkTodos.checked) { checkAlunos.disabled = true; checkProfessores.disabled = true; checkAlunos.checked = false; checkProfessores.checked = false; } else { checkAlunos.disabled = false; checkProfessores.disabled = false; } }
function handleCheckIndividual() { const checkTodos = document.getElementById('checkTodos'); const checkAlunos = document.getElementById('checkAlunos'); const checkProfessores = document.getElementById('checkProfessores'); if (checkAlunos.checked || checkProfessores.checked) { checkTodos.checked = false; } }

// Submissão do formulário de evento (professor criando evento)
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('formEvento');
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const titulo = document.getElementById('tituloEvento').value;
            const dataEvento = document.getElementById('dataEvento').value + 'T00:00:00';
            // For professor-created events: no recipient selection. Create an Evento
            // (tipo = 'PROFESSORES') and then create an Aviso for each turma of this professor
            const usuarioId = localStorage.getItem('usuarioId');
            try {
                // Create professor-specific event (won't appear as turmas' events)
                const payload = { titulo, descricao: null, dataEvento: dataEvento, professorId: parseInt(usuarioId) };
                const response = await fetch('http://localhost:8080/api/professor/eventos', {
                    method: 'POST', headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                if (!response.ok) throw new Error('Erro ao criar evento do professor');

                fecharModalEvento(); await carregarEventos(); alert('Evento criado com sucesso!');
            } catch (error) { console.error('Erro ao criar evento:', error); alert('Erro ao criar evento. Tente novamente.'); }
        });
    }
});