// ========================================================================
// UTILS - funções auxiliares globais
// ========================================================================

function fillCredentials(email, password) {
    const emailInput = document.getElementById('email');
    const passInput = document.getElementById('password');
    if (emailInput && passInput) {
        emailInput.value = email;
        passInput.value = password;
    }
}

function showAlert(message, type = 'danger') {
    const alertBox = document.getElementById('alert-box');
    if (!alertBox) return;
    alertBox.textContent = message;
    alertBox.className = `alert alert-${type}`;
}

function showBanner(message, type = 'danger') {
    const banner = document.getElementById('alert-banner');
    if (!banner) return;
    banner.textContent = message;
    banner.className = `alert alert-${type}`;
    banner.classList.remove('d-none');
    setTimeout(() => {
        banner.classList.add('d-none');
    }, 6000);
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    const parts = dateStr.split('-');
    if (parts.length === 3) {
        return `${parts[2]}/${parts[1]}/${parts[0]}`;
    }
    return dateStr;
}

function formatDateTime(isoStr) {
    if (!isoStr) return '-';
    try {
        const dt = new Date(isoStr);
        return dt.toLocaleString('pt-BR', {
            day: '2-digit', month: '2-digit', year: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });
    } catch (e) {
        return isoStr;
    }
}

function escapeHtml(text) {
    if (!text) return '';
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function authHeader() {
    const token = localStorage.getItem('sse_token');
    return { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' };
}

// ========================================================================
// LOGIN
// ========================================================================

const loginForm = document.getElementById('login-form');
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;
        const btn = document.getElementById('btn-login');

        try {
            btn.disabled = true;
            btn.textContent = 'Autenticando...';

            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });

            const data = await response.json();

            if (!response.ok) {
                const message = data.message || 'Falha ao autenticar. Tente novamente.';
                showAlert(message, 'danger');
                return;
            }

            localStorage.setItem('sse_token', data.token);
            localStorage.setItem('sse_user', JSON.stringify(data.user));

            showAlert('Autenticado com sucesso! Redirecionando...', 'success');
            setTimeout(() => {
                window.location.href = '/dashboard.html';
            }, 500);

        } catch (err) {
            showAlert('Não foi possível conectar ao servidor. Verifique sua conexão.', 'danger');
        } finally {
            btn.disabled = false;
            btn.textContent = 'Entrar no Sistema';
        }
    });
}

// ========================================================================
// DASHBOARD - inicialização e RBAC
// ========================================================================

function initDashboard() {
    const token = localStorage.getItem('sse_token');
    const rawUser = localStorage.getItem('sse_user');

    if (!token || !rawUser) {
        window.location.href = '/index.html';
        return;
    }

    try {
        const user = JSON.parse(rawUser);
        const userDisplay = document.getElementById('user-display');
        const roleBadge = document.getElementById('role-badge');
        const welcomeTitle = document.getElementById('welcome-title');
        const roleDesc = document.getElementById('role-description');

        const roleLabels = {
            COORDENADOR: 'Coordenador',
            SUPERVISOR: 'Supervisor',
            RESPONSAVEL: 'Responsável',
            ALUNO: 'Aluno'
        };

        const displayRole = user.roleDescription || roleLabels[user.role] || user.role;

        if (userDisplay) userDisplay.textContent = user.name;
        if (roleBadge) roleBadge.textContent = displayRole;
        if (welcomeTitle) welcomeTitle.textContent = `Olá, ${user.name}`;

        const roleDescriptions = {
            COORDENADOR: 'Perfil: Coordenador - Acesso completo à gestão escolar, matrículas e controle de acessos.',
            SUPERVISOR: 'Perfil: Supervisor - Monitoramento de entradas/saídas e operação de autorizações.',
            RESPONSAVEL: 'Perfil: Responsável Familiar - Acompanhamento das informações dos alunos vinculados.',
            ALUNO: 'Perfil: Aluno - Consulta básica de histórico e comunicados escolares.'
        };

        if (roleDesc && roleDescriptions[user.role]) {
            roleDesc.textContent = roleDescriptions[user.role];
        }

        if (user.role === 'COORDENADOR' || user.role === 'SUPERVISOR') {
            showSection('admin-students-section');
            showSection('admin-pickups-section');
            showSection('admin-early-exits-section'); showSection('admin-alerts-section'); showSection('admin-access-control-section'); loadSecurityAlerts(); loadDevices(); loadAccessLogs(); setInterval(() => { loadAccessLogs(); loadSecurityAlerts(); }, 5000);
            loadStudents();
            populateStudentSelects();
            loadEarlyExits(); showSection('admin-reports-section'); loadAdminReports(); showSection('admin-announcements-section'); loadAdminAnnouncements();
        } else if (user.role === 'RESPONSAVEL') {
            showSection('guardian-notifications-section'); showSection('guardian-students-section'); loadGuardianNotifications(); loadGuardianStudents(); setInterval(loadGuardianNotifications, 5000); showSection('guardian-announcements-section'); loadGuardianAnnouncements();
        } else if (user.role === 'ALUNO') {
            showSection('student-portal-section');
            loadStudentPortal();
        }

    } catch (e) {
        logout();
    }
}

function showSection(id) {
    const el = document.getElementById(id);
    if (el) el.classList.remove('d-none');
}

// ========================================================================
// ALUNOS - Fase 2 (mantido integralmente)
// ========================================================================

let searchTimer;
function debounceSearch() {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => { loadStudents(); }, 300);
}

