async function testServlet() {
    const output = document.getElementById('testOutput');
    output.className = 'show';
    output.textContent = '⏳ Probando servlet...';
    output.style.background = '#fff3cd';
    output.style.color = '#856404';

    try {
        const response = await fetch('/ContactoProfesionalesWeb/api/test');
        const data = await response.json();

        output.className = 'show success';
        output.textContent = '✓ Servlet: ' + data.message;
    } catch (error) {
        output.className = 'show error';
        output.textContent = '✗ Error en servlet: ' + error.message;
    }
}

async function testDatabase() {
    const output = document.getElementById('testOutput');
    output.className = 'show';
    output.textContent = '⏳ Probando base de datos...';
    output.style.background = '#fff3cd';
    output.style.color = '#856404';

    try {
        const response = await fetch('/ContactoProfesionalesWeb/api/test-db');
        const data = await response.json();

        if (data.status === 'success') {
            output.className = 'show success';
            output.textContent = '✓ Database: ' + data.message + ' | Usuarios: ' + data.totalUsuarios;
        } else {
            output.className = 'show error';
            output.textContent = '✗ Database: ' + data.message;
        }
    } catch (error) {
        output.className = 'show error';
        output.textContent = '✗ Error en database: ' + error.message;
    }
}

// Test automático al cargar
window.addEventListener('load', function() {
    console.log('✓ Página cargada correctamente');
    console.log('✓ JavaScript funcionando');
});
