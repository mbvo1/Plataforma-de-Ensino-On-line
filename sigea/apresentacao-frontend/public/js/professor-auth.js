async function handleProfessorLogin(event) {
    event.preventDefault();
    
    const email = document.getElementById('login-email').value;
    const senha = document.getElementById('login-senha').value;
    
    const errorDiv = document.getElementById('login-error');
    errorDiv.style.display = 'none';
    
    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email, senha })
        });
        
        if (response.ok) {
            const data = await response.json();
            
            // Verifica se o usuÃ¡rio Ã© realmente um professor
            if (data.perfil !== 'PROFESSOR') {
                errorDiv.textContent = 'Este login Ã© exclusivo para professores';
                errorDiv.style.display = 'block';
                return;
            }
            
            // Salvar dados do usuÃ¡rio no localStorage
            localStorage.setItem('usuarioId', data.usuarioId);
            localStorage.setItem('usuarioNome', data.nome);
            localStorage.setItem('usuarioEmail', data.email);
            localStorage.setItem('usuarioPerfil', data.perfil);
            localStorage.setItem('token', data.token);

            // Redirecionar para dashboard do professor
            window.location.href = 'dashboard-professor.html';
        } else {
            const error = await response.json();
            console.error('Erro de login:', error);
            console.error('Status:', response.status);
            errorDiv.textContent = error.message || 'Credenciais invÃ¡lidas';
            errorDiv.style.display = 'block';
        }
    } catch (error) {
        console.error('Erro ao fazer login:', error);
        errorDiv.textContent = 'Erro ao conectar com o servidor. Tente novamente.';
        errorDiv.style.display = 'block';
    }
}
