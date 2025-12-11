// Estado do calendário
let mesAtual = new Date().getMonth();
let anoAtual = new Date().getFullYear();
let eventos = [];
let periodos = [];

// Feriados nacionais brasileiros (fixos e móveis)
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

// Calcula Páscoa (algoritmo de Meeus/Jones/Butcher)
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

// Retorna feriados móveis de um ano
function getFeriadosMoveis(ano) {
    const pascoa = calcularPascoa(ano);
    const feriados = {};
    
    // Carnaval (47 dias antes da Páscoa)
    const carnaval = new Date(pascoa);
    carnaval.setDate(carnaval.getDate() - 47);
    feriados[formatarData(carnaval)] = 'Carnaval';
    
    // Sexta-feira Santa (2 dias antes da Páscoa)
    const sextaSanta = new Date(pascoa);
    sextaSanta.setDate(sextaSanta.getDate() - 2);
    feriados[formatarData(sextaSanta)] = 'Sexta-feira Santa';
    
    // Corpus Christi (60 dias depois da Páscoa)
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

// Verifica se uma data é feriado
function isFeriado(data) {
    const dataFormatada = formatarData(data);
    
    // Verifica feriados fixos
    if (feriadosFixos[dataFormatada]) {
        return feriadosFixos[dataFormatada];
    }
    
    // Verifica feriados móveis
    const feriadosMoveis = getFeriadosMoveis(data.getFullYear());
    if (feriadosMoveis[dataFormatada]) {
        return feriadosMoveis[dataFormatada];
    }
    
    return null;
}

// Verifica autenticação ao carregar a página
window.addEventListener('DOMContentLoaded', () => {
    const usuarioId = localStorage.getItem('usuarioId');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    
    if (!usuarioId || usuarioPerfil !== 'ADMINISTRADOR') {
        window.location.href = '/login-admin.html';
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
        userNameElement.textContent = `Admin - ${nome || 'Usuário'}`;
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

// Navegação entre meses
function mesAnterior() {
    mesAtual--;
    if (mesAtual < 0) {
        mesAtual = 11;
        anoAtual--;
    }
    renderizarCalendario();
}

function proximoMes() {
    mesAtual++;
    if (mesAtual > 11) {
        mesAtual = 0;
        anoAtual++;
    }
    renderizarCalendario();
}

// Renderiza o calendário
function renderizarCalendario() {
    const meses = ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
                   'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'];
    
    // Atualiza título
    document.getElementById('mesAnoAtual').textContent = `${meses[mesAtual]} ${anoAtual}`;
    
    // Primeiro dia do mês
    const primeiroDia = new Date(anoAtual, mesAtual, 1);
    const diaSemana = primeiroDia.getDay(); // 0=domingo, 1=segunda...
    
    // Ajusta para começar na segunda-feira
    const diasOffset = diaSemana === 0 ? 6 : diaSemana - 1;
    
    // Último dia do mês
    const ultimoDia = new Date(anoAtual, mesAtual + 1, 0).getDate();
    
    const calendarGrid = document.getElementById('calendarGrid');
    calendarGrid.innerHTML = '';
    
    // Células vazias antes do primeiro dia
    for (let i = 0; i < diasOffset; i++) {
        const cell = document.createElement('div');
        cell.className = 'calendar-cell empty';
        calendarGrid.appendChild(cell);
    }
    
    // Dias do mês
    for (let dia = 1; dia <= ultimoDia; dia++) {
        const data = new Date(anoAtual, mesAtual, dia);
        const cell = document.createElement('div');
        cell.className = 'calendar-cell';
        
        // Verifica se é hoje
        const hoje = new Date();
        if (data.toDateString() === hoje.toDateString()) {
            cell.classList.add('today');
        }
        
        // Número do dia
        const dayNumber = document.createElement('div');
        dayNumber.className = 'day-number';
        dayNumber.textContent = dia;
        cell.appendChild(dayNumber);
        
        // Container de eventos
        const eventosContainer = document.createElement('div');
        eventosContainer.className = 'eventos-container';
        
        // Verifica feriados
        const feriado = isFeriado(data);
        if (feriado) {
            const eventoEl = document.createElement('div');
            eventoEl.className = 'evento-tag feriado';
            eventoEl.textContent = feriado;
            eventoEl.title = feriado;
            eventosContainer.appendChild(eventoEl);
        }
        
        // Verifica período letivo
        const periodo = getPeriodoNaData(data);
        if (periodo) {
            const dataInicio = new Date(periodo.dataInicio);
            const dataFim = new Date(periodo.dataFim);
            
            if (data.toDateString() === dataInicio.toDateString()) {
                const eventoEl = document.createElement('div');
                eventoEl.className = 'evento-tag periodo-inicio';
                eventoEl.textContent = `Início: ${periodo.nome}`;
                eventoEl.title = `Início do período: ${periodo.nome}`;
                eventosContainer.appendChild(eventoEl);
            } else if (data.toDateString() === dataFim.toDateString()) {
                const eventoEl = document.createElement('div');
                eventoEl.className = 'evento-tag periodo-fim';
                eventoEl.textContent = `Fim: ${periodo.nome}`;
                eventoEl.title = `Fim do período: ${periodo.nome}`;
                eventosContainer.appendChild(eventoEl);
            } else {
                cell.classList.add('periodo-letivo');
            }
        }
        
        // Verifica eventos
        const eventosNoDia = getEventosNaData(data);
        eventosNoDia.forEach(evento => {
            const eventoEl = document.createElement('div');
            eventoEl.className = `evento-tag evento-${evento.tipo.toLowerCase()}`;
            
            // Texto do evento
            const eventoTexto = document.createElement('span');
            eventoTexto.textContent = evento.titulo;
            eventoEl.appendChild(eventoTexto);
            
            // Botão de excluir
            const btnExcluir = document.createElement('button');
            btnExcluir.className = 'evento-delete';
            btnExcluir.innerHTML = '×';
            btnExcluir.title = 'Excluir evento';
            btnExcluir.onclick = (e) => {
                e.stopPropagation();
                excluirEvento(evento.id);
            };
            eventoEl.appendChild(btnExcluir);
            
            // Define o rótulo do tipo
            let tipoLabel = evento.tipo;
            if (evento.tipo === 'TODOS') tipoLabel = 'Todos os Usuários';
            else if (evento.tipo === 'ALUNOS') tipoLabel = 'Apenas Alunos';
            else if (evento.tipo === 'PROFESSORES') tipoLabel = 'Apenas Professores';
            
            eventoEl.title = `${evento.titulo} - ${tipoLabel}`;
            eventosContainer.appendChild(eventoEl);
        });
        
        cell.appendChild(eventosContainer);
        calendarGrid.appendChild(cell);
    }
}

// Busca período ativo em uma data
function getPeriodoNaData(data) {
    return periodos.find(periodo => {
        const inicio = new Date(periodo.dataInicio);
        const fim = new Date(periodo.dataFim);
        return data >= inicio && data <= fim;
    });
}

// Busca eventos em uma data
function getEventosNaData(data) {
    return eventos.filter(evento => {
        const dataEvento = new Date(evento.dataEvento);
        return dataEvento.toDateString() === data.toDateString();
    });
}

// Carrega períodos letivos
async function carregarPeriodos() {
    try {
        const response = await fetch('http://localhost:8080/api/periodos');
        if (!response.ok) throw new Error('Erro ao carregar períodos');
        periodos = await response.json();
        renderizarCalendario();
    } catch (error) {
        console.error('Erro ao carregar períodos:', error);
    }
}

// Carrega eventos
async function carregarEventos() {
    try {
        const response = await fetch('http://localhost:8080/api/eventos');
        if (!response.ok) throw new Error('Erro ao carregar eventos');
        eventos = await response.json();
        renderizarCalendario();
    } catch (error) {
        console.error('Erro ao carregar eventos:', error);
    }
}

// Excluir evento
async function excluirEvento(eventoId) {
    if (!confirm('Deseja realmente excluir este evento?')) {
        return;
    }
    
    try {
        const response = await fetch(`http://localhost:8080/api/eventos/${eventoId}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) throw new Error('Erro ao excluir evento');
        
        await carregarEventos();
        alert('Evento excluído com sucesso!');
    } catch (error) {
        console.error('Erro ao excluir evento:', error);
        alert('Erro ao excluir evento. Tente novamente.');
    }
}

// Modal de Evento
function abrirModalEvento() {
    const modal = document.getElementById('modalEvento');
    if (modal) {
        modal.classList.add('show');
        // Define a data de hoje como padrão
        const hoje = new Date().toISOString().split('T')[0];
        document.getElementById('dataEvento').value = hoje;
        // Marca "Todos" por padrão
        document.getElementById('checkTodos').checked = true;
        handleCheckTodos();
    }
}

function fecharModalEvento() {
    const modal = document.getElementById('modalEvento');
    if (modal) {
        modal.classList.remove('show');
        document.getElementById('formEvento').reset();
    }
}

function handleCheckTodos() {
    const checkTodos = document.getElementById('checkTodos');
    const checkAlunos = document.getElementById('checkAlunos');
    const checkProfessores = document.getElementById('checkProfessores');
    
    if (checkTodos.checked) {
        checkAlunos.disabled = true;
        checkProfessores.disabled = true;
        checkAlunos.checked = false;
        checkProfessores.checked = false;
    } else {
        checkAlunos.disabled = false;
        checkProfessores.disabled = false;
    }
}

function handleCheckIndividual() {
    const checkTodos = document.getElementById('checkTodos');
    const checkAlunos = document.getElementById('checkAlunos');
    const checkProfessores = document.getElementById('checkProfessores');
    
    if (checkAlunos.checked || checkProfessores.checked) {
        checkTodos.checked = false;
    }
}

// Submissão do formulário de evento
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('formEvento');
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const titulo = document.getElementById('tituloEvento').value;
            const dataEvento = document.getElementById('dataEvento').value + 'T00:00:00';
            const checkTodos = document.getElementById('checkTodos').checked;
            const checkAlunos = document.getElementById('checkAlunos').checked;
            const checkProfessores = document.getElementById('checkProfessores').checked;
            
            let tipo;
            if (checkTodos) {
                tipo = 'TODOS';
            } else if (checkAlunos && !checkProfessores) {
                tipo = 'ALUNOS';
            } else if (checkProfessores && !checkAlunos) {
                tipo = 'PROFESSORES';
            } else {
                alert('Por favor, selecione para quem é o evento!');
                return;
            }
            
            const usuarioId = localStorage.getItem('usuarioId');
            
            try {
                const response = await fetch('http://localhost:8080/api/eventos', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        titulo,
                        dataEvento,
                        tipo,
                        responsavelId: parseInt(usuarioId)
                    })
                });
                
                if (!response.ok) throw new Error('Erro ao criar evento');
                
                fecharModalEvento();
                await carregarEventos();
                alert('Evento criado com sucesso!');
            } catch (error) {
                console.error('Erro ao criar evento:', error);
                alert('Erro ao criar evento. Tente novamente.');
            }
        });
    }
});
