// Verifica autenticação ao carregar a página
window.addEventListener('DOMContentLoaded', () => {
    const usuarioId = localStorage.getItem('usuarioId');
    const usuarioPerfil = localStorage.getItem('usuarioPerfil');
    
    if (!usuarioId || usuarioPerfil !== 'ALUNO') {
        window.location.href = '/';
        return;
    }
    
    loadUserInfo();
    initializeNavigation();
    carregarDadosDashboard();
});

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
    // Atualizar título de boas-vindas
    const welcomeTitle = document.querySelector('.welcome-title');
    if (welcomeTitle) {
        welcomeTitle.textContent = `Bem-vindo, "${dados.nomeAluno}"!`;
    }
    
    // Atualizar avisos
    const avisosCard = document.querySelector('.info-card:nth-child(2) .card-content');
    if (avisosCard) {
        if (dados.avisosNaoLidos > 0) {
            avisosCard.innerHTML = `<p style="color: #e74c3c; font-weight: bold;">Você tem ${dados.avisosNaoLidos} avisos não lidos</p>`;
        } else {
            avisosCard.innerHTML = `<p style="color: #27ae60;">Nenhum aviso novo</p>`;
        }
    }
    
    // Atualizar frequência
    const frequenciaContent = document.querySelector('.frequencia-content');
    if (frequenciaContent) {
        frequenciaContent.innerHTML = `
            <p class="freq-title">Total</p>
            <p>Faltas: ${dados.totalFaltas}</p>
            <p>Frequência: ${dados.frequenciaPercentual.toFixed(0)}%</p>
        `;
    }
}

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    const perfil = localStorage.getItem('usuarioPerfil');
    
    // Atualiza o nome do usuário no header
    const userNameElement = document.getElementById('user-name');
    if (userNameElement) {
        userNameElement.textContent = `Aluno - ${nome || 'Usuário'}`;
    }
}

function initializeNavigation() {
    const menuItems = document.querySelectorAll('.menu-item[data-section]');
    
    menuItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            
            const sectionName = item.getAttribute('data-section');
            
            // Remove active class from all menu items
            menuItems.forEach(mi => mi.classList.remove('active'));
            
            // Add active class to clicked item
            item.classList.add('active');
            
            // Hide all sections
            const sections = document.querySelectorAll('.content-section');
            sections.forEach(section => section.classList.remove('active'));
            
            // Show selected section
            const selectedSection = document.getElementById(`${sectionName}-section`);
            if (selectedSection) {
                selectedSection.classList.add('active');
            }
        });
    });
}

function handleLogout() {
    if (confirm('Deseja realmente sair?')) {
        localStorage.removeItem('usuarioId');
        localStorage.removeItem('usuarioNome');
        localStorage.removeItem('usuarioEmail');
        localStorage.removeItem('usuarioPerfil');
        
        window.location.href = '/';
    }
}
