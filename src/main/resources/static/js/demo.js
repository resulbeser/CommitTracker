// Web Demo JavaScript
document.addEventListener('DOMContentLoaded', function() {
    const commitForm = document.getElementById('commitForm');
    const resultsContainer = document.getElementById('resultsContainer');
    const loadingSpinner = document.getElementById('loadingSpinner');

    commitForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const formData = {
            platform: document.getElementById('platform').value,
            owner: document.getElementById('owner').value,
            repo: document.getElementById('repo').value,
            accessToken: document.getElementById('accessToken').value
        };

        if (!formData.platform || !formData.owner || !formData.repo) {
            showError('Lütfen tüm gerekli alanları doldurun');
            return;
        }

        showLoading(true);

        try {
            const response = await fetch('/api/commits', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const commits = await response.json();
            displayCommits(commits, formData);

        } catch (error) {
            console.error('Error:', error);
            showError('Commit verileri alınırken hata oluştu: ' + error.message);
        } finally {
            showLoading(false);
        }
    });

    function showLoading(show) {
        const submitButton = commitForm.querySelector('button[type="submit"]');
        if (show) {
            loadingSpinner.classList.remove('d-none');
            submitButton.disabled = true;
            submitButton.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Yükleniyor...';
        } else {
            loadingSpinner.classList.add('d-none');
            submitButton.disabled = false;
            submitButton.innerHTML = 'Commit\'leri Getir';
        }
    }

    function displayCommits(commits, formData) {
        if (!commits || commits.length === 0) {
            resultsContainer.innerHTML = `
                <div class="error-message">
                    <strong>Sonuç bulunamadı!</strong><br>
                    Bu repository için commit verisi bulunamadı veya erişim yetkisiz.
                </div>
            `;
            return;
        }

        // Show success toast notification
        showSuccessToast(`${formData.platform.toUpperCase()} - ${formData.owner}/${formData.repo} • ${commits.length} commit`);

        // Stats card
        const statsHtml = `
            <div class="stats-card">
                <span class="stats-number">${commits.length}</span>
                <span class="stats-label">Toplam Commit</span>
            </div>
        `;

        // Commits list
        const commitsHtml = commits.map(commit => {
            const sha = commit.sha ? commit.sha.substring(0, 7) : 'N/A';
            const author = commit.commit?.author?.name || 'Unknown';
            const date = commit.commit?.author?.date ?
                new Date(commit.commit.author.date).toLocaleString('tr-TR') : 'N/A';
            const message = commit.commit?.message || 'No message';

            return `
                <div class="commit-item">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <span class="commit-hash">${sha}</span>
                        <small class="commit-date">${date}</small>
                    </div>
                    <div class="commit-author mb-1">👤 ${author}</div>
                    <div class="commit-message">${escapeHtml(message)}</div>
                </div>
            `;
        }).join('');

        // Remove the old success message from results container
        resultsContainer.innerHTML = statsHtml + commitsHtml;
    }

    // Toast notification function
    function showSuccessToast(message) {
        const toastElement = document.getElementById('successToast');
        const toastMessage = document.getElementById('toastMessage');

        toastMessage.textContent = message;

        const toast = new bootstrap.Toast(toastElement, {
            autohide: true,
            delay: 5000 // 5 seconds
        });

        toast.show();
    }

    function showError(message) {
        resultsContainer.innerHTML = `
            <div class="error-message">
                <strong>❌ Hata!</strong><br>
                ${message}
            </div>
        `;
    }

    function escapeHtml(text) {
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return text.replace(/[&<>"']/g, function(m) { return map[m]; });
    }

    // Demo data için örnek buton
    const demoButton = document.createElement('button');
    demoButton.className = 'btn btn-outline-secondary btn-sm mt-2 w-100';
    demoButton.innerHTML = '🎯 Demo Verisi Yükle';
    demoButton.onclick = function() {
        document.getElementById('platform').value = 'github';
        document.getElementById('owner').value = 'octocat';
        document.getElementById('repo').value = 'Hello-World';
        document.getElementById('accessToken').value = '';
    };

    commitForm.appendChild(demoButton);
});
