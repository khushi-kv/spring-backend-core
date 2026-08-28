// Dynamic Theme Switcher Script for Swagger UI
(function () {
    function initThemeToggle() {
        if (document.getElementById('theme-toggle-btn')) return;

        const btn = document.createElement('button');
        btn.id = 'theme-toggle-btn';

        const savedTheme = localStorage.getItem('swagger-theme') || 'dark';

        if (savedTheme === 'dark') {
            document.body.classList.add('dark-mode');
            btn.innerHTML = '☀️ Light Mode';
        } else {
            document.body.classList.remove('dark-mode');
            btn.innerHTML = '🌙 Dark Mode';
        }

        btn.addEventListener('click', () => {
            const isDark = document.body.classList.toggle('dark-mode');
            if (isDark) {
                localStorage.setItem('swagger-theme', 'dark');
                btn.innerHTML = '☀️ Light Mode';
            } else {
                localStorage.setItem('swagger-theme', 'light');
                btn.innerHTML = '🌙 Dark Mode';
            }
        });

        document.body.appendChild(btn);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initThemeToggle);
    } else {
        initThemeToggle();
    }
})();
