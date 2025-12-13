// chamada-professor.js

const usuarioId = localStorage.getItem('usuarioId');
document.getElementById('userName').textContent = `Professor - ${localStorage.getItem('usuarioNome') || 'Usuário'}`;

// Obtém parâmetros da URL
const urlParams = new URLSearchParams(window.location.search);
const salaId = urlParams.get('salaId');
const disciplinaNome = urlParams.get('disciplina');
const salaIdentificador = urlParams.get('sala');

// Define o título da página
if (disciplinaNome && salaIdentificador) {
    document.getElementById('titulo-disciplina').textContent = `${disciplinaNome} - ${salaIdentificador}`;
} else {
    document.getElementById('titulo-disciplina').textContent = 'Chamada';
}

// Define data inicial como hoje
const hoje = new Date();
const dataFormatada = hoje.toISOString().split('T')[0];
document.getElementById('data-chamada').value = dataFormatada;
document.getElementById('subtitulo-chamada').textContent = `Chamada - ${formatarData(dataFormatada)}`;

// Armazena dados dos alunos e faltas totais
let alunosData = [];
let faltasTotais = {}; // matriculaId -> total de faltas

// Carrega chamada ao iniciar
if (salaId) {
    carregarChamada();
} else {
    document.getElementById('empty-state').style.display = 'block';
}

/**
 * Formata data no formato brasileiro
 */
function formatarData(dataISO) {
    const data = new Date(dataISO + 'T00:00:00');
    const dia = String(data.getDate()).padStart(2, '0');
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const ano = data.getFullYear();
    return `${dia}/${mes}/${ano}`;
}

/**
 * Carrega a chamada para a data selecionada
 */
async function carregarChamada() {
    const dataInput = document.getElementById('data-chamada');
    const data = dataInput.value;
    
    if (!data) {
        return;
    }
    
    const loadingElement = document.getElementById('loading');
    const chamadaContent = document.getElementById('chamada-content');
    const emptyState = document.getElementById('empty-state');
    const tbody = document.getElementById('alunos-tbody');
    
    // Atualiza subtítulo
    document.getElementById('subtitulo-chamada').textContent = `Chamada - ${formatarData(data)}`;
    
    loadingElement.style.display = 'block';
    chamadaContent.style.display = 'none';
    emptyState.style.display = 'none';
    
    try {
        const response = await fetch(
            `http://localhost:8080/api/chamadas/sala/${salaId}/data/${data}?professorId=${usuarioId}`
        );
        
        if (!response.ok) {
            throw new Error('Erro ao carregar chamada');
        }
        
        const dados = await response.json();
        alunosData = dados.alunos || [];
        
        // Carrega faltas totais de cada aluno
        await carregarFaltasTotais();
        
        loadingElement.style.display = 'none';
        
        if (alunosData.length === 0) {
            emptyState.style.display = 'block';
            return;
        }
        
        // Preenche a tabela
        tbody.innerHTML = '';
        alunosData.forEach(aluno => {
            const tr = document.createElement('tr');
            const faltaAula1 = aluno.faltaAula1 || false;
            const faltaAula2 = aluno.faltaAula2 || false;
            const totalFaltas = faltasTotais[aluno.matriculaId] || 0;
            
            tr.innerHTML = `
                <td>${aluno.nome}</td>
                <td>
                    <input type="checkbox" 
                           class="checkbox-falta" 
                           data-matricula="${aluno.matriculaId}"
                           data-aula="1"
                           ${faltaAula1 ? 'checked' : ''}
                           onchange="atualizarFaltas(${aluno.matriculaId})">
                </td>
                <td>
                    <input type="checkbox" 
                           class="checkbox-falta" 
                           data-matricula="${aluno.matriculaId}"
                           data-aula="2"
                           ${faltaAula2 ? 'checked' : ''}
                           onchange="atualizarFaltas(${aluno.matriculaId})">
                </td>
                <td class="faltas-total" id="faltas-${aluno.matriculaId}">${totalFaltas}</td>
            `;
            tbody.appendChild(tr);
        });
        
        chamadaContent.style.display = 'block';
        
    } catch (error) {
        console.error('Erro ao carregar chamada:', error);
        loadingElement.style.display = 'none';
        emptyState.style.display = 'block';
        emptyState.innerHTML = `
            <i class="fas fa-exclamation-triangle"></i>
            <p>Erro ao carregar chamada. Tente novamente.</p>
        `;
    }
}