async function loadStudents() {
    const token = localStorage.getItem('sse_token');
    const name = document.getElementById('filter-name')?.value || '';
    const className = document.getElementById('filter-class')?.value || '';
    const active = document.getElementById('filter-active')?.value || '';

    const params = new URLSearchParams();
    if (name.trim()) params.append('name', name.trim());
    if (className.trim()) params.append('className', className.trim());
    if (active !== '') params.append('active', active);

    const tbody = document.getElementById('students-table-body');
    if (!tbody) return;

    try {
        const response = await fetch(`/api/admin/students?${params.toString()}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) {
            tbody.innerHTML = `<tr><td colspan="9" class="text-center" style="color:var(--danger)">Erro ao carregar alunos (HTTP ${response.status}).</td></tr>`;
            return;
        }

        const students = await response.json();
        if (students.length === 0) {
            tbody.innerHTML = '<tr><td colspan="9" class="text-center">Nenhum aluno encontrado para os filtros informados.</td></tr>';
            return;
        }

        tbody.innerHTML = students.map(s => {
            const statusBadge = s.active
                ? '<span class="badge badge-active">Ativo</span>'
                : '<span class="badge badge-inactive">Inativo</span>';

            const toggleBtn = s.active
                ? `<button class="btn-action-status btn-deactivate" onclick="toggleStudentStatus(${s.id}, false)">Desativar</button>`
                : `<button class="btn-action-status btn-activate" onclick="toggleStudentStatus(${s.id}, true)">Ativar</button>`;

            const guardiansHtml = (s.guardians && s.guardians.length > 0)
                ? s.guardians.map(g => `
                    <span class="guardian-tag">
                        ${escapeHtml(g.guardianName)} (${escapeHtml(g.relationship)})
                        <span class="guardian-tag-remove" title="Desvincular" onclick="unlinkGuardian(${s.id}, ${g.guardianUserId})">&times;</span>
                    </span>
                  `).join('')
                : '<span style="color:var(--text-muted); font-size:0.75rem;">Nenhum vinculado</span>';

            return `
                <tr>
                    <td><strong>#${s.id}</strong></td>
                    <td><strong>${escapeHtml(s.fullName)}</strong></td>
                    <td>${escapeHtml(s.className)}</td>
                    <td>${formatDate(s.birthDate)}</td>
                    <td>${escapeHtml(s.contactPhone)}</td>
                    <td><code>${escapeHtml(s.badgeCode)}</code></td>
                    <td>${statusBadge}</td>
                    <td>
                        ${guardiansHtml}
                        <br>
                        <button class="btn btn-outline btn-sm" style="margin-top:0.35rem; font-size:0.7rem;" onclick="openLinkModal(${s.id}, '${escapeHtml(s.fullName)}')">+ Vincular</button>
                    </td>
                    <td>${toggleBtn}</td>
                </tr>
            `;
        }).join('');

    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="9" class="text-center" style="color:var(--danger)">Erro na conexão com o servidor.</td></tr>`;
    }
}

function toggleStudentForm() {
    const card = document.getElementById('student-form-card');
    if (!card) return;
    card.classList.toggle('d-none');
    if (!card.classList.contains('d-none')) {
        document.getElementById('student-form').reset();
        document.getElementById('student-id').value = '';
        document.getElementById('student-name').focus();
    }
}

async function saveStudent(event) {
    event.preventDefault();
    const token = localStorage.getItem('sse_token');

    const payload = {
        fullName: document.getElementById('student-name').value.trim(),
        birthDate: document.getElementById('student-birth').value,
        className: document.getElementById('student-class').value.trim(),
        contactPhone: document.getElementById('student-phone').value.trim(),
        badgeCode: document.getElementById('student-badge').value.trim(),
        active: document.getElementById('student-active').value === 'true'
    };

    try {
        const response = await fetch('/api/admin/students', {
            method: 'POST',
            headers: authHeader(),
            body: JSON.stringify(payload)
        });

        const data = await response.json();
        if (!response.ok) {
            showBanner(data.message || 'Erro ao salvar aluno.', 'danger');
            return;
        }

        showBanner(`Aluno "${data.fullName}" cadastrado com sucesso!`, 'success');
        toggleStudentForm();
        loadStudents();
        populateStudentSelects();

    } catch (err) {
        showBanner('Erro na comunicação com o servidor.', 'danger');
    }
}

async function toggleStudentStatus(studentId, newStatus) {
    const token = localStorage.getItem('sse_token');
    const actionName = newStatus ? 'ativar' : 'desativar';

    if (!confirm(`Deseja realmente ${actionName} o cadastro deste aluno?`)) return;

    try {
        const response = await fetch(`/api/admin/students/${studentId}/status`, {
            method: 'PATCH',
            headers: authHeader(),
            body: JSON.stringify({ active: newStatus })
        });

        const data = await response.json();
        if (!response.ok) {
            showBanner(data.message || 'Erro ao alterar situação do cadastro.', 'danger');
            return;
        }

        showBanner(`Cadastro de "${data.fullName}" atualizado para ${newStatus ? 'Ativo' : 'Inativo'}.`, 'success');
        loadStudents();

    } catch (err) {
        showBanner('Erro na comunicação com o servidor.', 'danger');
    }
}

function openLinkModal(studentId, studentName) {
    document.getElementById('modal-student-id').value = studentId;
    document.getElementById('modal-student-name').textContent = `Aluno: ${studentName}`;
    document.getElementById('modal-guardian-id').value = '';
    document.getElementById('modal-relationship').value = '';
    document.getElementById('modal-link-guardian').classList.remove('d-none');
}

function closeModal() {
    document.getElementById('modal-link-guardian').classList.add('d-none');
}

async function submitLinkGuardian(event) {
    event.preventDefault();
    const studentId = document.getElementById('modal-student-id').value;
    const guardianUserId = Number(document.getElementById('modal-guardian-id').value);
    const relationship = document.getElementById('modal-relationship').value.trim();

    try {
        const response = await fetch(`/api/admin/students/${studentId}/guardians`, {
            method: 'POST',
            headers: authHeader(),
            body: JSON.stringify({ guardianUserId, relationship })
        });

        const data = await response.json();
        if (!response.ok) {
            showBanner(data.message || 'Erro ao vincular responsável.', 'danger');
            return;
        }

        showBanner('Responsável vinculado com sucesso ao aluno!', 'success');
        closeModal();
        loadStudents();

    } catch (err) {
        showBanner('Erro na comunicação com o servidor.', 'danger');
    }
}

async function unlinkGuardian(studentId, guardianUserId) {
    if (!confirm('Deseja realmente desvincular este responsável do aluno?')) return;

    try {
        const response = await fetch(`/api/admin/students/${studentId}/guardians/${guardianUserId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${localStorage.getItem('sse_token')}` }
        });

        const data = await response.json();
        if (!response.ok) {
            showBanner(data.message || 'Erro ao desvincular responsável.', 'danger');
            return;
        }

        showBanner('Vínculo removido com sucesso.', 'success');
        loadStudents();

    } catch (err) {
        showBanner('Erro na comunicação com o servidor.', 'danger');
    }
}

// ========================================================================
// RESPONSÁVEL - dependentes (RF009)
// ========================================================================

