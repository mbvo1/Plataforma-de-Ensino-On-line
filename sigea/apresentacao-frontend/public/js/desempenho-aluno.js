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
    carregarDesempenho();
});

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    const userNameElement = document.getElementById('user-name');
    if (userNameElement) {
        userNameElement.textContent = `Aluno - ${nome || 'Usuário'}`;
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

async function carregarDesempenho() {
    const loadingElement = document.getElementById('loading');
    const desempenhoContent = document.getElementById('desempenho-content');
    const emptyState = document.getElementById('empty-state');
    const tbody = document.getElementById('desempenho-tbody');
    const usuarioId = localStorage.getItem('usuarioId');
    
    loadingElement.style.display = 'block';
    desempenhoContent.style.display = 'none';
    emptyState.style.display = 'none';
    
    try {
        const response = await fetch(`http://localhost:8080/api/aluno/${usuarioId}/desempenho`);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar desempenho');
        }
        
        const desempenho = await response.json();
        
        loadingElement.style.display = 'none';
        
        if (!desempenho || desempenho.length === 0) {
            emptyState.style.display = 'block';
            return;
        }
        
        // Limpa tbody
        tbody.innerHTML = '';
        
        // Preenche a tabela
        desempenho.forEach(d => {
            const tr = document.createElement('tr');
            
            // Formata valores
            const av1 = d.av1 !== null && d.av1 !== undefined ? d.av1.toFixed(2) : '—';
            const av2 = d.av2 !== null && d.av2 !== undefined ? d.av2.toFixed(2) : '—';
            const segundaChamada = d.segundaChamada !== null && d.segundaChamada !== undefined ? d.segundaChamada.toFixed(2) : '—';
            const mediaParcial = d.mediaParcial !== null && d.mediaParcial !== undefined ? d.mediaParcial.toFixed(2) : '—';
            const provaFinal = d.provaFinal !== null && d.provaFinal !== undefined ? d.provaFinal.toFixed(2) : '—';
            const mediaFinal = d.mediaFinal !== null && d.mediaFinal !== undefined ? d.mediaFinal.toFixed(2) : '—';
            const faltas = d.faltas !== null && d.faltas !== undefined ? d.faltas : 0;
            
            // Determina classe CSS para média final (aprovado/reprovado)
            let mediaFinalClass = '';
            if (d.mediaFinal !== null && d.mediaFinal !== undefined) {
                if (d.mediaFinal >= 6) {
                    mediaFinalClass = 'media-aprovado';
                } else {
                    mediaFinalClass = 'media-reprovado';
                }
            }
            
            tr.innerHTML = `
                <td>${d.disciplinaNome}</td>
                <td>${av1}</td>
                <td>${av2}</td>
                <td>${segundaChamada}</td>
                <td>${mediaParcial}</td>
                <td>${provaFinal}</td>
                <td class="${mediaFinalClass}">${mediaFinal}</td>
                <td>${faltas}</td>
            `;
            tbody.appendChild(tr);
        });
        
        desempenhoContent.style.display = 'block';
        
    } catch (error) {
        console.error('Erro ao carregar desempenho:', error);
        loadingElement.style.display = 'none';
        emptyState.style.display = 'block';
        emptyState.innerHTML = `
            <i class="fas fa-exclamation-triangle"></i>
            <p>Erro ao carregar desempenho. Tente novamente.</p>
        `;
    }
}

function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        localStorage.clear();
        window.location.href = '/';
    }
}
