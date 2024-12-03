let stompClient = null;

function connect() {
    try {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.debug = null;
        stompClient.connect({}, function(frame) {
            console.log('Connected: ' + frame);
            stompClient.subscribe('/user/queue/notifications', function(notification) {
                showNotification(JSON.parse(notification.body));
            });
            loadNotifications();
        }, function(error) {
            console.error('WebSocket连接失败:', error);
            $('#notificationCount').hide();
        });
    } catch (error) {
        console.error('WebSocket初始化失败:', error);
        $('#notificationCount').hide();
    }
}

function showNotification(notification) {
    toastr.info(notification.message);
    updateNotificationCount();
    loadNotifications();
}

function loadFiles(type) {
    console.log('Loading files of type:', type);
    const fileList = $('#fileList');
    fileList.html('<div class="text-center"><div class="spinner-border" role="status"></div></div>');
    
    const url = `/api/files/${type}`;
    console.log('Making request to:', url);
    
    $.get(url, function(files) {
        console.log('Received files:', files);
        fileList.empty();
        
        if (files.length === 0) {
            console.log('No files found');
            fileList.append(`
                <div class="alert alert-info">
                    暂无文件
                </div>
            `);
            return;
        }
        
        console.log('Rendering', files.length, 'files');
        files.forEach(file => {
            console.log('Processing file:', file);
            fileList.append(`
                <div class="card mb-2">
                    <div class="card-body d-flex justify-content-between align-items-center">
                        <div>
                            <h6 class="mb-0">${file.originalFileName}</h6>
                            <small class="text-muted">
                                大小: ${formatFileSize(file.fileSize)} | 
                                上传时间: ${formatDate(file.uploadTime)} |
                                上传者: ${file.uploaderName}
                            </small>
                        </div>
                        <div class="btn-group">
                            <button class="btn btn-sm btn-primary" onclick="downloadFile(${file.id})">
                                <i class="bi bi-download"></i> 下载
                            </button>
                            <button class="btn btn-sm btn-success" onclick="showShareModal(${file.id})">
                                <i class="bi bi-share"></i> 分享
                            </button>
                            ${file.uploaderId === currentUserId ? `
                                <button class="btn btn-sm btn-danger" onclick="deleteFile(${file.id})">
                                    <i class="bi bi-trash"></i> 删除
                                </button>
                            ` : ''}
                        </div>
                    </div>
                </div>
            `);
        });
    }).fail(function(error) {
        console.error('Failed to load files:', error);
        console.error('Error details:', error.responseText);
        fileList.html(`
            <div class="alert alert-danger">
                加载文件失败: ${error.responseText || '未知错误'}
            </div>
        `);
    });
}

function loadNotifications() {
    if (!stompClient || !stompClient.connected) {
        console.log('WebSocket not connected, skipping notification load');
        return;
    }
    
    $.get('/api/notifications', function(notifications) {
        const notificationList = $('#notificationList');
        notificationList.empty();
        
        notifications.forEach(notification => {
            notificationList.append(`
                <div class="alert alert-info">
                    <p class="mb-1">${notification.message}</p>
                    <small class="text-muted">${formatDate(notification.createTime)}</small>
                    ${!notification.read ? `
                        <button class="btn btn-sm btn-link" onclick="markAsRead(${notification.id})">
                            标记为已读
                        </button>
                    ` : ''}
                </div>
            `);
        });
        
        updateNotificationCount();
    }).fail(function(error) {
        console.error('Failed to load notifications:', error);
    });
}

function updateNotificationCount() {
    $.get('/api/notifications/unread/count', function(count) {
        const badge = $('#notificationCount');
        badge.text(count);
        badge.toggle(count > 0);
    });
}