async function loadGuardianStudents() {
    const token = localStorage.getItem('sse_token');
    const grid = document.getElementById('guardian-students-grid');
    if (!grid) return;

    try {
        const response = await fetch('/api/guardian/students', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) {
            grid.innerHTML = `<p style="color:var(--danger)">Erro ao carregar dependentes (HTTP ${response.status}).</p>`;
            return;
        }

        const students = await response.json();
        if (students.length === 0) {
            grid.innerHTML = '<p>Nenhum aluno vinculado à sua conta familiar. Entre em contato com a coordenação escolar.</p>';
            return;
        }

        grid.innerHTML = students.map(s => `
            <div class="student-card">
                <h3>${escapeHtml(s.fullName)}</h3>
                <div class="student-card-meta">
                    Turma: <strong>${escapeHtml(s.className)}</strong> | Nascimento: ${formatDate(s.birthDate)}
                </div>
                <div>
                    Situação Escolar: ${s.active
                        ? '<span class="badge badge-active">Ativo</span>'
                        : '<span class="badge badge-inactive">Inativo</span>'}
                </div>
                <div style="margin-top:0.75rem;">
                    <button class="btn btn-outline btn-sm" onclick="loadGuardianStudentPickups(${s.id}, '${escapeHtml(s.fullName)}')">
                        Ver Pessoas Autorizadas
                    </button>
                </div>
                <div id="guardian-pickups-${s.id}" class="d-none" style="margin-top:0.75rem;"></div>
            </div>
        `).join('');

    } catch (err) {
        grid.innerHTML = '<p style="color:var(--danger)">Erro ao conectar ao servidor.</p>';
    }
}

async function loadGuardianStudentPickups(studentId, studentName) {
    const token = localStorage.getItem('sse_token');
    const container = document.getElementById(`guardian-pickups-${studentId}`);
    if (!container) return;

    if (!container.classList.contains('d-none')) {
        container.classList.add('d-none');
        return;
    }

    container.classList.remove('d-none');
    container.innerHTML = '<p style="font-size:0.8rem; color:var(--text-muted)">Carregando autorizações...</p>';

    try {
        const response = await fetch(`/api/guardian/students/${studentId}/pickups`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) {
            const data = await response.json();
            container.innerHTML = `<p style="color:var(--danger);font-size:0.8rem">${escapeHtml(data.message || 'Erro ao carregar autorizações.')}</p>`;
            return;
        }

        const pickups = await response.json();
        if (pickups.length === 0) {
            container.innerHTML = '<p style="font-size:0.8rem;color:var(--text-muted)">Nenhuma pessoa autorizada cadastrada para este aluno.</p>';
            return;
        }

        container.innerHTML = `
            <p style="font-size:0.75rem;font-weight:600;color:var(--text-muted);margin-bottom:0.4rem;">Pessoas autorizadas a retirar ${escapeHtml(studentName)}:</p>
            ${pickups.map(p => `
                <div style="display:flex;align-items:center;gap:0.5rem;margin-bottom:0.3rem;font-size:0.82rem;">
                    ${pickupStatusBadge(p.status)}
                    <span><strong>${escapeHtml(p.fullName)}</strong> (${escapeHtml(p.relationship)})</span>
                </div>
            `).join('')}
        `;

    } catch (err) {
        container.innerHTML = '<p style="color:var(--danger);font-size:0.8rem">Erro ao conectar ao servidor.</p>';
    }
}

// ========================================================================
// AUTORIZAÃ‡Ã•ES DE RETIRADA - Fase 3 (RF005 / RS002 / RF007)
// ========================================================================

// Popula todos os selects de aluno no dashboard
async function populateStudentSelects() {
    const token = localStorage.getItem('sse_token');
    try {
        const res = await fetch('/api/admin/students?active=true', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!res.ok) return;
        const students = await res.json();

        const selects = [
            'pickup-student-select',
            'pickup-filter-student',
            'exit-student-select',
            'exit-filter-student'
        ];

        selects.forEach(id => {
            const el = document.getElementById(id);
            if (!el) return;
            const hasAll = id === 'exit-filter-student';
            el.innerHTML = hasAll
                ? '<option value="">Todas as Saídas</option>'
                : `<option value="">Selecione o aluno...</option>`;
            students.forEach(s => {
                const opt = document.createElement('option');
                opt.value = s.id;
                opt.textContent = `${s.fullName} - ${s.className}`;
                el.appendChild(opt);
            });
        });

    } catch (err) {
        console.warn('Erro ao popular selects de alunos:', err);
    }
}

function togglePickupForm() {
    const card = document.getElementById('pickup-form-card');
    if (!card) return;
    card.classList.toggle('d-none');
    if (!card.classList.contains('d-none')) {
        document.getElementById('pickup-form').reset();
    }
}

async function savePickup(event) {
    event.preventDefault();
    const studentId = document.getElementById('pickup-student-select').value;
    if (!studentId) {
        showBanner('Selecione o aluno antes de salvar.', 'danger');
        return;
    }

    const payload = {
        studentId: Number(studentId),
        fullName: document.getElementById('pickup-fullname').value.trim(),
        relationship: document.getElementById('pickup-relationship').value.trim(),
        contactPhone: document.getElementById('pickup-phone').value.trim(),
        documentNumber: document.getElementById('pickup-document').value.trim() || null,
        validUntil: document.getElementById('pickup-valid-until').value
    };

    try {
        const response = await fetch('/api/admin/authorizations/pickups', {
            method: 'POST',
            headers: authHeader(),
            body: JSON.stringify(payload)
        });

        const data = await response.json();
        if (!response.ok) {
            showBanner(data.message || 'Erro ao salvar autorização.', 'danger');
            return;
        }

        showBanner(`Autorização para "${data.fullName}" cadastrada com sucesso!`, 'success');
        togglePickupForm();
        // Recarrega a tabela se o aluno selecionado no filtro for o mesmo
        const filterStudent = document.getElementById('pickup-filter-student')?.value;
        if (filterStudent && Number(filterStudent) === Number(studentId)) {
            loadPickupsForSelectedStudent();
        }

    } catch (err) {
        showBanner('Erro na comunicação com o servidor.', 'danger');
    }
}

