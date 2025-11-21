const API_BASE_URL = 'http://localhost:8080/api';

let currentUser = null;
let authToken = null;

// Inicialização
document.addEventListener('DOMContentLoaded', () => {
    // Verificar autenticação
    authToken = localStorage.getItem('authToken');
    const userStr = localStorage.getItem('currentUser');
    
    if (!authToken || !userStr) {
        window.location.href = '/admin/login';
        return;
    }
    
    currentUser = JSON.parse(userStr);
    
    // Verificar se é admin
    if (currentUser.perfil !== 'ADMINISTRADOR') {
        localStorage.clear();
        window.location.href = '/admin/login';
        return;
    }
    
    initDashboard();
    loadDashboardStats();
});

// Dashboard
function initDashboard() {
    // Toggle sidebar
    const toggleBtn = document.getElementById('toggle-sidebar');
    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            document.getElementById('sidebar').classList.toggle('collapsed');
        });
    }
    
    // Navigation
    initNavigation();
    
    // Logout
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            localStorage.clear();
            window.location.href = '/admin/login';
        });
    }
    
    // Forms
    initProfessorForm();
    initDisciplinaForm();
    initPeriodoForm();
    
    // User info
    if (currentUser) {
        document.getElementById('user-name').textContent = currentUser.email || 'Administrador';
    }
}

function initNavigation() {
    const navItems = document.querySelectorAll('.nav-item[data-page]');
    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const page = item.getAttribute('data-page');
            showPage(page);
            
            // Update active state
            navItems.forEach(nav => nav.classList.remove('active'));
            item.classList.add('active');
        });
    });
}

function showPage(pageName) {
    // Hide all pages
    document.querySelectorAll('.page').forEach(page => {
        page.classList.remove('active');
    });
    
    // Show selected page
    const pageElement = document.getElementById(`${pageName}-page`);
    if (pageElement) {
        pageElement.classList.add('active');
        const title = pageElement.querySelector('h2')?.textContent || 'Dashboard';
        document.getElementById('page-title').textContent = title;
        
        // Load page data
        if (pageName === 'dashboard') {
            loadDashboardStats();
        } else if (pageName === 'professores') {
            loadProfessores();
        } else if (pageName === 'disciplinas') {
            loadDisciplinas();
        } else if (pageName === 'usuarios') {
            loadUsuarios();
        }
    }
}

async function loadDashboardStats() {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/dashboard/estatisticas`);
        
        if (response.ok) {
            const stats = await response.json();
            document.getElementById('total-alunos').textContent = stats.totalAlunos || 0;
            document.getElementById('total-professores').textContent = stats.totalProfessores || 0;
            document.getElementById('disciplinas-ativas').textContent = stats.disciplinasAtivas || 0;
            document.getElementById('turmas-ativas').textContent = stats.turmasAtivas || 0;
        }
    } catch (error) {
        console.error('Erro ao carregar estatísticas:', error);
    }
}

// Professores
function initProfessorForm() {
    const btnCriar = document.getElementById('btn-criar-professor');
    const form = document.getElementById('form-criar-professor');
    const btnCancelar = document.getElementById('btn-cancelar-professor');
    
    if (btnCriar) {
        btnCriar.addEventListener('click', () => {
            form.style.display = form.style.display === 'none' ? 'block' : 'none';
        });
    }
    
    if (btnCancelar) {
        btnCancelar.addEventListener('click', () => {
            form.style.display = 'none';
            form.reset();
        });
    }
    
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const data = {
                nome: document.getElementById('prof-nome').value,
                email: document.getElementById('prof-email').value,
                senha: document.getElementById('prof-senha').value
            };
            
            try {
                const response = await fetch(`${API_BASE_URL}/admin/usuarios/professores`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(data)
                });
                
                if (response.ok) {
                    form.style.display = 'none';
                    form.reset();
                    loadProfessores();
                    loadDashboardStats();
                    alert('Professor criado com sucesso!');
                } else {
                    alert('Erro ao criar professor');
                }
            } catch (error) {
                alert('Erro ao conectar com o servidor');
            }
        });
    }
}

async function loadProfessores() {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/usuarios/professores`);
        
        if (response.ok) {
            const professores = await response.json();
            const listDiv = document.getElementById('professores-list');
            listDiv.innerHTML = professores.map(prof => `
                <div class="list-item">
                    <div>
                        <h4>${prof.nome}</h4>
                        <p>${prof.email}</p>
                    </div>
                </div>
            `).join('');
        }
    } catch (error) {
        console.error('Erro ao carregar professores:', error);
    }
}

