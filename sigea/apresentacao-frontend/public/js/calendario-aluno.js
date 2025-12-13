// Calendário do Aluno - Visualização apenas (sem criar/excluir eventos)
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
    if (!usuarioId || usuarioPerfil !== 'ALUNO') {
        window.location.href = '/index.html';
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
            // Mapeia o tipo para classe CSS
            let tipoClasse = 'todos';
            if (evento.tipo === 'PROFESSOR') {
                tipoClasse = 'professor';
            } else if (evento.tipo === 'ATIVIDADE_TURMA') {
                tipoClasse = 'atividade-turma';
            } else if (evento.isFeriado) {
                tipoClasse = 'feriado';
            } else if (evento.titulo && evento.titulo.includes('Período')) {
                tipoClasse = evento.titulo.includes('Início') ? 'periodo-inicio' : 'periodo-fim';
            }
            eventoEl.className = `evento-tag evento-${tipoClasse}`;
            const eventoTexto = document.createElement('span'); eventoTexto.textContent = evento.titulo; eventoEl.appendChild(eventoTexto);
            
            // Aluno não pode excluir eventos (sem botão de excluir)
            
            let tipoLabel = evento.tipo; 
            if (evento.tipo === 'TODOS') tipoLabel = 'Todos os Usuários'; 
            else if (evento.tipo === 'ALUNOS') tipoLabel = 'Apenas Alunos'; 
            else if (evento.tipo === 'PROFESSORES') tipoLabel = 'Apenas Professores';
            else if (evento.tipo === 'PROFESSOR') tipoLabel = 'Evento do Professor';
            else if (evento.tipo === 'ATIVIDADE_TURMA') tipoLabel = 'Atividade da Turma';
            
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

// Carrega eventos do aluno (institucionais + eventos de professores das disciplinas matriculadas)
async function carregarEventos() {
    try {
        const usuarioId = parseInt(localStorage.getItem('usuarioId'));
        
        // Busca eventos do aluno através do endpoint específico
        const responseEventos = await fetch(`http://localhost:8080/api/aluno/${usuarioId}/eventos`);
        if (!responseEventos.ok) throw new Error('Erro ao carregar eventos');
        const eventosAluno = await responseEventos.json();

        // Mapeia eventos para o formato esperado
        eventos = eventosAluno.map(e => ({
            id: e.id,
            titulo: e.titulo,
            data: e.data,
            dataEvento: e.dataEvento,
            tipo: e.tipo || 'TODOS',
            responsavelId: e.responsavelId || e.professorId,
            isAutomatico: false
        }));

        // Adiciona feriados do ano atual e próximo
        const anoInicial = new Date().getFullYear();
        adicionarFeriadosDoAno(anoInicial);
        adicionarFeriadosDoAno(anoInicial + 1);

        // Tenta adicionar período letivo automático
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