function uploadFile() {
    const formData = new FormData();
    const fileInput = $('#file')[0];
    const isPublic = $('#isPublic').is(':checked');

    if (fileInput.files.length === 0) {
        toastr.warning('请选择要上传的文件');
        return;
    }

    formData.append('file', fileInput.files[0]);
    formData.append('isPublic', isPublic);

    $.ajax({
        url: '/api/files/upload',
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        dataType: 'json',
        success: function(response) {
            if (response.error) {
                toastr.error(response.error);
            } else {
                toastr.success(response.message || '文件上传成功');
            }
            $('#uploadModal').modal('hide');
            $('#uploadForm')[0].reset();
            loadFiles('my');
        },
        error: function(xhr) {
            if (xhr.status === 403) {
                toastr.error('您没有上传权限');
            } else {
                try {
                    const response = JSON.parse(xhr.responseText);
                    toastr.error(response.error || '上传失败');
                } catch (e) {
                    toastr.error('上传失败: ' + (xhr.responseText || '未知错误'));
                }
            }
            $('#uploadModal').modal('hide');
        }
    });
}

function downloadFile(fileId) {
    $.ajax({
        url: `/api/files/download/${fileId}`,
        method: 'GET',
        xhrFields: {
            responseType: 'blob'
        },
        success: function(response, status, xhr) {
            const contentType = xhr.getResponseHeader('content-type');
            if(contentType && contentType.indexOf('application/json') !== -1) {
                const reader = new FileReader();
                reader.onload = function() {
                    const error = JSON.parse(this.result);
                    toastr.error(error.error);
                };
                reader.readAsText(response);
                return;
            }
            
            // 处理文件下载
            const disposition = xhr.getResponseHeader('Content-Disposition');
            let filename = 'download';
            if (disposition && disposition.indexOf('attachment') !== -1) {
                const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
                const matches = filenameRegex.exec(disposition);
                if (matches != null && matches[1]) {
                    filename = decodeURIComponent(matches[1].replace(/['"]/g, ''));
                }
            }
            
            const url = window.URL.createObjectURL(response);
            const a = document.createElement('a');
            a.href = url;
            a.download = filename;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
        },
        error: function(xhr) {
            if (xhr.status === 403) {
                toastr.error('您没有下载权限');
            } else {
                toastr.error('下载失败: ' + (xhr.responseText || '未知错误'));
            }
        }
    });
}

function deleteFile(fileId) {
    if (confirm('确定要删除此文件吗？')) {
        $.ajax({
            url: `/api/files/${fileId}`,
            type: 'DELETE',
            success: function() {
                loadFiles('my');
                alert('文件删除成功');
            },
            error: function() {
                alert('文件删除失败');
            }
        });
    }
}

function markAsRead(notificationId) {
    $.ajax({
        url: `/api/notifications/${notificationId}/read`,
        type: 'PUT',
        success: function() {
            loadNotifications();
        }
    });
}

function formatFileSize(size) {
    if (!size) return '未知';
    const units = ['B', 'KB', 'MB', 'GB', 'TB'];
    let i = 0;
    while (size >= 1024 && i < units.length - 1) {
        size /= 1024;
        i++;
    }
    return Math.round(size * 100) / 100 + ' ' + units[i];
}

function formatDate(dateStr) {
    if (!dateStr) return '未知';
    const date = new Date(dateStr);
    return date.getFullYear() + '/' + 
           (date.getMonth() + 1).toString().padStart(2, '0') + '/' + 
           date.getDate().toString().padStart(2, '0') + ' ' +
           date.getHours().toString().padStart(2, '0') + ':' +
           date.getMinutes().toString().padStart(2, '0') + ':' +
           date.getSeconds().toString().padStart(2, '0');
}

function searchFiles() {
    const keyword = $('#searchInput').val().trim();
    if (keyword) {
        $.get(`/api/files/search?keyword=${encodeURIComponent(keyword)}`, function(files) {
            renderFiles(files);
        });
    }
}

function previewFile(fileId) {
    $.ajax({
        url: `/api/files/preview/${fileId}`,
        type: 'GET',
        success: function(response, status, xhr) {
            const contentType = xhr.getResponseHeader('content-type');
            const previewContent = $('#previewContent');
            previewContent.empty();
            
            if (contentType.startsWith('image/')) {
                previewContent.html(`<img src="/api/files/preview/${fileId}" alt="预览图">`);
            } else if (contentType === 'application/pdf') {
                previewContent.html(`
                    <iframe src="/api/files/preview/${fileId}" style="width:100%;height:70vh;border:none;">
                        您的浏览器不支持PDF预览
                    </iframe>
                `);
            } else if (contentType.startsWith('text/')) {
                previewContent.html(`<pre>${response}</pre>`);
            } else {
                previewContent.html('<div class="alert alert-warning">不支持预览此类型的文件</div>');
            }
            
            $('#previewModal').modal('show');
        },
        error: function() {
            alert('文件预览失败');
        }
    });
}

function getFileIcon(fileType) {
    if (!fileType) return '<i class="bi bi-file-earmark file-type-default"></i>';
    
    if (fileType.startsWith('image/')) {
        return '<i class="bi bi-file-earmark-image file-type-image"></i>';
    } else if (fileType === 'application/pdf') {
        return '<i class="bi bi-file-earmark-pdf file-type-pdf"></i>';
    } else if (fileType.startsWith('text/') || 
               fileType === 'application/json' || 
               fileType === 'application/xml') {
        return '<i class="bi bi-file-earmark-text file-type-text"></i>';
    } else if (fileType.includes('zip') || fileType.includes('rar') || 
               fileType.includes('tar') || fileType.includes('7z')) {
        return '<i class="bi bi-file-earmark-zip file-type-archive"></i>';
    }
    
    return '<i class="bi bi-file-earmark file-type-default"></i>';
}

function renderFiles(files) {
    const fileList = $('#fileList');
    fileList.empty();
    
    if (files.length === 0) {
        fileList.append(`
            <div class="alert alert-info">
                没有找到相关文件
            </div>
        `);
        return;
    }
    
    files.forEach(file => {
        fileList.append(`
            <div class="card mb-2">
                <div class="card-body d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="mb-0">${file.originalFileName}</h6>
                        <small class="text-muted">
                            大小: ${formatFileSize(file.fileSize)} | 
                            上传时间: ${formatDate(file.uploadTime)} |
                            上传者: ${file.uploaderName}
                        </small>
                    </div>
                    <div class="btn-group">
                        <button class="btn btn-sm btn-secondary" onclick="previewFile(${file.id})">
                            <i class="bi bi-eye"></i> 预览
                        </button>
                        <button class="btn btn-sm btn-primary" onclick="downloadFile(${file.id})">
                            <i class="bi bi-download"></i> 下载
                        </button>
                        ${file.uploaderId === currentUserId ? `
                            <button class="btn btn-sm btn-danger" onclick="deleteFile(${file.id})">
                                <i class="bi bi-trash"></i> 删除
                            </button>
                            <button class="btn btn-sm btn-info" onclick="showShareModal(${file.id})">
                                <i class="bi bi-share"></i> 分享
                            </button>
                        ` : ''}
                    </div>
                </div>
            </div>
        `);
    });
}

function editFile(fileId) {
    $.get(`/api/files/${fileId}`, function(file) {
        $('#editFileName').val(file.fileName);
        $('#editIsPublic').prop('checked', file.isPublic);
        $('#editFileId').val(fileId);
        $('#editFileModal').modal('show');
    });
}

function shareFile() {
    const fileId = $('#shareFileId').val();
    const userIds = [];
    
    $('input[name="userIds"]:checked').each(function() {
        userIds.push($(this).val());
    });
    
    if (userIds.length === 0) {
        toastr.warning('请选择至少一个用户');
        return;
    }
    
    $.ajax({
        url: `/api/files/${fileId}/share`,
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(userIds),
        success: function(response) {
            if (response.error) {
                toastr.error(response.error);
            } else {
                $('#shareModal').modal('hide');
                toastr.success(response.message || '文件分享成功');
            }
        },
        error: function(xhr) {
            if (xhr.status === 403) {
                toastr.error('您没有分享权限');
                $('#shareModal').modal('hide');
            } else {
                toastr.error('分享失败: ' + (xhr.responseText || '未知错误'));
            }
        }
    });
}

function showShareModal(fileId) {
    $('#shareFileId').val(fileId);
    
    // 加载用户列表
    $.get('/api/users/list', function(users) {
        let html = '';
        users.forEach(user => {
            if (user.id !== currentUserId) {  // 不显示当前用户
                html += `
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" 
                            name="userIds" value="${user.id}" id="user${user.id}">
                        <label class="form-check-label" for="user${user.id}">
                            ${user.username}
                        </label>
                    </div>
                `;
            }
        });
        $('#userList').html(html);
        $('#shareModal').modal('show');
    });
}

function loadUsers() {
    $.get('/api/users', function(users) {
        const userList = $('#userList');
        userList.empty();
        
        users.forEach(user => {
            if (user.id !== currentUserId) {
                userList.append(`
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" value="${user.id}" name="userIds">
                        <label class="form-check-label">
                            ${user.username}
                        </label>
                    </div>
                `);
            }
        });
    });
}

function updatePermission(userId, permission) {
    const checkbox = event.target;
    const originalState = checkbox.checked;
    
    $.ajax({
        url: `/admin/users/${userId}/permissions`,
        type: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify({
            id: userId,
            canUpload: permission === 'canUpload' ? checkbox.checked : undefined,
            canDownload: permission === 'canDownload' ? checkbox.checked : undefined,
            canShare: permission === 'canShare' ? checkbox.checked : undefined
        }),
        success: function(response) {
            if (response && typeof response === 'object' && !response.success) {
                toastr.error(response.error || '更新失败');
                checkbox.checked = originalState;
            } else {
                toastr.success(response.message || '权限更新成功');
            }
        },
        error: function(xhr) {
            let errorMsg = '权限更新失败';
            try {
                const response = JSON.parse(xhr.responseText);
                if (response && response.error) {
                    errorMsg = response.error;
                }
            } catch (e) {
                console.error('Error parsing response:', e);
            }
            toastr.error(errorMsg);
            checkbox.checked = originalState;
        }
    });
}

$(document).ready(function() {
    // 初始化 toastr
    toastr.options = {
        "closeButton": true,
        "positionClass": "toast-top-right",
        "timeOut": "3000",
        "progressBar": true
    };
    
    if (typeof currentUserId === 'undefined') {
        console.error('currentUserId not set');
        return;
    }
    
    console.log('Current user ID:', currentUserId);
    
    // 初始载公共文件
    loadFiles('public');
    
    // 尝试建立WebSocket连接
    connect();
    
    // 为导航链接添加点击事件
    $('#publicFiles').click(function(e) {
        e.preventDefault();
        console.log('Clicked public files');
        $(this).addClass('active');
        $('#myFiles').removeClass('active');
        loadFiles('public');
    });
    
    $('#myFiles').click(function(e) {
        e.preventDefault();
        console.log('Clicked my files');
        $(this).addClass('active');
        $('#publicFiles').removeClass('active');
        loadFiles('my');
    });
    
    $('#uploadBtn').click(uploadFile);
    $('#searchBtn').click(searchFiles);
    $('#shareBtn').click(shareFile);
    
    $('#searchInput').on('keypress', function(e) {
        if (e.which === 13) {
            searchFiles();
        }
    });
    
    $('#editFileForm').submit(function(e) {
        e.preventDefault();
        const fileId = $('#editFileId').val();
        const data = {
            fileName: $('#editFileName').val(),
            isPublic: $('#editIsPublic').is(':checked')
        };
        
        $.ajax({
            url: `/api/files/${fileId}`,
            type: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(data),
            success: function() {
                $('#editFileModal').modal('hide');
                toastr.success('文件更新成功');
                loadFiles('my');
            },
            error: function(xhr) {
                toastr.error('文件更新失败: ' + xhr.responseText);
            }
        });
    });
    
    $('#shareFileForm').submit(function(e) {
        e.preventDefault();
        const fileId = $('#shareFileId').val();
        const userIds = [];
        $('input[name="shareUsers"]:checked').each(function() {
            userIds.push($(this).val());
        });
        
        if (userIds.length === 0) {
            toastr.warning('请选择至少一个用户');
            return;
        }
        
        $.ajax({
            url: `/api/files/${fileId}/share`,
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(userIds),
            success: function() {
                $('#shareFileModal').modal('hide');
                toastr.success('文件分享成功');
            },
            error: function(xhr) {
                toastr.error('文件分享失败: ' + xhr.responseText);
            }
        });
    });
}); 