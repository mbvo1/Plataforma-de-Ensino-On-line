// disciplina-detalhes-professor.js

function escapeHtml(texto) {
    if (texto === null || texto === undefined) return '';
    const div = document.createElement('div');
    div.textContent = String(texto);
    return div.innerHTML;
}

const usuarioId = localStorage.getItem('usuarioId');
document.getElementById('userName').textContent = `Professor - ${localStorage.getItem('usuarioNome') || 'UsuÃ¡rio'}`;

// ObtÃ©m parÃ¢metros da URL
const urlParams = new URLSearchParams(window.location.search);
const salaId = urlParams.get('salaId');
const disciplinaNome = urlParams.get('disciplina');
const salaIdentificador = urlParams.get('sala');

// Define o tÃ­tulo da pÃ¡gina
if (disciplinaNome && salaIdentificador) {
    document.getElementById('titulo-disciplina').textContent = `${disciplinaNome} - ${salaIdentificador}`;
} else {
    // Tenta recuperar do sessionStorage
    try {
        const disciplinaSelecionada = JSON.parse(sessionStorage.getItem('disciplinaSelecionada'));
        if (disciplinaSelecionada) {
            document.getElementById('titulo-disciplina').textContent = 
                `${disciplinaSelecionada.nome} - ${disciplinaSelecionada.identificador}`;
        }
    } catch (e) {
        document.getElementById('titulo-disciplina').textContent = 'Disciplina';
    }
}

async function carregarAlunos() {
    const loadingElement = document.getElementById('loading');
    const tabelaContainer = document.getElementById('tabela-container');
    const emptyState = document.getElementById('empty-state');
    const tbody = document.getElementById('alunos-tbody');

    try {
        // Busca notas completas dos alunos da sala
        const response = await fetch(`/api/notas/sala/${salaId}?professorId=${usuarioId}`, { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        
        if (!response.ok) {
            throw new Error('Erro ao carregar notas');
        }

        const dados = await response.json();
        const alunos = dados.alunos || [];
        console.log('Alunos carregados:', alunos);

        loadingElement.style.display = 'none';

        if (!alunos || alunos.length === 0) {
            emptyState.style.display = 'block';
            return;
        }

        // Ordena alunos por nome (ordem alfabÃ©tica)
        alunos.sort((a, b) => a.nome.localeCompare(b.nome));

        // Busca faltas de todos os alunos de uma vez
        const faltasResponse = await fetch(`/api/salas/${salaId}/alunos?professorId=${usuarioId}`, { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        const alunosComFaltas = faltasResponse.ok ? await faltasResponse.json() : [];
        const faltasMap = new Map();
        alunosComFaltas.forEach(a => {
            faltasMap.set(a.alunoId, a.totalFaltas || 0);
        });

        // Preenche a tabela
        tbody.innerHTML = '';
        alunos.forEach(aluno => {
            // Calcula mÃ©dia parcial
            const mediaParcial = calcularMediaParcial(aluno.av1, aluno.av2, aluno.segundaChamada);
            
            // Calcula mÃ©dia final
            let mediaFinal = null;
            if (mediaParcial !== null && mediaParcial !== undefined && aluno.final !== null && aluno.final !== undefined) {
                mediaFinal = (mediaParcial + aluno.final) / 2.0;
            }
            
            // Formata valores
            const av1 = aluno.av1 !== null && aluno.av1 !== undefined ? aluno.av1.toFixed(2) : 'â€”';
            const av2 = aluno.av2 !== null && aluno.av2 !== undefined ? aluno.av2.toFixed(2) : 'â€”';
            const segundaChamada = aluno.segundaChamada !== null && aluno.segundaChamada !== undefined ? aluno.segundaChamada.toFixed(2) : 'â€”';
            const mediaParcialFormat = mediaParcial !== null && mediaParcial !== undefined ? mediaParcial.toFixed(2) : 'â€”';
            const provaFinal = aluno.final !== null && aluno.final !== undefined ? aluno.final.toFixed(2) : 'â€”';
            const mediaFinalFormat = mediaFinal !== null && mediaFinal !== undefined ? mediaFinal.toFixed(2) : 'â€”';
            const faltas = faltasMap.get(aluno.alunoId) || 0;
            
            const tr = document.createElement('tr');
            
            // Determina classe CSS para mÃ©dia final
            let mediaFinalClass = '';
            if (mediaFinal !== null && mediaFinal !== undefined) {
                if (mediaFinal >= 6) {
                    mediaFinalClass = 'media-aprovado';
                } else {
                    mediaFinalClass = 'media-reprovado';
                }
            }
            
            tr.innerHTML = `
                <td>${escapeHtml(aluno.nome)}</td>
                <td>${av1}</td>
                <td>${av2}</td>
                <td>${segundaChamada}</td>
                <td>${mediaParcialFormat}</td>
                <td>${provaFinal}</td>
                <td class="${mediaFinalClass}">${mediaFinalFormat}</td>
                <td>${faltas}</td>
            `;
            tbody.appendChild(tr);
        });

        tabelaContainer.style.display = 'block';

    } catch (error) {
        console.error('Erro ao carregar alunos:', error);
        loadingElement.style.display = 'none';
        emptyState.style.display = 'block';
    }
}

/**
 * Calcula a mÃ©dia parcial seguindo as regras:
 * - MÃ©dia = (Av1 + Av2) / 2
 * - Se Av1 preenchida, Av2 nÃ£o preenchida, e Segunda Chamada preenchida,
 *   entÃ£o Segunda Chamada substitui Av2
 */
function calcularMediaParcial(av1, av2, segundaChamada) {
    let nota1 = av1;
    let nota2 = av2;
    
    // Se Av1 estÃ¡ preenchida, Av2 nÃ£o estÃ¡, e Segunda Chamada estÃ¡ preenchida,
    // entÃ£o Segunda Chamada substitui Av2
    if (av1 !== null && av1 !== undefined && (av2 === null || av2 === undefined) && segundaChamada !== null && segundaChamada !== undefined) {
        nota2 = segundaChamada;
    }
    
    // Calcula mÃ©dia
    if (nota1 !== null && nota1 !== undefined && nota2 !== null && nota2 !== undefined) {
        return (nota1 + nota2) / 2.0;
    } else if (nota1 !== null && nota1 !== undefined) {
        return nota1;
    } else if (nota2 !== null && nota2 !== undefined) {
        return nota2;
    }
    
    return null;
}

function voltarParaDisciplinas() {
    window.location.href = 'disciplinas-professor.html';
}

function realizarChamada() {
    // Salva os parÃ¢metros para a prÃ³xima pÃ¡gina
    const params = new URLSearchParams({
        salaId: salaId,
        disciplina: disciplinaNome || '',
        sala: salaIdentificador || ''
    });
    window.location.href = `chamada-professor.html?${params.toString()}`;
}

function atribuirNotas() {
    // Salva os parÃ¢metros para a prÃ³xima pÃ¡gina
    const params = new URLSearchParams({
        salaId: salaId,
        disciplina: disciplinaNome || '',
        sala: salaIdentificador || ''
    });
    window.location.href = `notas-professor.html?${params.toString()}`;
}

function handleLogout() {
    localStorage.clear();
    window.location.href = 'login-professor.html';
}

function toggleSidebar() {
    document.querySelector('.sidebar').classList.toggle('collapsed');
}

// Carrega os alunos ao iniciar
if (salaId) {
    carregarAlunos();
} else {
    document.getElementById('loading').style.display = 'none';
    document.getElementById('empty-state').style.display = 'block';
}