/**
 * Carrega as faltas totais de cada aluno da sala
 */
async function carregarFaltasTotais() {
    try {
        // Busca alunos da sala para obter total de faltas
        const response = await fetch(
            `http://localhost:8080/api/salas/${salaId}/alunos?professorId=${usuarioId}`
        );
        
        if (response.ok) {
            const alunos = await response.json();
            alunos.forEach(aluno => {
                // Encontra a matrícula correspondente
                const alunoChamada = alunosData.find(a => a.alunoId === aluno.alunoId);
                if (alunoChamada) {
                    faltasTotais[alunoChamada.matriculaId] = aluno.totalFaltas || 0;
                }
            });
        }
    } catch (error) {
        console.error('Erro ao carregar faltas totais:', error);
    }
}

/**
 * Atualiza os dados do aluno quando checkbox é alterado
 */
function atualizarFaltas(matriculaId) {
    const aluno = alunosData.find(a => a.matriculaId === matriculaId);
    if (!aluno) return;
    
    const checkboxAula1 = document.querySelector(
        `input[data-matricula="${matriculaId}"][data-aula="1"]`
    );
    const checkboxAula2 = document.querySelector(
        `input[data-matricula="${matriculaId}"][data-aula="2"]`
    );
    
    // Atualiza dados do aluno (será salvo quando clicar em Salvar)
    aluno.faltaAula1 = checkboxAula1 ? checkboxAula1.checked : false;
    aluno.faltaAula2 = checkboxAula2 ? checkboxAula2.checked : false;
}

/**
 * Salva a chamada
 */
async function salvarChamada() {
    const data = document.getElementById('data-chamada').value;
    const btnSalvar = document.querySelector('.btn-salvar');
    
    btnSalvar.disabled = true;
    btnSalvar.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Salvando...';
    
    try {
        // Prepara dados para envio
        const alunosRequest = alunosData.map(aluno => ({
            matriculaId: aluno.matriculaId,
            faltaAula1: aluno.faltaAula1 || false,
            faltaAula2: aluno.faltaAula2 || false
        }));
        
        const response = await fetch(
            `http://localhost:8080/api/chamadas/sala/${salaId}/data/${data}?professorId=${usuarioId}`,
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    alunos: alunosRequest
                })
            }
        );
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.erro || 'Erro ao salvar chamada');
        }
        
        // Recarrega faltas totais após salvar
        await carregarFaltasTotais();
        
        // Recarrega a chamada para atualizar dados e exibição
        await carregarChamada();
        
        alert('Chamada salva com sucesso!');
        
    } catch (error) {
        console.error('Erro ao salvar chamada:', error);
        alert('Erro ao salvar chamada: ' + error.message);
    } finally {
        btnSalvar.disabled = false;
        btnSalvar.innerHTML = '<i class="fas fa-save"></i> Salvar Chamada';
    }
}

/**
 * Volta para a página de detalhes da disciplina
 */
function voltarParaDisciplina() {
    const params = new URLSearchParams({
        salaId: salaId,
        disciplina: disciplinaNome || '',
        sala: salaIdentificador || ''
    });
    window.location.href = `disciplina-detalhes-professor.html?${params.toString()}`;
}

/**
 * Logout
 */
function handleLogout() {
    localStorage.clear();
    window.location.href = 'login-professor.html';
}

/**
 * Toggle sidebar
 */
function toggleSidebar() {
    document.querySelector('.sidebar').classList.toggle('collapsed');
}

