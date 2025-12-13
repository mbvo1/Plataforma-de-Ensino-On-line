// disciplinas-professor.js
const usuarioId = localStorage.getItem('usuarioId');
document.getElementById('userName').textContent = `Professor - ${localStorage.getItem('usuarioNome') || 'Usuário'}`;

async function carregarDisciplinas() {
    const container = document.getElementById('disciplinasContainer');
    container.innerHTML = '';

    // Try to fetch from backend; fallback to mock list
    try {
        if (!usuarioId) throw new Error('No user');
        const resp = await fetch(`http://localhost:8080/api/professor/disciplinas?professorId=${usuarioId}`);
        if (!resp.ok) throw new Error('no-api');
        const list = await resp.json();
        if (!Array.isArray(list) || list.length === 0) {
            container.innerHTML = '<div class="empty-state-salas"><i class="fas fa-book empty-icon"></i><p>Nenhuma disciplina encontrada</p></div>';
            return;
        }
        list.forEach(item => renderDisciplina(item));
    } catch (err) {
        // fallback mock data (counts default to 0)
        const mock = [
            { disciplinaId: 1, nome: 'Disciplina 1', identificador: 'Sala A', alunosMatriculados: 0 },
            { disciplinaId: 1, nome: 'Disciplina 1', identificador: 'Sala B', alunosMatriculados: 0 },
            { disciplinaId: 7, nome: 'Disciplina 7', identificador: 'Sala A', alunosMatriculados: 0 },
            { disciplinaId: 7, nome: 'Disciplina 7', identificador: 'Sala B', alunosMatriculados: 0 }
        ];
        container.innerHTML = '';
        mock.forEach(d => renderDisciplina(d));
    }
}

function renderDisciplina(d) {
    const container = document.getElementById('disciplinasContainer');
    const card = document.createElement('div');
    card.className = 'disciplina-card';
    // Show "Nome da disciplina: Identificador da sala" in the left rectangle
    const identificador = d.identificador || d.identificador || '';
    const alunosCount = d.alunos ?? d.alunosMatriculados ?? 0;
    card.innerHTML = `
        <div class="disciplina-left">${d.nome}: ${identificador}</div>
        <div class="disciplina-right">Alunos : <strong>${alunosCount}</strong></div>
    `;
    card.addEventListener('click', () => {
        // Navega para a página de detalhes da disciplina
        try { sessionStorage.setItem('disciplinaSelecionada', JSON.stringify(d)); } catch(_) {}
        const params = new URLSearchParams({
            salaId: d.salaId || d.id || '',
            disciplina: d.nome || '',
            sala: identificador
        });
        window.location.href = `disciplina-detalhes-professor.html?${params.toString()}`;
    });
    container.appendChild(card);
}

carregarDisciplinas();

function handleLogout() { localStorage.clear(); window.location.href = 'login-professor.html'; }
