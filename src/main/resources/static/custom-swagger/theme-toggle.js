// Dynamic Theme Switcher Script for Swagger UI
(function () {
    function initThemeToggle() {
        if (!document.body || document.getElementById('theme-toggle-btn')) return;

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

        btn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            const isDark = document.body.classList.toggle('dark-mode');
            if (isDark) {
                localStorage.setItem('swagger-theme', 'dark');
                btn.innerHTML = '☀️ Light Mode';
            } else {
                localStorage.setItem('swagger-theme', 'light');
                btn.innerHTML = '🌙 Dark Mode';
            }
        });

        const topbarWrapper = document.querySelector('.swagger-ui .topbar .wrapper');
        if (topbarWrapper) {
            topbarWrapper.appendChild(btn);
        } else {
            document.body.appendChild(btn);
        }
    }

    if (document.readyState === 'interactive' || document.readyState === 'complete') {
        initThemeToggle();
    } else {
        window.addEventListener('DOMContentLoaded', initThemeToggle);
    }

    const interval = setInterval(() => {
        initThemeToggle();
        if (document.getElementById('theme-toggle-btn')) {
            clearInterval(interval);
        }
    }, 250);
})();