// Disciplinas
function initDisciplinaForm() {
    const btnCriar = document.getElementById('btn-criar-disciplina');
    const form = document.getElementById('form-criar-disciplina');
    const btnCancelar = document.getElementById('btn-cancelar-disciplina');
    
    if (btnCriar) {
        btnCriar.addEventListener('click', () => {
            form.style.display = form.style.display === 'none' ? 'block' : 'none';
        });
    }
    
    if (btnCancelar) {
        btnCancelar.addEventListener('click', () => {
            form.style.display = 'none';
            form.reset();
        });
    }
    
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const data = {
                codigoDisciplina: document.getElementById('disc-codigo').value,
                nome: document.getElementById('disc-nome').value,
                descricao: document.getElementById('disc-descricao').value,
                preRequisitosIds: []
            };
            
            try {
                const response = await fetch(`${API_BASE_URL}/admin/disciplinas`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(data)
                });
                
                if (response.ok) {
                    form.style.display = 'none';
                    form.reset();
                    loadDisciplinas();
                    loadDashboardStats();
                    alert('Disciplina criada com sucesso!');
                } else {
                    alert('Erro ao criar disciplina');
                }
            } catch (error) {
                alert('Erro ao conectar com o servidor');
            }
        });
    }
}

async function loadDisciplinas() {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/disciplinas`);
        
        if (response.ok) {
            const disciplinas = await response.json();
            const listDiv = document.getElementById('disciplinas-list');
            listDiv.innerHTML = disciplinas.map(disc => `
                <div class="list-item">
                    <div>
                        <h4>${disc.nome}</h4>
                        <p>Código: ${disc.codigoDisciplina}</p>
                        ${disc.descricao ? `<p>${disc.descricao}</p>` : ''}
                    </div>
                </div>
            `).join('');
        }
    } catch (error) {
        console.error('Erro ao carregar disciplinas:', error);
    }
}

// Períodos Letivos
function initPeriodoForm() {
    const btnCriar = document.getElementById('btn-criar-periodo');
    const form = document.getElementById('form-criar-periodo');
    const btnCancelar = document.getElementById('btn-cancelar-periodo');
    
    if (btnCriar) {
        btnCriar.addEventListener('click', () => {
            form.style.display = form.style.display === 'none' ? 'block' : 'none';
        });
    }
    
    if (btnCancelar) {
        btnCancelar.addEventListener('click', () => {
            form.style.display = 'none';
            form.reset();
        });
    }
    
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const data = {
                identificador: document.getElementById('periodo-identificador').value,
                dataInicio: document.getElementById('periodo-data-inicio').value,
                dataFim: document.getElementById('periodo-data-fim').value,
                dataInicioMatricula: document.getElementById('periodo-data-inicio-matricula').value,
                dataFimMatricula: document.getElementById('periodo-data-fim-matricula').value
            };
            
            try {
                const response = await fetch(`${API_BASE_URL}/admin/periodos-letivos`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(data)
                });
                
                if (response.ok) {
                    form.style.display = 'none';
                    form.reset();
                    alert('Período letivo criado com sucesso!');
                } else {
                    alert('Erro ao criar período letivo');
                }
            } catch (error) {
                alert('Erro ao conectar com o servidor');
            }
        });
    }
}

// Usuários
async function loadUsuarios() {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/usuarios`);
        
        if (response.ok) {
            const usuarios = await response.json();
            const listDiv = document.getElementById('usuarios-list');
            listDiv.innerHTML = usuarios.map(user => `
                <div class="list-item">
                    <div>
                        <h4>${user.nome}</h4>
                        <p>${user.email} - ${user.perfil}</p>
                        <p>Status: ${user.status}</p>
                    </div>
                </div>
            `).join('');
        }
    } catch (error) {
        console.error('Erro ao carregar usuários:', error);
    }
}