async function loadPickupsForSelectedStudent() {
    const studentId = document.getElementById('pickup-filter-student')?.value;
    const tbody = document.getElementById('pickups-table-body');
    const auditPanel = document.getElementById('audit-panel');
    if (!tbody) return;

    if (!studentId) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">Selecione um aluno acima para visualizar suas autorizações.</td></tr>';
        if (auditPanel) auditPanel.classList.add('d-none');
        return;
    }

    tbody.innerHTML = '<tr><td colspan="7" class="text-center">Carregando autorizações...</td></tr>';

    try {
        const response = await fetch(`/api/admin/authorizations/students/${studentId}/pickups`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('sse_token')}` }
        });

        if (!response.ok) {
            const data = await response.json();
            tbody.innerHTML = `<tr><td colspan="7" class="text-center" style="color:var(--danger)">${escapeHtml(data.message || 'Erro ao carregar.')}</td></tr>`;
            return;
        }

        const pickups = await response.json();
        if (pickups.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center">Nenhuma pessoa autorizada cadastrada para este aluno.</td></tr>';
            return;
        }

        tbody.innerHTML = pickups.map(p => {
            const badge = pickupStatusBadge(p.status);
            const revokeBtn = p.status !== 'REVOGADA'
                ? `<button class="btn-action-status btn-deactivate" onclick="openPickupStatusModal(${p.id}, 'REVOGADA', '${escapeHtml(p.fullName)}')">Revogar</button>`
                : '';
            const validateBtn = p.status !== 'VALIDA'
                ? `<button class="btn-action-status btn-activate" onclick="openPickupStatusModal(${p.id}, 'VALIDA', '${escapeHtml(p.fullName)}')">Revalidar</button>`
                : '';

            return `
                <tr>
                    <td><strong>${escapeHtml(p.fullName)}</strong></td>
                    <td>${escapeHtml(p.relationship)}</td>
                    <td>${escapeHtml(p.contactPhone)}</td>
                    <td>${escapeHtml(p.documentNumber || '-')}</td>
                    <td>${badge}</td>
                    <td>${p.validUntil ? formatDate(p.validUntil) : '-'}</td>
                    <td>${revokeBtn} ${validateBtn}</td>
                </tr>
            `;
        }).join('');

        // Exibe trilha de auditoria automaticamente ao selecionar aluno
        loadAuditLog(studentId);

    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-center" style="color:var(--danger)">Erro na conexão com o servidor.</td></tr>`;
    }
}

function pickupStatusBadge(status) {
    const labels = { VALIDA: 'VÁLIDA', REVOGADA: 'REVOGADA', VENCIDA: 'VENCIDA' };
    const classes = { VALIDA: 'badge-active', REVOGADA: 'badge-inactive', VENCIDA: 'badge-warning' };
    const label = labels[status] || status;
    const cls = classes[status] || '';
    return `<span class="badge ${cls}">${label}</span>`;
}

// Modal de alteração de status da autorização
function openPickupStatusModal(pickupId, newStatus, pickupName) {
    document.getElementById('modal-pickup-id').value = pickupId;
    document.getElementById('modal-new-status').value = newStatus;
    document.getElementById('modal-status-reason').value = '';

    const actionLabel = newStatus === 'REVOGADA' ? 'Revogar' : 'Revalidar';
    const colorClass = newStatus === 'REVOGADA' ? 'style="color:var(--danger)"' : '';
    document.getElementById('modal-status-title').innerHTML = `${actionLabel} Autorização`;
    document.getElementById('modal-status-desc').innerHTML =
        `Você está prestes a <strong ${colorClass}>${actionLabel.toLowerCase()}</strong> a autorização de <strong>${escapeHtml(pickupName)}</strong>. Informe o motivo.`;

    document.getElementById('modal-pickup-status').classList.remove('d-none');
}

function closePickupStatusModal() {
    document.getElementById('modal-pickup-status').classList.add('d-none');
}

async function submitPickupStatusChange(event) {
    event.preventDefault();
    const pickupId = document.getElementById('modal-pickup-id').value;
    const status = document.getElementById('modal-new-status').value;
    const reason = document.getElementById('modal-status-reason').value.trim();

    try {
        const response = await fetch(`/api/admin/authorizations/pickups/${pickupId}/status`, {
            method: 'PATCH',
            headers: authHeader(),
            body: JSON.stringify({ status, reason })
        });

        const data = await response.json();
        if (!response.ok) {
            showBanner(data.message || 'Erro ao alterar status da autorização.', 'danger');
            return;
        }

        showBanner(`Situação da autorização atualizada para ${data.status === 'VALIDA' ? 'VÁLIDA' : data.status} com sucesso.`, 'success');
        closePickupStatusModal();
        loadPickupsForSelectedStudent();

    } catch (err) {
        showBanner('Erro na comunicação com o servidor.', 'danger');
    }
}

// ========================================================================
// TRILHA DE AUDITORIA - Fase 3 (RF007 / RS004)
// ========================================================================

