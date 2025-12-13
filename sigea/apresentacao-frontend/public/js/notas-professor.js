// notas-professor.js

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
    document.getElementById('titulo-disciplina').textContent = 'Atribuir Notas';
}

// Armazena dados dos alunos
let alunosData = [];

// Carrega notas ao iniciar
if (salaId) {
    carregarNotas();
} else {
    document.getElementById('empty-state').style.display = 'block';
}

/**
 * Carrega as notas dos alunos
 */
async function carregarNotas() {
    const loadingElement = document.getElementById('loading');
    const notasContent = document.getElementById('notas-content');
    const emptyState = document.getElementById('empty-state');
    const tbody = document.getElementById('alunos-tbody');
    
    loadingElement.style.display = 'block';
    notasContent.style.display = 'none';
    emptyState.style.display = 'none';
    
    try {
        const response = await fetch(
            `http://localhost:8080/api/notas/sala/${salaId}?professorId=${usuarioId}`
        );
        
        if (!response.ok) {
            throw new Error('Erro ao carregar notas');
        }
        
        const dados = await response.json();
        alunosData = dados.alunos || [];
        
        loadingElement.style.display = 'none';
        
        if (alunosData.length === 0) {
            emptyState.style.display = 'block';
            return;
        }
        
        // Preenche a tabela
        tbody.innerHTML = '';
        alunosData.forEach((aluno, index) => {
            const tr = document.createElement('tr');
            const av1 = aluno.av1 !== null && aluno.av1 !== undefined ? aluno.av1 : '';
            const av2 = aluno.av2 !== null && aluno.av2 !== undefined ? aluno.av2 : '';
            const segundaChamada = aluno.segundaChamada !== null && aluno.segundaChamada !== undefined ? aluno.segundaChamada : '';
            const finalNota = aluno.final !== null && aluno.final !== undefined ? aluno.final : '';
            
            tr.innerHTML = `
                <td>${aluno.nome}</td>
                <td>
                    <input type="number" 
                           class="input-nota" 
                           data-matricula="${aluno.matriculaId}"
                           data-tipo="av1"
                           min="0" 
                           max="10" 
                           step="0.1"
                           value="${av1}"
                           onchange="atualizarMedia(${index})"
                           oninput="atualizarMedia(${index})">
                </td>
                <td>
                    <input type="number" 
                           class="input-nota" 
                           data-matricula="${aluno.matriculaId}"
                           data-tipo="av2"
                           min="0" 
                           max="10" 
                           step="0.1"
                           value="${av2}"
                           onchange="atualizarMedia(${index})"
                           oninput="atualizarMedia(${index})">
                </td>
                <td>
                    <input type="number" 
                           class="input-nota" 
                           data-matricula="${aluno.matriculaId}"
                           data-tipo="segundaChamada"
                           min="0" 
                           max="10" 
                           step="0.1"
                           value="${segundaChamada}"
                           onchange="atualizarMedia(${index})"
                           oninput="atualizarMedia(${index})">
                </td>
                <td>
                    <input type="number" 
                           class="input-nota" 
                           data-matricula="${aluno.matriculaId}"
                           data-tipo="final"
                           min="0" 
                           max="10" 
                           step="0.1"
                           value="${finalNota}"
                           onchange="atualizarMedia(${index})"
                           oninput="atualizarMedia(${index})">
                </td>
                <td class="media-cell" id="media-${aluno.matriculaId}">0</td>
            `;
            tbody.appendChild(tr);
        });
        
        // Calcula médias iniciais
        alunosData.forEach((aluno, index) => {
            atualizarMedia(index);
        });
        
        notasContent.style.display = 'block';
        
    } catch (error) {
        console.error('Erro ao carregar notas:', error);
        loadingElement.style.display = 'none';
        emptyState.style.display = 'block';
        emptyState.innerHTML = `
            <i class="fas fa-exclamation-triangle"></i>
            <p>Erro ao carregar notas. Tente novamente.</p>
        `;
    }
}

/**
 * Atualiza a média do aluno quando uma nota é alterada
 */
