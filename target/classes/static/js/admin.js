$(document).ready(function() {
    loadUsers();
    loadLogs();
    loadStats();
    
    // 导航切换
    $('.nav-link').click(function(e) {
        e.preventDefault();
        $('.nav-link').removeClass('active');
        $(this).addClass('active');
        
        const target = $(this).data('bs-target');
        $('.content-section').addClass('d-none');
        $(target).removeClass('d-none');
    });
    
    // 创建用户
    $('#createUserBtn').click(createUser);
    
    // 日志搜索
    $('#logSearch').on('input', debounce(searchLogs, 500));
});

function loadUsers() {
    $.get('/api/admin/users', function(users) {
        const userList = $('#userList');
        userList.empty();
        
        users.forEach(user => {
            userList.append(`
                <tr>
                    <td>${user.id}</td>
                    <td>${user.username}</td>
                    <td>${user.email}</td>
                    <td>
                        <span class="badge ${user.isEnabled ? 'bg-success' : 'bg-danger'}">
                            ${user.isEnabled ? '正常' : '禁用'}
                        </span>
                    </td>
                    <td>${formatDate(user.createTime)}</td>
                    <td>
                        ${user.isEnabled ? 
                            `<button class="btn btn-sm btn-warning" onclick="disableUser(${user.id})">禁用</button>` :
                            `<button class="btn btn-sm btn-success" onclick="enableUser(${user.id})">启用</button>`
                        }
                        <button class="btn btn-sm btn-danger" onclick="deleteUser(${user.id})">删除</button>
                    </td>
                </tr>
            `);
        });
    });
}

function loadLogs() {
    $.get('/api/admin/logs', function(logs) {
        renderLogs(logs);
    });
}

function loadStats() {
    $.get('/api/admin/stats', function(stats) {
        $('#userStats').html(`
            总用户数: ${stats.totalUsers}<br>
            活跃用户: ${stats.activeUsers}<br>
            今日新增: ${stats.newUsers}
        `);
        
        $('#fileStats').html(`
            总文件数: ${stats.totalFiles}<br>
            总存储量: ${formatFileSize(stats.totalStorage)}<br>
            今日上传: ${stats.todayUploads}
        `);
        
        $('#systemStatus').html(`
            CPU使用率: ${stats.cpuUsage}%<br>
            内存使用: ${formatFileSize(stats.memoryUsed)}/${formatFileSize(stats.totalMemory)}<br>
            运行时间: ${formatDuration(stats.uptime)}
        `);
    });
}

function createUser() {
    const formData = new FormData($('#createUserForm')[0]);
    const user = {
        username: formData.get('username'),
        password: formData.get('password'),
        email: formData.get('email'),
        isAdmin: formData.get('isAdmin') === 'on'
    };
    
    $.ajax({
        url: '/api/admin/users',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(user),
        success: function() {
            $('#createUserModal').modal('hide');
            $('#createUserForm')[0].reset();
            loadUsers();
            alert('用户创建成功');
        },
        error: function(xhr) {
            alert('创建失败: ' + xhr.responseText);
        }
    });
}

function disableUser(userId) {
    if (confirm('确定要禁用此用户吗？')) {
        $.ajax({
            url: `/api/admin/users/${userId}/disable`,
            type: 'PUT',
            success: function() {
                loadUsers();
            }
        });
    }
}

function enableUser(userId) {
    $.ajax({
        url: `/api/admin/users/${userId}/enable`,
        type: 'PUT',
        success: function() {
            loadUsers();
        }
    });
}

function deleteUser(userId) {
    if (confirm('确定要删除此用户吗？此操作不可恢复！')) {
        $.ajax({
            url: `/api/admin/users/${userId}`,
            type: 'DELETE',
            success: function() {
                loadUsers();
            }
        });
    }
}

function searchLogs(keyword) {
    $.get(`/api/admin/logs/search?keyword=${encodeURIComponent(keyword)}`, function(logs) {
        renderLogs(logs);
    });
}

function renderLogs(logs) {
    const logList = $('#logList');
    logList.empty();
    
    logs.forEach(log => {
        logList.append(`
            <tr>
                <td>${formatDate(log.operationTime)}</td>
                <td>${log.username}</td>
                <td>${log.operation}</td>
                <td>${log.details}</td>
                <td>${log.ipAddress}</td>
            </tr>
        `);
    });
}

function formatDate(dateString) {
    return new Date(dateString).toLocaleString();
}

function formatFileSize(bytes) {
    const units = ['B', 'KB', 'MB', 'GB', 'TB'];
    let size = bytes;
    let unitIndex = 0;
    while (size >= 1024 && unitIndex < units.length - 1) {
        size /= 1024;
        unitIndex++;
    }
    return `${size.toFixed(2)} ${units[unitIndex]}`;
}

function formatDuration(seconds) {
    const days = Math.floor(seconds / 86400);
    const hours = Math.floor((seconds % 86400) / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    return `${days}天 ${hours}小时 ${minutes}分钟`;
}

function debounce(func, wait) {
    let timeout;
    return function() {
        const context = this;
        const args = arguments;
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(context, args), wait);
    };
} 