async function loadAuditLog(studentId) {
    const panel = document.getElementById('audit-panel');
    const tbody = document.getElementById('audit-table-body');
    if (!panel || !tbody) return;

    panel.classList.remove('d-none');
    tbody.innerHTML = '<tr><td colspan="6" class="text-center">Carregando trilha de auditoria...</td></tr>';

    try {
        const response = await fetch(`/api/admin/authorizations/students/${studentId}/audit`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('sse_token')}` }
        });

        if (!response.ok) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center" style="color:var(--danger)">Erro ao carregar trilha de auditoria.</td></tr>';
            return;
        }

        const logs = await response.json();
        if (logs.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center" style="color:var(--text-muted)">Nenhum registro de auditoria para este aluno.</td></tr>';
            return;
        }

        tbody.innerHTML = logs.map(log => {
            const prevStatus = log.previousStatus ? pickupStatusBadge(log.previousStatus) : '<span style="color:var(--text-muted)">-</span>';
            const newStatus = log.newStatus ? pickupStatusBadge(log.newStatus) : '<span style="color:var(--text-muted)">-</span>';
            return `
                <tr>
                    <td style="white-space:nowrap">${formatDateTime(log.createdAt)}</td>
                    <td><code>${escapeHtml(log.action)}</code></td>
                    <td>${prevStatus}</td>
                    <td>${newStatus}</td>
                    <td>${escapeHtml(log.notes || '-')}</td>
                    <td>${escapeHtml(log.operatorName || '-')}</td>
                </tr>
            `;
        }).join('');

    } catch (err) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center" style="color:var(--danger)">Erro na conexão com o servidor.</td></tr>';
    }
}

function closeAuditPanel() {
    const panel = document.getElementById('audit-panel');
    if (panel) panel.classList.add('d-none');
}

// ========================================================================
// SAÍDAS ANTECIPADAS - Fase 3 (RF006 / RS002)
// ========================================================================

function toggleEarlyExitForm() {
    const card = document.getElementById('early-exit-form-card');
    if (!card) return;
    card.classList.toggle('d-none');
    if (!card.classList.contains('d-none')) {
        document.getElementById('early-exit-form').reset();
        const pickupSelect = document.getElementById('exit-pickup-select');
        if (pickupSelect) {
            pickupSelect.innerHTML = '<option value="">Selecione o aluno primeiro...</option>';
            pickupSelect.disabled = true;
        }
    }
}

// Carrega as autorizações VÁLIDAS do aluno selecionado no formulário de saída
async function loadPickupsForEarlyExit() {
    const studentId = document.getElementById('exit-student-select')?.value;
    const pickupSelect = document.getElementById('exit-pickup-select');
    if (!pickupSelect) return;

    if (!studentId) {
        pickupSelect.innerHTML = '<option value="">Selecione o aluno primeiro...</option>';
        pickupSelect.disabled = true;
        return;
    }

    pickupSelect.disabled = true;
    pickupSelect.innerHTML = '<option value="">Carregando...</option>';

    try {
        const response = await fetch(`/api/admin/authorizations/students/${studentId}/pickups`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('sse_token')}` }
        });

        if (!response.ok) {
            pickupSelect.innerHTML = '<option value="">Erro ao carregar autorizações.</option>';
            return;
        }

        const allPickups = await response.json();
        // Filtra apenas VÁLIDAS (RS002 - mas o backend também valida)
        const validPickups = allPickups.filter(p => p.status === 'VALIDA');

        if (validPickups.length === 0) {
            pickupSelect.innerHTML = '<option value="">Nenhuma autorização VÁLIDA cadastrada para este aluno.</option>';
            return;
        }

        pickupSelect.innerHTML = '<option value="">Selecione a pessoa autorizada...</option>';
        validPickups.forEach(p => {
            const opt = document.createElement('option');
            opt.value = p.id;
            opt.textContent = `${p.fullName} (${p.relationship}) - VÁLIDA até ${formatDate(p.validUntil)}`;
            pickupSelect.appendChild(opt);
        });

        pickupSelect.disabled = false;

    } catch (err) {
        pickupSelect.innerHTML = '<option value="">Erro ao conectar ao servidor.</option>';
    }
}

async function saveEarlyExit(event) {
    event.preventDefault();
    const studentId = document.getElementById('exit-student-select').value;
    const authorizedPickupId = document.getElementById('exit-pickup-select').value;
    const reason = document.getElementById('exit-reason').value.trim();

    if (!studentId || !authorizedPickupId) {
        showBanner('Selecione o aluno e a pessoa autorizada antes de confirmar.', 'danger');
        return;
    }

    const payload = {
        studentId: Number(studentId),
        authorizedPickupId: Number(authorizedPickupId),
        reason
    };

    try {
        const response = await fetch('/api/admin/authorizations/early-exits', {
            method: 'POST',
            headers: authHeader(),
            body: JSON.stringify(payload)
        });

        const data = await response.json();
        if (!response.ok) {
            // Exibe mensagem de bloqueio RS002 com destaque
            showBanner(data.message || 'Saída antecipada não autorizada.', 'danger');
            return;
        }

        showBanner(`Saída antecipada de "${data.studentName}" registrada com sucesso.`, 'success');
        toggleEarlyExitForm();
        loadEarlyExits(); showSection('admin-reports-section'); loadAdminReports(); showSection('admin-announcements-section'); loadAdminAnnouncements();

    } catch (err) {
        showBanner('Erro na comunicação com o servidor.', 'danger');
    }
}

async function loadEarlyExits() {
    const tbody = document.getElementById('early-exits-table-body');
    if (!tbody) return;

    const studentId = document.getElementById('exit-filter-student')?.value || '';
    const params = studentId ? `?studentId=${studentId}` : '';

    tbody.innerHTML = '<tr><td colspan="7" class="text-center">Carregando saídas antecipadas...</td></tr>';

    try {
        const response = await fetch(`/api/admin/authorizations/early-exits${params}`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('sse_token')}` }
        });

        if (!response.ok) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-center" style="color:var(--danger)">Erro ao carregar saídas (HTTP ${response.status}).</td></tr>`;
            return;
        }

        const exits = await response.json();
        if (exits.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center" style="color:var(--text-muted)">Nenhuma saída antecipada registrada.</td></tr>';
            return;
        }

        tbody.innerHTML = exits.map(e => `
            <tr>
                <td style="white-space:nowrap">${formatDateTime(e.exitTime)}</td>
                <td><strong>${escapeHtml(e.studentName)}</strong></td>
                <td>${escapeHtml(e.studentClassName)}</td>
                <td><strong>${escapeHtml(e.pickupPersonName)}</strong></td>
                <td>${escapeHtml(e.pickupPersonRelationship)}</td>
                <td>${escapeHtml(e.reason)}</td>
                <td>${escapeHtml(e.authorizedByName)}</td>
            </tr>
        `).join('');

    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-center" style="color:var(--danger)">Erro na conexão com o servidor.</td></tr>`;
    }
}

// ========================================================================
// RBAC TEST - testes das rotas protegidas
// ========================================================================