function atualizarMedia(index) {
    const aluno = alunosData[index];
    if (!aluno) return;
    
    // Busca valores dos inputs
    const inputAv1 = document.querySelector(`input[data-matricula="${aluno.matriculaId}"][data-tipo="av1"]`);
    const inputAv2 = document.querySelector(`input[data-matricula="${aluno.matriculaId}"][data-tipo="av2"]`);
    const inputSegundaChamada = document.querySelector(`input[data-matricula="${aluno.matriculaId}"][data-tipo="segundaChamada"]`);
    const inputFinal = document.querySelector(`input[data-matricula="${aluno.matriculaId}"][data-tipo="final"]`);
    
    // Valida e atualiza dados do aluno
    let av1Val = inputAv1.value ? parseFloat(inputAv1.value) : null;
    let av2Val = inputAv2.value ? parseFloat(inputAv2.value) : null;
    let segundaChamadaVal = inputSegundaChamada.value ? parseFloat(inputSegundaChamada.value) : null;
    let finalVal = inputFinal.value ? parseFloat(inputFinal.value) : null;
    
    // Validação: notas devem estar entre 0 e 10
    if (av1Val !== null && (av1Val < 0 || av1Val > 10)) {
        inputAv1.setCustomValidity('Nota deve estar entre 0 e 10');
        inputAv1.reportValidity();
        return;
    } else {
        inputAv1.setCustomValidity('');
    }
    
    if (av2Val !== null && (av2Val < 0 || av2Val > 10)) {
        inputAv2.setCustomValidity('Nota deve estar entre 0 e 10');
        inputAv2.reportValidity();
        return;
    } else {
        inputAv2.setCustomValidity('');
    }
    
    if (segundaChamadaVal !== null && (segundaChamadaVal < 0 || segundaChamadaVal > 10)) {
        inputSegundaChamada.setCustomValidity('Nota deve estar entre 0 e 10');
        inputSegundaChamada.reportValidity();
        return;
    } else {
        inputSegundaChamada.setCustomValidity('');
    }
    
    if (finalVal !== null && (finalVal < 0 || finalVal > 10)) {
        inputFinal.setCustomValidity('Nota deve estar entre 0 e 10');
        inputFinal.reportValidity();
        return;
    } else {
        inputFinal.setCustomValidity('');
    }
    
    aluno.av1 = av1Val;
    aluno.av2 = av2Val;
    aluno.segundaChamada = segundaChamadaVal;
    aluno.final = finalVal;
    
    // Calcula média
    let media = calcularMedia(aluno);
    
    // Atualiza exibição da média
    const mediaElement = document.getElementById(`media-${aluno.matriculaId}`);
    if (mediaElement) {
        let textoMedia = media.toFixed(1);
        
        // Se tiver nota final, calcula média final e mostra status de aprovação
        if (aluno.final !== null && aluno.final !== undefined && media > 0) {
            const mediaFinal = (media + aluno.final) / 2;
            textoMedia = media.toFixed(1);
            
            // Aplica cor baseado na aprovação
            mediaElement.classList.remove('media-aprovado', 'media-reprovado');
            if (mediaFinal >= 6) {
                mediaElement.classList.add('media-aprovado');
                textoMedia += ' ✓'; // Símbolo de aprovado
            } else {
                mediaElement.classList.add('media-reprovado');
                textoMedia += ' ✗'; // Símbolo de reprovado
            }
        }
        
        mediaElement.textContent = textoMedia;
    }
}

/**
 * Calcula a média do aluno seguindo as regras:
 * - Média = (Av1 + Av2) / 2
 * - Se Av1 preenchida, Av2 não preenchida, e Segunda Chamada preenchida,
 *   então Segunda Chamada substitui Av2 para calcular média
 */
function calcularMedia(aluno) {
    let nota1 = null;
    let nota2 = null;
    
    // Determina qual nota usar
    if (aluno.av1 !== null && aluno.av1 !== undefined) {
        nota1 = aluno.av1;
    }
    
    if (aluno.av2 !== null && aluno.av2 !== undefined) {
        nota2 = aluno.av2;
    } else if (aluno.segundaChamada !== null && aluno.segundaChamada !== undefined && nota1 !== null) {
        // Se Av1 está preenchida, Av2 não está, e Segunda Chamada está preenchida,
        // então Segunda Chamada substitui Av2
        nota2 = aluno.segundaChamada;
    }
    
    // Calcula média
    if (nota1 !== null && nota2 !== null) {
        return (nota1 + nota2) / 2;
    } else if (nota1 !== null) {
        return nota1;
    } else if (nota2 !== null) {
        return nota2;
    }
    
    return 0;
}

/**
 * Salva as notas
 */
async function salvarNotas() {
    const btnSalvar = document.querySelector('.btn-salvar');
    
    btnSalvar.disabled = true;
    btnSalvar.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Salvando...';
    
    try {
        // Atualiza todas as médias antes de salvar
        alunosData.forEach((aluno, index) => {
            atualizarMedia(index);
        });
        
        // Prepara dados para envio
        const alunosRequest = alunosData.map(aluno => ({
            matriculaId: aluno.matriculaId,
            av1: aluno.av1,
            av2: aluno.av2,
            segundaChamada: aluno.segundaChamada,
            final: aluno.final
        }));
        
        const response = await fetch(
            `http://localhost:8080/api/notas/sala/${salaId}?professorId=${usuarioId}`,
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
            throw new Error(error.erro || 'Erro ao salvar notas');
        }
        
        alert('Notas salvas com sucesso!');
        
    } catch (error) {
        console.error('Erro ao salvar notas:', error);
        alert('Erro ao salvar notas: ' + error.message);
    } finally {
        btnSalvar.disabled = false;
        btnSalvar.innerHTML = '<i class="fas fa-save"></i> Salvar';
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

