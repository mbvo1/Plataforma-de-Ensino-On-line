// Verifica autenticação ao carregar a página
window.addEventListener('DOMContentLoaded', () => {
    const usuarioId = localStorage.getItem('usuarioId');
    
    if (!usuarioId) {
        window.location.href = '/';
        return;
    }
    
    loadUserInfo();
    initializeNavigation();
});

function loadUserInfo() {
    const nome = localStorage.getItem('usuarioNome');
    const email = localStorage.getItem('usuarioEmail');
    const perfil = localStorage.getItem('usuarioPerfil');
    
    // Atualiza o nome do usuário no header
    document.getElementById('user-name').textContent = `Aluno - ${nome || 'Usuário'}`;
    
    // Atualiza o nome de boas-vindas
    const welcomeNameElement = document.getElementById('welcome-name');
    if (welcomeNameElement) {
        welcomeNameElement.textContent = nome || 'Usuário';
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