async function testAccess(url) {
    const token = localStorage.getItem('sse_token');
    const outputBox = document.getElementById('test-result');
    const outputContent = document.getElementById('test-output-content');

    if (!outputBox || !outputContent) return;

    outputBox.classList.remove('d-none');
    outputContent.textContent = 'Testando requisição...';

    try {
        const response = await fetch(url, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await response.json();
        outputContent.textContent = `HTTP ${response.status} ${response.statusText}\n` + JSON.stringify(data, null, 2);
    } catch (err) {
        outputContent.textContent = 'Erro ao realizar a requisição: ' + err.message;
    }
}

// ========================================================================
// AUTH - logout
// ========================================================================

function logout() {
    localStorage.removeItem('sse_token');
    localStorage.removeItem('sse_user');
    window.location.href = '/index.html';
}


function toggleDeviceForm() {
    const card = document.getElementById('device-form-card');
    if (card) card.classList.toggle('d-none');
}

async function saveDevice(e) {
    e.preventDefault();
    const payload = {
        name: document.getElementById('device-name').value.trim(),
        deviceType: document.getElementById('device-type').value,
        location: document.getElementById('device-location').value.trim()
    };
    try {
        const response = await fetch('/api/admin/devices', { method: 'POST', headers: authHeader(), body: JSON.stringify(payload) });
        const data = await response.json();
        if (!response.ok) { showBanner(data.message || 'Erro ao cadastrar dispositivo.', 'danger'); return; }
        document.getElementById('device-form').reset();
        toggleDeviceForm();
        const tokenBox = document.getElementById('new-device-token-box');
        const tokenVal = document.getElementById('new-device-token-value');
        const simTokenInput = document.getElementById('sim-device-token');
        if (tokenBox && tokenVal) { tokenVal.textContent = data.plainToken; tokenBox.classList.remove('d-none'); }
        if (simTokenInput) { simTokenInput.value = data.plainToken; }
        showBanner('Dispositivo cadastrado com sucesso!', 'success');
        loadDevices();
    } catch (err) { showBanner('Erro ao registrar dispositivo.', 'danger'); }
}

async function loadDevices() {
    const tbody = document.getElementById('devices-table-body');
    if (!tbody) return;
    try {
        const response = await fetch('/api/admin/devices', { headers: authHeader() });
        if (!response.ok) return;
        const devices = await response.json();
        if (devices.length === 0) { tbody.innerHTML = '<tr><td colspan="6" class="text-center">Nenhum dispositivo cadastrado.</td></tr>'; return; }
        tbody.innerHTML = devices.map(d => `<tr><td>${d.id}</td><td><strong>${escapeHtml(d.name)}</strong></td><td>${escapeHtml(d.deviceType)}</td><td>${escapeHtml(d.location)}</td><td><span class="badge ${d.active ? 'badge-active' : 'badge-inactive'}">${d.active ? 'Ativo' : 'Inativo'}</span></td><td><button class="btn btn-outline btn-sm" onclick="toggleDeviceStatus(${d.id}, ${!d.active})">${d.active ? 'Desativar' : 'Ativar'}</button></td></tr>`).join('');
    } catch (err) { console.error(err); }
}

async function toggleDeviceStatus(deviceId, newStatus) {
    try {
        const response = await fetch(`/api/admin/devices/${deviceId}/status`, { method: 'PATCH', headers: authHeader(), body: JSON.stringify({ active: newStatus }) });
        if (response.ok) { showBanner('Status atualizado.', 'success'); loadDevices(); }
    } catch (err) { showBanner('Erro ao atualizar status.', 'danger'); }
}

async function simulateTurnstileScan(e) {
    e.preventDefault();
    const deviceToken = document.getElementById('sim-device-token').value.trim();
    const identifier = document.getElementById('sim-badge-code').value.trim();
    const direction = document.getElementById('sim-direction').value;
    const feedback = document.getElementById('sim-feedback');
    try {
        const response = await fetch('/api/devices/access', { method: 'POST', headers: { 'Content-Type': 'application/json', 'X-Device-Token': deviceToken }, body: JSON.stringify({ identifier, direction }) });
        const data = await response.json();
        feedback.classList.remove('d-none', 'alert-success', 'alert-danger');
        if (response.ok && data.status === 'AUTORIZADO') {
            feedback.classList.add('alert-success');
            feedback.textContent = `AUTORIZADO: ${data.studentName} (${data.className || '-'}) - ${data.direction} registrada na ${data.deviceName}.`;
        } else {
            feedback.classList.add('alert-danger');
            feedback.textContent = `NEGADO: ${data.denialReason || data.message || 'Token inválido ou acesso bloqueado.'}`;
        }
        loadAccessLogs();
    } catch (err) {
        feedback.classList.remove('d-none', 'alert-success');
        feedback.classList.add('alert-danger');
        feedback.textContent = 'Erro de conexão com a catraca.';
    }
}

async function loadAccessLogs() {
    const tbody = document.getElementById('access-logs-table-body');
    if (!tbody) return;
    try {
        const response = await fetch('/api/admin/devices/access-logs', { headers: authHeader() });
        if (!response.ok) return;
        const logs = await response.json();
        if (logs.length === 0) { tbody.innerHTML = '<tr><td colspan="7" class="text-center">Nenhum registro ainda.</td></tr>'; return; }
        tbody.innerHTML = logs.map(log => {
            const isAuth = log.status === 'AUTORIZADO';
            return `<tr><td style="white-space:nowrap">${formatDateTime(log.recordedAt)}</td><td><strong>${escapeHtml(log.studentName)}</strong></td><td>${escapeHtml(log.className || '-')}</td><td>${escapeHtml(log.deviceName)}</td><td>${escapeHtml(log.direction)}</td><td><span class="badge ${isAuth ? 'badge-active' : 'badge-inactive'}">${escapeHtml(log.status)}</span></td><td>${escapeHtml(log.denialReason || 'Acesso liberado normalmente')}</td></tr>`;
        }).join('');
    } catch (err) { console.error(err); }
}

// ========================================================================
// FASE 5 - ALERTAS DE SEGURANÇA (ADMIN) E NOTIFICAÇÕES (RESPONSÁVEL)
// ========================================================================

async function loadSecurityAlerts() {
    const tbody = document.getElementById('security-alerts-table-body');
    if (!tbody) return;
    try {
        const response = await fetch('/api/admin/alerts', { headers: authHeader() });
        if (!response.ok) return;
        const alerts = await response.json();
        if (alerts.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center">Nenhum alerta de segurança registrado.</td></tr>';
            return;
        }
        tbody.innerHTML = alerts.map(a => {
            const sevClass = a.severity === 'ALTA' ? 'badge-inactive' : (a.severity === 'MEDIA' ? 'badge-warning' : 'badge-active');
            const isOpen = a.status === 'ABERTO';
            const statusBadge = isOpen
                ? '<span class="badge badge-inactive">ABERTO</span>'
                : '<span class="badge badge-active">RESOLVIDO</span>';
            const actionHtml = isOpen
                ? `<button class="btn btn-primary btn-sm" onclick="resolveSecurityAlert(${a.id})">Resolver</button>`
                : `<small style="color:var(--text-muted)">${escapeHtml(a.resolutionNotes || '')}<br><em>Por: ${escapeHtml(a.resolvedByEmail || '-')}</em></small>`;
            return `
                <tr>
                    <td style="white-space:nowrap">${formatDateTime(a.createdAt)}</td>
                    <td><span class="badge ${sevClass}">${escapeHtml(a.severity)}</span></td>
                    <td><code>${escapeHtml(a.category)}</code></td>
                    <td><strong>${escapeHtml(a.studentName)}</strong><br><small>${escapeHtml(a.deviceName)}</small></td>
                    <td>${escapeHtml(a.description)}</td>
                    <td>${statusBadge}</td>
                    <td>${actionHtml}</td>
                </tr>
            `;
        }).join('');
    } catch (err) {
        console.error('Erro ao carregar alertas:', err);
    }
}

async function resolveSecurityAlert(alertId) {
    const notes = prompt('Informe a tratativa ou justificativa para encerrar este alerta de segurança:');
    if (!notes || !notes.trim()) return;
    try {
        const response = await fetch(`/api/admin/alerts/${alertId}/resolve`, {
            method: 'PATCH',
            headers: authHeader(),
            body: JSON.stringify({ resolutionNotes: notes.trim() })
        });
        if (!response.ok) {
            const data = await response.json();
            showBanner(data.message || 'Erro ao resolver alerta.', 'danger');
            return;
        }
        showBanner('Alerta de segurança encerrado com sucesso!', 'success');
        loadSecurityAlerts();
    } catch (err) {
        showBanner('Erro de comunicação ao resolver alerta.', 'danger');
    }
}

async function loadGuardianNotifications() {
    const listEl = document.getElementById('guardian-notifications-list');
    const badgeEl = document.getElementById('guardian-unread-badge');
    if (!listEl) return;
    try {
        const [listRes, countRes] = await Promise.all([
            fetch('/api/notifications', { headers: authHeader() }),
            fetch('/api/notifications/unread-count', { headers: authHeader() })
        ]);
        if (countRes.ok && badgeEl) {
            const countData = await countRes.json();
            const count = countData.unreadCount || 0;
            badgeEl.textContent = count === 1 ? '1 não lida' : `${count} não lidas`;
            badgeEl.className = count > 0 ? 'badge badge-inactive' : 'badge badge-active';
        }
        if (!listRes.ok) return;
        const notifications = await listRes.json();
        if (notifications.length === 0) {
            listEl.innerHTML = '<p style="color:var(--text-muted)">Nenhuma notificação registrada até o momento.</p>';
            return;
        }
        listEl.innerHTML = notifications.map(n => `
            <div class="student-card" style="border-left: 4px solid ${n.read ? '#cbd5e1' : '#2563eb'}; opacity: ${n.read ? '0.82' : '1'};">
                <div style="display:flex; justify-content:space-between; align-items:flex-start; gap:0.5rem;">
                    <h3 style="margin:0; font-size:1rem;">${escapeHtml(n.title)}</h3>
                    <span class="badge ${n.read ? 'badge-active' : 'badge-inactive'}">${n.read ? 'Lida' : 'Nova'}</span>
                </div>
                <p style="margin:0.5rem 0; font-size:0.9rem;">${escapeHtml(n.message)}</p>
                <div style="display:flex; justify-content:space-between; align-items:center; margin-top:0.5rem;">
                    <small style="color:var(--text-muted)">${formatDateTime(n.createdAt)}</small>
                    ${!n.read ? `<button class="btn btn-outline btn-sm" onclick="markNotificationRead(${n.id})">Marcar como lida</button>` : ''}
                </div>
            </div>
        `).join('');
    } catch (err) {
        console.error('Erro ao carregar notificações:', err);
    }
}

async function markNotificationRead(id) {
    try {
        const response = await fetch(`/api/notifications/${id}/read`, {
            method: 'PATCH',
            headers: authHeader()
        });
        if (response.ok) loadGuardianNotifications();
    } catch (err) {
        console.error(err);
    }
}

async function markAllNotificationsRead() {
    try {
        const response = await fetch('/api/notifications/read-all', {
            method: 'PATCH',
            headers: authHeader()
        });
        if (response.ok) {
            showBanner('Todas as notificações foram marcadas como lidas.', 'success');
            loadGuardianNotifications();
        }
    } catch (err) {
        console.error(err);
    }
}
// =========================================================
// FASE 6A: INDICADORES GERENCIAIS E RELATÓRIOS CSV
// =========================================================
async function loadAdminReports() {
    await Promise.all([loadReportSummary(), loadAccessReportTable()]);
}

async function loadReportSummary() {
    try {
        const res = await fetch('/api/admin/reports/summary', { headers: authHeader() });
        if (!res.ok) return;
        const m = await res.json();
        const setTxt = (id, val) => { const el = document.getElementById(id); if (el) el.textContent = val; };
        setTxt('metric-inside', m.studentsCurrentlyInside);
        setTxt('metric-active-students', m.totalActiveStudents);
        setTxt('metric-today-entries', m.todayAuthorizedEntries);
        setTxt('metric-today-exits', m.todayAuthorizedExits);
        setTxt('metric-early-exits', m.totalEarlyExits);
        setTxt('metric-open-alerts', m.openSecurityAlerts);
    } catch (e) {
        console.error('Erro ao carregar resumo gerencial:', e);
    }
}

function buildReportQueryParams() {
    const params = new URLSearchParams();
    const cls = document.getElementById('report-filter-class')?.value?.trim();
    const dir = document.getElementById('report-filter-direction')?.value;
    const st = document.getElementById('report-filter-status')?.value;
    if (cls) params.append('className', cls);
    if (dir) params.append('direction', dir);
    if (st) params.append('status', st);
    const qs = params.toString();
    return qs ? `?${qs}` : '';
}

async function loadAccessReportTable() {
    const tbody = document.getElementById('admin-reports-tbody');
    if (!tbody) return;
    try {
        const res = await fetch('/api/admin/reports/access-logs' + buildReportQueryParams(), { headers: authHeader() });
        if (!res.ok) return;
        const items = await res.json();
        if (items.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center">Nenhum registro encontrado para os filtros aplicados.</td></tr>';
            return;
        }
        tbody.innerHTML = items.map(r => `
            <tr>
                <td>${formatDateTime(r.recordedAt)}</td>
                <td><strong>${escapeHtml(r.studentName)}</strong></td>
                <td>${escapeHtml(r.className)}</td>
                <td><code>${escapeHtml(r.badgeCode)}</code></td>
                <td>${escapeHtml(r.deviceName)}</td>
                <td>${escapeHtml(r.direction)}</td>
                <td><span class="badge ${r.validationStatus === 'AUTORIZADO' ? 'badge-active' : 'badge-inactive'}">${escapeHtml(r.validationStatus)}</span></td>
                <td>${escapeHtml(r.denialReason)}</td>
            </tr>
        `).join('');
    } catch (e) {
        console.error('Erro ao carregar relatório:', e);
    }
}

async function exportAccessReportCsv() {
    try {
        const res = await fetch('/api/admin/reports/access-logs/export.csv' + buildReportQueryParams(), { headers: authHeader() });
        if (!res.ok) {
            showBanner('Não foi possível exportar o relatório CSV.', 'error');
            return;
        }
        const blob = await res.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'relatorio-acessos-escolares.csv';
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
        showBanner('Relatório CSV exportado com sucesso!', 'success');
    } catch (e) {
        console.error(e);
        showBanner('Erro de comunicação ao exportar CSV.', 'error');
    }
}
// =========================================================
// FASE 6B: COMUNICADOS ESCOLARES E PORTAL DO ALUNO (RF010)
// =========================================================
function renderAnnouncementCards(items, containerId) {
    const el = document.getElementById(containerId);
    if (!el) return;
    if (!items || items.length === 0) {
        el.innerHTML = '<p>Nenhum comunicado publicado no momento.</p>';
        return;
    }
    el.innerHTML = items.map(a => `
        <div class="student-card" style="padding:1rem;">
            <div style="display:flex; justify-content:space-between; align-items:center; gap:0.5rem; margin-bottom:0.4rem;">
                <span class="badge badge-active">${escapeHtml(a.category)}</span>
                <small style="color:var(--text-muted)">${formatDateTime(a.createdAt)}</small>
            </div>
            <h4 style="margin:0.35rem 0;">${escapeHtml(a.title)}</h4>
            <p style="margin:0.4rem 0 0.65rem; line-height:1.45;">${escapeHtml(a.content)}</p>
            <small style="color:var(--text-muted)">Publicado por: <strong>${escapeHtml(a.authorName)}</strong></small>
        </div>
    `).join('');
}

async function loadAdminAnnouncements() {
    try {
        const res = await fetch('/api/admin/announcements', { headers: authHeader() });
        if (!res.ok) return;
        renderAnnouncementCards(await res.json(), 'admin-announcements-list');
    } catch (e) {
        console.error('Erro ao carregar comunicados admin:', e);
    }
}

async function createSchoolAnnouncement(event) {
    event.preventDefault();
    const title = document.getElementById('ann-title')?.value?.trim();
    const category = document.getElementById('ann-category')?.value || 'GERAL';
    const content = document.getElementById('ann-content')?.value?.trim();
    if (!title || !content) return;

    try {
        const res = await fetch('/api/admin/announcements', {
            method: 'POST',
            headers: { ...authHeader(), 'Content-Type': 'application/json' },
            body: JSON.stringify({ title, category, content })
        });
        if (!res.ok) {
            showBanner('Não foi possível publicar o comunicado.', 'error');
            return;
        }
        document.getElementById('announcement-form')?.reset();
        showBanner('Comunicado publicado com sucesso!', 'success');
        await loadAdminAnnouncements();
    } catch (e) {
        console.error(e);
        showBanner('Erro de comunicação ao publicar comunicado.', 'error');
    }
}

async function loadGuardianAnnouncements() {
    try {
        const res = await fetch('/api/guardian/announcements', { headers: authHeader() });
        if (!res.ok) return;
        renderAnnouncementCards(await res.json(), 'guardian-announcements-list');
    } catch (e) {
        console.error('Erro ao carregar comunicados do responsável:', e);
    }
}

async function loadStudentPortal() {
    try {
        const res = await fetch('/api/student/dashboard', { headers: authHeader() });
        if (!res.ok) return;
        const data = await res.json();

        const profileCard = document.getElementById('student-profile-card');
        if (profileCard) {
            profileCard.innerHTML = `
                <div style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:1rem;">
                    <div>
                        <h3 style="margin:0 0 0.35rem;">${escapeHtml(data.fullName)}</h3>
                        <p style="margin:0.2rem 0;"><strong>Turma:</strong> ${escapeHtml(data.className)}</p>
                        <p style="margin:0.2rem 0;"><strong>E-mail Institucional:</strong> ${escapeHtml(data.userEmail)}</p>
                        <p style="margin:0.2rem 0;"><strong>Crachá / RFID:</strong> <code>${escapeHtml(data.badgeCode)}</code></p>
                    </div>
                    <div>
                        <span class="badge ${data.active ? 'badge-active' : 'badge-inactive'}">
                            ${data.active ? 'MATRÍCULA ATIVA' : 'ACESSO BLOQUEADO'}
                        </span>
                    </div>
                </div>
            `;
        }

        const logsTbody = document.getElementById('student-own-logs-tbody');
        if (logsTbody) {
            if (!data.recentAccessLogs || data.recentAccessLogs.length === 0) {
                logsTbody.innerHTML = '<tr><td colspan="5" class="text-center">Nenhum registro de catraca encontrado ainda.</td></tr>';
            } else {
                logsTbody.innerHTML = data.recentAccessLogs.map(l => `
                    <tr>
                        <td>${formatDateTime(l.recordedAt)}</td>
                        <td><strong>${escapeHtml(l.direction)}</strong></td>
                        <td><span class="badge ${l.validationStatus === 'AUTORIZADO' ? 'badge-active' : 'badge-inactive'}">${escapeHtml(l.validationStatus)}</span></td>
                        <td>${escapeHtml(l.deviceName)}</td>
                        <td>${escapeHtml(l.denialReason)}</td>
                    </tr>
                `).join('');
            }
        }

        renderAnnouncementCards(data.announcements, 'student-announcements-list');
    } catch (e) {
        console.error('Erro ao carregar Portal do Aluno:', e);
    }
}